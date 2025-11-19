package org.dubytube.dubytube.util;

import com.google.gson.*;
import org.dubytube.dubytube.ds.MyLinkedList;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador personalizado de GSON para serializar/deserializar MyLinkedList.
 * 
 * <p>Convierte MyLinkedList a/desde un array JSON estándar para persistencia.</p>
 * 
 * @param <T> Tipo de elemento en la lista
 * @author DubyTube Team
 * @version 1.0
 * @since 2025-11-18
 */
public class MyLinkedListAdapter<T> implements JsonSerializer<MyLinkedList<T>>, JsonDeserializer<MyLinkedList<T>> {

    @Override
    public JsonElement serialize(MyLinkedList<T> src, Type typeOfSrc, JsonSerializationContext context) {
        // Convertir MyLinkedList a ArrayList para serialización
        List<T> list = new ArrayList<>();
        for (T item : src) {
            list.add(item);
        }
        return context.serialize(list);
    }

    @Override
    public MyLinkedList<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        MyLinkedList<T> result = new MyLinkedList<>();
        
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            // Para deserializar, necesitamos obtener el tipo de elemento
            // Usamos Object.class como fallback si no podemos determinar el tipo exacto
            try {
                if (typeOfT instanceof java.lang.reflect.ParameterizedType) {
                    java.lang.reflect.ParameterizedType pType = (java.lang.reflect.ParameterizedType) typeOfT;
                    Type[] typeArgs = pType.getActualTypeArguments();
                    if (typeArgs.length > 0) {
                        Type elementType = typeArgs[0];
                        for (JsonElement elem : array) {
                            T item = context.deserialize(elem, elementType);
                            result.add(item);
                        }
                        return result;
                    }
                }
                // Fallback: intentar deserializar como JsonObject genérico
                for (JsonElement elem : array) {
                    if (elem.isJsonObject()) {
                        // Asumimos que es un objeto genérico
                        result.add((T) elem);
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠ Error deserializando MyLinkedList: " + e.getMessage());
            }
        }
        
        return result;
    }
}
