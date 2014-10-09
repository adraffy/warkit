#WarKit
**A high-level World of Warcraft toolkit for Java**

*Warning: this document is a work-in-progress.*

####Instructions
1. download single-jar library: WarKit.jar
2. download latest database: WKDB.dat
3. include WarKit in your Java project
4. make sure WKDB.dat is accessible from your application

####Random Examples
```java
// load warkit
WarKit wk = WarKit.load();

// create an armory 
Armory a = new Armory(wk, new HttpCache());

// get realm list for US
a.getRealmList(RegionT.US).forEach(System.out::println);

// find all Druids named "Edgy" on US
a.find("Fdgy", RegionT.US, 0, (name, realmName, realmSlug, level, race, cls) -> cls == ClassT.DRUID);

// import "Edgy/Suramar" from US Armory
Player p = a.getPlayer("Edgy", "Suramar", RegionT.US, false, System.out::println);

// and then change the gear a bit:
p.HEAD.clear();
p.FEET.clearGems(); 
p.NECK.setUpgradeLevel(4); 
p.MH.getSocket(0).setGem(wk.gemMap.get(12345)); 
p.F1.setBonuses(566, 42); 
p.WAIST.setCustomItemLevel(900);
p.T2.setUpgradeLevelMax();
p.T1.setExtraSocket(true);
p.HANDS.setItem(wk.wearableMap.get(23456));
p.getProf(0).setProf(ProfT.BS, 699);

// most changes are dynamic, but some require manual validation
p.playerLevel = 90;
p.spec = SpecT.GUARDIAN;
p.validate();

// load to characters and diff them
Player p1 = ...
Player p2 = ...
PlayerDiff diff = PlayerDiff.compare(p1, p2);
System.out.println(diff.title);
System.out.println(diff.text);

// import from compact gear string
Player p = CompactGear.fromString("12345\n56789 :12345 $4125");

// export to compact gear string
String code = CompactGear.toString(p);

// import from simc profile string
Player p = SimcProfile.fromString("hands=bigswordofthebear,id=12345");

// export to simc profile string
String code = SimcProfile.toString(p);

```


