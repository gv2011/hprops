package com.github.gv2011.hprops;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.Test;

import com.google.gson.GsonBuilder;


public class ConverterTest {

  @Test
  public void testToPropertiesJsonReader() throws IOException {
    final Converter converter = new Converter();
    Properties props = converter.toProperties("{}");
    assertThat(props.size(), is(0));

    props = converter.toProperties("[]");
    assertThat(props.size(), is(0));

    final Map<String, Object> map = new HashMap<>();
    map.put("text", "Nanü\\\"\nf");
    map.put("bool.ean", true);
    map.put("int", 17);
    map.put("double", 7.123456789012345678D);
    map.put("null", null);

    final Map<String, Object> nested = new HashMap<>();
    final List<Object> list = new ArrayList<>();
    list.add(Long.MAX_VALUE);
    list.add(null);
    nested.put("list", list);

    map.put("nested", nested);

    final String json = new GsonBuilder().setPrettyPrinting().create().toJson(map);
    System.out.println(json);
    props = converter.toProperties(json);
    assertThat(props.size(), is(5));
    for(final Entry<String, Object> e: map.entrySet()){
      if(!e.getKey().equals("nested")){
        final Object value = e.getValue();
        final String strValue = value==null?null:value.toString();
        assertThat(props.getProperty(converter.encodeKey(e.getKey())), is(strValue));
      }
    }
    final StringWriter writer = new StringWriter();
    props.store(writer, null);
    System.out.println(writer);
  }

}
