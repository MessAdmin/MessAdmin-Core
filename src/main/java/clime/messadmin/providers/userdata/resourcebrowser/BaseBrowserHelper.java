/**
 *
 */
package clime.messadmin.providers.userdata.resourcebrowser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.filechooser.FileSystemView;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionProvider;
import clime.messadmin.admin.BaseAdminActionWithContext;
import clime.messadmin.filter.MessAdminResponseWrapper;
import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.model.ApplicationInfo;
import clime.messadmin.model.Server;
import clime.messadmin.providers.spi.DisplayProvider;
import clime.messadmin.utils.BytesFormat;
import clime.messadmin.utils.DateUtils;
import clime.messadmin.utils.StringUtils;
import clime.messadmin.utils.compress.gzip.GZipConfiguration;
import clime.messadmin.utils.compress.zip.ZipConfiguration;
import clime.messadmin.utils.compress.zip.ZipOutputStreamAdapter;
import clime.messadmin.utils.compress.zip.ZipUtils;

/**
 * Helper class for browsing files-like resources.
 *
 * TODO sort by xyz; see org.apache.commons.io.comparator
 * TODO allow to expand GZip archives (but probably not ZIP: tarbomb, absolute or ".." paths, file name encodings...)
 * @author C&eacute;drik LIME
 */
public abstract class BaseBrowserHelper {
	public static final String FILE_ACTION_PARAMETER_NAME = "fileAction";//$NON-NLS-1$
	public static final String FILE_DELETE_ACTION         = "delete";//$NON-NLS-1$
	public static final String FILE_RENAME_ACTION         = "rename";//$NON-NLS-1$
	public static final String FILE_RENAME_ACTION_PARAMETER_NAME = "fileNewName";//$NON-NLS-1$
	public static final String FILE_COMPRESS_ACTION       = "compress";//$NON-NLS-1$
	public static final String FILE_DOWNLOAD_ACTION       = "download";//$NON-NLS-1$

	private static final String DEFAULT_RESOURCE_ID = "resource";//$NON-NLS-1$

	// 1240 bytes = IPv6 minimum MTU 1280 - IPv6 header size 40 bytes
	// 1420 bytes = max payload of a VPN IPv4 packet: PPTP/L2TP MTU 1460 bytes - IPv4 header size 40 bytes (IP: 20 bytes, TCP: 20 bytes, UDP: 8 bytes)
	private static final int COMPRESSION_MIN_SIZE = 1240;

	protected final AdminActionProvider adminActionProviderCallback;
	protected final DisplayProvider displayProviderCallback;


	public BaseBrowserHelper(AdminActionProvider adminActionProviderCallback, DisplayProvider displayProviderCallback) {
		super();
		this.adminActionProviderCallback = adminActionProviderCallback;
		this.displayProviderCallback = displayProviderCallback;
	}

	/**
	 * @return i18n bundle file name with the following keys: error.404
	 */
	protected abstract String getI18nBundleName();
	protected String getI18nInternalBundleName() {
		return BaseBrowserHelper.class.getName();
	}

	/**
	 * @return root path
	 */
	protected abstract BaseResource getDefaultRootResource();
	/**
	 * @return root user path for browsing when none required in the HttpRequest
	 * @see FileSystemView#getDefaultDirectory()
	 * @see FileSystemView#getHomeDirectory()
	 */
	protected BaseResource getDefaultUserResource() {
		return getDefaultRootResource();
	}

	/**
	 * Creates a new Resource object
	 *
	 * @param context (can be {@code null})
	 */
	protected abstract BaseResource getResource(
			ServletContext context, String resourcePath);

