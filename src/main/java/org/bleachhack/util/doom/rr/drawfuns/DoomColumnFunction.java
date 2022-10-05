package org.bleachhack.util.doom.rr.drawfuns;

import org.bleachhack.util.doom.i.IDoomSystem;
import org.bleachhack.util.doom.v.tables.BlurryTable;

/** Prototype for 
 * 
 * @author velktron
 *
 * @param <T>
 */

public abstract class DoomColumnFunction<T,V> implements ColumnFunction<T,V>{
    
    protected final boolean RANGECHECK=false;
    protected final int SCREENWIDTH;
    protected final int SCREENHEIGHT;
    protected ColVars<T,V> dcvars;
    protected final V screen;
    protected final IDoomSystem I;
    protected final int[] ylookup;
    protected final int[] columnofs;
    protected BlurryTable blurryTable;
    protected int flags;
    
    public DoomColumnFunction(int sCREENWIDTH, int sCREENHEIGHT,int[] ylookup,
            int[] columnofs, ColVars<T,V> dcvars, V screen,IDoomSystem I) {
        SCREENWIDTH = sCREENWIDTH;
        SCREENHEIGHT = sCREENHEIGHT;
        this.ylookup=ylookup;
        this.columnofs=columnofs;
        this.dcvars = dcvars;
        this.screen = screen;
        this.I=I;
        this.blurryTable=null;
    }
    
    public DoomColumnFunction(int sCREENWIDTH, int sCREENHEIGHT,int[] ylookup,
            int[] columnofs, ColVars<T,V> dcvars, V screen,IDoomSystem I, BlurryTable BLURRY_MAP) {
        SCREENWIDTH = sCREENWIDTH;
        SCREENHEIGHT = sCREENHEIGHT;
        this.ylookup = ylookup;
        this.columnofs = columnofs;
        this.dcvars = dcvars;
        this.screen = screen;
        this.I = I;
        this.blurryTable = BLURRY_MAP;
    }

    protected final void performRangeCheck(){
        if (dcvars.dc_x >= SCREENWIDTH || dcvars.dc_yl < 0 || dcvars.dc_yh >= SCREENHEIGHT)
            I.Error("R_DrawColumn: %d to %d at %d", dcvars.dc_yl, dcvars.dc_yh, dcvars.dc_x);
    }
    
    /**
     * 
     * Use ylookup LUT to avoid multiply with ScreenWidth.
     * Use columnofs LUT for subwindows?
     * 
     * @return Framebuffer destination address.
     */
    
    protected final int computeScreenDest() {
        return ylookup[dcvars.dc_yl] + columnofs[dcvars.dc_x];
    }

    protected final int blockyDest1() {
        return ylookup[dcvars.dc_yl] + columnofs[dcvars.dc_x << 1];
    }

    protected final int blockyDest2() {
        return ylookup[dcvars.dc_yl] + columnofs[(dcvars.dc_x << 1) + 1];
    }

    @Override
    public final void invoke(ColVars<T,V> dcvars) {
        this.dcvars=dcvars;
        invoke();
    }
    
    @Override
    public final int getFlags(){
        return this.flags;
    }
    
}
