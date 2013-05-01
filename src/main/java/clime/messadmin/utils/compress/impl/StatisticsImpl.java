/**
 *
 */
package clime.messadmin.utils.compress.impl;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicLong;

import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.utils.BytesFormat;
import clime.messadmin.utils.compress.Statistics;

/**
 * @author C&eacute;drik LIME
 */
public class StatisticsImpl implements Statistics {
	public volatile boolean enableTimeStatistics = false;
	public AtomicLong realTimeNano        = new AtomicLong();
	public AtomicLong readTimeNano        = new AtomicLong();
	public AtomicLong checksumTimeNano    = new AtomicLong();
	public AtomicLong compressionTimeNano = new AtomicLong();
	public AtomicLong writeTimeNano       = new AtomicLong();
	public AtomicLong uncompressedSize = new AtomicLong();
	public AtomicLong compressedSize   = new AtomicLong();

	public StatisticsImpl() {
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(128);
		NumberFormat numberFormat = NumberFormat.getIntegerInstance(I18NSupport.getAdminLocale());
		numberFormat.setMaximumFractionDigits(0);
		NumberFormat percentFormat = NumberFormat.getPercentInstance(I18NSupport.getAdminLocale());
		percentFormat.setMaximumFractionDigits(1);
		BytesFormat bytesFormat = BytesFormat.getBytesInstance(I18NSupport.getAdminLocale(), false);
		bytesFormat.setMaximumFractionDigits(1);
		str.append("Compression statistics:\n");
		str.append("Wall clock time:\t").append(numberFormat.format(getRealTime())).append(" ms\n");
		str.append("CPU time:\t\t").append(numberFormat.format(getCpuTime())).append(" ms\n");
		str.append("\tRead:\t\t").append(numberFormat.format(getReadTime())).append(" ms\n");
		str.append("\tChecksum:\t").append(numberFormat.format(getChecksumTime())).append(" ms\n");
		str.append("\tCompress:\t").append(numberFormat.format(getCompressionTime())).append(" ms\n");
		str.append("\tWrite:\t\t").append(numberFormat.format(getWriteTime())).append(" ms\n");
		str.append("Uncompressed size:\t").append(bytesFormat.format(getUncompressedSize())).append('\n');
		str.append("Compressed size:\t").append(bytesFormat.format(getCompressedSize())).append('\n');
		numberFormat.setMaximumFractionDigits(1);
		str.append("CPU compression speed:\t").append(numberFormat.format(getUncompressedSize()/1000.0/getCompressionTime())).append(" MB/s\n");
		str.append("User compression speed:\t").append(numberFormat.format(getUncompressedSize()/1000.0/getRealTime())).append(" MB/s\n");
		str.append("Compression ratio:\t").append(percentFormat.format((double)getCompressedSize() / getUncompressedSize())).append('\n');
		str.append("Space savings:\t\t").append(percentFormat.format(1 - (double)getCompressedSize() / getUncompressedSize())).append('\n');
		str.append("Speedup:\t\t").append(percentFormat.format((double)getCpuTime()/getRealTime()));
		return str.toString();
	}


	public long nanoTime() {
		if (enableTimeStatistics) {
			return System.nanoTime();
		} else {
			return 0;
		}
	}


	/** {@inheritDoc} */
	public long getRealTime() {
		return NANOSECONDS.toMillis(realTimeNano.get());
	}

	/** {@inheritDoc} */
	public long getCpuTime() {
		return NANOSECONDS.toMillis(readTimeNano.get() + checksumTimeNano.get() + compressionTimeNano.get() + writeTimeNano.get());
	}

	/** {@inheritDoc} */
	public long getChecksumTime() {
		return NANOSECONDS.toMillis(checksumTimeNano.get());
	}

	/** {@inheritDoc} */
	public long getCompressionTime() {
		return NANOSECONDS.toMillis(compressionTimeNano.get());
	}

	/** {@inheritDoc} */
	public long getReadTime() {
		return NANOSECONDS.toMillis(readTimeNano.get());
	}

	/** {@inheritDoc} */
	public long getWriteTime() {
		return NANOSECONDS.toMillis(writeTimeNano.get());
	}

	/** {@inheritDoc} */
	public long getUncompressedSize() {
		return uncompressedSize.get();
	}

	/** {@inheritDoc} */
	public long getCompressedSize() {
		return compressedSize.get();
	}
}
