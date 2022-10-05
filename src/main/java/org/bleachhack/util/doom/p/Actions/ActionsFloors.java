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

import static org.bleachhack.util.doom.data.Limits.MAXINT;
import org.bleachhack.util.doom.data.sounds;
import org.bleachhack.util.doom.m.fixed_t;
import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import org.bleachhack.util.doom.p.ActiveStates;
import org.bleachhack.util.doom.p.floor_e;
import org.bleachhack.util.doom.p.floormove_t;
import org.bleachhack.util.doom.p.plat_e;
import org.bleachhack.util.doom.p.plat_t;
import org.bleachhack.util.doom.p.plattype_e;
import org.bleachhack.util.doom.p.result_e;
import org.bleachhack.util.doom.p.stair_e;
import org.bleachhack.util.doom.rr.line_t;
import static org.bleachhack.util.doom.rr.line_t.ML_TWOSIDED;
import org.bleachhack.util.doom.rr.sector_t;
import org.bleachhack.util.doom.rr.side_t;
import static org.bleachhack.util.doom.utils.C2JUtils.eval;

public interface ActionsFloors extends ActionsPlats {

    result_e MovePlane(sector_t sector, int speed, int floordestheight, boolean crush, int i, int direction);
    boolean twoSided(int secnum, int i);
    side_t getSide(int secnum, int i, int s);
    sector_t getSector(int secnum, int i, int i0);

    //
    // FLOORS
    //
    int FLOORSPEED = fixed_t.MAPFRACUNIT;

    /**
     * MOVE A FLOOR TO IT'S DESTINATION (UP OR DOWN)
     */
    default void MoveFloor(floormove_t floor) {
        final result_e res = MovePlane(floor.sector, floor.speed, floor.floordestheight, floor.crush, 0, floor.direction);

        if (!eval(LevelTime() & 7)) {
            StartSound(floor.sector.soundorg, sounds.sfxenum_t.sfx_stnmov);
        }

        if (res == result_e.pastdest) {
            floor.sector.specialdata = null;

            if (floor.direction == 1) {
                switch (floor.type) {
                    case donutRaise:
                        floor.sector.special = (short) floor.newspecial;
                        floor.sector.floorpic = floor.texture;
                    default:
                        break;
                }
            } else if (floor.direction == -1) {
                switch (floor.type) //TODO: check if a null floor.type is valid or a bug 
                // MAES: actually, type should always be set to something.
                // In C, this means "zero" or "null". In Java, we must make sure
                // it's actually set to something all the time.
                {
                    case lowerAndChange:
                        floor.sector.special = (short) floor.newspecial;
                        floor.sector.floorpic = floor.texture;
                    default:
                    	break;
                }
            }

            RemoveThinker(floor);
            StartSound(floor.sector.soundorg, sounds.sfxenum_t.sfx_pstop);
        }
    }

