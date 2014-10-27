package com.antistupid.warkit.player.coders;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;
import com.antistupid.warbase.utils.Commented;
import com.antistupid.warbase.IntSet;
import com.antistupid.warbase.utils.LineError;
import com.antistupid.warbase.types.ClassT;
import com.antistupid.warbase.types.ProfT;
import com.antistupid.warbase.types.RaceT;
import com.antistupid.warbase.types.SlotT;
import com.antistupid.warbase.types.SpecT;
import com.antistupid.warbase.types.TypeT;
import com.antistupid.warkit.WarKit;
import com.antistupid.warkit.items.ItemEnchant;
import com.antistupid.warkit.items.Gem;
import com.antistupid.warkit.items.Item;
import com.antistupid.warkit.items.Wearable;
import com.antistupid.warkit.player.Player;
import com.antistupid.warkit.player.PlayerError;
import com.antistupid.warkit.player.PlayerProf;
import com.antistupid.warkit.player.PlayerSlot;

public class SimcProfile {

    /*
    level=100
    race=tauren
    role=attack
    professions=alchemy=609/engineering=604
    talents=2133212
    glyphs=stag/savage_roar/one_with_nature/ferocious_bite/aquatic_form/cat_form
    spec=feral

    head=living_wood_headpiece,id=115542,bonus_id=43/566
    neck=sabermaw_maulers_chain,id=119088
    shoulder=living_wood_spaulders,id=115544,bonus_id=566
    back=fenyu_fury_of_xuen,id=102248,upgrade=4,gem_id=76671,enchant_id=5302
    chest=living_wood_raiment,id=115540,bonus_id=566
    wrist=beastrider_wristwraps,id=114711,bonus_id=171
    hands=living_wood_grips,id=115541,bonus_id=566
    waist=beastrider_belt,id=114707,bonus_id=171
    legs=living_wood_legguards,id=115543,bonus_id=566
    feet=pandaren_roofsprinters,id=104586,upgrade=4,gem_id=76643,enchant_id=4428
    finger1=seal_of_rumbling_earth,id=119074
    finger2=sporebat_glowtail_loop,id=119071
    trinket1=haromms_talisman,id=104531
    trinket2=call_of_the_wolfmother,id=118246,bonus_id=171
    main_hand=hellscreams_pig_sticker,id=105686,gem_id=76670/76670,enchant_id=4444
    */
    
    static public String race(RaceT race) {
        return race.name.toLowerCase();
    }
    
    static public String spec(SpecT spec) {
        return spec.name.toLowerCase();
    }
    
    static public String underscore(String name) {         
        char[] buf = name.toCharArray();
        int len = 0;
        for (int i = 0; i < buf.length; i++) {
            char ch = buf[i];
            if (ch >= 0x80) {
                continue;
            } 
            ch = Character.toLowerCase(ch);
            if (ch >= 'a' && ch <= 'z') {
                // alpha
            } else if (ch >= '0' && ch <= '9') {
                // digit
            } else if (ch == ' ') {
                ch = '_';
            } else if (ch == '_' || ch == '+' || ch == '.' || ch == '%') {
                // weird shit
            } else {
                continue;
            }
            buf[len++] = ch;
        }
        return new String(buf, 0, len);
    }
    
    static class R<T extends TypeT> {
        final HashMap<T,String> aMap = new HashMap<>();
        final TreeMap<String,T> bMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        void add(T a, String... b) {
            aMap.put(a, b[0]); // first is gud
            for (String x: b) {
                bMap.put(x, a);
            }
        }
        void add(T a) {
            add(a, underscore(a.name));
        }
        T get(String b) {
            return bMap.get(b);
        }
        String get(T a) {
            return aMap.get(a);
        }
    }
    
    /*
    static private boolean ignoreSlot(String name) {
        return name.equalsIgnoreCase("shirt") || name.equalsIgnoreCase("tabard");                        
    }
    */
    
