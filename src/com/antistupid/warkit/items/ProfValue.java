package com.antistupid.warkit.items;

import com.antistupid.warbase.types.ProfT;

public class ProfValue {
    
    public final ProfT prof;
    public final int value;
    
    public ProfValue(ProfT prof, int value) {
        this.prof = prof;
        this.value = value;
    }
    
    @Override
    public String toString() {
        return prof + "(" + value + ")";
    }

}
