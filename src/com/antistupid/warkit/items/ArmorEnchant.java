package com.antistupid.warkit.items;

public class ArmorEnchant extends AbstractEnchant {
    
    public final long allowedEquip;
    public final long allowedArmor;
    
    public ArmorEnchant(int spellId, String spellName, String spellDesc, String spellIcon, int maxItemLevel, boolean isTinker, Enchantment enchantment, long allowedEquip, long allowedArmor) {
        super(spellId, spellName, spellDesc, spellIcon, maxItemLevel, isTinker, enchantment);
        this.allowedEquip = allowedEquip;
        this.allowedArmor = allowedArmor;
    }

    @Override
    public boolean canApply(Item item) {
        if (item instanceof Armor) {
            Armor a = (Armor)item;
            return a.equip.isMemberOf(allowedEquip) && a.type.isMemberOf(allowedArmor);
        }
        return false;
    }
    
    
}
