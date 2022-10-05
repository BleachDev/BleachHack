package org.bleachhack.util.doom.rr;


/** A maptexturedef_t describes a rectangular texture,
 *  which is composed of one or more mappatch_t structures
 *  that arrange graphic patches.
 *  
 *  This is the in-memory format, which is similar to maptexture_t (which is on-disk).
 *  
 * @author Maes
 *
 */

public class texture_t {
    /** Keep name for switch changing, etc. */
    public String    name;        
    public short   width;
    public short   height;
    
    // All the patches[patchcount]
    //  are drawn back to front into the cached texture.
    public short   patchcount;
    public texpatch_t[]  patches;     
    
    /** Unmarshalling at its best! */
    
    public void copyFromMapTexture(maptexture_t mt){
        this.name=new String(mt.name);
        this.width=mt.width;
        this.height=mt.height;
        this.patchcount=mt.patchcount;
        this.patches=new texpatch_t[patchcount];
        
        for (int i=0;i<patchcount;i++){
            patches[i]=new texpatch_t();
            patches[i].copyFromMapPatch(mt.patches[i]);
        }
    }
    
    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder();
        sb.append(name);
        sb.append(" Height ");
        sb.append(height);
        sb.append(" Width ");
        sb.append(width);
        sb.append(" Patchcount ");
        sb.append(patchcount);
        return sb.toString();
            
        }    
}