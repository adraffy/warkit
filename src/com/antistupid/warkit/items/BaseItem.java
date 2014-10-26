package com.antistupid.warkit.items;

import com.antistupid.warbase.types.TypeT;
import java.util.Comparator;

abstract public class BaseItem<T extends TypeT> {
    
    public final int itemId;
    public final T type;
    public final String name;
    
    public BaseItem(int itemId, T type, String name) {
        this.itemId = itemId;
        this.type = type;
        this.name = name;
    }
    
    
}
