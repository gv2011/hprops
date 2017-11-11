package com.github.gv2011.hprops;

/*-
 * #%L
 * Hprops
 * %%
 * Copyright (C) 2016 - 2017 Vinz (https://github.com/gv2011)
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
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
