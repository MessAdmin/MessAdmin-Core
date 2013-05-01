/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package clime.messadmin.taglib.core;
//package org.apache.taglibs.standard.tag.common.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * <p>Support for tag handlers for &lt;url&gt;, the URL creation
 * and rewriting tag in JSTL 1.0.</p>
 *
 * @author Shawn Bayern, C&eacute;drik LIME
 */

public class UrlTag extends BodyTagSupport implements ParamParent {

    //*********************************************************************
    // Protected state

    protected String value;                      // 'value' attribute
    protected String context;             // 'context' attribute

    //*********************************************************************
    // Private state

    private String var;                          // 'var' attribute
    private int scope;                 // processed 'scope' attr
    private ParamSupport.ParamManager params;     // added parameters

    //*********************************************************************
    // Constructor and initialization

    public UrlTag() {
        super();
        init();
    }

    private void init() {
        value = var = null;
        params = null;
        context = null;
        scope = PageContext.PAGE_SCOPE;
    }


    //*********************************************************************
    // Tag attributes known at translation time

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }


    //*********************************************************************
    // Collaboration with subtags

    /**
     * Adds a parameter to this tag's URL.  The intent is that the
     * &lt;param&gt; subtag will call this to register URL parameters.
     * Assumes that 'name' and 'value' are appropriately encoded and do
     * not contain any meaningful metacharacters; in order words, escaping
     * is the responsibility of the caller.
     *
     * @see ParamSupport
     */
    // inherit Javadoc
    public void addParameter(String name, String value) {
        params.addParameter(name, value);
    }


    //*********************************************************************
    // Tag logic

    // resets any parameters that might be sent
    public int doStartTag() throws JspException {
        params = new ParamSupport.ParamManager();
        return EVAL_BODY_BUFFERED;
    }


    // gets the right value, encodes it, and prints or stores it
    public int doEndTag() throws JspException {
        String result;                // the eventual result

        // add (already encoded) parameters
        String baseUrl = resolveUrl(value, context, pageContext);
        result = params.aggregateParams(baseUrl);

        // if the URL is relative, rewrite it
        if (!isAbsoluteUrl(result)) {
            HttpServletResponse response =
                    ((HttpServletResponse) pageContext.getResponse());
            result = response.encodeURL(result);
        }

        // store or print the output
        if (var != null) {
            pageContext.setAttribute(var, result, scope);
        } else {
            try {
                pageContext.getOut().print(result);
            } catch (java.io.IOException ex) {
                throw new JspTagException(ex.toString());//, ex);//FIXME JSP 2.0
            }
        }

        return EVAL_PAGE;
    }

    // Releases any resources we may have (or inherit)
    public void release() {
        super.release();
        init();
    }

    //*********************************************************************
    // Utility methods

    public static String resolveUrl(
            String url, String context, PageContext pageContext)
            throws JspException {
        // don't touch absolute URLs
        if (isAbsoluteUrl(url)) {
            return url;
        }

        // normalize relative URLs against a context root
        HttpServletRequest request =
                (HttpServletRequest) pageContext.getRequest();
        if (context == null) {
            if (url.startsWith("/")) {//$NON-NLS-1$
                return (request.getContextPath() + url);
            } else {
                return url;
            }
        } else {
            if (!context.startsWith("/") || !url.startsWith("/")) {//$NON-NLS-1$//$NON-NLS-2$
                throw new JspTagException("In URL tags, when the \"context\" attribute is specified, values of both \"context\" and \"url\" must start with \"/\".");//$NON-NLS-1$
            }
            if (context.endsWith("/") && url.startsWith("/")) {//context.equals("/")//$NON-NLS-1$//$NON-NLS-2$
                // Don't produce string starting with '//', many
                // browsers interpret this as host name, not as
                // path on same host. Bug 22860
                // Also avoid // inside the url. Bug 34109
                return (context.substring(0, context.length() - 1) + url);//return url;
            } else {
                return (context + url);
            }
        }
    }

    //*********************************************************************
    // Accessor methods

    // for tag attribute
    public void setValue(String value) throws JspTagException {
        this.value = value;
    }

    // for tag attribute
    public void setContext(String context) throws JspTagException {
        this.context = context;
    }


    /** <p>Valid characters in a scheme.</p>
     *  <p>RFC 1738 says the following:</p>
     *  <blockquote>
     *   Scheme names consist of a sequence of characters. The lower
     *   case letters "a"--"z", digits, and the characters plus ("+"),
     *   period ("."), and hyphen ("-") are allowed. For resiliency,
     *   programs interpreting URLs should treat upper case letters as
     *   equivalent to lower case in scheme names (e.g., allow "HTTP" as
     *   well as "http").
     *  </blockquote>
     * <p>We treat as absolute any URL that begins with such a scheme name,
     * followed by a colon.</p>
     */
    private static final String VALID_SCHEME_CHARS =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+.-";//$NON-NLS-1$
    /**
     * Returns <tt>true</tt> if our current URL is absolute,
     * <tt>false</tt> otherwise.
     */
    protected static boolean isAbsoluteUrl(String url) {
        // a null URL is not absolute, by our definition
        if (url == null) {
            return false;
        }

        // do a fast, simple check first
        int colonPos;
        if ((colonPos = url.indexOf(':')) == -1) {
            return false;
        }

        // if we DO have a colon, make sure that every character
        // leading up to it is a valid scheme character
        for (int i = 0; i < colonPos; ++i) {
            if (VALID_SCHEME_CHARS.indexOf(url.charAt(i)) == -1) {
                return false;
            }
        }

        // if so, we've got an absolute url
        return true;
    }
}
