package com.antistupid.warkit.items;

import java.util.Arrays;

public class RandomSuffixGroup {

    public final int id;
    public final RandomSuffix[] suffixes; // this is always sorted by id
    
    public RandomSuffixGroup(int id, RandomSuffix[] suffixes) {
        this.id = id;
        this.suffixes = suffixes;
    }
    
    public int find(int id) {
        int a = 0;
        int b = suffixes.length;
        while (a < b) {
            int m = (a + b) >>> 1;
            int k = suffixes[m].id;
            if (k == id) {
                return m;
            } else if (k > id) {
                b = m;
            } else {
                a = m + 1;
            }
        }
        return -1; //~a;
    }
    
    public boolean contains(int id) {
        return find(id) >= 0;
    }
    
    @Override
    public String toString() {
        return String.format("%s<%d>%s", getClass().getSimpleName(), id, Arrays.toString(suffixes));
    }
        
}
