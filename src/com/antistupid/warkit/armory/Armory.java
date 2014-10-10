package com.antistupid.warkit.armory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import com.antistupid.warbase.structs.RandomSuffix;
import com.antistupid.warbase.utils.SystemHelp;
import com.antistupid.warbase.types.ClassT;
import com.antistupid.warbase.types.ProfT;
import com.antistupid.warbase.types.RaceT;
import com.antistupid.warbase.types.RegionT;
import com.antistupid.warbase.types.SpecT;
import com.antistupid.warbase.HttpCache;
import com.antistupid.warbase.HttpCache.Result;
import com.antistupid.warkit.JSONHelp;
import com.antistupid.warkit.WarKit;
import com.antistupid.warkit.items.AbstractEnchant;
import com.antistupid.warkit.items.Gem;
import com.antistupid.warkit.items.Item;
import com.antistupid.warkit.items.Wearable;
import com.antistupid.warkit.player.Player;
import com.antistupid.warkit.player.PlayerError;
import com.antistupid.warkit.player.PlayerSlot;

public class Armory {

    final WarKit wk;
    final HttpCache hc;
    
    public Armory(WarKit wk, HttpCache hc) {
        this.wk = wk;
        this.hc = hc;
    }
    
    
    static private String urlEncode(String x) {
        try {
            return URLEncoder.encode(x, "UTF-8");
        } catch (UnsupportedEncodingException fuckyou) {                
            return x;
        }
    }
    
    static private String parsePlayerName(String temp) {
        String prefix = "<strong>";
        int pos = temp.indexOf(prefix);
        if (pos == -1) {
            return null;
        }
        pos += prefix.length();
        int end = temp.indexOf('<', pos);
        if (end == -1) {
            return null;
        }
        return temp.substring(pos, end).trim();
    }
    
    static private String parseRealmName(String temp) {
        int pos = temp.indexOf('>');
        if (pos == -1) {
            return null;
        }
        return temp.substring(pos + 1).trim().replace("&#39;", "'");
    }
    
    static private int parsePlayerLevel(String temp) {
        int pos = temp.indexOf('>');
        if (pos == -1) {
            return 0;
        }
        try {
            return Integer.parseInt(temp.substring(pos + 1).trim());
        } catch (NumberFormatException err) {
            return 0;
        }
    }
    
    static private RaceT parseRace(String temp) {
        String prefix = "/race_";
        int pos = temp.indexOf(prefix);
        if (pos == -1) {
            return null;
        }
        pos += prefix.length();
        int end = temp.indexOf('_', pos);
        if (end == -1) {
            return null;
        }
        try {
            return RaceT.db.by_id.get(Integer.parseInt(temp.substring(pos, end)));
        } catch (NumberFormatException err) {
            return null;
        }
    }
    
    static private ClassT parseClass(String temp) {
        String prefix = "/class_";
        int pos = temp.indexOf(prefix);
        if (pos == -1) {
            return null;
        }
        pos += prefix.length();
        int end = temp.indexOf('.', pos);
        if (end == -1) {
            return null;
        }
        try {
            return ClassT.db.by_id.get(Integer.parseInt(temp.substring(pos, end)));
        } catch (NumberFormatException err) {
            return null;
        }
    }
    
    static private int parseMaxPages(String html) {
        String prefix = "data-pagenum=\"";
        int max = 0;
        int pos = 0;
        while (true) {
            pos = html.indexOf(prefix, pos);
            if (pos == -1) {
                break;
            }
            pos += prefix.length();
            int end = html.indexOf('\"', pos);
            if (end == -1) {
                continue;
            }
            try {
                max = Math.max(max, Integer.parseInt(html.substring(pos, end)));
            } catch (NumberFormatException err) {
            }                            
        }
        return max;        
    }
        
    private void extract(String html, Consumer<ArmorySearchResult> c) {
        String prefix_char = "/character/";
        int global_pos = 0;
        while (true) {            
            int index = html.indexOf(prefix_char, global_pos); // findPlayers an armory url
            if (index == -1) {
                break;
            }
            index += prefix_char.length();
            int start = html.lastIndexOf("<tr", index); // back up tthe
            if (start == -1) {
                break;
            }
            int rowEnd = html.indexOf("</tr>", index);
            if (rowEnd == -1) {
                break;
            }
            global_pos = rowEnd;         
            String row = html.substring(start, rowEnd);
            String[] comp = row.split("</td>");
            if (comp.length != 9) {
                break;
            }    
            String name = parsePlayerName(comp[0]);
            if (name == null) {
                continue;
            }
            int level = parsePlayerLevel(comp[1]);
            if (level == 0) {
                continue;
            }
            RaceT race = parseRace(comp[2]);
            if (race == null) {
                continue;
            }
            ClassT cls = parseClass(comp[3]);
            if (cls == null) {
                continue;
            }
            //comp[5]; guild
            String realmName = parseRealmName(comp[6]);
            if (realmName == null) {
                continue;
            }
            String realmSlug = html.substring(index, html.indexOf('/', index));
            c.accept(new ArmorySearchResult(name, realmName, realmSlug, level, race, cls));            
        }
        
    }
    
