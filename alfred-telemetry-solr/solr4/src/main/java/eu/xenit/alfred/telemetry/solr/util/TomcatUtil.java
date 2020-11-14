package eu.xenit.alfred.telemetry.solr.util;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardManager;

public class TomcatUtil {
    public static Manager getTomcatManagerFromMBeanServer(MBeanServer mBeanServer) {
        ObjectName objectName = null;
        try {
            objectName = new ObjectName("Catalina:type=Manager,context=/solr4,host=localhost");
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
        ObjectInstance objectInstance = null;
        try {
            objectInstance = mBeanServer.getObjectInstance(objectName);
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }
        if(objectInstance instanceof Manager)
            return (Manager)objectInstance;

        return null;
    }
}
