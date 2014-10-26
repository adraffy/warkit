package com.antistupid.warkit.examples;

import com.antistupid.warbase.HttpCache;
import com.antistupid.warbase.types.RegionT;
import com.antistupid.warkit.WarKit;
import com.antistupid.warkit.armory.Armory;
import com.antistupid.warkit.player.Player;

public class BnetForum_HeirloomWarbow {

    static public void main(String... args) {        
        WarKit wk = WarKit.load();        
        Armory a = new Armory(wk, new HttpCache(), args[0]); // arg[0] = API key
        Player p = a.getPlayer("greenninja", "Bleeding Hollow", RegionT.US, Armory.TALENT_SPEC_SELECTED, false, System.err::println);
        p.MH.dump();        
    }
    
}
