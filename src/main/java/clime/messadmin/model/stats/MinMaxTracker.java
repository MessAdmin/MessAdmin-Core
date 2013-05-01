/**
 *
 */
package clime.messadmin.model.stats;

import java.io.InvalidObjectException;
import java.io.ObjectInputValidation;
import java.io.Serializable;
import java.util.Date;

/**
 * Used for collecting min/max data.
 *
 * @author C&eacute;drik LIME
 */
public class MinMaxTracker extends HitsCounter implements /*RangeStatistic,*/ Serializable, ObjectInputValidation {
	protected volatile long lastValue = 0;

	protected volatile long min = Long.MAX_VALUE;
	/** The time this object was updated for a min */
	protected volatile long minAccessTime = 0;

	protected volatile long max = Long.MIN_VALUE;
	/** The time this object was updated for a max */
	protected volatile long maxAccessTime = 0;

	public MinMaxTracker() {
		super();
	}
	public MinMaxTracker(long min, long max, long current) {
		super();
		this.min = min;
		this.max = max;
		this.lastValue = current;
	}

	public void addValue(long value) {
		addValue(value, System.currentTimeMillis());
	}

	/** Calculate aggregate stats (min, max, etc.) */
	public void addValue(long value, long currentTimeMillis) {
		lastValue += value;
		registerValue(lastValue, currentTimeMillis);
	}

	public void registerValue(long value) {
		registerValue(value, System.currentTimeMillis());
	}

	/** Calculate aggregate stats (min, max, etc.) */
	public void registerValue(long value, long currentTimeMillis) {
		lastValue = value;

		if (value < min) {
			min = value;
			minAccessTime = currentTimeMillis;
		}

		if (value > max) {
			max = value;
			maxAccessTime = currentTimeMillis;
		}

		super.hit(currentTimeMillis);
	}

	public long getLastValue() {
		return lastValue;
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

	public Date getMinAccessTime() {
		return new Date(minAccessTime);
	}

	public Date getMaxAccessTime() {
		return new Date(maxAccessTime);
	}

	/** {@inheritDoc} */
	protected StringBuffer toStringBuffer() {
		StringBuffer buffer = super.toStringBuffer().append(',');
		buffer.append("lastValue=").append(getLastValue()).append(',');//$NON-NLS-1$
		buffer.append("min=").append(getMin()).append(',');//$NON-NLS-1$
		buffer.append("minAccessTime=").append(getMinAccessTime()).append(',');//$NON-NLS-1$
		buffer.append("max=").append(getMax()).append(',');//$NON-NLS-1$
		buffer.append("maxAccessTime=").append(getMaxAccessTime());//$NON-NLS-1$
		return buffer;
	}

	/** {@inheritDoc} */
	public void validateObject() throws InvalidObjectException {
		super.validateObject();
		if (minAccessTime < 0) {
			throw new InvalidObjectException("Negative minAccessTime: " + minAccessTime);
		}
		if (maxAccessTime < 0) {
			throw new InvalidObjectException("Negative maxAccessTime: " + maxAccessTime);
		}
	}
}