    public ArrayList<ArmorySearchResult> findPlayers(String name, RegionT region, boolean force, int maxResults, Predicate<ArmorySearchResult> filter) {
        String url0 = "http://" + region.host + "/wow/en/search?f=wowcharacter&sort=level&dir=d&q=" + urlEncode(name) + "&page=";
        int page = 0;
        int maxPages = 1;
        Result last;
        ArrayList<ArmorySearchResult> matches = new ArrayList<>();
        while (page < maxPages) {
            page++;
            String url = url0 + page + "|dir=Search|name=#.html";
            last = hc.fetchData(url, force ? -1 : HttpCache.ONE_DAY, true);
            if (last.error != null) {
                break;                
            }
            String html = new String(last.data, StandardCharsets.UTF_8);
            if (page == 1) {
                maxPages = parseMaxPages(html);
            }
            extract(html, x -> {
                if (filter.test(x)) {
                    matches.add(x);
                    //System.out.println(x);
                }                
            });
            if (maxResults > 0 && matches.size() >= maxResults) {
                break;
            }
        }        
        return matches;
    }
    
    private JSONObject json(Result res) {
        if (res.error != null) {
            throw new ArmoryError("Download Error: " + res.error);
        }
        JSONObject root;
        try {
            root = (JSONObject)JSONValue.parse(new String(res.data, StandardCharsets.UTF_8));
            if (root == null) {
                throw new NullPointerException();
            }
        } catch (RuntimeException err) {
            throw new ArmoryError("Invalid JSON: " + err);
        }        
        String status = (String)root.get("status");        
        if (status != null && status.equals("nok")) {      
            String err = (String)root.get("reason");
            throw new ArmoryError("Blizzard API Error: " + err);
        }
        return root;
    }
    
    public String resolveRealmSlug(String realmGuess, RegionT region) { //, boolean silent) {
        try {
            realmGuess = realmGuess.trim();            
            if (realmGuess.isEmpty()) {            
                throw new IllegalArgumentException("Realm is empty");
            }
            ArrayList<ArmoryRealm> list = getRealmList(region);      
            for (ArmoryRealm x: list) {
                if (x.realmSlug.equalsIgnoreCase(realmGuess) || x.realmName.equalsIgnoreCase(realmGuess)) {
                    //System.out.println("Found Match: " + x);
                    return x.realmSlug;
                }      
            }
            return realmGuess;            
        } catch (ArmoryError err) {
            /*if (silent) {
                return realmGuess;
            }*/
            throw new ArmoryError(String.format("Unable to Resolve US Realm \"%s\" Failed: %s", region.name, realmGuess, err.getMessage()));
        }        
    }
    
    public String cleanRealmSlug(String realmSlug) {
        return realmSlug.trim().toLowerCase().replaceAll("\\s+", "-");
    }
    
    public ArrayList<ArmoryRealm> getRealmList(RegionT region) {        
        String url = "http://" + region.host + "/api/wow/realm/status|name=Realms.json";           
        try {
            JSONObject root = json(hc.fetchData(url, HttpCache.ONE_DAY, true));        
            JSONArray realmList = (JSONArray)root.get("realms");          
            ArrayList<ArmoryRealm> list = new ArrayList<>(realmList.size());
            for (Object x: realmList) {
                JSONObject realmInfo = (JSONObject)x;
                String name = (String)realmInfo.get("name");
                String slug = (String)realmInfo.get("slug");
                String group = (String)realmInfo.get("battlegroup");                
                list.add(new ArmoryRealm(name, slug, group));
            } 
            return list;
        } catch (RuntimeException err) {
            throw new ArmoryError("Realm List Failed: " + err.getMessage());
        }
    }
    
        
    static final String SPEC_CHARS = "aZbY";
    
    private String _realmSlashName(String name, String realmSlug) {        
        return urlEncode(realmSlug.toLowerCase().replaceAll("\\s+", "-")) +  "/" + urlEncode(name.trim());
    }
    
