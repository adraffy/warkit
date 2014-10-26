package com.antistupid.warkit.examples;

import com.antistupid.warbase.stats.StatMap;
import com.antistupid.warbase.types.StatT;
import com.antistupid.warkit.WarKit;
import com.antistupid.warkit.items.ItemEnchant;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BnetForum_EnchantDump {

    static public void main(String[] args) {
        WarKit wk = WarKit.load();
        JSONArray root = new JSONArray();
        for (ItemEnchant x: wk.enchantMap.values()) {
            if (x.isTinker || x.isRetired) {
                continue; // skip tinkers and old enchants, like arcanums
            }
            JSONObject obj = new JSONObject(); 
            obj.put("name", x.name);
            obj.put("icon", x.icon);
            obj.put("spellId", x.spellId);
            obj.put("enchantId", x.enchantment.id);
            if (x.itemId > 0) { // sometimes we dont have an item providing the enchant
                obj.put("itemId", x.itemId);
            } 
            StatMap stats = new StatMap();
            x.enchantment.collectStats(stats, 100); // render as level 100
            JSONArray statList = new JSONArray();
            for (StatT s: StatT.db.types) {
                int amt = stats.getRaw(s);
                if (amt > 0) {
                    JSONObject row = new JSONObject();
                    row.put("statId", s.id);
                    row.put("amount", amt);
                    statList.add(row);
                }
            }
            obj.put("stats", statList); 
            root.add(obj);
        }
        try {
            Files.write(Paths.get("BnetForumEnchants.json"), root.toJSONString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException err) {
            throw new UncheckedIOException(err);
        }        
    }
    
}
