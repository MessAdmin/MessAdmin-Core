//package sun.misc;
package clime.messadmin.providers;

/**
 * From JDK 1.4.2 / 1.5.0
 * see java.util.ServiceConfigurationError (Java 6+)
 *
 * Error thrown when something goes wrong while looking up a service provider.
 * <p> This error will be thrown in the following situations:
 * <ul>
 * <li> The format of a provider-configuration file violates the <a href="Service.html#format">specification</a>;</li>
 * <li> An <code>IOException</code> occurs while reading a provider-configuration file;</li>
 * <li> A concrete provider class named in a provider-configuration file cannot be found;</li>
 * <li> A concrete provider class is not a subclass of the service class;</li>
 * <li> A concrete provider class cannot be instantiated; or</li>
 * <li> Some other kind of error occurs.</li>
 * </ul>
 * @author C&eacute;drik LIME
 */
public class ServiceConfigurationError extends Error {

	/**
	 * Constructs a new instance with the specified message.
	 * @param message The message, or <tt>null</tt> if there is no message
	 */
	public ServiceConfigurationError(String message) {
		super(message);
	}

	/**
	 * Constructs a new instance with the specified cause.
	 * @param cause The cause, or <tt>null</tt> if the cause is nonexistent or unknown
	 * @since 1.4
	 */
	public ServiceConfigurationError(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new instance with the specified message and cause.
	 * @param msg The message, or <tt>null</tt> if there is no message
	 * @param cause The cause, or <tt>null</tt> if the cause is nonexistent or unknown
	 * @since 1.6
	 */
	public ServiceConfigurationError(String msg, Throwable cause) {
		super(msg, cause);
	}
}