    static private final R<SlotT> SLOTS = new R<>();
    static {
        SLOTS.add(SlotT.HEAD,       "head");
        SLOTS.add(SlotT.NECK,       "neck");
        SLOTS.add(SlotT.SHOULDER,   "shoulders", "shoulder");
        SLOTS.add(SlotT.BACK,       "back");
        SLOTS.add(SlotT.CHEST,      "chest");
        SLOTS.add(SlotT.WRIST,      "wrists", "wrist");
        SLOTS.add(SlotT.HANDS,      "hands");
        SLOTS.add(SlotT.WAIST,      "waist");
        SLOTS.add(SlotT.LEGS,       "legs");
        SLOTS.add(SlotT.FEET,       "feet");
        SLOTS.add(SlotT.TRINKET_1,  "trinket1");
        SLOTS.add(SlotT.TRINKET_2,  "trinket2");
        SLOTS.add(SlotT.FINGER_1,   "finger1");
        SLOTS.add(SlotT.FINGER_2,   "finger2");
        SLOTS.add(SlotT.MAIN_HAND,  "main_hand");
        SLOTS.add(SlotT.OFF_HAND,   "off_hand");   
    }
    
    static private final String unknownClass = "unknown";
    static private final R<ClassT> CLASSES = new R<>();
    static {
        CLASSES.add(ClassT.DK, "deathknight");
        CLASSES.add(ClassT.DRUID);
        CLASSES.add(ClassT.HUNTER);
        CLASSES.add(ClassT.MAGE);
        CLASSES.add(ClassT.MONK);
        CLASSES.add(ClassT.PALADIN);
        CLASSES.add(ClassT.PRIEST);
        CLASSES.add(ClassT.ROGUE);
        CLASSES.add(ClassT.SHAMAN);
        CLASSES.add(ClassT.LOCK);
        CLASSES.add(ClassT.WAR);
    }
    
    static private final R<RaceT> RACES = new R<>();
    static private final String RACE_PANDA = "pandaren";
    static {
        RACES.add(RaceT.BE);
        RACES.add(RaceT.DRAENEI);
        RACES.add(RaceT.DWARF);
        RACES.add(RaceT.GNOME);
        RACES.add(RaceT.GOBLIN);
        RACES.add(RaceT.HUMAN);
        RACES.add(RaceT.NE);
        RACES.add(RaceT.ORC);
        //RACES.add(RaceT.PANDAREN_A);
        //RACES.add(RaceT.PANDAREN_H);
        //RACES.add(RaceT.PANDAREN_N);
        RACES.add(RaceT.TAUREN);
        RACES.add(RaceT.TROLL);
        RACES.add(RaceT.UNDEAD);
        RACES.add(RaceT.WORGEN);        
    }
    
    
    
    static public ArrayList<LineError> fromString(String code, Player p, WarKit wk) {
        return fromLines(Arrays.asList(code.split("\n")), p, wk);
    }
    
    static public ArrayList<LineError> fromFile(Path file, Player p, WarKit wk) {
        try {
            return fromLines(Files.readAllLines(file), p, wk);
        } catch (IOException err) {
            throw new UncheckedIOException("Error reading Simc Profile from " + file, err);
        }        
    }
        
