/**
 *
 */
package clime.messadmin.utils.compress.impl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import clime.messadmin.utils.compress.zip.ZipConfiguration;

/**
 * @author C&eacute;drik LIME
 */
public abstract class AbstractSerialExecutor implements Runnable {
//	protected Logger log = LoggerFactory.getLogger(this.getClass());
	private final BlockingQueue<Block> tasks;
	private volatile boolean finished = false;
	protected ZipConfiguration configuration;

	public AbstractSerialExecutor(ZipConfiguration configuration) {
		super();
		this.configuration = configuration;
//		log.debug("Using block queue size of " + configuration.getBlockPoolSize());
		tasks = new ArrayBlockingQueue<Block>(configuration.getBlockPoolSize());
	}

	public void submit(Block block) {
		try {
			tasks.put(block);
		} catch (InterruptedException ignore) {
		}
	}

	public void prepareForInterrupt() {
		finished = true;
		tasks.clear();
	}

	/** {@inheritDoc} */
	public void run() {
		while (! finished) {
			try {
				Block block = tasks.take();
				if (block.isLastBlock()) {
					finished = true;
				}
				process(block);
			} catch (InterruptedException ignore) {
				// Same player try again. Hopefully finished == true
			} catch (Exception e) {
				throw (e instanceof RuntimeException) ? (RuntimeException)e : new RuntimeException(e);
			}
		}
	}

	protected abstract void process(Block block) throws Exception;
}
