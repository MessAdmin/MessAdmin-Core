/**
 *
 */
package clime.messadmin.taglib.fmt;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.taglib.core.Util;
import clime.messadmin.utils.BytesFormat;

/**
 * @author C&eacute;drik LIME
 */
public class FormatNumberTag extends BodyTagSupport {
	private static final String BUNDLE_NAME = FormatNumberTag.class.getName();

	protected static final int DEFAULT_FRACTIONS_DIGITS = 2;

	// *********************************************************************
	// Private constants

	private static final String NUMBER = "number";//$NON-NLS-1$
	private static final String PERCENT = "percent";//$NON-NLS-1$
	private static final String BYTES = "bytes";//$NON-NLS-1$

	// *********************************************************************
	// Protected state

	protected Object value; // 'value' attribute
	protected boolean valueSpecified; // status
	protected String type; // 'type' attribute
	protected String pattern; // 'pattern' attribute
	protected boolean isGroupingUsed; // 'groupingUsed' attribute
	protected boolean groupingUsedSpecified;

	// *********************************************************************
	// Private state

	private String var; // 'var' attribute
	private int scope; // 'scope' attribute

	// *********************************************************************
	// Constructor and initialization

	public FormatNumberTag() {
		super();
		init();
	}

	private void init() {
		value = type = null;
		valueSpecified = false;
		pattern = var = null;
		groupingUsedSpecified = false;
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

	public int doEndTag() throws JspException {
		String formatted = null;
		Object input = null;

		// determine the input by...
		if (valueSpecified) {
			// ... reading 'value' attribute
			input = value;
		} else {
			// ... retrieving and trimming our body
			if (bodyContent != null && bodyContent.getString() != null) {
				input = bodyContent.getString().trim();
			}
		}

		if ((input == null) || input.equals("")) {//$NON-NLS-1$
			// Spec says:
			// If value is null or empty, remove the scoped variable
			// if it is specified (see attributes var and scope).
			if (var != null) {
				pageContext.removeAttribute(var, scope);
			}
			return EVAL_PAGE;
		}

		formatted = getFormattedString(input);

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

	/**
	 * @param input
	 * @return
	 * @throws JspException
	 */
	public String getFormattedString(Object input) throws JspException {
		String formatted;
		/*
		 * If 'value' is a String, it is first parsed into an instance of
		 * java.lang.Number
		 */
		if (input instanceof String) {
			try {
				if (((String) input).indexOf('.') != -1) {
					input = Double.valueOf((String) input);
				} else {
					input = Long.valueOf((String) input);
				}
			} catch (NumberFormatException nfe) {
				throw new JspException("In &lt;formatNumber&gt;, 'value' attribute can not be parsed into java.lang.Number: \""+input+'"', nfe);//$NON-NLS-1$
			}
		}

		// Determine formatting locale
		Locale loc = SetLocaleTag.getFormattingLocale(
				pageContext,
				this,
				true,
				NumberFormat.getAvailableLocales());

		if (loc != null) {
			// Create formatter
			NumberFormat formatter = null;
			if ((pattern != null) && !pattern.equals("")) {//$NON-NLS-1$
				// if 'pattern' is specified, 'type' is ignored
				DecimalFormatSymbols symbols = new DecimalFormatSymbols();
				formatter = new DecimalFormat(pattern, symbols);
			} else {
				formatter = createFormatter(loc);
			}
			configureFormatter(formatter);
			if (BYTES.equalsIgnoreCase(type)) {
				NumberFormat formatterPlain = NumberFormat.getNumberInstance(loc);
				configureFormatter(formatterPlain);
				String inputWithBytes = I18NSupport.getLocalizedMessage(BUNDLE_NAME, "bytes", new Object[] {formatterPlain.format(input)});//$NON-NLS-1$
				formatted = new StringBuffer()
					.append("<span title=\"").append(inputWithBytes).append("\">")//$NON-NLS-1$//$NON-NLS-2$
					.append(formatter.format(input)).append("</span>").toString();//$NON-NLS-1$
			} else {
				formatted = formatter.format(input);
			}
		} else {
			// no formatting locale available, use toString()
			formatted = input.toString();
		}
		return formatted;
	}

	// Releases any resources we may have (or inherit)
	public void release() {
		super.release();
		init();
	}

	// *********************************************************************
	// Private utility methods

	private NumberFormat createFormatter(Locale loc) throws JspException {
		if (loc == null) {
			loc = Locale.getDefault();
		}
		NumberFormat formatter = null;

		if ((type == null) || NUMBER.equalsIgnoreCase(type)) {
			formatter = NumberFormat.getNumberInstance(loc);
		} else if (PERCENT.equalsIgnoreCase(type)) {
			formatter = NumberFormat.getPercentInstance(loc);
		} else if (BYTES.equalsIgnoreCase(type)) {
			formatter = BytesFormat.getBytesInstance(loc, true);
		} else {
			throw new JspException("In &lt;parseNumber&gt;, invalid 'type' attribute: \""+type+'"');//$NON-NLS-1$
		}

		return formatter;
	}

	/*
	 * Applies the 'groupingUsed' attributes to the given formatter.
	 */
	private void configureFormatter(NumberFormat formatter) {
		if (groupingUsedSpecified) {
			formatter.setGroupingUsed(isGroupingUsed);
		}
		formatter.setMaximumFractionDigits(DEFAULT_FRACTIONS_DIGITS);
	}

	// *********************************************************************
	// Accessor methods

	// 'value' attribute
	public void setValue(Object value) throws JspTagException {
		this.value = value;
		this.valueSpecified = true;
	}
	public void setValue(long value) throws JspTagException {
		this.value = Long.valueOf(value);
		this.valueSpecified = true;
	}
	public void setValue(int value) throws JspTagException {
		this.value = Integer.valueOf(value);
		this.valueSpecified = true;
	}
	public void setValue(double value) throws JspTagException {
		this.value = new Double(value);
		this.valueSpecified = true;
	}
	public void setValue(float value) throws JspTagException {
		this.value = new Float(value);
		this.valueSpecified = true;
	}

	// 'type' attribute
	public void setType(String type) throws JspTagException {
		this.type = type;
	}

	// 'pattern' attribute
	public void setPattern(String pattern) throws JspTagException {
		this.pattern = pattern;
	}

	// 'groupingUsed' attribute
	public void setGroupingUsed(boolean isGroupingUsed) throws JspTagException {
		this.isGroupingUsed = isGroupingUsed;
		this.groupingUsedSpecified = true;
	}
}
