package com.antistupid.warkit.items;

public class WeaponEnchant extends AbstractEnchant {

    public final long allowedWeapons;
    
    public WeaponEnchant(int spellId, String spellName, String spellDesc, String spellIcon, int maxItemLevel, boolean isTinker, Enchantment enchantment, long allowedWeapons) {
        super(spellId, spellName, spellDesc, spellIcon, maxItemLevel, isTinker, enchantment);
        this.allowedWeapons = allowedWeapons;
    }

    @Override
    public boolean canApply(Item item) {
        return item instanceof Weapon && ((Weapon)item).type.isMemberOf(allowedWeapons);
    }
    
}
