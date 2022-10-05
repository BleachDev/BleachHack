package org.bleachhack.util.doom.rr.drawfuns;

public class SpanVars<T,V> {

    public int ds_xfrac;
    public int ds_yfrac;
    public int ds_xstep;
    public T ds_source;
    
    /** DrawSpan colormap. */
    public V ds_colormap;
    public int ds_y;
    public int ds_x2;
    public int ds_x1;
    public int ds_ystep;
    
    public DoomSpanFunction<T,V>  spanfunc;
}
