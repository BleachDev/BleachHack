package org.bleachhack.util.doom.rr.parallel;

import static org.bleachhack.util.doom.data.Defines.FF_FRAMEMASK;
import static org.bleachhack.util.doom.data.Defines.FF_FULLBRIGHT;
import static org.bleachhack.util.doom.data.Defines.pw_invisibility;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import static org.bleachhack.util.doom.m.fixed_t.*;
import static org.bleachhack.util.doom.p.mobj_t.MF_TRANSLATION;
import org.bleachhack.util.doom.p.pspdef_t;
import org.bleachhack.util.doom.rr.AbstractThings;
import org.bleachhack.util.doom.rr.IDetailAware;
import org.bleachhack.util.doom.rr.SceneRenderer;
import org.bleachhack.util.doom.rr.column_t;
import org.bleachhack.util.doom.rr.drawfuns.ColFuncs;
import org.bleachhack.util.doom.rr.drawfuns.ColVars;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawColumnBoom;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawColumnBoomLow;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawFuzzColumn;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawFuzzColumnLow;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawTranslatedColumn;
import org.bleachhack.util.doom.rr.drawfuns.R_DrawTranslatedColumnLow;
import org.bleachhack.util.doom.rr.drawseg_t;
import static org.bleachhack.util.doom.rr.line_t.*;
import org.bleachhack.util.doom.rr.patch_t;
import org.bleachhack.util.doom.rr.spritedef_t;
import org.bleachhack.util.doom.rr.spriteframe_t;
import org.bleachhack.util.doom.rr.vissprite_t;
import org.bleachhack.util.doom.v.graphics.Palettes;
import org.bleachhack.util.doom.v.scale.VideoScale;
import org.bleachhack.util.doom.v.tables.BlurryTable;

/** A "Masked Worker" draws sprites in a split-screen strategy. Used by 
 * ParallelRenderer2. Each Masked Worker is essentially a complete Things
 * drawer, and reuses much of the serial methods.
 * 
 * @author velktron
 *
 * @param <T>
 * @param <V>
 */

public abstract class MaskedWorker<T,V> extends AbstractThings<T,V> implements Runnable, IDetailAware{
    
    private final static boolean DEBUG=false;
    private final static boolean RANGECHECK=false;
	
    protected final CyclicBarrier barrier;
    protected final int id;
    protected final int numthreads;
    
    //protected ColVars<T,V> maskedcvars;
   
    public MaskedWorker(VideoScale vs, SceneRenderer<T, V> R, int id, int numthreads, CyclicBarrier barrier) {
	    super(vs, R);
	    // Workers have their own set, not a "pegged" one.
	    this.colfuncshi=new ColFuncs<>();
	    this.colfuncslow=new ColFuncs<>();
	    this.maskedcvars=new ColVars<>();
	    this.id=id;
        this.numthreads=numthreads;
        this.barrier=barrier;        
    }
	
    @Override
	public final void completeColumn(){
	    // Does nothing. Shuts up inheritance
	}
    
    public static final class HiColor extends MaskedWorker<byte[],short[]>{

		public HiColor(VideoScale vs, SceneRenderer<byte[],short[]> R,int id,
				int[] ylookup, int[] columnofs, int numthreads, short[] screen,
                CyclicBarrier barrier, BlurryTable BLURRY_MAP) {
			super(vs, R,id,numthreads, barrier);

	        // Non-optimized stuff for masked.
			colfuncshi.base=colfuncshi.main=colfuncshi.masked=new R_DrawColumnBoom.HiColor(vs.getScreenWidth(),vs.getScreenHeight(),ylookup,columnofs,maskedcvars,screen,I);
	        colfuncslow.masked=new R_DrawColumnBoomLow.HiColor(vs.getScreenWidth(),vs.getScreenHeight(),ylookup,columnofs,maskedcvars,screen,I);

	        // Fuzzy columns. These are also masked.
	        colfuncshi.fuzz=new R_DrawFuzzColumn.HiColor(vs.getScreenWidth(),vs.getScreenHeight(),ylookup,columnofs,maskedcvars,screen,I,BLURRY_MAP);
	        colfuncslow.fuzz=new R_DrawFuzzColumnLow.HiColor(vs.getScreenWidth(),vs.getScreenHeight(),ylookup,columnofs,maskedcvars,screen,I,BLURRY_MAP);

	        // Translated columns are usually sprites-only.
	        colfuncshi.trans=new R_DrawTranslatedColumn.HiColor(vs.getScreenWidth(),vs.getScreenHeight(),ylookup,columnofs,maskedcvars,screen,I);
	        colfuncslow.trans=new R_DrawTranslatedColumnLow.HiColor(vs.getScreenWidth(),vs.getScreenHeight(),ylookup,columnofs,maskedcvars,screen,I);
	        
	        colfuncs=colfuncshi;

		}
    	
    }
    
