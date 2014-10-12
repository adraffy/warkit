package com.antistupid.warkit.player;

import com.antistupid.warbase.types.ProfT;

public class PlayerProf {

    public final int index;
    public final Player owner;
    // should we add support for secondary profs?
    
    PlayerProf(Player owner, int index) {
        this.owner = owner;
        this.index = index;
    }     
    
    // i dont want these accessed directly
    ProfT _prof;
    int _level;
    
    public boolean hasProf() {
        return _prof != null;
    }
    
    public ProfT getProf() {
        return _prof;
    }
    
    public int getLevel() {
        return _level;
    }
    
    public void setProf(ProfT p) { setProf(p, 0); }
    public void setProf(ProfT p, int level) {
        if (_prof == p) {
            return;
        } else if (p == null) {
            clear();
            return;
        } else if (!p.primary) {
            throw new PlayerError(p + " is not a primary profession");
        }
        if (ProfT.checkBit(owner._profBits, p)) {
            throw new PlayerError(p + " is already active");
        }
        level = checkLevel(p, level); // do this first so we can throw    
        clear();
        _prof = p;
        _level = level;
        owner._profBits |= p.getBit();
    }
    
    static int checkLevel(ProfT prof, int level) {
        if (level == 0) {
            return Player.MAX_PROF_LEVEL;
        } else if (level < 1 || level > Player.MAX_PROF_LEVEL) {
            throw new PlayerError(String.format("%s (%d) is not valid [1,%d]", prof, level, Player.MAX_PROF_LEVEL));
        } else {
            return level;
        }
    }
    
    public void setLevel(int newLevel) {
        if (_prof == null) {
            throw new PlayerError("Empty professions cannot have a skill level");
        }
        _level = checkLevel(_prof, newLevel);            
    }     
    
    public void clear() {
        if (_prof != null) {
            owner._profBits &= ~_prof.getBit();
            _prof = null; 
        }
    }
    
    public void copy(PlayerProf other) {
        if (other._prof == null) {
            clear();
        } else {
            setProf(other._prof, other._level);
        }
    }
}
