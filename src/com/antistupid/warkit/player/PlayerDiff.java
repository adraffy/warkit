package com.antistupid.warkit.player;

import java.util.ArrayList;
import java.util.Collection;
import com.antistupid.warkit.items.RandomSuffix;
import com.antistupid.warbase.stats.StatMap;
import com.antistupid.warbase.utils.StringBuilderHelp;
import com.antistupid.warbase.types.RaceT;
import com.antistupid.warbase.types.SlotT;
import com.antistupid.warbase.types.SpecT;
import com.antistupid.warbase.types.StatT;
import com.antistupid.warkit.items.ItemEnchant;
import com.antistupid.warkit.items.Wearable;
import com.antistupid.warkit.player.coders.CompactGear;

public class PlayerDiff {
    
    static public final PlayerDiff SAME = new PlayerDiff(false, "Identical Profiles", "Both profiles are the same.");
    
    public final boolean diff;
    public final String title;
    public final String message;
    
    private PlayerDiff(boolean diff, String title, String message) {
        this.diff = diff;
        this.title = title;
        this.message = message;
    }
        
    static String formatScaled(int scaled) {
        if (scaled > 0) {
            return String.format("To %d", scaled);
        } else if (scaled < 0) {
            return String.format("Down-to %d", -scaled);
        } else {
            return null;
        }
    }
    
    static String formatUpgrade(PlayerSlot slot) {
        if (slot.isItemLevelScaled()) {
            return String.format("%d (scaled)", slot.getScaledItemLevel());
        }   
        /*
        String suffix = slot.item.nameDesc;
        if (suffix == null) {
            suffix = "Normal";
        }        
        if (itemLevel == itemLevel0) {
            return String.format("%d <%s>", itemLevel, suffix);
        } else {
            return String.format("%d <%s> (%+d)", itemLevel, suffix, itemLevel - itemLevel0);
        }*/
        return "";
    }
    
    static String orNone(Object obj) {
        return obj == null ? "None" : obj.toString();
    }
    
    static String getName(Object obj) {
        if (obj instanceof PlayerSlot) {
            return orNone(((PlayerSlot)obj).getItemName(false, false, true));            
        } else if (obj instanceof PlayerSocket) {
            return ((PlayerSocket)obj).getGemName(false);
        } else if (obj instanceof StatMap) {
            return ((StatMap)obj).toString_noBracket(true); // this has brackets :|
        } else if (obj instanceof RandomSuffix) {
            return ((RandomSuffix)obj).name;
        } else if (obj instanceof ItemEnchant) {
            return ((ItemEnchant)obj).name;
        } else if (obj instanceof SpecT) {
            return ((SpecT)obj).fullName;
        } else if (obj instanceof RaceT) {
            return ((RaceT)obj).name;
        } else {
            return orNone(obj);
        }        
    }

    static String[] diff(String desc, Object left, Object right) {
        return new String[]{desc, getName(left), getName(right)};
    }
    
    static void render(StringBuilder sb, String heading, Collection<String[]> rows, int[] max) {
        sb.append(heading);
        sb.append(": ");
        StringBuilderHelp.plural(sb, rows.size(), " ", "difference", "s", true);
        sb.append('\n');
        for (String[] row: rows) {
            StringBuilderHelp.padLeftOrTrim(sb, row[0], max[0]);
            sb.append(": ");
            StringBuilderHelp.padRightOrTrim(sb, row[1], max[1]);
            sb.append(" => ");
            StringBuilderHelp.trim(sb, row[2], max[2]);
            sb.append('\n');
        }
        sb.append('\n');      
    }
    
    
    
    /*
     static String formatItemLevel(Profile.Slot slot) {
        if (slot.item == null) {
            return null;
        }         
        int itemLevel0 = slot.item.itemLevel;
        int itemLevel = slot.getItemLevel();        
        if (slot.isScaledItemLevel()) {
            return String.render("%d (scaled)", slot.getScaledItemLevel());
        }        
        String suffix = slot.item.nameDesc;
        if (suffix == null) {
            suffix = "Normal";
        }        
        if (itemLevel == itemLevel0) {
            return String.render("%d <%s>", itemLevel, suffix);
        } else {
            return String.render("%d <%s> (%+d)", itemLevel, suffix, itemLevel - itemLevel0);
        }
    }
    */
    static void maxify(int[] max, Collection<String[]> rows) {
        for (String[] row: rows) {
            for (int i = 0; i < max.length; i++) {
                max[i] = Math.max(max[i], row[i].length());
            }
        }
    }
    
