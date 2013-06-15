package clime.messadmin.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Used for counting resquest size
 * @author C&eacute;drik LIME
 */
public class MessAdminRequestWrapper extends HttpServletRequestWrapper {
	protected long requestBodyLength = -1;
	private BufferedReader decryptedDataReader;
	private ServletInputStream decryptedDataInputStream;

	class DelegatingServletInputStream extends ServletInputStream {
		private final InputStream sourceStream;

		/**
		 * Create a new DelegatingServletInputStream.
		 * @param sourceStream the sourceStream InputStream
		 */
		public DelegatingServletInputStream(InputStream sourceStream) {
			this.sourceStream = sourceStream;
		}

		/** {@inheritDoc} */
		@Override
		public int read() throws IOException {
			int value = sourceStream.read();
			++requestBodyLength;
			return value;
		}
		/** {@inheritDoc} */
		@Override
		public int available() throws IOException {
			return sourceStream.available();
		}
		/** {@inheritDoc} */
		@Override
		public void close() throws IOException {
			super.close();
			sourceStream.close();
		}
		/** {@inheritDoc} */
		@Override
		public void mark(int readlimit) {
			sourceStream.mark(readlimit);
		}
		/** {@inheritDoc} */
		@Override
		public void reset() throws IOException {
			sourceStream.reset();
		}
		/** {@inheritDoc} */
		@Override
		public boolean markSupported() {
			return sourceStream.markSupported();
		}
	}

	class DelegatingBufferedReader extends BufferedReader {
		DelegatingBufferedReader(BufferedReader sourceReader) {
			super(sourceReader, 1);// source is already buffered
		}
		/** {@inheritDoc} */
		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			int returnValue = super.read(cbuf, off, len);
			if (returnValue != -1) {
				requestBodyLength += returnValue;
			}
			return returnValue;
		}
	}

	public MessAdminRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized BufferedReader getReader() throws IOException {
		if (null == decryptedDataReader) {
			decryptedDataReader = new DelegatingBufferedReader(super.getReader());
		}
		return decryptedDataReader;
	}

	/** {@inheritDoc} */
	@Override
	public synchronized ServletInputStream getInputStream() throws IOException {
		if (null == decryptedDataInputStream) {
			decryptedDataInputStream = new DelegatingServletInputStream(super.getInputStream());
		}
		return decryptedDataInputStream;
	}

	/**
	 * @return the total request length (header + body)
	 */
	public long getRequestLength() {
		long result = getRequestBodyLength();
		if (result == -1) {
			result = 0; // reset
		}
		result += getHeadersSize();
		return result;
	}

	protected long getRequestBodyLength() {
		return (requestBodyLength == -1) ?
				((HttpServletRequest) super.getRequest()).getContentLength()
				: requestBodyLength + 1;
	}

	public static long getHeadersSize(HttpServletRequest request) {
		Enumeration headerNames = request.getHeaderNames();
		if (headerNames == null) {
			return 0;
		} //else
		long size = 0;
		while (headerNames.hasMoreElements()) {
			String headerName = (String) headerNames.nextElement();
			Enumeration headerValues = request.getHeaders(headerName);
			if (headerValues != null) {
				while (headerValues.hasMoreElements()) {
					String headerValue = (String) headerValues.nextElement();
					size += headerName.length() + 3 + headerValue.length();//+3: "<name>: value\n"
				}
			}
		}
		return size + 1;//+1: empty line separator between headers and body
	}
	protected long getHeadersSize() {
		HttpServletRequest request = (HttpServletRequest) super.getRequest();
		return getHeadersSize(request);
	}
}