	/**
	 * Sorts the given resources according to the given comparator.
	 * Additionally, the resulting list will feature first directories, then
	 * files, and finally all other objects (if any).
	 *
	 * @param comparator the comparator that will be used to sort the (String) resources.
	 *        A {@code null} value indicates that the keys' <i>natural
	 *        ordering</i> should be used.
	 */
	//TODO put hidden elements on the top?
	protected List/*<BaseResource>*/ sortResourcePaths(Collection/*<BaseResource>*/ resources, Comparator comparator, boolean mixFilesAndDirectories) {
		// Can not do:
		// Collections.sort(new ArrayList(resources), comparator);
		// as there would be too many o(n log(n)) calls to isFile() and isDirectory() while sorting.
		// Split the resources in separate lists for files and directories o(n).
		List/*<BaseResource>*/ files = new ArrayList();
		List/*<BaseResource>*/ directories;
		List/*<BaseResource>*/ others;
		if (mixFilesAndDirectories) {
			directories = files;
			others = files;
		} else {
			directories = new ArrayList();
			others = new ArrayList();
		}
		Iterator iter = resources.iterator();
		while (iter.hasNext()) {
			BaseResource resource = (BaseResource) iter.next();
			if (resource.isFile()) {
				files.add(resource);
			} else if (resource.isDirectory()) {
				directories.add(resource);
			} else {
				others.add(resource);
			}
		}
		Collections.sort(files, comparator);
		if (mixFilesAndDirectories) {
			return files;
		} else {
			Collections.sort(directories, comparator);
			Collections.sort(others, comparator);
			List/*<BaseResource>*/ result = new ArrayList(files.size() + directories.size() + others.size());
			result.addAll(directories);
			result.addAll(files);
			result.addAll(others);
			return result;
		}
	}


	protected String getResourceID() {
		return DEFAULT_RESOURCE_ID;
	}

	/**
	 * This method will only be used for the initial display. Assume a {@code RESOURCE_ID} of {@code /}.
	 * @param context (can be {@code null})
	 */
	public String getXHTMLResourceListing(ServletContext context) {
		return getXHTMLResourceListing(context, getDefaultUserResource());
	}
	/**
	 * @param context (can be {@code null})
	 * @param resource
	 * @return
	 */
	protected String getXHTMLResourceListing(ServletContext context, BaseResource resource) {
		if (resource == null) {
			resource = getDefaultUserResource();
		}
		Collection/*<BaseResource>*/ resources = sortResourcePaths(resource.getChildResources(context), BaseResource.CASE_INSENSITIVE_ORDER, false);
		if (resources == null) {
			ClassLoader cl = null;
			if (context != null) {
				cl = Server.getInstance().getApplication(context).getApplicationInfo().getClassLoader();
			}
			String msg = I18NSupport.getLocalizedMessage(getI18nBundleName(), cl, "error.404", new Object[] {StringUtils.escapeXml(resource.getPath())});//$NON-NLS-1$
			return msg;
		}
		StringBuffer xhtml = new StringBuffer(16384);
		xhtml.append(getXHTMLPreResourceListing(resource));
		xhtml.append("<ul>\n");
		Iterator/*<BaseResource>*/ iter = resources.iterator();
		String urlPrefix;
		if (context != null) {
			try {
				urlPrefix = "?" + AdminActionProvider.ACTION_PARAMETER_NAME + '=' + adminActionProviderCallback.getActionID()
					+ '&' + BaseAdminActionWithContext.CONTEXT_KEY + '=' + URLEncoder.encode(Server.getInstance().getApplication(context).getApplicationInfo().getInternalContextPath(), "UTF-8")
					+ '&' + getResourceID() + '=';
			} catch (UnsupportedEncodingException uue) {
				throw new RuntimeException(uue.toString());
			}
		} else {
			urlPrefix = "?" + AdminActionProvider.ACTION_PARAMETER_NAME + '=' + adminActionProviderCallback.getActionID()
				+ '&' + getResourceID() + '=';
		}
		if (resource.getParentDirectory() != null) {
			// add ".." entry
			appendEntry(xhtml, context, resource.getParentDirectory(), "..", urlPrefix);
		}
		while (iter.hasNext()) {
			BaseResource child = (BaseResource) iter.next();
			appendEntry(xhtml, context, child, null, urlPrefix);
		}
		xhtml.append("</ul>\n");
		xhtml.append(getXHTMLPostResourceListing(resource));
		return xhtml.toString();
	}

