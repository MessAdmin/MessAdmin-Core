package clime.messadmin.taglib;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;

/**
 * Provides helper methods for JSP tags.
 * Some methods from Struts 1.2
 * @author C&eacute;drik LIME
 */
public class TagUtils {

	private TagUtils() {
		super();
	}

	/**
	 * Copied and adapted from org.apache.struts.taglib.TagUtils v1.2.8
	 * 
	 * Write the specified text as the response to the writer associated with
	 * this page.  <strong>WARNING</strong> - If you are writing body content
	 * from the <code>doAfterBody()</code> method of a custom tag class that
	 * implements <code>BodyTag</code>, you should be calling
	 * <code>writePrevious()</code> instead.
	 *
	 * @param pageContext The PageContext object for this page
	 * @param value The text to be written
	 *
	 * @exception JspException if an input/output error occurs (already saved)
	 */
	public static void write(PageContext pageContext, boolean escapeXml, Object value)
			throws JspException {

		if (value == null) {
			return;
		}
		JspWriter writer = pageContext.getOut();

		out(writer, escapeXml, value);
	}

	/**
	 * Copied and adapted from org.apache.struts.taglib.TagUtils v1.2.8
	 * 
	 * Write the specified text as the response to the writer associated with
	 * the body content for the tag within which we are currently nested.
	 *
	 * @param pageContext The PageContext object for this page
	 * @param value The text to be written
	 *
	 * @exception JspException if an input/output error occurs (already saved)
	 */
	public static void writePrevious(PageContext pageContext, boolean escapeXml, Object value)
			throws JspException {

		if (value == null) {
			return;
		}
		JspWriter writer = pageContext.getOut();
		if (writer instanceof BodyContent) {
			writer = ((BodyContent) writer).getEnclosingWriter();
		}

		out(writer, escapeXml, value);
	}

    /**
     * Copied and adapted from org.apache.taglibs.standard.tag.common.core.OutSupport v1.1.2
     * 
     * Outputs <tt>text</tt> to <tt>writer</tt> JspWriter.
     * If <tt>escapeXml</tt> is true, performs the following substring
     * replacements (to facilitate output to XML/HTML pages):
     *
     *    & -> &amp;
     *    < -> &lt;
     *    > -> &gt;
     *    " -> &#034;
     *    ' -> &#039;
     *
     * See also escapeXml().
     */
	protected static void out(JspWriter writer, boolean escapeXml, Object value)
			throws JspException {

		if (value == null) {
			return;
		}

		try {
			String text = value.toString();
			if (escapeXml) {
				writeEscapedXml(text.toCharArray(), text.length(), writer);
			} else {
				writer.print(text);
			}
		} catch (RuntimeException rte) {
			// most likely from value.toString
			// print the error as an HTML comment
			try {
				String text = rte.toString();
				writer.print("<!--");//$NON-NLS-1$
				writeEscapedXml(text.toCharArray(), text.length(), writer);
				writer.print("-->");//$NON-NLS-1$
			} catch (IOException e) {
			}
		} catch (IOException ioe) {
			throw new JspException(ioe.toString());
		}
	}

	/**
	 * Copied and adapted from org.apache.taglibs.standard.tag.common.core.OutSupport v1.1.2
	 * 
	 *  Optimized to create no extra objects and write directly
	 *  to the JspWriter using blocks of escaped and unescaped characters
	 *
	 */
	protected static void writeEscapedXml(char[] buffer, int length, Writer w)
			throws IOException {
		if (null == buffer || null == w) {
			return;
		}
		int start = 0;
		//int length = buffer.length();

		for (int i = 0; i < length; ++i) {
			char c = buffer[i];
			if (c <= HIGHEST_SPECIAL) {
				char[] escaped = specialCharactersRepresentation[c];
				if (escaped != null) {
					// add unescaped portion
					if (start < i) {
						w.write(buffer, start, i - start);
					}
					// add escaped xml
					w.write(escaped);
					start = i + 1;
				}
			}
		}
		// add rest of unescaped portion
		if (start < length) {
			w.write(buffer, start, length - start);
		}
	}

	private static final int HIGHEST_SPECIAL = '>';
	private static char[][] specialCharactersRepresentation = new char[HIGHEST_SPECIAL + 1][];
	static {
		specialCharactersRepresentation['&'] = "&amp;".toCharArray();//$NON-NLS-1$
		specialCharactersRepresentation['<'] = "&lt;".toCharArray();//$NON-NLS-1$
		specialCharactersRepresentation['>'] = "&gt;".toCharArray();//$NON-NLS-1$
		specialCharactersRepresentation['"'] = "&#034;".toCharArray();//$NON-NLS-1$
		specialCharactersRepresentation['\''] = "&#039;".toCharArray();//$NON-NLS-1$
	}
}