    static public ArrayList<LineError> fromLines(Collection<String> lines, Player p, WarKit wk) {
        p.clearSlots();
        ArrayList<LineError> errors = new ArrayList<>();
        int lineno = 0;
        //boolean first = true;
        //IntSet bonuses = new IntSet();
        //ArrayList<Gem> gemBuf = new ArrayList();
        IntSet bonuses = new IntSet();
        boolean guessPanda = false;
        ClassT cls = null;
        int state = 0;
        nextLine: for (String line0: lines) {
            ++lineno;
            String line = Commented.strip(line0);
            if (line == null) {
                continue;
            }
            int pos = line.indexOf('=');
            if (pos == -1) {
                errors.add(new LineError(lineno, line0, "Missing [=]"));
                continue;
            }
            String key = line.substring(0, pos).trim().toLowerCase();
            String rest = line.substring(pos + 1).trim();
            if (state == 0) {
                if (key.equalsIgnoreCase(unknownClass)) {
                    state = 1;
                    continue;
                }
                cls = ClassT.names.resolve(key);
                if (cls == null) {
                    errors.add(new LineError(lineno, line0, "Unknown Class"));
                    break;
                }
                state = 1;
                continue;
            } 
            if (state == 1) {
                switch (key) {
                    case "level": {
                        int level;
                        try {
                            level = Integer.parseInt(rest);
                        } catch (NumberFormatException err) {
                            errors.add(new LineError(lineno, line0, "Not an integer"));
                            continue;
                        }
                        if (level < 1 || level > Player.MAX_PLAYER_LEVEL) {
                            errors.add(new LineError(lineno, line0, "Invalid Level: " + level));
                            continue;
                        }
                        p.playerLevel = level;
                        continue;
                    }
                    case "spec": {
                        ClassT cls_ = cls;
                        SpecT spec = SpecT.names.resolve(rest, cls == null ? null : x -> x.classType == cls_);
                        if (spec == null) {
                            errors.add(new LineError(lineno, line0, "Unknown Spec"));
                            continue;
                        }
                        cls = spec.classType; // since we might not have class specified
                        p.spec = spec;
                        continue;
                    }
                    case "race": {
                        RaceT race = null;
                        if (rest.equalsIgnoreCase(RACE_PANDA)) {
                            pos = line0.indexOf('#'); // look for hint
                            if (pos != 0) {
                                if (line0.indexOf("horde", pos) != -1) {
                                    race = RaceT.PANDAREN_H;                                    
                                } else if (line0.indexOf("alliance", pos) != -1) {
                                    race = RaceT.PANDAREN_A;
                                } else if (line0.indexOf("neutral", pos) != -1) {
                                    race = RaceT.PANDAREN_N;
                                }
                            }
                            if (race == null) {
                                guessPanda = true;
                                continue;
                            }
                        } else {
                            race = RACES.get(rest);
                        }
                        if (race == null) {
                            errors.add(new LineError(lineno, line0, "Unknown Race"));
                            continue;
                        }
                        if (cls != null && !cls.canBe(race)) {
                            errors.add(new LineError(lineno, line0, cls + " cannot be " + race));
                            continue;
                        }                       
                        p.race = race;
                        guessPanda = false;
                        continue;                        
                    }
                    case "role":    
                    case "position":
                    case "glyphs":
                    case "talents":
                        continue;
                    case "professions": {    
                        //engineering=600/enchanting=600
                        int profIndex = 0;
                        p.clearProfs(); // unneeded
                        for (String comp: rest.split("/")) {
                            pos = comp.indexOf('=');
                            String left = comp.substring(0, pos).trim();
                            String right = comp.substring(pos + 1).trim();                            
                            ProfT prof = ProfT.names.resolve(left, xx -> xx.primary);
                            if (prof == null) {
                                errors.add(new LineError(lineno, line0, String.format("Unknown Profession: \"%s\"", left)));
                                continue;
                            }
                            int level;
                            try {
                                level = Integer.parseInt(right);
                            } catch (NumberFormatException err) {
                                errors.add(new LineError(lineno, line0, String.format("Illegal Profession Level: \"%s\"", comp)));
                                continue;                                
                            }
                            try {
                                p.getProf(profIndex++).setProf(prof, level);
                            } catch (PlayerError err) {
                                errors.add(new LineError(lineno, line0, err.getMessage()));
                            }                             
                        }
                        continue;
                    }
                }
                if (key.startsWith("action")) {
                    continue;
                }                
                if (SLOTS.get(key) == null) { // && !ignoreSlot(key)) {                    
                    errors.add(new LineError(lineno, line0, "Unknown option: "  + key));
                    continue;
                }                   
                if (p.spec == null) {
                    errors.add(new LineError(-1, null, "Specialization not specified"));
                    break;
                }              
                state = 2;                
            }
            /*if (ignoreSlot(key)) {
                continue;
            }*/
            SlotT slotType = SLOTS.get(key);
            if (slotType == null) {
                errors.add(new LineError(lineno, line0, "Unknown slot: "  + key));
                continue;
            }
            int itemId = 0;
            int upgrade = 0;
            int enchantId = 0;
            String enchantName = null;
            Gem[] gems = null;
            bonuses.clear();
            String[] parts = rest.split(",");
            for (int i = 1; i < parts.length; i++) {
                String part = parts[i];
                pos = part.indexOf('=');
                if (pos == -1) {
                    errors.add(new LineError(lineno, line0, String.format("Missing [=] \"%s\"", part)));
                    continue;
                }
                key = part.substring(0, pos).trim().toLowerCase();
                rest = part.substring(pos + 1).trim();
                switch (key) {
                    case "id": {
                        try {
                            itemId = Integer.parseInt(rest);
                        } catch (NumberFormatException err) {
                            errors.add(new LineError(lineno, line0, String.format("Invalid Item ID: \"%s\"", rest)));
                            continue nextLine;
                        }
                        break;                        
                    }
                    case "upgrade": {
                        try {
                            upgrade = Integer.parseInt(rest);
                        } catch (NumberFormatException err) {
                            errors.add(new LineError(lineno, line0, String.format("Invalid Upgrade Level: \"%s\"", rest)));
                        }
                        break;
                    }
                    case "enchant": {
                        enchantName = rest;
                        break;
                    }
                    case "enchant_id": {
                        try {
                            enchantId = Integer.parseInt(rest);
                        } catch (NumberFormatException err) {
                            errors.add(new LineError(lineno, line0, String.format("Invalid Enchant ID: \"%s\"", rest)));
                        }
                        break;                        
                    }
                    case "bonus_id": {                        
                        bonuses.clear();
                        for (String x: rest.split("/")) {
                            x = x.trim();
                            if (x.isEmpty()) continue;
                            int id;
                            try {
                                id = Integer.parseInt(x);
                            } catch (NumberFormatException err) {
                                errors.add(new LineError(lineno, line0, String.format("Invalid Bonus ID: \"%s\"", x)));
                                continue;
                            }
                            bonuses.add(id);                            
                        }                        
                        break;
                    }
                    case "gem_id": {
                        String[] v = rest.split("/");
                        gems = new Gem[v.length];
                        for (int j = 0; j < v.length; j++) {
                            int gemId;
                            String x = v[j].trim();
                            try {
                                gemId = Integer.parseInt(x);
                            } catch (NumberFormatException err) {
                                errors.add(new LineError(lineno, line0, String.format("Invalid Gem ID: \"%s\"", x)));
                                continue;
                            }       
                            if (gemId == 0) {
                                continue;                                
                            }
                            Gem gem = wk.gemMap.get(gemId);
                            if (gem == null) {
                                errors.add(new LineError(lineno, line0, String.format("Invalid Gem ID: \"%s\"", x)));
                                continue;
                            }
                            gems[j] = gem;                               
                        }
                    }
                }
                    
            }      
            if (itemId == 0) {
                errors.add(new LineError(lineno, line0, "Missing Item ID"));
                continue nextLine;
            }
            Wearable item = wk.wearableMap.get(itemId);
            if (item == null) {
                errors.add(new LineError(lineno, line0, "Unknown Item ID: " + itemId));
                continue;
            }
            if (guessPanda) {
                RaceT panda = RaceT.resolvePandarenFaction(item.reqRace);
                if (panda != null) {
                    p.race = panda;
                    guessPanda = false;
                    // revalidate all of the gear we have so far
                    p.validate(null, e -> errors.add(new LineError(e.getMessage())));
                }                
            }
            PlayerSlot slot =  p.SLOT[slotType.index];
            try {
                slot.setItem(item);
            } catch (PlayerError err) {
                errors.add(new LineError(lineno, line0, err.getMessage()));
                continue; // fatal
            }            
            try {
                bonuses = wk.repairItemBonuses(item, bonuses); // since the ids are often wrong               
                slot.setItemBonuses(bonuses);
            } catch (PlayerError err) {
                errors.add(new LineError(lineno, line0, err.getMessage()));
            }            
            if (!bonuses.isEmpty()) {
                errors.add(new LineError(lineno, line0, "Unexpected Item Bonuses: " + bonuses));
            }         
            try {
                ItemEnchant e = null;
                if (enchantId != 0) {
                    e = wk.findEnchant(item, enchantId);
                    if (e == null) {
                        errors.add(new LineError(lineno, line0, "Unknown Enchant ID: " + enchantId));
                    }
                } else if (enchantName != null) {                    
                    for (ItemEnchant x: wk.getEnchantUniverse(item).values()) {
                        if (underscore(x.name).equals(enchantName)) {
                            e = x;
                            break;
                        }                        
                    }
                    if (e == null) {
                        errors.add(new LineError(lineno, line0, String.format("Unknown Enchant Name: \"%s\"", enchantName)));
                    }
                }
                slot.setEnchant(e);                                                
            } catch (PlayerError err) {
                errors.add(new LineError(lineno, line0, err.getMessage()));
            }   
            try {
                slot.setUpgradeLevel(upgrade);
            } catch (PlayerError err) {
                errors.add(new LineError(lineno, line0, err.getMessage()));
            }
            if (gems != null) {
                int socketCount = slot.getSocketCount();
                if (gems.length > socketCount && gems[socketCount] != null) {
                    slot.setExtraSocket(true); // hack this i guess
                }                
                for (int i = 0; i < gems.length; i++) {
                    Gem gem = gems[i];
                    if (gem != null) {
                        try {
                            slot.getSocket(i).setGem(gem);
                        } catch (PlayerError err) {
                            errors.add(new LineError(lineno, line0, err.getMessage()));
                        }
                    }                    
                }
            }
            
        }
        return errors;
    }
    