	protected String getXHTMLPreResourceListing(BaseResource resource) {
		StringBuilder out = new StringBuilder(256);
		// Text field for direct CD command
		{
			String urlPrefix = "?" + AdminActionProvider.ACTION_PARAMETER_NAME + '=' + adminActionProviderCallback.getActionID()
				+ '&' + getResourceID() + '=';
			out.append("<form action=\"\" method=\"get\">\n");
			out.append("<input type=\"hidden\" name=\""+AdminActionProvider.ACTION_PARAMETER_NAME+"\" value=\""+adminActionProviderCallback.getActionID()+"\"/>\n");
			out.append("<label>");
			out.append(I18NSupport.getLocalizedMessage(getI18nInternalBundleName(), "action.cd"));//$NON-NLS-1$
			out.append("<input type=\"text\" name=\""+getResourceID()+"\" size=\"80\" value=\""+(getDefaultRootResource().equals(resource)?"":StringUtils.escapeXml(resource.getPath()))+"\"/>");
			out.append("</label>&nbsp;");
			out.append("<button name=\"submit\" onclick=\"javascript:jah('"+urlPrefix+"'+"+getResourceID()+".value,'"+DisplayProvider.Util.getId(displayProviderCallback)+"');return false;\">cd</button>\n");
			out.append("</form>\n");
		}
		return out.toString();
	}

	protected String getXHTMLPostResourceListing(BaseResource resource) {
		return "";//$NON-NLS-1$
	}

	/**
	 * @param xhtml
	 * @param context (can be {@code null})
	 * @param resource
	 * @param urlPrefix
	 */
	protected void appendEntry(StringBuffer xhtml, ServletContext context, BaseResource resource, String displayName, String urlPrefix) {
		if (displayName == null) {
			displayName = resource.getPath();
		}
		String pathEncoded;
		try {
			pathEncoded = URLEncoder.encode(resource.getPath(), "UTF-8");
		} catch (UnsupportedEncodingException uue) {
			throw new RuntimeException(uue);
		}
		// span with file/folder information (if available)
		xhtml.append("	<li style=\"");
		if (resource.isFile()) {
			xhtml.append("list-style-type: disc;");
			if (resource.isHidden()) {
				xhtml.append(" color: graytext;");//font-weight: lighter;
			}
			xhtml.append('"');//style
			String title = getEntryTitle(context, resource);
			if (StringUtils.isNotBlank(title)) {
				xhtml.append(" title=\"").append(StringUtils.escapeXml(title)).append('"');
			}
			xhtml.append('>');//<li>
			appendEntryPre(xhtml, context, resource, displayName, urlPrefix);
			if (resource.canRead()) {// link only if we have permission to download the file
				xhtml.append("<a href=\"").append(urlPrefix).append(pathEncoded).append("\" target=\"_blank\">");
			}
			xhtml.append(StringUtils.escapeXml(displayName));
			if (resource.canRead()) {
				xhtml.append("</a>");
			}
			appendEntryPost(xhtml, context, resource, displayName, urlPrefix);
		} else if (resource.isDirectory()) {
			xhtml.append("list-style-type: circle;");
			if (resource.isHidden()) {
				xhtml.append(" font-weight: lighter;");
			}
			xhtml.append('"');//style
			String title = getEntryTitle(context, resource);
			if (StringUtils.isNotBlank(title)) {
				xhtml.append(" title=\"").append(StringUtils.escapeXml(title)).append('"');
			}
			xhtml.append('>');//<li>
			appendEntryPre(xhtml, context, resource, displayName, urlPrefix);
			if (resource.canRead()) {// link only if we have permission to download the file
				// AJAX call
				xhtml.append(BaseAdminActionProvider.buildActionLink(urlPrefix+pathEncoded, StringUtils.escapeXml(displayName), displayProviderCallback));
			} else {
				xhtml.append(StringUtils.escapeXml(displayName));
			}
			appendEntryPost(xhtml, context, resource, displayName, urlPrefix);
		} else { // special file (pipe, etc.)
			xhtml.append("list-style-type: square;\">").append(StringUtils.escapeXml(displayName));
		}
		xhtml.append("</li>\n");
	}

