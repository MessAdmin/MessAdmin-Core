package clime.messadmin.taglib;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.TryCatchFinally;

import clime.messadmin.providers.spi.SizeOfProvider;

/**
 * JSP Tag <code>sizeOf</code>, used to get an object size.
 * 
 * @author C&eacute;drik LIME
 */

public class SizeofTag extends TagSupport implements TryCatchFinally {
	protected transient Object object;

	/**
	 * Print the requested object size.
	 * 
	 * @return EVAL_PAGE
	 */
	public final int doEndTag() throws JspException {
		if (object == null && id != null && !"".equals(id.trim())) {//$NON-NLS-1$
			object = pageContext.findAttribute(id);
		}
		String valueStr = getSizeString(object);
		TagUtils.write(pageContext, false, valueStr);
		return EVAL_PAGE;
	}

	/**
	 * @param objectToSize
	 * @return size of objectToSize as String
	 */
	public String getSizeString(Object objectToSize) {
		String valueStr = null;
		try {
			// if sizing an HttpSession, we are really only interested in its attributes!
			if (objectToSize != null && objectToSize instanceof HttpSession) {
				HttpSession session = (HttpSession) objectToSize;
				Map attributes = new HashMap();
				Enumeration enumeration = session.getAttributeNames();
				while (enumeration.hasMoreElements()) {
					String name = (String) enumeration.nextElement();
					Object attribute = session.getAttribute(name);
					attributes.put(name, attribute);
				}
				objectToSize = attributes;
			}
			// user can chose formater in JSP as we write a simple Long
            long currentItemSize = SizeOfProvider.Util.getObjectSize(objectToSize, null);
			valueStr = Long.toString(currentItemSize);
		} catch (IllegalStateException ise) {
			// Session is invalidated: do nothing
		}
		return valueStr;
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
