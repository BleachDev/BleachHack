package org.bleachhack.util.doom.rr;

//
// ?
//

public class drawseg_t {
	
	public drawseg_t(){
		
	}

     /** MAES: was pointer. Not array? */
     public  seg_t      curline;
     public int         x1, x2;

     /** fixed_t */
     public int     scale1,  scale2,  scalestep;

     /** 0=none, 1=bottom, 2=top, 3=both */
     public  int         silhouette;

     /** do not clip sprites above this (fixed_t) */
     public int     bsilheight;

     /** do not clip sprites below this (fixed_t) */
     public int     tsilheight;
     
     /** Indexes to lists for sprite clipping, 
        all three adjusted so [x1] is first value. */
     private int      psprtopclip, psprbottomclip, pmaskedtexturecol;

     /** Pointers to the actual lists  */

     private short[]      sprtopclip, sprbottomclip, maskedtexturecol;
     
     ///////////////// Accessor methods to simulate mid-array pointers ///////////
     
     public void setSprTopClip(short[] array, int index){
         this.sprtopclip=array;
         this.psprtopclip=index;         
     }
     
     public void setSprBottomClip(short[] array, int index){
         this.sprbottomclip=array;
         this.psprbottomclip=index;         
     }
     
     public void setMaskedTextureCol(short[] array, int index){
         this.maskedtexturecol=array;
         this.pmaskedtexturecol=index;         
     }
     
     public short getSprTopClip(int index){
         return this.sprtopclip[this.psprtopclip+index];
     }
     
     public short getSprBottomClip( int index){
         return this.sprbottomclip[this.psprbottomclip+index];     
         }
     
     public short getMaskedTextureCol(int index){
         return this.maskedtexturecol[this.pmaskedtexturecol+index];         
     }
     
     public short[] getSprTopClipList(){
         return this.sprtopclip;
     }
     
     public short[] getSprBottomClipList(){
         return this.sprbottomclip;
     }
     
     public short[] getMaskedTextureColList(){
         return this.maskedtexturecol;
     }
     
     public int getSprTopClipPointer(){
         return this.psprtopclip;
     }
     
     public int getSprBottomClipPointer(){
         return this.psprbottomclip;
     }
     
     public int getMaskedTextureColPointer(){
         return this.pmaskedtexturecol;
     }
     
     public void setSprTopClipPointer(int index){
         this.psprtopclip=index;
     }
     
     public void setSprBottomClipPointer(int index){
         this.psprbottomclip=index;
     }
     
     public void setMaskedTextureColPointer(int index){
         this.pmaskedtexturecol=index;
     }     
     
     public boolean nullSprTopClip(){
         return this.sprtopclip==null;
     }
     
     public boolean nullSprBottomClip(){
         return this.sprbottomclip==null;
     }
     
     public boolean nullMaskedTextureCol(){
         return this.maskedtexturecol==null;
     }
     
}
