/**
 * 
 */
package clime.messadmin.filter;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * @author C&eacute;drik LIME
 */
class CharResponseWrapper extends HttpServletResponseWrapper {
	protected CharArrayWriter output;
	protected ServletOutputStream stream = null;
	protected PrintWriter writer = null;

	static class CharOutputStream extends ServletOutputStream {
		CharArrayWriter outstream;

		public CharOutputStream(CharArrayWriter out) {
			outstream = out;
		}

		/** {@inheritDoc} */
//	  @Override
		public void write(int b) {
			outstream.write(b);
		}
		public void write(String str) throws IOException {
			outstream.write(str);
		}
	}

	/** {@inheritDoc} */
//  @Override
	public String toString() {
		return output.toString();
	}

	public char[] toCharArray() {
		return (output.toCharArray());
	}

	public CharResponseWrapper(HttpServletResponse response) {
		super(response);
		output = new CharArrayWriter();
	}

	/** {@inheritDoc} */
//  @Override
	public synchronized PrintWriter getWriter() {
		if (stream != null) {
			throw new IllegalStateException("getOutputStream() has already been called for this response");//$NON-NLS-1$
		}
		if (writer == null) {
			writer = new PrintWriter(output);
		}
		return writer;
	}

	/** {@inheritDoc} */
//  @Override
	public synchronized ServletOutputStream getOutputStream() {
		if (writer != null) {
			throw new IllegalStateException("getWriter() has already been called for this response");//$NON-NLS-1$
		}
		if (stream == null) {
			stream = new CharOutputStream(output);
		}
		return stream;
	}

}
