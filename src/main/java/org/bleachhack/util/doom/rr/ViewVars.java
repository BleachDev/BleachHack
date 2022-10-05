package org.bleachhack.util.doom.rr;

import static org.bleachhack.util.doom.data.Tables.ANG180;
import static org.bleachhack.util.doom.data.Tables.ANG270;
import static org.bleachhack.util.doom.data.Tables.ANG90;
import static org.bleachhack.util.doom.data.Tables.SlopeDiv;
import static org.bleachhack.util.doom.data.Tables.tantoangle;
import org.bleachhack.util.doom.doom.player_t;
import org.bleachhack.util.doom.utils.C2JUtils;
import org.bleachhack.util.doom.v.scale.VideoScale;

public class ViewVars {

    public ViewVars(VideoScale vs) {
         negonearray = new short[vs.getScreenWidth()]; // MAES: in scaling
         screenheightarray = new short[vs.getScreenWidth()];// MAES: in scaling
         xtoviewangle = new long[vs.getScreenWidth() + 1];
         C2JUtils.memset(negonearray, (short)-1, negonearray.length);
    }
    
    
    
    // Found in draw_c. Only ever used in renderer.
    public int windowx;
    public int windowy;
    public int width;
    public int height;
    
    // MAES: outsiders have no business peeking into this.
    // Or...well..maybe they do. It's only used to center the "pause" X
    // position.
    // TODO: get rid of this?
    public int scaledwidth;
    public int centerx;
    public int centery;
    
    /** Used to determine the view center and projection in view units fixed_t */
    public int centerxfrac, centeryfrac, projection;

    /** fixed_t */
    public int x, y, z;

    // MAES: an exception to strict type safety. These are used only in here,
    // anyway (?) and have no special functions.
    // Plus I must use them as indexes. angle_t
    public long angle;

    /** fixed */
    public int cos, sin;

    public player_t player;

    /** Heretic/freeview stuff? */

    public int lookdir;
    
    // 0 = high, 1 = low. Normally only the menu and the interface can change
    // that.
    public int detailshift;
    
    public int WEAPONADJUST;
    public int BOBADJUST;
	
	/**
	 * constant arrays used for psprite clipping and initializing clipping
	 */
    public final short[] negonearray; // MAES: in scaling
    public short[] screenheightarray;// MAES: in scaling
    
    /** Mirrors the one in renderer... */
    public long[] xtoviewangle;
	
    public final long PointToAngle(int x, int y) {
        // MAES: note how we don't use &BITS32 here. That is because
        // we know that the maximum possible value of tantoangle is angle
        // This way, we are actually working with vectors emanating
        // from our current position.
        x -= this.x;
        y -= this.y;

        if ((x == 0) && (y == 0))
            return 0;

        if (x >= 0) {
            // x >=0
            if (y >= 0) {
                // y>= 0

                if (x > y) {
                    // octant 0
                    return tantoangle[SlopeDiv(y, x)];
                } else {
                    // octant 1
                    return (ANG90 - 1 - tantoangle[SlopeDiv(x, y)]);
                }
            } else {
                // y<0
                y = -y;

                if (x > y) {
                    // octant 8
                    return (-tantoangle[SlopeDiv(y, x)]);
                } else {
                    // octant 7
                    return (ANG270 + tantoangle[SlopeDiv(x, y)]);
                }
            }
        } else {
            // x<0
            x = -x;

            if (y >= 0) {
                // y>= 0
                if (x > y) {
                    // octant 3
                    return (ANG180 - 1 - tantoangle[SlopeDiv(y, x)]);
                } else {
                    // octant 2
                    return (ANG90 + tantoangle[SlopeDiv(x, y)]);
                }
            } else {
                // y<0
                y = -y;

                if (x > y) {
                    // octant 4
                    return (ANG180 + tantoangle[SlopeDiv(y, x)]);
                } else {
                    // octant 5
                    return (ANG270 - 1 - tantoangle[SlopeDiv(x, y)]);
                }
            }
        }
        // This is actually unreachable.
        // return 0;
    }
    
    public final int getViewWindowX(){
        return windowx;
    }

    public final int getViewWindowY(){
        return windowy;
    }
        
    public final int getScaledViewWidth(){
        return scaledwidth;
    }

    public final int getScaledViewHeight() {
        return height;
    }

}
