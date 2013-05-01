/**
 *
 */
package clime.messadmin.utils.compress.impl;

import java.util.concurrent.atomic.AtomicInteger;

import clime.messadmin.utils.backport.java.util.concurrent.BlockingDeque;
import clime.messadmin.utils.backport.java.util.concurrent.LinkedBlockingDeque;
import clime.messadmin.utils.compress.zip.ZipConfiguration;

/**
 * @author C&eacute;drik LIME
 */
public class BlockManager {
//	protected Logger log = LoggerFactory.getLogger(this.getClass());
	/* used as a blocking Stack (LIFO) */
	private final BlockingDeque<Block> blockPool;
	private final AtomicInteger blockNumber = new AtomicInteger(0);
	private final ZipConfiguration configuration;

	public BlockManager(ZipConfiguration configuration) {
		this.configuration = configuration;
		int blockPoolSize = configuration.getBlockPoolSize();
//		log.debug("Using block pool size of " + blockPoolSize);
		blockPool = new LinkedBlockingDeque<Block>(blockPoolSize);
		for (int i = 0; i < blockPoolSize; ++i) {
			blockPool.addFirst(new Block(configuration));
		}
	}

	public Block getBlockFromPool() throws InterruptedException {
		Block block = blockPool.takeFirst();
		block.initializeIfNeeded(configuration);
		block.blockNumber = blockNumber.getAndIncrement();
		return block;
	}

	public void releaseBlockToPool(Block block) throws InterruptedException {
		block.waitUntilCanRecycle();//FIXME wait in another thread, do not block caller!
		forceReleaseBlockToPool(block);
	}

	public void forceReleaseBlockToPool(Block block) throws InterruptedException {
		assert ! blockPool.contains(block);
		block.reset();
		blockPool.putFirst(block);
	}
}
