/**
 * 
 */
package clime.messadmin.providers.sizeof;

import clime.messadmin.providers.spi.SizeOfProvider;

/**
 * Default (and fastest) provider for object sizing.
 * Always returns {@code -1}.
 *
 * @author C&eacute;drik LIME
 */
public class NoopProvider implements SizeOfProvider {

	/**
	 * 
	 */
	public NoopProvider() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPriority() {
		return Integer.MIN_VALUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public long sizeof(Object objectToSize) {
		return -1;
	}

}
