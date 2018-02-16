package org.launchcode.projectmanager;

import java.util.HashMap;

public class CustomSession {

    static HashMap<String, Object> attributes = new HashMap<>();

    public static void addAttribute(String key, Object value) {
        attributes.put(key,value);
    }

    public static Object getAttribute(String key) {
        return attributes.get(key);
    }

    public static Object getOrDeafultAttribute(String key, Object theDefault) {
        return attributes.getOrDefault(key, theDefault);
    }

}
