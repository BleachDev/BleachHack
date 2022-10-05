/*
 * Copyright (C) 1993-1996 by id Software, Inc.
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
package org.bleachhack.util.doom.p.Actions;

import org.bleachhack.util.doom.doom.SourceCode;
import org.bleachhack.util.doom.doom.SourceCode.P_Lights;
import static org.bleachhack.util.doom.doom.SourceCode.P_Lights.P_SpawnFireFlicker;
import static org.bleachhack.util.doom.doom.SourceCode.P_Lights.P_SpawnGlowingLight;
import static org.bleachhack.util.doom.doom.SourceCode.P_Lights.P_SpawnLightFlash;
import static org.bleachhack.util.doom.doom.SourceCode.P_Lights.P_SpawnStrobeFlash;
import org.bleachhack.util.doom.doom.SourceCode.P_Spec;
import static org.bleachhack.util.doom.doom.SourceCode.P_Spec.P_FindMinSurroundingLight;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.bleachhack.util.doom.p.AbstractLevelLoader;
import static org.bleachhack.util.doom.p.ActiveStates.T_FireFlicker;
import static org.bleachhack.util.doom.p.ActiveStates.T_Glow;
import static org.bleachhack.util.doom.p.ActiveStates.T_LightFlash;
import static org.bleachhack.util.doom.p.ActiveStates.T_StrobeFlash;
import static org.bleachhack.util.doom.p.DoorDefines.SLOWDARK;
import static org.bleachhack.util.doom.p.DoorDefines.STROBEBRIGHT;
import org.bleachhack.util.doom.p.strobe_t;
import org.bleachhack.util.doom.rr.SectorAction;
import org.bleachhack.util.doom.rr.line_t;
import org.bleachhack.util.doom.rr.sector_t;
import org.bleachhack.util.doom.wad.DoomIO;

public interface ActionsLights extends ActionsMoveEvents, ActionsUseEvents {

    int FindSectorFromLineTag(line_t line, int secnum);
    
    //
    // P_LIGHTS
    //
    public class fireflicker_t extends SectorAction {
        public int count;
        public int maxlight;
        public int minlight;
    }

    //
    // BROKEN LIGHT EFFECT
    //
    public class lightflash_t extends SectorAction {

        public int count;
        public int maxlight;
        public int minlight;
        public int maxtime;
        public int mintime;

        @Override
        public void read(DataInputStream f) throws IOException {
            super.read(f); // Call thinker reader first            
            super.sectorid = DoomIO.readLEInt(f); // Sector index
            count = DoomIO.readLEInt(f);
            maxlight = DoomIO.readLEInt(f);
            minlight = DoomIO.readLEInt(f);
            maxtime = DoomIO.readLEInt(f);
            mintime = DoomIO.readLEInt(f);
        }

        @Override
        public void pack(ByteBuffer b) throws IOException {
            super.pack(b); //12            
            b.putInt(super.sectorid); // 16
            b.putInt(count); //20
            b.putInt(maxlight);//24
            b.putInt(minlight);//28
            b.putInt(maxtime);//32
            b.putInt(mintime);//36
        }
    }
    
    public class glow_t extends SectorAction {

        public int minlight;
        public int maxlight;
        public int direction;


        @Override
        public void read(DataInputStream f) throws IOException {

            super.read(f); // Call thinker reader first            
            super.sectorid = DoomIO.readLEInt(f); // Sector index
            minlight = DoomIO.readLEInt(f);
            maxlight = DoomIO.readLEInt(f);
            direction = DoomIO.readLEInt(f);
        }

        @Override
        public void pack(ByteBuffer b) throws IOException {
            super.pack(b); //12            
            b.putInt(super.sectorid); // 16
            b.putInt(minlight);//20
            b.putInt(maxlight);//24
            b.putInt(direction);//38
        }
    }
    
    //
    // Find minimum light from an adjacent sector
    //
    @SourceCode.Exact
    @P_Spec.C(P_FindMinSurroundingLight)
    default int FindMinSurroundingLight(sector_t sector, int max) {
        int min;
        line_t line;
        sector_t check;

        min = max;
        for (int i = 0; i < sector.linecount; i++) {
            line = sector.lines[i];
            getNextSector: {
                check = line.getNextSector(sector);
            }

            if (check == null) {
                continue;
            }

            if (check.lightlevel < min) {
                min = check.lightlevel;
            }
        }
        return min;
    }
    
    /**
     * P_SpawnLightFlash After the map has been loaded, scan each sector for
     * specials that spawn thinkers
     */
    @SourceCode.Exact
    @P_Lights.C(P_SpawnLightFlash)
    default void SpawnLightFlash(sector_t sector) {
        lightflash_t flash;

        // nothing special about it during gameplay
        sector.special = 0;

        Z_Malloc: {
            flash = new lightflash_t();
        }

        P_AddThinker: {
            AddThinker(flash);
        }

        flash.thinkerFunction = T_LightFlash;
        flash.sector = sector;
        flash.maxlight = sector.lightlevel;

        flash.minlight = FindMinSurroundingLight(sector, sector.lightlevel);
        flash.maxtime = 64;
        flash.mintime = 7;
        flash.count = (P_Random() & flash.maxtime) + 1;
    }

    //
    // P_SpawnStrobeFlash
    // After the map has been loaded, scan each sector
    // for specials that spawn thinkers
    //
    @SourceCode.Exact
    @P_Lights.C(P_SpawnStrobeFlash)
    default void SpawnStrobeFlash(sector_t sector, int fastOrSlow, int inSync) {
        strobe_t flash;

        Z_Malloc: {
            flash = new strobe_t();
        }

        P_AddThinker: {
            AddThinker(flash);
        }

        flash.sector = sector;
        flash.darktime = fastOrSlow;
        flash.brighttime = STROBEBRIGHT;
        flash.thinkerFunction = T_StrobeFlash;
        flash.maxlight = sector.lightlevel;
        flash.minlight = FindMinSurroundingLight(sector, sector.lightlevel);

        if (flash.minlight == flash.maxlight) {
            flash.minlight = 0;
        }

        // nothing special about it during gameplay
        sector.special = 0;

        if (inSync == 0) {
            flash.count = (P_Random() & 7) + 1;
        } else {
            flash.count = 1;
        }
    }

    @SourceCode.Exact
    @P_Lights.C(P_SpawnGlowingLight)
    default void SpawnGlowingLight(sector_t sector) {
        glow_t g;

        Z_Malloc: {
            g = new glow_t();
        }

        P_AddThinker: {
            AddThinker(g);
        }

        g.sector = sector;
        P_FindMinSurroundingLight: {
            g.minlight = FindMinSurroundingLight(sector, sector.lightlevel);
        }
        g.maxlight = sector.lightlevel;
        g.thinkerFunction = T_Glow;
        g.direction = -1;

        sector.special = 0;
    }

    //
    // Start strobing lights (usually from a trigger)
    //
    @Override
    default void StartLightStrobing(line_t line) {
        final AbstractLevelLoader ll = levelLoader();

        int secnum;
        sector_t sec;

        secnum = -1;
        while ((secnum = FindSectorFromLineTag(line, secnum)) >= 0) {
            sec = ll.sectors[secnum];
            if (sec.specialdata != null) {
                continue;
            }

            SpawnStrobeFlash(sec, SLOWDARK, 0);
        }
    }

    //
    // P_SpawnFireFlicker
    //
    @SourceCode.Exact
    @P_Lights.C(P_SpawnFireFlicker)
    default void SpawnFireFlicker(sector_t sector) {
        fireflicker_t flick;

        // Note that we are resetting sector attributes.
        // Nothing special about it during gameplay.
        sector.special = 0;

        Z_Malloc: {
            flick = new fireflicker_t();
        }
        
        P_AddThinker: {
            AddThinker(flick);
        }
        
        flick.thinkerFunction = T_FireFlicker;
        flick.sector = sector;
        flick.maxlight = sector.lightlevel;
        flick.minlight = FindMinSurroundingLight(sector, sector.lightlevel) + 16;
        flick.count = 4;
    }

    //
    // TURN LINE'S TAG LIGHTS OFF
    //
    @Override
    default void TurnTagLightsOff(line_t line) {
        final AbstractLevelLoader ll = levelLoader();

        int i;
        int min;
        sector_t sector;
        sector_t tsec;
        line_t templine;

        for (int j = 0; j < ll.numsectors; j++) {
            sector = ll.sectors[j];
            if (sector.tag == line.tag) {

                min = sector.lightlevel;
                for (i = 0; i < sector.linecount; i++) {
                    templine = sector.lines[i];
                    tsec = templine.getNextSector(sector);
                    if (tsec == null) {
                        continue;
                    }
                    if (tsec.lightlevel < min) {
                        min = tsec.lightlevel;
                    }
                }
                sector.lightlevel = (short) min;
            }
        }
    }

    //
    // TURN LINE'S TAG LIGHTS ON
    //
    @Override
    default void LightTurnOn(line_t line, int bright) {
        final AbstractLevelLoader ll = levelLoader();

        sector_t sector;
        sector_t temp;
        line_t templine;

        for (int i = 0; i < ll.numsectors; i++) {
            sector = ll.sectors[i];
            if (sector.tag == line.tag) {
                // bright = 0 means to search
                // for highest light level
                // surrounding sector
                if (bright == 0) {
                    for (int j = 0; j < sector.linecount; j++) {
                        templine = sector.lines[j];
                        temp = templine.getNextSector(sector);

                        if (temp == null) {
                            continue;
                        }

                        if (temp.lightlevel > bright) {
                            bright = temp.lightlevel;
                        }
                    }
                }
                sector.lightlevel = (short) bright;
            }
        }
    }
}
