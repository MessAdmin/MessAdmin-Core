/**
 * 
 */
package clime.messadmin.utils;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;
import clime.messadmin.providers.ProviderUtils;
import clime.messadmin.providers.spi.SerializableProvider;

/**
 * @author C&eacute;drik LIME
 */
public class SerializableUtilsTest extends TestCase {
	private static final SerializableProvider provider;
	private static final Collection<Object> serializables = new ArrayList<Object>();
	private static final Collection<Object> nonSerializables = new ArrayList<Object>();

	static {
		provider = ProviderUtils.getProviders(SerializableProvider.class).get(0);
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
	@Override
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
		Collection<Object> coll = new ArrayList<Object>(1);
		coll.add(new Object());
		nonSerializables.add(coll);
		nonSerializables.add(nonSerializables); // I mean it!
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void tearDown() throws Exception {
		serializables.clear();
		nonSerializables.clear();
		super.tearDown();
	}

	/*
	 * Test method for 'clime.messadmin.utils.SerializableUtils.isSerializable(Object)'
	 */
	public void testIsSerializableObject() {
		for (Object obj : serializables) {
			assertTrue(provider.isSerializable(obj));
		}
	}

	/*
	 * Test method for 'clime.messadmin.utils.SerializableUtils.isSerializable(Object)'
	 */
	public void testIsNotSerializableObject() {
		for (Object obj : nonSerializables) {
			assertFalse(provider.isSerializable(obj));
		}
	}
}
