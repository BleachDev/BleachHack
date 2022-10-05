package org.bleachhack.util.doom.p.Actions;

import org.bleachhack.util.doom.doom.thinker_t;
import java.util.logging.Level;
import org.bleachhack.util.doom.mochadoom.Loggers;
import org.bleachhack.util.doom.p.AbstractLevelLoader;
import static org.bleachhack.util.doom.p.ActiveStates.T_SlidingDoor;
import org.bleachhack.util.doom.p.mobj_t;
import org.bleachhack.util.doom.p.sd_e;
import org.bleachhack.util.doom.p.sdt_e;
import org.bleachhack.util.doom.p.slidedoor_t;
import org.bleachhack.util.doom.p.slideframe_t;
import org.bleachhack.util.doom.p.slidename_t;
import org.bleachhack.util.doom.rr.TextureManager;
import org.bleachhack.util.doom.rr.line_t;
import static org.bleachhack.util.doom.rr.line_t.ML_BLOCKING;
import org.bleachhack.util.doom.rr.sector_t;
import static org.bleachhack.util.doom.utils.GenericCopy.malloc;
import org.bleachhack.util.doom.utils.TraitFactory.ContextKey;

public interface ActionsSlideDoors extends ActionTrait {

    ContextKey<SlideDoors> KEY_SLIDEDOORS = ACTION_KEY_CHAIN.newKey(ActionsSlideDoors.class, SlideDoors::new);

    void RemoveThinker(thinker_t t);

    // UNUSED
    // Separate into p_slidoor.c?
    // ABANDONED TO THE MISTS OF TIME!!!
    //
    // EV_SlidingDoor : slide a door horizontally
    // (animate midtexture, then set noblocking line)
    //
    int MAXSLIDEDOORS = 5;
    // how many frames of animation
    int SNUMFRAMES = 4;

    int SDOORWAIT = 35 * 3;
    int SWAITTICS = 4;

    slidename_t[] slideFrameNames = {
        new slidename_t(
        "GDOORF1", "GDOORF2", "GDOORF3", "GDOORF4", // front
        "GDOORB1", "GDOORB2", "GDOORB3", "GDOORB4" // back
        ),
        new slidename_t(), new slidename_t(), new slidename_t(), new slidename_t()
    };

    final class SlideDoors {
        slideframe_t[] slideFrames = malloc(slideframe_t::new, slideframe_t[]::new, MAXSLIDEDOORS);
    }

    default void SlidingDoor(slidedoor_t door) {
        final AbstractLevelLoader ll = levelLoader();
        final SlideDoors sd = contextRequire(KEY_SLIDEDOORS);
        switch (door.status) {
            case sd_opening:
                if (door.timer-- == 0) {
                    if (++door.frame == ActionsSlideDoors.SNUMFRAMES) {
                        // IF DOOR IS DONE OPENING...
                        ll.sides[door.line.sidenum[0]].midtexture = 0;
                        ll.sides[door.line.sidenum[1]].midtexture = 0;
                        door.line.flags &= ML_BLOCKING ^ 0xff;

                        if (door.type == sdt_e.sdt_openOnly) {
                            door.frontsector.specialdata = null;
                            RemoveThinker(door);
                            break;
                        }

                        door.timer = ActionsSlideDoors.SDOORWAIT;
                        door.status = sd_e.sd_waiting;
                    } else {
                        // IF DOOR NEEDS TO ANIMATE TO NEXT FRAME...
                        door.timer = ActionsSlideDoors.SWAITTICS;

                        ll.sides[door.line.sidenum[0]].midtexture = (short) sd.slideFrames[door.whichDoorIndex].frontFrames[door.frame];
                        ll.sides[door.line.sidenum[1]].midtexture = (short) sd.slideFrames[door.whichDoorIndex].backFrames[door.frame];
                    }
                }
                break;

            case sd_waiting:
                // IF DOOR IS DONE WAITING...
                if (door.timer-- == 0) {
                    // CAN DOOR CLOSE?
                    if (door.frontsector.thinglist != null
                        || door.backsector.thinglist != null) {
                        door.timer = ActionsSlideDoors.SDOORWAIT;
                        break;
                    }

                    // door.frame = SNUMFRAMES-1;
                    door.status = sd_e.sd_closing;
                    door.timer = ActionsSlideDoors.SWAITTICS;
                }
                break;

            case sd_closing:
                if (door.timer-- == 0) {
                    if (--door.frame < 0) {
                        // IF DOOR IS DONE CLOSING...
                        door.line.flags |= ML_BLOCKING;
                        door.frontsector.specialdata = null;
                        RemoveThinker(door);
                        break;
                    } else {
                        // IF DOOR NEEDS TO ANIMATE TO NEXT FRAME...
                        door.timer = ActionsSlideDoors.SWAITTICS;

                        ll.sides[door.line.sidenum[0]].midtexture = (short) sd.slideFrames[door.whichDoorIndex].frontFrames[door.frame];
                        ll.sides[door.line.sidenum[1]].midtexture = (short) sd.slideFrames[door.whichDoorIndex].backFrames[door.frame];
                    }
                }
                break;
        }
    }

