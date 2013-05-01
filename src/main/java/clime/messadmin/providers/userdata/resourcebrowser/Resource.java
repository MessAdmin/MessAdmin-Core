/**
 *
 */
package clime.messadmin.providers.userdata.resourcebrowser;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletContext;

import clime.messadmin.utils.StringUtils;

/**
 * Browser the war content
 * @author C&eacute;drik LIME
 */
class Resource extends BaseResource {

	public Resource(String path) {
		super(path);
	}

	protected Resource(String path, Resource parent) {
		super(path, parent);
	}

	/** {@inheritDoc} */
	public Collection/*<Resource>*/ getChildResources(ServletContext context) {
		Set/*<String>*/ children = context.getResourcePaths(resourcePath);
		Collection/*<Resource>*/ result = new ArrayList();
		for (Iterator iter = children.iterator(); iter.hasNext();) {
			String child = (String) iter.next();
			result.add(new Resource(child, this));
		}
		return result;
	}

	/** {@inheritDoc} */
	public BaseResource getCanonicalResource() {
		return this;//FIXME
	}

	/** {@inheritDoc} */
	protected BaseResource getParentDirectoryInternal() {
		String directoryPath = resourcePath;
		if (isDirectory()) {
			// remove trailing "/"
			directoryPath = resourcePath.substring(0, resourcePath.length()-1);
		}
		if (StringUtils.isEmpty(directoryPath)) {
			return null;
		} else {
			return new Resource(directoryPath.substring(0, directoryPath.lastIndexOf("/")+1));
		}
	}

	/** {@inheritDoc} */
	public String getFileName() {
		String resource = resourcePath;
		int slashIndex = resource.lastIndexOf("/");//$NON-NLS-1$
		if (slashIndex != -1) {
			resource = resource.substring(slashIndex+1);
		}
		return resource;
	}

	/** {@inheritDoc} */
	public boolean isFile() {
		return ! isDirectory();
	}
	/** {@inheritDoc} */
	public boolean isDirectory() {
		return resourcePath.endsWith("/");//$NON-NLS-1$
	}

	/** {@inheritDoc} */
	public boolean isHidden() {
		return false;
	}

	/** {@inheritDoc} */
	public boolean canRead() {
		return true;
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
		return servletContext.getResourceAsStream(resourcePath);
	}

	protected URL getURL(ServletContext context) {
		try {
			return context.getResource(resourcePath);
		} catch (MalformedURLException mue) {
			return null;
		}
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
}