    public Player getPlayer(String name, String realmSlug, RegionT region, boolean force, Consumer<String> errors) { 
        String base = "http://" + region.host + "/api/wow/character/" + _realmSlashName(name, realmSlug);
        String url = base + "?fields=items,talents,professions&locale=en|dir=Character|name=#.json";    
        JSONObject root;
        try {
            root = json(hc.fetchData(url, force ? -1 : 30000, true));  
        } catch (RuntimeException err) {
            throw new ArmoryError("Fetch Player Failed: " + err.getMessage());
        }
        JSONObject localized;
        if (region.asia) {
            String url2 = base + "|dir=Localized|name=#.json";    
            try {
                localized = json(hc.fetchData(url2, force ? -1 : HttpCache.ONE_DAY, true));  
            } catch (RuntimeException err) {
                throw new ArmoryError("Fetch Localized Player Failed: " + err.getMessage());
            }
        } else {
            localized = root;
        }
        Player p = new Player();
        try {
            p.playerName = JSONHelp.requireStr(localized, "name");            
            p.realmName = JSONHelp.requireStr(localized, "realm");
            p.playerLevel = JSONHelp.requireNum(root, "level").intValue();
            p.realmSlug = realmSlug; // this must be valid since it worked
            p.region = region; // same
        } catch (RuntimeException err) {
            throw new ArmoryError("Parse Player Failed (Header)");
        }
        // identity is defined
        try {
            ClassT cls = ClassT.db.by_id.require(JSONHelp.requireNum(root, "class").intValue());
            RaceT race = RaceT.db.by_id.require(JSONHelp.requireNum(root, "race").intValue());
            JSONArray talents = JSONHelp.require(root, "talents", JSONArray.class);     
            int specIndex = -1;
            for (Object x: talents) {
                JSONObject talentsInfo = (JSONObject)x;                                             
                if (talentsInfo.containsKey("selected")) {
                    //p.talents = (String)talentsInfo.get("calcTalent");
                    //p.glyphs = (String)talentsInfo.get("calcGlyph");
                    specIndex = SPEC_CHARS.indexOf(JSONHelp.requireStr(talentsInfo, "calcSpec"));
                    break;
                }
            }
            if (specIndex < 0 || specIndex >= cls.specs.size()) {
                throw new ArmoryError("No Selected Spec");
            }
            p.spec = cls.specs.get(specIndex);
            if (!p.spec.classType.canBe(race)) {
                throw new ArmoryError("Bullshit Race");
            }
            p.race = race;
                        
            JSONObject profData = JSONHelp.require(root, "professions", JSONObject.class);
            JSONArray primary = JSONHelp.require(profData, "primary", JSONArray.class);

            int profIndex = 0;
            for (Object x: primary) {
                JSONObject profInfo = (JSONObject)x;                
                ProfT prof = ProfT.db.by_id.require(JSONHelp.requireInt(profInfo, "id"));                 
                int level = JSONHelp.requireInt(profInfo, "rank");    
                p.setProf(profIndex++, prof, level);                    
            }
            
            JSONObject items = (JSONObject)root.get("items");       
            loadSlot(p.HEAD,        JSONHelp.get(items, "head", JSONObject.class), errors); 
            loadSlot(p.BACK,        JSONHelp.get(items, "back", JSONObject.class), errors);      
            loadSlot(p.NECK,        JSONHelp.get(items, "neck", JSONObject.class), errors); 
            loadSlot(p.SHOULDER,    JSONHelp.get(items, "shoulder", JSONObject.class), errors);   
            loadSlot(p.CHEST,       JSONHelp.get(items, "chest", JSONObject.class), errors);   
            loadSlot(p.WRIST,       JSONHelp.get(items, "wrist", JSONObject.class), errors); 
            loadSlot(p.HANDS,       JSONHelp.get(items, "hands", JSONObject.class), errors); 
            loadSlot(p.WAIST,       JSONHelp.get(items, "waist", JSONObject.class), errors); 
            loadSlot(p.LEGS,        JSONHelp.get(items, "legs", JSONObject.class), errors); 
            loadSlot(p.FEET,        JSONHelp.get(items, "feet", JSONObject.class), errors); 
            loadSlot(p.F1,          JSONHelp.get(items, "finger1", JSONObject.class), errors); 
            loadSlot(p.F2,          JSONHelp.get(items, "finger2", JSONObject.class), errors); 
            loadSlot(p.T1,          JSONHelp.get(items, "trinket1", JSONObject.class), errors); 
            loadSlot(p.T2,          JSONHelp.get(items, "trinket2", JSONObject.class), errors); 
            loadSlot(p.MH,          JSONHelp.get(items, "mainHand", JSONObject.class), errors); 
            loadSlot(p.OH,          JSONHelp.get(items, "offHand", JSONObject.class), errors); 
          
            
            return p;
        } catch (RuntimeException err) {
            throw new ArmoryError("Load Player Failed: " + err.getMessage());
        }
    }
    
