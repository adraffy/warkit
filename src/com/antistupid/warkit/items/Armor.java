package com.antistupid.warkit.items;

import com.antistupid.warbase.structs.StatAlloc;
import com.antistupid.warbase.types.ArmorT;
import com.antistupid.warbase.types.EquipT;
import com.antistupid.warbase.types.BindT;
import com.antistupid.warbase.types.ProfT;
import com.antistupid.warbase.types.QualityT;
import com.antistupid.warbase.types.SocketT;

public class Armor extends Wearable<ArmorT> {
    
    public Armor(
            int itemId, int itemLevel, ArmorT type, QualityT quality, EquipT equip, 
            BindT bind, Unique unique, String name, String text, String fileName,
            ProfT reqSkill, int reqSkillRank, int reqRepId, int reqRepRank,
            long reqRace, long reqClass,
            String nameDesc, int reqLevel, int reqLevelMax, int reqLevelCurveId, 
            StatAlloc[] statAllocs, SocketT[] sockets, Enchantment socketBonus,
            Upgrade upgrade, int pvpItemLevel, 
            RandomSuffixGroup suffixGroup, ItemContext[] contexts, ///BonusGroup namedGroup, BonusGroup auxGroup,
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
                suffixGroup, contexts, //namedGroup, auxGroup,
                set, group, groupIndex, spellIds, extraSocket
        );
    }
    
}
