package com.mq.utils;


import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GsonHelper
{
    private static Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .disableHtmlEscaping()
            .registerTypeAdapter(Map.class, new MapTypeAdapter())
            //.addSerializationExclusionStrategy(new IgnoreStrategy());
            .registerTypeAdapter(java.util.Date.class, new DateSerializer()).setDateFormat(DateFormat.LONG)
            .registerTypeAdapter(java.util.Date.class, new DateDeserializer()).setDateFormat(DateFormat.LONG)
            .create();


    public static String toJson(Object obj)
    {

        try {
            return gson.toJson(obj);
        } catch (Exception e) {
            log.error("GsonHelper toJson error cause by:",e);
        }
        return null;
    }

    public static <T> T fromJson(String s, Class<T> clasz) {
        try {
            return gson.fromJson(s, clasz);
        } catch (Exception e) {
            log.error("GsonHelper fromJson error cause by:",e);
        }
        return null;
    }

    public static <T> T fromJson(String s, Type type)
    {
        try {
            return gson.fromJson(s, type);
        } catch (Exception e) {
            log.error("GsonHelper fromJson error cause by:",e);
        }return null;
    }

    public static class DateSerializer implements JsonSerializer<Date> {
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getTime());
        }
    }

    public static class DateDeserializer implements JsonDeserializer<Date> {
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Long datetime =null;
            JsonPrimitive js = json.getAsJsonPrimitive();
            try{
                if(js.isNumber()){
                    return new java.util.Date(js.getAsLong());
                }else if(js.isString()){
                   return DateUtils.parseDate(json.getAsJsonPrimitive().getAsString(), "yyyy-MM-dd HH:mm:ss");
                }
            }catch (Exception e){
                throw  new JsonParseException(e);
            }
            return null;
        }
    }

    public static class MapTypeAdapter
            implements JsonDeserializer
    {   @Override
        public Map deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext)
                throws JsonParseException
        {
            JsonObject jsonObject = json.getAsJsonObject();
            Map p = new HashMap();
            for (Map.Entry e : jsonObject.entrySet()) {
                if (((JsonElement)e.getValue()).isJsonPrimitive()) {
                    p.put(e.getKey(), e.getValue());
                }
                p.put(e.getKey(), ((JsonElement)e.getValue()).getAsString());
            }
            return p;
        }
    }
}
