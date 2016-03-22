package com.clearcapital.oss.java;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;

import com.clearcapital.oss.java.exceptions.DeserializingException;
import com.clearcapital.oss.java.exceptions.ReflectionPathException;
import com.google.common.collect.Iterables;

public class ReflectionHelpers {

    static final String MAP_KEY_OPEN = "[";
    static final String MAP_KEY_CLOSE = "]";
    static final String COLLECTION_INDEX_OPEN = "{";
    static final String COLLECTION_INDEX_CLOSE = "}";

    static Serializer defaultSerializer = null;

    // c.getDeclaredField() could fail, e.g., if the field is in a superclass.
    private static Field getDeclaredField(Class<?> c, final String name) {
        for (; c != null; c = c.getSuperclass()) {
            try {
                Field result = c.getDeclaredField(name);
                return result;
            } catch (NoSuchFieldException | SecurityException e) {
                // ignore - we'll try again on the super class.
            }
        }
        return null; // not found.
    }

    /**
     * Returns the value of a given hierarchy of fields, Map keys, and/or Collection indexes as specified in
     * {@code reflectionPath} for a given {@code sourceObject}.
     * <p/>
     * The {@code reflectionPath} is an ordered list of entries in an object containment hierarchy, where an entry can
     * be a field name, a key into a Map, or an index into a Collection. The {@code reflectionPath} is walked in order
     * to acquire a value, where the first entry is found in the {@code sourcePath} object, the second entry is found in
     * the object specified by the first entry, and so on.
     * <p/>
     * The syntax for one entry in {@code reflectionPath} is:
     * 
     * <pre>
     *      [mapKey] : use "mapKey" as key into a map.
     *      {(0-9)*} : use an integer as index into collection
     *             . : terminate and return the object specified by the prior entry, or {@code sourceObject} if no prior entry
     * anything-else : use as field name
     * </pre>
     * 
     * Furthermore, each entry in {@code reflectionPath} must be non-null and non-empty.
     * 
     * @param sourceObject
     *            the object to look start looking in
     * @param reflectionPath
     *            a hierarchical list of field names in which to walk down in order to acquire a value
     * @return an object representation of the value of the specified reflectionPath
     * @throws CoreException
     *             if any portion of the reflectionPath is invalid, not declared or not accessible
     */
    public static Object getFieldValue(final Object sourceObject, final Collection<String> reflectionPath)
            throws ReflectionPathException {
        return getFieldValue(sourceObject, reflectionPath, defaultSerializer);
    }

