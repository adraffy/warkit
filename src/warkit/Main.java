package warkit;

import warbase.HttpCache;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import warbase.IntSet;
import warbase.data.PlayerScaling;
import warbase.types.EquipT;
import warbase.types.SpecT;
import warbase.types.ProfT;
import warbase.types.RaceT;
import warbase.types.RegionT;
import warkit.armory.Armory;
import warkit.items.Enchantment;
import warkit.items.Gem;
import warkit.items.Item;
import warkit.items.ItemBonus;
import warkit.items.NamedItemBonus;
import warkit.items.Wearable;
import warkit.player.coders.CompactGear;
import warkit.player.Player;

public class Main {

    public static void main(String[] args) {
        
        WarKit wk = WarKit.load(Paths.get("../WarExport/WKDB.dat"));
        
        if (true) {
            
            System.out.println(wk.itemBonusMap.get(499));
            System.out.println(wk.itemBonusMap.get(524));
            
            System.out.println(wk.wearableMap.get(114700));
         
            
            for (Wearable x: wk.wearableMap.values()) {
                if (x.namedBonuses != null) {
                    boolean found = false;
                    for (NamedItemBonus b: x.namedBonuses) {
                        for (ItemBonus y: b.components) {
                            if (y.id == 566) {
                                found = true;
                                break;                                
                            }                            
                        }
                    }
                    if (found) {
                        System.out.println(x.itemId + " # " + x.name);
                    }
                }
                
            }
            
            return;
        }
        
        
        HttpCache hc = new HttpCache();

        if (false) {
            for (Wearable x: wk.wearableMap.values()) {
                if (x.namedBonuses != null && x.nameDesc != null) {
                    System.out.println(x);
                }

            }
            return;
        }
        
        Armory a = new Armory(wk, hc);
        
        
        if (true) { 
            Player p = a.getPlayer("Edgy", "Suramar", RegionT.US, false, System.out::println);
            p.WAIST.dump();
            return;
        }
        
    }
    
}