    public static final class Indexed extends MaskedWorker<byte[],byte[]>{

        public Indexed(VideoScale vs, SceneRenderer<byte[],byte[]> R,int id,
                int[] ylookup, int[] columnofs, int numthreads, byte[] screen,
                CyclicBarrier barrier, BlurryTable BLURRY_MAP) {
            super(vs, R,id,numthreads, barrier);
            colfuncshi.base=colfuncshi.main=colfuncshi.masked=new R_DrawColumnBoom.Indexed(vs.getScreenWidth(),vs.getScreenHeight(),ylookup,columnofs,maskedcvars,screen,I);
            colfuncslow.masked=new R_DrawColumnBoomLow.Indexed(vs.getScreenWidth(),vs.getScreenHeight(),ylookup,columnofs,maskedcvars,screen,I);

            // Fuzzy columns. These are also masked.
            colfuncshi.fuzz=new R_DrawFuzzColumn.Indexed(vs.getScreenWidth(),vs.getScreenHeight(),ylookup,columnofs,maskedcvars,screen,I,BLURRY_MAP);
            colfuncslow.fuzz=new R_DrawFuzzColumnLow.Indexed(vs.getScreenWidth(),vs.getScreenHeight(),ylookup,columnofs,maskedcvars,screen,I,BLURRY_MAP);

            // Translated columns are usually sprites-only.
            colfuncshi.trans=new R_DrawTranslatedColumn.Indexed(vs.getScreenWidth(),vs.getScreenHeight(),ylookup,columnofs,maskedcvars,screen,I);
            colfuncslow.trans=new R_DrawTranslatedColumnLow.Indexed(vs.getScreenWidth(),vs.getScreenHeight(),ylookup,columnofs,maskedcvars,screen,I);
            
            colfuncs=colfuncshi;
        }
        
    }
    
    public static final class TrueColor extends MaskedWorker<byte[],int[]>{

        public TrueColor(VideoScale vs, SceneRenderer<byte[],int[]> R,int id,
                int[] ylookup, int[] columnofs, int numthreads, int[] screen,
                CyclicBarrier barrier, BlurryTable BLURRY_MAP) {
            super(vs, R,id,numthreads, barrier);

            // Non-optimized stuff for masked.
            colfuncshi.base=colfuncshi.main=colfuncshi.masked=new R_DrawColumnBoom.TrueColor(vs.getScreenWidth(),vs.getScreenHeight(),ylookup,columnofs,maskedcvars,screen,I);
            colfuncslow.masked=new R_DrawColumnBoomLow.TrueColor(vs.getScreenWidth(),vs.getScreenHeight(),ylookup,columnofs,maskedcvars,screen,I);

            // Fuzzy columns. These are also masked.
            colfuncshi.fuzz=new R_DrawFuzzColumn.TrueColor(vs.getScreenWidth(),vs.getScreenHeight(),ylookup,columnofs,maskedcvars,screen,I,BLURRY_MAP);
            colfuncslow.fuzz=new R_DrawFuzzColumnLow.TrueColor(vs.getScreenWidth(),vs.getScreenHeight(),ylookup,columnofs,maskedcvars,screen,I,BLURRY_MAP);

            // Translated columns are usually sprites-only.
            colfuncshi.trans=new R_DrawTranslatedColumn.TrueColor(vs.getScreenWidth(),vs.getScreenHeight(),ylookup,columnofs,maskedcvars,screen,I);
            colfuncslow.trans=new R_DrawTranslatedColumnLow.TrueColor(vs.getScreenWidth(),vs.getScreenHeight(),ylookup,columnofs,maskedcvars,screen,I);
            
            colfuncs=colfuncshi;

        }
        
    }
    
    protected int startx, endx;
    
