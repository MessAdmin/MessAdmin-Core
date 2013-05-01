/**
 * 
 */
package clime.messadmin.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;
import clime.messadmin.providers.ProviderUtils;
import clime.messadmin.providers.spi.SerializableProvider;

/**
 * @author C&eacute;drik LIME
 */
public class SerializableUtilsTest extends TestCase {
	private static final SerializableProvider provider;
	private static final Collection serializables = new ArrayList();
	private static final Collection nonSerializables = new ArrayList();

	static {
		provider = (SerializableProvider) ProviderUtils.getProviders(SerializableProvider.class).get(0);
		System.out.println("Using provider " + provider.getClass());
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(SerializableUtilsTest.class);
	}

	/**
	 * Constructor for SerializableUtilsTest.
	 * @param name
	 */
	public SerializableUtilsTest(String name) {
		super(name);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void setUp() throws Exception {
		super.setUp();
		serializables.add(null);
		serializables.add(new ArrayList());
		serializables.add(new Long(2));
		serializables.add(new RuntimeException("test exception"));
		serializables.add("foobar");
		serializables.add(new int[] {1, 2, 3});
		serializables.add(new Long[] {new Long(1), new Long(2)});
		serializables.add(new Object[] {});
		serializables.add(serializables); // I mean it!

		nonSerializables.add(new Object());
		nonSerializables.add(new ThreadLocal());
		nonSerializables.add(new Object[] {new Object()});
		Collection coll = new ArrayList(1);
		coll.add(new Object());
		nonSerializables.add(coll);
		nonSerializables.add(nonSerializables); // I mean it!
	}

	/**
	 * {@inheritDoc}
	 */
	protected void tearDown() throws Exception {
		serializables.clear();
		nonSerializables.clear();
		super.tearDown();
	}

	/*
	 * Test method for 'clime.messadmin.utils.SerializableUtils.isSerializable(Object)'
	 */
	public void testIsSerializableObject() {
		for (Iterator iter = serializables.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			assertTrue(provider.isSerializable(obj));
		}
	}

	/*
	 * Test method for 'clime.messadmin.utils.SerializableUtils.isSerializable(Object)'
	 */
	public void testIsNotSerializableObject() {
		for (Iterator iter = nonSerializables.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			assertFalse(provider.isSerializable(obj));
		}
	}
}
