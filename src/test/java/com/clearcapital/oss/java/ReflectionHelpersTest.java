package com.clearcapital.oss.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.clearcapital.oss.java.exceptions.DeserializingException;
import com.clearcapital.oss.java.exceptions.ReflectionPathException;

public class ReflectionHelpersTest {

    enum KeyEnum {
        KEY0,
        KEY1,
        KEY2,
        KEY3,
        KEY4
    }

    static class Hierarchical {

        final String field;
        final String nullField = null;
        final StringWrapper wrappedField;
        final Map<String, String> map;
        final List<String> list;
        final Map<String, Map<String, String>> mapMap;
        final List<List<String>> listList;
        final List<Map<String, String>> listMap;
        final Map<String, List<String>> mapList;
        final Map<String, List<Map<String, String>>> mapListMap;
        final Map<KeyEnum, String> enumMap;
        final Map<Integer, String> integerMap;
        final Map<Long, String> longMap;

        public Hierarchical() {
            // field
            field = "value";
            // nestedField
            wrappedField = new StringWrapper("wrappedValue");
            // map
            map = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                map.put("key" + i, "value" + i);
            }
            map.put("nullValue", null);
            map.put(null, "nonNullValue");
            map.put("", "emptyKey");
            // list
            list = Arrays.asList("value0", "value1", "value2", "value3", "value4");
            // mapMap
            mapMap = new HashMap<>();
            mapMap.put("key0", map);
            // listList
            listList = new ArrayList<>();
            listList.add(list);
            // listMap
            listMap = new ArrayList<>();
            listMap.add(map);
            // mapList
            mapList = new HashMap<>();
            mapList.put("key0", list);
            // mapListMap
            mapListMap = new HashMap<>();
            mapListMap.put("key0", listMap);
            // enumMap
            enumMap = new HashMap<>();
            for (KeyEnum e : KeyEnum.values()) {
                enumMap.put(e, "value" + e.ordinal());
            }
            // Integer map
            integerMap = new HashMap<>();
            for (int i = 0; i < 10; i++) {
                integerMap.put(i, "Number " + String.valueOf(i));
            }
            // Long map
            longMap = new HashMap<>();
            for (long i = 0; i < 10; i++) {
                longMap.put(i, "Number " + String.valueOf(i));
            }
        }
    }

    static class StringWrapper {

        String field;

        public StringWrapper(final String s) {
            field = s;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            StringWrapper that = (StringWrapper) o;

            return !(field != null ? !field.equals(that.field) : that.field != null);

        }

        @Override
        public int hashCode() {
            return field != null ? field.hashCode() : 0;
        }
    }

    private static final Hierarchical sourceObject = new Hierarchical();

    @Test
    public void testIsMapKey() {
        // positive cases
        String[] args = new String[] { "[foo]", "[1]", "[ ]", "[]" };
        for (String arg : args) {
            assertTrue(arg + " should be a valid map key", ReflectionHelpers.isMapKey(arg));
        }

        // negative cases
        args = new String[] { "", null, "{}", "{foo}", "[foo" };
        for (String arg : args) {
            assertFalse(arg + " should not be a valid map key", ReflectionHelpers.isMapKey(arg));
        }
    }

    @Before
    public void beforeTest() throws DeserializingException {
        Serializer mockSerializer = mock(Serializer.class);

        // doReturn(7).when(mockSerializer).getObject(eq("7"), eq(Integer.class));
        // doReturn(7).when(mockSerializer).getObject(anyString(), eq(Integer.class));
        when(mockSerializer.getObject(eq("7"), eq(Integer.class))).thenReturn(7);
        when(mockSerializer.getObject(eq("7"), eq(Long.class))).thenReturn(7L);
        when(mockSerializer.getObject(eq("\"KEY0\""), eq(KeyEnum.class))).thenReturn(KeyEnum.KEY0);

        ReflectionHelpers.defaultSerializer = mockSerializer;
    }

    @Test
    public void testGetMapKey() {
        // positive cases
        assertEquals(ReflectionHelpers.getMapKey("[foo]"), "foo");
        assertEquals(ReflectionHelpers.getMapKey("[1]"), "1");
        assertEquals(ReflectionHelpers.getMapKey("[ ]"), " ");
        assertEquals(ReflectionHelpers.getMapKey("[]"), "");

        // negative cases
        String[] args = new String[] { "", null, "{}", "{foo}", "[foo" };
        for (String arg : args) {
            try {
                ReflectionHelpers.getMapKey(arg);
                fail(arg + " should not be valid");
            } catch (IllegalArgumentException e) {
                // expected
            }
        }
    }

    @Test
    public void testIsCollectionIndex() {
        // positive cases
        String[] args = new String[] { "{1}", "{58}" };
        for (String arg : args) {
            assertTrue(arg + " should be a valid collection index", ReflectionHelpers.isCollectionIndex(arg));
        }

        // negative cases
        args = new String[] { "", null, "[]", "{foo}", "{1", "{}", "{0xAF2}" };
        for (String arg : args) {
            assertFalse(arg + " should not be a valid collection index", ReflectionHelpers.isCollectionIndex(arg));
        }
    }

    @Test
    public void testGetCollectionIndex() {
        // positive cases
        assertEquals(1, ReflectionHelpers.getCollectionIndex("{1}"));
        assertEquals(58, ReflectionHelpers.getCollectionIndex("{58}"));

        // negative cases
        String[] args = new String[] { "", null, "[]", "{foo}", "{1", "{}", "{0xAF2}" };
        for (String arg : args) {
            try {
                ReflectionHelpers.getCollectionIndex(arg);
                fail(arg + " should not be valid");
            } catch (NumberFormatException e) {
                // expected
            }
        }
    }

    @Test
    public void testGetFieldValue() throws Exception {
        Object actual = ReflectionHelpers.getFieldValue(sourceObject, "field");
        assertNotNull(actual);
        assertEquals("value", actual);
    }

    @Test(expected = ReflectionPathException.class)
    public void testGetFieldValueEmpty() throws Exception {
        ReflectionHelpers.getFieldValue(sourceObject, "");
    }

    @Test(expected = ReflectionPathException.class)
    public void testGetFieldValueNotFound() throws Exception {
        ReflectionHelpers.getFieldValue(sourceObject, "bogusField");
    }

    @Test
    public void testGetFieldValuePathNull() throws Exception {
        // field exists and has a null value
        Object actual = ReflectionHelpers.getFieldValue(sourceObject, Collections.singletonList("nullField"));
        assertNull(actual);
    }

    @Test
    public void testGetFieldValuePathDot() throws Exception {
        // ".", aka, this object
        StringWrapper sourceObject = new StringWrapper("wrappedValue");
        Object actual = ReflectionHelpers.getFieldValue(sourceObject, Collections.singletonList("."));
        assertNotNull(actual);
        assertEquals(sourceObject, actual);
    }

    @Test
    public void testGetFieldValuePathTrailingDot() throws Exception {
        // "." after other path element(s)
        Object actual = ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("field", "."));
        assertNotNull(actual);
        assertEquals("value", actual);
    }

    @Test
    public void testGetFieldValuePathNestedDot() throws Exception {
        // "." in the middle of the path
        Object actual = ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("wrappedField", ".", "field"));
        assertNotNull(actual);
        assertEquals(sourceObject.wrappedField, actual);
    }

    @Test
    public void testGetFieldValuePathSingle() throws Exception {
        // single level javaPath
        Object actual = ReflectionHelpers.getFieldValue(sourceObject, Collections.singletonList("field"));
        assertNotNull(actual);
        assertEquals("value", actual);
    }

    @Test
    public void testGetFieldValuePathDeeper() throws Exception {
        // two level javaPath
        Object actual = ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("wrappedField", "field"));
        assertNotNull(actual);
        assertEquals("wrappedValue", actual);
    }

    @Test
    public void testGetFieldValuePathMap() throws Exception {
        // Map<String, String>
        Object actual = ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("map", "[key3]"));
        assertNotNull(actual);
        assertEquals("value3", actual);
    }

    @Test(expected = ReflectionPathException.class)
    public void testGetFieldValuePathMapKeyNotFound() throws Exception {
        // Map<String, String> with key not found
        ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("map", "[bogusKey]"));
    }

    @Test
    public void testGetFieldValuePathMapNullValue() throws Exception {
        // Map<String, String> with valid key but null value
        Object actual = ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("map", "[nullValue]"));
        assertNull(actual);
    }

    @Test(expected = ReflectionPathException.class)
    public void testGetFieldValuePathMapNullKey() throws Exception {
        // Map<String, String> with null key and non-null value
        ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("map", null));
    }

    @Test
    public void testGetFieldValuePathMapEmptyKey() throws Exception {
        // Map<String, String> with empty-string key and valid value
        Object actual = ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("map", "[]"));
        assertNotNull(actual);
        assertEquals("emptyKey", actual);
    }

    @Test
    public void testGetFieldValuePathList() throws Exception {
        // List<String>
        Object actual = ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("list", "{2}"));
        assertNotNull(actual);
        assertEquals("value2", actual);
    }

    @Test(expected = ReflectionPathException.class)
    public void testGetFieldValuePathListNonNumericIndex() throws Exception {
        // List<String> list with non-number index (expect CoreException)
        ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("list", "{two}"));
    }

    @Test(expected = ReflectionPathException.class)
    public void testGetFieldValuePathListEmptyIndex() throws Exception {
        // List<String> list with empty index (expect CoreException)
        ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("list", ""));
    }

    @Test(expected = ReflectionPathException.class)
    public void testGetFieldValuePathListIndexOutOfBounds() throws Exception {
        // List<String> list with index out of bounds (expect CoreException)
        ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("list", "{42}"));
    }

    @Test
    public void testGetFieldValuePathListSelf() throws Exception {
        // List<String> with no index (expect the actual list object returned)
        Object actual = ReflectionHelpers.getFieldValue(sourceObject, Collections.singletonList("list"));
        assertNotNull(actual);
        assertEquals(sourceObject.list, actual);
    }

    @Test
    public void testGetFieldValuePathNestedMap() throws Exception {
        // Map<String, Map<String,String>>
        Object actual = ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("mapMap", "[key0]", "[key3]"));
        assertNotNull(actual);
        assertEquals("value3", actual);
    }

    @Test
    public void testGetFieldValuePathNestedMapMap() throws Exception {
        // Map<String, Map<String,String>> with value of inner map
        Object actual = ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("mapMap", "[key0]"));
        assertNotNull(actual);
        assertEquals(sourceObject.map, actual);
    }

    @Test(expected = ReflectionPathException.class)
    public void testGetFieldValuePathNestedMapBogusKey() throws Exception {
        // Map<String, Map<String,String>> bogus key in outer map
        ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("mapMap", "[bogusKey]"));
    }

    @Test(expected = ReflectionPathException.class)
    public void testGetFieldValuePathNestedMapBogusNestedKey() throws Exception {
        // Map<String, Map<String,String>> bogus key in inner map
        ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("mapMap", "[key0]", "[bogusKey]"));
    }

    @Test
    public void testGetFieldValuePathNestedList() throws Exception {
        // List<List<String>>
        Object actual = ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("listList", "{0}", "{2}"));
        assertNotNull(actual);
        assertEquals("value2", actual);
    }

    @Test
    public void testGetFieldValuePathNestedListOfMap() throws Exception {
        // List<Map<String,String>>
        Object actual = ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("listMap", "{0}", "[key2]"));
        assertNotNull(actual);
        assertEquals("value2", actual);
    }

    @Test
    public void testGetFieldValuePathNestedMapOfList() throws Exception {
        // Map<String, List<String>>
        Object actual = ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("mapList", "[key0]", "{2}"));
        assertNotNull(actual);
        assertEquals("value2", actual);
    }

    @Test
    public void testGetFieldValuePathNestedMapOfListOfMap() throws Exception {
        // Map<String, List<Map<String,String>>>
        Object actual = ReflectionHelpers.getFieldValue(sourceObject,
                Arrays.asList("mapListMap", "[key0]", "{0}", "[key2]"));
        assertNotNull(actual);
        assertEquals("value2", actual);
    }

    @Test
    public void testGetFieldValuePathEnumMap() throws Exception {
        // Map<KeyEnum, String>
        Object actual = ReflectionHelpers.getFieldValue(sourceObject,
                Arrays.asList("enumMap", "[" + KeyEnum.KEY0.name() + "]"));
        assertNotNull(actual);
        assertEquals("value0", actual);
    }

    @Test
    public void testGetFieldValuePathMapField() throws Exception {
        // difference between map field (e.g., map.size) and map content (e.g., key/value traversal)
        Object actual = ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("map", "size"));
        assertNotNull(actual);
        assertEquals(sourceObject.map.size(), actual);
    }

    @Test(expected = ReflectionPathException.class)
    public void testGetFieldValuePathNotFound() throws Exception {
        ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("wrappedField", "bogusField"));
    }

    @Test
    public void testGetFieldValueIntegerMap() throws Exception {
        Object val = ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("integerMap", "[7]"));
        assertNotNull(val);
        assertEquals("Number 7", val);
    }

    @Test(expected = ReflectionPathException.class)
    public void testGetFieldValueIntegerMap_valueNotFound() throws Exception {
        ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("integerMap", "[10]"));
    }

    @Test
    public void testGetFieldValueLongMap() throws Exception {
        Object val = ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("longMap", "[7]"));
        assertNotNull(val);
        assertEquals("Number 7", val);
    }

    @Test(expected = ReflectionPathException.class)
    public void testGetFieldValueLongrMap_valueNotFound() throws Exception {
        ReflectionHelpers.getFieldValue(sourceObject, Arrays.asList("integerMap", "[10]"));
    }
}
