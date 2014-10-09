package warkit.player;

import warbase.types.ProfT;

public class PlayerProf {

    public final int index;
    public final Player owner;
    
    PlayerProf(Player owner, int index) {
        this.owner = owner;
        this.index = index;
    }     
    
    ProfT prof;
    int level;
    
    public boolean hasProf() {
        return prof != null;
    }
    
    public ProfT getProf() {
        return prof;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setProf(ProfT newProf) {
        if (prof == newProf) {
            return;
        }
        if (ProfT.checkBit(owner._profBits, newProf)) {
            throw new PlayerError(newProf + " is already active");
        }
        clear();
        prof = newProf;
        level = 1;
        owner._profBits |= newProf.getBit();
    }
    
    public void setLevel(int newLevel) {
        if (prof == null) {
            throw new PlayerError("Empty professions cannot have a skill level.");
        }
        if (newLevel == 0) {
            newLevel = Player.MAX_PROF_LEVEL;
        } else if (newLevel < 1 || newLevel > Player.MAX_PROF_LEVEL) {
            throw new PlayerError(String.format("%s (%d) is not valid [1,%d]", prof, newLevel, Player.MAX_PROF_LEVEL));
        }
        level = newLevel;            
    }     
    
    public void clear() {
        if (prof != null) {
            owner._profBits &= ~prof.getBit();
            prof = null; 
        }
    }
    
    public void copy(PlayerProf other) {
        if (other.prof == null) {
            clear();
        } else {
            setProf(other.prof);  
            setLevel(other.level);
        }
    }
}
