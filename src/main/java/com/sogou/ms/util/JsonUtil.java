package com.sogou.ms.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class JsonUtil {

    public static String format(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(obj);
    }

    public static void format(Object obj, Writer out) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(out, obj);
    }

    public static JsonNode parse(String json) throws IOException {
        return new ObjectMapper().readTree(json);
    }

    public static JsonNode parse(Reader reader) throws IOException {
        return new ObjectMapper().readTree(reader);
    }

    public static String text(JsonNode rootNode, String name) {
        JsonNode jsonNode = rootNode.findValue(name);
        return jsonNode == null ? null : jsonNode.asText();
    }

}
