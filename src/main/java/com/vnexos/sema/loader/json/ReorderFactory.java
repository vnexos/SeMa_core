package com.vnexos.sema.loader.json;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.vnexos.sema.database.annotations.Identity;
import com.vnexos.sema.util.ClassUtils;

/**
 * A specialized {@code TypeAdapterFactory} to helps Gson factory take the field
 * annotated by {@code &#64;Identity} or have the name with {@code id} to the
 * first of the Json object.
 * 
 * @author Trần Việt Đăng Quang
 * @see Identity
 */
public class ReorderFactory implements TypeAdapterFactory {

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

    if (!Object.class.isAssignableFrom(type.getRawType())) {
      return delegate;
    }

    return new TypeAdapter<T>() {

      @Override
      public T read(JsonReader reader) throws IOException {
        return delegate.read(reader);
      }

      @Override
      public void write(JsonWriter writer, T value) throws IOException {
        if (value == null) {
          writer.nullValue();
          return;
        }

        Field field = ClassUtils.findAnnotatedField(value.getClass(), Identity.class);
        String idField = field == null ? "id" : field.getName();

        // Safely serialize to a JsonObject
        JsonElement tree = delegate.toJsonTree(value);
        if (!tree.isJsonObject()) {
          // Not a typical POJO — fallback
          Streams.write(tree, writer);
          return;
        }

        JsonObject original = tree.getAsJsonObject();
        JsonObject reordered = new JsonObject();

        // Move "id" field to the top if it exists
        if (original.has(idField)) {
          reordered.add(idField, original.get(idField));
        }

        // Add all other fields
        for (Map.Entry<String, JsonElement> entry : original.entrySet()) {
          if (!entry.getKey().equals("id")) {
            reordered.add(entry.getKey(), entry.getValue());
          }
        }

        Streams.write(reordered, writer);
      }
    };
  }

}
