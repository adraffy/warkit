package com.antistupid.warkit.examples;

import com.antistupid.warbase.types.ConsumeT;
import com.antistupid.warkit.WarKit;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BnetForum_ItemIds {

    static public void main(String... args) {
        WarKit wk = WarKit.load();
        JSONObject root = new JSONObject();        
        root.put("items", wk.wearableMap.values().stream().map(x -> x.itemId).collect(Collectors.toCollection(JSONArray::new)));
        root.put("gems", wk.gemMap.values().stream().map(x -> x.itemId).collect(Collectors.toCollection(JSONArray::new)));        
        // not all enchants have a corresponding item
        root.put("enchants", wk.enchantMap.values().stream().filter(x -> x.itemId > 0).map(x -> x.itemId).collect(Collectors.toCollection(JSONArray::new)));        
        root.put("foods", wk.consumeMap.values().stream().filter(x -> x.type == ConsumeT.FOOD).map(x -> x.itemId).collect(Collectors.toCollection(JSONArray::new)));
        root.put("potions", wk.consumeMap.values().stream().filter(x -> x.type == ConsumeT.POTION).map(x -> x.itemId).collect(Collectors.toCollection(JSONArray::new)));
        // includes elixirs, flasks, scrolls
        root.put("elixirs", wk.consumeMap.values().stream().filter(x -> x.type.isElixirFlaskScroll).map(x -> x.itemId).collect(Collectors.toCollection(JSONArray::new)));        
        try {
            Files.write(Paths.get("ItemIds.json"), root.toJSONString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException err) {
            throw new UncheckedIOException(err);
        }
    }
    
}
