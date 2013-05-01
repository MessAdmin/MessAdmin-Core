package clime.messadmin.utils.compress.zip;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;

import clime.messadmin.utils.compress.Statistics;
import clime.messadmin.utils.compress.gzip.GZipUtils;
import clime.messadmin.utils.compress.impl.Java7Deflater;
import clime.messadmin.utils.compress.impl.StatisticsImpl;

/**
 * @author C&eacute;drik LIME
 */
public class ZipUtils {
	public static final long MIN_FILE_TOTAL_SIZE_FOR_MT_COMPRESSION = GZipUtils.MIN_FILE_SIZE_FOR_MT_COMPRESSION;
	public static final long MIN_PROCESSORS_FOR_MT_COMPRESSION = GZipUtils.MIN_PROCESSORS_FOR_MT_COMPRESSION;

	private ZipUtils() {
	}

	/**
	 * @param src source file / directory to compress. Can be {@code null}. Used to compute if we should use multi-threaded compression.
	 * @param out the actual output stream
	 * @return {@link PZipOutputStream} (multi-threaded) or {@link ZipOutputStream} (mono-threaded) depending on the available # processors.
	 * @see #MIN_FILE_SIZE_FOR_MT_COMPRESSION
	 * @see #MIN_PROCESSORS_FOR_MT_COMPRESSION
	 * @throws IOException
	 */
	public static ZipOutputStreamAdapter getZipOutputStream(File src, OutputStream out, ZipConfiguration config) throws IOException {
		/*if ((src != null) && isMT(src, config)) {
			return new PZipAdapter(new PZipOutputStream(out, config));
		} else */if (Java7Deflater.isEnhancedDeflateAvailable) {
			// Java 7 introduced ZIP64 support. Use "their" facilities since we have not much more to offer.
			ZipOutputStreamAdapter zosa = new JavaZipAdapter(new java.util.zip.ZipOutputStream(out));
			zosa.setLevel(config.getCompressionLevel());
			zosa.setMethod(config.getCompressionMethod());
			if (config.getComment() != null) {
				zosa.setComment(config.getComment().toString());
			}
			return zosa;
		} else {
			return new EnhancedJavaZipAdapter(new ZipOutputStream(out, config));
		}
	}

	private static boolean isMT(File src, ZipConfiguration config) throws IOException {
		// accept MT compression for only 2 cores if the file is really big
		if (config.getMaxProcessors() >= MIN_PROCESSORS_FOR_MT_COMPRESSION) {
			return isSourceSizeAtLeast(src, MIN_FILE_TOTAL_SIZE_FOR_MT_COMPRESSION, 0) >= MIN_FILE_TOTAL_SIZE_FOR_MT_COMPRESSION;
		} else if (config.getMaxProcessors() == 2) {
			return isSourceSizeAtLeast(src, 3 * MIN_FILE_TOTAL_SIZE_FOR_MT_COMPRESSION, 0) >= 3 * MIN_FILE_TOTAL_SIZE_FOR_MT_COMPRESSION;
		} else {
			return false;
		}
	}
	private static long isSourceSizeAtLeast(File source, long minimumSize, long currentVisitedSize) {
		if (source.isFile() && source.canRead()) {
			currentVisitedSize += source.length();
		} else if (source.isDirectory()) {
			// Iterate
			File files[] = source.listFiles(ReadableFiles.INSTANCE);
			for (int i = 0; i < files.length; ++i) {
				currentVisitedSize = isSourceSizeAtLeast(files[i], minimumSize, currentVisitedSize);
				if (currentVisitedSize >= minimumSize) {
					return currentVisitedSize;
				}
			}
		} else {
			//skip
//			log.warn("Skipping file {}, which is neither a file nor a directory, or is not readable", source);
		}
		return currentVisitedSize;
	}

	public static Statistics compress(File file) throws IOException {
		File zip = new File(file.getAbsolutePath() + ".zip");//$NON-NLS-1$
		return compress(file, zip);
	}

	/**
	 *
	 * @param source usually a directory
	 * @param target target ZIP archive file. Will be overwritten.
	 */
	public static Statistics compress(File source, File target) throws IOException {
		if  ( !source.canRead()) {
			throw new FileNotFoundException(source.getPath() + " does not exists, or can not be read.");
		}
		ZipConfiguration configuration = new ZipConfiguration();
		long startNanoTime = System.nanoTime();
		ZipOutputStreamAdapter out = getZipOutputStream(source, new FileOutputStream(target), configuration);
		long lastModified;
		try {
			lastModified = compress(source, out, "");
		} finally {
			out.getDelegate().close();
		}
		if (lastModified > 0) {
			target.setLastModified(lastModified);
		}
		// Do not automatically delete the directory, as files may have been skipped during compression (e.g. read permission, open files)!
		if (configuration.getStatistics().getRealTime() <= 0) {
			long endNanoTime = System.nanoTime();
			((StatisticsImpl)configuration.getStatistics()).realTimeNano.addAndGet(endNanoTime - startNanoTime);
		}
		return configuration.getStatistics();
	}

	/**
	 * Note: this method will not close the output stream!
	 *
	 * @return most recent lastModifiedDate
	 */
	// package, not private, to allow JUnit access
	static long compress(File source, ZipOutputStreamAdapter out, String rootPath) throws IOException {
		long lastModified = -1;
		if (source.isFile() && source.canRead()) {
			// Store file
			ZipEntry zipEntry = new clime.messadmin.utils.compress.zip.ZipEntry(rootPath + source.getName());// store relative (to archive root) path only!
			zipEntry.setSize(source.length());
			zipEntry.setTime(source.lastModified());
			lastModified = Math.max(lastModified, source.lastModified());
			out.putNextEntry(zipEntry);
			InputStream in = new FileInputStream(source);
			try {
				copy(in, out.getDelegate());
			} finally {
				in.close();
			}
			out.closeEntry();
		} else if (source.isDirectory()) {
			// Store directory
			rootPath += source.getName() + '/';
			ZipEntry zipEntry = new ZipEntry(rootPath);// store relative (to archive root) path only!
			zipEntry.setTime(source.lastModified());
			out.putNextEntry(zipEntry);
			out.closeEntry();
			// Iterate
			File files[] = source.listFiles(ReadableFiles.INSTANCE);
			for (int i = 0; i < files.length; ++i) {
				long lastMod = compress(files[i], out, rootPath);
				lastModified = Math.max(lastModified, lastMod);
			}
		} else {
			//skip
//			log.warn("Skipping file {}, which is neither a file nor a directory, or is not readable", source);
		}
		return lastModified;
	}

	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buff = new byte[16384];//FIXME magic number
		int nRead = 0;
		while ((nRead = in.read(buff)) != -1) {
			out.write(buff, 0, nRead);
		}
		out.flush();
	}

	// package, not private, to allow JUnit access
	static class ReadableFiles implements FileFilter {
		public static final FileFilter INSTANCE = new ReadableFiles();
		private ReadableFiles() {
		}
		/** {@inheritDoc} */
		public boolean accept(File pathname) {
			return pathname.canRead();
		}
	}
}
