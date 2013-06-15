package clime.messadmin.utils.compress;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import clime.messadmin.utils.compress.impl.Java7Deflater;

import junit.framework.TestCase;

/**
 * @author C&eacute;drik LIME
 */
// Tests:
// * empty file
// * empty directory
// * "standard" random file
// * 2+ GB file
// * 4+ GB file
// * directory hierarchy with 65K+ files
// Test with:
// * Java 5 / 6
// * Java 7
public abstract class BaseCompressionTest extends TestCase {
	protected File src;
	protected File dst;
	protected int copyBufferSize;

	public BaseCompressionTest() {
	}

	/** {@inheritDoc} */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		copyBufferSize = 16384;//FIXME should test with 16384, 49999, and config.getBlockSize()
		if (Java7Deflater.isEnhancedDeflateAvailable) {
			System.out.println("PIGZ: Using Java 7 Deflater");
		} else {
			System.out.println("PIGZ: Using JZlib Deflater");
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void tearDown() throws Exception {
		dst.delete();
		super.tearDown();
	}


	protected static void copy(InputStream in, OutputStream out, int bufferSize) throws IOException {
		byte[] buff = new byte[bufferSize];
		int nRead = 0;
		while ((nRead = in.read(buff)) != -1) {
			out.write(buff, 0, nRead);
		}
		out.flush();
	}

	/**
	 * Compares an uncompressed source file and its compressed representation
	 * @param src         uncompressed source file
	 * @param compressed  compressed representation of {@code src}
	 * @throws IOException
	 * @throws AssertionError
	 */
	protected static void compareFileContent(File src, InputStream compressed) throws IOException, AssertionError {
		InputStream srcIn = new BufferedInputStream(new FileInputStream(src), 32768);
		InputStream zipIn = new BufferedInputStream(compressed, 32768);
		try {
			int nSrcRead = 0;
			int nZipRead = 0;
			do {
				nSrcRead = srcIn.read();
				nZipRead = zipIn.read();
				if (nSrcRead != nZipRead) {
					throw new AssertionError("Source and compressed files are different!");
				}
			} while (nSrcRead != -1 && nZipRead != -1);
			if (nSrcRead != -1 || nZipRead != -1) {
				throw new AssertionError("Source and compressed files have different size!");
			}
		} finally {
			srcIn.close();
		}
	}

	protected static final String EMPTY_FILE = "empty.dat";
	protected static final String EMPTY_DIR = "empty_dir";
	protected static final String RANDOM_FILE = "random.dat";
	protected static final String THREE_GB_ZERO_FILE = "3GB_of_Zeros";
	protected static final String FIVE_GB_ZERO_FILE = "5GB_of_Zeros";
	protected static final String HUNDRED_THOUSAND_FILES_DIR = "100k_Files";// 10 folders, 4-level deep, 10 empty files in each leaf folder
	public static void main(String[] args) throws IOException {
		// Generate test files
		if (args.length != 1) {
			System.err.println("Syntax: " + BaseCompressionTest.class.getName() + " <outputDir>");
			System.exit(-1);
		}
		File dstDir = new File(args[0]);
		if (! dstDir.exists()) {
			dstDir.mkdirs();
		}
		if ((! dstDir.exists()) || (! dstDir.isDirectory()) || (! dstDir.canWrite())) {
			System.err.println("Can not write to " + args[0]);
			System.exit(-2);
		}
		// 1. Empty file
		{
			File empty = new File(dstDir, EMPTY_FILE);
			System.out.println("Creating " + empty);
			empty.createNewFile();
		}
		// 2. Empty dir
		{
			File empty = new File(dstDir, EMPTY_DIR);
			System.out.println("Creating " + empty);
			empty.mkdir();
		}
		// 3. Standard, random, file
		{
			Random rng = new Random();
			int size = 100000 + rng.nextInt(1000*1000);
			File random = new File(dstDir, RANDOM_FILE);
			System.out.println("Creating " + random + " of size " + size);
			OutputStream out = new FileOutputStream(random);
			for (int i = 0; i < size; ++i) {
				out.write(rng.nextInt());
			}
			out.close();
		}
		// 4. 3GB file
		{
			byte[] oneMB = new byte[1024*1024];
			File threeGB = new File(dstDir, THREE_GB_ZERO_FILE);
			System.out.println("Creating " + threeGB);
			OutputStream out = new FileOutputStream(threeGB);
			for (int i = 0; i < 2.02*1024; ++i) {
				out.write(oneMB);
			}
			out.close();
		}
		// 5. 5GB file
		{
			byte[] oneMB = new byte[1024*1024];
			File fiveGB = new File(dstDir, FIVE_GB_ZERO_FILE);
			System.out.println("Creating " + fiveGB);
			OutputStream out = new FileOutputStream(fiveGB);
			for (int i = 0; i < 4.04*1024; ++i) {
				out.write(oneMB);
			}
			out.close();
		}
		// 6. 100k files
		{
			File hydraDir = new File(dstDir, HUNDRED_THOUSAND_FILES_DIR);
			System.out.println("Creating " + hydraDir);
			hydraDir.mkdir();
			for (int i = 0; i < 10; ++i) {
				File l0 = new File(hydraDir, Integer.toString(i));
				for (int j = 0; j < 10; ++j) {
					File l1 = new File(l0, Integer.toString(j));
					for (int k = 0; k < 10; ++k) {
						File l2 = new File(l1, Integer.toString(k));
						for (int l = 0; l < 10; ++l) {
							File l3 = new File(l2, Integer.toString(l));
							l3.mkdirs();
							for (int m = 0; m < 10; ++m) {
								File d = new File(l3, Integer.toString(m));
								d.createNewFile();
							}
						}
					}
				}
			}
		}
		System.out.println("All done!");
	}
}
