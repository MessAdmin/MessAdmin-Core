/**
 *
 */
package clime.messadmin.model;

import java.io.Serializable;
import java.util.Date;

import clime.messadmin.model.stats.StatisticsAgregator;

/**
 * @author C&eacute;drik LIME
 */
public class RequestInfo implements Serializable {
	protected String url;

	//protected volatile int hits = 0;//number of hits == usedTime.getCount()

	protected StatisticsAgregator requestLength = new StatisticsAgregator(Long.MAX_VALUE, -1);//bytes
	protected volatile long lastRequestDate = -1;// java.util.Date;
	protected StatisticsAgregator responseLength = new StatisticsAgregator(Long.MAX_VALUE, -1);//bytes
	protected volatile long lastResponseDate = -1;// java.util.Date;

	protected StatisticsAgregator usedTime = new StatisticsAgregator(Long.MAX_VALUE, -1);//milliseconds

	protected volatile int lastResponseStatus;
	protected ResponseStatusInfo responseStatus = new ResponseStatusInfo();

	protected volatile int nErrors = 0;
	protected ErrorData lastError;

	/**
	 *
	 */
	public RequestInfo(String url) {
		super();
		this.url = url;
//		for (int i = 0; i < responseStatus.length; ++i) {
//			responseStatus[i] = new HitsCounter();
//		}
	}

	/** {@inheritDoc} */
	public String getURL() {
		return url;
	}

	/** {@inheritDoc} */
	public int getHits() {
		return (int) usedTime.getCount();
	}

	/** {@inheritDoc} */
	public int getNErrors() {
		return nErrors;
	}

	/** {@inheritDoc} */
	public ErrorData getLastError() {
		return lastError;
	}

	/** {@inheritDoc} */
	public long getRequestLastLength() {
		return requestLength.getLastValue();
	}

	/** {@inheritDoc} */
	public long getResponseLastLength() {
		return responseLength.getLastValue();
	}

	/** {@inheritDoc} */
	public long getRequestMinLength() {
		return requestLength.getMin();
	}

	/** {@inheritDoc} */
	public long getResponseMinLength() {
		return responseLength.getMin();
	}

	/** {@inheritDoc} */
	public Date getRequestMinLengthDate() {
		return requestLength.getMinAccessTime();
	}

	/** {@inheritDoc} */
	public Date getResponseMinLengthDate() {
		return responseLength.getMinAccessTime();
	}

	/** {@inheritDoc} */
	public long getRequestMaxLength() {
		return requestLength.getMax();
	}

	/** {@inheritDoc} */
	public long getResponseMaxLength() {
		return responseLength.getMax();
	}

	/** {@inheritDoc} */
	public Date getRequestMaxLengthDate() {
		return requestLength.getMaxAccessTime();
	}

	/** {@inheritDoc} */
	public Date getResponseMaxLengthDate() {
		return responseLength.getMaxAccessTime();
	}

	/** {@inheritDoc} */
	public long getRequestTotalLength() {
		return (long) requestLength.getTotal();
	}

	/** {@inheritDoc} */
	public long getResponseTotalLength() {
		return (long) responseLength.getTotal();
	}

	/** {@inheritDoc} */
	public double getRequestMeanLength() {
		return requestLength.getAvg();
	}

	/** {@inheritDoc} */
	public double getResponseMeanLength() {
		return responseLength.getAvg();
	}

	/** {@inheritDoc} */
	public double getRequestStdDevLength() {
		return requestLength.getStdDev();
	}

	/** {@inheritDoc} */
	public double getResponseStdDevLength() {
		return responseLength.getStdDev();
	}

	/** {@inheritDoc} */
	public Date getLastRequestDate() {
		return new Date(lastRequestDate); //requestLength.getLastAccessTime();
	}

	/** {@inheritDoc} */
	public Date getLastResponseDate() {
		return new Date(lastResponseDate); //responseLength.getLastAccessTime();
	}

	/** {@inheritDoc} */
	public int getLastResponseStatus() {
		return lastResponseStatus;
	}

	/** {@inheritDoc} */
	public ResponseStatusInfo getResponseStatusInfo() {
		return responseStatus;
	}

	/** {@inheritDoc} */
	public long getLastUsedTime() {
		return usedTime.getLastValue();
	}

	/** {@inheritDoc} */
	public long getMinUsedTime() {
		return usedTime.getMin();
	}

	/** {@inheritDoc} */
	public Date getMinUsedTimeDate() {
		return usedTime.getMinAccessTime();
	}

	/** {@inheritDoc} */
	public long getMaxUsedTime() {
		return usedTime.getMax();
	}

	/** {@inheritDoc} */
	public Date getMaxUsedTimeDate() {
		return usedTime.getMaxAccessTime();
	}

	/** {@inheritDoc} */
	public long getTotalUsedTime() {
		return (long) usedTime.getTotal();
	}

	/** {@inheritDoc} */
	public double getMeanUsedTime() {
		return usedTime.getAvg();
	}

	/** {@inheritDoc} */
	public double getStdDevUsedTime() {
		return usedTime.getStdDev();
	}
}
