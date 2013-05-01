package clime.messadmin.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TryCatchFinally;

import clime.messadmin.providers.spi.SerializableProvider;

/**
 * JSP Tag <code>serializable</code>, used to get the serializable state of an object.
 * 
 * @author C&eacute;drik LIME
 */

public class NotSerializableTag extends BodyTagSupport implements TryCatchFinally {
	protected transient Object object;

	/**
	 * Print the requested object size.
	 * 
	 * @return EVAL_PAGE
	 */
	public final int doStartTag() throws JspException {
		if (object == null && id != null && !"".equals(id.trim())) {//$NON-NLS-1$
			object = pageContext.findAttribute(id);
		}
		boolean serializable;
		try {
			serializable = ! SerializableProvider.Util.isSerializable(object, null);
		} catch (RuntimeException rte) {
			serializable = false;
		}
		return serializable ? EVAL_BODY_INCLUDE : SKIP_BODY;
	}

	/** {@inheritDoc} */
	public void release() {
		object = null;
		super.release();
	}

	/**
	 * @param object The object to set.
	 */
	public void setObject(Object object) {
		this.object = object;
	}

	/** {@inheritDoc} */
	public void doCatch(Throwable t) throws Throwable {
		if (! (t instanceof IllegalStateException)) {
			throw t;
		}
	}

	/** {@inheritDoc} */
	public void doFinally() {
		release();
	}
}