    //
    // HANDLE FLOOR TYPES
    //
    @Override
    default boolean DoFloor(line_t line, floor_e floortype) {
        int secnum = -1;
        boolean rtn = false;
        sector_t sec;
        floormove_t floor;

        while ((secnum = FindSectorFromLineTag(line, secnum)) >= 0) {
            sec = levelLoader().sectors[secnum];

            // ALREADY MOVING?  IF SO, KEEP GOING...
            if (sec.specialdata != null) {
                continue;
            }

            // new floor thinker
            rtn = true;
            floor = new floormove_t();
            sec.specialdata = floor;
            floor.thinkerFunction = ActiveStates.T_MoveFloor;
            AddThinker(floor);
            floor.type = floortype;
            floor.crush = false;

            switch (floortype) {
                case lowerFloor:
                    floor.direction = -1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED;
                    floor.floordestheight = sec.FindHighestFloorSurrounding();
                    break;

                case lowerFloorToLowest:
                    floor.direction = -1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED;
                    floor.floordestheight = sec.FindLowestFloorSurrounding();
                    break;

                case turboLower:
                    floor.direction = -1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED * 4;
                    floor.floordestheight = sec.FindHighestFloorSurrounding();
                    if (floor.floordestheight != sec.floorheight) {
                        floor.floordestheight += 8 * FRACUNIT;
                    }
                    break;

                case raiseFloorCrush:
                    floor.crush = true;
                case raiseFloor:
                    floor.direction = 1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED;
                    floor.floordestheight = sec.FindLowestCeilingSurrounding();
                    if (floor.floordestheight > sec.ceilingheight) {
                        floor.floordestheight = sec.ceilingheight;
                    }
                    floor.floordestheight -= (8 * FRACUNIT)
                        * eval(floortype == floor_e.raiseFloorCrush);
                    break;

                case raiseFloorTurbo:
                    floor.direction = 1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED * 4;
                    floor.floordestheight = sec.FindNextHighestFloor(sec.floorheight);
                    break;

                case raiseFloorToNearest:
                    floor.direction = 1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED;
                    floor.floordestheight = sec.FindNextHighestFloor(sec.floorheight);
                    break;

                case raiseFloor24:
                    floor.direction = 1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED;
                    floor.floordestheight = floor.sector.floorheight + 24 * FRACUNIT;
                    break;
                case raiseFloor512:
                    floor.direction = 1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED;
                    floor.floordestheight = floor.sector.floorheight + 512 * FRACUNIT;
                    break;

                case raiseFloor24AndChange:
                    floor.direction = 1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED;
                    floor.floordestheight = floor.sector.floorheight + 24 * FRACUNIT;
                    sec.floorpic = line.frontsector.floorpic;
                    sec.special = line.frontsector.special;
                    break;

                case raiseToTexture: {
                    int minsize = MAXINT;
                    side_t side;

                    floor.direction = 1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED;
                    for (int i = 0; i < sec.linecount; ++i) {
                        if (twoSided(secnum, i)) {
                            for (int s = 0; s < 2; ++s) {
                                side = getSide(secnum, i, s);
                                if (side.bottomtexture >= 0) {
                                    if (DOOM().textureManager.getTextureheight(side.bottomtexture) < minsize) {
                                        minsize = DOOM().textureManager.getTextureheight(side.bottomtexture);
                                    }
                                }
                            }
                        }
                    }
                    floor.floordestheight = floor.sector.floorheight + minsize;
                }
                break;

                case lowerAndChange:
                    floor.direction = -1;
                    floor.sector = sec;
                    floor.speed = FLOORSPEED;
                    floor.floordestheight = sec.FindLowestFloorSurrounding();
                    floor.texture = sec.floorpic;

                    for (int i = 0; i < sec.linecount; i++) {
                        if (twoSided(secnum, i)) {
                            if (getSide(secnum, i, 0).sector.id == secnum) {
                                sec = getSector(secnum, i, 1);
                                if (sec.floorheight == floor.floordestheight) {
                                    floor.texture = sec.floorpic;
                                    floor.newspecial = sec.special;
                                    break;
                                }
                            } else {
                                sec = getSector(secnum, i, 0);
                                if (sec.floorheight == floor.floordestheight) {
                                    floor.texture = sec.floorpic;
                                    floor.newspecial = sec.special;
                                    break;
                                }
                            }
                        }
                    }
                default:
                	break;
            }
        }
        return rtn;
    }

