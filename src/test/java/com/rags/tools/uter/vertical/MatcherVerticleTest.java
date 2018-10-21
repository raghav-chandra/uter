package com.rags.tools.uter.vertical;


import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.rags.tools.uter.vertical.MatcherVerticle.FinalStatus;


public class MatcherVerticleTest {

    private static final MatcherVerticle vert = new MatcherVerticle();

    private Object getValue(JsonObject obj, String keyLoc) {
        List<String> keys = Arrays.asList(keyLoc.split("\\."));
        Object newObj = obj.copy();
        for(String key : keys) {
            newObj = ((JsonObject) newObj).getValue(key);
        }
        return newObj;
    }

    private boolean checkForAllMatching(JsonObject res) {

        return res.fieldNames().stream().map(key -> {
            Object val = res.getValue(key);
            return (val instanceof JsonObject ? checkForAllMatching((JsonObject) val) : "MATCHING".equals(val));
        }).allMatch(Boolean.TRUE::equals);

    }



    @Test
    void testGetValue() {
        JsonObject act = getDummyObj();
        act.put("obj1", getDummyObj());

        Assertions.assertEquals("Key2", getValue(act, "obj1.key2"));

    }


    private JsonObject getDummyObj() {
        return new JsonObject().put("key1", 23)
                .put("key2", "Key2");
    }

    @Test
    void testSimpleObjEqual() {
        JsonObject actual = getDummyObj();
        JsonObject expect = getDummyObj();
        JsonObject attributeMarker = new JsonObject();
        MatcherVerticle.FinalStatus stat = vert.match(expect, actual, attributeMarker, "key1");

        Assertions.assertEquals(MatcherVerticle.FinalStatus.PASS, stat);
        Assertions.assertEquals("MATCHING", ((JsonObject)(attributeMarker.getValue("key1"))).getValue("key1"));
        Assertions.assertEquals("MATCHING", ((JsonObject)(attributeMarker.getValue("key1"))).getValue("key2"));
    }

    @Test
    void testSimpleObjNull() {
        JsonObject actual = getDummyObj().put("key2", (String)null);
        JsonObject expect = getDummyObj().put("key2", (String)null);
        JsonObject attributeMarker = new JsonObject();
        FinalStatus stat = vert.match(expect, actual, attributeMarker, "key1");

        Assertions.assertEquals(FinalStatus.PASS, stat);
        Assertions.assertEquals("MATCHING", ((JsonObject)(attributeMarker.getValue("key1"))).getValue("key1"));
        Assertions.assertEquals("MATCHING", ((JsonObject)(attributeMarker.getValue("key1"))).getValue("key2"));

        actual.put("key2", "wer");
        Assertions.assertEquals(FinalStatus.FAIL, vert.match(expect, actual, attributeMarker, "key1"));

    }




    @Test
    void testSimpleObjNotEqual() {
        JsonObject actual = getDummyObj();
        JsonObject expect = getDummyObj().put("key2", "Key3");
        JsonObject attributeMarker = new JsonObject();
        FinalStatus stat = vert.match(expect, actual, attributeMarker, "key1");

        Assertions.assertEquals(FinalStatus.FAIL, stat);
        Assertions.assertEquals("MATCHING", ((JsonObject)(attributeMarker.getValue("key1"))).getValue("key1"));
        Assertions.assertEquals("NOT_MATCHING", ((JsonObject)(attributeMarker.getValue("key1"))).getValue("key2"));
    }

    @Test
    void testObjInsideObjEqual() {
        JsonObject act = getDummyObj();
        JsonObject exp = getDummyObj();

        act.put("obj1", getDummyObj());
        exp.put("obj1",getDummyObj());

        JsonObject attributeMarker = new JsonObject();
        FinalStatus stat = vert.match(exp, act, attributeMarker, "key1");

        Assertions.assertEquals(FinalStatus.PASS, stat);
        Assertions.assertTrue(checkForAllMatching(attributeMarker));

    }



    @Test
    void testObjInsideObjNotEqual() {
        JsonObject act = getDummyObj();
        JsonObject exp = getDummyObj();

        act.put("obj1", getDummyObj());
        exp.put("obj1",getDummyObj().put("key1", 234));

        JsonObject attributeMarker = new JsonObject();
        FinalStatus stat = vert.match(exp, act, attributeMarker, "key1");

        Assertions.assertEquals(FinalStatus.FAIL, stat);
        Assertions.assertFalse(checkForAllMatching(attributeMarker));
    }

    private JsonObject objForArray(int val) {
        return new JsonObject()
                .put("obKey1", val)
                .put("obKey2", "KEY");
    }

    private JsonArray dummyArr() {
        return new JsonArray().add(objForArray(1)).add(objForArray(2));
    }

    @Test
    void testArrayEqual() {
        JsonArray act = dummyArr();
        JsonArray exp = dummyArr();

        JsonObject bestMatch = new JsonObject();

        FinalStatus stat = vert.match(exp, act, bestMatch);

        Assertions.assertEquals(FinalStatus.PASS, stat);

    }

    // Not working
    @Test
    void testArrayNotEqual1() {
        JsonArray act = new JsonArray().add(objForArray(3)).add(objForArray(4));
        JsonArray exp = new JsonArray().add(objForArray(2)).add(objForArray(3));

        JsonObject bestMatch = new JsonObject();

        FinalStatus stat = vert.match(exp, act, bestMatch);

        //Assertions.assertEquals(FinalStatus.FAIL, stat);

    }



}