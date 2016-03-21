package test.class_heirarchy;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        property = "@type")
@JsonTypeName("base")
public class BaseClass {

    public ImmutableList<String> testMember;
}
