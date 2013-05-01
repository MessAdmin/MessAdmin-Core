/**
 *
 */
package clime.messadmin.providers.userdata.resourcebrowser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import clime.messadmin.utils.Charsets;

/**
 * JNDI resource
 * @author C&eacute;drik LIME
 */
/*
 * Implementation note: in order to avoid creating numerous InitialContext()'s,
 * this class has a request lifecycle...
 */
//
class JNDIResource extends BaseResource {
	private static final Set/*<Class>*/ ALLOWED_TYPES;

	static {
		Set allowedTypes = new HashSet();
		allowedTypes.addAll(Arrays.asList(new Class[] {
				java.lang.Character.class, java.lang.String.class,
				java.lang.Boolean.class, java.lang.Byte.class, java.lang.Short.class, java.lang.Integer.class, java.lang.Long.class,
				java.lang.Float.class, java.lang.Double.class
			}));
		ALLOWED_TYPES = Collections.unmodifiableSet(allowedTypes);
	}

	private final Context jndiContext;

	public JNDIResource(String path, Context jndiContext) {
		this(path, null, jndiContext);
	}

	protected JNDIResource(String path, JNDIResource parent, Context jndiContext) {
		super(path, parent);
		this.jndiContext = jndiContext;
		isFile = isFileInternal();
	}

	/** {@inheritDoc} */
	public Collection/*<JNDIResource>*/ getChildResources(ServletContext servletContext) {
		List result = new ArrayList();
		NamingEnumeration enumeration = null;
		try {
			enumeration = jndiContext.list(resourcePath);
			while (enumeration.hasMore()) {
				NameClassPair nameClassPair = (NameClassPair) enumeration.next();
				String name = nameClassPair.getName();
				if (nameClassPair.isRelative()) {
					//name = context.composeName(name, context.getNameInNamespace());
					name = resourcePath + name;
				}
				JNDIResource child = new JNDIResource(name, this, jndiContext);
				if (child.isDirectory()) {
					name += '/';
					child = new JNDIResource(name, this, jndiContext);
				}
				result.add(child);
			}
		} catch (NamingException ne) {
//			result = new ArrayList();
//			result.add(ne.toString());
//			return result;
			return Collections.EMPTY_LIST;
		} finally {
			closeQuietly(enumeration);
		}
		return result;
	}

	/** {@inheritDoc} */
	protected BaseResource getParentDirectoryInternal() {
		if (! isDirectory()) {
			throw new IllegalArgumentException("Not a directory: " + resourcePath);
		}
		// remove trailing "/"
		String directoryPath = resourcePath.substring(0, resourcePath.length()-1);
		int slashIndex = directoryPath.lastIndexOf("/");//$NON-NLS-1$
		if (slashIndex == -1) {
			if ("java:".equals(resourcePath)) {
				return null;
			} else {
				return new JNDIResource("java:", jndiContext);
			}
		} else {
			return new JNDIResource(directoryPath.substring(0, slashIndex+1), jndiContext);
		}
	}

	/** {@inheritDoc} */
	public String getFileName() {
		String result = resourcePath;
		int slashIndex = result.lastIndexOf("/");//$NON-NLS-1$
		if (slashIndex != -1) {
			result = result.substring(slashIndex+1);
		}
		return result;
	}

	/** {@inheritDoc} */
	public BaseResource getCanonicalResource() {
		return this;
	}

	private boolean isFile = false;
	/** {@inheritDoc} */
	public boolean isFile() {
		return isFile;
	}
	private boolean isFileInternal() {
		boolean isFile = false;
		NamingEnumeration enumeration = null;
		try {
			enumeration = jndiContext.list(resourcePath);
			isFile = ! enumeration.hasMore();
		} catch (NameNotFoundException nnfe) {
			isFile = true;
		} catch (NamingException ne) {
			isFile = true;
		} finally {
			closeQuietly(enumeration);
		}
		return isFile;
	}

	/** {@inheritDoc} */
	public boolean isDirectory() {
		return ! isFile();
	}

	/** {@inheritDoc} */
	public boolean isHidden() {
		return false;
	}

	private Boolean canRead = null;
	/** {@inheritDoc} */
	public boolean canRead() {
		if (canRead == null) {
			if (isDirectory()) {
				canRead = Boolean.TRUE; // a directory is browseable
			} else {
				// Try to fetch the class of resource to see if we can display it
				try {
					Object object = jndiContext.lookup(resourcePath);
					if (object != null && ALLOWED_TYPES.contains(object.getClass())) {
						canRead = Boolean.TRUE;
					} else {
						canRead = Boolean.FALSE;
					}
				} catch (NamingException ignore) {
					canRead = Boolean.FALSE;
				}
			}
		}
		return canRead.booleanValue();
	}
	/** {@inheritDoc} */
	public boolean canWrite() {
		return false;
	}
	/** {@inheritDoc} */
	public boolean canDelete() {
		return false;
	}
	/** {@inheritDoc} */
	public boolean canRename() {
		return false;
	}
	/** {@inheritDoc} */
	public boolean canCompress() {
		return false;
	}

	/** {@inheritDoc} */
	public InputStream getResourceAsStream(ServletContext servletContext) {
		if (! canRead()) {
			return null;
		}
		try {
			Object object = jndiContext.lookup(resourcePath);
//			ByteArrayOutputStream os = new ByteArrayOutputStream();
//			ObjectOutputStream out = new ObjectOutputStream(os);
//			out.writeObject(object);
//			out.flush();
//			out.close();
//			return new ByteArrayInputStream(os.toByteArray());
			return new ByteArrayInputStream(Charsets.getStringBytesUTF8(String.valueOf(object)));// Java 7: String.valueOf(object).getBytes("UTF-8")
		} catch (NamingException ignore) {
			return null;
		} catch (IOException ignore) {
			return null;
		}
	}

	/** {@inheritDoc} */
	protected URL getURL(ServletContext context) {
		return null;
	}

	/** {@inheritDoc} */
	public long getContentLength(ServletContext context) {
		URLConnection connection = getURLConnection(context);
		if (connection != null) {
			return connection.getContentLength();
		}
		return -1;
	}

	/** {@inheritDoc} */
	public long getLastModified(ServletContext context) {
		URLConnection connection = getURLConnection(context);
		if (connection != null) {
			return connection.getLastModified();
		}
		return 0;
	}

	/** {@inheritDoc} */
	public String getContentType(ServletContext context) {
		return "text/plain";//$NON-NLS-1$
	}


	protected void closeQuietly(NamingEnumeration enumeration) {
		if (enumeration != null) {
			try {
				enumeration.close();
			} catch (NamingException ignore) {
			}
		}
	}
}
