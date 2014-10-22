package com.antistupid.warkit.examples;

import com.antistupid.warkit.WarKit;
import com.antistupid.warkit.items.ItemContext;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BnetForum {

    static public void exportContexts(WarKit wk, Path file) {
        JSONObject root = new JSONObject();
        {
            // matches dungeon-heroic, dungeon-normal, dungeon-level...
            Predicate<ItemContext> test = c -> c.context.startsWith("dungeon-");        
            root.put("dungeons", wk.wearableMap.values().stream()
                    .filter(x -> x.findContext(test) >= 0)
                    .map(x -> x.itemId)
                    .collect(Collectors.toCollection(JSONArray::new)));
        }
        {
            // matches raid-finder, raid-normal, raid-heroic, raid-mythic
            Predicate<ItemContext> test = c -> c.context.startsWith("raid-");        
            root.put("raids", wk.wearableMap.values().stream()
                    .filter(x -> x.findContext(test) >= 0)
                    .map(x -> x.itemId)
                    .collect(Collectors.toCollection(JSONArray::new)));
        }
        {
            // bonus 15 promotes default uncommon to epic
            // bonus 545 promotes default rare to epic
            Predicate<ItemContext> test = c -> c.defaultBonus.containsBonus(15) || c.defaultBonus.containsBonus(545); 
            root.put("quests", wk.wearableMap.values().stream()
                    .filter(x -> x.findContext(test) >= 0)
                    .map(x -> x.itemId)
                    .collect(Collectors.toCollection(JSONArray::new)));
        }        
        try {
            Files.write(file, root.toJSONString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException err) {
            throw new UncheckedIOException(err);
        }  
    }
    
}
