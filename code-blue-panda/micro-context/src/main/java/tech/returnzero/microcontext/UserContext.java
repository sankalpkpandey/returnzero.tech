package tech.returnzero.microcontext;

import java.util.Map;

public class UserContext {
    private static final ThreadLocal<Map<String, Object>> USERCONTEXT = new ThreadLocal<>();

    public static final void set(Map<String, Object> user) {
        USERCONTEXT.set(user);
    }

    public static final Object get(String property) {
        return USERCONTEXT.get().get(property);
    }
}
