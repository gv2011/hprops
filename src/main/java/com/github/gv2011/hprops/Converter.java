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

import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.run;

import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;
import java.util.function.Consumer;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;


public class Converter {


  public Properties toProperties(final String json){
    return toProperties(new StringReader(json));
  }

  public Properties toProperties(final Reader json){
    final Properties result = new Properties();
    toProperties(new JsonReader(json), (p)->result.setProperty(p.key(), p.value()));
    return result;
  }

  private void toProperties(final JsonReader json, final Consumer<Property> target){
    final JsonToken t = call(json::peek);
    dispatchTop(json, target, t, "");
  }

  private void doObject(final JsonReader json, final Consumer<Property> target, final String prefix){
    run(json::beginObject);
    JsonToken t = call(json::peek);
    while(!t.equals(JsonToken.END_OBJECT)){
      final String key = (prefix.isEmpty()?"":prefix+".")+encodeKey(call(json::nextName));
      t = call(json::peek);
      dispatch(json, target, t, key);
      t = call(json::peek);
    }
    run(json::endObject);
  }

  private void dispatchTop(final JsonReader json, final Consumer<Property> target, final JsonToken t, final String key){
    if(t.equals(JsonToken.BEGIN_OBJECT)) doObject(json, target, key);
    else if(t.equals(JsonToken.BEGIN_ARRAY)) doList(json, target, key);
    else throw new IllegalArgumentException(t.toString());
  }

  private void dispatch(
    final JsonReader json, final Consumer<Property> target, final JsonToken t, final String key
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

  private void doList(final JsonReader json, final Consumer<Property> target, final String prefix){
    run(json::beginArray);
    int i=1;
    JsonToken t = call(json::peek);
    while(!t.equals(JsonToken.END_ARRAY)){
      final String key = (prefix.isEmpty()?"":prefix+".")+i;
      dispatch(json, target, t, key);
      i++;
      t = call(json::peek);
    }
    run(json::endArray);
  }

  private void getNull(final JsonReader json) {
    run(json::nextNull);
  }

  private String getNumber(final JsonReader json){
    return call(json::nextString);
  }

  private String getString(final JsonReader json){
    return call(json::nextString);
  }

  private String getBoolean(final JsonReader json){
    return Boolean.toString(call(json::nextBoolean));
  }

}
