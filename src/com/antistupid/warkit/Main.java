package com.antistupid.warkit;

import com.antistupid.warbase.HttpCache;
import java.nio.file.Paths;
import com.antistupid.warbase.types.EquipT;
import com.antistupid.warbase.types.RegionT;
import com.antistupid.warkit.armory.Armory;
import com.antistupid.warkit.examples.BnetForum;
import com.antistupid.warkit.items.Wearable;
import com.antistupid.warkit.player.Player;
import com.antistupid.warkit.player.coders.CompactGear;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Main {

    static public void main(String[] args)  {
  
        // obfuscate api key
        String apiKey;
        try {
            apiKey = new String(Files.readAllBytes(Paths.get("APIKey.txt")), StandardCharsets.UTF_8);
        } catch (IOException err) {
            return;
        }      
        
        HttpCache hc = new HttpCache();       
        WarKit wk = WarKit.load(Paths.get("../WarExport/WKDB.dat"));            
        Armory a = new Armory(wk, hc, apiKey);        
        
        if (false) {
            for (Wearable x: wk.wearableMap.values()) {
                if (x.itemLevel >= 800) {
                    System.out.println(x.itemId + " # [" + x.itemLevel + "] " + x.name);
                }
            }            
            return;
        }
        
        if (false) {
            BnetForum.exportContexts(wk, Paths.get("BnetForumItemContexts.json"));
            return;
        }

        if (false) {
            a.getRealmList(RegionT.EU).forEach(System.out::println);
            return;
        }
        
        if (false) {
            a.findPlayers("Edgy", RegionT.US, false, 0, null).forEach(System.out::println);
            return;
        }
        
        if (false) { 
            Player p = a.getPlayer("Edgy", "Suramar", RegionT.US, 0, false, System.out::println);
            p.WAIST.dump();
            return;
        }
        
    }
    
}
