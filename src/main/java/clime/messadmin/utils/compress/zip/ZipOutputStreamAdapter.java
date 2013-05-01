/**
 * 
 */
package clime.messadmin.utils.compress.zip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;

/**
 * Allows to use the same logic with different implementations of {@code ZipOutputStream}
 * @author C&eacute;drik LIME
 */
public interface ZipOutputStreamAdapter {
	OutputStream getDelegate();
	/** @see java.util.zip.ZipOutputStream#setComment(String) */
	void setComment(String comment);
	/** @see java.util.zip.ZipOutputStream#setMethod(int) */
	void setMethod(int method);
	/** @see java.util.zip.ZipOutputStream#setLevel(int) */
	void setLevel(int level);
	/** @see java.util.zip.ZipOutputStream#putNextEntry(ZipEntry) */
	void putNextEntry(ZipEntry e) throws IOException;
	/** @see java.util.zip.ZipOutputStream#closeEntry() */
	void closeEntry() throws IOException;
}

final class JavaZipAdapter implements ZipOutputStreamAdapter {
	private java.util.zip.ZipOutputStream delegate;
	public JavaZipAdapter(java.util.zip.ZipOutputStream delegate) {
		this.delegate = delegate;
	}
	/** {@inheritDoc} */
	public OutputStream getDelegate() {
		return delegate;
	}
	/** {@inheritDoc} */
	public void setComment(String comment) {
		delegate.setComment(comment);
	}
	/** {@inheritDoc} */
	public void setMethod(int method) {
		delegate.setMethod(method);
	}
	/** {@inheritDoc} */
	public void setLevel(int level) {
		delegate.setLevel(level);
	}
	/** {@inheritDoc} */
	public void putNextEntry(ZipEntry e) throws IOException {
		delegate.putNextEntry(e);
	}
	/** {@inheritDoc} */
	public void closeEntry() throws IOException {
		delegate.closeEntry();
	}
};

final class EnhancedJavaZipAdapter implements ZipOutputStreamAdapter {
	private ZipOutputStream delegate;
	public EnhancedJavaZipAdapter(ZipOutputStream delegate) {
		this.delegate = delegate;
	}
	/** {@inheritDoc} */
	public OutputStream getDelegate() {
		return delegate;
	}
	/** {@inheritDoc} */
	public void setComment(String comment) {
		delegate.setComment(comment);
	}
	/** {@inheritDoc} */
	public void setMethod(int method) {
		delegate.setMethod(method);
	}
	/** {@inheritDoc} */
	public void setLevel(int level) {
		delegate.setLevel(level);
	}
	/** {@inheritDoc} */
	public void putNextEntry(ZipEntry e) throws IOException {
		delegate.putNextEntry(e);
	}
	/** {@inheritDoc} */
	public void closeEntry() throws IOException {
		delegate.closeEntry();
	}
};
