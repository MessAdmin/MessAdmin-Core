/**
 * 
 */
package clime.messadmin.model;

import java.io.Serializable;

/**
 * @author C&eacute;drik LIME
 */
public class ResponseStatusInfo implements Serializable, Cloneable {
	// Reminder: if changing anything here, don't forget to
	// report changes in toString() & valueOf()!
	protected volatile int[] responseStatus = new int[6]; // totals; 1xx to 5xx; 0 is for unset status
	protected volatile int responseStatusRedirect; // 301, 302
	protected volatile int responseStatus304NotModified; // 304
	protected volatile int responseStatus403Forbidden; // 403 (not 401)
	protected volatile int responseStatus404NotFound; // 404

	/**
	 * 
	 */
	public ResponseStatusInfo() {
		super();
	}

	void register(int status) {
		++responseStatus[status/100];
		//responseStatus[status/100].hit(lastResponseDate);
		switch (status) {
			case 301:
			case 302:
				++responseStatusRedirect;
				break;
			case 304:
				++responseStatus304NotModified;
				break;
			case 403:
				++responseStatus403Forbidden;
				break;
			case 404:
				++responseStatus404NotFound;
				break;
			default:
				break;
		}
	}

	/** {@inheritDoc} */
	public int[] getReponseStatus() {
		return responseStatus;
	}

	public int getTotalResponses() {
		int result = 0;
		int[] status = responseStatus.clone();
		for (int i = 0; i < status.length; ++i) {
			result += status[i];
		}
		return result;
	}

	/** {@inheritDoc} */
	public int getResponseStatusRedirect() {
		return responseStatusRedirect;
	}

	/** {@inheritDoc} */
	public float getResponseStatusRedirectPercent() {
		int[] allStatus = getReponseStatus();
		int totalStatus = 0;
		for (int i = 0; i < allStatus.length; ++i) {
			totalStatus += allStatus[i];
		}
		return responseStatusRedirect / (float)totalStatus;
	}

	/** {@inheritDoc} */
	public int getResponseStatus304NotModified() {
		return responseStatus304NotModified;
	}

	/** {@inheritDoc} */
	public float getResponseStatus304NotModifiedPercent() {
		int[] allStatus = getReponseStatus();
		int totalStatus = 0;
		for (int i = 0; i < allStatus.length; ++i) {
			totalStatus += allStatus[i];
		}
		return responseStatus304NotModified / (float)totalStatus;
	}

	/** {@inheritDoc} */
	public int getResponseStatus403Forbidden() {
		return responseStatus403Forbidden;
	}

	/** {@inheritDoc} */
	public float getResponseStatus403ForbiddenPercent() {
		int[] allStatus = getReponseStatus();
		int totalStatus = 0;
		for (int i = 0; i < allStatus.length; ++i) {
			totalStatus += allStatus[i];
		}
		return responseStatus403Forbidden / (float)totalStatus;
	}

	/** {@inheritDoc} */
	public int getResponseStatus404NotFound() {
		return responseStatus404NotFound;
	}

	/** {@inheritDoc} */
	public float getResponseStatus404NotFoundPercent() {
		int[] allStatus = getReponseStatus();
		int totalStatus = 0;
		for (int i = 0; i < allStatus.length; ++i) {
			totalStatus += allStatus[i];
		}
		return responseStatus404NotFound / (float)totalStatus;
	}


	/** {@inheritDoc} */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		ResponseStatusInfo clone = (ResponseStatusInfo) super.clone();
		clone.responseStatus = responseStatus.clone();
		return clone;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder out = new StringBuilder(32);
		for (int i = 0; i < 6; ++i) {
			out.append(responseStatus[i]).append('|');
		}
		out.append(responseStatusRedirect).append('|');
		out.append(responseStatus304NotModified).append('|');
		out.append(responseStatus403Forbidden).append('|');
		out.append(responseStatus404NotFound);
		return out.toString();
	}

	public static ResponseStatusInfo valueOf(String str) {
		String[] parts = str.split("\\|");
		if (parts.length != 10) {
			throw new IllegalArgumentException(str);
		}
		ResponseStatusInfo result = new ResponseStatusInfo();
		for (int i = 0; i < 6; ++i) {
			result.responseStatus[i] = Integer.parseInt(parts[i]);
		}
		result.responseStatusRedirect = Integer.parseInt(parts[6]);
		result.responseStatus304NotModified = Integer.parseInt(parts[7]);
		result.responseStatus403Forbidden = Integer.parseInt(parts[8]);
		result.responseStatus404NotFound = Integer.parseInt(parts[9]);
		return result;
	}
}
