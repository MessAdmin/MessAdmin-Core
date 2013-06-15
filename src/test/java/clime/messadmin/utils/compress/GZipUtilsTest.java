package clime.messadmin.utils.compress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;

import clime.messadmin.utils.compress.gzip.GZipConfiguration;
import clime.messadmin.utils.compress.gzip.GZipUtils;
import clime.messadmin.utils.compress.gzip.PGZIPOutputStream;
import clime.messadmin.utils.compress.impl.StatisticsImpl;

/**
 * @author C&eacute;drik LIME
 */
// Tests:
// * empty file
// * "standard" random file
// * 2+ GB file
// * 4+ GB file
// * files specially crafted to make the compressor bug when flushing
// Test with:
// * Java 5 / 6
// * Java 7
public class GZipUtilsTest extends BaseCompressionTest {
	protected GZipConfiguration config;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(GZipUtilsTest.class);
	}

	public GZipUtilsTest() {
	}

	/** {@inheritDoc} */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		src = new File("C:\\temp\\test.dat");
		dst = new File("C:\\temp\\test.dat.gz");
		config = new GZipConfiguration();
//		config.setMaxProcessors(4);
//		config.setBlockSize((int)src.length() + 1);
		config.setConfigurationParameters(src);
		config.setEnableTimeStatistics(true);
		config.setCompressionLevel(Deflater.BEST_SPEED);
	}

	/** {@inheritDoc} */
	@Override
	protected void tearDown() throws Exception {
		dst.delete();
		super.tearDown();
	}


	public void testJavaGZIPOutputStream() throws IOException {
		System.out.println("*** Testing Java GZipOutputStream (baseline)");
		OutputStream out = new java.util.zip.GZIPOutputStream(new FileOutputStream(dst));
		runGZipTest(out);
	}

	public void testEnhancedGZIPOutputStream() throws IOException {
		System.out.println("*** Testing enhanced GZipOutputStream");
		OutputStream out = new clime.messadmin.utils.compress.gzip.GZIPOutputStream(new FileOutputStream(dst), config);
		runGZipTest(out);
	}

	public void testPGZIPOutputStreamBlock() throws IOException {
		System.out.println("*** Testing PGZipOutputStream (block API)");
		OutputStream out = new PGZIPOutputStream(new FileOutputStream(dst), config);
		runGZipTest(out);
	}

	public void testPGZIPOutputStreamStream() throws IOException {
		System.out.println("*** Testing PGZipOutputStream (Stream API)");
		PGZIPOutputStream out = new PGZIPOutputStream(new FileOutputStream(dst), config);
		InputStream in = new FileInputStream(src);
		System.out.println(config);
		try {
			out.write(in);
		} finally {
			in.close();
			out.close();
		}
		System.out.println(config.getStatistics());
		compareFileContent(src, dst);
		System.out.println("File content comparison OK");
	}

	public void testCompress() throws IOException {
		System.out.println("*** Testing compress()");
		System.out.println(config);
		Statistics stats = GZipUtils.compress(src, dst);
		System.out.println(stats);
		compareFileContent(src, dst);
		System.out.println("File content comparison OK");
	}


	protected void runGZipTest(OutputStream out) throws IOException {
		InputStream in = new FileInputStream(src);
		System.out.println(config);
		long s = System.currentTimeMillis();
		try {
			copy(in, out, copyBufferSize);
		} finally {
			in.close();
			out.close();
		}
		long e = System.currentTimeMillis();
		if (config.getStatistics().getRealTime() == 0) {
			System.out.println("GZip time: " + (e-s)/1000.0 + " s");
			((StatisticsImpl)config.getStatistics()).realTimeNano.set((e-s)*1000*1000);
		}
		System.out.println(config.getStatistics());
		compareFileContent(src, dst);
		System.out.println("File content comparison OK");
	}


	/**
	 * Compares an uncompressed source file and its compressed representation
	 * @param src uncompressed source file
	 * @param gz  compressed representation of {@code src}
	 * @throws IOException
	 * @throws AssertionError
	 */
	private static void compareFileContent(File src, File gz) throws IOException, AssertionError {
		InputStream gzIn = new GZIPInputStream(new FileInputStream(gz));
		try {
			compareFileContent(src, gzIn);
		} finally {
			gzIn.close();
		}
	}

}
