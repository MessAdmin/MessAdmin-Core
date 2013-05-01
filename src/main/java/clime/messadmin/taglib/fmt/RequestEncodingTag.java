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

import java.io.UnsupportedEncodingException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * <p>A handler for &lt;requestEncoding&gt; that supports rtexprvalue-based
 * attributes.</p>
 *
 * @author Jan Luehe
 */

public class RequestEncodingTag extends TagSupport {

    //*********************************************************************
    // Accessor methods

    // for tag attribute
    public void setValue(String value) throws JspTagException {
        this.value = value;
    }

    //*********************************************************************
    // Package-scoped constants

    static final String REQUEST_CHAR_SET =
	"javax.servlet.jsp.jstl.fmt.request.charset";//$NON-NLS-1$


    //*********************************************************************
    // Private constants

    private static final String DEFAULT_ENCODING = "ISO-8859-1";//$NON-NLS-1$


    //*********************************************************************
    // Tag attributes

    protected String value;             // 'value' attribute
    

    //*********************************************************************
    // Derived information
    
    protected String charEncoding;   // derived from 'value' attribute  
    

    //*********************************************************************
    // Constructor and initialization

    public RequestEncodingTag() {
	super();
	init();
    }

    private void init() {
	value = null;
    }


    //*********************************************************************
    // Tag logic

    public int doEndTag() throws JspException {
        charEncoding = value;
	if ((charEncoding == null)
	        && (pageContext.getRequest().getCharacterEncoding() == null)) { 
            // Use charset from session-scoped attribute
	    charEncoding = (String)
		pageContext.getAttribute(REQUEST_CHAR_SET,
					 PageContext.SESSION_SCOPE);
	    if (charEncoding == null) {
		// Use default encoding
		charEncoding = DEFAULT_ENCODING;
	    }
	}

	/*
	 * If char encoding was already set in the request, we don't need to 
	 * set it again.
	 */
	if (charEncoding != null) {
	    try {
		pageContext.getRequest().setCharacterEncoding(charEncoding);
	    } catch (UnsupportedEncodingException uee) {
		throw new JspTagException(uee.toString());//, uee);
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
