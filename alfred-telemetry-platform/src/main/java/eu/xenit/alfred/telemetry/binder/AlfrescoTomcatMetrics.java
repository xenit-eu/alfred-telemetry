package eu.xenit.alfred.telemetry.binder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.tomcat.TomcatMetrics;
import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.catalina.Manager;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.StandardContext;
import org.springframework.web.context.ServletContextAware;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class AlfrescoTomcatMetrics implements NamedMeterBinder, ServletContextAware {


    private ServletContext servletContext;
    private Manager manager;

    @Override
    public void bindTo(@Nonnull MeterRegistry registry) {
        new TomcatMetrics(getManager(), new ArrayList<>()).bindTo(registry);
    }

    @Override
    public String getName() {
        return "tomcat";
    }

    private Manager getManager() {
        if (manager == null) {
            try {
                Field applicationContextField = servletContext.getClass().getDeclaredField("context");
                applicationContextField.setAccessible(true);
                ApplicationContext appContextObj = (ApplicationContext) applicationContextField.get(servletContext);
                Field standardContextField = appContextObj.getClass().getDeclaredField("context");
                standardContextField.setAccessible(true);
                StandardContext standardContextObj = (StandardContext) standardContextField.get(appContextObj);
                manager = standardContextObj.getManager();
            } catch (ReflectiveOperationException e) {
                throw new AlfrescoRuntimeException(e.getMessage());
            }
        }
        return manager;
    }

    public void setServletContext(ServletContext context) {
        this.servletContext = context;
    }
}