    /**
     * Returns the value of a given hierarchy of fields, Map keys, and/or Collection indexes as specified in
     * {@code reflectionPath} for a given {@code sourceObject}.
     * <p/>
     * The {@code reflectionPath} is an ordered list of entries in an object containment hierarchy, where an entry can
     * be a field name, a key into a Map, or an index into a Collection. The {@code reflectionPath} is walked in order
     * to acquire a value, where the first entry is found in the {@code sourcePath} object, the second entry is found in
     * the object specified by the first entry, and so on.
     * <p/>
     * The syntax for one entry in {@code reflectionPath} is:
     * 
     * <pre>
     *      [mapKey] : use "mapKey" as key into a map.
     *      {(0-9)*} : use an integer as index into collection
     *             . : terminate and return the object specified by the prior entry, or {@code sourceObject} if no prior entry
     * anything-else : use as field name
     * </pre>
     * 
     * Furthermore, each entry in {@code reflectionPath} must be non-null and non-empty.
     * 
     * @param sourceObject
     *            the object to look start looking in
     * @param reflectionPath
     *            a hierarchical list of field names in which to walk down in order to acquire a value
     * @param serializer
     * 
     * @return an object representation of the value of the specified reflectionPath
     * @throws ReflectionPathException
     *             if any portion of the reflectionPath is invalid, not declared or not accessible
     */
    public static Object getFieldValue(final Object sourceObject, final Collection<String> reflectionPath,
            Serializer serializer) throws ReflectionPathException {
        if (CollectionUtils.isEmpty(reflectionPath)) {
            return null;
        }

        if (reflectionPath.contains(null)) {
            throw new ReflectionPathException("null entry in reflectionPath is not supported");
        }

        Object fieldValue = sourceObject;
        for (String pathEntry : reflectionPath) {
            if (pathEntry.equals(".")) {
                // NOTE: a dot before the end skips any remaining entries
                break;
            }
            if (fieldValue == null) {
                break;
            }
            if (isMapKey(pathEntry)) {
                String keyString = getMapKey(pathEntry);
                Map<?, ?> map = (Map<?, ?>) fieldValue;

                if (map.isEmpty()) {
                    throw new ReflectionPathException("reflectionPath entry " + pathEntry + " does not exist");
                }

                Map.Entry<?, ?> mapEntry = map.entrySet().iterator().next();
                Class<?> entryClass = mapEntry.getKey().getClass();

                Object key = null;
                if (mapEntry.getKey() instanceof String) {
                    key = keyString;
                } else if (serializer != null) {
                    if (mapEntry.getKey() instanceof Enum) {
                        keyString = "\"" + keyString + "\"";
                    }
                    try {
                        key = serializer.getObject(keyString, entryClass);
                    } catch (DeserializingException e) {
                        throw new ReflectionPathException("reflectionPath entry " + pathEntry
                                + " could not be deserialized", e);
                    }
                } else {
                    throw new ReflectionPathException("reflectionPath entry " + pathEntry
                            + " could not be deserialized");
                }

                if (!map.containsKey(key)) {
                    throw new ReflectionPathException("reflectionPath entry " + pathEntry + " does not exist");
                }
                fieldValue = map.get(key);

            } else if (isCollectionIndex(pathEntry)) {
                try {
                    int index = getCollectionIndex(pathEntry);
                    Collection<?> collection = (Collection<?>) fieldValue;
                    fieldValue = Iterables.get(collection, index);
                } catch (NumberFormatException e) {
                    throw new ReflectionPathException("reflectionPath entry '" + pathEntry
                            + "' refers to a collection index but improperly formed", e);
                } catch (IndexOutOfBoundsException e) {
                    throw new ReflectionPathException("reflectionPath entry '" + pathEntry
                            + "' refers to a collection index beyond the collection size", e);
                } catch (ClassCastException e) {
                    throw new ReflectionPathException("reflectionPath entry '" + pathEntry
                            + "' refers to a collection index but prior object is not a Collection", e);
                }
            } else {
                fieldValue = getFieldValue(fieldValue, pathEntry);
            }
        }
        return fieldValue;
    }

    /**
     * Returns true if {@code s} is a string wrapped by square braces, false otherwise. E.g, "[foo]" and "[]" return
     * true, while "foo" returns false.
     * 
     * @param s
     *            a String to check for map key syntax
     */
    static boolean isMapKey(final String s) {
        return s != null && s.length() >= (MAP_KEY_OPEN.length() + MAP_KEY_CLOSE.length())
                && s.startsWith(MAP_KEY_OPEN) && s.endsWith(MAP_KEY_CLOSE);
    }

    /**
     * Extracts and returns a map key from a String with valid map key syntax.
     * 
     * @param s
     *            a String containing valid Map key syntax
     * @throws IllegalArgumentException
     *             if {@code s} is not of valid map key format
     * @see #isMapKey(String)
     */
    static String getMapKey(final String s) throws IllegalArgumentException {
        // TODO: May have to handle conversion of pathEntry into an enum for Map<SomeEnum,...>), and return Object
        if (isMapKey(s)) {
            return s.substring(MAP_KEY_OPEN.length(), s.length() - MAP_KEY_CLOSE.length());
        }
        throw new IllegalArgumentException("Invalid map key format for '" + s + "'.");
    }

