/**
 * 
 */
package clime.messadmin.taglib.fmt;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import clime.messadmin.taglib.core.Util;
import clime.messadmin.utils.DateUtils;

/**
 * @author C&eacute;drik LIME
 */
public class FormatTimeIntervalTag extends TagSupport {

	// *********************************************************************
	// Protected state

	protected long value; // 'value' attribute

	// *********************************************************************
	// Private state

	private String var; // 'var' attribute
	private int scope; // 'scope' attribute

	// *********************************************************************
	// Constructor and initialization

	public FormatTimeIntervalTag() {
		super();
		init();
	}

	private void init() {
		var = null;
		value = -1;
		scope = PageContext.PAGE_SCOPE;
	}

	// *********************************************************************
	// Tag attributes known at translation time

	public void setVar(String var) {
		this.var = var;
	}

	public void setScope(String scope) {
		this.scope = Util.getScope(scope);
	}

	// *********************************************************************
	// Tag logic

	/*
	 * Formats the given date and time.
	 */
	public int doEndTag() throws JspException {

		String formatted = null;

		if (value == -1) {
			if (var != null) {
				pageContext.removeAttribute(var, scope);
			}
			return EVAL_PAGE;
		}

		formatted = DateUtils.timeIntervalToFormattedString(value);

		if (var != null) {
			pageContext.setAttribute(var, formatted, scope);
		} else {
			try {
				pageContext.getOut().print(formatted);
			} catch (IOException ioe) {
				throw new JspTagException(ioe.toString());//, ioe);
			}
		}

		return EVAL_PAGE;
	}

	// Releases any resources we may have (or inherit)
	public void release() {
		super.release();
		init();
	}

    //*********************************************************************
	// Accessor methods

    // 'value' attribute
	public void setValue(long value) throws JspTagException {
		this.value = value;
	}
	public void setValue(int value) throws JspTagException {
		this.value = value;
	}
}
