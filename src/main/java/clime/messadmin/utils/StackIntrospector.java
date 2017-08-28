/**
 * 
 */
package clime.messadmin.utils;


/**
 * A set of utilities to inspect current stack frame.
 * Heavily inspired by, and based on, Apache LogKit's org.apache.log.util.StackIntrospector
 * 
 * Note: this information is also available since Java 1.4 via
 * {@link Throwable#getStackTrace()}
 * 
 * @author C&eacute;drik LIME
 */
public final class StackIntrospector {
	/* Magic number CALL_CONTEXT_OFFSET identifies our caller */
	private static final int CALL_CONTEXT_OFFSET = 2; // may need to change if this class is redesigned

	/**
	 * Hack to get the call stack as an array of classes.
	 * The SecurityManager class provides this information as a protected method,
	 * so all we have to do is violate OO encapsulation principles and
	 * permit its access through a new public method!
	 */
	private static final class ClassContext extends SecurityManager {
		/**
		 * @throws SecurityException if an existing SecurityManager disallows construction of another SecurityManager
		 */
		ClassContext() {
			super();
		}
		/**
		 * Returns the current execution stack as an array of classes.<br>
		 * The length of the array is the number of methods on the execution
		 * stack. The element at index <code>0</code> is the class of the
		 * currently executing method, the element at index <code>1</code> is
		 * the class of that method's caller, and so on.
		 * 
		 * @return the execution stack.
		 * @see @link SecurityManager#getClassContext()
		 */
		public Class[] get() {
			return getClassContext();
		}
	}

	/**
	 * Create Hack SecurityManager to get ClassContext
	 * @throws SecurityException if an existing SecurityManager disallows construction of another SecurityManager
	 */
	private static final ClassContext CLASS_CONTEXT = new ClassContext();

	/**
	 * Private constructor to block instantiation.
	 */
	private StackIntrospector() {
		/* assert false; */
	}

	/**
	 * Find our caller's caller.
	 * May return null if caller not found on execution stack.
	 */
	public static Class getCallerClass() {
		Class[] stack = CLASS_CONTEXT.get();
		if (stack.length <= CALL_CONTEXT_OFFSET + 1) {
			return null;
		}
		Class c = stack[CALL_CONTEXT_OFFSET + 1];
		return c;
	}

	/**
	 * Find the caller of the passed in Class.
	 * May return null if caller not found on execution stack.
	 *
	 * @param clazz the Class to search for on stack to find caller of
	 * @return the Class of object that called parameter class
	 */
	public static Class getCallerClass(final Class clazz) throws SecurityException {
		// return getCallerClass(clazz, 0); // can't do that, as this adds a stack frame...
		final Class[] stack = CLASS_CONTEXT.get();

		if (stack.length <= CALL_CONTEXT_OFFSET) {
			return null;
		}
		// Traverse the call stack until we find clazz
		for (int i = CALL_CONTEXT_OFFSET; i < stack.length; ++i) {
			if (clazz.isAssignableFrom(stack[i])) {
				// Found: the caller is the previous stack element
				return i+1 >= stack.length ? null : stack[i + 1];
			}
		}

		//Unable to locate class in call stack
		return null;
	}

	/**
	 * Find the caller of the passed in Class.
	 * May return null if caller not found on execution stack.
	 *
	 * @param clazz the Class to search for on stack to find caller of
	 * @param stackDepthOffset Offset call-stack depth to find caller
	 * @return the Class of object that called parrameter class
	 */
	public static Class getCallerClass(final Class clazz, final int stackDepthOffset) {
		final Class[] stack = CLASS_CONTEXT.get();

		if (stack.length <= stackDepthOffset + CALL_CONTEXT_OFFSET) {
			return null;
		}
		// Traverse the call stack until we find clazz
		for (int i = stackDepthOffset + CALL_CONTEXT_OFFSET; i < stack.length; ++i) {
			if (clazz.isAssignableFrom(stack[i])) {
				// Found: the caller is the previous stack element
				return i+1 >= stack.length ? null : stack[i + 1];
			}
		}

		//Unable to locate class in call stack
		return null;
	}
}
