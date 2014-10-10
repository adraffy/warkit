package com.antistupid.warkit;

import com.antistupid.warbase.HttpCache;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import com.antistupid.warbase.IntSet;
import com.antistupid.warbase.data.PlayerScaling;
import com.antistupid.warbase.types.EquipT;
import com.antistupid.warbase.types.SpecT;
import com.antistupid.warbase.types.ProfT;
import com.antistupid.warbase.types.RaceT;
import com.antistupid.warbase.types.RegionT;
import com.antistupid.warkit.armory.Armory;
import com.antistupid.warkit.items.Enchantment;
import com.antistupid.warkit.items.Gem;
import com.antistupid.warkit.items.Item;
import com.antistupid.warkit.items.ItemBonus;
import com.antistupid.warkit.items.NamedItemBonus;
import com.antistupid.warkit.items.Wearable;
import com.antistupid.warkit.player.coders.CompactGear;
import com.antistupid.warkit.player.Player;

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
            wk.wearableMap.get(106604).dump();
            return;
        }
        
        if (true) { 
            Player p = a.getPlayer("Edgy", "Suramar", RegionT.US, false, System.out::println);
            p.WAIST.dump();
            return;
        }
        
    }
    
}
