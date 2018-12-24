package com.rags.tools.uter.vertical;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MatchingUtilTest {

    @Test
    public void testSimple0LevelJsonObject() {
        JsonObject expected = new JsonObject()
                .put("name", "Raghav Chandra")
                .put("kerberos", "charag")
                .put("mobile", 8867987654L)
                .put("tension", "NO")
                .put("id", 10110);

        JsonObject actual = new JsonObject()
                .put("name", "Raghav Chandra")
                .put("kerberos", "charag")
                .put("mobile", 9065065882L)
                .put("id", 10110);

        JsonObject status = new MatchingUtil().findBestMatchingAttrCount(expected, actual);

        assertEquals("F", status.getString("status"));
        assertEquals(expected, status.getJsonObject("exp"));
        assertEquals(actual, status.getJsonObject("act"));
        assertEquals((Integer) 3, status.getInteger("count"));

        JsonObject diff = status.getJsonObject("diff");
        assertEquals("P", diff.getJsonObject("name").getString("status"));
        assertEquals("P", diff.getJsonObject("kerberos").getString("status"));
        assertEquals("F", diff.getJsonObject("mobile").getString("status"));
        assertEquals((Long) 8867987654L, diff.getJsonObject("mobile").getLong("exp"));
        assertEquals((Long) 9065065882L, diff.getJsonObject("mobile").getLong("act"));
        assertEquals("P", diff.getJsonObject("id").getString("status"));

        assertEquals("F", diff.getJsonObject("tension").getString("status"));
        assertEquals("NO", diff.getJsonObject("tension").getString("exp"));
        assertNull(diff.getJsonObject("tension").getString("act"));
    }

    @Test
    public void testSimple1LevelJsonObject() {
        JsonObject expected = new JsonObject()
                .put("name", "Raghav Chandra")
                .put("kerberos", "charag")
                .put("mobile", 8867987654L)
                .put("id", 10110)
                .put("add", new JsonObject()
                        .put("city", "Prayagraj")
                        .put("state", "UP")
                        .put("pin", 211002)
                        .put("landmark", "mosque"));

        JsonObject actual = new JsonObject()
                .put("name", "Raghav Chandra")
                .put("kerberos", "charag")
                .put("mobile", 8867987654L)
                .put("id", 10110)
                .put("add", new JsonObject()
                        .put("city", "Prayagraj")
                        .put("state", "UP")
                        .put("pin", 211002)
                        .put("landmark", "temple"));

        JsonObject status = new MatchingUtil().findBestMatchingAttrCount(expected, actual);


        assertEquals("F", status.getString("status"));
        assertEquals(expected, status.getJsonObject("exp"));
        assertEquals(actual, status.getJsonObject("act"));
        assertEquals((Integer) 4, status.getInteger("count"));

        JsonObject diff = status.getJsonObject("diff");
        assertEquals("P", diff.getJsonObject("name").getString("status"));
        assertEquals("P", diff.getJsonObject("kerberos").getString("status"));
        assertEquals("P", diff.getJsonObject("mobile").getString("status"));
        assertEquals("P", diff.getJsonObject("id").getString("status"));

        assertEquals("F", diff.getJsonObject("add").getString("status"));
        JsonObject addDiff = diff.getJsonObject("add").getJsonObject("diff");
        assertEquals("P", addDiff.getJsonObject("city").getString("status"));
        assertEquals("P", addDiff.getJsonObject("state").getString("status"));
        assertEquals("P", addDiff.getJsonObject("pin").getString("status"));
        assertEquals("F", addDiff.getJsonObject("landmark").getString("status"));
        assertEquals("mosque", addDiff.getJsonObject("landmark").getString("exp"));
        assertEquals("temple", addDiff.getJsonObject("landmark").getString("act"));
    }


    @Test
    public void testSimple0LevelJsonArray() {

        JsonArray expected = new JsonArray(Arrays.asList("Raghav", "Chandra", "Bengaluru", "Prayagraj", "yoyo"));
        JsonArray actual = new JsonArray(Arrays.asList("Raghav", "Chandra", "Bengaluru", "Prayagraj", "noyo"));

        JsonObject status = new MatchingUtil().findBestMatchingAttrCount(expected, actual);
        System.out.println(status);
    }
}
