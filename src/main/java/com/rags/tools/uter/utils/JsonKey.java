package com.rags.tools.uter.utils;

import io.vertx.core.json.JsonObject;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.List;

public class JsonKey {
    private JsonObject jsonObj;
    private List<String> keyList;

    JsonKey(JsonObject jsonObj, List<String> keyList) {
        this.jsonObj = jsonObj;
        this.keyList = keyList;
    }

    public JsonObject getJsonObj() {
        return jsonObj;
    }

    public void setJsonObj(JsonObject jsonObj) {
        this.jsonObj = jsonObj;
    }

    public List<String> getKeyList() {
        return keyList;
    }

    public void setKeyList(List<String> keyList) {
        this.keyList = keyList;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JsonKey)) {
            return false;
        }
        for (String key : keyList) {
            if (!this.jsonObj.getValue(key).equals(((JsonKey) obj).jsonObj.getValue(key))) {
                return false;
            }
        }
        return true;

    }

    @Override
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        keyList.forEach(key -> {
            if (jsonObj.containsKey(key)) {
                hashCodeBuilder.append(jsonObj.getValue(key));
            }
        });
        return hashCodeBuilder.toHashCode();
    }
}
