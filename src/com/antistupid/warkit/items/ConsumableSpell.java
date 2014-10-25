package com.antistupid.warkit.items;

import com.antistupid.warbase.structs.StatAlloc;

public class ConsumableSpell {
    
    public final int spellId;
    public final int duration;
    public final int scalingLevelMax;
    public final int scalingId;
    public final StatAlloc[] statAllocs;
    
    public ConsumableSpell(int spellId, int duration, int scalingLevelMax, int scalingId, StatAlloc[] statAllocs) {
        this.spellId = spellId;
        this.duration = duration;
        this.scalingLevelMax = scalingLevelMax;
        this.scalingId = scalingId;
        this.statAllocs = statAllocs;
    }

}
