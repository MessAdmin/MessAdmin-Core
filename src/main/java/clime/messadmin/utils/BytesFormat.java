/**
 *
 */
package clime.messadmin.utils;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import clime.messadmin.i18n.I18NSupport;

/**
 * Formats a number of bytes, adding a suffix
 * @author C&eacute;drik LIME
 */
public class BytesFormat extends NumberFormat {
	private static final String BUNDLE_NAME = BytesFormat.class.getName();
	protected static final int DEFAULT_FRACTIONS_DIGITS = 2;

	protected static final double KB = 1024;
	protected static final double MB = KB * 1024;
	protected static final double GB = MB * 1024;
	protected static final double TB = GB * 1024;

	protected String B_suffix;
	protected String KB_suffix;
	protected String MB_suffix;
	protected String GB_suffix;
	protected String TB_suffix;

	protected Locale locale;

	/**
	 * @param html should we convert a ' ' separator before the size unit to an HTML non-breakable space ({@code &nbsp;})?
	 */
	public BytesFormat(Locale locale, boolean html) {
		super();
		this.locale = locale;
		String separator = " ";//$NON-NLS-1$
		if (html) {
			separator = "&nbsp;";//$NON-NLS-1$
		}
		B_suffix = separator + I18NSupport.getLocalizedMessage(BUNDLE_NAME, locale, "B");//$NON-NLS-1$
		KB_suffix = separator + I18NSupport.getLocalizedMessage(BUNDLE_NAME, locale, "KB");//$NON-NLS-1$
		MB_suffix = separator + I18NSupport.getLocalizedMessage(BUNDLE_NAME, locale, "MB");//$NON-NLS-1$
		GB_suffix = separator + I18NSupport.getLocalizedMessage(BUNDLE_NAME, locale, "GB");//$NON-NLS-1$
		TB_suffix = separator + I18NSupport.getLocalizedMessage(BUNDLE_NAME, locale, "TB");//$NON-NLS-1$
	}

	/**
	 * @param html should we convert a ' ' separator before the size unit to an HTML non-breakable space ({@code &nbsp;})?
	 */
	public static BytesFormat getBytesInstance(boolean html) {
		return getBytesInstance(Locale.getDefault(), html);
	}
	/**
	 * @param html should we convert a ' ' separator before the size unit to an HTML non-breakable space ({@code &nbsp;})?
	 */
	public static BytesFormat getBytesInstance(Locale locale, boolean html) {
		return new BytesFormat(locale, html); // super-class is not thread-safe
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
		if (number > Long.MAX_VALUE) {
			throw new IllegalArgumentException("Can not format numbers greater than " + Long.MAX_VALUE);//$NON-NLS-1$
		}
		return format((long)number, toAppendTo, pos);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
		bytesToHumanString(number, DEFAULT_FRACTIONS_DIGITS, toAppendTo);
		return toAppendTo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Number parse(String text, ParsePosition parsePosition) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Smart formatter for bytes
	 * @param bytes
	 * @param toAppendTo  buffer where to put the formated, human-readable String
	 */
	protected void bytesToHumanString(long bytes, int fractionsDigits, StringBuffer toAppendTo) {
		if (bytes < 0) {
			toAppendTo.append(bytes);
			return;
		}
		double result = bytes;
		String suffix = "";//$NON-NLS-1$
		// Could use a ChoiceFormat() + MessageFormat() instead,
		// but this is faster
		if (bytes < KB) {
			result = bytes;
			suffix = B_suffix;
		} else if (bytes < MB) {
			result = round(bytes / KB, fractionsDigits);
			suffix = KB_suffix;
		} else if (bytes < GB) {
			result = round(bytes / MB, fractionsDigits);
			suffix = MB_suffix;
		} else if (bytes < TB) {
			result = round(bytes / GB, fractionsDigits);
			suffix = GB_suffix;
		} else {
			result = round(bytes / TB, fractionsDigits);
			suffix = TB_suffix;
		}
		NumberFormat nf = NumberFormat.getNumberInstance(locale);
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(fractionsDigits);
		nf.setGroupingUsed(super.isGroupingUsed());
		toAppendTo.append(nf.format(result)).append(suffix);
	}

	private double round(double value, int fractionsDigits) {
		double powValue = Math.pow(10, fractionsDigits);
		return Math.round(value * powValue) / powValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object clone() {
		BytesFormat other = (BytesFormat) super.clone();
		return other;
	}
}
