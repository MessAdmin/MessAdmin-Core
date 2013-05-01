/**
 * 
 */
package clime.messadmin.providers;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * @author C&eacute;drik LIME
 */
public abstract class BaseProviderTest extends TestCase {
	protected List providers;
	protected Iterator providersIterator;

	/**
	 * Constructor for SizeOfTest.
	 * @param name
	 */
	public BaseProviderTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(BaseProviderTest.class);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void setUp() throws Exception {
		super.setUp();
		providers = ProviderUtils.getProviders(getProviderClass());
		providersIterator = providers.iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void tearDown() throws Exception {
		providersIterator = null;
		providers = null;
		super.tearDown();
	}

	protected abstract Class getProviderClass();

}
