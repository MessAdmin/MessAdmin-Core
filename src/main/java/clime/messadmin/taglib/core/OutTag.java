package clime.messadmin.taglib.core;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import clime.messadmin.taglib.TagUtils;

/**
 * <p>Support for handlers of the &lt;out&gt; tag, which simply evalutes and
 * prints the result of the expression it's passed.  If the result is
 * null, we print the value of the 'default' attribute's expression or
 * our body (which two are mutually exclusive, although this constraint
 * is enforced outside this handler, in our TagLibraryValidator).</p>
 * @author C&eacute;drik LIME
 */
public class OutTag extends BodyTagSupport {
	protected Object value = null;
    protected Object defaultValue = null;
	protected boolean escapeXml = true;
	private boolean needBody = false;

	public OutTag() {
		super();
	}

	/** {@inheritDoc} */
	// evaluates 'value' and determines if the body should be evaluted
	public int doStartTag() throws JspException {
		needBody = false;
		this.bodyContent = null; // clean-up body (just in case container is pooling tag handlers)
		Object toPrint = null;
		if (value != null) {
			toPrint = value;
		} else if (defaultValue != null) {
			toPrint = defaultValue;
		}
		if (toPrint != null) {
			TagUtils.write(pageContext, escapeXml, toPrint);
			return SKIP_BODY;
		} else {
			needBody = true;
			return EVAL_BODY_BUFFERED;
		}
	}

	/** {@inheritDoc} */
	public int doEndTag() throws JspException {
		if (needBody && bodyContent != null && bodyContent.getString() != null) {
			// trim and print out the body
			TagUtils.write(pageContext, escapeXml, bodyContent.getString().trim());
		}
		return EVAL_PAGE;
	}

	/** {@inheritDoc} */
	public void release() {
		value = null;
		defaultValue = null;
		escapeXml = true;
		needBody = false;
		super.release();
	}

	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
// DO NOT uncomment, or else face the wrath of WebLogic 9...
//	public void setValue(long value) {
//		this.value = new Long(value); // Long.valueOf(value);
//	}

	public Object getDefault() {
		return defaultValue;
	}
	public void setDefault(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
// DO NOT uncomment, or else face the wrath of WebLogic 9...
//	public void setDefault(long defaultValue) {
//		this.defaultValue = new Long(defaultValue); // Long.valueOf(defaultValue);
//	}

	public boolean isEscapeXml() {
		return escapeXml;
	}
	public void setEscapeXml(boolean escapeXml) {
		this.escapeXml = escapeXml;
	}
}