    static public String toString(Player p, String name) {
        StringBuilder sb = new StringBuilder();
        
        if (name != null) {
            sb.append(p.spec == null ? unknownClass : CLASSES.get(p.spec.classType));
            sb.append("=");
            sb.append("\"");
            sb.append(name);
            sb.append("\"");
            sb.append("\n");
            
            sb.append("level=").append(p.playerLevel).append("\n");
            
            if (p.race != null) {
                sb.append("race=");
                if (p.race == RaceT.PANDAREN_A) {
                    sb.append(RACE_PANDA);
                    sb.append("#alliance");
                } else if (p.race == RaceT.PANDAREN_H) {
                    sb.append(RACE_PANDA);
                    sb.append("#horde");                
                } else if (p.race == RaceT.PANDAREN_N) {
                    sb.append(RACE_PANDA);
                    sb.append("#neutral");
                } else {
                    sb.append(RACES.get(p.race));
                }            
                sb.append("\n");
            }
            
            int profCount = 0;
            for (PlayerProf x: p.PROF) {
                if (x.hasProf()) {
                    if (profCount++ == 0) {
                        sb.append("professions=");
                    } else {
                        sb.append("/");
                    }
                    sb.append(underscore(x.getProf().name));
                    sb.append("=");
                    sb.append(x.getLevel());
                }
            }            
            if (profCount > 0) {
                sb.append("\n");
            }
            
            if (p.spec != null) {
                sb.append("spec=");
                sb.append(underscore(p.spec.name));
                sb.append("\n");
            }
            
        }
        IntSet setBuf = new IntSet();
        for (PlayerSlot x: p.SLOT) {
            if (x.isEmpty()) {
                continue;
            }
            String slotName = SLOTS.get(x.slotType);
            if (slotName == null) {
                continue;
            }
            Wearable item = x.getItem();
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(slotName);
            sb.append("=");
            sb.append(underscore(item.name));
            sb.append(",id=");
            sb.append(item.itemId);
            setBuf.clear();
            x.collectItemBonuses(setBuf);
            int n = setBuf.size();
            if (n > 0) {
                sb.append(",bonus_id=");
                sb.append(setBuf.keys[0]);
                for (int i = 1; i < n; i++) {
                    sb.append("/");
                    sb.append(setBuf.keys[i]);
                }                
            }
            if (x.getGemCount() > 0) {
                for (int i = 0; i < x.getSocketCount(); i++) {
                    Gem gem = x.getSocket(i).getGem();
                    if (i == 0) {
                        sb.append(",gem_id=");
                    } else {
                        sb.append("/");
                    }
                    if (gem == null) {
                        sb.append("0");
                    } else {
                        sb.append(gem.itemId);
                    }
                }
            }
            int up = x.getUpgradeLevel();
            if (up > 0) {
                sb.append(",upgrade=");
                sb.append(up);
            }
            ItemEnchant ench = x.getEnchant();
            if (ench != null) {
                sb.append(",enchant_id");
                sb.append(ench.enchantment.id);
            }
        }        
        return sb.toString();
    }
    
}
