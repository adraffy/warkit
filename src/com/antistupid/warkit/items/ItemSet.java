package com.antistupid.warkit.items;

import java.util.Arrays;
import com.antistupid.warbase.types.SpecT;

public class ItemSet {
    
    public final int id;
    public final String name;
    public final int size;
    public final ProfValue reqProf;
    public final SpecT[] specs; 
    public final SetBonus[][] bonuses;
    
    public ItemSet(int id, String name, int size, ProfValue reqProf, int specCount) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.reqProf = reqProf;
        specs = new SpecT[specCount];
        bonuses = new SetBonus[specCount][];
    }
    
    public boolean isOldStyle() {
        return specs.length == 1 && specs[0] == null;
    }    
    
    public boolean canBenefitSpec(SpecT spec) {
        return findSpec(spec) >= 0;
    }
    
    public SetBonus[] getBonuses(SpecT spec) {
        int index = findSpec(spec);
        return index >= 0 ? bonuses[index] : null;
    }
    
    public int findSpec(SpecT spec) {
        if (isOldStyle()) {
            return 0;
        } 
        for (int i = 0; i < specs.length; i++) {
            if (specs[i] == spec) {
                return i;
            }            
        }
        return -1;        
    }
    
    @Override
    public String toString() {
        return String.format("%s<%d>(%s,%d,%s)%s", getClass().getSimpleName(), id, name, size, reqProf, Arrays.toString(specs));
    }

}
