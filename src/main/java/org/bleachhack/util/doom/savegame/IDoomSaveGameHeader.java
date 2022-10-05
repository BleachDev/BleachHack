package org.bleachhack.util.doom.savegame;

import org.bleachhack.util.doom.defines.skill_t;


/** A Save Game Header should be able to be loaded quickly and return 
 *  some basic info about it (name, version, game time, etc.) in an unified
 *  manner, no matter what actual format you use for saving.
 * 
 * @author admin
 *
 */

public interface IDoomSaveGameHeader {

    String getName();

    void setName(String name);

    skill_t getGameskill();

    void setGameskill(skill_t gameskill);
    
    String getVersion();

    void setVersion(String vcheck);

    int getGameepisode();
    
    void setGameepisode(int gameepisode);

    boolean isProperend();

    void setWrongversion(boolean wrongversion);

    boolean isWrongversion();

    void setLeveltime(int leveltime);

    int getLeveltime();

    void setPlayeringame(boolean[] playeringame);

    boolean[] getPlayeringame();

    void setGamemap(int gamemap);

    int getGamemap();

}
