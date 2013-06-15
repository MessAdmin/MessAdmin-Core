package clime.messadmin.utils.compress.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import clime.messadmin.utils.compress.BaseCompressionTest;
import clime.messadmin.utils.compress.Statistics;
import clime.messadmin.utils.compress.impl.StatisticsImpl;
import clime.messadmin.utils.compress.zip.ZipUtils;

/**
 * @author C&eacute;drik LIME
 */
// Tests:
// * empty file
// * empty directory
// * "standard" random file
// * 2+ GB file
// * 4+ GB file
// * 2+ level directory hierarchy with files above
// * directory hierarchy with 65K+ files
// Test with:
// * Java 5 / 6
// * Java 7
public class ZipUtilsTest extends BaseCompressionTest {
	protected ZipConfiguration config;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(ZipUtilsTest.class);
	}

	public ZipUtilsTest() {
	}

	/** {@inheritDoc} */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		src = new File("C:\\temp\\test");
		dst = new File("C:\\temp\\test.zip");
		config = new ZipConfiguration();
//		config.setMaxProcessors(4);
//		config.setBlockSize((int)src.length() + 1);
		config.setEnableTimeStatistics(true);
		config.setCompressionLevel(Deflater.BEST_SPEED);
	}

	/** {@inheritDoc} */
	@Override
	protected void tearDown() throws Exception {
		dst.delete();
		super.tearDown();
	}


	public void testJavaZipOutputStream() throws IOException {
		System.out.println("*** Testing Java ZipOutputStream (baseline)");
		ZipOutputStreamAdapter out = new JavaZipAdapter(new java.util.zip.ZipOutputStream(new FileOutputStream(dst)));
		runZipTest(out);
	}

	public void testEnhancedZipOutputStream() throws IOException {
		System.out.println("*** Testing enhanced ZipOutputStream");
		ZipOutputStreamAdapter out = new EnhancedJavaZipAdapter(new clime.messadmin.utils.compress.zip.ZipOutputStream(new FileOutputStream(dst), config));
		runZipTest(out);
	}

//	public void testPZipOutputStreamBlock() throws IOException {
//		System.out.println("*** Testing PZipOutputStream (block API)");
//		ZipOutputStreamAdapter out = new PZipAdapter(new PZipOutputStream(new FileOutputStream(dst), config));
//		runZipTest(out);
//	}
//
//	public void testPZipOutputStreamStream() throws IOException {//FIXME
//		System.out.println("*** Testing PZipOutputStream (Stream API)");
//		PGZIPOutputStream out = new PZipOutputStream(new FileOutputStream(dst), config);
//		InputStream in = new FileInputStream(src);
//		System.out.println(config);
//		try {
//			out.write(in);
//		} finally {
//			in.close();
//			out.close();
//		}
//		System.out.println(config.getStatistics());
//		compare(src, dst);
//		System.out.println("File content comparison OK");
//	}

	public void testCompress() throws IOException {
		System.out.println("*** Testing compress()");
		System.out.println(config);
		Statistics stats = ZipUtils.compress(src, dst);
		System.out.println(stats);
		compare(src, dst);
		System.out.println("File content comparison OK");
	}


	protected void runZipTest(ZipOutputStreamAdapter out) throws IOException {
		System.out.println(config);
		long s = System.currentTimeMillis();
		try {
			ZipUtils.compress(src, out, "");
		} finally {
			out.getDelegate().close();
		}
		long e = System.currentTimeMillis();
		if (config.getStatistics().getRealTime() == 0) {
			System.out.println("ZIP time: " + (e-s)/1000.0 + " s");
			((StatisticsImpl)config.getStatistics()).realTimeNano.set((e-s)*1000*1000);
		}
		System.out.println(config.getStatistics());
		compare(src, dst);
		System.out.println("File content comparison OK");
	}


	/**
	 * Compares an uncompressed source file/directory and its compressed representation
	 * @param src uncompressed source file/directory
	 * @param zip compressed representation of {@code src}
	 * @throws IOException
	 * @throws AssertionError
	 */
	private static void compare(File src, File zip) throws IOException, AssertionError {
		// 1. check all ZIP files are identical to source
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zip));
		Set<String> zipVisitedFiles = new HashSet<String>();
		Set<String> zipVisitedDirectories = new HashSet<String>();
		File parent = src.getParentFile();
		try {
			ZipEntry zipEntry = zipIn.getNextEntry();
			while (zipEntry != null) {
				File fileOrDir = new File(parent, zipEntry.getName());
				if (zipEntry.isDirectory()) {
					zipVisitedDirectories.add(fileOrDir.getPath());
					assert fileOrDir.isDirectory() : fileOrDir.toString();
				} else {
					zipVisitedFiles.add(fileOrDir.getPath());
//					assert fileOrDir.length() == zipEntry.getSize() : "Files size differ";//zipEntry.getSize() == -1
					assert Math.abs(fileOrDir.lastModified() - zipEntry.getTime()) < 2000 : "Files mtime differ";
					compareFileContent(fileOrDir, zipIn);
				}
				zipIn.closeEntry();
				zipEntry = zipIn.getNextEntry();
			}
		} finally {
			zipIn.close();
		}
		// 2. check all input files are in the ZIP archive
		compareFilesExistence(src, zipVisitedFiles, zipVisitedDirectories);
	}

	private static void compareFilesExistence(File src, Set<String> zipVisitedFiles, Set<String> zipVisitedDirectories) throws AssertionError {
		if (src.isFile() && src.canRead()) {
			if (! zipVisitedFiles.contains(src.getPath())) {
				throw new AssertionError("Source file absent from ZIP archive: " + src.getPath());
			}
		} else if (src.isDirectory()) {
			if (! zipVisitedDirectories.contains(src.getPath())) {
				throw new AssertionError("Source directory absent from ZIP archive: " + src.getPath());
			}
			// Iterate
			File files[] = src.listFiles(ZipUtils.ReadableFiles.INSTANCE);
			for (int i = 0; i < files.length; ++i) {
				compareFilesExistence(files[i], zipVisitedFiles, zipVisitedDirectories);
			}
		} else {
			//skip
//			log.warn("Skipping file {}, which is neither a file nor a directory, or is not readable", src);
		}
	}
}
