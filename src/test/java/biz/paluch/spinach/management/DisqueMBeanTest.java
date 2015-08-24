package biz.paluch.spinach.management;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import biz.paluch.spinach.commands.AbstractCommandTest;

public class DisqueMBeanTest extends AbstractCommandTest {

    private JMXConnector cc;
    private JMXConnectorServer cs;
    private MBeanServerConnection serverConnection;

    @Before
    public void openConnection() throws Exception {

        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://");
        cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mBeanServer);
        cs.start();

        JMXServiceURL addr = cs.getAddress();
        cc = JMXConnectorFactory.connect(addr);

        serverConnection = cc.getMBeanServerConnection();

        client.enableJmx();
        disque = client.connect().sync();
        disque.debugFlushall();
    }

    @After
    public void closeMBeanServer() {

        try {
            if (cc != null) {
                cc.close();
            }

            cs.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testManagementBean() throws Exception {
        ObjectName objectName = new ObjectName(DisqueManagement.MBeanObjectName);
        Long totalConnections = (Long) serverConnection.getAttribute(objectName, "TotalConnectionsReceived");
        Assert.assertTrue(totalConnections >= 0);
    }
}
