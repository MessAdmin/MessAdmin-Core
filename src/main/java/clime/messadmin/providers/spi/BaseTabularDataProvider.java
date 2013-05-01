/**
 *
 */
package clime.messadmin.providers.spi;

import java.util.Iterator;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.model.Server;
import clime.messadmin.utils.StringUtils;


/**
 * Base implementation class for DataProvider displaying tabular data.
 * @author C&eacute;drik LIME
 */
//TODO rename to TableBuilder
public abstract class BaseTabularDataProvider {

	/**
	 *
	 */
	public BaseTabularDataProvider() {
		super();
	}

	/**
	 * Convenience method for i18n
	 * @since 4.1
	 */
	protected ClassLoader getClassLoader(final HttpSession session) {
		return getClassLoader(session.getServletContext());
	}
	/**
	 * Convenience method for i18n
	 * @since 4.1
	 */
	protected ClassLoader getClassLoader(final ServletContext context) {
		return Server.getInstance().getApplication(context).getApplicationInfo().getClassLoader();
	}

	/**
	 * Convenience method
	 * @since 5.2
	 */
	protected String getInternalContext(final HttpSession session) {
		return getInternalContext(session.getServletContext());
	}

	/**
	 * Convenience method
	 * @since 5.2
	 */
	protected String getInternalContext(final ServletContext context) {
		return Server.getInstance().getApplication(context).getApplicationInfo().getInternalContextPath();
	}

	/**
	 * Convenience method for i18n
	 * @since 4.1
	 * @deprecated
	 */
	protected ResourceBundle getResourceBundle(String baseName, ClassLoader cl) {
		return I18NSupport.getResourceBundle(baseName, cl);
	}

	/**
	 * Convenience method for i18n
	 * @since 4.1
	 * @deprecated
	 */
	protected String getLocalizedMessage(String baseName, ClassLoader cl, String key) {
		return I18NSupport.getLocalizedMessage(baseName, cl, key);
	}
	/**
	 * Convenience method for i18n
	 * @deprecated
	 * @since 4.1
	 */
	protected String getLocalizedMessage(String baseName, ClassLoader cl, String key, Object[] args) {
		return I18NSupport.getLocalizedMessage(baseName, cl, key, args);
	}

	//	/**
//	 * @param object
//	 * @return specific data labels for given object, or null if it can be determined
//	 */
//	public abstract String[] getTabularDataLabels(final Object object);
//	/**
//	 * @param object
//	 * @return specific data values for given object, or null if it can be determined
//	 */
//	public abstract Object[][] getTabularData(final Object object);
//
//	protected abstract String getTableCaption(String[] labels, Object[][] data);
//
//	protected String getXHTMLData(Object object) {
//		try {
//			String[] labels = getTabularDataLabels(object);
//			Object[][] values = getTabularData(object);
//			return buildXHTML(labels, values, tableId, getTableCaption(labels, values));
//		} catch (RuntimeException rte) {
//			return "Error in " + this.getClass().getName() + ": " + rte;
//		}
//	}

	/**
	 * @deprecated
	 */
	protected String buildXHTML(String[] labels, Object[][] values, String tableId, String tableCaption) {
		if (labels == null || labels.length == 0 || values == null) {// || values.length == 0
			return null;
		}
		StringBuffer buffer = new StringBuffer(128 + 16*labels.length + 16*values.length*labels.length);
		return buildXHTML(buffer, labels, values, tableId, tableCaption).toString();
	}
	protected String buildXHTML(String[] labels, RowIterator rows, String tableId, String tableCaption) {
		if (labels == null || labels.length == 0 || rows == null) {// || !rows.hasNext()
			return null;
		}
		StringBuffer buffer = new StringBuffer(256*labels.length);
		return buildXHTML(buffer, labels, rows, tableId, tableCaption).toString();
	}
	/**
	 * @deprecated
	 */
	protected StringBuffer buildXHTML(StringBuffer buffer, String[] labels, Object[][] values, String tableId, String tableCaption) {
		if (labels == null || labels.length == 0 || values == null) {// || values.length == 0
			return buffer;
		}
		buffer.append("<table id=\"").append(tableId).append("\" class=\"strippable\" style=\"text-align: left;\" border=\"1\" cellpadding=\"2\" cellspacing=\"2\">\n");
		appendCaption(buffer, tableCaption);
		appendHeader(buffer, labels);
		appendFooter(buffer, labels);
		appendBody(buffer, values);
		buffer.append("</table>\n");
		return buffer;
	}
	protected StringBuffer buildXHTML(StringBuffer buffer, String[] labels, RowIterator rows, String tableId, String tableCaption) {
		if (labels == null || labels.length == 0 || rows == null) {// || !rows.hasNext()
			return buffer;
		}
		buffer.append("<table id=\"").append(tableId).append("\" class=\"strippable\" style=\"text-align: left;\" border=\"1\" cellpadding=\"2\" cellspacing=\"2\">\n");
		appendCaption(buffer, tableCaption);
		appendHeader(buffer, labels);
		appendFooter(buffer, labels);
		appendBody(buffer, rows);
		buffer.append("</table>\n");
		return buffer;
	}