    /**
     * R_DrawVisSprite mfloorclip and mceilingclip should also be set.
     * 
     * Sprites are actually drawn here. Obviously overrides the serial
     * method, and only draws a portion of the sprite.
     * 
     * 
     */
    @Override
    protected final void DrawVisSprite(vissprite_t<V> vis) {
        column_t column;
        int texturecolumn;
        int frac; // fixed_t
        patch_t patch;
        // The sprite may have been partially drawn on another portion of the
        // screen.
        int bias=startx-vis.x1;
            if (bias<0) bias=0; // nope, it ain't.

        // Trim bounds to zone NOW
        int x1=Math.max(startx, vis.x1);
        int x2=Math.min(endx,vis.x2);
            
        // At this point, the view angle (and patch) has already been
        // chosen. Go back.
        patch = W.CachePatchNum(vis.patch + SM.getFirstSpriteLump());
        
        maskedcvars.dc_colormap = vis.colormap;
        // colfunc=glasscolfunc;
        if (maskedcvars.dc_colormap == null) {
            // NULL colormap = shadow draw
            colfunc = colfuncs.fuzz;
        } else if ((vis.mobjflags & MF_TRANSLATION) != 0) {
            colfunc = colfuncs.trans;
            @SuppressWarnings("unchecked")
            final T translation = (T) colormaps.getTranslationTable(vis.mobjflags);
            maskedcvars.dc_translation = translation;
        }

        maskedcvars.dc_iscale = Math.abs(vis.xiscale) >> view.detailshift;
        maskedcvars.dc_texturemid = vis.texturemid;
        // Add bias to compensate for partially drawn sprite which has not been rejected.
        frac = vis.startfrac+vis.xiscale*bias;
        spryscale = vis.scale;
        sprtopscreen = view.centeryfrac - FixedMul(maskedcvars.dc_texturemid, spryscale);

        // A texture height of 0 means "not tiling" and holds for
        // all sprite/masked renders.
        maskedcvars.dc_texheight=0;
        
        for (maskedcvars.dc_x = x1; maskedcvars.dc_x <= x2; maskedcvars.dc_x++, frac += vis.xiscale) {
            texturecolumn = frac >> FRACBITS;
            if (true) {
                if (texturecolumn < 0 || texturecolumn >= patch.width) {
                    I.Error("R_DrawSpriteRange: bad texturecolumn %d vs %d %d %d", texturecolumn, patch.width, x1, x2);
                }
            }
            column = patch.columns[texturecolumn];
            
            if (column == null) {
                System.err.printf("Null column for texturecolumn %d\n", texturecolumn, x1, x2);
            } else {
                DrawMaskedColumn(column);
            }
        }

        colfunc = colfuncs.masked;
    }

    /**
     * R_RenderMaskedSegRange
     * 
     * @param ds
     * @param x1
     * @param x2
     */
    
