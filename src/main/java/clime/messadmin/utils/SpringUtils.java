package clime.messadmin.utils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletContext;

/**
 * @author C&eacute;drik LIME
 */
public final class SpringUtils {

	private static transient Method getWebApplicationContext;
	private static transient Method beansOfTypeIncludingAncestors;

	static {
		try {
			Class<?> webApplicationContextUtilsClass = Class.forName("org.springframework.web.context.support.WebApplicationContextUtils");
			getWebApplicationContext = webApplicationContextUtilsClass.getMethod("getWebApplicationContext", ServletContext.class);
			//Class<?> webApplicationContextClass = Class.forName("org.springframework.web.context.WebApplicationContext");
			Class<?> beanFactoryUtilsClass = Class.forName("org.springframework.beans.factory.BeanFactoryUtils");
			Class<?> listableBeanFactoryClass = Class.forName("org.springframework.beans.factory.ListableBeanFactory");
			beansOfTypeIncludingAncestors = beanFactoryUtilsClass.getMethod("beansOfTypeIncludingAncestors", listableBeanFactoryClass, Class.class, Boolean.TYPE, Boolean.TYPE);
		} catch (LinkageError e) {
		} catch (ClassNotFoundException e) {
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
	}

	private SpringUtils() {
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactoryUtils#beansOfTypeIncludingAncestors(ListableBeanFactory, Class<T>)
	 * @param context
	 * @return
	 */
	public static <T> Map<String, T> beansOfTypeIncludingAncestors(ServletContext context, Class<T> type) {
		Map<String, T> result = Collections.emptyMap();
		//WebApplicationContext webContext = WebApplicationContextUtils.getWebApplicationContext(context);
		//return BeanFactoryUtils.beansOfTypeIncludingAncestors(webContext, type, true, false);
		try {
			Object webContext = getWebApplicationContext.invoke(null, context);
			result = (Map<String, T>) beansOfTypeIncludingAncestors.invoke(null, webContext, type, true, false);
		} catch (Exception ignore) {
		}
		return result;
	}

}