    static final String[] gemKeys = {"gem0", "gem1", "gem2"};
    
    private void loadSlot(PlayerSlot slot, JSONObject root, Consumer<String> errors) {
        if (root == null) {
            slot.clear(); // not needed
            return;
        }
        int itemId = JSONHelp.requireNum(root, "id").intValue();        
        slot.setItem(wk.wearableMap.get(itemId));
        Wearable item = slot.getItem();
        
        JSONObject paramMap = JSONHelp.get(root, "tooltipParams", JSONObject.class);
        if (paramMap != null) {
            if (item.suffixGroup != null) {
                int suffixId = -JSONHelp.getInt(paramMap, "suffix", 0);
                if (suffixId != 0) {
                    int index = item.suffixGroup.find(suffixId);
                    if (index < 0) {
                        errors.accept(String.format("%s: Unknown Suffix: %d", slot.slotType, suffixId));
                        index = 0; // use default
                    }
                    slot.setSuffixIndex(index);                
                }                        
            }
            slot.setExtraSocket(paramMap.containsKey("extraSocket"));
            for (int i = 0; i < gemKeys.length; i++) {
                int gemId = JSONHelp.getInt(paramMap, gemKeys[i], 0);
                if (gemId != 0) {
                    Gem gem = wk.gemMap.get(gemId);
                    if (gem != null) {
                        try {
                            slot.getSocket(i).setGem(wk.gemMap.get(gemId));
                        } catch (PlayerError err) {
                            errors.accept(err.getMessage());
                        }
                    } else {
                        errors.accept(String.format("%s/Gem%d: Unknown Gem: ", slot.slotType.name, i + 1, gemId));
                    }
                }
            }
            JSONObject upInfo = JSONHelp.get(paramMap, "upgrade", JSONObject.class);
            if (upInfo != null) {            
                int delta = JSONHelp.requireInt(upInfo, "itemLevelIncrement");            
                slot.setUpgradeLevelOrDelta(delta);    
            }
            int enchantId = JSONHelp.getInt(paramMap, "enchant", 0);
            if (enchantId != 0) {                                    
                AbstractEnchant enchant = wk.findEnchant(item, enchantId);
                if (enchant != null) {    
                    try {
                        slot.setEnchant(enchant);
                    } catch (PlayerError err) {
                        errors.accept(err.getMessage());
                    }
                } else {
                    errors.accept(String.format("%s: Unknown Enchant: %d", slot.slotType.name, enchantId));
                }
            }         
            
            
        }
        
        /*
            Number reforgeId = (Number)paramMap.get("reforge");
            if (reforgeId != null) {
                ReforgePair reforge = ReforgePair.decode(reforgeId.intValue()); 
                slot.setReforge(reforge);
            }
            Number tinkerId = ((Number)paramMap.get("tinker"));
            if (tinkerId != null) {
                slot.tinker = loadTinker(tinkerId.intValue());
            }
            Number enchantId = (Number)paramMap.get("enchant");
            if (enchantId != null) {                    
                slot.enchant = loadEnchant(enchantId.intValue());
            }                
            slot.extraSocket = paramMap.containsKey("extraSocket");            
            for (int i = 0; i < GemT.KEYS.length; i++) {
                Number gemId = (Number)paramMap.get(GemT.KEYS[i]);
                if (gemId != null) {
                    slot.setGemAt(i, (Gem)loadItem(gemId.intValue()));                    
                }
            }
            */
        
        
        
        
        
    }
    
    
    public void visitArmory(String name, String realmSlug, RegionT region) {        
        String url = "http://" + region.host + "/wow/character/" + _realmSlashName(name, realmSlug) + "/advanced";   
        if (!SystemHelp.openURL(url)) {
            throw new ArmoryError("Unable to visit Armory URL: " + url);
        }
    }
    
    public void visitWowProgress(String name, String realmSlug, RegionT region) {
        //http://www.wowprogress.com/character/us/suramar/Edgy
        String url = "http://www.wowprogress.com/character/" + region.name.toLowerCase() + "/" + _realmSlashName(name, realmSlug);   
        if (!SystemHelp.openURL(url)) {
            throw new ArmoryError("Unable to visit WoW Progress URL: " + url);
        }
    }
    
}