	protected void appendEntryPre(StringBuffer xhtml, ServletContext context, BaseResource resource, String displayName, String urlPrefix) {
		if ( ! "..".equals(displayName)) {
			// link to rename
			if (resource.canRename()) {
				String pathEncoded;
				try {
					pathEncoded = URLEncoder.encode(resource.getPath(), "UTF-8");
				} catch (UnsupportedEncodingException uue) {
					throw new RuntimeException(uue);
				}
				String urlRename = urlPrefix + pathEncoded + '&' + FILE_ACTION_PARAMETER_NAME + '=' + FILE_RENAME_ACTION + '&' + FILE_RENAME_ACTION_PARAMETER_NAME + '=';
				xhtml.append("<a href=\"").append(urlRename).append("\" onclick=\"var newName=window.prompt('").append(I18NSupport.getLocalizedMessage(getI18nInternalBundleName(), "action.rename.prompt")).append("','").append(StringUtils.escapeJavaScript(resource.getPath())).append("'); if(newName) {jah('").append(urlRename).append("'+newName,'").append(DisplayProvider.Util.getId(displayProviderCallback)).append("','POST');} return false;\">");
				xhtml.append(I18NSupport.getLocalizedMessage(getI18nInternalBundleName(), "action.rename"));//$NON-NLS-1$
				xhtml.append("</a>");
				xhtml.append(' ');
			}
		}
	}

	protected void appendEntryPost(StringBuffer xhtml, ServletContext context, BaseResource resource, String displayName, String urlPrefix) {
		if (! "..".equals(displayName)) {
			if (resource.isFile()) {
				long contentLength = resource.getContentLength(context);
				if (contentLength >= 0) {
					NumberFormat format = BytesFormat.getBytesInstance(I18NSupport.getAdminLocale(), true);
					xhtml.append(" (").append(format.format(contentLength)).append(')');
				}
			}
			String pathEncoded;
			try {
				pathEncoded = URLEncoder.encode(resource.getPath(), "UTF-8");
			} catch (UnsupportedEncodingException uue) {
				throw new RuntimeException(uue);
			}
			if (resource.canDelete()) {
				xhtml.append(' ');
				// link to delete
				String jsI18nKey = "action.delete.confirm";//$NON-NLS-1$
				if (resource.isFile()) {
					jsI18nKey += ".file";//$NON-NLS-1$
				} else if (resource.isDirectory()) {
					jsI18nKey += ".directory";//$NON-NLS-1$
				}
				String urlDelete = urlPrefix + pathEncoded + '&' + FILE_ACTION_PARAMETER_NAME + '=' + FILE_DELETE_ACTION;
				String jsConfirmationMessage = I18NSupport.getLocalizedMessage(getI18nInternalBundleName(), jsI18nKey, new Object[] {StringUtils.escapeJavaScript(resource.getPath())});
				xhtml.append(BaseAdminActionProvider.buildActionLink(urlDelete,
						I18NSupport.getLocalizedMessage(getI18nInternalBundleName(), "action.delete"),//$NON-NLS-1$
						jsConfirmationMessage,
						displayProviderCallback)
				);
			}
			if (resource.canCompress()) {
				xhtml.append(' ');
				// link to compress
				String urlCompress = urlPrefix + pathEncoded + '&' + FILE_ACTION_PARAMETER_NAME + '=' + FILE_COMPRESS_ACTION;
				String jsConfirmationMessage = I18NSupport.getLocalizedMessage(getI18nInternalBundleName(), "action.compress.confirm", new Object[] {StringUtils.escapeJavaScript(resource.getPath())});//$NON-NLS-1$
				xhtml.append(BaseAdminActionProvider.buildActionLink(urlCompress,
						I18NSupport.getLocalizedMessage(getI18nInternalBundleName(), "action.compress"),//$NON-NLS-1$
						jsConfirmationMessage,
						displayProviderCallback)
				);
			}
		}
	}

	/**
	 * @param context ServletContext, if any
	 * @param path path of element to compute the tool tip for
	 * @return the tool tip (html "title" attribute) for a given element
	 */
	protected String getEntryTitle(ServletContext context, BaseResource resource) {
		long lastModified = resource.getLastModified(context);
		if (lastModified > 0) {
			String titleLastModified = DateUtils.dateToFormattedDateTimeString(lastModified, DateUtils.DEFAULT_DATE_TIME_FORMAT);
			return titleLastModified;
		}
		return null;
	}

	public void serviceWithContext(HttpServletRequest request, HttpServletResponse response, String context) throws ServletException, IOException {
		ServletContext servletContext = null;
		if (context != null) {
			servletContext = ((ApplicationInfo)Server.getInstance().getApplication(context).getApplicationInfo()).getServletContext();
		}
		String fileAction = request.getParameter(FILE_ACTION_PARAMETER_NAME);
		if (FILE_COMPRESS_ACTION.equals(fileAction)) {
			compressResource(request, response, servletContext);
		} else if (FILE_DELETE_ACTION.equals(fileAction)) {
			deleteResource(request, response, servletContext);
		} else if (FILE_RENAME_ACTION.equals(fileAction)) {
			renameResource(request, response, servletContext);
		} else if (FILE_DOWNLOAD_ACTION.equals(fileAction)) {
			downloadResources(request, response, servletContext);
		} else {
			serveResource(request, response, servletContext);
		}
	}