    static public PlayerDiff compare(Player p1, Player p2) {
        
        if (p1 == null || p2 == null) {
            throw new IllegalArgumentException("Both players must exist");
        }

        ArrayList<String[]> majorDiffs = new ArrayList<>();
        ArrayList<String[]> itemDiffs = new ArrayList<>();
        ArrayList<String[]> powerDiffs = new ArrayList<>();
        ArrayList<String[]> socketDiffs = new ArrayList<>();
        ArrayList<String[]> gemDiffs = new ArrayList<>();
        ArrayList<String[]> enchantDiffs = new ArrayList<>();
        ArrayList<String[]> statDiffs = new ArrayList<>();
        
        if (p1.spec != p2.spec) {            
            majorDiffs.add(diff("Spec", p1.spec, p2.spec));
        }
        if (p1.race != p2.race) {            
            majorDiffs.add(diff("Race", p1.race, p2.race));
        }
        if (p1.playerLevel != p2.playerLevel) {            
            majorDiffs.add(diff("Level", p1.playerLevel, p2.playerLevel));
        }
        if (p1.scaledItemLevel != p2.scaledItemLevel) {
            majorDiffs.add(diff("Scaling", formatScaled(p1.scaledItemLevel), formatScaled(p2.scaledItemLevel)));
        }
                     
        for (SlotT slot: SlotT.db.types) {
            PlayerSlot slot1 = p1.SLOT[slot.index];
            PlayerSlot slot2 = p2.SLOT[slot.index];
            Wearable item1 = slot1.getItem();
            Wearable item2 = slot2.getItem();
            if (Wearable.sameBaseItem(item1, item2)) {
                if (item1 == null) {
                    continue;
                }
                if (slot1.getSuffix() != slot2.getSuffix()) {
                    itemDiffs.add(diff(slot.name + "/Suffix", slot1.getSuffix(), slot2.getSuffix()));
                }     
                if (slot1.isItemLevelCustom() || slot1.isItemLevelCustom()) {
                    if (slot1.getActualItemLevel() != slot2.getActualItemLevel()) {
                        // this shit needs work
                    }
                } else {
                    if (slot1.getUpgradeLevel() != slot2.getUpgradeLevel()) {
                        itemDiffs.add(diff(slot.name + "/Upgrade", formatUpgrade(slot1), formatUpgrade(slot2)));
                    }
                }
            } else {
                itemDiffs.add(diff(slot.name, slot1, slot2));          
            }            
            /*    
            } else {                
                if (s1.item != null && s2.item != null && s1.item.title.equals(s2.item.title)) {
                    // diff item, different suffix or scaling
                    powerDiffs.add(diff(s1.type.title, formatItemLevel(s1), formatItemLevel(s2)));
                } else {
                    itemDiffs.add(diff(s1.type.title, s1.getItemBaseName(false), s2.getItemBaseName(false)));                
                }            
            }
            */
            StatMap sb1 = slot1.getSocketBonusStats();
            StatMap sb2 = slot2.getSocketBonusStats();
            if (!sb1.isSame(sb2)) {
                socketDiffs.add(diff(String.format("%s/Bonus", slot.name), sb1, sb2));
            }
            
            
            for (int i = 0, e = Math.max(slot1.getSocketCount(), slot2.getSocketCount()); i < e; i++) {                       
                PlayerSocket socket1 = slot1.getSocket(i);
                PlayerSocket socket2 = slot2.getSocket(i);
                if (socket1.getSocketColor() != socket1.getSocketColor()) {
                    socketDiffs.add(diff(String.format("%s/Socket#%d", slot.name, i + 1), socket1.getSocketColor(), socket1.getSocketColor()));
                }                
                if (!socket1.isGemEffectivelyEqual(socket2)) {                        
                    gemDiffs.add(diff(String.format("%s/Gem#%d", slot.name, i + 1), socket1.getGemName(false), socket2.getGemName(false)));
                }                    
            }                               
            
            if (slot1.getEnchant() != slot2.getEnchant()) {
                enchantDiffs.add(diff(slot.name, slot1.getEnchant(), slot2.getEnchant()));
            }            
            /*if (s1.tinker != s2.tinker) {
                tinkerDiffs.add(diff(s1.type.title, BlizzT.getGemName(s1.tinker), BlizzT.getGemName(s2.tinker)));
            }*/
        }
        
        StatMap stats1 = new StatMap();        
        StatMap stats2 = new StatMap();        
        p1.collectStats(stats1);
        p2.collectStats(stats2);
        
        System.out.println(stats1);
        System.out.println(stats2);
        
        /*
        long filter = -1;
        if (p1.spec != null && p1.spec == p2.spec) {
            
            filter |= p1.spec.primaryStat.getBit();
            
        }
        */
        
        for (StatT x: StatT.db.types) {
            int value1 = stats1.getEffective(x);
            int value2 = stats2.getEffective(x);
            if (value1 != value2) {                
                statDiffs.add(diff(x.shortName, x.formatValue(value1 - value2), x.formatValue(value2 - value1)));
            }
        }
        
        int total = majorDiffs.size() 
                + itemDiffs.size() 
                + powerDiffs.size() 
                + statDiffs.size()
                + gemDiffs.size() 
                + socketDiffs.size() 
                + enchantDiffs.size(); // + reforgeDiffs.size() + tinkerDiffs.size();
        
        if (total == 0) {
            return SAME;
        }           
        
        int[] max = new int[3];
        max[1] = max[2] = 20; // floor
        maxify(max, majorDiffs);
        maxify(max, itemDiffs);
        maxify(max, powerDiffs);
        maxify(max, gemDiffs);
        maxify(max, enchantDiffs);
        maxify(max, socketDiffs);
        maxify(max, statDiffs);
        ++max[0]; // cause indent
        max[1] = max[2] = Math.max(max[1], max[2]); // symmetry
        
        StringBuilder sb = new StringBuilder();  
        render(sb, "Major", majorDiffs, max);        
        render(sb, "Gear", itemDiffs, max);        
        render(sb, "Power", powerDiffs, max);        
        render(sb, "Enchants", enchantDiffs, max);
        render(sb, "Sockets", socketDiffs, max);
        render(sb, "Gems", gemDiffs, max);
        render(sb, "Stats", statDiffs, max);
                
        sb.append("\n#1:\n");
        sb.append(CompactGear.toString(p1));
        sb.append("\n\n#2:\n");
        sb.append(CompactGear.toString(p2));
        String text = sb.toString();
        
        sb.setLength(0);
        sb.append("Compare: ");
        StringBuilderHelp.plural(sb, total, " ", "difference", "s", true);
        
        return new PlayerDiff(false, sb.toString(), text);
    }
    
}
