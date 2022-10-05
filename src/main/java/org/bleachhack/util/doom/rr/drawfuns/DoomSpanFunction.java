package org.bleachhack.util.doom.rr.drawfuns;

import org.bleachhack.util.doom.i.IDoomSystem;

public abstract class DoomSpanFunction<T,V> implements SpanFunction<T,V> {
    
    protected final boolean RANGECHECK=false;
    protected final int SCREENWIDTH;
    protected final int SCREENHEIGHT;
    protected SpanVars<T,V> dsvars;
    protected final int[] ylookup;
    protected final int[] columnofs;
    protected final V screen;
    protected final IDoomSystem I;
    
    public DoomSpanFunction(int sCREENWIDTH, int sCREENHEIGHT,
            int[] ylookup, int[] columnofs,SpanVars<T,V> dsvars, V screen,IDoomSystem I) {
        SCREENWIDTH = sCREENWIDTH;
        SCREENHEIGHT = sCREENHEIGHT;
        this.ylookup=ylookup;
        this.columnofs=columnofs;
        this.dsvars = dsvars;
        this.screen = screen;
        this.I=I;
    }
    
    protected final void doRangeCheck(){
        if (dsvars.ds_x2 < dsvars.ds_x1 || dsvars.ds_x1 < 0 || dsvars.ds_x2 >= SCREENWIDTH
                || dsvars.ds_y > SCREENHEIGHT) {
            I.Error("R_DrawSpan: %d to %d at %d", dsvars.ds_x1, dsvars.ds_x2, dsvars.ds_y);
        }
    }

    @Override
    public final void invoke(SpanVars<T,V> dsvars) {
        this.dsvars=dsvars;
        invoke();
    }
    
}
