package org.bleachhack.util.doom.rr.drawfuns;

/** This is all the information needed to draw a particular column. Really.
 *  So if we store all of this crap somewhere instead of drawing, we can do the
 *  drawing when it's more convenient, and since they are non-overlapping we can 
 *  parallelize them. Any questions?
 *  
 */

public class ColVars<T,V> {    
    /** when passing dc_source around, also set this */
    public T dc_source;
    public int dc_source_ofs;
    
    public T dc_translation;
    public int viewheight;
    
    /** Used by functions that accept transparency or other special 
     *  remapping tables.
     * 
     */
    public T tranmap;
    
    public int centery;
    public int dc_iscale;

    public int dc_texturemid;
    public int dc_texheight; // Boom enhancement
    public int dc_x;
    public int dc_yh;
    public int dc_yl;
    
    public int dc_flags;

    /**
     * MAES: this was a typedef for unsigned bytes, called "lighttable_t". It
     * makes more sense to make it generic and parametrize it to an array of 
     * primitives since it's performance-critical in the renderer. 
     * Now, whether this should be made bytes or shorts or chars or even ints 
     * is debatable.
     */
    
    public V dc_colormap;

    /** Copies all BUT flags */
    
    public final void copyFrom(ColVars<T,V> dcvars) {
        this.dc_source=dcvars.dc_source;
        this.dc_colormap=dcvars.dc_colormap;
        this.dc_source_ofs=dcvars.dc_source_ofs;
        this.viewheight=dcvars.viewheight;
        this.centery=dcvars.centery;
        this.dc_x=dcvars.dc_x;
        this.dc_yh=dcvars.dc_yh;
        this.dc_yl=dcvars.dc_yl;
        this.dc_texturemid=dcvars.dc_texturemid;
        this.dc_iscale=dcvars.dc_iscale;
        this.dc_texheight=dcvars.dc_texheight;        
    }
    
    /** Assigns specific flags */
    public void copyFrom(ColVars<T,V> dcvars,int flags) {
        this.copyFrom(dcvars);
        this.dc_flags=flags;
    }
    

}
