package warkit.items;

public class WeaponEnchant extends AbstractEnchant {

    public final long allowedWeapons;
    
    public WeaponEnchant(int spellId, String spellName, String spellDesc, int maxItemLevel, boolean isTinker, Enchantment enchantment, long allowedWeapons) {
        super(spellId, spellName, spellDesc, maxItemLevel, isTinker, enchantment);
        this.allowedWeapons = allowedWeapons;
    }

    @Override
    public boolean canApply(Item item) {
        return item instanceof Weapon && ((Weapon)item).type.memberOf(allowedWeapons);
    }
    
}
