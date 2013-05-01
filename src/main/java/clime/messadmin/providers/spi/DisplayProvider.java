/**
 * 
 */
package clime.messadmin.providers.spi;


/**
 * This is a marker interface for all providers that will contribute displaying information.
 * @author C&eacute;drik LIME
 * @since 4.2
 */
public abstract interface DisplayProvider extends BaseProvider {
	public static class Util {
		public static String getId(DisplayProvider displayProvider) {
			if (displayProvider == null) {
				return "";
			}
			return displayProvider.getClass().getName();
		}
	}

//	/**
//	 * @return the HTML ID of this provider
//	 */
//	int getHTMLId();
}
