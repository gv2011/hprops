package com.github.gv2011.hprops;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Consumer;

import com.google.gson.stream.JsonWriter;

public class Converter2 {


  public Properties toProperties(final Object obj){
    final Properties result = new Properties();
    toProperties(obj, (p)->result.setProperty(p.key(), p.value()));
    return result;
  }

  public void toProperties(final Object obj, final Consumer<Property> target){
    dispatchTop(obj, target, "");
  }

  private void doObject(final Map<?,?> map, final Consumer<Property> target, final String prefix){
    for(final Entry<?, ?> e: map.entrySet()){
      if(!(e.getKey() instanceof String)) throw new IllegalArgumentException();
      final String key = (prefix.isEmpty()?"":prefix+".")+encodeKey((String)e.getKey());
      dispatch(e.getValue(), target, key);
    }
  }

  private void dispatchTop(final Object obj, final Consumer<Property> target, final String key){
    if(obj instanceof Map) doObject((Map<?,?>)obj, target, key);
    else if(obj instanceof Iterable) doList((Iterable<?>)obj, target, key);
    else throw new IllegalArgumentException(obj==null?"null":obj.getClass().getName());
  }

  private void dispatch(
    final Object obj, final Consumer<Property> target, final String key
  ){
    if(obj instanceof Boolean) write(target, key, obj.toString());
    else if(obj instanceof String) write(target, key, encodeString(obj.toString()));
    else if(obj instanceof Number) write(target, key, obj.toString());
    else if(obj instanceof Map) doObject((Map<?,?>)obj, target, key);
    else if(obj instanceof Iterable) doList((Iterable<?>)obj, target, key);
    else if(obj==null);
    else throw new IllegalArgumentException(obj.getClass().getName());
  }

  private String encodeString(final Object obj) {
    new JsonWriter();
    return null;
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

  private void doList(final Iterable<?> list, final Consumer<Property> target, final String prefix){
    int i=1;
    for(final Object e: list){
      final String key = (prefix.isEmpty()?"":prefix+".")+i;
      dispatch(e, target, key);
      i++;
    }
  }

}
