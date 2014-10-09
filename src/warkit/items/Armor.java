package warkit.items;

import warbase.RandomSuffixGroup;
import warbase.StatAlloc;
import warbase.types.ArmorT;
import warbase.types.EquipT;
import warbase.types.BindT;
import warbase.types.ProfT;
import warbase.types.QualityT;
import warbase.types.SocketT;

public class Armor extends Wearable<ArmorT> {
    
    public Armor(
            int itemId, int itemLevel, ArmorT type, QualityT quality, EquipT equip, 
            BindT bind, Unique unique, String name, String text, String fileName,
            ProfT reqSkill, int reqSkillRank, int reqRepId, int reqRepRank,
            long reqRace, long reqClass,
            String nameDesc, int reqLevel, int reqLevelMax, int reqLevelCurveId, 
            StatAlloc[] statAllocs, SocketT[] sockets, Enchantment socketBonus,
            Upgrade upgrade, int pvpItemLevel, 
            RandomSuffixGroup suffixGroup, BonusGroup namedGroup, BonusGroup auxGroup,
            ItemSet set, Wearable[] group, int groupIndex, int[] spellIds, boolean extraSocket
    ) {        
        super(
                itemId, itemLevel, type, quality, equip, 
                bind, unique, name, text, fileName,
                reqSkill, reqSkillRank, reqRepId, reqRepRank,
                reqRace, reqClass,
                nameDesc, reqLevel, reqLevelMax, reqLevelCurveId, 
                statAllocs, sockets, socketBonus,
                upgrade, pvpItemLevel,
                suffixGroup, namedGroup, auxGroup,
                set, group, groupIndex, spellIds, extraSocket
        );
    }
    
}
