/**
 *
 */
package clime.messadmin.model.stats;

import java.io.InvalidObjectException;
import java.io.ObjectInputValidation;
import java.io.Serializable;

import clime.messadmin.utils.backport.javax.management.j2ee.statistics.CountStatistic;

/**
 * Used for counting hits and building basic statistics.
 *
 * @author C&eacute;drik LIME
 */
public class HitsCounter extends BaseStatistics implements CountStatistic, Serializable, ObjectInputValidation {
	/** The total number of calls to this object's hit() method */
	protected volatile int hits = 0;

	public HitsCounter() {
		super();
	}

	public void hit() {
		hit(System.currentTimeMillis());
	}

	public void hit(long currentTimeMillis) {
		++hits;
		setLastSampleTime(currentTimeMillis);
	}

	/**
	 * {@inheritDoc}
	 * @return int
	 */
	public long getCount() {
		return hits;
	}

//	public Date getFirstAccessTime() {
//		return new Date(firstAccessTime);
//	}

//	public Date getLastAccessTime() {
//		return new Date(lastAccessTime);
//	}

	/** {@inheritDoc} */
	protected StringBuffer toStringBuffer() {
		StringBuffer buffer = super.toStringBuffer().append(',');
		buffer.append("hits=").append(getCount());//.append(',');//$NON-NLS-1$
		return buffer;
	}

	/** {@inheritDoc} */
	public void validateObject() throws InvalidObjectException {
		super.validateObject();
		if (hits < 0) {
			throw new InvalidObjectException("Negative hits: " + hits);
		}
	}
}
