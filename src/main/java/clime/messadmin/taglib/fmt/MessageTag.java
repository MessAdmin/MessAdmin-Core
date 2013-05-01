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

package clime.messadmin.taglib.fmt;
//package org.apache.taglibs.standard.tag.rt.fmt;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

import clime.messadmin.taglib.core.Util;
import clime.messadmin.taglib.jstl.fmt.LocalizationContext;

/**
 * <p>A handler for &lt;message&gt; that supports rtexprvalue-based
 * attributes.</p>
 *
 * @author Jan Luehe
 */

public class MessageTag extends BodyTagSupport {

    //*********************************************************************
    // Accessor methods

    // for tag attribute
    public void setKey(String key) throws JspTagException {
        this.keyAttrValue = key;
        this.keySpecified = true;
    }

    // for tag attribute
    public void setBundle(LocalizationContext locCtxt) throws JspTagException {
        this.bundleAttrValue = locCtxt;
        this.bundleSpecified = true;
    }

    //*********************************************************************
    // Public constants

    public static final String UNDEFINED_KEY = "???";//$NON-NLS-1$


    //*********************************************************************
    // Protected state

    protected String keyAttrValue;       // 'key' attribute value
    protected boolean keySpecified;	 // 'key' attribute specified
    protected LocalizationContext bundleAttrValue; // 'bundle' attribute value
    protected boolean bundleSpecified;   // 'bundle' attribute specified?


    //*********************************************************************
    // Private state

    private String var;                           // 'var' attribute
    private int scope;                            // 'scope' attribute
    private List params;


    //*********************************************************************
    // Constructor and initialization

    public MessageTag() {
	super();
	params = new ArrayList();
	init();
    }

    private void init() {
	var = null;
	scope = PageContext.PAGE_SCOPE;
	keyAttrValue = null;
	keySpecified = false;
	bundleAttrValue = null;
	bundleSpecified = false;
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
     * Adds an argument (for parametric replacement) to this tag's message.
     *
     * @see ParamSupport
     */
    public void addParam(Object arg) {
	params.add(arg);
    }


    //*********************************************************************
    // Tag logic

    public int doStartTag() throws JspException {
	params.clear();
	return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {

        String key = null;
	LocalizationContext locCtxt = null;

        // determine the message key by...
        if (keySpecified) {
	    // ... reading 'key' attribute
	    key = keyAttrValue;
	} else {
	    // ... retrieving and trimming our body
	    if (bodyContent != null && bodyContent.getString() != null)
	        key = bodyContent.getString().trim();
	}

	if ((key == null) || key.equals("")) {//$NON-NLS-1$
	    try {
		pageContext.getOut().print("??????");//$NON-NLS-1$
	    } catch (IOException ioe) {
		throw new JspTagException(ioe.toString());//, ioe);
	    }
	    return EVAL_PAGE;
	}

	String prefix = null;
	if (!bundleSpecified) {
	    Tag t = findAncestorWithClass(this, BundleTag.class);
	    if (t != null) {
		// use resource bundle from parent <bundle> tag
		BundleTag parent = (BundleTag) t;
		locCtxt = parent.getLocalizationContext();
		prefix = parent.getPrefix();
	    } else {
		locCtxt = BundleTag.getLocalizationContext(pageContext);
	    }
	} else {
	    // localization context taken from 'bundle' attribute
	    locCtxt = bundleAttrValue;
	    if (locCtxt.getLocale() != null) {
		SetLocaleTag.setResponseLocale(pageContext,
						   locCtxt.getLocale());
	    }
	}
        
 	String message = UNDEFINED_KEY + key + UNDEFINED_KEY;
	if (locCtxt != null) {
	    ResourceBundle bundle = locCtxt.getResourceBundle();
	    if (bundle != null) {
		try {
		    // prepend 'prefix' attribute from parent bundle
		    if (prefix != null)
			key = prefix + key;
		    message = bundle.getString(key);
		    // Perform parametric replacement if required
		    if (!params.isEmpty()) {
			Object[] messageArgs = params.toArray();
			MessageFormat formatter = new MessageFormat(""); // empty pattern, default Locale //$NON-NLS-1$
			if (locCtxt.getLocale() != null) {
			    formatter.setLocale(locCtxt.getLocale());
			} else {
                            // For consistency with the <fmt:formatXXX> actions,
                            // we try to get a locale that matches the user's preferences
                            // as well as the locales supported by 'date' and 'number'.
                            //System.out.println("LOCALE-LESS LOCCTXT: GETTING FORMATTING LOCALE");
                            Locale locale = SetLocaleTag.getFormattingLocale(pageContext);
                            //System.out.println("LOCALE: " + locale);
                            if (locale != null) {
                                formatter.setLocale(locale);
                            }
                        }
			formatter.applyPattern(message);
			message = formatter.format(messageArgs);
		    }
		} catch (MissingResourceException mre) {
		    message = UNDEFINED_KEY + key + UNDEFINED_KEY;
		}
	    }
	}

	if (var != null) {
	    pageContext.setAttribute(var, message, scope);	
	} else {
	    try {
		pageContext.getOut().print(message);
	    } catch (IOException ioe) {
		throw new JspTagException(ioe.toString());//, ioe);
	    }
	}

	return EVAL_PAGE;
    }

    // Releases any resources we may have (or inherit)
    public void release() {
    super.release();
	init();
    }
}
