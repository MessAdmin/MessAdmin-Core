/**
 *
 */
package clime.messadmin.profiler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.jsp.JspWriter;

import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.utils.DateUtils;
import clime.messadmin.utils.StringUtils;

/**
 * Implementation note: this class intentionally does not implement a loop for sampling,
 * thus preventing continuous sampling when the user has left the monitoring JSP page...
 *
 * @author C&eacute;drik LIME
 */
public class SamplingProfiler implements java.io.Closeable {
	private static final String BUNDLE_NAME = SamplingProfiler.class.getName();

	/**
	 * Maximum number of StackTraceElement to hold into memory
	 */
	private static final int MAX_ELEMENTS = 10000;
	/**
	 * Avoid flooding the out stream with ticks... :-)
	 */
	private static final long MIN_TICK_DELAY_MS = 2000;//milliseconds

	private final Set<String> ignoredThreads = new HashSet<String>();
	private final Set<String> ignoredPackagesInStackTrace = new HashSet<String>();

	// can not store StackTraceElement[] as key, as hashCode() will not work correctly...
	private final Map<String, Integer> traces = new HashMap<String, Integer>();
	/**
	 * Minimum count for a {@code StackTraceElement} to be kept in memory,
	 * so that {@link SamplingProfiler#traces} stays under {@link #MAX_ELEMENTS} elements
	 */
	private int minCount = 0;
	/**
	 * Total number of {@code StackTraceElement} analyzed
	 * (i.e. after ignoring {@link #ignoredThreads} and {@link #ignoredPackagesInStackTrace})
	 */
	private long totalStackTracesAnalysed = 0;
	/**
	 * Avoid flooding the out stream with ticks... :-)
	 */
	private long lastTickTime = 0;
	private long startDate;
	private long endDate;

	/**
	 *
	 */
	public SamplingProfiler() {
		super();
	}

	public void profileSample(JspWriter out) {
		printTick(out);
		if (startDate == 0) {
			startDate = System.currentTimeMillis();
		}
		Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
		for (Map.Entry<Thread, StackTraceElement[]> stackTrace : allStackTraces.entrySet()) {
			Thread thread = stackTrace.getKey();
			Thread.State state = thread.getState();
			if (state != Thread.State.RUNNABLE) {
				continue;
			}
			StackTraceElement[] elements = stackTrace.getValue();
			if (elements.length == 0) {
				continue;
			}
			if (startsWith(thread.getName(), ignoredThreads)) {
				continue;
			}
			boolean skip = false;
			for (StackTraceElement element : elements) {
				if (startsWith(element.getClassName() + '#' + element.getMethodName(), ignoredPackagesInStackTrace)) {
					skip = true;
					break;
				}
			}
			if (skip) {
				continue;
			}
			register(out, elements);
		}
		endDate = System.currentTimeMillis();
	}

	private boolean startsWith(String str, Collection<String> matches) {
		for (String match : matches) {
			if (str.startsWith(match)) {
				return true;
			}
		}
		return false;
	}

	protected void printTick(JspWriter out) {
		long now = System.currentTimeMillis();
		if (now > lastTickTime + MIN_TICK_DELAY_MS) {
			lastTickTime = now;
			print(out, ".");
		}
	}