    /**
     * Returns true if {@code s} contains a positive decimal integer wrapped by curly braces, false otherwise. E.g.,
     * "{42}" returns true, while "42", "{-1}" and "{foo}" return false.
     * 
     * @param s
     *            a String to check for collection key syntax
     */
    static boolean isCollectionIndex(final String s) {
        if (s == null || s.length() <= (COLLECTION_INDEX_OPEN.length() + COLLECTION_INDEX_CLOSE.length())
                || !s.startsWith(COLLECTION_INDEX_OPEN) || !s.endsWith(COLLECTION_INDEX_CLOSE)) {
            return false;
        }
        try {
            int i = Integer.parseInt(s.substring(COLLECTION_INDEX_OPEN.length(),
                    s.length() - COLLECTION_INDEX_CLOSE.length()));
            return i >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Extracts and returns a collection index from a String with valid collection index syntax.
     * 
     * @param s
     *            a string containing valid collection index syntax
     * @see #isCollectionIndex(String)
     */
    static int getCollectionIndex(final String s) throws NumberFormatException {
        if (isCollectionIndex(s)) {
            return Integer.parseInt(s.substring(COLLECTION_INDEX_OPEN.length(),
                    s.length() - COLLECTION_INDEX_CLOSE.length()));
        }
        throw new NumberFormatException("Invalid collection index '" + s + "'.");
    }

    /**
     * Returns the value of a given {@code fieldName} of a given {@code object}.
     * 
     * @param object
     *            the object to look in
     * @param fieldName
     *            the declared name of the field in {@code object}
     * @return an object representation of the value of {@code fieldName}
     * @throws CoreException
     *             if {@code fieldName} is not declared or is not accessible
     */
    public static Object getFieldValue(final Object object, final String fieldName) throws ReflectionPathException {
        Field field = getDeclaredField(object.getClass(), fieldName);
        if (null == field) {
            throw new ReflectionPathException("field " + fieldName + " does not exist", new NoSuchFieldException());
        }
        try {
            boolean isAccessible = field.isAccessible();
            field.setAccessible(true);
            Object value = field.get(object);
            field.setAccessible(isAccessible);
            return value;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new ReflectionPathException("field " + fieldName + " is not accessible", e);
        }
    }

    public static Object setFieldValue(final Object object, final String name, final Object value)
            throws ReflectionPathException {
        try {
            Field field = getDeclaredField(object.getClass(), name);
            if (null == field) {
                throw new NoSuchFieldException();
            }
            boolean isAccessible = field.isAccessible();
            field.setAccessible(true);
            field.set(object, value);
            field.setAccessible(isAccessible);
            return value;
        } catch (NoSuchFieldException e) {
            throw new ReflectionPathException("Field \"" + name + "\" not found in object", e);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new ReflectionPathException("Could not set field \"" + name + "\" in object", e);
        }
    }

	public static Reflections getReflections(final Collection<String> packageNames) {
		Reflections reflections = null;
		for (String packageName : packageNames) {
            Reflections packageReflections = getReflections(packageName);
			if (reflections == null) {
				reflections = packageReflections;
			} else {
				reflections.merge(packageReflections);
			}
		}
		return reflections;
	}

    public static Reflections getReflections(final String packageName) {
        Reflections packageReflections = new Reflections(ClasspathHelper.forPackage(packageName),
                new TypeAnnotationsScanner(), new SubTypesScanner());
        return packageReflections;
    }

    public static Reflections getReflections(final Package[] packages) {
        Reflections reflections = null;
        for (Package item : packages) {
            Reflections packageReflections = new Reflections(ClasspathHelper.forPackage(item.getName()),
                    new TypeAnnotationsScanner(), new SubTypesScanner());
            if (reflections == null) {
                reflections = packageReflections;
            } else {
                reflections.merge(packageReflections);
            }
        }
        return reflections;
    }

    public static Set<Class<?>> getTypesAnnotatedWith(String packageName,
            final Class<? extends Annotation> annotation) {
        Reflections reflections = getReflections(packageName);
        return reflections.getTypesAnnotatedWith(annotation);
    }

	public static Set<Class<?>> getTypesAnnotatedWith(Collection<String> packageNames, Class<? extends Annotation> annotation) {
        Reflections reflections = getReflections(packageNames);
        return reflections.getTypesAnnotatedWith(annotation);
	}

}