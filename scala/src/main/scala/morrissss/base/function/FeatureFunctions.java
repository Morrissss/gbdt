package morrissss.base.function;

import morrissss.base.feature.FeatureKey;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class FeatureFunctions {

    public static FeatureFunctions instance() {
        if (INSTANCE == null) {
            synchronized (FeatureFunctions.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FeatureFunctions();
                }
            }
        }
        return INSTANCE;
    }
    private static FeatureFunctions INSTANCE;
    private FeatureFunctions() {
        Reflections reflections = new Reflections("com.red.algo");
        Map<String, Class<? extends FeatureFunction>> functionKeys = new HashMap<>();
        for (Class<? extends FeatureFunction> function : reflections.getSubTypesOf(FeatureFunction.class)) {
            if (concreteClass(function)) {
                try {
                    Field field = function.getDeclaredField("FUNC_NAME");
                    if (!publicStaticFinalField(field)) {
                        throw new AssertionError("No legal FUNC_NAME field for key " + function);
                    }
                    String name = (String) field.get(null);
                    if (!name.matches("[A-Z]+")) {
                        throw new AssertionError("Feature function name should match [A-Z]+");
                    }
                    if (functionKeys.containsKey(name)) {
                        throw new AssertionError("Duplicate feature function name " + name);
                    }
                    function.getConstructor(String[].class, String[].class);
                    functionKeys.put(name, function);
                } catch (NoSuchMethodException e) {
                    throw new AssertionError("No constructor of (String[], String[]) for key " + function, e);
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
        }
        FUNCTION_KEYS = Collections.unmodifiableMap(functionKeys);
    }

    private final Map<String, Class<? extends FeatureFunction>> FUNCTION_KEYS;

    public FeatureFunction function(FeatureKey featureKey) throws ReflectiveOperationException {
        String parts[] = StringUtils.splitByWholeSeparatorPreserveAllTokens(featureKey.functionKey, "|");
        String key = parts[0];
        String[] params = ArrayUtils.subarray(parts, 1, parts.length);
        // getConstructor() only get accessible constructor
        Constructor<? extends FeatureFunction> constructor =
                FUNCTION_KEYS.get(key).getDeclaredConstructor(String[].class, String[].class);
        // already checked for null during initializing
        constructor.setAccessible(true);
        return constructor.newInstance(featureKey.columns, params);
    }

    private boolean concreteClass(Class<?> clazz) {
        int modifier = clazz.getModifiers();
        return !Modifier.isInterface(modifier) && !Modifier.isAbstract(modifier);
    }

    private boolean publicStaticFinalField(Field field) {
        int modifier = field.getModifiers();
        return Modifier.isFinal(modifier) && Modifier.isPublic(modifier) && Modifier.isStatic(modifier);
    }

}
