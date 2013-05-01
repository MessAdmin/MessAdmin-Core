/**
 *
 */
package clime.messadmin.utils;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Comparator;

/**
 * Compares 2 IPs in either {@link String} or {@link InetAddress} format
 * @author C&eacute;drik LIME
 */
public final class IPComparator implements Comparator, Serializable {

	private static final long serialVersionUID = 815044383151815300L;

	/**
	 *
	 */
	public IPComparator() {
		super();
	}

	protected void checkType(Object ip) throws ClassCastException {
		if ( ! (ip instanceof String || ip instanceof InetAddress)) {
			throw new ClassCastException(String.valueOf(ip));
		}
	}

	protected byte[] toParts(Object ip) throws IllegalArgumentException {
		assert ip != null;
		if (ip instanceof String) {
			try {
				return InetAddress.getByName((String)ip).getAddress();
			} catch (UnknownHostException uhe) {
				IllegalArgumentException iae = new IllegalArgumentException(((String)ip) + ": " + uhe);
				iae.initCause(uhe);
				throw iae;
			}
		} else if (ip instanceof InetAddress) {
			return ((InetAddress)ip).getAddress();
		} else {
			throw new ClassCastException(String.valueOf(ip));
		}
	}

	/** {@inheritDoc} */
	public int compare(Object ip1, Object ip2) {
		if (ip1 == ip2) {
			return 0;
		}
		if (ip1 == null) {
			return Integer.MIN_VALUE;
		}
		if (ip2 == null) {
			return Integer.MAX_VALUE;
		}
		if (ip1.equals(ip2)) {
			return 0;
		}
		checkType(ip1);
		checkType(ip2);
		byte[] parts1 = toParts(ip1);
		byte[] parts2 = toParts(ip2);
//		assert parts1.length == parts2.length;
		int index = 0;
		while (index < parts1.length) {
			if (parts1[index] != parts2[index]) {
				return (parts1[index] - parts2[index]) << (parts1.length - index);
			} else {
				++index;
			}
		}

		return 0;
	}

	/** {@inheritDoc} */
	public boolean equals(Object obj) {
		return super.equals(obj) || (obj != null && obj.getClass() == this.getClass());
	}

	/** {@inheritDoc} */
	public int hashCode() {
		return 151815300;
	}
}
