package org.openmrs.module.namephonetics;

import javax.servlet.ServletContext;

import org.openmrs.api.context.Context;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;

public class NamePhoneticsContextAware implements ApplicationContextAware, ServletContextAware {

    protected static ApplicationContext appContext = null;
    protected static ServletContext srvContext = null;
    
    /**
     * Sets the Spring application context
     * @param ctx the application context
     */
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        appContext = ctx;
    }

    /**
     * Gets the Spring application context
     * @return the application context
     */
    public static ApplicationContext getApplicationContext() {
        return appContext;
    }

    /**
     * Sets the servlet context
     * @param ctx the servlet context
     */
    public void setServletContext(ServletContext ctx) {
        srvContext = ctx;
    }
    
    /**
     * Gets the servlet context
     * @return the servlet context
     */
    public static ServletContext getServletContext() {
        return srvContext;
    }
    
    /**
     * Convenience method to get a named message from the application context
     * @param code the message code
     * @return the message value
     */
    public static String getMessage(String code) {
        return getMessage(code, null);
    }
    
    /**
     * Convenience method to get a named message from the application context with arguments
     * @param code the message code
     * @param args the message arguments
     * @return the formatted message value
     */
    public static String getMessage(String code, Object[] args) {
        return appContext.getMessage(code, args, Context.getUserContext().getLocale());
    }
}  
