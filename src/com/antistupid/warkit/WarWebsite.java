package com.antistupid.warkit;

import com.antistupid.warbase.utils.SystemHelp;
import com.antistupid.warkit.items.ItemEnchant;
import com.antistupid.warkit.items.Item;
import com.antistupid.warkit.items.ItemSet;

abstract public class WarWebsite {

    public final String name;
    public final String baseURL;
    
    public WarWebsite(String name, String baseURL) {
        this.name = name;
        this.baseURL = baseURL;
        
    }
    
    abstract public String getItemURL(int id);
    abstract public String getSpellURL(int id);
    abstract public String getItemSetURL(int id);
    
    public boolean show(Object x) {
        String url;
        if (x instanceof Item) {
            url = getItemURL(((Item)x).itemId);
        } else if (x instanceof ItemSet) {
            url = getItemSetURL(((ItemSet)x).id);
        } else if (x instanceof ItemEnchant) {
            url = getSpellURL(((ItemEnchant)x).spellId);
        } else {
            return false;
        }
        return SystemHelp.openURL(url);        
    }
    
    static public final WarWebsite WOWHEAD = new WarWebsite("Wowhead", "http://wod.wowhead.com/") {
        @Override
        public String getItemURL(int id) {
            return baseURL + "item=" + id;
        }
        @Override
        public String getSpellURL(int id) {
            return baseURL + "spell=" + id;
        }        
        @Override
        public String getItemSetURL(int id) {
            return baseURL + "itemset=" + id;
        }        
    };
    
    static public final WarWebsite WOWDB = new WarWebsite("WowDB", "http://beta.wowdb.com/") {
        @Override
        public String getItemURL(int id) {
            return baseURL + "items/" + id;
        }
        @Override
        public String getSpellURL(int id) {
            return baseURL + "spells/" + id;
        }        
        @Override
        public String getItemSetURL(int id) {
            return baseURL + "itemsets/" + id;
        }        
    };
    
    
}
