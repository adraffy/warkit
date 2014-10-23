package com.antistupid.warkit.items;

public class WeaponEnchant extends ItemEnchant {

    public final long allowedWeapons;
    
    public WeaponEnchant(int spellId, int itemId, String name, String spellDesc, String icon, int maxItemLevel, 
            boolean isTinker, boolean isRetired, Enchantment enchantment, long allowedWeapons) {
        super(spellId, itemId, name, spellDesc, icon, maxItemLevel, isTinker, isRetired, enchantment);
        this.allowedWeapons = allowedWeapons;
    }

    @Override
    public boolean canApply(Item item) {
        return item instanceof Weapon && ((Weapon)item).type.isMemberOf(allowedWeapons);
    }
    
}
