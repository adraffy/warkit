package warkit.items;

import java.util.Arrays;
import warbase.RandomSuffixGroup;
import warbase.StatAlloc;
import warbase.types.BindT;
import warbase.types.EquipT;
import warbase.types.ProfT;
import warbase.types.QualityT;
import warbase.types.SocketT;
import warbase.types.TypeT;

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
        } else if (a.group == b.group) {
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
    public final NamedItemBonus[] namedBonuses;
    public final AuxBonusGroup auxBonusGroup;
    public final Wearable[] group;
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
            RandomSuffixGroup suffixGroup, NamedItemBonus[] bonuses, AuxBonusGroup auxGroup,
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
        this.namedBonuses = bonuses;
        this.auxBonusGroup = auxGroup;
        this.set = set;
        this.group = group;
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
        if (namedBonuses != null) {
            for (int i = 0; i < namedBonuses.length; i++) {
                sb.append("NamedBonus").append(1 + i).append(": ").append(namedBonuses[i]).append("\n");
            }
        }
        if (auxBonusGroup != null) {
            for (int i = 0; i < auxBonusGroup.universe.length; i++) {
                sb.append("AuxBonus").append(1 + i).append(": ").append(auxBonusGroup.universe[i]).append("\n");
            }
        }
        if (group != null) {
            for (int i = 0; i < group.length; i++) {
                sb.append("Group").append(1 + i).append(": <").append(group[i].groupIndex).append(":").append(group[i].itemId).append("> ").append(group[i].nameDesc);
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
