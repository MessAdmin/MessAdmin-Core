/**
 *
 */
package clime.messadmin.utils.compress.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import clime.messadmin.utils.compress.zip.ZipConfiguration;

/**
 * @author C&edrik; LIME
 */
public class Compressor {
	private static Method ThreadPoolExecutor_allowCoreThreadTimeOut;

	//	protected Logger log = LoggerFactory.getLogger(this.getClass());
	protected ZipConfiguration configuration;
	protected ExecutorService compressExecutor;

	static {
		// @since Java 6
		try {
			ThreadPoolExecutor_allowCoreThreadTimeOut = ThreadPoolExecutor.class.getMethod("allowCoreThreadTimeOut", Boolean.TYPE);//$NON-NLS-1$
		} catch (LinkageError e) {
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
	}

	public Compressor(ZipConfiguration configuration) {
		this.configuration = configuration;
		int nThreads = configuration.getMaxProcessors();
//		log.debug("Using {} threads for compression", Integer.valueOf(nThreads));
//		compressExecutor = Executors.newFixedThreadPool(nThreads);
		compressExecutor = new ThreadPoolExecutor(nThreads, nThreads, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		//((ThreadPoolExecutor)compressExecutor).allowCoreThreadTimeOut(true);
		if (ThreadPoolExecutor_allowCoreThreadTimeOut != null) {
			try {
				ThreadPoolExecutor_allowCoreThreadTimeOut.invoke(compressExecutor, Boolean.TRUE);
			} catch (Exception ignore) {
			}
		}
	}

	public void compress(Block block, Block previousBlock) {
		Runnable compressTask;
		if (Java7Deflater.isEnhancedDeflateAvailable) {
			compressTask = new CompressTaskJava7(block, previousBlock, configuration);
		} else {
			compressTask = new CompressTaskJZlib(block, previousBlock, configuration);
		}
		compressExecutor.execute(compressTask);
	}

	/**
	 * @see ExecutorService#shutdown()
	 */
	public void shutdown() {
		compressExecutor.shutdown();
	}

	/**
	 * @see ExecutorService#shutdownNow()
	 */
	public List<Runnable> shutdownNow() {
		return compressExecutor.shutdownNow();
	}
}
