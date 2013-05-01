package clime.messadmin.taglib.core;

import javax.servlet.jsp.JspException;

import clime.messadmin.taglib.TagUtils;

/**
 * <p>Support for handlers of the &lt;out&gt; tag, which simply evalutes and
 * prints the result of the expression it's passed.  If the result is
 * null, we print the value of the 'default' attribute's expression or
 * our body (which two are mutually exclusive, although this constraint
 * is enforced outside this handler, in our TagLibraryValidator).</p>
 * @author C&eacute;drik LIME
 */
public class OutWithClassTag extends OutTag {

	public OutWithClassTag() {
		super();
	}

	/** {@inheritDoc} */
	// evaluates 'value' and determines if the body should be evaluted
	public int doStartTag() throws JspException {
		Object toPrint = null;
		if (value != null) {
			toPrint = value;
		} else if (defaultValue != null) {
			toPrint = defaultValue;
		}
		TagUtils.write(pageContext, false, "<span title=\"");//$NON-NLS-1$
		if (toPrint != null) {
			TagUtils.write(pageContext, true, toPrint.getClass());
		}
		TagUtils.write(pageContext, false, "\">");//$NON-NLS-1$
		return super.doStartTag();
	}

	/** {@inheritDoc} */
	public int doEndTag() throws JspException {
		int returnValue = super.doEndTag();
		TagUtils.write(pageContext, false, "</span>");//$NON-NLS-1$
		return returnValue;
	}

	/** {@inheritDoc} */
	public void release() {
		super.release();
	}
}
