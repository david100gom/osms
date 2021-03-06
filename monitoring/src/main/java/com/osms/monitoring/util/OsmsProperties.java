package com.osms.monitoring.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsmsProperties {

    private static final Logger logger = LoggerFactory.getLogger(OsmsProperties.class);
    public static final String Cons_value = "value";
    public static final String Cons_tag = "tag";
    public static final String Cons_desc = "desc";
    public static final String Cons_title = "title";
    public static final String Cons_timestamp = "_timestamp";

    private static final int _DEFAULT_PERIOD = 10000;
    private static Properties props;
    private static boolean loaded;
    private static String concatenator;
    private static String prefix;
    private static Map<String, Map<String, Object>> deviceMeta = new HashMap<String, Map<String, Object>>();

    public static synchronized Properties loadPangProperties() throws IOException {
        if(props != null) {
            return props;
        }
        loaded = true;
        logger.info("Loading osms.properties in your classpath");
        InputStream is = OsmsUtils.class.getResourceAsStream("/conf/osms.properties");
        if(is == null) {
            throw new IOException("Could not load the file osms.properites in your classpath");
        }
        props = new Properties();
        props.load(is);
        is.close();

        concatenator = props.getProperty("osms.concatenator");
        if(concatenator == null) {
            concatenator = "-";
        } else if(concatenator.trim().equalsIgnoreCase("underscore")){
            concatenator = "_";
        } else {
            concatenator = "-";
        }
        logger.debug("osms.concatenator: '{}'", concatenator);

        prefix = props.getProperty("osms.prefix");
        if(prefix != null) {
            prefix = prefix.trim();
        }
        logger.debug("osms.prefix: {}.", prefix);

        return props;
    }

    public static String getConcatenator() {
        checkNull();
        return concatenator;
    }

    public static String getPrefix() {
        checkNull();
        return prefix;
    }

    public static void setProperties(Properties props) {
        OsmsProperties.props = props;
    }

    public static Properties getProperties() {
        checkNull();
        return props;
    }

    public static boolean isEnabledToSend() {
        String enabled = (String) get("osms.enabled");
        if(enabled != null && enabled.equalsIgnoreCase("false")) {
            return false;
        }
        return true;
    }

    private static String get(String key) {
        checkNull();
        return props.getProperty(key);
    }

    /**
     * Time unit is seconds.
     * @return period of schedule
     */
    public static long getPeriod() {
        String period = (String) get("osms.period");
        if(period == null) {
            return _DEFAULT_PERIOD; // ten seconds
        }
        long lPeriod = Long.valueOf(period.trim());
        if(lPeriod <= 0) {
            return _DEFAULT_PERIOD; // ten seconds
        }
        return lPeriod * 1000;
    }

    private static void checkNull() {
        if(!loaded) {
            try {
                loadPangProperties();
            } catch (IOException e) {
                logger.error("error", e);
            }
        }
        if(props == null) {
            throw new IllegalStateException("Properties object must not be null");
        }
    }

    public static Object getProperty(String key) {
        checkNull();
        return props.get(key);
    }

    public static Map<Integer, Map<String, String>> extractPrefixedMultipleProperties(String prefix) {
        checkNull();
        Set<Entry<Object, Object>> entrySet = props.entrySet();
        Iterator<Entry<Object, Object>> iterator = entrySet.iterator();

        Map<Integer, Map<String, String>> prefxiedProperties = new HashMap<Integer, Map<String, String>>();

        while (iterator.hasNext()) {
            Entry<Object, Object> next = iterator.next();
            String key = (String) next.getKey();
            if (key.startsWith(prefix)) {
                String sub1 = key.substring(key.indexOf(".") + 1);
                int indexOf = sub1.indexOf(".");
                String number = sub1.substring(0, indexOf);
                Map<String, String> property = prefxiedProperties.get(Integer.valueOf(number));
                if (property == null) {
                    property = new HashMap<String, String>();
                    prefxiedProperties.put(Integer.valueOf(number), property);
                }

                property.put(sub1.substring(sub1.lastIndexOf(".") + 1).trim(), (String) next.getValue());
            }
        }

        return prefxiedProperties;
    }

    public static List<String> extractPrefixedKey(String prefix) {
        checkNull();
        Set<Entry<Object, Object>> entrySet = props.entrySet();
        Iterator<Entry<Object, Object>> iterator = entrySet.iterator();

        List<String> properties = new ArrayList<String>();

        while (iterator.hasNext()) {
            Entry<Object, Object> next = iterator.next();
            String key = (String) next.getKey();
            if (key.startsWith(prefix)) {
                String sub1 = key.substring(key.indexOf(".") + 1);

                properties.add(sub1);
            }
        }
        return properties;
    }

    public static Map<String, Object> extractPrefixedProperties(String prefix) {
        checkNull();
        Set<Entry<Object, Object>> entrySet = props.entrySet();
        Iterator<Entry<Object, Object>> iterator = entrySet.iterator();

        Map<String, Object> properties = new HashMap<String, Object>();

        while (iterator.hasNext()) {
            Entry<Object, Object> next = iterator.next();
            String key = (String) next.getKey();
            if (key.startsWith(prefix)) {
                properties.put(key, next.getValue());
            }
        }
        return properties;
    }

    public static Map<Integer, Map<String, String>> extractVariableProperties(String prefix) {
        Properties properties = OsmsProperties.getProperties();
        Set<Entry<Object, Object>> entrySet = properties.entrySet();
        Iterator<Entry<Object, Object>> iterator = entrySet.iterator();

        Map<Integer, Map<String, String>> targets = new HashMap<Integer, Map<String, String>>();

        while (iterator.hasNext()) {
            Entry<Object, Object> next = iterator.next();
            String key = (String) next.getKey();
            if (key.startsWith(prefix)) {
                String sub1 = key.substring(key.indexOf(".") + 1);
                int indexOf = sub1.indexOf(".");
                String number = sub1.substring(0, indexOf);
                Map<String, String> target = targets.get(Integer.valueOf(number));
                if (target == null) {
                    target = new HashMap<String, String>();
                    targets.put(Integer.valueOf(number), target);
                }

                target.put(sub1.substring(sub1.lastIndexOf(".") + 1).trim(), (String) next.getValue());
            }
        }

        return targets;
    }

    public static Map<Integer, Map<String, String>> extractVariablePropertiesSubstringByPrefix(String prefix, String exclude) {
        Properties properties = OsmsProperties.getProperties();
        Set<Entry<Object, Object>> entrySet = properties.entrySet();
        Iterator<Entry<Object, Object>> iterator = entrySet.iterator();

        Map<Integer, Map<String, String>> targets = new HashMap<Integer, Map<String, String>>();

        while (iterator.hasNext()) {
            Entry<Object, Object> next = iterator.next();
            String key = (String) next.getKey();
            if (key.startsWith(prefix)) {
                String sub1 = key.substring(prefix.length());
                int indexOf = sub1.indexOf(".");
                String number = sub1.substring(0, indexOf);
                if(exclude != null && key.startsWith(prefix+number+"."+exclude)) {
                    continue;
                }
                Map<String, String> target = targets.get(Integer.valueOf(number));
                if (target == null) {
                    target = new HashMap<String, String>();
                    targets.put(Integer.valueOf(number), target);
                }

                target.put(sub1.substring(sub1.lastIndexOf(".") + 1).trim(), (String) next.getValue());
            }
        }

        return targets;
    }

    public static Set<String> getList(Object object) {
        return getList(object, false);
    }

    public static Set<String> getList(Object object, boolean toLower) {
        Set<String> partitions = new HashSet<String>();
        if (object == null) {
            return null;
        } else if (object instanceof Map) {
            Map<String, Object> keyMap = (Map<String, Object>) object;
            Set<String> keySet = keyMap.keySet();
            for (String key : keySet) {
                if(toLower) {
                    partitions.add(((String) keyMap.get(key)).toLowerCase());
                } else {
                    partitions.add((String) keyMap.get(key));
                }
            }
        } else if (object instanceof String) {
            String[] split = ((String) object).split(",");
            for (String k : split) {
                if(toLower) {
                    partitions.add(k.trim().toLowerCase());
                } else {
                    partitions.add(k.trim());
                }
            }
        } else {
            return null;
        }
        return partitions;
    }

    public static Map<String, Object> getDeviceMeta(String devicename) {
        return deviceMeta.get(devicename);
    }

    public static void setDeviceMeta(String devicename, Map<String, Object> d) {
        deviceMeta.put(devicename, d);
    }
}
