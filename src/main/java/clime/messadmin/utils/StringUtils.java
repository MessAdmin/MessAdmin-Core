/**
 *
 */
package clime.messadmin.utils;

/**
 * Slightly modified copy of org.apache.commons.lang.StringUtils 2.6 and org.apache.taglibs.standard.tag.common.core.Util 1.1.2
 * @author C&eacute;drik LIME
 */
public class StringUtils {

	private StringUtils() {
	}

	/**
	 * Checks if a String is empty ("") or null.
	 *
	 * @param str  the String to check, may be null
	 * @return <code>true</code> if the String is empty or null
	 */
	public static boolean isEmpty(CharSequence str) {
		return str == null || str.length() == 0;
	}

	/**
	 * Checks if a String is not empty ("") and not null.
	 *
	 * @param str  the String to check, may be null
	 * @return <code>true</code> if the String is not empty and not null
	 */
	public static boolean isNotEmpty(CharSequence str) {
		return str != null && str.length() > 0;
	}

	/**
	 * Checks if a String is whitespace, empty ("") or null.
	 *
	 * @param str  the String to check, may be null
	 * @return <code>true</code> if the String is null, empty or whitespace
	 */
	public static boolean isBlank(CharSequence str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; ++i) {
			if ( ! Character.isWhitespace(str.charAt(i)) ) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if a String is not empty (""), not null and not whitespace only.
	 *
	 * @param str  the String to check, may be null
	 * @return <code>true</code> if the String is not empty and not null and not whitespace
	 */
	public static boolean isNotBlank(CharSequence str) {
		return ! isBlank(str);
	}

	/**
	 * <p>Replaces all occurrences of a String within another String.</p>
	 *
	 * <p>A <code>null</code> reference passed to this method is a no-op.</p>
	 *
	 * @param text  text to search and replace in, may be null
	 * @param searchString  the String to search for, may be null
	 * @param replacement  the String to replace it with, may be null
	 * @return the text with any replacements processed,
	 *  <code>null</code> if null String input
	 */
	public static String replace(String text, String searchString, String replacement) {
		if (isEmpty(text) || isEmpty(searchString) || replacement == null) {
			return text;
		}
		int start = 0;
		int end = text.indexOf(searchString, start);
		if (end == -1) {
			return text;
		}
		int replLength = searchString.length();
		int increase = replacement.length() - replLength;
		increase = (increase < 0 ? 0 : increase);
		increase *= 16;
		StringBuilder buf = new StringBuilder(text.length() + increase);
		while (end != -1) {
			buf.append(text.substring(start, end)).append(replacement);
			start = end + replLength;
			end = text.indexOf(searchString, start);
		}
		buf.append(text.substring(start));
		return buf.toString();
	}


	private static final int HIGHEST_SPECIAL = '>';
	private static char[][] specialCharactersXMLRepresentation = new char[HIGHEST_SPECIAL + 1][];
	static {
		specialCharactersXMLRepresentation['&'] = "&amp;".toCharArray();//$NON-NLS-1$
		specialCharactersXMLRepresentation['<'] = "&lt;".toCharArray();//$NON-NLS-1$
		specialCharactersXMLRepresentation['>'] = "&gt;".toCharArray();//$NON-NLS-1$
		specialCharactersXMLRepresentation['"'] = "&#034;".toCharArray();//$NON-NLS-1$
		specialCharactersXMLRepresentation['\''] = "&#039;".toCharArray();//$NON-NLS-1$
	}
	/**
	 * Copied and adapted from org.apache.taglibs.standard.tag.common.core.Util v1.1.2
	 *
	 * Performs the following substring replacements
	 * (to facilitate output to XML/HTML pages):
	 *
	 *	& -> &amp;
	 *	< -> &lt;
	 *	> -> &gt;
	 *	" -> &#034;
	 *	' -> &#039;
	 *
	 * See also OutSupport.writeEscapedXml().
	 */
	public static String escapeXml(String buffer) {
		if (buffer == null) {
			return null;
		}
		int start = 0;
		int length = buffer.length();
		char[] arrayBuffer = buffer.toCharArray();
		StringBuilder escapedBuffer = null;

		for (int i = 0; i < length; ++i) {
			char c = arrayBuffer[i];
			if (c <= HIGHEST_SPECIAL) {
				char[] escaped = specialCharactersXMLRepresentation[c];
				if (escaped != null) {
					// create StringBuilder to hold escaped xml string
					if (start == 0) {
						escapedBuffer = new StringBuilder(length + 5);
					}
					// add unescaped portion
					if (start < i) {
						escapedBuffer.append(arrayBuffer,start,i-start);
					}
					start = i + 1;
					// add escaped xml
					escapedBuffer.append(escaped);
				}
			}
		}
		// no xml escaping was necessary
		if (start == 0) {
			return buffer;
		}
		// add rest of unescaped portion
		if (start < length) {
			escapedBuffer.append(arrayBuffer,start,length-start);
		}
		return escapedBuffer.toString();
	}

	/**
	 * Turn special characters into escaped characters conforming to JavaScript.
	 * @param input the input string
	 * @return the escaped string
	 */
	public static String escapeJavaScript(CharSequence input) {
		if (input == null || input.length() == 0) {
			return "";
		}
		StringBuilder filtered = new StringBuilder(input.length());
		char prevChar = '\u0000';
		char c;
		for (int i = 0; i < input.length(); ++i) {
			c = input.charAt(i);
			switch (c) {
			case '\t':
				filtered.append("\\t");
				break;
			case '\b':
				filtered.append("\\b");
				break;
			case '\n':
				if (prevChar != '\r') {
					filtered.append("\\n");
				}
				break;
			case '\r':
				filtered.append("\\n");
				break;
			case '\f':
				filtered.append("\\f");
				break;
			case '\'':
				filtered.append("\\'");
				break;
			case '"':
				filtered.append("\\\"");
				break;
			case '\\':
				filtered.append("\\\\");
				break;
			case '/':
				filtered.append("\\/");
				break;
			default:
				filtered.append(c);
				break;
			}
			prevChar = c;
		}
		return filtered.toString();
	}

}
