package com.antistupid.warkit.items;

public class Upgrade {
    
    public final UpgradeChain normal; // both not null
    public final UpgradeChain asia;   // can be the same as normal
    
    public Upgrade(UpgradeChain normal, UpgradeChain asia) {
        this.normal = normal;
        this.asia = asia;
    }
    
    public UpgradeChain getChain(boolean isAsia) {
        return isAsia ? asia : normal;
    }
    
    public boolean isAsiaSpecial() {
        return normal != asia;
    }
    
    @Override
    public String toString() {
        if (isAsiaSpecial()) {
            return normal + " / " + asia;
        } else {
            return normal.toString();
        }
    }
    

}
