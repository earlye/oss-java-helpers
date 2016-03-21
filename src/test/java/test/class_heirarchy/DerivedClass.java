package test.class_heirarchy;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("derived")
public class DerivedClass extends BaseClass {

    public String derivedMember;
}
