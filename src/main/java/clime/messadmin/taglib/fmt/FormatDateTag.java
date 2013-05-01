/**
 * 
 */
package clime.messadmin.taglib.fmt;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import clime.messadmin.taglib.core.Util;

/**
 * @author C&eacute;drik LIME
 */
public class FormatDateTag extends TagSupport {

    //*********************************************************************
    // Private constants

    private static final String DATE = "date";//$NON-NLS-1$
    private static final String TIME = "time";//$NON-NLS-1$
    private static final String DATETIME = "both";//$NON-NLS-1$

    //*********************************************************************
    // Protected state

    protected Date value;                        // 'value' attribute
    protected String type;                       // 'type' attribute
    protected String pattern;                    // 'pattern' attribute
    protected Object timeZone;                   // 'timeZone' attribute
    protected String dateStyle;                  // 'dateStyle' attribute
    protected String timeStyle;                  // 'timeStyle' attribute

    //*********************************************************************
    // Private state

    private String var;                          // 'var' attribute
    private int scope;                           // 'scope' attribute

    //*********************************************************************
    // Constructor and initialization

    public FormatDateTag() {
        super();
        init();
    }

    private void init() {
        type = dateStyle = timeStyle = null;
        pattern = var = null;
        value = null;
        timeZone = null;
        scope = PageContext.PAGE_SCOPE;
    }

    //*********************************************************************
    // Tag attributes known at translation time

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    //*********************************************************************
    // Tag logic

    /*
     * Formats the given date and time.
     */
    public int doEndTag() throws JspException {

        String formatted = null;

        if (value == null) {
            if (var != null) {
                pageContext.removeAttribute(var, scope);
            }
            return EVAL_PAGE;
        }

        // Create formatter
        Locale locale = SetLocaleTag.getFormattingLocale(
                pageContext,
                this,
                true,
                DateFormat.getAvailableLocales());

        if (locale != null) {
            DateFormat formatter = createFormatter(locale, pattern);

            // Set time zone
            TimeZone tz = null;
            if ((timeZone instanceof String)
                    && ((String) timeZone).equals("")) {//$NON-NLS-1$
                timeZone = null;
            }
            if (timeZone != null) {
                if (timeZone instanceof String) {
                    tz = TimeZone.getTimeZone((String) timeZone);
                } else if (timeZone instanceof TimeZone) {
                    tz = (TimeZone) timeZone;
                } else {
                    throw new JspTagException("In &lt;formatDate&gt;, 'timeZone' must be an instance of java.lang.String or java.util.TimeZone");//$NON-NLS-1$
                }
            }
            if (tz != null) {
                formatter.setTimeZone(tz);
            }
            formatted = formatter.format(value);
        } else {
            // no formatting locale available, use Date.toString()
            formatted = value.toString();
        }

        if (var != null) {
            pageContext.setAttribute(var, formatted, scope);
        } else {
            try {
                pageContext.getOut().print(formatted);
            } catch (IOException ioe) {
            	JspTagException jte = new JspTagException(ioe.toString());
                jte.initCause(ioe);
                throw jte;//FIXME JSP 2.0: new JspTagException(ioe.toString() ioe);
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
    // Private utility methods

    private DateFormat createFormatter(Locale loc, String pattern) throws JspException {
        // Apply pattern, if present
        if (pattern != null) {
            return new SimpleDateFormat(pattern, loc);
        }

        if ((type == null) || DATE.equalsIgnoreCase(type)) {
            int style = Util.getStyle(dateStyle, "In &lt;formatDate&gt;, invalid 'dateStyle' attribute: \"" + dateStyle + '"');//$NON-NLS-1$
            return DateFormat.getDateInstance(style, loc);
        } else if (TIME.equalsIgnoreCase(type)) {
            int style = Util.getStyle(timeStyle, "In &lt;formatDate&gt;, invalid 'timeStyle' attribute: \"" + timeStyle + '"');//$NON-NLS-1$
            return DateFormat.getTimeInstance(style, loc);
        } else if (DATETIME.equalsIgnoreCase(type)) {
            int style1 = Util.getStyle(dateStyle, "In &lt;formatDate&gt;, invalid 'dateStyle' attribute: \"" + dateStyle + '"');//$NON-NLS-1$
            int style2 = Util.getStyle(timeStyle, "In &lt;formatDate&gt;, invalid 'timeStyle' attribute: \"" + timeStyle + '"');//$NON-NLS-1$
            return DateFormat.getDateTimeInstance(style1, style2, loc);
        } else {
            throw new JspException("In &lt;formatDate&gt;, invalid 'type' attribute: \"" + type + '"');//$NON-NLS-1$
        }

    }

    //*********************************************************************
    // Accessor methods

    // 'value' attribute
    public void setValue(Date value) throws JspTagException {
        this.value = value;
    }
    public void setValue(long milliseconds) throws JspTagException {
        this.value = new Date(milliseconds);
    }

    // 'type' attribute
    public void setType(String type) throws JspTagException {
        this.type = type;
    }

    // 'dateStyle' attribute
    public void setDateStyle(String dateStyle) throws JspTagException {
        this.dateStyle = dateStyle;
    }

    // 'timeStyle' attribute
    public void setTimeStyle(String timeStyle) throws JspTagException {
        this.timeStyle = timeStyle;
    }

    // 'pattern' attribute
    public void setPattern(String pattern) throws JspTagException {
        this.pattern = pattern;
    }

    // 'timeZone' attribute
    public void setTimeZone(Object timeZone) throws JspTagException {
        this.timeZone = timeZone;
    }
}
