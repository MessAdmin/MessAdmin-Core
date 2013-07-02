package clime.messadmin.utils.compress.gzip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import clime.messadmin.utils.compress.Statistics;
import clime.messadmin.utils.compress.impl.Java7Deflater;

/**
 * @author C&eacute;drik LIME
 */
public class GZipUtils {
	public static final long MIN_FILE_SIZE_FOR_MT_COMPRESSION = 40*1024*1024;// 40MB: about 2 seconds of native gzip -6
	public static final long MIN_PROCESSORS_FOR_MT_COMPRESSION;

	static {
		if (Java7Deflater.isEnhancedDeflateAvailable) {
			MIN_PROCESSORS_FOR_MT_COMPRESSION = 2;// Java 7 facilities (native compression)
		} else {
			MIN_PROCESSORS_FOR_MT_COMPRESSION = 3;// JZlib facilities (slower)
		}
	}

	private GZipUtils() {
	}

	/**
	 * @return {@link PGZIPOutputStream} (multi-threaded) or {@link GZIPOutputStream} (mono-threaded) depending on the inputSize and available # processors.
	 * @see #MIN_FILE_SIZE_FOR_MT_COMPRESSION
	 * @see #MIN_PROCESSORS_FOR_MT_COMPRESSION
	 * @throws IOException
	 */
	public static OutputStream getGZipOutputStream(long inputSize, OutputStream out, GZipConfiguration config) throws IOException {
		if (isMT(inputSize, config)) {
			return new PGZIPOutputStream(out, config);
		} else {
			return new GZIPOutputStream(out, config);
		}
	}

	private static boolean isMT(long inputSize, GZipConfiguration config) throws IOException {
		// accept MT compression for only 2 cores if the file is really big
		return
			(inputSize >= MIN_FILE_SIZE_FOR_MT_COMPRESSION && config.getMaxProcessors() >= MIN_PROCESSORS_FOR_MT_COMPRESSION)
				||
			(config.getMaxProcessors() == 2 && inputSize >= 3 * MIN_FILE_SIZE_FOR_MT_COMPRESSION);
	}

	public static Statistics compress(File file) throws IOException {
		File gz = new File(file.getAbsolutePath() + ".gz");//$NON-NLS-1$
		return compress(file, gz);
	}

	/**
	 * @param source file to compress
	 * @param target target GZip archive file. Will be overwritten.
	 */
	// synchronized since there is no point executing 2 instances in parallel...
	public static synchronized Statistics compress(File source, File target) throws IOException {
		if  ( !source.isFile() || !source.canRead()) {
			throw new FileNotFoundException(source.getPath() + " does not exists, is not a file, or can not be read.");
		}
		GZipConfiguration config = new GZipConfiguration();
		Statistics stats = config.getStatistics();
		config.setConfigurationParameters(source);
		InputStream in = new FileInputStream(source);
		OutputStream out = new FileOutputStream(target);
		try {
			if (isMT(source.length(), config)) {
				out = new PGZIPOutputStream(out, config);// override "out" so that it is the PGZIPOutputStream that is closed at the end!
				((PGZIPOutputStream)out).write(in);
			} else {
				// single thread if file.size < 20M or available processors == 1
				out = new GZIPOutputStream(out, config);// override "out" so that it is the GZIPOutputStream that is closed at the end!
				copy(in, out);
			}
		} finally {
			try {
				in.close();
			} finally {
				out.close();
			}
		}
		long lastModified = source.lastModified();
		if (lastModified > 0) {
			target.setLastModified(lastModified);
		}
		if (! source.canWrite()) {
			target.setReadOnly();
		}
		return stats;
	}

	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buff = new byte[32768];//FIXME magic number
		int nRead = 0;
		while ((nRead = in.read(buff)) != -1) {
			out.write(buff, 0, nRead);
		}
		out.flush();
	}
}