    default void P_InitSlidingDoorFrames() {
        final TextureManager<?> tm = DOOM().textureManager;
        final SlideDoors sd = contextRequire(KEY_SLIDEDOORS);

        int i;
        int f1;
        int f2;
        int f3;
        int f4;

        // DOOM II ONLY...
        if (!DOOM().isCommercial()) {
            return;
        }

        for (i = 0; i < MAXSLIDEDOORS; i++) {
            if (slideFrameNames[i].frontFrame1 == null) {
                break;
            }

            f1 = tm.TextureNumForName(slideFrameNames[i].frontFrame1);
            f2 = tm.TextureNumForName(slideFrameNames[i].frontFrame2);
            f3 = tm.TextureNumForName(slideFrameNames[i].frontFrame3);
            f4 = tm.TextureNumForName(slideFrameNames[i].frontFrame4);

            sd.slideFrames[i].frontFrames[0] = f1;
            sd.slideFrames[i].frontFrames[1] = f2;
            sd.slideFrames[i].frontFrames[2] = f3;
            sd.slideFrames[i].frontFrames[3] = f4;

            f1 = tm.TextureNumForName(slideFrameNames[i].backFrame1);
            f2 = tm.TextureNumForName(slideFrameNames[i].backFrame2);
            f3 = tm.TextureNumForName(slideFrameNames[i].backFrame3);
            f4 = tm.TextureNumForName(slideFrameNames[i].backFrame4);

            sd.slideFrames[i].backFrames[0] = f1;
            sd.slideFrames[i].backFrames[1] = f2;
            sd.slideFrames[i].backFrames[2] = f3;
            sd.slideFrames[i].backFrames[3] = f4;
        }
    }

    //
    // Return index into "slideFrames" array
    // for which door type to use
    //
    default int P_FindSlidingDoorType(line_t line) {
        final AbstractLevelLoader ll = levelLoader();
        final SlideDoors sd = contextRequire(KEY_SLIDEDOORS);

        for (int i = 0; i < MAXSLIDEDOORS; i++) {
            int val = ll.sides[line.sidenum[0]].midtexture;
            if (val == sd.slideFrames[i].frontFrames[0]) {
                return i;
            }
        }

        return -1;
    }

    default void EV_SlidingDoor(line_t line, mobj_t thing) {
        sector_t sec;
        slidedoor_t door;

        // DOOM II ONLY...
        if (!DOOM().isCommercial()) {
            return;
        }

        Loggers.getLogger(ActionsSlideDoors.class.getName()).log(Level.WARNING, "EV_SlidingDoor");

        // Make sure door isn't already being animated
        sec = line.frontsector;
        door = null;
        if (sec.specialdata != null) {
            if (thing.player == null) {
                return;
            }

            door = (slidedoor_t) sec.specialdata;
            if (door.type == sdt_e.sdt_openAndClose) {
                if (door.status == sd_e.sd_waiting) {
                    door.status = sd_e.sd_closing;
                }
            } else {
                return;
            }
        }

        // Init sliding door vars
        if (door == null) {
            door = new slidedoor_t();
            AddThinker(door);
            sec.specialdata = door;

            door.type = sdt_e.sdt_openAndClose;
            door.status = sd_e.sd_opening;
            door.whichDoorIndex = P_FindSlidingDoorType(line);

            if (door.whichDoorIndex < 0) {
                doomSystem().Error("EV_SlidingDoor: Can't use texture for sliding door!");
            }

            door.frontsector = sec;
            door.backsector = line.backsector;
            door.thinkerFunction = T_SlidingDoor;
            door.timer = SWAITTICS;
            door.frame = 0;
            door.line = line;
        }
    }
}
