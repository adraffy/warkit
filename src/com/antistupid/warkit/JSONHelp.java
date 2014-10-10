package com.antistupid.warkit;

import org.json.simple.JSONObject;

public class JSONHelp {
    
    static class JSONError extends RuntimeException {
        JSONError(String msg) {
            super(msg);
        }
    }
    
    static public int requireInt(JSONObject obj, String key) { return requireNum(obj, key).intValue(); }
    static public Number requireNum(JSONObject obj, String key) { return require(obj, key, Number.class); }
    
    static public String requireStr(JSONObject obj, String key) { return require(obj, key, String.class); }
    static public <T> T require(JSONObject obj, String key, Class<T> cls) {
        Object value = obj.get(key);
        if (value == null) {
            throw new JSONError(String.format("Missing \"%s\": Expect(%s)", key, cls.getSimpleName()));
        }
        if (!(cls.isInstance(value))) {
            throw new JSONError(String.format("Wrong \"%s\" Type: Expect(%s) Got(%s)", key, cls.getSimpleName(), value.getClass().getSimpleName()));
        }
        return cls.cast(value);
    }
    
    static public <T> T get(JSONObject obj, String key, Class<T> cls) {
        Object value = obj.get(key);
        if (value == null) {
            return null;
        }
        if (!(cls.isInstance(value))) {
            throw new JSONError(String.format("Wrong \"%s\" Type: Expect(%s) Got(%s)", key, cls.getSimpleName(), value.getClass().getSimpleName()));
        }
        return cls.cast(value);
    }
    
    static public int getInt(JSONObject obj, String key, int fill) {
        Number value = get(obj, key, Number.class);        
        return value != null ? value.intValue() : fill;
    }
    
    
}
