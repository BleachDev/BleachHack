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

import static org.bleachhack.util.doom.data.Defines.ITEMQUESIZE;
import static org.bleachhack.util.doom.data.Defines.MELEERANGE;
import org.bleachhack.util.doom.data.mapthing_t;
import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.defines.statenum_t;
import org.bleachhack.util.doom.doom.SourceCode.P_Map;
import static org.bleachhack.util.doom.doom.SourceCode.P_Map.PIT_ChangeSector;
import org.bleachhack.util.doom.doom.SourceCode.fixed_t;
import java.util.logging.Logger;
import static org.bleachhack.util.doom.m.BBox.BOXBOTTOM;
import static org.bleachhack.util.doom.m.BBox.BOXLEFT;
import static org.bleachhack.util.doom.m.BBox.BOXRIGHT;
import static org.bleachhack.util.doom.m.BBox.BOXTOP;
import org.bleachhack.util.doom.mochadoom.Loggers;
import org.bleachhack.util.doom.p.AbstractLevelLoader;
import org.bleachhack.util.doom.p.ActiveStates;
import org.bleachhack.util.doom.p.divline_t;
import org.bleachhack.util.doom.p.floor_e;
import org.bleachhack.util.doom.p.floormove_t;
import org.bleachhack.util.doom.p.mobj_t;
import static org.bleachhack.util.doom.p.mobj_t.MF_DROPPED;
import static org.bleachhack.util.doom.p.mobj_t.MF_SHOOTABLE;
import static org.bleachhack.util.doom.p.mobj_t.MF_SOLID;
import org.bleachhack.util.doom.p.result_e;
import org.bleachhack.util.doom.rr.line_t;
import static org.bleachhack.util.doom.rr.line_t.ML_TWOSIDED;
import org.bleachhack.util.doom.rr.sector_t;
import org.bleachhack.util.doom.rr.side_t;
import static org.bleachhack.util.doom.utils.C2JUtils.eval;
import org.bleachhack.util.doom.utils.TraitFactory.ContextKey;

public interface ActionsSectors extends ActionsLights, ActionsFloors, ActionsDoors, ActionsCeilings, ActionsSlideDoors {

    ContextKey<RespawnQueue> KEY_RESP_QUEUE = ACTION_KEY_CHAIN.newKey(ActionsSectors.class, RespawnQueue::new);
    ContextKey<Spawn> KEY_SPAWN = ACTION_KEY_CHAIN.newKey(ActionsSectors.class, Spawn::new);
    ContextKey<Crushes> KEY_CRUSHES = ACTION_KEY_CHAIN.newKey(ActionsSectors.class, Crushes::new);

    void RemoveMobj(mobj_t thing);
    void DamageMobj(mobj_t thing, mobj_t tmthing, mobj_t tmthing0, int damage);
    mobj_t SpawnMobj(@fixed_t int x, @fixed_t int y, @fixed_t int z, mobjtype_t type);

    final class Crushes {

        boolean crushchange;
        boolean nofit;
    }

    final class RespawnQueue {

        //
        // P_RemoveMobj
        //
        mapthing_t[] itemrespawnque = new mapthing_t[ITEMQUESIZE];
        int[] itemrespawntime = new int[ITEMQUESIZE];
        int iquehead;
        int iquetail;
    }

    final class Spawn {

        final static Logger LOGGER = Loggers.getLogger(ActionsSectors.class.getName());

        /**
         * who got hit (or NULL)
         */
        public mobj_t linetarget;

        @fixed_t
        public int attackrange;

        public mobj_t shootthing;
        // Height if not aiming up or down
        // ???: use slope for monsters?
        @fixed_t
        public int shootz;

        public int la_damage;

        @fixed_t
        public int aimslope;

        public divline_t trace = new divline_t();

        public int topslope, bottomslope; // slopes to top and bottom of target

        //
        // P_BulletSlope
        // Sets a slope so a near miss is at aproximately
        // the height of the intended target
        //
        public int bulletslope;

        boolean isMeleeRange() {
            return attackrange == MELEERANGE;
        }
    }

    //
    // P_ChangeSector
    //
    //
    // SECTOR HEIGHT CHANGING
    // After modifying a sectors floor or ceiling height,
    // call this routine to adjust the positions
    // of all things that touch the sector.
    //
    // If anything doesn't fit anymore, true will be returned.
    // If crunch is true, they will take damage
    //  as they are being crushed.
    // If Crunch is false, you should set the sector height back
    //  the way it was and call P_ChangeSector again
    //  to undo the changes.
    //
    default boolean ChangeSector(sector_t sector, boolean crunch) {
        final Crushes cr = contextRequire(KEY_CRUSHES);
        int x;
        int y;

        cr.nofit = false;
        cr.crushchange = crunch;

        // re-check heights for all things near the moving sector
        for (x = sector.blockbox[BOXLEFT]; x <= sector.blockbox[BOXRIGHT]; x++) {
            for (y = sector.blockbox[BOXBOTTOM]; y <= sector.blockbox[BOXTOP]; y++) {
                this.BlockThingsIterator(x, y, this::ChangeSector);
            }
        }

        return cr.nofit;
    }