	protected void register(JspWriter out, StackTraceElement[] trace) {
		// convert from StackTraceElement[] to printable (html) String
		String traceStr = printStackTrace(trace);
		Integer oldCount = traces.get(traceStr);
		if (oldCount == null) {
			traces.put(traceStr, Integer.valueOf(1));
		} else {
			traces.put(traceStr, Integer.valueOf(1 + oldCount.intValue()));
		}
		++totalStackTracesAnalysed;
		if (traces.size() > MAX_ELEMENTS) {
			println(out, I18NSupport.getLocalizedMessage(BUNDLE_NAME, "stacktrace.remove"));//$NON-NLS-1$
			Iterator<Map.Entry<String, Integer>> iterator = traces.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Integer> entry = iterator.next();
				Integer count = entry.getValue();
				if (count.longValue() <= minCount) {
					iterator.remove();
				}
			}
			if (traces.size() > MAX_ELEMENTS) {
				++minCount;
				println(out, I18NSupport.getLocalizedMessage(BUNDLE_NAME, "stacktrace.increase", new Object[] {Integer.valueOf(minCount)}));//$NON-NLS-1$
			}
		}
	}

	/**
	 * Dumps about {@code maxTraces} most significant traces
	 */
	public void dump(JspWriter out, int maxTraces) throws IOException {
		String startDateStr = DateUtils.dateToFormattedDateTimeString(startDate, DateUtils.DEFAULT_DATE_TIME_FORMAT);
		String endDateStr = DateUtils.dateToFormattedDateTimeString(endDate, DateUtils.DEFAULT_DATE_TIME_FORMAT);
		String monitoringTime = DateUtils.timeIntervalToFormattedString(endDate - startDate);
		println(out, I18NSupport.getLocalizedMessage(BUNDLE_NAME, "results", new Object[] {startDateStr, endDateStr, monitoringTime}));//$NON-NLS-1$
		// sort traces by decreasing frequency
		Map.Entry<String, Integer>[] sortedTraces = traces.entrySet().toArray(new Map.Entry[traces.size()]);
		Arrays.sort(sortedTraces, new Comparator<Map.Entry<?, Integer>>() {
			/** {@inheritDoc} */
			public int compare(Map.Entry<?, Integer> e1, Map.Entry<?, Integer> e2) {
				return e2.getValue().intValue() - e1.getValue().intValue();
			}
		});
		// print the sorted traces
		int printCount = 0;
		boolean shouldStop = false;
		Integer lastCount = null;
		for (Map.Entry<String, Integer> entry : sortedTraces) {
			if (shouldStop && ! entry.getValue().equals(lastCount)) {
				// stop at lastCount boundary
				break;
			} else {
				++printCount;
				if (printCount > maxTraces) {
					shouldStop = true;
				}
			}
			lastCount = entry.getValue();
			Double percent = new Double(lastCount.intValue() / Math.max(totalStackTracesAnalysed, 1.0));
			out.println(I18NSupport.getLocalizedMessage(BUNDLE_NAME, "stacktrace.dump",//$NON-NLS-1$
					new Object[] {lastCount, percent, entry.getKey()})
			);
			out.flush();
		}
	}

	protected String printStackTrace(StackTraceElement[] elements) {
		StringBuilder result = new StringBuilder(256);
		for (int i = 0; i < elements.length; ++i) {
			StackTraceElement element = elements[i];
			result.append(StringUtils.escapeXml(element.toString())).append('\n');
		}
		return result.toString();
	}

	private void print(JspWriter out, String message) {
		try {
			out.print(message);
			out.flush();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	private void println(JspWriter out, String message) {
		try {
			out.println(message);
			out.flush();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	/** {@inheritDoc} */
//	@javax.annotation.PreDestroy
	public void close() {
		traces.clear();
		ignoredThreads.clear();
		ignoredPackagesInStackTrace.clear();
	}

	public Set<String> getIgnoredThreadNames() {
		return ignoredThreads;
	}
	public void addIgnoredThreadName(String threadName) {
		ignoredThreads.add(threadName);
	}
	/**
	 * Convenience method
	 */
	public void addAllIgnoredThreadNames(String ignoredThreadsList, String delimRE) {
		if (ignoredThreadsList.trim().length() > 0) {
			String[] ignores = ignoredThreadsList.split(delimRE);
			for (int i = 0; i < ignores.length; ++i) {
				if (ignores[i].trim().length() > 0) {
					addIgnoredThreadName(ignores[i].trim());
				}
			}
		}
	}

	public Set<String> getIgnoredPackagesInStackTrace() {
		return ignoredPackagesInStackTrace;
	}
	public void addIgnoredPackagesInStackTrace(String ignoredClasseInStackTrace) {
		ignoredPackagesInStackTrace.add(ignoredClasseInStackTrace);
	}
	/**
	 * Convenience method
	 */
	public void addAllIgnoredPackagesInStackTrace(String ignoredPackagesList, String delimRE) {
		if (ignoredPackagesList.trim().length() > 0) {
			String[] ignores = ignoredPackagesList.split(delimRE);
			for (int i = 0; i < ignores.length; ++i) {
				if (ignores[i].trim().length() > 0) {
					addIgnoredPackagesInStackTrace(ignores[i].trim());
				}
			}
		}
	}
}
