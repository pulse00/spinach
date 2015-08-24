package biz.paluch.spinach.management;

import com.google.common.base.Splitter;

import java.util.Map;

import biz.paluch.spinach.api.DisqueConnection;

public class DisqueManagement<K, V> implements DisqueManagementMBean {

    public static final String MBeanObjectName = "biz.paluch.spinach.disque:name=DisqueManagementMBean";

    private final DisqueConnection connection;

    public DisqueManagement(DisqueConnection<K, V> client) {
        this.connection = client;
    }

    @Override
    public Long getTotalConnectionsReceived() {
        return Long.valueOf(getSystemInfo("total_connections_received"));
    }

    protected String getSystemInfo(String key) {

        // run the info command
        String info = connection.sync().info();

        // remove the comments beginning with #
        info = info.replaceAll("(?m)^#.*", "");

        Map<String, String> split = Splitter
            .on(System.getProperty("line.separator"))
            .trimResults()
            .omitEmptyStrings()
            .withKeyValueSeparator(':')
            .split(info.toString());

        return split.get(key);

    }
}
