package org.bleachhack.util.doom.rr;

import org.bleachhack.util.doom.data.Limits;
import org.bleachhack.util.doom.data.Tables;
import static org.bleachhack.util.doom.data.Tables.ANG90;
import static org.bleachhack.util.doom.data.Tables.finecosine;
import static org.bleachhack.util.doom.data.Tables.finesine;
import java.util.Arrays;
import static org.bleachhack.util.doom.m.fixed_t.FixedDiv;
import org.bleachhack.util.doom.utils.C2JUtils;
import org.bleachhack.util.doom.v.scale.VideoScale;

/** Actual visplane data and methods are isolate here.
 *  This allows more encapsulation and some neat hacks like sharing 
 *  visplane data among parallel renderers, without duplicating them.
 */

public class Visplanes {

    private static final boolean DEBUG2=false;

    protected final ViewVars view;
    protected final TextureManager<?> TexMan;
    protected final VideoScale vs;
    
    public Visplanes(VideoScale vs, ViewVars view, TextureManager<?> TexMan){
        this.vs = vs;
        this.view=view;
        this.TexMan=TexMan;
        MAXOPENINGS = vs.getScreenWidth() * 64;
        openings = new short[MAXOPENINGS];
        BLANKCACHEDHEIGHT = new int[vs.getScreenHeight()];
        yslope = new int[vs.getScreenHeight()];
    }
    

    // HACK: An all zeroes array used for fast clearing of certain visplanes.
    public int[] cachedheight,BLANKCACHEDHEIGHT;

    
    /** To treat as fixed_t */
    public int basexscale, baseyscale;
    
    /** To treat at fixed_t */
    protected int[] yslope;
    
    // initially.
    public int MAXVISPLANES = Limits.MAXVISPLANES;
    public int MAXOPENINGS;
    
    /** visplane_t*, treat as indexes into visplanes */
    public int lastvisplane, floorplane, ceilingplane;
    public visplane_t[] visplanes = new visplane_t[MAXVISPLANES];

    /**
     * openings is supposed to show where "openings" in visplanes start and end
     * e.g. due to sprites, windows etc.
     */
    public short[] openings;
    /** Maes: this is supposed to be a pointer inside openings */
    public int lastopening;
    
    protected int skyscale;

    
    /**
     * Call only after visplanes have been properly resized for resolution.
     * In case of dynamic resolution changes, the old ones should just be
     * discarded, as they would be nonsensical.
     */

    public void initVisplanes() {
        cachedheight = new int[vs.getScreenHeight()];
        Arrays.setAll(visplanes, j -> new visplane_t());
    }
    
    public int getBaseXScale(){
        return basexscale;
    }

    public int getBaseYScale(){
        return baseyscale;
    }

    public int getSkyScale(){
        return skyscale;
    }
    
    public void setSkyScale(int i) {
        skyscale=i;        
    }
    
    public int getLength(){
        return visplanes.length;
    }
    
    /** Return the last of visplanes, allocating a new one if needed */
    
    public visplane_t allocate(){
        if (lastvisplane == visplanes.length) {
            //  visplane overflows could occur at this point.
            resizeVisplanes();
        }
        
        return visplanes[lastvisplane++];
    }
    
    public final void resizeVisplanes() {
        // Bye bye, old visplanes.
        visplanes = C2JUtils.resize(visplanes[0], visplanes, visplanes.length*2);
    }
    
    /**
     * R_FindPlane
     * 
     * Checks whether a visplane with the specified height, picnum and light
     * level exists among those already created. This looks like a half-assed
     * attempt at reusing already existing visplanes, rather than creating new
     * ones. The tricky part is understanding what happens if one DOESN'T exist.
     * Called only from within R_Subsector (so while we're still trasversing
     * stuff).
     * 
     * @param height
     *            (fixed_t)
     * @param picnum
     * @param lightlevel
     * @return was visplane_t*, returns index into visplanes[]
     */

    public final int FindPlane(int height, int picnum, int lightlevel) {
        // System.out.println("\tChecking for visplane merging...");
        int check = 0; // visplane_t*
        visplane_t chk = null;

        if (picnum == TexMan.getSkyFlatNum()) {
            height = 0; // all skys map together
            lightlevel = 0;
        }

        chk = visplanes[0];

        // Find visplane with the desired attributes
        for (check = 0; check < lastvisplane; check++) {

            chk = visplanes[check];
            if (height == chk.height && picnum == chk.picnum
                    && lightlevel == chk.lightlevel) {
                // Found a visplane with the desired specs.
                break;
            }
        }

        if (check < lastvisplane) {
            return check;
        }

        // This should return the next available visplane and resize if needed,
        // no need to hack with lastvisplane++
        chk = allocate();
        // Add a visplane
        chk.height = height;
        chk.picnum = picnum;
        chk.lightlevel = lightlevel;
        chk.minx = vs.getScreenWidth();
        chk.maxx = -1;
        // memset (chk.top,0xff,sizeof(chk.top));
        chk.clearTop();

        return check;
    }
    
    /**
     * R_ClearPlanes At begining of frame.
     * 
     */

