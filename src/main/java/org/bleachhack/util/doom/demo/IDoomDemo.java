package org.bleachhack.util.doom.demo;

import org.bleachhack.util.doom.wad.IWritableDoomObject;
import org.bleachhack.util.doom.defines.skill_t;

public interface IDoomDemo extends IWritableDoomObject{
	
    
    /** Vanilla end demo marker, to append at the end of recorded demos */
    
   public static final int DEMOMARKER =0x80;
   
    /** Get next demo command, in its raw format. Use
     * its own adapters if you need it converted to a 
     * standard ticcmd_t.
     *  
     * @return
     */
    IDemoTicCmd getNextTic();
    
    /** Record a demo command in the IDoomDemo's native format.
     * Use the IDemoTicCmd's objects adaptors to convert it.
     * 
     * @param tic
     */
    void putTic(IDemoTicCmd tic);

    int getVersion();

    void setVersion(int version);

    skill_t getSkill();

    void setSkill(skill_t skill);

    int getEpisode();

    void setEpisode(int episode);

    int getMap();

    void setMap(int map);

    boolean isDeathmatch();

    void setDeathmatch(boolean deathmatch);

    boolean isRespawnparm();
    
    void setRespawnparm(boolean respawnparm);

    boolean isFastparm();

    void setFastparm(boolean fastparm);
    
    boolean isNomonsters();

    void setNomonsters(boolean nomonsters);

    int getConsoleplayer();

    void setConsoleplayer(int consoleplayer);

    boolean[] getPlayeringame();

    void setPlayeringame(boolean[] playeringame);

    void resetDemo();

    


}
