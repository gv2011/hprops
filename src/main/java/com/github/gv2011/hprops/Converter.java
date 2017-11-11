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
import static com.github.gv2011.util.ServiceLoaderUtils.loadService;

import java.io.StringReader;
import java.util.Properties;
import java.util.function.Consumer;

import com.github.gv2011.gsoncore.JsonFactory;
import com.github.gv2011.gsoncore.JsonParser;
import com.github.gv2011.gsoncore.JsonToken;


public class Converter {

  private static final JsonFactory JSON = loadService(JsonFactory.class);


  public Properties toProperties(final String json){
    return toProperties(JSON.newJsonParser(new StringReader(json)));
  }

  public Properties toProperties(final JsonParser json){
    final Properties result = new Properties();
    toProperties(json, (p)->result.setProperty(p.key(), p.value()));
    return result;
  }

  public void toProperties(final JsonParser json, final Consumer<Property> target){
    final JsonToken t = json.peek();
    dispatchTop(json, target, t, "");
  }

  private void doObject(final JsonParser json, final Consumer<Property> target, final String prefix){
    json.beginObject();
    JsonToken t = json.peek();
    while(!t.equals(JsonToken.END_OBJECT)){
      final String key = (prefix.isEmpty()?"":prefix+".")+encodeKey(json.nextName());
      t = json.peek();
      dispatch(json, target, t, key);
      t = json.peek();
    }
    json.endObject();
  }

  private void dispatchTop(final JsonParser json, final Consumer<Property> target, final JsonToken t, final String key){
    if(t.equals(JsonToken.BEGIN_OBJECT)) doObject(json, target, key);
    else if(t.equals(JsonToken.BEGIN_ARRAY)) doList(json, target, key);
    else throw new IllegalArgumentException(t.toString());
  }

  private void dispatch(
    final JsonParser json, final Consumer<Property> target, final JsonToken t, final String key
  ){
    if(t.equals(JsonToken.BOOLEAN)) write(target, key, getBoolean(json));
    else if(t.equals(JsonToken.STRING)) write(target, key, getString(json));
    else if(t.equals(JsonToken.NUMBER)) write(target, key, getNumber(json));
    else if(t.equals(JsonToken.BEGIN_OBJECT)) doObject(json, target, key);
    else if(t.equals(JsonToken.BEGIN_ARRAY)) doList(json, target, key);
    else if(t.equals(JsonToken.NULL)) getNull(json);
    else throw new IllegalArgumentException(t.toString());
  }

  private void write(final Consumer<Property> target, final String key, final String value) {
    target.accept(new Property(key, value));
  }

  String encodeKey(final String key) {
    return key.replace("\\", "\\\\").replace(".", "\\.");
  }

  String decodeKey(final String key) {
    return key.replace("\\.", ".").replace("\\\\", "\\");
  }

  private void doList(final JsonParser json, final Consumer<Property> target, final String prefix){
    json.beginArray();
    int i=1;
    JsonToken t = json.peek();
    while(!t.equals(JsonToken.END_ARRAY)){
      final String key = (prefix.isEmpty()?"":prefix+".")+i;
      dispatch(json, target, t, key);
      i++;
      t = json.peek();
    }
    json.endArray();
  }

  private void getNull(final JsonParser json) {
    json.nextNull();
  }

  private String getNumber(final JsonParser json){
    return json.nextString();
  }

  private String getString(final JsonParser json){
    return json.nextString();
  }

  private String getBoolean(final JsonParser json){
    return Boolean.toString(json.nextBoolean());
  }

}
