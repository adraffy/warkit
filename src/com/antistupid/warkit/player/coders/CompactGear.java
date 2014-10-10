package com.antistupid.warkit.player.coders;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import com.antistupid.warbase.utils.Commented;
import com.antistupid.warbase.IntSet;
import com.antistupid.warbase.utils.LineError;
import com.antistupid.warbase.structs.Pair;
import com.antistupid.warbase.utils.StringBuilderHelp;
import com.antistupid.warbase.types.ClassT;
import com.antistupid.warbase.types.EquipT;
import com.antistupid.warbase.types.ProfT;
import com.antistupid.warbase.types.RaceT;
import com.antistupid.warbase.types.SlotT;
import com.antistupid.warbase.types.SpecT;
import com.antistupid.warkit.WarKit;
import com.antistupid.warkit.items.AbstractEnchant;
import com.antistupid.warkit.items.Gem;
import com.antistupid.warkit.items.Item;
import com.antistupid.warkit.items.Wearable;
import com.antistupid.warkit.player.Player;
import com.antistupid.warkit.player.PlayerError;
import com.antistupid.warkit.player.PlayerProf;
import com.antistupid.warkit.player.PlayerSlot;
import com.antistupid.warkit.player.PlayerSocket;

public class CompactGear {

    /*
    @FunctionalInterface
    static public interface ParseError {
        void error(int lineno, String line, String error);
    }
    */
    
    static boolean parseProf(Player p, int profIndex, ProfT prof, String comp, String right, Consumer<String> errorHandler) {
        int level;
        if (right == null) {
            level = Player.MAX_PROF_LEVEL;
        } else {
            try {   
                level = Integer.parseInt(right);
            } catch (NumberFormatException err) {
                errorHandler.accept("Profession level not an integer: " + comp);
                return false;
            }   
        }
        if (profIndex >= Player.MAX_PROF) {
            errorHandler.accept("Too many professions: " + comp);
        }
        try {
            p.setProf(profIndex, prof, level);
            return true;
        } catch (PlayerError err) {
            errorHandler.accept(err.getMessage());
            return false;
        }
    }
    
    static void parseHeader(Player p, String line, Consumer<String> errorHandler) {
        line = line.trim();
        if (line.isEmpty()) {
            return;
        }
        int level = 0;
        SpecT spec = null;
        RaceT race = null;
        int profIndex = 0;        
        next: for (String comp: line.split(",")) {
            comp = comp.trim();
            int pos = comp.indexOf('/');
            String left, right;
            if (pos >= 0) {
                left = comp.substring(0, pos).trim();
                right = comp.substring(pos + 1).trim();
            } else {
                left = comp;
                right = null;
            }
            if (left.isEmpty()) {
                if (right != null) {
                    errorHandler.accept(String.format("Invalid Option: \"%s\"", comp));            
                }
                continue;
            }
            // firstMatch try get
            if (level == 0 && right == null && Character.isDigit(left.charAt(0))) {
                try {
                    level = Integer.parseInt(left);
                } catch (NumberFormatException err) {
                    level = -1;
                }            
                if (level < 1 || level > Player.MAX_PLAYER_LEVEL) {
                    errorHandler.accept(String.format("Invalid Player Level: \"%s\"", comp));  
                    level = 0;
                }
                continue;                
            }
            if (spec == null) {
                if (right == null) {
                    spec = SpecT.names.get(comp);
                    if (spec != null) {
                        continue;
                    }   
                } else {
                    ClassT cls = ClassT.names.get(left);
                    if (cls != null) {
                        for (SpecT x: cls.specs) {
                            if (x.name.equalsIgnoreCase(right)) {
                                spec = x;
                                continue next;
                            }
                        }                  
                    }  
                }             
            }
            if (race == null) {
                if (spec == null) {
                    race = RaceT.names.get(left);
                    if (race != null) {
                        continue;
                    } 
                } else {
                    for (RaceT x: spec.classType.races) {
                        if (x.name.equalsIgnoreCase(right)) {
                            race = x;
                            continue next;
                        }
                    }  
                }                
            }
            ProfT prof = ProfT.names.get(left);
            if (prof != null) {                
                if (parseProf(p, profIndex, prof, comp, right, errorHandler)) {
                    profIndex++;
                }
                continue;
            }
            // sloppy mode
            if (spec == null) { 
                if (right == null) { // "Feral"
                    spec = SpecT.names.resolve(left);                    
                } else { // "Druid/Feral"
                    ClassT cls = ClassT.names.resolve(left);
                    if (cls != null) {
                        spec = SpecT.names.resolve(right, x -> x.classType == cls);
                    }                    
                }
                if (spec != null) {
                    continue;
                }
            }
            if (race == null) {
                if (spec == null) {
                    race = RaceT.names.resolve(left);
                } else {
                    ClassT cls = spec.classType;
                    race = RaceT.names.resolve(left, x -> cls.canBe(x));
                }            
                if (race != null) {
                    continue;
                }
            }    
            prof = ProfT.names.resolve(left);
            if (prof != null) {             
                if (parseProf(p, profIndex, prof, comp, right, errorHandler)) {
                    profIndex++;
                }
                continue;                
            }
            errorHandler.accept(String.format("Unknown Option: \"%s\"", comp));            
        }
        if (spec != null && race != null && !spec.classType.canBe(race)) {            
            errorHandler.accept(String.format("%s cannot be %s", spec, race));
            race = spec.classType.races.get(0);
        }
        p.playerLevel = level == 0 ? Player.MAX_PLAYER_LEVEL : level;
        p.spec = spec;
        p.race = race;
    }
 