    /**
     * BUILD A STAIRCASE!
     */
    @Override
    default boolean BuildStairs(line_t line, stair_e type) {
        int secnum;
        int height;
        int i;
        int newsecnum;
        int texture;
        boolean ok;
        boolean rtn;

        sector_t sec;
        sector_t tsec;

        floormove_t floor;

        int stairsize = 0;
        int speed = 0; // shut up compiler

        secnum = -1;
        rtn = false;
        while ((secnum = FindSectorFromLineTag(line, secnum)) >= 0) {
            sec = levelLoader().sectors[secnum];

            // ALREADY MOVING?  IF SO, KEEP GOING...
            if (sec.specialdata != null) {
                continue;
            }

            // new floor thinker
            rtn = true;
            floor = new floormove_t();
            sec.specialdata = floor;
            floor.thinkerFunction = ActiveStates.T_MoveFloor;
            AddThinker(floor);
            floor.direction = 1;
            floor.sector = sec;
            switch (type) {
                case build8:
                    speed = FLOORSPEED / 4;
                    stairsize = 8 * FRACUNIT;
                    break;
                case turbo16:
                    speed = FLOORSPEED * 4;
                    stairsize = 16 * FRACUNIT;
                    break;
            }
            floor.speed = speed;
            height = sec.floorheight + stairsize;
            floor.floordestheight = height;

            texture = sec.floorpic;

            // Find next sector to raise
            // 1.   Find 2-sided line with same sector side[0]
            // 2.   Other side is the next sector to raise
            do {
                ok = false;
                for (i = 0; i < sec.linecount; i++) {
                    if (!eval((sec.lines[i]).flags & ML_TWOSIDED)) {
                        continue;
                    }

                    tsec = (sec.lines[i]).frontsector;
                    newsecnum = tsec.id;

                    if (secnum != newsecnum) {
                        continue;
                    }

                    tsec = (sec.lines[i]).backsector;
                    newsecnum = tsec.id;

                    if (tsec.floorpic != texture) {
                        continue;
                    }

                    height += stairsize;

                    if (tsec.specialdata != null) {
                        continue;
                    }

                    sec = tsec;
                    secnum = newsecnum;
                    floor = new floormove_t();
                    sec.specialdata = floor;
                    floor.thinkerFunction = ActiveStates.T_MoveFloor;
                    AddThinker(floor);
                    floor.direction = 1;
                    floor.sector = sec;
                    floor.speed = speed;
                    floor.floordestheight = height;
                    ok = true;
                    break;
                }
            } while (ok);
        }
        return rtn;
    }

    /**
     * Move a plat up and down
     */
    default void PlatRaise(plat_t plat) {
        result_e res;

        switch (plat.status) {
            case up:
                res = MovePlane(plat.sector, plat.speed, plat.high, plat.crush, 0, 1);

                if (plat.type == plattype_e.raiseAndChange
                    || plat.type == plattype_e.raiseToNearestAndChange) {
                    if (!eval(LevelTime() & 7)) {
                        StartSound(plat.sector.soundorg, sounds.sfxenum_t.sfx_stnmov);
                    }
                }

                if (res == result_e.crushed && (!plat.crush)) {
                    plat.count = plat.wait;
                    plat.status = plat_e.down;
                    StartSound(plat.sector.soundorg, sounds.sfxenum_t.sfx_pstart);
                } else {
                    if (res == result_e.pastdest) {
                        plat.count = plat.wait;
                        plat.status = plat_e.waiting;
                        StartSound(plat.sector.soundorg, sounds.sfxenum_t.sfx_pstop);

                        switch (plat.type) {
                            case blazeDWUS:
                            case downWaitUpStay:
                                RemoveActivePlat(plat);
                                break;

                            case raiseAndChange:
                            case raiseToNearestAndChange:
                                RemoveActivePlat(plat);
                                break;

                            default:
                                break;
                        }
                    }
                }
                break;

            case down:
                res = MovePlane(plat.sector, plat.speed, plat.low, false, 0, -1);

                if (res == result_e.pastdest) {
                    plat.count = plat.wait;
                    plat.status = plat_e.waiting;
                    StartSound(plat.sector.soundorg, sounds.sfxenum_t.sfx_pstop);
                }
                break;

            case waiting:
                if (--plat.count == 0) {
                    if (plat.sector.floorheight == plat.low) {
                        plat.status = plat_e.up;
                    } else {
                        plat.status = plat_e.down;
                    }
                    StartSound(plat.sector.soundorg, sounds.sfxenum_t.sfx_pstart);
                }
            case in_stasis:
                break;
        }
    }
}
