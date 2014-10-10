package com.antistupid.warkit.items;

public class ArmorEnchant extends AbstractEnchant {
    
    public final long allowedEquip;
    public final long allowedArmor;
    
    public ArmorEnchant(int spellId, String spellName, String spellDesc, int maxItemLevel, boolean isTinker, Enchantment enchantment, long allowedEquip, long allowedArmor) {
        super(spellId, spellName, spellDesc, maxItemLevel, isTinker, enchantment);
        this.allowedEquip = allowedEquip;
        this.allowedArmor = allowedArmor;
    }

    @Override
    public boolean canApply(Item item) {
        if (item instanceof Armor) {
            Armor a = (Armor)item;
            return a.equip.memberOf(allowedEquip) && a.type.memberOf(allowedArmor);
        }
        return false;
    }
    
    
}
