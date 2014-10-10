package com.antistupid.warkit.items;

import com.antistupid.warbase.structs.RandomSuffixGroup;
import com.antistupid.warbase.structs.StatAlloc;
import com.antistupid.warbase.types.BindT;
import com.antistupid.warbase.types.EquipT;
import com.antistupid.warbase.types.ProfT;
import com.antistupid.warbase.types.QualityT;
import com.antistupid.warbase.types.SocketT;
import com.antistupid.warbase.types.WeaponT;

public class Weapon extends Wearable<WeaponT> {
    
    public final int speed;
    public final float range;
    public final boolean caster;
    public final int damageType;
    
    public Weapon(
            int itemId, int itemLevel, WeaponT type, QualityT quality, EquipT equip, 
            BindT bind, Unique unique, String name, String text, String fileName,
            ProfT reqSkill, int reqSkillRank, int reqRepId, int reqRepRank,
            long reqRace, long reqClass,
            String nameDesc, int reqLevel, int reqLevelMax, int reqLevelCurveId, 
            StatAlloc[] statAllocs, SocketT[] sockets, Enchantment socketBonus,
            Upgrade upgrade, int pvpItemLevel, 
            RandomSuffixGroup suffixGroup, BonusGroup namedGroup, BonusGroup auxGroup,
            ItemSet set, Wearable[] group, int groupIndex, int[] itemSpells, boolean extraSocket,
            int speed, float range, boolean caster, int damageType
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
                set, group, groupIndex, itemSpells, extraSocket
        );
        this.speed = speed;
        this.range = range;
        this.caster = caster;
        this.damageType = damageType; // wands only atm
    }
        
    @Override
    public boolean isTwoHand() {
        return type.hands == 0 ? super.isTwoHand() : type.hands == 2;
    }
    
    
    @Override
    public int getRandPropIndex() {
        return type.randPropIndex;
    }
  
    
}
