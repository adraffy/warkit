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
        HttpCache hc = new HttpCache();
        Armory a = new Armory(wk, hc);
        
        if (false) {
            a.getRealmList(RegionT.EU).forEach(System.out::println);
            return;
        }
        
        if (true) {
            wk.wearableMap.get(116182).dump();
            return;
        }
        
        if (true) { 
            Player p = a.getPlayer("Edgy", "Suramar", RegionT.US, false, System.out::println);
            p.WAIST.dump();
            return;
        }
        
    }
    
}
