/**
 *
 */
package clime.messadmin.providers.userdata.resourcebrowser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Comparator;

import javax.servlet.ServletContext;

/**
 * Base class for representing files-like resources.
 *
 * TODO use FileSystemView#getSystemIcon() to display a nice resource icon
 * TODO use FileSystemView#getSystemDisplayName() to display a user-friendly name
 *
 * @author C&eacute;drik LIME
 */
public abstract class BaseResource {

	private static transient Object fileTypeMap_defaultInstance = null;
	private static transient Method fileTypeMap_getContentType = null;

	static {
		// Requires javax.activation or Java 6
		try {
			Class fileTypeMapClass = Class.forName("javax.activation.FileTypeMap");//$NON-NLS-1$
			Method getDefaultFileTypeMap = fileTypeMapClass.getMethod("getDefaultFileTypeMap");//$NON-NLS-1$
			fileTypeMap_defaultInstance = getDefaultFileTypeMap.invoke(null);
			fileTypeMap_getContentType = fileTypeMapClass.getMethod("getContentType", String.class);//$NON-NLS-1$
		} catch (RuntimeException rte) {
		} catch (Exception e) {
		}
	}

	public static final Comparator/*<BaseResource>*/ CASE_INSENSITIVE_ORDER = new Comparator() {
		/** {@inheritDoc} */
		public int compare(Object o1, Object o2) {
			BaseResource r1 = (BaseResource) o1;
			BaseResource r2 = (BaseResource) o2;
			return String.CASE_INSENSITIVE_ORDER.compare(r1.getPath(), r2.getPath());
		}
	};
	protected final String resourcePath;
	protected BaseResource parent;

	protected BaseResource(String path) {
		this(path, null);
	}

