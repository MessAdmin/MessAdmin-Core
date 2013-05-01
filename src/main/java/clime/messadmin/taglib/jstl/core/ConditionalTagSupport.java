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

package clime.messadmin.taglib.jstl.core;
//package javax.servlet.jsp.jstl.core;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * <p>Abstract class that facilitates implementation of conditional actions 
 * where the boolean result is exposed as a JSP scoped variable. The 
 * boolean result may then be used as the test condition in a &lt;c:when&gt;
 * action.</p>
 *
 * <p>This base class provides support for:</p>
 * 
 * <ul>
 *  <li> Conditional processing of the action's body based on the returned value
 *       of the abstract method <tt>condition()</tt>.</li>
 *  <li> Storing the result of <tt>condition()</tt> as a <tt>Boolean</tt> object
 *       into a JSP scoped variable identified by attributes <tt>var</tt> and
 *       <tt>scope</tt>.
 * </ul>
 * 
 * @author Shawn Bayern
 */

public abstract class ConditionalTagSupport extends TagSupport {
    //*********************************************************************
    // Abstract methods

    /**
     * <p>Subclasses implement this method to compute the boolean result
     * of the conditional action. This method is invoked once per tag invocation 
     * by <tt>doStartTag()</tt>.
     *
     * @return a boolean representing the condition that a particular subclass
     *   uses to drive its conditional logic.
     */
    protected abstract boolean condition() throws JspTagException;


    //*********************************************************************
    // Constructor

    /**
     * Base constructor to initialize local state.  As with <tt>TagSupport</tt>,
     * subclasses should not implement constructors with arguments, and
     * no-argument constructors implemented by subclasses must call the 
     * superclass constructor.
     */
    public ConditionalTagSupport() {
        super();
        init();
    }


    //*********************************************************************
    // Lifecycle management and implementation of conditional behavior

    /**
     * Includes its body if <tt>condition()</tt> evaluates to true.
     */
    public int doStartTag() throws JspException {

        // execute our condition() method once per invocation
        result = condition();

        // expose variables if appropriate
        exposeVariables();

        // handle conditional behavior
        if (result)
            return EVAL_BODY_INCLUDE;
        else
            return SKIP_BODY;
    }

    /**
     * Releases any resources this ConditionalTagSupport may have (or inherit).
     */
    public void release() {
        super.release();
        init();
    }

    //*********************************************************************
    // Private state

    private boolean result;             // the saved result of condition()
    private String var;			// scoped attribute name
    private int scope;			// scoped attribute scope


    //*********************************************************************
    // Accessors

    /**
     * Sets the 'var' attribute.
     *
     * @param var Name of the exported scoped variable storing the result of
     * <tt>condition()</tt>.
     */
    public void setVar(String var) {
	this.var = var;
    }

    /**
     * Sets the 'scope' attribute.
     *
     * @param scope Scope of the 'var' attribute
     */
    public void setScope(String scope) {
	if (scope.equalsIgnoreCase("page"))//$NON-NLS-1$
	    this.scope = PageContext.PAGE_SCOPE;
	else if (scope.equalsIgnoreCase("request"))//$NON-NLS-1$
	    this.scope = PageContext.REQUEST_SCOPE;
	else if (scope.equalsIgnoreCase("session"))//$NON-NLS-1$
	    this.scope = PageContext.SESSION_SCOPE;
	else if (scope.equalsIgnoreCase("application"))//$NON-NLS-1$
	    this.scope = PageContext.APPLICATION_SCOPE;
	// TODO: Add error handling?  Needs direction from spec.
    }


    //*********************************************************************
    // Utility methods

    // expose attributes if we have a non-null 'var'
    private void exposeVariables() {
        if (var != null)
            pageContext.setAttribute(var, result ? Boolean.TRUE : Boolean.FALSE/*new Boolean(result)*/, scope);
    }

    // initializes internal state
    private void init() {
        result = false;                 // not really necessary
	    var = null;
	    scope = PageContext.PAGE_SCOPE;
    }
}
