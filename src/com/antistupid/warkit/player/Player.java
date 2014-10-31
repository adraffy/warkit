package com.antistupid.warkit.player;

import java.util.function.Consumer;
import com.antistupid.warbase.stats.StatMap;
import com.antistupid.warbase.types.EquipT;
import com.antistupid.warbase.types.ProfT;
import com.antistupid.warbase.types.RaceT;
import com.antistupid.warbase.types.RegionT;
import com.antistupid.warbase.types.SlotT;
import com.antistupid.warbase.types.SpecT;
import com.antistupid.warbase.types.StatT;
import com.antistupid.warkit.items.Item;
import com.antistupid.warkit.items.ItemSet;
import com.antistupid.warkit.items.SetBonus;
import com.antistupid.warkit.items.Unique;
import com.antistupid.warkit.items.Wearable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Player {

    static public final int MAX_PROFS = 2;
    static public final int MAX_SOCKETS = 6;

    static public final int MAX_PLAYER_LEVEL = 100;
    static public final int MAX_PROF_LEVEL = 700;
    static public final int MAX_ITEM_LEVEL = 1000;
    
      
    public SpecT spec;
    public RaceT race;
    
    public String playerName;
    public boolean playerMale;
    public RegionT region;
    public String realmName;
    public String realmSlug;
    
    public int playerLevel = MAX_PLAYER_LEVEL;
    public boolean pvpMode;
    public int scaledItemLevel;

    /*
    static class WeaponData {
        float min;
        float max;
        float swing;
        
        public double getDPS() {
            return (min + max);
        }
    }
    
    public final WeaponData weapon_MH;
    public final WeaponData weapon_OH;  
    */
    boolean _bothHandsForMH;
    
    public final PlayerProf[] PROF = new PlayerProf[MAX_PROFS];
    long _profBits;
    
    public final PlayerSlot[] SLOT;
    public final PlayerSlot HEAD, NECK, SHOULDER, BACK, CHEST, SHIRT, TABARD, WRIST, HANDS, WAIST, LEGS, FEET, F1, F2, T1, T2, MH, OH;

    
    public Player() {
        for (int i = 0; i < PROF.length; i++) {
            PROF[i] = new PlayerProf(this, i);
        }        
        SLOT = new PlayerSlot[SlotT.db.types.length];
        for (SlotT x: SlotT.db.types) {
            SLOT[x.index] = new PlayerSlot(this, x);
        }     
        HEAD = SLOT[SlotT.HEAD.index];
        NECK = SLOT[SlotT.NECK.index];
        SHOULDER = SLOT[SlotT.SHOULDER.index];
        BACK = SLOT[SlotT.BACK.index];
        CHEST = SLOT[SlotT.CHEST.index];
        SHIRT = SLOT[SlotT.SHIRT.index];
        TABARD = SLOT[SlotT.TABARD.index];
        WRIST = SLOT[SlotT.WRIST.index];
        HANDS = SLOT[SlotT.HANDS.index];
        WAIST = SLOT[SlotT.WAIST.index];
        LEGS = SLOT[SlotT.LEGS.index];
        FEET = SLOT[SlotT.FEET.index];
        F1 = SLOT[SlotT.FINGER_1.index];
        F2 = SLOT[SlotT.FINGER_2.index];
        T1 = SLOT[SlotT.TRINKET_1.index];
        T2 = SLOT[SlotT.TRINKET_2.index];
        MH = SLOT[SlotT.MAIN_HAND.index];
        OH = SLOT[SlotT.OFF_HAND.index];
    }
    
    public void setItem(Item item, boolean empty) {
        if (item instanceof Wearable) {            
            PlayerSlot slot = findSlot(item.equip, empty);
            if (slot == null) {
                throw new PlayerError(String.format("No room available for %s", item.name));
            }
            slot.setItem(item);
        } else { //if (item instanceof Gem) {
            throw new PlayerError(String.format("%s cannot be equipped", item.name));
        }        
    }
    
    public PlayerSlot findSlot(EquipT equip, boolean empty) {
        for (PlayerSlot x: SLOT) {
            if ((!empty || x._item == null) && x.slotType.canContain(equip)) {
                return x;
            }
        }
        return null;
    }
    
    public void forEach(Consumer<PlayerSlot> c) {
        for (PlayerSlot x: SLOT) {
            c.accept(x);
        }
    }
    
    /*
    public void setPvP(boolean pvp) {
        this.pvpMode = pvp;
        updateAll();
    }
    
    public void setScaledItemLevel(int itemLevel) {
        scaledItemLevel = itemLevel;
        updateAll();
    }
    */
    
    public void clear() {
        clearSlots();
        clearProfs();
        spec = null;
        race = null;
        playerLevel = Player.MAX_PLAYER_LEVEL;
        pvpMode = false;
        scaledItemLevel = 0;
    }
    
    public void copySlots(Player p, Consumer<PlayerError> errors) {
        clearSlots();
        for (int i = 0; i < SLOT.length; i++) {
            try {
                SLOT[i].copy(p.SLOT[i], errors);            
            } catch (PlayerError err) {   
                if (errors != null) {
                    errors.accept(err);
                }
            }
        }
    }
    
    public void copyProfs(Player p, Consumer<PlayerError> errors) {
        clearProfs();
        for (int i = 0; i < PROF.length; i++) {
            try {
                PROF[i].copy(p.PROF[i]);
            } catch (PlayerError err) {   
                if (errors != null) {
                    errors.accept(err);
                }
            }
        }
    }
    
    public void copySetup(Player p) {
        spec = p.spec;
        race = p.race;
        playerName = p.playerName;
        playerMale = p.playerMale;
        playerLevel = p.playerLevel;
        realmName = p.realmName;
        realmSlug = p.realmSlug;
        region = p.region;
        pvpMode = p.pvpMode;
        scaledItemLevel = p.scaledItemLevel;
    }
    
    public boolean isAsia() {
        return region != null && region.asia;
    }
    
    // return true if was invalid
    public boolean validateRace() {
        if (spec == null) {
            if (race == null) {
                return false;
            } else {
                race = null;
                return true;
            }
        } else if (!spec.classType.canBe(race)) {                
            race = spec.classType.races.get(0);
            return true;
        } else {
            return false;
        }
    }
    
    public void copy(Player p, Consumer<PlayerError> errors) {
        clear();
        copySetup(p);
        copyProfs(p, errors);
        copySlots(p, errors);
    }
    
    public void clearSlots() {
        for (PlayerSlot x: SLOT) {
            x.clear();
        }
    }
    
    public void clearGems() {
        for (PlayerSlot x: SLOT) {
            x.clearGems();
        }
    }
    
    public void clearEnchants() { // and tinkers
        for (PlayerSlot x: SLOT) {
            x.setEnchant(null);
            x.setTinker(null);
        }
    }
    
    public void clearUpgrades() { // and custom item levels
        for (PlayerSlot x: SLOT) {
            x._upgradeIndex = 0;
            x.setCustomItemLevel(0);
        }
    }
    
    public void clearProfs() {
        for (int i = 0; i < PROF.length; i++) {
            PROF[i].clear();
        }
        _profBits = 0;
    }
    public int getProfCount() {
        return Long.bitCount(_profBits);
    }    
    public boolean clearProf(ProfT prof) {
        if (prof == null) {
            return false;
        }
        int index = findProf(prof);
        if (index == -1) {
            return false;
        }
        PROF[index].clear();           
        return true;
    }
    public boolean hasProf(ProfT prof) {
        return findProf(prof) >= 0;
    }
    public int findProf(ProfT prof) {
        for (int i = 0; i < PROF.length; i++) {
            if (PROF[i]._prof == prof) {
                return i;
            }
        }        
        return -1;
    }
    public PlayerProf getProf(int index) { // save accessor
        if (index < 0 || index >= PROF.length) {
            throw new PlayerError("Invalid profession index: " + index);
        } 
        return PROF[index];
    }
    
    /*
    public void setProf(int index, ProfT prof, int level) {
        if (index < 0 || index >= PROF.length) {
            throw new PlayerError("Invalid profession index: " + index);
        } 
        PROF[index].setProf(prof);
        PROF[index].setLevel(level);
    }*/
  
    public void collectStats(StatMap stats) {
        for (PlayerSlot x: SLOT) {
            x.collectStats(stats);
        }
    }
    
    public boolean twoHandsRequired() {
        return _bothHandsForMH;
    }
    
    int uniqueCount(Unique unique, int ignoreSlotIndex, int ignoreGemIndex) {
        int count = 0;
        for (int i = 0; i < SLOT.length; i++) {
            if (ignoreGemIndex < 0 && i == ignoreSlotIndex) continue;
            Item item = SLOT[i]._item;
            if (item == null) continue;
            if (item.unique == unique) {
                count++;
            }     
            count += SLOT[i].uniqueGemCount(unique, ignoreGemIndex);            
        }        
        return count;
    }
    
    /*
    public boolean hasTwoHander() {
        Wearable temp = MH._socket;
        return temp != null && temp.allowedEquip.twoHand;
    }
    */
    
    
    
    public boolean canArmorSpecialization() {
        return spec != null && playerLevel >= SpecT.PLAYER_LEVEL_ARMOR_SPECIALIZATION;
    }
    
    public boolean hasArmorSpecialization() {
        if (!canArmorSpecialization()) {
            return false;            
        }
        for (PlayerSlot x: SLOT) {
            if (x.slotType.bodyPart == SlotT.BODYPART_ARMOR) {
                if (x.isEmpty() || x.getItem().type != spec.armorType) {
                    return false;
                }                
            }
        }        
        return true;
    }    
    
    public double getAverageItemLevel() {
        double sum = 0;
        int num = 0;
        for (PlayerSlot x: SLOT) {
            if (x.slotType.bodyPart != SlotT.BODYPART_COSMETIC && x.isValid()) {
                sum += x.getScaledItemLevel();
                num++;
            }
        }
        return sum / num;
    }
    
    // does not validate againt spec
    public Map<Integer,ItemSet> getItemSets() { // integer = ItemSet.id
        HashMap<Integer,ItemSet> map = new HashMap<>();
        for (PlayerSlot x: SLOT) {
            ItemSet set = x.getItemSet();
            if (set != null) {    
                map.put(set.id, set);
            }
        }
        return map;
    }
    
    // spec-specific bonuses if applicable
    public Map<Integer,SetBonus> getItemSetBonuses() { // integer = SetBonus.spellId
        HashMap<ItemSet,Integer> tally = new HashMap<>();
        for (PlayerSlot x: SLOT) {
            ItemSet set = x.getItemSet();
            if (set != null) {                                
                Integer old = tally.get(set);
                if (old == null) {
                    tally.put(set, 1);
                } else {
                    tally.put(set, old + 1);
                }                
            }
        }
        HashMap<Integer,SetBonus> map = new HashMap();
        tally.forEach((set, num) -> {
            SetBonus[] v = set.getBonuses(spec);
            if (v == null) {
                return;
            }
            for (SetBonus x: v) {
                if (num >= x.thres) {
                    map.put(x.spellId, x);
                }
            }
        });
        return map;        
    }
    
    public void validate() { validate(null, null); }
    public void validate(Player buf, Consumer<PlayerError> errors) {
        if (buf == null || buf == this) {
            buf = new Player();
        }
        buf.clear();
        buf.playerLevel = playerLevel;
        buf.spec = spec;
        buf.race = race;
        buf.copy(this, null); // export
        copySlots(buf, errors); // copy it back        
        // aww yiss
    }
    
}