    /**
     * PIT_ChangeSector
     */
    @P_Map.C(PIT_ChangeSector)
    default boolean ChangeSector(mobj_t thing) {
        final Crushes cr = contextRequire(KEY_CRUSHES);
        mobj_t mo;

        if (ThingHeightClip(thing)) {
            // keep checking
            return true;
        }

        // crunch bodies to giblets
        if (thing.health <= 0) {
            thing.SetMobjState(statenum_t.S_GIBS);

            thing.flags &= ~MF_SOLID;
            thing.height = 0;
            thing.radius = 0;

            // keep checking
            return true;
        }

        // crunch dropped items
        if (eval(thing.flags & MF_DROPPED)) {
            RemoveMobj(thing);

            // keep checking
            return true;
        }

        if (!eval(thing.flags & MF_SHOOTABLE)) {
            // assume it is bloody gibs or something
            return true;
        }

        cr.nofit = true;

        if (cr.crushchange && !eval(LevelTime() & 3)) {
            DamageMobj(thing, null, null, 10);

            // spray blood in a random direction
            mo = SpawnMobj(thing.x, thing.y, thing.z + thing.height / 2, mobjtype_t.MT_BLOOD);

            mo.momx = (P_Random() - P_Random()) << 12;
            mo.momy = (P_Random() - P_Random()) << 12;
        }

        // keep checking (crush other things)   
        return true;
    }

    ;

    /**
     * Move a plane (floor or ceiling) and check for crushing
     *
     * @param sector
     * @param speed fixed
     * @param dest fixed
     * @param crush
     * @param floorOrCeiling
     * @param direction
     */
    @Override
    default result_e MovePlane(sector_t sector, int speed, int dest, boolean crush, int floorOrCeiling, int direction) {
        boolean flag;
        @fixed_t
        int lastpos;

        switch (floorOrCeiling) {
            case 0:
                // FLOOR
                switch (direction) {
                    case -1:
                        // DOWN
                        if (sector.floorheight - speed < dest) {
                            lastpos = sector.floorheight;
                            sector.floorheight = dest;
                            flag = ChangeSector(sector, crush);
                            if (flag == true) {
                                sector.floorheight = lastpos;
                                ChangeSector(sector, crush);
                                //return crushed;
                            }
                            return result_e.pastdest;
                        } else {
                            lastpos = sector.floorheight;
                            sector.floorheight -= speed;
                            flag = ChangeSector(sector, crush);
                            if (flag == true) {
                                sector.floorheight = lastpos;
                                ChangeSector(sector, crush);
                                return result_e.crushed;
                            }
                        }
                        break;

                    case 1:
                        // UP
                        if (sector.floorheight + speed > dest) {
                            lastpos = sector.floorheight;
                            sector.floorheight = dest;
                            flag = ChangeSector(sector, crush);
                            if (flag == true) {
                                sector.floorheight = lastpos;
                                ChangeSector(sector, crush);
                                //return crushed;
                            }
                            return result_e.pastdest;
                        } else {
                            // COULD GET CRUSHED
                            lastpos = sector.floorheight;
                            sector.floorheight += speed;
                            flag = ChangeSector(sector, crush);
                            if (flag == true) {
                                if (crush == true) {
                                    return result_e.crushed;
                                }
                                sector.floorheight = lastpos;
                                ChangeSector(sector, crush);
                                return result_e.crushed;
                            }
                        }
                        break;
                }
                break;

            case 1:
                // CEILING
                switch (direction) {
                    case -1:
                        // DOWN
                        if (sector.ceilingheight - speed < dest) {
                            lastpos = sector.ceilingheight;
                            sector.ceilingheight = dest;
                            flag = ChangeSector(sector, crush);

                            if (flag == true) {
                                sector.ceilingheight = lastpos;
                                ChangeSector(sector, crush);
                                //return crushed;
                            }
                            return result_e.pastdest;
                        } else {
                            // COULD GET CRUSHED
                            lastpos = sector.ceilingheight;
                            sector.ceilingheight -= speed;
                            flag = ChangeSector(sector, crush);

                            if (flag == true) {
                                if (crush == true) {
                                    return result_e.crushed;
                                }
                                sector.ceilingheight = lastpos;
                                ChangeSector(sector, crush);
                                return result_e.crushed;
                            }
                        }
                        break;

                    case 1:
                        // UP
                        if (sector.ceilingheight + speed > dest) {
                            lastpos = sector.ceilingheight;
                            sector.ceilingheight = dest;
                            flag = ChangeSector(sector, crush);
                            if (flag == true) {
                                sector.ceilingheight = lastpos;
                                ChangeSector(sector, crush);
                                //return crushed;
                            }
                            return result_e.pastdest;
                        } else {
                            lastpos = sector.ceilingheight;
                            sector.ceilingheight += speed;
                            flag = ChangeSector(sector, crush);
                            // UNUSED
                            /*
                            if (flag == true)
                            {
                                sector.ceilingheight = lastpos;
                                P_ChangeSector(sector,crush);
                                return crushed;
                            }
                             */
                        }
                        break;
                }
                break;

        }
        return result_e.ok;
    }

