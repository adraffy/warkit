package warkit;

import warbase.SystemHelp;
import warkit.items.AbstractEnchant;
import warkit.items.Item;
import warkit.items.ItemSet;

abstract public class ExternalWebsite {

    public final String name;
    public final String baseURL;
    
    public ExternalWebsite(String name, String baseURL) {
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
        } else if (x instanceof AbstractEnchant) {
            url = getSpellURL(((AbstractEnchant)x).spellId);
        } else {
            return false;
        }
        return SystemHelp.openURL(url);        
    }
    
    static public final ExternalWebsite WH = new ExternalWebsite("Wowhead", "http://wod.wowhead.com/") {

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
    
    
}
