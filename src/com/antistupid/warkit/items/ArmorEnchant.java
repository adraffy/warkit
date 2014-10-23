package com.antistupid.warkit.items;

public class ArmorEnchant extends ItemEnchant {
    
    public final long allowedEquip;
    public final long allowedArmor;
    
    public ArmorEnchant(int spellId, int itemId, String name, String spellDesc, String icon, int maxItemLevel, 
            boolean isTinker, boolean isRetired, Enchantment enchantment, long allowedEquip, long allowedArmor) {
        super(spellId, itemId, name, spellDesc, icon, maxItemLevel, isTinker, isRetired, enchantment);
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
