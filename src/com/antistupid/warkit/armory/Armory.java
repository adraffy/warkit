package com.antistupid.warkit.armory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import com.antistupid.warbase.types.ClassT;
import com.antistupid.warbase.types.ProfT;
import com.antistupid.warbase.types.RaceT;
import com.antistupid.warbase.types.RegionT;
import com.antistupid.warbase.HttpCache;
import com.antistupid.warbase.HttpCache.Result;
import com.antistupid.warbase.IntSet;
import com.antistupid.warkit.JSONHelp;
import com.antistupid.warkit.WarKit;
import com.antistupid.warkit.items.AbstractEnchant;
import com.antistupid.warkit.items.Gem;
import com.antistupid.warkit.items.Wearable;
import com.antistupid.warkit.player.Player;
import com.antistupid.warkit.player.PlayerError;
import com.antistupid.warkit.player.PlayerSlot;

public class Armory {

    final WarKit wk;
    final HttpCache hc;
    final String apiKey;
    
    public Armory(WarKit wk, HttpCache hc, String apiKey) {
        this.wk = wk;
        this.hc = hc;
        this.apiKey = apiKey;
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
    
    static private String parseGuildName(String temp) {
        int end = temp.indexOf("</a>");
        if (end == -1) {
            return null;
        }
        int pos = temp.lastIndexOf('>', end);
        if (pos == -1) {
            return null;
        }
        return temp.substring(pos + 1, end);        
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
    
    static private boolean parseGender(String temp) {
        String prefix = "/race_";
        int pos = temp.indexOf(prefix);
        if (pos == -1) {
            return true;
        }
        pos += prefix.length();
        pos = temp.indexOf('_', pos);
        if (pos == -1) {
            return true;
        }
        try {
            return temp.charAt(pos + 1) != '1';
        } catch (NumberFormatException err) {
            return true;
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
            // comp[4] = faction
            String guildName = parseGuildName(comp[5]); // can be null
            String realmName = parseRealmName(comp[6]);
            if (realmName == null) {
                continue;
            }
            boolean male = parseGender(comp[2]);            
            String realmSlug = html.substring(index, html.indexOf('/', index));
            c.accept(new ArmorySearchResult(name, realmName, realmSlug, level, race, male, cls, guildName));            
        }
        
    }
    
    public ArrayList<ArmorySearchResult> findPlayers(String name, RegionT region, boolean force, int maxResults, Predicate<ArmorySearchResult> filter) {
        String url0 = region.wwwURLPrefix + "/wow/en/search?f=wowcharacter&sort=level&dir=d&q=" + urlEncode(name) + "&page=";
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
                if (filter == null || filter.test(x)) {
                    matches.add(x);
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
            root = (JSONObject)JSONValue.parseWithException(new String(res.data, StandardCharsets.UTF_8));
        } catch (Exception err) {
            throw new ArmoryError("Invalid JSON: " + err);
        }        
        String status = (String)root.get("status");        
        if (status != null && status.equals("nok")) {      
            String err = (String)root.get("reason");
            throw new ArmoryError("Blizzard API Error: " + err);
        }
        return root;
    }
    
    public ArmoryRealm resolveRealm(String realmGuess, RegionT region) { //, boolean silent) {
        try {
            realmGuess = cleanRealmSlug(realmGuess);      
            if (realmGuess.isEmpty()) {            
                throw new IllegalArgumentException("Realm is empty");
            }
            ArrayList<ArmoryRealm> list = getRealmList(region);      
            for (ArmoryRealm x: list) {
                if (x.realmSlug.equalsIgnoreCase(realmGuess) || x.realmName.equalsIgnoreCase(realmGuess)) {
                    return x;
                }      
            }
            throw new ArmoryError(String.format("No %s realm named \"%s\"", region.name, realmGuess));
        } catch (ArmoryError err) {
            throw new ArmoryError(String.format("Resolve %s realm \"%s\" Failed: %s", region.name, realmGuess, err.getMessage()));
        }        
    }
        
    public ArrayList<ArmoryRealm> getRealmList(RegionT region) {        
        String url = _prefix(region) + "wow/realm/status?apikey=" + apiKey + "|name=Realms.json";       
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
            throw new ArmoryError(region.name + " Realm List Failed: " + err.getMessage());
        }
    }
    
    static public final int TALENT_SPEC_SELECTED = 0;
    static public final int TALENT_SPEC_1 = 1;
    static public final int TALENT_SPEC_2 = 2;
    
    static final String SPEC_CHARS = "aZbY";
    
    static public String cleanRealmSlug(String realmSlug) {
        return realmSlug.trim().toLowerCase().replaceAll("\\s+", "-");
    }
    
    private String _realmSlashName(String name, String realmSlug) {        
        return urlEncode(cleanRealmSlug(realmSlug)) + "/" + urlEncode(name.trim());
    }                  
    private String _prefix(RegionT region) {
        return apiKey != null ? region.apiURLPrefix : region.wwwURLPrefix + "api/";
    }
    
    public Player getPlayer(String name, String realmSlug, RegionT region, int talentIndex, boolean force, Consumer<String> errors) { 
        String base = _prefix(region) + "wow/character/" + _realmSlashName(name, realmSlug);
        String url = base + "?fields=items,talents,professions&locale=en_US&apikey=" + apiKey + "|dir=Character|name=#.json";    
        JSONObject root;
        try {
            root = json(hc.fetchData(url, force ? -1 : 60000, true));  
        } catch (RuntimeException err) {
            throw new ArmoryError("Fetch Player Failed: " + err.getMessage());
        }
        JSONObject localized;
        if (region.asia) {
            String url2 = base + "?apikey=" + apiKey + "|dir=Localized|name=#.json";    
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
            p.playerMale = JSONHelp.requireInt(root, "gender") == 0;
            p.realmName = JSONHelp.requireStr(localized, "realm");
            p.playerLevel = JSONHelp.requireInt(root, "level");
            p.realmSlug = realmSlug; // this must be valid since it worked
            p.region = region; // same
        } catch (RuntimeException err) {
            throw new ArmoryError("Parse Player Failed" + err);
        }
        // identity is defined
        try {
            ClassT cls = ClassT.db.by_id.require(JSONHelp.requireInt(root, "class"));
            RaceT race = RaceT.db.by_id.require(JSONHelp.requireInt(root, "race"));
            JSONArray talents = JSONHelp.require(root, "talents", JSONArray.class);     
            
            JSONObject talentInfo = null;
            if (talentIndex == TALENT_SPEC_SELECTED) {
                talentInfo = (JSONObject)talents.get(0); // default to first
                for (Object x: talents) {
                    JSONObject info = (JSONObject)x;                     
                    if (info.containsKey("selected")) {
                        talentInfo = info;
                        break;
                    }
                }
            } else if (talentIndex < 1 || talentIndex > talents.size()) {
                throw new ArmoryError("Invalid Talent Index: " + talentIndex);
            } else {
                talentInfo = (JSONObject)talents.get(talentIndex - 1);
            }           
            int specIndex = SPEC_CHARS.indexOf(JSONHelp.requireStr(talentInfo, "calcSpec"));
            //p.talents = (String)talentsInfo.get("calcTalent");
            //p.glyphs = (String)talentsInfo.get("calcGlyph");
            if (specIndex < 0 || specIndex >= cls.specs.size()) {
                //throw new ArmoryError("No Specialization Active");
                specIndex = 0; // default to first
            }
            p.spec = cls.specs.get(specIndex);
            if (!p.spec.classType.canBe(race)) {
                throw new ArmoryError("Bullshit Race"); // wont happen
            }
            p.race = race;
                        
            JSONObject profData = JSONHelp.require(root, "professions", JSONObject.class);
            JSONArray primary = JSONHelp.require(profData, "primary", JSONArray.class);

            int profIndex = 0;
            for (Object x: primary) {
                JSONObject profInfo = (JSONObject)x;                
                ProfT prof = ProfT.db.by_id.require(JSONHelp.requireInt(profInfo, "id"));                 
                int level = JSONHelp.requireInt(profInfo, "rank");    
                p.getProf(profIndex++).setProf(prof, level);                    
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
        int itemId = JSONHelp.requireInt(root, "id");    
        slot.setItem(wk.wearableMap.get(itemId));
        Wearable item = slot.getItem();
        
        JSONArray bonusList = JSONHelp.get(root, "bonusLists", JSONArray.class);
        if (bonusList != null) {
            IntSet bonuses = new IntSet();
            for (Object x: bonusList) {
                bonuses.add(((Number)x).intValue());
            }            
            slot.setItemBonuses(bonuses);
            if (!bonuses.isEmpty()) {
                errors.accept(String.format("%s: Unknown Item Bonuses: %s", slot.slotType, bonuses));
            }
        }
        
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
    
    
    public String getArmoryURL(String name, String realmSlug, RegionT region) {        
        return "http://" + region.wwwURLPrefix + "/wow/character/" + _realmSlashName(name, realmSlug) + "/advanced";   
    }
    
    public String getWowProgressURL(String name, String realmSlug, RegionT region) {
        //http://www.wowprogress.com/character/us/suramar/Edgy
        return "http://www.wowprogress.com/character/" + region.name.toLowerCase() + "/" + _realmSlashName(name, realmSlug);           
    }
    
    public String getWarcraftLogsURL(String name, String realmSlug, RegionT region) {        
        //http://www.warcraftlogs.com/search/autocomplete?term=
        ArmoryRealm realm = resolveRealm(realmSlug, region);
        name = name.trim().toLowerCase();
        Result res = hc.fetchData("http://www.warcraftlogs.com/search/autocomplete?term=" + urlEncode(name) + "|name=Search-#.json", HttpCache.ONE_DAY, true);
        if (res.error != null) {            
            throw new ArmoryError("Unable to search warcraftlogs.com: " + res.error);
        }
        JSONArray root;
        try {
            root = (JSONArray)JSONValue.parse(new String(res.data, StandardCharsets.UTF_8));
            if (root == null) {
                throw new NullPointerException();
            }
        } catch (RuntimeException err) {
            throw new ArmoryError("Invalid JSON: " + err);
        }        
        String prefix = " - ";
        String suffix = " on " + realm.realmName + " (" + region.name + ")";
        try {
            for (Object x: root) {
                JSONObject info = (JSONObject)x;            
                String label = JSONHelp.requireStr(info, "label"); //"label":"Character - Edgytriangle on Kel'Thuzad (US)"        
                                System.out.println(label);

                if (!label.endsWith(suffix)) {
                    continue;
                }
                int pos = label.indexOf(prefix);
                if (pos == -1) {
                    continue;
                }                
                String charName = label.substring(pos + prefix.length(), label.length() - suffix.length());
                if (charName.equalsIgnoreCase(name)) {
                    return "http://www.warcraftlogs.com/" + JSONHelp.requireStr(info, "link");
                }                
            }    
        } catch (RuntimeException err) {
            throw new ArmoryError("Unable to parse warcraftlogs.com search results");
        }      
        throw new ArmoryError(String.format("Unable to \"%s\" on warcraftlogs.com", name));
    }
    
    
}
