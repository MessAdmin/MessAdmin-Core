/**
 *
 */
package clime.messadmin.model.stats;

import java.io.InvalidObjectException;
import java.io.ObjectInputValidation;
import java.io.Serializable;

import clime.messadmin.utils.backport.javax.management.j2ee.statistics.Statistic;

/**
 * Base Statistics class.
 *
 * @author C&eacute;drik LIME
 */
abstract class BaseStatistics implements Statistic, Serializable, ObjectInputValidation {

	/** The first time this object was updated */
//	protected volatile long startTime = 0;
	/** The last time this object was updated */
//	protected volatile long lastSampleTime = 0;

	public BaseStatistics() {
		super();
	}

	public void setLastSampleTime(long now) {
		// set the first and last access times.
//		if (startTime <= 0) {
//			startTime = now;
//		}
//		lastSampleTime = now;
	}

	/**
	 * {@inheritDoc}
	 * @return Empty string (not implemented)
	 * @throws UnsupportedOperationException
	 */
	public String getName() {
//		return "";
		throw new UnsupportedOperationException();
	}
	/**
	 * {@inheritDoc}
	 * @return Empty string (not implemented)
	 * @throws UnsupportedOperationException
	 */
	public String getUnit() {
//		return "";
		throw new UnsupportedOperationException();
	}
	/**
	 * {@inheritDoc}
	 * @return Empty string (not implemented)
	 * @throws UnsupportedOperationException
	 */
	public String getDescription() {
//		return "";
		throw new UnsupportedOperationException();
	}
	/**
	 * {@inheritDoc}
	 * @return 0 (not implemented)
	 */
	public long getStartTime() {
//		return startTime;
		return 0;
	}
	/**
	 * {@inheritDoc}
	 * @return 0 (not implemented)
	 */
	public long getLastSampleTime() {
//		return lastSampleTime;
		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return toStringBuffer().append(']').toString();
	}
	protected StringBuffer toStringBuffer() {
		StringBuffer buffer = new StringBuffer(128);
		buffer.append(getClass().getName()).append('[');
//		buffer.append("startTime=").append(getStartTime()).append(',');//$NON-NLS-1$
//		buffer.append("lastSampleTime=").append(getLastSampleTime());//$NON-NLS-1$
		return buffer;
	}

	/** {@inheritDoc} */
	public void validateObject() throws InvalidObjectException {
//		if (startTime < 0) {
//			throw new InvalidObjectException("Negative startTime: " + startTime);
//		}
//		if (lastSampleTime < 0) {
//			throw new InvalidObjectException("Negative lastSampleTime: " + lastSampleTime);
//		}
	}
}