	protected void appendCaption(StringBuffer buffer, String tableCaption) {
		if (StringUtils.isNotBlank(tableCaption)) {
			buffer.append("<caption style=\"font-variant: small-caps;\">").append(tableCaption).append("</caption>\n");
		}
	}

	protected void appendHeader(StringBuffer buffer, String[] labels) {
		buffer.append("<thead><tr>\n");
		for (int i = 0; i < labels.length; ++i) {
			String label = labels[i];
			buffer.append("\t<th>");
			appendHeaderLabel(buffer, i, label);
			buffer.append("</th>\n");
		}
		buffer.append("</tr></thead>\n");
	}

	protected void appendHeaderLabel(StringBuffer buffer, int cellNumber, String label) {
		if (label != null) {
			buffer.append(label);
		}
	}

	protected void appendBody(StringBuffer buffer, RowIterator rows) {
		buffer.append("<tbody>\n");
		while (rows.hasNext()) {
			appendRow(buffer, rows);
		}
		buffer.append("</tbody>\n");
	}

	/**
	 * @deprecated
	 */
	protected void appendBody(StringBuffer buffer, Object[][] values) {
		buffer.append("<tbody>\n");
		for (int i = 0; i < values.length; ++i) {
			Object[] row = values[i];
			appendRow(buffer, row);
		}
		buffer.append("</tbody>\n");
	}

	protected void appendRow(StringBuffer buffer, RowIterator rows) {
		Object[] row = (Object[]) rows.next();
		String rowClass = rows.getRowClass();
		String rowStyle = rows.getRowStyle();
		buffer.append("<tr");
		if (StringUtils.isNotBlank(rowClass)) {
			buffer.append(" class=\"").append(rowClass).append('"');
		}
		if (StringUtils.isNotBlank(rowStyle)) {
			buffer.append(" style=\"").append(rowStyle).append('"');
		}
		buffer.append(">\n");
		for (int j = 0; j < row.length; ++j) {
			Object value = row[j];
			String cellClass = rows.getCellClass(j, value);
			String cellStyle = rows.getCellStyle(j, value);
			buffer.append("\t<td");
			if (StringUtils.isNotBlank(cellClass)) {
				buffer.append(" class=\"").append(cellClass).append('"');
			}
			if (StringUtils.isNotBlank(cellStyle)) {
				buffer.append(" style=\"").append(cellStyle).append('"');
			}
			buffer.append('>');
			appendValue(buffer, j, value);
			buffer.append("</td>\n");
		}
		buffer.append("</tr>\n");
	}

	/**
	 * @deprecated
	 */
	protected void appendRow(StringBuffer buffer, Object[] row) {
		buffer.append("<tr>\n");
		for (int j = 0; j < row.length; ++j) {
			Object value = row[j];
			String cellClass = getCellClass(j, value);
			buffer.append("<td");
			if (StringUtils.isNotBlank(cellClass)) {
				buffer.append(" class=\"").append(cellClass).append('"');
			}
			buffer.append('>');
			appendValue(buffer, j, value);
			buffer.append("</td>");
		}
		buffer.append("</tr>\n");
	}

	/**
	 * @return CSS class for &lt;td&gt; HTML element
	 * @deprecated
	 */
	protected String getCellClass(int cellNumber, Object value) {
		return null;
	}

	protected void appendValue(StringBuffer buffer, int cellNumber, Object value) {
		if (value != null) {
//			buffer.append("<span title=\"").append(value.getClass()).append("\">");
			buffer.append(value);
//			buffer.append("</span>");
		}
	}

	protected void appendFooter(StringBuffer buffer, String[] labels) {
//		buffer.append("<tfoot><tr>\n");
//		for (int i = 0; i < labels.length; ++i) {
//			String label = labels[i];
//			buffer.append("\t<th>");
//			appendFooterLabel(buffer, i, label);
//			buffer.append("</th>\n");
//		}
//		buffer.append("</tr></tfoot>\n");
	}

	protected void appendFooterLabel(StringBuffer buffer, int cellNumber, String label) {
		appendHeaderLabel(buffer, cellNumber, label);
	}


	public static interface RowIterator extends Iterator/*Object[]*/ {
		/**
		 * @return number of rows, or {@code -1} if not available
		 */
		int getNRows();
		/**
		 * @return CSS class for &lt;tr&gt; HTML element
		 */
		String getRowClass();
		/**
		 * @return CSS style for &lt;tr&gt; HTML element
		 */
		String getRowStyle();
		/**
		 * @return CSS class for &lt;td&gt; HTML element
		 */
		String getCellClass(int cellNumber, Object value);
		/**
		 * @return CSS style for &lt;td&gt; HTML element
		 */
		String getCellStyle(int cellNumber, Object value);
	}

	public static abstract class BaseRowIterator implements RowIterator {
		/** {@inheritDoc} */
		public void remove() {
			throw new UnsupportedOperationException();
		}
		/** {@inheritDoc} */
		public int getNRows() {
			return -1;
		}
		/** {@inheritDoc} */
		public String getRowClass() {
			return null;
		}
		/** {@inheritDoc} */
		public String getRowStyle() {
			return null;
		}
		/** {@inheritDoc} */
		public String getCellClass(int cellNumber, Object value) {
			return null;
		}
		/** {@inheritDoc} */
		public String getCellStyle(int cellNumber, Object value) {
			return null;
		}
	}
}
