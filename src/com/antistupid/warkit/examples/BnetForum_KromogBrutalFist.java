package com.antistupid.warkit.examples;

import com.antistupid.warkit.WarKit;
import com.antistupid.warkit.items.ItemContext;
import com.antistupid.warkit.items.Weapon;
import com.antistupid.warkit.items.Wearable;
import com.antistupid.warkit.player.Player;

public class BnetForum_KromogBrutalFist {

    static public void main(String... args) {        
        // load warkit
        WarKit wk = WarKit.load();        
        // find the item, make sure it's a weapon
        Wearable item = wk.wearableMap.get(113927);
        if (!(item instanceof Weapon)) {
            throw new IllegalArgumentException("not a weapon!");
        }        
        Weapon w = (Weapon)item;
        // dump out the contexts (can be null)
        if (w.contexts != null) {
            for (ItemContext x: w.contexts) {
                System.out.println(x);
            }
        }
        // equip the weapon in the main-hand of a level 100 spec-less player
        Player p = new Player();
        p.playerLevel = 100;        
        p.MH.setItem(w);
        // by default, the context is the first one (normal)
        System.out.println(p.MH.getItemName(true, true, true) +  " => " + p.MH.getWeaponMin() + " - " + p.MH.getWeaponMax());        
        // change the context to "raid-heroic"
        p.MH.setContextIndex(w.findContext(x -> x.context.equals("raid-heroic")));
        System.out.println(p.MH.getItemName(true, true, true) +  " => " + p.MH.getWeaponMin() + " - " + p.MH.getWeaponMax());        
        // change the contex to "raid-mythic"
        p.MH.setContextIndex(w.findContext(x -> x.context.equals("raid-mythic")));              
        System.out.println(p.MH.getItemName(true, true, true) +  " => " + p.MH.getWeaponMin() + " - " + p.MH.getWeaponMax());        
    }
    
}