    @Override
    protected final void RenderMaskedSegRange(drawseg_t ds, int x1, int x2) {
    	
    	// Trivial rejection
        if (ds.x1>endx || ds.x2<startx) return;
        
        // Trim bounds to zone NOW
        x1=Math.max(startx, x1);
        x2=Math.min(endx,x2);
    	
        int index;

        int lightnum;
        int texnum;
        int bias=startx-ds.x1; // Correct for starting outside
        if (bias < 0) {
            bias = 0; // nope, it ain't.
        }        
        // System.out.printf("RenderMaskedSegRange from %d to %d\n",x1,x2);

        // Calculate light table.
        // Use different light tables
        // for horizontal / vertical / diagonal. Diagonal?
        // OPTIMIZE: get rid of LIGHTSEGSHIFT globally
        MyBSP.curline = ds.curline;
        frontsector = MyBSP.curline.frontsector;
        backsector = MyBSP.curline.backsector;
        texnum = TexMan.getTextureTranslation(MyBSP.curline.sidedef.midtexture);
        // System.out.print(" for texture "+textures[texnum].name+"\n:");
        lightnum = (frontsector.lightlevel >> colormaps.lightSegShift()) + colormaps.extralight;

        if (MyBSP.curline.v1y == MyBSP.curline.v2y)
            lightnum--;
        else if (MyBSP.curline.v1x == MyBSP.curline.v2x)
            lightnum++;

        // Killough code.
        colormaps.walllights = lightnum >= colormaps.lightLevels() ? colormaps.scalelight[colormaps.lightLevels() - 1]
                : lightnum < 0 ? colormaps.scalelight[0] : colormaps.scalelight[lightnum];

        // Get the list
        maskedtexturecol = ds.getMaskedTextureColList();
        // And this is the pointer.
        pmaskedtexturecol = ds.getMaskedTextureColPointer();

        rw_scalestep = ds.scalestep;
        spryscale = ds.scale1 + (x1 - ds.x1) * rw_scalestep;

        // HACK to get "pointers" inside clipping lists
        mfloorclip = ds.getSprBottomClipList();
        p_mfloorclip = ds.getSprBottomClipPointer();
        mceilingclip = ds.getSprTopClipList();
        p_mceilingclip = ds.getSprTopClipPointer();
        // find positioning
        if ((MyBSP.curline.linedef.flags & ML_DONTPEGBOTTOM) != 0) {
            maskedcvars.dc_texturemid = frontsector.floorheight > backsector.floorheight ? frontsector.floorheight
                    : backsector.floorheight;
            maskedcvars.dc_texturemid = maskedcvars.dc_texturemid + TexMan.getTextureheight(texnum)
                    - view.z;
        } else {
            maskedcvars.dc_texturemid = frontsector.ceilingheight < backsector.ceilingheight
                ? frontsector.ceilingheight
                : backsector.ceilingheight;
            
            maskedcvars.dc_texturemid -= view.z;
        }
        maskedcvars.dc_texturemid += MyBSP.curline.sidedef.rowoffset;

        if (colormaps.fixedcolormap != null)
            maskedcvars.dc_colormap = colormaps.fixedcolormap;

        // Texture height must be set at this point. This will trigger
        // tiling. For sprites, it should be set to 0.
        maskedcvars.dc_texheight = TexMan.getTextureheight(texnum) >> FRACBITS;

        // draw the columns
        for (maskedcvars.dc_x = x1; maskedcvars.dc_x <= x2; maskedcvars.dc_x++) {
            // calculate lighting
            if (maskedtexturecol[pmaskedtexturecol + maskedcvars.dc_x] != Short.MAX_VALUE) {
                if (colormaps.fixedcolormap == null) {
                    index = spryscale >>> colormaps.lightScaleShift();

                    if (index >= colormaps.maxLightScale())
                        index = colormaps.maxLightScale() - 1;

                    maskedcvars.dc_colormap = colormaps.walllights[index];
                }

                sprtopscreen = view.centeryfrac
                        - FixedMul(maskedcvars.dc_texturemid, spryscale);
                maskedcvars.dc_iscale = (int) (0xffffffffL / spryscale);

                // draw the texture
                column_t data = TexMan.GetSmpColumn(texnum,
                        maskedtexturecol[pmaskedtexturecol + maskedcvars.dc_x],id);
                
                DrawMaskedColumn(data);
                maskedtexturecol[pmaskedtexturecol + maskedcvars.dc_x] = Short.MAX_VALUE;
            }
            spryscale += rw_scalestep;
        }

    }		
    
    /**
     * R_DrawPSprite
     * 
     * Draws a "player sprite" with slighly different rules than normal
     * sprites. This is actually a PITA, at best :-/
     * 
     * Also different than normal implementation.
     * 
     */