    static public ArrayList<LineError> fromString(String code, Player p, WarKit wk) {
        return fromLines(Arrays.asList(code.split("\n")), p, wk);
    }
    
    static public ArrayList<LineError> fromFile(Path file, Player p, WarKit wk) {
        try {
            return fromLines(Files.readAllLines(file), p, wk);
        } catch (IOException err) {
            throw new UncheckedIOException("Error reading CompactGear from " + file, err);
        }        
    }
        
    static public ArrayList<LineError> fromLines(Collection<String> lines, Player p, WarKit wk) {
        p.clearSlots();
        ArrayList<LineError> errors = new ArrayList<>();
        int lineno = 0;
        boolean first = true;
        IntSet bonuses = new IntSet();
        ArrayList<Gem> gemBuf = new ArrayList();
        for (String line0: lines) {
            ++lineno;
            String line = Commented.strip(line0);
            if (line == null) {
                continue;
            }
            if (first) { 
                first = false;   
                if (line.startsWith("?")) { // header
                    p.spec = null;
                    p.race = null;
                    p.clearProfs();
                    int lineno_ = lineno; // fucking christ java
                    parseHeader(p, line.substring(1), x -> errors.add(new LineError(lineno_, line0, "Header: " + x)));    
                    continue;
                }                    
            }
            String left, right;
            int pos = line.indexOf(' ');
            if (pos >= 0) {
                left = line.substring(0, pos).trim();
                right = line.substring(pos + 1).trim();                
            } else {
                left = line;
                right = null;
            }
            int itemId;
            try {
                itemId = Integer.parseInt(left);
            } catch (NumberFormatException err) {
                errors.add(new LineError(lineno, line0, "Item ID not an integer: \"" + left + "\""));
                continue;
            } 
            Wearable item = wk.wearableMap.get(itemId);
            if (item == null) {
                errors.add(new LineError(lineno, line0, "Unknown Item ID: " + itemId));
                continue;
            }/*
            if (!(item instanceof Wearable)) {
                errors.add(new LineError(lineno, line0, item.getNameAndId() + " is not wearable"));
                continue;
            }*/
            PlayerSlot slot = p.findSlot(item.equip, true);
            if (slot == null) {
                errors.add(new LineError(lineno, line0, "No free slot for " + item.getNameAndId() + " (" + item.equip + ")"));
                continue;
            }
            try {
                slot.setItem(item);
            } catch (PlayerError err) {
                errors.add(new LineError(lineno, line0, err.getMessage()));
                continue;
            }
            if (right != null) {
                bonuses.clear();
                gemBuf.clear();
                for (String comp: right.split("\\s+")) {
                    switch (comp.charAt(0)) {
                        case '!': { // item bonuses
                            for (String x: comp.substring(1).split("/")) {
                                int bonusId;
                                try {
                                    bonusId = Integer.parseInt(x);
                                } catch (NumberFormatException err) {
                                    errors.add(new LineError(lineno, line0, "Invalid Item Bonus: \"" + x + "\""));
                                    continue;  
                                }
                                if (!wk.itemBonusMap.containsKey(bonusId)) {
                                    errors.add(new LineError(lineno, line0, "Unknown Item Bonus Id: " + bonusId));
                                    continue;                                     
                                }
                                bonuses.add(bonusId);                                
                            }
                            break;                            
                        }    
                        case ':': { // socket
                            if (comp.length() == 1) {
                                gemBuf.add(null); // isEmpty gem
                                break;
                            }
                            int gemId;
                            try {
                                gemId = Integer.parseInt(comp.substring(1));
                            } catch (NumberFormatException err) {
                                errors.add(new LineError(lineno, line0, "Invalid Gem Id: " + comp));
                                continue;  
                            }    
                            Gem gem = wk.gemMap.get(gemId);
                            if (gem == null) {
                                errors.add(new LineError(lineno, line0, "Unknown Gem Id: " + gemId));
                                continue;  
                            }
                            gemBuf.add(gem);                            
                            break;
                        }
                        case '-': {
                            int delta;
                            try {
                                delta = Integer.parseInt(comp);
                            } catch (NumberFormatException err) {
                                errors.add(new LineError(lineno, line0, "Invalid Upgrade or Item Level Delta: " + comp));
                                continue;  
                            }                               
                            try {
                                slot.setUpgradeLevelOrDelta(delta);
                            } catch (PlayerError err) {
                                errors.add(new LineError(lineno, line0, err.getMessage()));
                                continue;
                            }
                            break;
                        }
                        case '+': { // effective upgrade or delta
                            if (comp.equals("++")) {
                                try {
                                    slot.setUpgradeLevelMax();
                                } catch (PlayerError err) {
                                    errors.add(new LineError(lineno, line0, err.getMessage()));
                                    continue;
                                }
                            } else {
                                int delta;
                                try {
                                    delta = Integer.parseInt(comp.substring(1));
                                } catch (NumberFormatException err) {
                                    errors.add(new LineError(lineno, line0, "Invalid Upgrade or Item Level Delta: " + comp));
                                    continue;  
                                }                               
                                try {
                                    slot.setUpgradeLevelOrDelta(delta);
                                } catch (PlayerError err) {
                                    errors.add(new LineError(lineno, line0, err.getMessage()));
                                    continue;
                                }
                            }
                            break;
                        }
                        case 'u': { // upgrade
                            int index;
                            try {
                                index = Integer.parseInt(comp.substring(1)); //.substring(1));
                            } catch (NumberFormatException err) {
                                errors.add(new LineError(lineno, line0, "Invalid Upgrade Index: " + comp));
                                continue;  
                            }   
                            try {
                                slot.setUpgradeLevel(index);
                            } catch (PlayerError err) {
                                errors.add(new LineError(lineno, line0, err.getMessage()));
                                continue;
                            }
                            break;                            
                        }
                        case 'i': { // custom item level
                            int ilvl;
                            try {
                                ilvl = Integer.parseInt(comp.substring(1));
                            } catch (NumberFormatException err) {
                                errors.add(new LineError(lineno, line0, "Invalid Custom Item Level: " + comp));
                                continue;  
                            }   
                            slot.setCustomItemLevel(ilvl);
                            break;
                        }
                        case '$': { // enchant
                            String rest = comp.substring(1);
                            if (rest.isEmpty()) {
                                errors.add(new LineError(lineno, line0, "Empty Enchant"));
                                continue;
                            }                            
                            if (rest.equals(":")) {
                                slot.setExtraSocket(true);
                                continue;
                            } 
                            int enchantId;
                            try {
                                enchantId = Integer.parseInt(rest);
                            } catch (NumberFormatException err) {
                                errors.add(new LineError(lineno, line0, String.format("Invalid Enchant Id: \"%s\"", comp)));
                                continue;  
                            }
                            AbstractEnchant enchant = wk.findEnchant(item, enchantId);
                            if (enchant == null) {
                                errors.add(new LineError(lineno, line0, "Unknown Enchant Id: " + enchantId));
                                continue;
                            }
                            try {
                                slot.setEnchant(enchant);                            
                            } catch (PlayerError err) {
                                errors.add(new LineError(lineno, line0, err.getMessage()));
                                continue;  
                            }
                            break;
                        }
                        default: {
                            errors.add(new LineError(lineno, line0, "Unknown Item Modifier: " + comp));
                        }                            
                    }                    
                }     
                try {
                    bonuses = wk.repairItemBonuses(item, bonuses);
                    slot.setItemBonuses(bonuses);
                } catch (PlayerError err) {
                    errors.add(new LineError(lineno, line0, err.getMessage()));
                }            
                if (!bonuses.isEmpty()) {
                    errors.add(new LineError(lineno, line0, "Unexpected Item Bonuses: " + bonuses));
                }
                for (int i = 0; i < gemBuf.size(); i++) {
                    Gem gem = gemBuf.get(i);
                    PlayerSocket socket = slot.getSocket(i);
                    if (socket == null) {
                        errors.add(new LineError(lineno, line0, String.format("No socket for gem[%d]: %s", i + 1, gem)));
                        continue;
                    }
                    try {
                        socket.setGem(gem);
                    } catch (PlayerError err) {
                        errors.add(new LineError(lineno, line0, err.getMessage()));
                    }
                }
            }
        }
        errors.forEach(x -> System.out.println(x.lineno + ": " + x.error));
        return errors;
    }
    
