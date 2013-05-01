/**
 *
 */
package clime.messadmin.model.stats;

import java.io.InvalidObjectException;
import java.io.ObjectInputValidation;
import java.io.Serializable;

/**
 * Used for collecting data and building basic statistics.
 *
 * @author C&eacute;drik LIME
 */
public class StatisticsAgregator extends MinMaxTracker implements Serializable, ObjectInputValidation {
	protected volatile double total = 0.0;
	/** Intermediate value used to calculate std dev */
	protected volatile double sumOfSquares = 0.0;

	public StatisticsAgregator() {
		super();
	}

	public StatisticsAgregator(long min, long max) {
		super(min, max, 0);
	}

	/** {@inheritDoc} */
	public void registerValue(long value) {
		registerValue(value, System.currentTimeMillis());
	}

	/** {@inheritDoc} */
	public void registerValue(long value, long currentTimeMillis) {
		total += value;
		sumOfSquares += value * value;
		super.registerValue(value, currentTimeMillis);
	}

	public double getTotal() {
		return total;
	}

	public double getAvg() {
		return (hits == 0) ? 0.0 : total / hits;
	}

	public double getStdDev() {
		double stdDeviation = 0.0;
		if (hits != 0) {
			double sumOfX = total;
			int n = hits;
			int nMinus1 = (n <= 1) ? 1 : n - 1; // avoid 0 divides;

			double numerator = sumOfSquares - ((sumOfX * sumOfX) / n);
			stdDeviation = Math.sqrt(numerator / nMinus1);
		}

		return stdDeviation;
	}

	/** {@inheritDoc} */
	protected StringBuffer toStringBuffer() {
		StringBuffer buffer = super.toStringBuffer().append(',');
		buffer.append("total=").append(getTotal()).append(',');//$NON-NLS-1$
		buffer.append("avg*=").append(getAvg()).append(',');//$NON-NLS-1$
		buffer.append("stdDev*=").append(getStdDev());//$NON-NLS-1$
		return buffer;
	}

	/** {@inheritDoc} */
	public void validateObject() throws InvalidObjectException {
		super.validateObject();
		if (sumOfSquares < 0) {
			throw new InvalidObjectException("Negative sumOfSquares: " + sumOfSquares);
		}
	}
}
