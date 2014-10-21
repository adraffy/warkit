package com.antistupid.warkit.items;

import java.util.Arrays;
import com.antistupid.warbase.structs.StatAlloc;
import com.antistupid.warbase.types.BindT;
import com.antistupid.warbase.types.EquipT;
import com.antistupid.warbase.types.ProfT;
import com.antistupid.warbase.types.QualityT;
import com.antistupid.warbase.types.SocketT;
import com.antistupid.warbase.types.TypeT;

abstract public class Wearable<T extends TypeT> extends Item<T> {
 
    static public boolean sameBaseItem(Wearable a, Wearable b) {
        if (a == null) {
            return b == null;
        } else if (b == null) {
            return false;
        } else if (a == b) {
            return true;
        } else if (a.suffixGroup != a.suffixGroup) {
            return false;
        } else if (a.itemGroup == b.itemGroup) {
            return a.name.equals(b.name);
            // check faction swap
        } else {
            return false;
        }
    }
    
    
    public final String nameDesc;
    public final int reqLevel;
    public final int reqLevelMax;
    public final int reqLevelCurveId;
    public final StatAlloc[] statAllocs;    
    public final SocketT[] sockets;
    public final Enchantment socketBonus;
    public final Upgrade upgrade;
    public final int pvpItemLevelDelta;
    public final RandomSuffixGroup suffixGroup;
    //public final BonusGroup namedBonusGroup;
    //public final BonusGroup auxBonusGroup;
    public final ItemContext[] contexts;
    public final Wearable[] itemGroup;
    public final int groupIndex;
    public final ItemSet set;
    public final boolean extraSocket;
    public final int[] itemSpells;
    
    // equip must fit a slot
    
    public Wearable(
            int itemId, int itemLevel, T type, QualityT quality, EquipT equip, 
            BindT bind, Unique unique, String name, String text, String fileName,
            ProfT reqSkill, int reqSkillRank, int reqRepId, int reqRepRank,
            long reqRace, long reqClass,
            String nameDesc, int reqLevel, int reqLevelMax, int reqLevelCurveId, 
            StatAlloc[] statAllocs, SocketT[] sockets, Enchantment socketBonus,
            Upgrade upgrade, int pvpItemLevel,
            RandomSuffixGroup suffixGroup, ItemContext[] contexts,
            ItemSet set, Wearable[] group, int groupIndex, int[] itemSpells, boolean extraSocket
    ) {        
        super(
                itemId, itemLevel, type, quality, equip, 
                bind, unique, name, text, fileName,
                reqSkill, reqSkillRank, reqRepId, reqRepRank,
                reqRace, reqClass
        );
        this.nameDesc = nameDesc;
        this.reqLevel = reqLevel;
        this.reqLevelMax = reqLevelMax;
        this.reqLevelCurveId = reqLevelCurveId;
        this.statAllocs = statAllocs; 
        this.sockets = sockets;        
        this.socketBonus = socketBonus;
        this.upgrade = upgrade;
        this.pvpItemLevelDelta = pvpItemLevel;
        this.suffixGroup = suffixGroup;
        //this.namedBonusGroup = namedGroup;
        //this.auxBonusGroup = auxGroup;
        this.contexts = contexts;
        this.set = set;
        this.itemGroup = group;
        this.groupIndex = groupIndex;
        this.extraSocket = extraSocket;
        this.itemSpells = itemSpells;
    }
    
    public boolean isTwoHand() {
        return equip.twoHand;
    }
    
    @Override
    public void appendTo(StringBuilder sb) {
        super.appendTo(sb);
        sb.append("NameDesc: ").append(nameDesc).append("\n");
        sb.append("ReqLevel: ").append(reqLevel).append(" / ReqLevelMax: ").append(reqLevelMax).append(" / CurveId: ").append(reqLevelCurveId).append("\n");
        if (statAllocs != null) {
            for (int i = 0; i < statAllocs.length; i++) {
                sb.append("StatAlloc").append(1 + i).append(": ").append(statAllocs[i]).append("\n");
            }
        }
        sb.append("Sockets: ").append(Arrays.toString(sockets)).append("\n");
        sb.append("SocketBonus: ").append(socketBonus).append("\n");
        sb.append("Upgrade: ").append(upgrade).append("\n");
        sb.append("PvPItemLevel: ").append(pvpItemLevelDelta).append("\n");
        if (suffixGroup != null) {
            sb.append("SuffixGroup: ").append(suffixGroup.id).append("\n");
            for (int i = 0; i < suffixGroup.suffixes.length; i++) {
                sb.append(String.format("Suffix%02d: ", i + 1)).append(suffixGroup.suffixes[i]).append("\n");
            }                
        }     
        if (contexts != null) {
            for (int i = 0; i < contexts.length; i++) {
                ItemContext ctx = contexts[i];
                sb.append(String.format("Contexts%02d: ", i + 1)).append(ctx.defaultBonus.name).append(" <").append(ctx.context).append(">").append("\n");                
                sb.append(" - Default: ").append(ctx.defaultBonus).append("\n");
                if (ctx.optionalBonuses != null) {
                    for (int j = 0; j < ctx.optionalBonuses.length; j++) {
                        sb.append(String.format(" - Optional%2d: ", j + 1)).append(ctx.optionalBonuses[j]).append("\n");
                    }           
                }
                
            }
            
        }
        /*
        if (namedBonusGroup != null) {
            for (int i = 0; i < namedBonusGroup.universe.length; i++) {
                sb.append("NamedBonus").append(1 + i).append(": ").append(namedBonusGroup.universe[i]).append("\n");
            }
        }
        if (auxBonusGroup != null) {
            for (int i = 0; i < auxBonusGroup.universe.length; i++) {
                sb.append("AuxBonus").append(1 + i).append(": ").append(auxBonusGroup.universe[i]).append("\n");
            }
        }
        */
        if (itemGroup != null) {
            for (int i = 0; i < itemGroup.length; i++) {
                sb.append("Group").append(1 + i).append(": <").append(itemGroup[i].groupIndex).append(":").append(itemGroup[i].itemId).append("> ").append(itemGroup[i].nameDesc);
                if (i == groupIndex) {
                    sb.append(" <--");
                }
                sb.append("\n");
            }   
        }
        sb.append("ItemSet: ").append(set).append("\n");
        if (set != null) {
           for (int i = 0; i < set.specs.length; i++) {
               sb.append("+ ").append(set.specs[i]).append(" (").append(set.bonuses[i].length).append(")\n");
               for (SetBonus x: set.bonuses[i]) {
                   sb.append(" ").append(x.index + 1).append(". ").append(x).append(": ").append(x.desc).append("\n");                   
               }
           }
            
        }
        
    }
    
    
    
}