    public void ClearPlanes() {
        int angle;

        /*
         * View planes are cleared at the beginning of every plane, by
         * setting them "just outside" the borders of the screen (-1 and
         * viewheight).
         */

        // Point to #1 in visplane list? OK... ?!
        lastvisplane = 0;

        // We point back to the first opening of the list openings[0],
        // again.
        lastopening = 0;

        // texture calculation
        System.arraycopy(BLANKCACHEDHEIGHT, 0, cachedheight, 0,
                BLANKCACHEDHEIGHT.length);

        // left to right mapping
        // FIXME: If viewangle is ever < ANG90, you're fucked. How can this
        // be prevented?
        // Answer: 32-bit unsigned are supposed to roll over. You can & with
        // 0xFFFFFFFFL.
        angle = (int) Tables.toBAMIndex(view.angle - ANG90);

        // scale will be unit scale at vs.getScreenWidth()/2 distance
        basexscale = FixedDiv(finecosine[angle], view.centerxfrac);
        baseyscale = -FixedDiv(finesine[angle], view.centerxfrac);
    }
    
    /**
     * R_CheckPlane
     * 
     * Called from within StoreWallRange
     * 
     * Presumably decides if a visplane should be split or not?
     * 
     */

    public int CheckPlane(int index, int start, int stop) {

        if (DEBUG2)
            System.out.println("Checkplane " + index + " between " + start
                    + " and " + stop);

        // Interval ?
        int intrl;
        int intrh;

        // Union?
        int unionl;
        int unionh;
        // OK, so we check out ONE particular visplane.
        visplane_t pl = visplanes[index];

        if (DEBUG2)
            System.out.println("Checking out plane " + pl);

        int x;

        // If start is smaller than the plane's min...
        //
        // start minx maxx stop
        // | | | |
        // --------PPPPPPPPPPPPPP-----------
        //
        //
        if (start < pl.minx) {
            intrl = pl.minx;
            unionl = start;
            // Then we will have this:
            //
            // unionl intrl maxx stop
            // | | | |
            // --------PPPPPPPPPPPPPP-----------
            //

        } else {
            unionl = pl.minx;
            intrl = start;

            // else we will have this:
            //
            // union1 intrl maxx stop
            // | | | |
            // --------PPPPPPPPPPPPPP-----------
            //
            // unionl comes before intrl in any case.
            //
            //
        }

        // Same as before, for for stop and maxx.
        // This time, intrh comes before unionh.
        //

        if (stop > pl.maxx) {
            intrh = pl.maxx;
            unionh = stop;
        } else {
            unionh = pl.maxx;
            intrh = stop;
        }

        // An interval is now defined, which is entirely contained in the
        // visplane.
        //

        // If the value FF is NOT stored ANYWWHERE inside it, we bail out
        // early
        for (x = intrl; x <= intrh; x++)
            if (pl.getTop(x) != Character.MAX_VALUE)
                break;

        // This can only occur if the loop above completes,
        // else the visplane we were checking has non-visible/clipped
        // portions within that range: we must split.

        if (x > intrh) {
            // Merge the visplane
            pl.minx = unionl;
            pl.maxx = unionh;
            // System.out.println("Plane modified as follows "+pl);
            // use the same one
            return index;
        }

        // SPLIT: make a new visplane at "last" position, copying materials
        // and light.

        visplane_t last=allocate();
        last.height = pl.height;
        last.picnum = pl.picnum;
        last.lightlevel = pl.lightlevel;

        pl = last;
        pl.minx = start;
        pl.maxx = stop;

        // memset (pl.top,0xff,sizeof(pl.top));
        pl.clearTop();

        // return pl;

        // System.out.println("New plane created: "+pl);
        return lastvisplane - 1;
    }
    
    /*
     
       /**
     * A hashtable used to retrieve planes with particular attributes faster
     * -hopefully-. The planes are still stored in the visplane array for
     * convenience, but we can search them in the hashtable too -as a bonus, we
     * can reuse previously created planes that match newer ones-.
     */
    /*
    Hashtable<visplane_t, Integer> planehash = new Hashtable<visplane_t, Integer>(
            128);
    visplane_t check = new visplane_t();
    */

    /*
    protected final int FindPlane2(int height, int picnum, int lightlevel) {
        // System.out.println("\tChecking for visplane merging...");
        // int check=0; // visplane_t*
        visplane_t chk = null;
        Integer checknum;

        if (picnum == TexMan.getSkyFlatNum()) {
            height = 0; // all skys map together
            lightlevel = 0;
        }

        // Try and find this.
        check.lightlevel = lightlevel;
        check.picnum = picnum;
        check.height = height;
        check.updateHashCode();
        checknum = planehash.get(check);

        // Something found, get it.

        if (!(checknum == null)) {

            // Visplane exists and is within those allocated in the current tic.
            if (checknum < lastvisplane) {
                return checknum;
            }

            // Found a visplane, but we can't add anymore.
            // Resize right away. This shouldn't take too long.
            if (lastvisplane == MAXVISPLANES) {
                // I.Error ("R_FindPlane: no more visplanes");
                ResizeVisplanes();
            }
        }

        // We found a visplane (possibly one allocated on a previous tic)
        // but we can't link directly to it, we need to copy its data
        // around.

        checknum = new Integer(Math.max(0, lastvisplane));

        chk = visplanes[checknum];
        // Add a visplane
        lastvisplane++;
        chk.height = height;
        chk.picnum = picnum;
        chk.lightlevel = lightlevel;
        chk.minx = vs.getScreenWidth();
        chk.maxx = -1;
        chk.updateHashCode();
        planehash.put(chk, checknum);
        // memset (chk.top,0xff,sizeof(chk.top));
        chk.clearTop();

        return checknum;
    }
    */
    
}
