package one.terenin.rpc.spec;

import java.util.Map;

// will be used for generate flat buff spec in runtime
// generate spec -> push to flat buff spec gen -> generate classes -> compile classes.
public class EntitySpec {

    private final Map<String, Type> paramsMap;

    public EntitySpec(Map<String, Type> paramsMap) {
        this.paramsMap = paramsMap;
    }

    public EntitySpec withParam(String paramName, Type type) {
        this.paramsMap.put(paramName, type);
        return this;
    }

    public enum Type {
        BYTE,
        INT_2, INT_4, INT_8, INT_12, // integer types
        FIXED_BYTE_ARRAY, ENUM,
        STRING, BYTE_ARRAY
    }

}