	protected BaseResource(String path, BaseResource parent) {
		super();
		this.resourcePath = path;
		this.parent = parent;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof BaseResource)) {
			return false;
		}
		BaseResource other = (BaseResource) obj;
		return resourcePath == other.resourcePath || (resourcePath != null && resourcePath.equals(other.resourcePath));
	}
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return resourcePath == null ? 0 : resourcePath.hashCode();
	}

	/**
	 * Returns a directory-like listing of all the paths to resources whose longest sub-path
	 * matches the supplied path argument. Paths indicating subdirectory paths end with a '/'.
	 * The returned paths have a leading '/'. For example, for a web application
	 * containing<br><br>
	 *
	 * /welcome.html<br>
	 * /catalog/index.html<br>
	 * /catalog/products.html<br>
	 * /catalog/offers/books.html<br>
	 * /catalog/offers/music.html<br>
	 * /customer/login.jsp<br>
	 * /WEB-INF/web.xml<br>
	 * /WEB-INF/classes/com.acme.OrderServlet.class,<br><br>
	 *
	 * getResourcePaths("/") returns {"/welcome.html", "/catalog/", "/customer/", "/WEB-INF/"}<br>
	 * getResourcePaths("/catalog/") returns {"/catalog/index.html", "/catalog/products.html", "/catalog/offers/"}.<br>
	 *
	 * @param context (can be {@code null})
	 * @param resource should be a directory
	 * @return list of resources within {@code resource}
	 *
	 * @see File#listFiles()
	 * @see ServletContext#getResourcePaths(String)
	 */
	public abstract Collection/*<BaseResource>*/ getChildResources(
			ServletContext context);


	/**
	 * @see File#getParent()
	 */
	public BaseResource getParentDirectory() {
		if (parent == null) {
			parent = getParentDirectoryInternal();
		}
		return parent;
	}
	protected abstract BaseResource getParentDirectoryInternal();

	/**
	 * @see File#getName()
	 */
	public abstract String getFileName();

	/**
	 * @see File#getPath()
	 */
	public String getPath() {
		return resourcePath;
	}

	/**
	 * Helps to manage user input like "."
	 * @return canonical (i.e. both absolute and unique) resource name
	 * @see File#getCanonicalPath()
	 */
	public abstract BaseResource getCanonicalResource();

	/**
	 * @see File#isFile()
	 */
	public abstract boolean isFile();

	/**
	 * @see File#isDirectory()
	 */
	public abstract boolean isDirectory();

	/**
	 * @see File#isHidden()
	 */
	public abstract boolean isHidden();

	/**
	 * @see File#canRead()
	 */
	public abstract boolean canRead();

	/**
	 * @see File#canWrite()
	 */
	public abstract boolean canWrite();

	public abstract boolean canDelete();

	public abstract boolean canRename();

	public abstract boolean canCompress();

	/**
	 * Returns the resource located at the named path as
	 * an <code>InputStream</code> object.
	 *
	 * <p>The data in the <code>InputStream</code> can be
	 * of any type or length.
	 * This method returns <code>null</code> if no resource exists at
	 * the specified path.
	 *
	 * <p>This method is different from
	 * <code>java.lang.Class.getResourceAsStream</code>,
	 * which uses a class loader. This method allows
	 * to make a resource available
	 * from any location, without using a class loader.
	 *
	 * @param	servletContext (can be {@code null})
	 *
	 * @return	the <code>InputStream</code> returned,
	 *			or <code>null</code> if no resource
	 *			exists at the specified path
	 *
	 * @see ServletContext#getResourceAsStream(String)
	 */
	public abstract InputStream getResourceAsStream(
			ServletContext servletContext);

	/**
	 * @param context (can be {@code null})
	 * @return {@code fileName} as a {@link URL}
	 */
	protected abstract URL getURL(ServletContext context);

	/**
	 * @param context (can be {@code null})
	 * @see File#length()
	 */
	public abstract long getContentLength(ServletContext context);

	/**
	 * @param context (can be {@code null})
	 * @see File#lastModified()
	 */
	public abstract long getLastModified(ServletContext context);

	/**
	 * @see File#delete()
	 */
	public boolean delete() throws IOException {
		return false;
	}

	public boolean compress() throws IOException {
		return false;
	}

	/**
	 * @see File#renameTo(File)
	 */
	public boolean renameTo(String newName) throws IOException {
		return false;
	}


	/**
	 * @param context (can be {@code null})
	 * @param fileName
	 * @return
	 */
	protected URLConnection getURLConnection(ServletContext context) {
		try {
			URL url = getURL(context);
			URLConnection connection = url.openConnection();
			connection.setAllowUserInteraction(false);
			connection.setDoInput(false);
			connection.setDoOutput(false);
			connection.connect();//FIXME this can leak file descriptors (no way to close the connection...)!
			return connection;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @param context (can be {@code null})
	 * @return
	 */
	protected String getContentEncoding(ServletContext context) {
		URLConnection connection = getURLConnection(context);
		if (connection != null) {
			return connection.getContentEncoding();
//			if (connection instanceof HttpURLConnection) {
//				((HttpURLConnection)connection).disconnect();
//			}
		}
		return null;
	}

	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";//$NON-NLS-1$
	/**
	 * Utility method that guesses a file's mime type.
	 * Uses, in this order:
	 * <ul>
	 * <li>{@link ServletContext#getMimeType(String)} -- based on web.xml &lt;mime-mapping&gt; section</li>
	 * <li>{@link URLConnection#guessContentTypeFromName(String)} -- uses ${java.home}/lib/content-types.properties</li>
	 * <li>{@link javax.activation.FileTypeMap#getContentType(String)} -- uses "mime.types" (needs javax.activation or Java >= 6), see {@link javax.activation.MimetypesFileTypeMap}</li>
	 * <li>{@link URLConnection#guessContentTypeFromStream(InputStream)} -- hard-coded in the JDK</li>
	 * <li>{@link URLConnection#getContentType()}</li>
	 * </ul>
	 * See also http://sourceforge.net/projects/mime-util/ for reading UNIX "magic.mime"
	 *
	 * @param context (can be {@code null})
	 * @param checkContent should we try to read the content of the file to compute its mime type?
	 * @return file's mime type, or "application/octet-stream" if none found
	 */
	public String getContentType(ServletContext context, boolean checkContent) {
		String contentType = null;
		if (context != null) {
			contentType = context.getMimeType(resourcePath);
		}
		if (contentType == null) {
			contentType = URLConnection.getFileNameMap().getContentTypeFor(resourcePath);//==URLConnection.guessContentTypeFromName(resourcePath);
		}
		// javax.activation.FileTypeMap#getContentType(String) returns "application/octet-stream" if none is defined
		if (contentType == null && fileTypeMap_defaultInstance != null && fileTypeMap_getContentType != null) {
			//contentType = javax.activation.FileTypeMap.getDefaultFileTypeMap().getContentType(fileName);
			try {
				contentType = (String) fileTypeMap_getContentType.invoke(fileTypeMap_defaultInstance, new Object[] {resourcePath});
			} catch (Exception ignore) {
			}
		}
		if (checkContent) {
			if (contentType == null || DEFAULT_MIME_TYPE.equals(contentType)) {
				InputStream is = getResourceAsStream(context);
				if (is != null) {
					try {
						contentType = URLConnection.guessContentTypeFromStream(is);
					} catch (IOException ioe) {
					} finally {
						try {
							is.close();
						} catch (IOException ignore) {
						}
					}
				}
			}
			if (contentType == null || DEFAULT_MIME_TYPE.equals(contentType)) {
				URLConnection connection = getURLConnection(context);
				if (connection != null) {
					contentType = connection.getContentType();
//					if (connection instanceof HttpURLConnection) {
//						((HttpURLConnection)connection).disconnect();
//					}
				}
			}
		}
		if (contentType == null) {
			contentType = DEFAULT_MIME_TYPE;
		}
		return contentType;
	}

	public String getContentType(ServletContext context) {
		return getContentType(context, true);
	}
}
