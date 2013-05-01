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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import clime.messadmin.taglib.core.Util;
import clime.messadmin.taglib.jstl.core.Config;
import clime.messadmin.taglib.jstl.fmt.LocalizationContext;

/**
 * <p>A handler for &lt;setBundle&gt; that supports rtexprvalue-based
 * attributes.</p>
 *
 * @author Jan Luehe
 */

public class SetBundleTag extends TagSupport {

    //*********************************************************************
    // Accessor methods

    // for tag attribute
    public void setBasename(String basename) throws JspTagException {
        this.basename = basename;
    }

    //*********************************************************************
    // Protected state

    protected String basename;                  // 'basename' attribute


    //*********************************************************************
    // Private state

    private int scope;                          // 'scope' attribute
    private String var;                         // 'var' attribute


    //*********************************************************************
    // Constructor and initialization

    public SetBundleTag() {
	super();
	init();
    }

    private void init() {
	basename = null;
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
    // Tag logic

    public int doEndTag() throws JspException {
	LocalizationContext locCtxt =
	    BundleTag.getLocalizationContext(pageContext, basename);

	if (var != null) {
	    pageContext.setAttribute(var, locCtxt, scope);
	} else {
	    Config.set(pageContext, Config.FMT_LOCALIZATION_CONTEXT, locCtxt,
		       scope);
	}

	return EVAL_PAGE;
    }

    // Releases any resources we may have (or inherit)
    public void release() {
    super.release();
	init();
    }
}