    /**
     * Special Stuff that can not be categorized
     *
     * (I'm sure it has something to do with John Romero's obsession with fucking stuff and making them his bitches).
     *
     * @param line
     *
     */
    @Override
    default boolean DoDonut(line_t line) {
        sector_t s1;
        sector_t s2;
        sector_t s3;
        int secnum;
        boolean rtn;
        int i;
        floormove_t floor;

        secnum = -1;
        rtn = false;
        while ((secnum = FindSectorFromLineTag(line, secnum)) >= 0) {
            s1 = levelLoader().sectors[secnum];

            // ALREADY MOVING?  IF SO, KEEP GOING...
            if (s1.specialdata != null) {
                continue;
            }

            rtn = true;
            s2 = s1.lines[0].getNextSector(s1);
            for (i = 0; i < s2.linecount; i++) {
                if ((!eval(s2.lines[i].flags & ML_TWOSIDED)) || (s2.lines[i].backsector == s1)) {
                    continue;
                }
                s3 = s2.lines[i].backsector;

                //  Spawn rising slime
                floor = new floormove_t();
                s2.specialdata = floor;
                floor.thinkerFunction = ActiveStates.T_MoveFloor;
                AddThinker(floor);
                floor.type = floor_e.donutRaise;
                floor.crush = false;
                floor.direction = 1;
                floor.sector = s2;
                floor.speed = FLOORSPEED / 2;
                floor.texture = s3.floorpic;
                floor.newspecial = 0;
                floor.floordestheight = s3.floorheight;

                //  Spawn lowering donut-hole
                floor = new floormove_t();
                s1.specialdata = floor;
                floor.thinkerFunction = ActiveStates.T_MoveFloor;
                AddThinker(floor);
                floor.type = floor_e.lowerFloor;
                floor.crush = false;
                floor.direction = -1;
                floor.sector = s1;
                floor.speed = FLOORSPEED / 2;
                floor.floordestheight = s3.floorheight;
                break;
            }
        }
        return rtn;
    }

    /**
     * RETURN NEXT SECTOR # THAT LINE TAG REFERS TO
     */
    @Override
    default int FindSectorFromLineTag(line_t line, int start) {
        final AbstractLevelLoader ll = levelLoader();

        for (int i = start + 1; i < ll.numsectors; i++) {
            if (ll.sectors[i].tag == line.tag) {
                return i;
            }
        }

        return -1;
    }

    //
    // UTILITIES
    //
    //
    // getSide()
    // Will return a side_t*
    // given the number of the current sector,
    // the line number, and the side (0/1) that you want.
    //
    @Override
    default side_t getSide(int currentSector, int line, int side) {
        final AbstractLevelLoader ll = levelLoader();
        return ll.sides[(ll.sectors[currentSector].lines[line]).sidenum[side]];
    }

    /**
     * getSector()
     * Will return a sector_t
     * given the number of the current sector,
     * the line number and the side (0/1) that you want.
     */
    @Override
    default sector_t getSector(int currentSector, int line, int side) {
        final AbstractLevelLoader ll = levelLoader();
        return ll.sides[(ll.sectors[currentSector].lines[line]).sidenum[side]].sector;
    }

    /**
     * twoSided()
     * Given the sector number and the line number,
     * it will tell you whether the line is two-sided or not.
     */
    @Override
    default boolean twoSided(int sector, int line) {
        return eval((levelLoader().sectors[sector].lines[line]).flags & ML_TWOSIDED);
    }
    
    default void ClearRespawnQueue() {
        // clear special respawning que
        final RespawnQueue rq = contextRequire(KEY_RESP_QUEUE);
        rq.iquehead = rq.iquetail = 0;
    }
}
