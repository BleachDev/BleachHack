/*
 * Copyright (C) 2017 Good Sign
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.bleachhack.util.doom.defines;

import org.bleachhack.util.doom.doom.DoomMain;
import java.util.logging.Level;
import org.bleachhack.util.doom.mochadoom.Loggers;
import org.bleachhack.util.doom.utils.C2JUtils;
import static org.bleachhack.util.doom.utils.C2JUtils.testReadAccess;

public enum DoomVersion {
    DOOM2F_WAD("doom2f.wad"),
    DOOM2_WAD("doom2.wad"),
    PLUTONIA_WAD("plutonia.wad"),
    TNT_WAD("tnt.wad"),
    XBLA_WAD("xbla.wad"),
    DOOMU_WAD("doomu.wad"),
    UDOOM_WAD("udoom.wad"),
    DOOM_WAD("doom.wad"),
    DOOM1_WAD("doom1.wad"),
    FREEDM_WAD("freedm.wad"),
    FREEDOOM1_WAD("freedoom1.wad"),
    FREEDOOM2_WAD("freedoom2.wad");
    
    public final String wadFileName;

    private DoomVersion(String wadFileName) {
        this.wadFileName = wadFileName;
    }
	
	/**
     * Try all versions in given doomwaddir
     * 
     * @return full path to the wad of success
	 */
    public static String tryAllWads(final DoomMain<?, ?> DOOM, final String doomwaddir) {
        for (DoomVersion v: values()) {
            final String vFullPath = doomwaddir + '/' + v.wadFileName;
            if (testReadAccess(vFullPath)) {
                DOOM.setGameMode(GameMode.forVersion(v));
                if (v == DOOM2F_WAD) {
                    // C'est ridicule!
                    // Let's handle languages in config files, okay?
                    DOOM.language = Language_t.french;
                    System.out.println("French version\n");
                }
                
                return vFullPath;
            }
        }
        
        return null;
    }
	
	/**
     * Try only one IWAD. 
	 * 
	 * @param iwad
     * @param doomwaddir
	 * @return game mode
	 */
	public static GameMode tryOnlyOne(String iwad, String doomwaddir) {
        try {
            // Is it a known and valid version?
            final DoomVersion v = DoomVersion.valueOf(iwad.trim().toUpperCase().replace('.', '_'));
            final GameMode tmp = GameMode.forVersion(v);
            
            // Can we read it?
            if (tmp != null && C2JUtils.testReadAccess(doomwaddir + iwad)) {
                return tmp; // Yes, so communicate the gamemode back.
            }
            
        } catch (IllegalArgumentException ex) {
            Loggers.getLogger(DoomVersion.class.getName()).log(Level.WARNING, iwad, ex);
        }

		// It's either invalid or we can't read it.
		// Fuck that.
		return null;
	}
}
