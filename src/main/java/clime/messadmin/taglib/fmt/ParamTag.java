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
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

/**
 * <p>A handler for &lt;param&gt; that supports rtexprvalue-based
 * message arguments.</p>
 *
 * @author Jan Luehe
 */

public class ParamTag extends BodyTagSupport {

    //*********************************************************************
    // Accessor methods

    // for tag attribute
	public void setValue(Object value) throws JspTagException {
		this.value = value;
		this.valueSpecified = true;
	}
	public void setValue(long value) throws JspTagException {
		this.value = Long.valueOf(value);
		this.valueSpecified = true;
	}
	public void setValue(int value) throws JspTagException {
		this.value = Integer.valueOf(value);
		this.valueSpecified = true;
	}
	public void setValue(double value) throws JspTagException {
		this.value = new Double(value);
		this.valueSpecified = true;
	}
	public void setValue(float value) throws JspTagException {
		this.value = new Float(value);
		this.valueSpecified = true;
	}

    //*********************************************************************
    // Protected state

    protected Object value;                          // 'value' attribute
    protected boolean valueSpecified;                 // status


    //*********************************************************************
    // Constructor and initialization

    public ParamTag() {
        super();
        init();
    }

    private void init() {
        value = null;
        valueSpecified = false;
    }


    //*********************************************************************
    // Tag logic

    // Supply our value to our parent <fmt:message> tag
    public int doEndTag() throws JspException {
        Tag t = findAncestorWithClass(this, MessageTag.class);
        if (t == null) {
            throw new JspTagException("&lt;param&gt; outside &lt;message&gt;");//$NON-NLS-1$
                            //Resources.getMessage("PARAM_OUTSIDE_MESSAGE"));
        }
        MessageTag parent = (MessageTag) t;

        /*
         * Get argument from 'value' attribute or body, as appropriate, and
         * add it to enclosing <fmt:message> tag, even if it is null or equal
         * to "".
         */
        Object input = null;
        // determine the input by...
        if (valueSpecified) {
            // ... reading 'value' attribute
            input = value;
        } else {
            // ... retrieving and trimming our body (TLV has ensured that it's
            // non-empty)
            input = bodyContent.getString().trim();
        }
        parent.addParam(input);

        return EVAL_PAGE;
    }

    // Releases any resources we may have (or inherit)
    public void release() {
        super.release();
        init();
    }
}