    static public String toString(Player p) {
        StringBuilder sb = new StringBuilder(256);
        ArrayList<Pair<String,PlayerSlot>> list = new ArrayList();        
        IntSet bonuses = new IntSet();
        for (PlayerSlot slot: p.SLOT) {
            Wearable item = slot.getItem();
            if (item == null) {
                continue;
            }
            sb.setLength(0);
            sb.append(item.itemId);   
            if (slot.isItemLevelCustom()) {
                sb.append(" i");
                sb.append(slot.getActualItemLevel());
            } else {
                int up = slot.getUpgradeItemLevelDelta();
                if (up != 0) {
                    sb.append(" +");
                    sb.append(up);
                }
            }       
            if (slot.getExtraSocket()) {
                sb.append(" $:");
            }
            AbstractEnchant enchant = slot.getEnchant();
            if (enchant != null) {
                sb.append(" $");
                sb.append(enchant.enchantment.id);
            }            
            int num = slot.getSocketCount();
            if (num > 0) {
                for (int i = 0; i < num; i++) {
                    sb.append(" :");
                    PlayerSocket socket = slot.getSocket(i);
                    Gem gem = socket.getGem();
                    if (gem != null) {
                        sb.append(gem.itemId);
                    }                
                }
            }
            bonuses.clear();
            slot.getItemBonuses(bonuses);
            if (!bonuses.isEmpty()) {
                sb.append(" !");
                sb.append(bonuses.keys[0]);
                for (int i = 1; i < bonuses.size(); i++) {
                    sb.append("/");
                    sb.append(bonuses.keys[i]);
                }
            }
            
            list.add(new Pair<>(sb.toString(), slot));
        }
        int max = 0;
        for (Pair<String,PlayerSlot> x: list) {
            max = Math.max(max, x.a.length());
        }        
        //ArrayList<String> things = new ArrayList<>();        
        sb.setLength(0);
        sb.append("?");
        String sep = ", ";
        sb.append(p.playerLevel);
        if (p.spec != null) {
            sb.append(sep);
            sb.append(p.spec.classType.name);
            sb.append("/");
            sb.append(p.spec.name);
        }
        if (p.race != null) {
            sb.append(sep);
            sb.append(p.race.name);
        }
        for (PlayerProf x: p.PROF) {
            if (x.hasProf()) {
                sb.append(sep);
                sb.append(x.getProf().tinyName);
                sb.append("/");
                sb.append(x.getLevel());
            }            
        }
        /*
        if (!things.isEmpty()) {
            sb.append("?");                
            Iterator<String> iter = things.iterator();            
            sb.append(iter.next());
            while (iter.hasNext()) {
                sb.append(", ");
                sb.append(iter.next());
            }
        }*/
        for (Pair<String,PlayerSlot> x: list) {
            /*if (sb.length() > 0) {
                sb.append("\n");
            }*/
            sb.append('\n');
            StringBuilderHelp.padRightOrTrim(sb, x.a, max);
            sb.append(" # ");
            sb.append(x.b.getItemName(true, true, true));   
        }      
        return sb.toString();
    }
    
}
