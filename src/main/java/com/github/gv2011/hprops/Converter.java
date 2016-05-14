package com.github.gv2011.hprops;

import static com.github.gv2011.util.ServiceLoaderUtils.loadService;

import java.io.StringReader;
import java.util.Properties;
import java.util.function.Consumer;

import com.github.gv2011.jsoncore.JsonFactory;
import com.github.gv2011.jsoncore.JsonParser;
import com.github.gv2011.jsoncore.JsonToken;


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
    final com.github.gv2011.jsoncore.JsonToken t = json.peek();
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