	/**
	 * @param request
	 * @param response
	 * @param context (can be {@code null})
	 * @throws ServletException
	 * @throws IOException
	 */
	public final void serveResource(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException, IOException {
		String resourceName = request.getParameter(getResourceID());
		BaseResource resource = resourceName == null ? getDefaultUserResource() : getResource(servletContext, resourceName).getCanonicalResource();
		if (resource.isFile()) {
			if (! resource.canRead()) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, StringUtils.escapeXml(resourceName));
				return;
			}
			// serve the required file
			InputStream in = resource.getResourceAsStream(servletContext);
			if (in == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, StringUtils.escapeXml(resourceName));
				return;
			} //else
			String contentType = resource.getContentType(servletContext);
			response.setContentType(contentType);
			response.setHeader("Content-Disposition", getContentDispositionHeader(servletContext, resource));//$NON-NLS-1$
//			resp.setHeader("Content-Description", "");//$NON-NLS-1$
//			resp.setHeader("Content-Transfer-Encoding", "binary");//$NON-NLS-1$//$NON-NLS-2$
			final long lastModified = resource.getLastModified(servletContext);
			if (lastModified > 0) {
				response.setDateHeader("Last-Modified", lastModified);//$NON-NLS-1$
			}
			long contentLength = resource.getContentLength(servletContext);
			sendFileAndCloseStreams(in, contentLength, contentType, request, response);
		} else if (resource.isDirectory()) {
			if (! resource.canRead()) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, StringUtils.escapeXml(resourceName));
				return;
			}
			// display a directory listing
			String data = getXHTMLResourceListing(servletContext, resource);
			PrintWriter out = response.getWriter();
			out.print(data);
			out.flush();
			out.close();
		} else {
			response.sendError(HttpServletResponse.SC_CONFLICT, StringUtils.escapeXml(resourceName));
		}
	}

	/**
	 * @param request
	 * @param response
	 * @param servletContext (can be {@code null})
	 * @throws ServletException
	 * @throws IOException
	 */
	public final void deleteResource(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException, IOException {
		String[] resourceNames = request.getParameterValues(getResourceID());
		if (resourceNames == null || resourceNames.length == 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		assert resourceNames.length >= 1;
		String resourceName = "stupid compiler...";//$NON-NLS-1$
		BaseResource resource = null; // stupid compiler...
		Exception lastException = null;
		for (int i = 0; i < resourceNames.length; ++i) {
			resourceName = resourceNames[i];
			// error on empty file, only if no mass deletion (in which case, best effort)
			if (resourceNames.length == 1 && StringUtils.isBlank(resourceName)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			resource = getResource(servletContext, resourceName).getCanonicalResource();
			if (resource.isFile()) {
				// delete the required file
				try {
					deleteFile(resource, request, response, servletContext);
				} catch (IOException ioe) {
					lastException = ioe;
				}
			} else if (resource.isDirectory()) {
				try {
					deleteDirectory(resource, request, response, servletContext);
				} catch (IOException ioe) {
					lastException = ioe;
				}
			} else {
				// error on single file request; if mass deletion: best effort
				if (resourceNames.length == 1) {
					response.sendError(HttpServletResponse.SC_CONFLICT, StringUtils.escapeXml(resourceName));
					return;
				}
			}
		}
		// error on single file request; if mass deletion: best effort, and error at the end
		if (lastException != null) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, StringUtils.escapeXml(lastException.toString()));
			return;
		}
		// TODO display message that file / directory was deleted
		// redirect to a directory listing
		sendRedirect(request, response, resource.getParentDirectory());
	}

	protected boolean deleteFile(BaseResource resource, HttpServletRequest request, HttpServletResponse response, ServletContext context) throws ServletException, IOException {
		return resource.delete();
	}

	protected boolean deleteDirectory(BaseResource resource, HttpServletRequest request, HttpServletResponse response, ServletContext context) throws ServletException, IOException {
		return resource.delete();
	}

	/**
	 * @param request
	 * @param response
	 * @param servletContext (can be {@code null})
	 * @throws ServletException
	 * @throws IOException
	 */
	public final void compressResource(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException, IOException {
		String[] resourceNames = request.getParameterValues(getResourceID());
		if (resourceNames == null || resourceNames.length == 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		assert resourceNames.length >= 1;
		String resourceName = "stupid compiler...";//$NON-NLS-1$
		BaseResource resource = null; // stupid compiler...
		Exception lastException = null;
		for (int i = 0; i < resourceNames.length; ++i) {
			resourceName = resourceNames[i];
			// error on empty file, only if no mass compression (in which case, best effort)
			if (StringUtils.isBlank(resourceName) && resourceNames.length == 1) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			resource = getResource(servletContext, resourceName).getCanonicalResource();
			if (resource.isFile()) {
				if (! resource.canRead()) {
					if (resourceNames.length == 1) {
						response.sendError(HttpServletResponse.SC_FORBIDDEN, StringUtils.escapeXml(resourceName));
						return;
					} else {
						continue;
					}
				}
				// compress the required file
				if ( ! resource.canCompress()) {
					if (resourceNames.length == 1) {
						response.sendError(HttpServletResponse.SC_CONFLICT, StringUtils.escapeXml(resourceName));
						return;
					} else {
						continue;
					}
				}
				InputStream in = resource.getResourceAsStream(servletContext);
				if (in == null) {
					if (resourceNames.length == 1) {
						response.sendError(HttpServletResponse.SC_NOT_FOUND, StringUtils.escapeXml(resourceName));
						return;
					} else {
						continue;
					}
				} //else
				in.close();
				// compress the required file
				try {
					compressFile(resource, request, response, servletContext);
				} catch (IOException ioe) {
					lastException = ioe;
				}
			} else if (resource.isDirectory()) {
				try {
					compressDirectory(resource, request, response, servletContext);
				} catch (IOException ioe) {
					lastException = ioe;
				}
			} else {
				// error on single file request; if mass compression: best effort
				if (resourceNames.length == 1) {
					response.sendError(HttpServletResponse.SC_CONFLICT, StringUtils.escapeXml(resourceName));
					return;
				}
			}
		}
		// error on single file request; if mass compression: best effort, and error at the end
		if (lastException != null) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, StringUtils.escapeXml(lastException.toString()));
			return;
		}
		// TODO display message that file / directory was compressed
		// redirect to a directory listing
		sendRedirect(request, response, resource.getParentDirectory());
	}

	protected boolean compressFile(BaseResource resource, HttpServletRequest request, HttpServletResponse response, ServletContext context) throws ServletException, IOException {
		return resource.compress();
	}

	protected boolean compressDirectory(BaseResource resource, HttpServletRequest request, HttpServletResponse response, ServletContext context) throws ServletException, IOException {
		return resource.compress();
	}

	/**
	 * @param request
	 * @param response
	 * @param servletContext (can be {@code null})
	 * @throws ServletException
	 * @throws IOException
	 */
	public final void renameResource(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException, IOException {
		String resourceName = request.getParameter(getResourceID());
		String newName = request.getParameter(FILE_RENAME_ACTION_PARAMETER_NAME);
		if (StringUtils.isBlank(resourceName) || StringUtils.isBlank(newName) || newName.equals(resourceName)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		newName = getResource(servletContext, newName).getCanonicalResource().getPath();
		BaseResource resource = getResource(servletContext, resourceName).getCanonicalResource();
		// rename the required file
		boolean shouldRedirect;
		try {
			shouldRedirect = resource.renameTo(newName);
		} catch (IOException ioe) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, StringUtils.escapeXml(ioe.toString()));
			return;
		}
		if (shouldRedirect) {
			// TODO display message that file was renamed
			// redirect to a directory listing
			sendRedirect(request, response, resource.getParentDirectory());
		}
	}

	protected boolean renameFile(BaseResource resource, String newName, HttpServletRequest request, HttpServletResponse response, ServletContext context) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
		return false;
	}

	/**
	 * @param request
	 * @param response
	 * @param servletContext (can be {@code null})
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void downloadResources(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException, IOException {
		String[] resourceNames = request.getParameterValues(getResourceID());
		if (resourceNames == null || resourceNames.length == 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		response.setContentType("application/zip");//$NON-NLS-1$
		{
			String theFileName = "Archive.zip"; // default name
			if (resourceNames.length == 1) {
				theFileName = getResource(servletContext, resourceNames[0]).getFileName() + ".zip";//$NON-NLS-1$
			} else {
				// must offer a default file name, otherwise the browser saves as "messadmin" (no .zip)
				for (int i = 0; i < resourceNames.length; ++i) {
					BaseResource parent = getResource(servletContext, resourceNames[i]).getParentDirectory();
					if (parent != null && StringUtils.isNotBlank(parent.getFileName())) {
						theFileName = parent.getFileName() + ".zip";//$NON-NLS-1$
						break;
					}
				}
			}
			theFileName = StringUtils.replace(theFileName, "\"", "\\\"");//$NON-NLS-1$//$NON-NLS-2$
			String contentDisposition = "inline;filename=\""+theFileName+'"';//$NON-NLS-1$
			response.setHeader("Content-Disposition", contentDisposition);//$NON-NLS-1$
		}
//		resp.setHeader("Content-Description", "");//$NON-NLS-1$
//		resp.setHeader("Content-Transfer-Encoding", "binary");//$NON-NLS-1$//$NON-NLS-2$
		ZipConfiguration zipConfiguration = new ZipConfiguration();
		zipConfiguration.setCompressionLevel(Deflater.BEST_SPEED);
		ZipOutputStreamAdapter zipOutputStream = ZipUtils.getZipOutputStream(null, response.getOutputStream(), zipConfiguration);
		try {
			for (String resourcePath : resourceNames) {
				BaseResource resource = getResource(servletContext, resourcePath);
				compress(resource, servletContext, zipOutputStream, "");
			}
		} finally {
			zipOutputStream.getDelegate().close();
		}
	}
	/**
	 * Note: this method will not close the output stream!
	 */
	private void compress(BaseResource source, ServletContext servletContext, ZipOutputStreamAdapter out, String rootPath) throws IOException {
		if (source.isFile() && source.canRead()) {
			// Store file
			ZipEntry zipEntry = new clime.messadmin.utils.compress.zip.ZipEntry(rootPath + source.getFileName());// store relative (to archive root) path only!
			zipEntry.setSize(source.getContentLength(servletContext));
			zipEntry.setTime(source.getLastModified(servletContext));
			out.putNextEntry(zipEntry);
			InputStream in = source.getResourceAsStream(servletContext);
			try {
				copy(in, out.getDelegate());
			} finally {
				in.close();
			}
			out.closeEntry();
		} else if (source.isDirectory()) {
			// Store directory
			rootPath += source.getFileName() + '/';
			ZipEntry zipEntry = new ZipEntry(rootPath);// store relative (to archive root) path only!
			zipEntry.setTime(source.getLastModified(servletContext));
			out.putNextEntry(zipEntry);
			out.closeEntry();
			// Iterate
			Collection<BaseResource> childs = source.getChildResources(servletContext);
			for (BaseResource child : childs) {
				compress(child, servletContext, out, rootPath);
			}
		} else {
			//skip
//			log.warn("Skipping file {}, which is neither a file nor a directory, or is not readable", source);
		}
	}

	protected void sendRedirect(HttpServletRequest request, HttpServletResponse response, BaseResource resource) throws ServletException, IOException {
		StringBuffer redirectURL = ((BaseAdminActionProvider)adminActionProviderCallback).getURL(request, response);
		redirectURL.append('&').append(getResourceID()).append('=').append(resource.getPath());
		response.sendRedirect(response.encodeRedirectURL(redirectURL.toString()));
	}


	/**
	 * @param input
	 * @param request
	 * @param response
	 */
	private void sendFileAndCloseStreams(InputStream input, long contentLength, String mimeType, HttpServletRequest request, HttpServletResponse response) throws IOException {
		OutputStream output = response.getOutputStream();
		if (contentLength > COMPRESSION_MIN_SIZE
				&& ! (output instanceof java.util.zip.GZIPOutputStream)
				&& ! (output instanceof clime.messadmin.utils.compress.gzip.GZIPOutputStream)
				&& headerContains(request, "Accept-Encoding", "gzip")//$NON-NLS-1$//$NON-NLS-2$
				&& ! headerContains(response, "Content-Encoding", "gzip")//$NON-NLS-1$//$NON-NLS-2$
				&& isCompressableMimeType(mimeType)
				) {
			GZipConfiguration config = new GZipConfiguration();
			config.setCompressionLevel(Deflater.BEST_SPEED);
			output = new clime.messadmin.utils.compress.gzip.GZIPOutputStream(output, 8192, config);
			response.setHeader("Content-Encoding", "gzip");//$NON-NLS-1$//$NON-NLS-2$
			response.addHeader("Vary", "Accept-Encoding");//$NON-NLS-1$//$NON-NLS-2$
			//response.setContentLength(compressedBytes.length);
		}
		if (contentLength >= 0
				&& ! (output instanceof java.util.zip.GZIPOutputStream)
				&& ! (output instanceof clime.messadmin.utils.compress.gzip.GZIPOutputStream)
				&& contentLength < Integer.MAX_VALUE) {
			response.setContentLength((int) contentLength);
		}
		copyAndClose(input, output);
	}

	protected void copy(InputStream input, OutputStream output) throws IOException {
		int nRead;
		byte[] buffer = new byte[32768];//FIXME magic number
		while ((nRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, nRead);
		}
		output.flush();
	}

	protected void copyAndClose(InputStream input, OutputStream output) throws IOException {
		try {
			copy(input, output);
		} finally {
			input.close();
			output.close();
		}
	}

	/**
	 * Checks if request contains the header value.
	 */
	private boolean headerContains(final HttpServletRequest request, final String header, final String value) {
		final Enumeration<String> accepted = request.getHeaders(header);
		if (accepted != null) {
			while (accepted.hasMoreElements()) {
				final String headerValue = accepted.nextElement();
				if (headerValue.indexOf(value) != -1) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Checks if response contains the header value.
	 */
	private boolean headerContains(final HttpServletResponse response, final String header, final String value) {
		final Iterator<String> accepted = MessAdminResponseWrapper.getHeaders(response, header).iterator();
		while (accepted.hasNext()) {
			final String headerValue = accepted.next();
			if (headerValue.indexOf(value) != -1) {
				return true;
			}
		}
		return false;
	}

	private static final Collection<String> compressableMimeType = Arrays.asList(
			//"text/*",
			"application/xhtml+xml",
			"application/xml",
			"application/xslt+xml",
			"application/rss+xml",
			"application/atom+xml",
			"application/rdf+xml",
			"application/javascript",
			"application/ecmascript",
			"application/json",
			"image/bmp",
			"image/svg+xml"
			// + all fonts except in WOFF format
	);
	protected boolean isCompressableMimeType(String mimeType) {
		mimeType = mimeType.toLowerCase();
		return
			// enable compression for text files
			mimeType.startsWith("text/")
			// enable compression for "binary" text files and known compressible binaries
			|| compressableMimeType.contains(mimeType)
			// fall back to enable compression for unknown types (e.g. log files with no extensions...)
			// all non-compressible types like audio, video, images, pdf, flash... are thus excluded
			|| DEFAULT_MIME_TYPE.equals(mimeType);
	}


	/***
	 * @param context (can be {@code null})
	 * @param resource
	 * @return
	 */
	protected String getContentDispositionHeader(ServletContext context, BaseResource resource) {
		String result = "inline";//$NON-NLS-1$ //rfc2183: inline|attachment
		// remove directories
		{
			String theFileName = resource.getFileName();
			theFileName = StringUtils.replace(theFileName, "\"", "\\\"");//$NON-NLS-1$//$NON-NLS-2$
			result += ";filename=\""+theFileName+'"';//$NON-NLS-1$
		}
		//;creation-date="Wed, 12 Feb 1997 16:29:51 -0500"//RFC 822 'date-time'
		long lastModified = resource.getLastModified(context);
		if (lastModified > 0) {//RFC 822 'date-time'
			result += ";modification-date=\""+DateUtils.formatRFC2822Date(lastModified)+'"';//$NON-NLS-1$
		}
		long contentLength = resource.getContentLength(context);
		if (contentLength >= 0) {
			result += ";size=" + contentLength;//$NON-NLS-1$
		}
		return result;
	}

	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";//$NON-NLS-1$
}
