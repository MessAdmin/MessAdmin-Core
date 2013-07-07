/**
 *
 */
package clime.messadmin.providers.userdata;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Array;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionProvider;
import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.profiler.ProfilerAction;
import clime.messadmin.providers.spi.ServerDataProvider;
import clime.messadmin.utils.DateUtils;
import clime.messadmin.utils.JMX;
import clime.messadmin.utils.StringUtils;

/**
 * Dumps all Threads in a pretty HTML page.
 * @author C&eacute;drik LIME
 */
public class ThreadsDumper extends BaseAdminActionProvider implements ServerDataProvider, AdminActionProvider {
	private static final String BUNDLE_NAME = ThreadsDumper.class.getName();
	public static final String ACTION_ID = "dumpThreads";//$NON-NLS-1$

	/**
	 *
	 */
	public ThreadsDumper() {
		super();
	}

	/** {@inheritDoc} */
	public String getActionID() {
		return ACTION_ID;
	}

	/** {@inheritDoc} */
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		setNoCache(response);
		response.setContentType("text/plain;charset=UTF-8");//$NON-NLS-1$
		PrintWriter out = response.getWriter();
		out.println(
				DateUtils.dateToFormattedDateTimeString(System.currentTimeMillis(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
		{
		out.print("Full thread dump ");
		out.print(System.getProperties().get("java.vm.name"));
		out.print(" (");
		out.print(System.getProperties().get("java.vm.version"));
		out.println("):");
		}
		out.println();
		Map<Thread,StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
		for (Map.Entry<Thread,StackTraceElement[]> entry : stackTraces.entrySet()) {
			Thread t = entry.getKey();
			StackTraceElement[] stes = entry.getValue();
			// Line 1: Name, Type, Priority, Thread ID, Native ID, State, Address Range
			{
			out.print('"');
			out.print(t.getName());
			out.print('"');
			if (t.isDaemon()) {
				out.print(" daemon");
			}
			out.print(" prio=");
			out.print(t.getPriority());
			out.print(" tid=0x");
			out.print(Long.toHexString(t.getId()));
			// native id
			// state
			// address range
			}
			out.println();

			// Line 2: Thread State
			{
			Thread.State state = t.getState();
			assert state != null;
			out.print('\t');
			out.print(state.getClass().getName());
			out.print(": ");
			out.println(state);
			}

			// Lines 3+: Stack Trace
			for (int i = 0; i < stes.length; ++i) {
				StackTraceElement trace = stes[i];
				out.println("\t\tat " + trace);//$NON-NLS-1$
			}
			out.println();
		}
		out.flush();
		out.close();
	}

	/***********************************************************************/

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPriority() {
		return 50;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getServerDataTitle() {
		return I18NSupport.getLocalizedMessage(BUNDLE_NAME, "title");//$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public String getXHTMLServerData() {
		ThreadGroup tg = Thread.currentThread().getThreadGroup();
		// Find the root thread group
		while (tg.getParent() != null) {
			tg = tg.getParent();
		}
		StringBuilder buffer = new StringBuilder(tg.activeCount()*400 + tg.activeGroupCount()*120);

		// alert user about deadlocked threads
		long[] deadlockedThreadsIDs = JMX.findDeadlockedThreadsIDs();
		if (deadlockedThreadsIDs != null && deadlockedThreadsIDs.length > 0) {
			buffer.append("<div style=\"font-weight: bolder; color: red;\">\n");
			buffer.append(I18NSupport.getLocalizedMessage(BUNDLE_NAME, "stuckThreads.warning"));//$NON-NLS-1$
			buffer.append("\n\t<ul>\n");
			for (int i = 0; i < deadlockedThreadsIDs.length; ++i) {
				buffer.append("\t\t<li>").append(deadlockedThreadsIDs[i]).append("</li>\n");
			}
			buffer.append("\t</ul>\n");
			buffer.append("</div>\n");
		}

		// thread dump & CPU profiler
		{
			buffer.append("[ ");
			buffer.append("<a href=\"?").append(ACTION_PARAMETER_NAME).append('=').append(getActionID()).append("\" target=\"_blank\">");
			buffer.append(I18NSupport.getLocalizedMessage(BUNDLE_NAME, "thread_dump"));//$NON-NLS-1$
			buffer.append("</a> | ");
			buffer.append("<a href=\"?").append(ACTION_PARAMETER_NAME).append('=').append(ProfilerAction.ACTION_ID).append("\" target=\"_blank\">");
			buffer.append(I18NSupport.getLocalizedMessage(BUNDLE_NAME, "profiler"));//$NON-NLS-1$
			buffer.append("</a>");
			buffer.append(" ]<br />\n");
		}

		// default UncaughtExceptionHandler
		UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		if (defaultUncaughtExceptionHandler != null) {
			buffer.append("<br/>\n");// pretty bit of vertical space...
			buffer.append(I18NSupport.getLocalizedMessage(BUNDLE_NAME, "thread_default_uncaught_exception_handler",//$NON-NLS-1$
					new Object[] {StringUtils.escapeXml(defaultUncaughtExceptionHandler.toString())}));
			buffer.append("<br/>\n");
		}

		// list threads
		buffer.append("<dl>");
		dump(tg, buffer);
		buffer.append("</dl>");
		return buffer.toString();
	}

	protected void dump(Thread t, StringBuilder out) {
		ClassLoader cl = t.getContextClassLoader();
		long id = t.getId();
		String name = t.getName();
		int priority = t.getPriority();
//		Object[] stackTrace = (Object[]) invoke(getStackTrace, t, null);
		Thread.State state = t.getState();
		UncaughtExceptionHandler uncaughtExceptionHandler = t.getUncaughtExceptionHandler();
		boolean alive = t.isAlive();
		boolean daemon = t.isDaemon();
		boolean interrupted = t.isInterrupted();

		String style = "";
		if (daemon) {
			style += "list-style-type: circle;";
		} else {
			style += "list-style-type: disc;";
		}
		if (priority > Thread.NORM_PRIORITY) {
			String color = Integer.toHexString(5 + priority - Thread.NORM_PRIORITY);
			style += " color: #" + color + color + "0000;";
		}
		if (priority < Thread.NORM_PRIORITY) {
			String color = Integer.toHexString(5 + Thread.NORM_PRIORITY - priority);
			style += " color: #" + color + color + color + color + color + color + ';';
		}
		String infoballoonId = Long.toString(id);// Long.toString(t.hashCode())+Long.toString(Math.round(100000*Math.random()));

		out.append("<li style=\"" + style + "\">");
		out.append("<span id=\"").append(infoballoonId).append("\" class=\"infoballoonable\">");
		out.append(StringUtils.escapeXml(name));
		out.append(" [");
		if (id >= 0) {
			// Java 5+
			out.append("id=" + id);
			out.append(", state=" + state);
			out.append(", ");
		}
		out.append("priority=" + priority);
		out.append(", class=" + t.getClass().getName());
		if (alive) {
			out.append(", alive");
		}
		if (daemon) {
			out.append(", daemon");
		}
		if (interrupted) {
			out.append(", interrupted");
		}
		out.append(']');
		out.append("</span>");
		out.append("<div id=\"").append(infoballoonId).append("-infoballoon\" class=\"infoballoon\">");
		out.append("<table border=\"0\">");
		out.append("<tr><th>ClassLoader</th><td><pre>").append(cl==null?null:StringUtils.escapeXml(cl.toString())).append("</pre></td></tr>");
		if (uncaughtExceptionHandler != null && uncaughtExceptionHandler != t.getThreadGroup()) {
			out.append("<tr><th>UncaughtExceptionHandler</th><td>");
			out.append(StringUtils.escapeXml(uncaughtExceptionHandler.toString()));
			out.append("</td></tr>");
		}
		out.append("</table></div>");
		out.append("</li>\n");
	}

	protected void dump(ThreadGroup tg, StringBuilder out) {
		int maxPriority = tg.getMaxPriority();
		String name = tg.getName();
		boolean daemon = tg.isDaemon();
		boolean destroyed = tg.isDestroyed();
		ThreadGroup parent = tg.getParent();
		out.append("<dt>");
	    out.append(StringUtils.escapeXml(name));
	    out.append(" [");
	    if (parent != null) {
		    out.append("parent=" + StringUtils.escapeXml(parent.getName()));
			out.append(", ");
		}
	    out.append("class=" + tg.getClass().getName());
	    out.append(", maxpri=" + maxPriority);
		if (daemon) {
			out.append(", daemon");
		}
	    if (destroyed) {
			out.append(", destroyed");
		}
	    out.append(']');
		out.append("</dt>\n<dd>");
		Thread[] threads = getThreads(tg, false);
		if (threads.length > 0) {
			out.append("Threads: <ul>\n");
			for (int i = 0; i < threads.length; ++i) {
				if (threads[i] != null) {
					dump(threads[i], out);
				}
			}
			out.append("</ul>\n");
		}
		ThreadGroup[] threadGroups = getThreadGroups(tg, false);
		if (threadGroups.length > 0) {
			out.append("ThreadGroups: <dl>");
			for (int i = 0; i < threadGroups.length; ++i) {
				if (threadGroups[i] != null) {
					dump(threadGroups[i], out);
				}
			}
			out.append("</dl>\n");
		}
		out.append("</dd>");
	}

	protected Thread[] getThreads(ThreadGroup tg, boolean recurse) {
		int threadCountGuess = tg.activeCount() + 16;
		Thread[] threads = new Thread[threadCountGuess];
		// tg.enumerate(Thread[]) silently ignores any threads that can't fit into the array
		int threadCountActual = tg.enumerate(threads, recurse);
		// Make sure we don't miss any threads
		while (threadCountActual == threadCountGuess) {
			threadCountGuess *= 1.2;
			threads = new Thread[threadCountGuess];
			threadCountActual = tg.enumerate(threads);
		}
		threads = (Thread[]) resize(threads, threadCountActual);
		return threads;
	}

	protected ThreadGroup[] getThreadGroups(ThreadGroup tg, boolean recurse) {
		int threadCountGuess = tg.activeGroupCount() + 4;
		ThreadGroup[] threadGroups = new ThreadGroup[threadCountGuess];
		// tg.enumerate(Thread[]) silently ignores any ThreadGroup that can't fit into the array
		int threadCountActual = tg.enumerate(threadGroups, recurse);
		// Make sure we don't miss any ThreadGroup
		while (threadCountActual == threadCountGuess) {
			threadCountGuess *= 1.2;
			threadGroups = new ThreadGroup[threadCountGuess];
			threadCountActual = tg.enumerate(threadGroups);
		}
		threadGroups = (ThreadGroup[]) resize(threadGroups, threadCountActual);
		return threadGroups;
	}

	private Object[] resize(Object[] array, int newSize) {
		assert newSize >= 0;
		if (array.length == newSize) {
			return array;
		}
		Class<?> type = array.getClass().getComponentType();
		Object[] subArray = (Object[]) Array.newInstance(type, newSize);
		if (newSize > 0) {
			System.arraycopy(array, 0, subArray, 0, newSize);
		}
		return subArray;
	}
}