    @Override
    protected final void DrawPSprite(pspdef_t psp) {

        int tx;
        int x1;
        int x2;
        spritedef_t sprdef;
        spriteframe_t sprframe;
        vissprite_t<V> vis;
        int lump;
        boolean flip;

        //

        // decide which patch to use (in terms of angle?)
        if (RANGECHECK) {
            if (psp.state.sprite.ordinal() >= SM.getNumSprites()) {
                I.Error("R_ProjectSprite: invalid sprite number %d ", psp.state.sprite);
            }
        }

        sprdef = SM.getSprite(psp.state.sprite.ordinal());
        
        if (RANGECHECK) {
            if ((psp.state.frame & FF_FRAMEMASK) >= sprdef.numframes) {
                I.Error("R_ProjectSprite: invalid sprite frame %d : %d ", psp.state.sprite, psp.state.frame);
            }
        }
        
        sprframe = sprdef.spriteframes[psp.state.frame & FF_FRAMEMASK];

        // Base frame for "angle 0" aka viewed from dead-front.
        lump = sprframe.lump[0];
        // Q: where can this be set? A: at sprite loadtime.
        flip = sprframe.flip[0] != 0;

        // calculate edges of the shape. tx is expressed in "view units".
        tx = FixedMul(psp.sx, view.BOBADJUST) - view.WEAPONADJUST;

        tx -= spriteoffset[lump];

        // So...centerxfrac is the center of the screen (pixel coords in
        // fixed point).
        x1 = (view.centerxfrac + FixedMul(tx, pspritescale)) >> FRACBITS;

        // off the right side
        if (x1 > endx)
            return;

        tx += spritewidth[lump];
        x2 = ((view.centerxfrac + FixedMul(tx, pspritescale)) >> FRACBITS) - 1;

        // off the left side
        if (x2 < startx)
            return;

        // store information in a vissprite ?
        vis = avis;
        vis.mobjflags = 0;
        vis.texturemid = ((BASEYCENTER+view.lookdir) << FRACBITS) + FRACUNIT / 2
                - (psp.sy - spritetopoffset[lump]);
        vis.x1 = x1 < startx ? startx : x1;
        vis.x2 = x2 >= endx ? endx - 1 : x2;
        vis.scale = (pspritescale) << view.detailshift;

        if (flip) {
            vis.xiscale = -pspriteiscale;
            vis.startfrac = spritewidth[lump] - 1;
        } else {
            vis.xiscale = pspriteiscale;
            vis.startfrac = 0;
        }

        if (vis.x1 > x1)
            vis.startfrac += vis.xiscale * (vis.x1 - x1);

        vis.patch = lump;

        if ((view.player.powers[pw_invisibility] > 4 * 32)
                || (view.player.powers[pw_invisibility] & 8) != 0) {
            // shadow draw
            vis.colormap = null;

        } else if (colormaps.fixedcolormap != null) {
            // fixed color
            vis.colormap = colormaps.fixedcolormap;
            // vis.pcolormap=0;
        } else if ((psp.state.frame & FF_FULLBRIGHT) != 0) {
            // full bright
            vis.colormap = colormaps.colormaps[Palettes.COLORMAP_FIXED];
            // vis.pcolormap=0;
        } else {
            // local light
            vis.colormap = colormaps.spritelights[colormaps.maxLightScale() - 1];
        }

        //System.out.printf("Weapon draw from %d to %d\n",vis.x1,vis.x2);
        DrawVisSprite(vis);
    }
    
    
    /**
     * R_DrawMasked
     * 
     * Sorts and draws vissprites (room for optimization in sorting func.)
     * Draws masked textures. Draws player weapons and overlays (psprites).
     * 
     * Sorting function can be swapped for almost anything, and it will work
     * better, in-place and be simpler to draw, too.
     * 
     * 
     */
    
    @Override
    public void run() {
        // vissprite_t spr;
        int ds;
        drawseg_t dss;

        // Sprites should already be sorted for distance 

        colfunc = colfuncs.masked; // Sprites use fully-masked capable
                                 // function.

        // Update view height
        
        this.maskedcvars.viewheight=view.height;
        this.maskedcvars.centery=view.centery;
        this.startx=((id*view.width)/numthreads);
        this.endx=(((id+1)*view.width)/numthreads);
        
        // Update thread's own vissprites
        
        final vissprite_t<V>[] vissprites=VIS.getVisSprites();
        final int numvissprites=VIS.getNumVisSprites();
        
        //System.out.printf("Sprites to render: %d\n",numvissprites);
        
        // Try drawing all sprites that are on your side of
        // the screen. Limit by x1 and x2, if you have to.
        for (int i = 0; i < numvissprites; i++) {
            DrawSprite(vissprites[i]);
        }
        
        //System.out.printf("Segs to render: %d\n",ds_p);

        // render any remaining masked mid textures
        for (ds = seg_vars.ds_p - 1; ds >= 0; ds--) {
            dss = seg_vars.drawsegs[ds];
            if (!(dss.x1>endx || dss.x2<startx)&&!dss.nullMaskedTextureCol())
                RenderMaskedSegRange(dss, dss.x1,dss.x2);
        }
        // draw the psprites on top of everything
        // but does not draw on side views
        // if (viewangleoffset==0)

        colfunc = colfuncs.player;
        DrawPlayerSprites();
        colfunc = colfuncs.masked;
        
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // TODO Auto-generated catch block
    }
    
}
