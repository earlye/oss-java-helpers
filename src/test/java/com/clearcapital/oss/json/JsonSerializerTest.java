package com.clearcapital.oss.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import test.class_heirarchy.BaseClass;
import test.class_heirarchy.DerivedClass;

public class JsonSerializerTest {

	@Test
	public void testImmutableList() throws Exception {
		BaseClass original = new BaseClass();
		original.testMember = ImmutableList.<String> of("Foo");
		
		String json = JsonSerializer.getInstance().getStringRepresentation(original);
		
        assertEquals("{\"@type\":\"base\",\"testMember\":[\"Foo\"]}", json);
		
		BaseClass deserialized = JsonSerializer.getInstance().getObject(json, BaseClass.class);
		assertNotNull(deserialized);
		assertEquals(original.testMember,deserialized.testMember);
	}
	
    @Test
    public void testDerivedClasses() throws Exception {
        DerivedClass original = new DerivedClass();
        original.testMember = ImmutableList.<String> of("Foo");
        original.derivedMember = "Foo";

        String json = JsonSerializer.getInstance().getStringRepresentation(original);
        assertEquals("{\"@type\":\"derived\",\"testMember\":[\"Foo\"],\"derivedMember\":\"Foo\"}", json);

        BaseClass deserialized = JsonSerializer.getInstance().getObject(json, BaseClass.class);
        assertNotNull(deserialized);
        assertEquals(original.testMember, deserialized.testMember);
        assertTrue(deserialized instanceof DerivedClass);
        assertEquals(original.derivedMember, ((DerivedClass) deserialized).derivedMember);
    }

}
