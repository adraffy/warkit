package com.antistupid.warkit;

import com.antistupid.warbase.HttpCache;
import java.nio.file.Paths;
import com.antistupid.warbase.types.EquipT;
import com.antistupid.warbase.types.RegionT;
import com.antistupid.warkit.armory.Armory;
import com.antistupid.warkit.player.Player;
import com.antistupid.warkit.player.coders.CompactGear;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class Main {

    public static void main(String[] args)  {
        
        //-verbose:class
        
        HttpCache hc = new HttpCache();       
        WarKit wk = WarKit.load(Paths.get("../WarExport/WKDB.dat"));    
        Armory a = new Armory(wk, hc, "6maqaabqfmk4k26nguwjtw5c86w33twy");
        
        
        if (false) {            
            wk.wearableMap.get(109759).dump();
            return;
        }
        
        
        if (true) {
            
            Player p = new Player();
            CompactGear.fromString("109759 $5324 :115809 !524/561/564 ", p, wk);
            
            
            
            return;
        }
        
        
        if (false) {
            a.getRealmList(RegionT.EU).forEach(System.out::println);
            return;
        }
        
        if (false) {
            //wk.wearableMap.values().stream().filter(x -> (x.reqLevel > 90 || x.itemLevel == 378) && x.quality == QualityT.PURPLE && x.nameDesc == null).forEach(x -> System.out.println(x.itemId + " # " + x.name + " (" + x.nameDesc + ")"));            
            //wk.wearableMap.get(106604).dump();
            wk.wearableMap.values().stream().filter(x -> x.itemId > 105000 && x.statAllocs != null && x.statAllocs.length < 3 && x.equip != EquipT.TRINKET).forEach(x -> System.out.println(x.itemId + " # " + x.name));            
            return;
        }
        
        if (false) {
            a.findPlayers("Edgy", RegionT.US, false, 0, null).forEach(System.out::println);
            return;
        }
        
        
        if (true) { 
            Player p = a.getPlayer("Edgy", "Suramar", RegionT.US, 0, false, System.out::println);
            p.WAIST.dump();
            return;
        }
        
    }
    
}
