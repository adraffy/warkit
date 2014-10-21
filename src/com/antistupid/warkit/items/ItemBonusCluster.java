package com.antistupid.warkit.items;

import com.antistupid.warbase.structs.StatAlloc;
import java.util.Arrays;
import java.util.Comparator;
import com.antistupid.warbase.types.QualityT;
import com.antistupid.warbase.types.SocketT;

public class ItemBonusCluster {
        
    public final ItemBonus[] components; // never null
    public final String name;
    public final int itemLevelDelta;
    public final int reqLevelDelta;
    public final QualityT quality; // can be null
    public final StatAlloc[] statAllocs; // can be null
    public final SocketT[] sockets; // can be null

    public ItemBonusCluster(ItemBonus[] components, String name, int itemLevelDelta, int reqLevelDelta, QualityT quality, StatAlloc[] statAllocs, SocketT[] sockets) {
        this.components = components;
        this.name = name;
        this.itemLevelDelta = itemLevelDelta;
        this.reqLevelDelta = reqLevelDelta;     
        this.quality = quality;
        this.statAllocs = statAllocs;
        this.sockets = sockets;
    }
    
    static public final ItemBonusCluster NONE = new ItemBonusCluster(new ItemBonus[0], "None", 0, 0, null, null, null);
          
    @Override
    public String toString() {
        return String.format("%s(%s,%d,%d,%s)%s", getClass().getSimpleName(), name, itemLevelDelta, reqLevelDelta, quality, Arrays.toString(components));
    }
    
    static public final Comparator<ItemBonusCluster> CMP_ITEM_LEVEL = (a, b) -> a.itemLevelDelta - b.itemLevelDelta;
    
    
}
