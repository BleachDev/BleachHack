package org.bleachhack.util.doom.rr;

/**
 * A sprite definition:
 * a number of animation frames.
 */

public class spritedef_t {

    /** the very least, primitive fields won't bomb,
     *  and copy constructors can do their job.
     */
    public spritedef_t(){        
    }
    
    public spritedef_t(int numframes){
        this.numframes=numframes;
        this.spriteframes=new spriteframe_t[numframes];        
    }
    
    public spritedef_t(spriteframe_t[] frames){
        this.numframes=frames.length;
        this.spriteframes=new spriteframe_t[numframes];
        // copy shit over...
        for (int i=0;i<numframes;i++){
            spriteframes[i]=frames[i].clone();
        }
    }
    
    /** Use this constructor, as we usually need less than 30 frames 
     *  It will actually clone the frames.
     */
    
    public void copy(spriteframe_t[] from, int maxframes){
        this.numframes=maxframes;
        this.spriteframes=new spriteframe_t[maxframes];
        // copy shit over...
        for (int i=0;i<maxframes;i++){
            spriteframes[i]=from[i].clone();
        }
    }
    
    
 public int         numframes;
 public spriteframe_t[]  spriteframes;

};