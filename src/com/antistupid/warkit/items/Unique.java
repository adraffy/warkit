package com.antistupid.warkit.items;

public class Unique {

    public final int id;
    public final String name;
    public final int max;
    
    public Unique(int id, String name, int max) {
        this.id = id;
        this.name = name;
        this.max = max;
    }
    
    public boolean isItemSpecific() {
        return id < 0;
    }
    
    @Override
    public String toString() {
        return String.format("%s<%d>[%s (%d)]", getClass().getSimpleName(), id, name, max);
    }
    
    
}
