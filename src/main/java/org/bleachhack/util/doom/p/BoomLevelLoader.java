package org.bleachhack.util.doom.p;

import static org.bleachhack.util.doom.boom.Compatibility.*;
import org.bleachhack.util.doom.boom.DeepBSPNodesV4;
import static org.bleachhack.util.doom.boom.E6Y.NO_INDEX;
import org.bleachhack.util.doom.boom.mapglvertex_t;
import org.bleachhack.util.doom.boom.mapnode_v4_t;
import org.bleachhack.util.doom.boom.mapnode_znod_t;
import org.bleachhack.util.doom.boom.mapseg_v4_t;
import org.bleachhack.util.doom.boom.mapseg_znod_t;
import org.bleachhack.util.doom.boom.mapsubsector_v4_t;
import org.bleachhack.util.doom.boom.mapsubsector_znod_t;
import static org.bleachhack.util.doom.data.Defines.*;
import org.bleachhack.util.doom.data.Limits;
import org.bleachhack.util.doom.data.maplinedef_t;
import org.bleachhack.util.doom.data.mapnode_t;
import org.bleachhack.util.doom.data.mapsector_t;
import org.bleachhack.util.doom.data.mapseg_t;
import org.bleachhack.util.doom.data.mapsidedef_t;
import org.bleachhack.util.doom.data.mapsubsector_t;
import org.bleachhack.util.doom.data.mapthing_t;
import org.bleachhack.util.doom.data.mapvertex_t;
import org.bleachhack.util.doom.defines.skill_t;
import org.bleachhack.util.doom.defines.slopetype_t;
import org.bleachhack.util.doom.doom.CommandVariable;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.DoomStatus;
import org.bleachhack.util.doom.doom.SourceCode;
import org.bleachhack.util.doom.doom.SourceCode.CauseOfDesyncProbability;
import org.bleachhack.util.doom.doom.SourceCode.P_Setup;
import static org.bleachhack.util.doom.doom.SourceCode.P_Setup.P_LoadThings;
import static org.bleachhack.util.doom.doom.SourceCode.P_Setup.P_SetupLevel;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.function.IntFunction;
import org.bleachhack.util.doom.m.BBox;
import static org.bleachhack.util.doom.m.BBox.*;
import org.bleachhack.util.doom.m.fixed_t;
import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;
import static org.bleachhack.util.doom.m.fixed_t.FRACUNIT;
import org.bleachhack.util.doom.rr.RendererState;
import org.bleachhack.util.doom.rr.line_t;
import static org.bleachhack.util.doom.rr.line_t.ML_TWOSIDED;
import org.bleachhack.util.doom.rr.node_t;
import org.bleachhack.util.doom.rr.sector_t;
import org.bleachhack.util.doom.rr.seg_t;
import org.bleachhack.util.doom.rr.side_t;
import org.bleachhack.util.doom.rr.subsector_t;
import org.bleachhack.util.doom.rr.vertex_t;
import org.bleachhack.util.doom.rr.z_vertex_t;
import org.bleachhack.util.doom.s.degenmobj_t;
import org.bleachhack.util.doom.utils.C2JUtils;
import static org.bleachhack.util.doom.utils.C2JUtils.flags;
import static org.bleachhack.util.doom.utils.C2JUtils.unsigned;
import org.bleachhack.util.doom.utils.GenericCopy.ArraySupplier;
import static org.bleachhack.util.doom.utils.GenericCopy.malloc;
import org.bleachhack.util.doom.wad.CacheableDoomObjectContainer;
import org.bleachhack.util.doom.wad.DoomBuffer;
import org.bleachhack.util.doom.wad.wadfile_info_t;

/*
 * Emacs style mode select -*- C++ -*-
 * -----------------------------------------------------------------------------
 * PrBoom: a Doom port merged with LxDoom and LSDLDoom based on BOOM, a modified
 * and improved DOOM engine Copyright (C) 1999 by id Software, Chi Hoang, Lee
 * Killough, Jim Flynn, Rand Phares, Ty Halderman Copyright (C) 1999-2000 by
 * Jess Haas, Nicolas Kalkhof, Colin Phipps, Florian Schulze Copyright 2005,
 * 2006 by Florian Schulze, Colin Phipps, Neil Stevens, Andrey Budko This
 * program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. DESCRIPTION: Do all
 * the WAD I/O, get map description, set up initial state and misc. LUTs.
 * 
 * MAES 30/9/2011: This is a direct translation of prBoom+'s 2.5.0.8 p_setup.c
 * and p_setup.h.
 * 
 * 
 * 
 * -----------------------------------------------------------------------------
 */

public class BoomLevelLoader extends AbstractLevelLoader {

    public BoomLevelLoader(DoomMain<?,?> DM) {
        super(DM);
        // TODO Auto-generated constructor stub
    }

    // OpenGL related.
    byte[] map_subsectors;

    // //////////////////////////////////////////////////////////////////////////////////////////
    // figgi 08/21/00 -- finalants and globals for glBsp support
    public static final int gNd2 = 0x32644E67; // figgi -- suppport for new
                                               // GL_VERT format v2.0

    public static final int gNd3 = 0x33644E67;

    public static final int gNd4 = 0x34644E67;

    public static final int gNd5 = 0x35644E67;

    public static final int ZNOD = 0x444F4E5A;

    public static final int ZGLN = 0x4E4C475A;

    public static final int GL_VERT_OFFSET = 4;

    int firstglvertex = 0;

    int nodesVersion = 0;

    boolean forceOldBsp = false;

    // figgi 08/21/00 -- glSegs
    class glseg_t {
        char v1; // start vertex (16 bit)

        char v2; // end vertex (16 bit)

        char linedef; // linedef, or -1 for minisegs

        short side; // side on linedef: 0 for right, 1 for left

        short partner; // corresponding partner seg, or -1 on one-sided walls
    }

    public static final int ML_GL_LABEL = 0; // A separator name, GL_ExMx or
                                             // GL_MAPxx

    public static final int ML_GL_VERTS = 1; // Extra Vertices

    public static final int ML_GL_SEGS = 2; // Segs, from linedefs & minisegs

    public static final int ML_GL_SSECT = 3; // SubSectors, list of segs

    public static final int ML_GL_NODES = 4; // GL BSP nodes

    // //////////////////////////////////////////////////////////////////////////////////////////

    //
    // REJECT
    // For fast sight rejection.
    // Speeds up enemy AI by skipping detailed
    // LineOf Sight calculation.
    // Without the special effect, this could
    // be used as a PVS lookup as well.
    //

    private int rejectlump = -1;// cph - store reject lump num if cached

    private int current_episode = -1;

    private int current_map = -1;

    private int current_nodesVersion = -1;

    private boolean samelevel = false;

    /**
     * e6y: Smart malloc Used by P_SetupLevel() for smart data loading. Do
     * nothing if level is the same. Passing a null array forces allocation.
     * 
     * @param p
     *        generically typed array to consider
     * @param numstuff
     *        elements to realloc
     */

    private <T> T[] malloc_IfSameLevel(T[] p, int numstuff, ArraySupplier<T> supplier, IntFunction<T[]> generator) {
        if (!samelevel || (p == null)) {
            return malloc(supplier, generator, numstuff);
        }
        return p;
    }

    // e6y: Smart calloc
    // Used by P_SetupLevel() for smart data loading
    // Clear the memory without allocation if level is the same
    private <T extends Resettable> T[] calloc_IfSameLevel(T[] p, int numstuff, ArraySupplier<T> supplier, IntFunction<T[]> generator) {
        if (!samelevel) {
            return malloc(supplier, generator, numstuff);
        } else {
            // TODO: stuff should be resetted!
            C2JUtils.resetAll(p);
            return p;
        }
    }

    //
    // P_CheckForZDoomNodes
    //

    private boolean P_CheckForZDoomNodes(int lumpnum, int gl_lumpnum) {
        byte[] data;
        int check;
        
        data = DOOM.wadLoader.CacheLumpNumAsRawBytes(lumpnum + ML_NODES, 0);
        check = ByteBuffer.wrap(data).getInt();
        
        if (check == ZNOD)
            DOOM.doomSystem.Error("P_CheckForZDoomNodes: ZDoom nodes not supported yet");

        data = DOOM.wadLoader.CacheLumpNumAsRawBytes(lumpnum + ML_SSECTORS, 0);
        check = ByteBuffer.wrap(data).getInt();
        
        if (check == ZGLN) {
            DOOM.doomSystem.Error("P_CheckForZDoomNodes: ZDoom GL nodes not supported yet");
        }

        // Unlock them to force different buffering interpretation.
        DOOM.wadLoader.UnlockLumpNum(lumpnum + ML_NODES);
        DOOM.wadLoader.UnlockLumpNum(lumpnum + ML_SSECTORS);

        return false;
    }

    //
    // P_CheckForDeePBSPv4Nodes
    // http://www.sbsoftware.com/files/DeePBSPV4specs.txt
    //

    private boolean P_CheckForDeePBSPv4Nodes(int lumpnum, int gl_lumpnum) {
        byte[] data;
        boolean result = false;

        data = DOOM.wadLoader.CacheLumpNumAsRawBytes(lumpnum + ML_NODES, 0);
        byte[] compare = Arrays.copyOfRange(data, 0, 7);

        if (Arrays.equals(compare, DeepBSPNodesV4.DeepBSPHeader)) {
            System.out.println("P_CheckForDeePBSPv4Nodes: DeePBSP v4 Extended nodes are detected");
            result = true;
        }

        DOOM.wadLoader.UnlockLumpNum(lumpnum + ML_NODES);

        return result;
    }

    //
    // P_CheckForZDoomUncompressedNodes
    // http://zdoom.org/wiki/ZDBSP#Compressed_Nodes
    //

    private static final int XNOD = 0x584e4f44;

    private boolean P_CheckForZDoomUncompressedNodes(int lumpnum, int gl_lumpnum) {
        byte[] data;
        int wrapper;
        boolean result = false;

        data = DOOM.wadLoader.CacheLumpNumAsRawBytes(lumpnum + ML_NODES, 0);
        wrapper=ByteBuffer.wrap(data).getInt();

        if (wrapper==XNOD) {
            System.out.println("P_CheckForZDoomUncompressedNodes: ZDoom uncompressed normal nodes are detected");
            result = true;
        }

        DOOM.wadLoader.UnlockLumpNum(lumpnum + ML_NODES);

        return result;
    }

    //
    // P_GetNodesVersion
    //

    public void P_GetNodesVersion(int lumpnum, int gl_lumpnum) {
        int ver = -1;
        nodesVersion = 0;

        if ((gl_lumpnum > lumpnum) && (forceOldBsp == false)
            &&(DoomStatus.compatibility_level>=prboom_2_compatibility)
                                                             ) {

            byte[] data = DOOM.wadLoader.CacheLumpNumAsRawBytes(gl_lumpnum + ML_GL_VERTS, 0);
            int wrapper = ByteBuffer.wrap(data).getInt();
            if (wrapper == gNd2) {
                data = DOOM.wadLoader.CacheLumpNumAsRawBytes(gl_lumpnum + ML_GL_SEGS, 0);
                wrapper = ByteBuffer.wrap(data).getInt();
                if (wrapper == gNd3) {
                    ver = 3;
                } else {
                    nodesVersion = gNd2;
                    System.out.println("P_GetNodesVersion: found version 2 nodes");
                }
            }
            if (wrapper == gNd4) {
                ver = 4;
            }
            if (wrapper == gNd5) {
                ver = 5;
            }
            // e6y: unknown gl nodes will be ignored
            if (nodesVersion == 0 && ver != -1) {
                System.out.printf("P_GetNodesVersion: found version %d nodes\n", ver);
                System.out.printf("P_GetNodesVersion: version %d nodes not supported\n", ver);
            }
        } else {
            nodesVersion = 0;
            System.out.println("P_GetNodesVersion: using normal BSP nodes");
            if (P_CheckForZDoomNodes(lumpnum, gl_lumpnum)) {
                DOOM.doomSystem.Error("P_GetNodesVersion: ZDoom nodes not supported yet");
            }
        }
    }

    //
    // P_LoadVertexes
    //
    // killough 5/3/98: reformatted, cleaned up
    //
    private void P_LoadVertexes(int lump) {
        final mapvertex_t[] data; // cph - final

        // Determine number of lumps:
        // total lump length / vertex record length.
        numvertexes = DOOM.wadLoader.LumpLength(lump) / mapvertex_t.sizeOf();

        // Allocate zone memory for buffer.
        vertexes = calloc_IfSameLevel(vertexes, numvertexes, vertex_t::new, vertex_t[]::new);

        // Load data into cache.
        // cph 2006/07/29 - cast to mapvertex_t here, making the loop below much
        // neater
        data = DOOM.wadLoader.CacheLumpNumIntoArray(lump, numvertexes, mapvertex_t::new, mapvertex_t[]::new);

        // Copy and convert vertex coordinates,
        // internal representation as fixed.
        for (int i = 0; i < numvertexes; i++) {
            vertexes[i].x = data[i].x << org.bleachhack.util.doom.m.fixed_t.FRACBITS;
            vertexes[i].y = data[i].y << FRACBITS;
        }

        // Free buffer memory.
        DOOM.wadLoader.UnlockLumpNum(lump);
    }

    /*******************************************
     * Name : P_LoadVertexes2 * modified : 09/18/00, adapted for PrBoom * author
     * : figgi * what : support for gl nodes
     * 
     * @throws IOException
     *         *
     *******************************************/

    // figgi -- FIXME: Automap showes wrong zoom boundaries when starting game
    // when P_LoadVertexes2 is used with classic BSP nodes.

    private void P_LoadVertexes2(int lump, int gllump) throws IOException {
        final ByteBuffer gldata;
        mapvertex_t[] ml;

        // GL vertexes come after regular ones.
        firstglvertex = DOOM.wadLoader.LumpLength(lump) / mapvertex_t.sizeOf();
        numvertexes = DOOM.wadLoader.LumpLength(lump) / mapvertex_t.sizeOf();

        if (gllump >= 0) { // check for glVertices
            // Read GL lump into buffer. This allows some flexibility
            gldata = DOOM.wadLoader.CacheLumpNumAsDoomBuffer(gllump).getBuffer();

            if (nodesVersion == gNd2) { // 32 bit GL_VERT format (16.16 fixed)
                // These vertexes are double in size than regular Doom vertexes.
                // Furthermore, we have to skip the first 4 bytes
                // (GL_VERT_OFFSET)
                // of the gl lump.
                numvertexes += (DOOM.wadLoader.LumpLength(gllump) - GL_VERT_OFFSET) / mapglvertex_t.sizeOf();

                // Vertexes size accomodates both normal and GL nodes.
                vertexes = malloc_IfSameLevel(vertexes, numvertexes, vertex_t::new, vertex_t[]::new);

                final mapglvertex_t mgl[] = malloc(mapglvertex_t::new, mapglvertex_t[]::new, numvertexes - firstglvertex);

                // Get lump and skip first 4 bytes
                gldata.rewind();
                gldata.position(GL_VERT_OFFSET);

                CacheableDoomObjectContainer.unpack(gldata, mgl);

                int mgl_count = 0;

                for (int i = firstglvertex; i < numvertexes; i++) {
                    vertexes[i].x = mgl[mgl_count].x;
                    vertexes[i].y = mgl[mgl_count].y;
                    mgl_count++;
                }
            } else {
                // Vertexes size accomodates both normal and GL nodes.
                numvertexes += DOOM.wadLoader.LumpLength(gllump) / mapvertex_t.sizeOf();
                vertexes = malloc_IfSameLevel(vertexes, numvertexes, vertex_t::new, vertex_t[]::new);

                ml = malloc(mapvertex_t::new, mapvertex_t[]::new, numvertexes - firstglvertex);

                // We can read this "directly" because no skipping is involved.
                gldata.rewind();
                CacheableDoomObjectContainer.unpack(gldata, ml);
                // ml = W.CacheLumpNumIntoArray(gllump,
                // numvertexes-firstglvertex,mapvertex_t.class);
                int ml_count = 0;

                for (int i = firstglvertex; i < numvertexes; i++) {
                    vertexes[i].x = ml[ml_count].x;
                    vertexes[i].y = ml[ml_count].y;
                    ml_count++;
                }
            }
            DOOM.wadLoader.UnlockLumpNum(gllump);
        }

        // Loading of regular lumps (sheesh!)
        ml = DOOM.wadLoader.CacheLumpNumIntoArray(lump, firstglvertex, mapvertex_t::new, mapvertex_t[]::new);

        for (int i = 0; i < firstglvertex; i++) {
            vertexes[i].x = ml[i].x;
            vertexes[i].y = ml[i].y;
        }

        DOOM.wadLoader.UnlockLumpNum(lump);

    }

    /*******************************************
     * created : 08/13/00 * modified : 09/18/00, adapted for PrBoom * author :
     * figgi * what : basic functions needed for * computing gl nodes *
     *******************************************/

    public int checkGLVertex(int num) {
        if ((num & 0x8000) != 0)
            num = (num & 0x7FFF) + firstglvertex;
        return num;
    }

    public static float GetDistance(int dx, int dy) {
        float fx = (float) (dx) / FRACUNIT, fy = (float) (dy) / FRACUNIT;
        return (float) Math.sqrt(fx * fx + fy * fy);
    }

    public static float GetTexelDistance(int dx, int dy) {
        // return (float)((int)(GetDistance(dx, dy) + 0.5f));
        float fx = (float) (dx) / FRACUNIT, fy = (float) (dy) / FRACUNIT;
        return ((int) (0.5f + (float) Math.sqrt(fx * fx + fy * fy)));
    }

    public static int GetOffset(vertex_t v1, vertex_t v2) {
        float a, b;
        int r;
        a = (v1.x - v2.x) / (float) FRACUNIT;
        b = (v1.y - v2.y) / (float) FRACUNIT;
        r = (int) (Math.sqrt(a * a + b * b) * FRACUNIT);
        return r;
    }

    //
    // P_LoadSegs
    //
    // killough 5/3/98: reformatted, cleaned up

    private void P_LoadSegs(int lump) {
        final mapseg_t[] data; // cph - final

        numsegs = DOOM.wadLoader.LumpLength(lump) / mapseg_t.sizeOf();
        segs = calloc_IfSameLevel(segs, numsegs, seg_t::new, seg_t[]::new);

        data = DOOM.wadLoader.CacheLumpNumIntoArray(lump, numsegs, mapseg_t::new, mapseg_t[]::new); // cph -
                                                                       // wad
                                                                       // lump
                                                                       // handling
                                                                       // updated

        if ((data == null) || (numsegs == 0))
            DOOM.doomSystem.Error("P_LoadSegs: no segs in level");

        for (int i = 0; i < numsegs; i++) {
            seg_t li = segs[i];
            final mapseg_t ml = data[i];
            char v1, v2;

            int side, linedef;
            line_t ldef;

            li.iSegID = i; // proff 11/05/2000: needed for OpenGL

            v1 = ml.v1;
            v2 = ml.v2;

            // e6y
            // moved down for additional checks to avoid overflow
            // if wrong vertexe's indexes are in SEGS lump
            // see below for more detailed information
            // li.v1 = &vertexes[v1];
            // li.v2 = &vertexes[v2];

            li.miniseg = false; // figgi -- there are no minisegs in classic BSP
                                // nodes

            // e6y: moved down, see below
            // li.length = GetDistance(li.v2.x - li.v1.x, li.v2.y - li.v1.y);

            li.angle = ml.angle << 16;
            li.offset = ml.offset << 16;
            linedef = ml.linedef;

            // e6y: check for wrong indexes
            if (linedef >= numlines) {
                DOOM.doomSystem.Error( "P_LoadSegs: seg %d references a non-existent linedef %d", i, linedef);
            }

            ldef = lines[linedef];
            li.linedef = ldef;
            side = ml.side;

            // e6y: fix wrong side index
            if (side != 0 && side != 1) {
                System.err.printf("P_LoadSegs: seg %d contains wrong side index %d. Replaced with 1.\n", i, side);
                side = 1;
            }

            // e6y: check for wrong indexes
            if (ldef.sidenum[side] >= (char) numsides) {
                DOOM.doomSystem.Error(
                    "P_LoadSegs: linedef %d for seg %d references a non-existent sidedef %d",
                    linedef, i, ldef.sidenum[side]
                );
            }

            li.sidedef = sides[ldef.sidenum[side]];

            /*
             * cph 2006/09/30 - our frontsector can be the second side of the
             * linedef, so must check for NO_INDEX in case we are incorrectly
             * referencing the back of a 1S line
             */
            if (ldef.sidenum[side] != NO_INDEX)
                li.frontsector = sides[ldef.sidenum[side]].sector;
            else {
                li.frontsector = null;
                System.err.printf("P_LoadSegs: front of seg %i has no sidedef\n", i);
            }

            if (flags(ldef.flags, ML_TWOSIDED) && ldef.sidenum[side ^ 1] != NO_INDEX) {
                li.backsector = sides[ldef.sidenum[side ^ 1]].sector;
            } else {
                li.backsector = null;
            }

            // e6y
            // check and fix wrong references to non-existent vertexes
            // see e1m9 @ NIVELES.WAD
            // http://www.doomworld.com/idgames/index.php?id=12647
            if (v1 >= numvertexes || v2 >= numvertexes) {
                String str = "P_LoadSegs: compatibility loss - seg %d references a non-existent vertex %d\n";

                if (DOOM.demorecording) {
                    DOOM.doomSystem.Error(
                        str + "Demo recording on levels with invalid nodes is not allowed",
                        i, (v1 >= numvertexes ? v1 : v2)
                    );
                }

                if (v1 >= numvertexes) {
                    System.err.printf(str, i, v1);
                }
                if (v2 >= numvertexes) {
                    System.err.printf(str, i, v2);
                }

                if (li.sidedef == sides[li.linedef.sidenum[0]]) {
                    li.v1 = lines[ml.linedef].v1;
                    li.v2 = lines[ml.linedef].v2;
                } else {
                    li.v1 = lines[ml.linedef].v2;
                    li.v2 = lines[ml.linedef].v1;
                }
            } else {
                li.v1 = vertexes[v1];
                li.v2 = vertexes[v2];
            }

            li.assignVertexValues();

            // e6y: now we can calculate it
            li.length = GetDistance(li.v2x - li.v1x, li.v2y - li.v1y);

            // Recalculate seg offsets that are sometimes incorrect
            // with certain nodebuilders. Fixes among others, line 20365
            // of DV.wad, map 5
            li.offset = GetOffset(li.v1, (ml.side != 0 ? ldef.v2 : ldef.v1));
        }

        DOOM.wadLoader.UnlockLumpNum(lump); // cph - release the data
    }

    private void P_LoadSegs_V4(int lump) {
        int i;
        mapseg_v4_t[] data;

        numsegs = DOOM.wadLoader.LumpLength(lump) / mapseg_v4_t.sizeOf();
        segs = calloc_IfSameLevel(segs, numsegs, seg_t::new, seg_t[]::new);
        data = DOOM.wadLoader.CacheLumpNumIntoArray(lump, numsegs, mapseg_v4_t::new, mapseg_v4_t[]::new);

        if ((data == null) || (numsegs == 0))
            DOOM.doomSystem.Error("P_LoadSegs_V4: no segs in level");

        for (i = 0; i < numsegs; i++) {
            seg_t li = segs[i];
            final mapseg_v4_t ml = data[i];
            int v1, v2;

            int side, linedef;
            line_t ldef;

            li.iSegID = i; // proff 11/05/2000: needed for OpenGL

            v1 = ml.v1;
            v2 = ml.v2;

            li.miniseg = false; // figgi -- there are no minisegs in classic BSP
                                // nodes

            li.angle = ml.angle << 16;
            li.offset = ml.offset << 16;
            linedef = ml.linedef;

            // e6y: check for wrong indexes
            if (unsigned(linedef) >= unsigned(numlines)) {
                DOOM.doomSystem.Error(
                    "P_LoadSegs_V4: seg %d references a non-existent linedef %d",
                    i, unsigned(linedef));
            }

            ldef = lines[linedef];
            li.linedef = ldef;
            side = ml.side;

            // e6y: fix wrong side index
            if (side != 0 && side != 1) {
                System.err.printf("P_LoadSegs_V4: seg %d contains wrong side index %d. Replaced with 1.\n", i, side);
                side = 1;
            }

            // e6y: check for wrong indexes
            if (unsigned(ldef.sidenum[side]) >= unsigned(numsides)) {
                DOOM.doomSystem.Error(
                    "P_LoadSegs_V4: linedef %d for seg %d references a non-existent sidedef %d",
                    linedef, i, unsigned(ldef.sidenum[side])
                );
            }

            li.sidedef = sides[ldef.sidenum[side]];

            /*
             * cph 2006/09/30 - our frontsector can be the second side of the
             * linedef, so must check for NO_INDEX in case we are incorrectly
             * referencing the back of a 1S line
             */
            if (ldef.sidenum[side] != NO_INDEX) {
                li.frontsector = sides[ldef.sidenum[side]].sector;
            } else {
                li.frontsector = null;
                System.err.printf("P_LoadSegs_V4: front of seg %i has no sidedef\n", i);
            }

            if (flags(ldef.flags, ML_TWOSIDED)
                && ldef.sidenum[side ^ 1] != NO_INDEX) {
                li.backsector = sides[ldef.sidenum[side ^ 1]].sector;
            } else {
                li.backsector = null;
            }

            // e6y
            // check and fix wrong references to non-existent vertexes
            // see e1m9 @ NIVELES.WAD
            // http://www.doomworld.com/idgames/index.php?id=12647
            if (v1 >= numvertexes || v2 >= numvertexes) {
                String str = "P_LoadSegs_V4: compatibility loss - seg %d references a non-existent vertex %d\n";

                if (DOOM.demorecording) {
                    DOOM.doomSystem.Error(
                        (str + "Demo recording on levels with invalid nodes is not allowed"),
                        i, (v1 >= numvertexes ? v1 : v2)
                    );
                }

                if (v1 >= numvertexes) {
                    System.err.printf(str, i, v1);
                }
                if (v2 >= numvertexes) {
                    System.err.printf(str, i, v2);
                }

                if (li.sidedef == sides[li.linedef.sidenum[0]]) {
                    li.v1 = lines[ml.linedef].v1;
                    li.v2 = lines[ml.linedef].v2;
                } else {
                    li.v1 = lines[ml.linedef].v2;
                    li.v2 = lines[ml.linedef].v1;
                }
            } else {
                li.v1 = vertexes[v1];
                li.v2 = vertexes[v2];
            }

            // e6y: now we can calculate it
            li.length = GetDistance(li.v2.x - li.v1.x, li.v2.y - li.v1.y);

            // Recalculate seg offsets that are sometimes incorrect
            // with certain nodebuilders. Fixes among others, line 20365
            // of DV.wad, map 5
            li.offset = GetOffset(li.v1, (ml.side != 0 ? ldef.v2 : ldef.v1));
        }

        DOOM.wadLoader.UnlockLumpNum(lump); // cph - release the data
    }

    /*******************************************
     * Name : P_LoadGLSegs * created : 08/13/00 * modified : 09/18/00, adapted
     * for PrBoom * author : figgi * what : support for gl nodes *
     *******************************************/
    /*
     * private void P_LoadGLSegs(int lump) { int i; final glseg_t ml; line_t
     * ldef; numsegs = W.LumpLength(lump) / sizeof(glseg_t); segs =
     * malloc_IfSameLevel(segs, numsegs * sizeof(seg_t)); memset(segs, 0,
     * numsegs * sizeof(seg_t)); ml = (final glseg_t*)W.CacheLumpNum(lump); if
     * ((!ml) || (!numsegs)) I_Error("P_LoadGLSegs: no glsegs in level"); for(i
     * = 0; i < numsegs; i++) { // check for gl-vertices segs[i].v1 =
     * &vertexes[checkGLVertex(LittleShort(ml.v1))]; segs[i].v2 =
     * &vertexes[checkGLVertex(LittleShort(ml.v2))]; segs[i].iSegID = i;
     * if(ml.linedef != (unsigned short)-1) // skip minisegs { ldef =
     * &lines[ml.linedef]; segs[i].linedef = ldef; segs[i].miniseg = false;
     * segs[i].angle =
     * R_PointToAngle2(segs[i].v1.x,segs[i].v1.y,segs[i].v2.x,segs[i].v2.y);
     * segs[i].sidedef = &sides[ldef.sidenum[ml.side]]; segs[i].length =
     * GetDistance(segs[i].v2.x - segs[i].v1.x, segs[i].v2.y - segs[i].v1.y);
     * segs[i].frontsector = sides[ldef.sidenum[ml.side]].sector; if (ldef.flags
     * & ML_TWOSIDED) segs[i].backsector =
     * sides[ldef.sidenum[ml.side^1]].sector; else segs[i].backsector = 0; if
     * (ml.side) segs[i].offset = GetOffset(segs[i].v1, ldef.v2); else
     * segs[i].offset = GetOffset(segs[i].v1, ldef.v1); } else { segs[i].miniseg
     * = true; segs[i].angle = 0; segs[i].offset = 0; segs[i].length = 0;
     * segs[i].linedef = NULL; segs[i].sidedef = NULL; segs[i].frontsector =
     * NULL; segs[i].backsector = NULL; } ml++; } W.UnlockLumpNum(lump); }
     */

    //
    // P_LoadSubsectors
    //
    // killough 5/3/98: reformatted, cleaned up

    private void P_LoadSubsectors(int lump) {
        /*
         * cph 2006/07/29 - make data a final mapsubsector_t *, so the loop
         * below is simpler & gives no finalness warnings
         */
        final mapsubsector_t[] data;

        numsubsectors = DOOM.wadLoader.LumpLength(lump) / mapsubsector_t.sizeOf();
        subsectors = calloc_IfSameLevel(subsectors, numsubsectors, subsector_t::new, subsector_t[]::new);
        data = DOOM.wadLoader.CacheLumpNumIntoArray(lump, numsubsectors, mapsubsector_t::new, mapsubsector_t[]::new);

        if ((data == null) || (numsubsectors == 0)) {
            DOOM.doomSystem.Error("P_LoadSubsectors: no subsectors in level");
        }

        for (int i = 0; i < numsubsectors; i++) {
            // e6y: support for extended nodes
            subsectors[i].numlines = data[i].numsegs;
            subsectors[i].firstline = data[i].firstseg;
        }

        DOOM.wadLoader.UnlockLumpNum(lump); // cph - release the data
    }

    private void P_LoadSubsectors_V4(int lump) {
        /*
         * cph 2006/07/29 - make data a final mapsubsector_t *, so the loop
         * below is simpler & gives no finalness warnings
         */
        final mapsubsector_v4_t[] data;

        numsubsectors = DOOM.wadLoader.LumpLength(lump) / mapsubsector_v4_t.sizeOf();
        subsectors = calloc_IfSameLevel(subsectors, numsubsectors, subsector_t::new, subsector_t[]::new);
        data = DOOM.wadLoader.CacheLumpNumIntoArray(lump, numsubsectors, mapsubsector_v4_t::new, mapsubsector_v4_t[]::new);

        if ((data == null) || (numsubsectors == 0))
            DOOM.doomSystem.Error("P_LoadSubsectors_V4: no subsectors in level");

        for (int i = 0; i < numsubsectors; i++) {
            subsectors[i].numlines = data[i].numsegs;
            subsectors[i].firstline = data[i].firstseg;
        }

        DOOM.wadLoader.UnlockLumpNum(lump); // cph - release the data
    }

    //
    // P_LoadSectors
    //
    // killough 5/3/98: reformatted, cleaned up

    private void P_LoadSectors(int lump) {
        final mapsector_t[] data; // cph - final*

        numsectors = DOOM.wadLoader.LumpLength(lump) / mapsector_t.sizeOf();
        sectors = calloc_IfSameLevel(sectors, numsectors, sector_t::new, sector_t[]::new);
        data = DOOM.wadLoader.CacheLumpNumIntoArray(lump, numsectors, mapsector_t::new, mapsector_t[]::new); // cph
                                                                             // -
                                                                             // wad
                                                                             // lump
                                                                             // handling
                                                                             // updated

        for (int i = 0; i < numsectors; i++) {
            sector_t ss = sectors[i];
            final mapsector_t ms = data[i];

            ss.id = i; // proff 04/05/2000: needed for OpenGL
            ss.floorheight = ms.floorheight << FRACBITS;
            ss.ceilingheight = ms.ceilingheight << FRACBITS;
            ss.floorpic = (short) DOOM.textureManager.FlatNumForName(ms.floorpic);
            ss.ceilingpic = (short) DOOM.textureManager.FlatNumForName(ms.ceilingpic);
            ss.lightlevel = ms.lightlevel;
            ss.special = ms.special;
            // ss.oldspecial = ms.special; huh?
            ss.tag = ms.tag;
            ss.thinglist = null;
            // MAES: link to thinker list and RNG
            ss.TL = this.DOOM.actions;
            ss.RND = this.DOOM.random;

            // ss.touching_thinglist = null; // phares 3/14/98

            // ss.nextsec = -1; //jff 2/26/98 add fields to support locking out
            // ss.prevsec = -1; // stair retriggering until build completes

            // killough 3/7/98:
            // ss.floor_xoffs = 0;
            // ss.floor_yoffs = 0; // floor and ceiling flats offsets
            // ss.ceiling_xoffs = 0;
            // ss.ceiling_yoffs = 0;
            // ss.heightsec = -1; // sector used to get floor and ceiling height
            // ss.floorlightsec = -1; // sector used to get floor lighting
            // killough 3/7/98: end changes

            // killough 4/11/98 sector used to get ceiling lighting:
            // ss.ceilinglightsec = -1;

            // killough 4/4/98: colormaps:
            // ss.bottommap = ss.midmap = ss.topmap = 0;

            // killough 10/98: sky textures coming from sidedefs:
            // ss.sky = 0;
        }

        DOOM.wadLoader.UnlockLumpNum(lump); // cph - release the data
    }

    //
    // P_LoadNodes
    //
    // killough 5/3/98: reformatted, cleaned up

    private void P_LoadNodes(int lump) {
        final mapnode_t[] data; // cph - final*

        numnodes = DOOM.wadLoader.LumpLength(lump) / mapnode_t.sizeOf();
        nodes = malloc_IfSameLevel(nodes, numnodes, node_t::new, node_t[]::new);
        data = DOOM.wadLoader.CacheLumpNumIntoArray(lump, numnodes, mapnode_t::new, mapnode_t[]::new); // cph
                                                                         // -
                                                                         // wad
                                                                         // lump
                                                                         // handling
                                                                         // updated

        if ((data == null) || (numnodes == 0)) {
            // allow trivial maps
            if (numsubsectors == 1)
                System.out
                        .print("P_LoadNodes: trivial map (no nodes, one subsector)\n");
            else
                DOOM.doomSystem.Error("P_LoadNodes: no nodes in level");
        }

        for (int i = 0; i < numnodes; i++) {
            node_t no = nodes[i];
            final mapnode_t mn = data[i];

            no.x = mn.x << FRACBITS;
            no.y = mn.y << FRACBITS;
            no.dx = mn.dx << FRACBITS;
            no.dy = mn.dy << FRACBITS;

            for (int j = 0; j < 2; j++) {
                // e6y: support for extended nodes
                no.children[j] = mn.children[j];

                // e6y: support for extended nodes
                if (no.children[j] == 0xFFFF) {
                    no.children[j] = 0xFFFFFFFF;
                } else if (flags(no.children[j], NF_SUBSECTOR_CLASSIC)) {
                    // Convert to extended type
                    no.children[j] &= ~NF_SUBSECTOR_CLASSIC;

                    // haleyjd 11/06/10: check for invalid subsector reference
                    if (no.children[j] >= numsubsectors) {
                        System.err.printf("P_LoadNodes: BSP tree references invalid subsector %d.\n", no.children[j]);
                        no.children[j] = 0;
                    }

                    no.children[j] |= NF_SUBSECTOR;
                }

                for (int k = 0; k < 4; k++) {
                    no.bbox[j].set(k, mn.bbox[j][k] << FRACBITS);
                }
            }
        }

        DOOM.wadLoader.UnlockLumpNum(lump); // cph - release the data
    }

    private void P_LoadNodes_V4(int lump) {
        final DeepBSPNodesV4 data; // cph - final*

        numnodes = (DOOM.wadLoader.LumpLength(lump) - 8) / mapnode_v4_t.sizeOf();
        nodes = malloc_IfSameLevel(nodes, numnodes, node_t::new, node_t[]::new);
        data = DOOM.wadLoader.CacheLumpNum(lump, 0, DeepBSPNodesV4.class); // cph
                                                                               // -
                                                                               // wad
                                                                               // lump
                                                                               // handling
                                                                               // updated

        if ((data == null) || (numnodes == 0)) {
            // allow trivial maps
            if (numsubsectors == 1) {
                System.out.print("P_LoadNodes_V4: trivial map (no nodes, one subsector)\n");
            } else {
                DOOM.doomSystem.Error("P_LoadNodes_V4: no nodes in level");
            }
        }

        for (int i = 0; i < numnodes; i++) {
            node_t no = nodes[i];
            final mapnode_v4_t mn = data.getNodes()[i];

            no.x = mn.x << FRACBITS;
            no.y = mn.y << FRACBITS;
            no.dx = mn.dx << FRACBITS;
            no.dy = mn.dy << FRACBITS;

            for (int j = 0; j < 2; j++) {
                no.children[j] = mn.children[j];

                for (int k = 0; k < 4; k++) {
                    no.bbox[j].bbox[k] = mn.bbox[j][k] << FRACBITS;
                }
            }
        }

        DOOM.wadLoader.UnlockLumpNum(lump); // cph - release the data
    }

     private void P_LoadZSegs(ByteBuffer data) throws IOException {
        final mapseg_znod_t nodes[] = malloc(mapseg_znod_t::new, mapseg_znod_t[]::new, numsegs);
        CacheableDoomObjectContainer.unpack(data,nodes);

        for (int i = 0; i < numsegs; i++) {
            line_t ldef;
            int v1, v2;
            int linedef;
            char side;
            seg_t li = segs[i];
            final mapseg_znod_t ml = nodes[i];

            v1 = ml.v1;
            v2 = ml.v2;

            li.iSegID = i; // proff 11/05/2000: needed for OpenGL
            li.miniseg = false;

            linedef = ml.linedef;

            // e6y: check for wrong indexes
            if (unsigned(linedef) >= unsigned(numlines)) {
                DOOM.doomSystem.Error(
                    "P_LoadZSegs: seg %d references a non-existent linedef %d",
                    i, unsigned(linedef)
                );
            }

            ldef = lines[linedef];
            li.linedef = ldef;
            side = (char) ml.side;

            // e6y: fix wrong side index
            if (side != 0 && side != 1) {
                System.err.printf("P_LoadZSegs: seg %d contains wrong side index %d. Replaced with 1.\n", i, side);
                side = 1;
            }

            // e6y: check for wrong indexes
            if (unsigned(ldef.sidenum[side]) >= unsigned(numsides)) {
                DOOM.doomSystem.Error(
                    "P_LoadZSegs: linedef %d for seg %d references a non-existent sidedef %d",
                    linedef, i, unsigned(ldef.sidenum[side])
                );
            }

            li.sidedef = sides[ldef.sidenum[side]];

            /*
             * cph 2006/09/30 - our frontsector can be the second side of the
             * linedef, so must check for NO_INDEX in case we are incorrectly
             * referencing the back of a 1S line
             */
            if (ldef.sidenum[side] != NO_INDEX) {
                li.frontsector = sides[ldef.sidenum[side]].sector;
            } else {
                li.frontsector = null;
                System.err.printf("P_LoadZSegs: front of seg %i has no sidedef\n", i);
            }

            if (flags(ldef.flags, ML_TWOSIDED) && (ldef.sidenum[side ^ 1] != NO_INDEX)) {
                li.backsector = sides[ldef.sidenum[side ^ 1]].sector;
            } else {
                li.backsector = null;
            }

            li.v1 = vertexes[v1];
            li.v2 = vertexes[v2];

            li.length = GetDistance(li.v2.x - li.v1.x, li.v2.y - li.v1.y);
            li.offset = GetOffset(li.v1, (side != 0 ? ldef.v2 : ldef.v1));
            li.angle = RendererState.PointToAngle(segs[i].v1.x, segs[i].v1.y, segs[i].v2.x, segs[i].v2.y);
            // li.angle = (int)((float)atan2(li.v2.y - li.v1.y,li.v2.x -
            // li.v1.x) * (ANG180 / M_PI));
        }
    }

    private int CheckZNodesOverflow(int size, int count) {
        size -= count;

        if (size < 0) {
            DOOM.doomSystem.Error("P_LoadZNodes: incorrect nodes");
        }

        return size;
    }
    
    private void P_LoadZNodes(int lump, int glnodes) throws IOException {
        ByteBuffer data;
        int len;
        int header; // for debugging

        int orgVerts, newVerts;
        int numSubs, currSeg;
        int numSegs;
        int numNodes;
        vertex_t[] newvertarray = null;

        data = DOOM.wadLoader.CacheLumpNumAsDoomBuffer(lump).getBuffer();
        data.order(ByteOrder.LITTLE_ENDIAN);
        len = DOOM.wadLoader.LumpLength(lump);

        // skip header
        len = CheckZNodesOverflow(len, 4);
        header = data.getInt();

        // Read extra vertices added during node building
        len = CheckZNodesOverflow(len, 4);
        orgVerts = data.getInt();

        len = CheckZNodesOverflow(len, 4);
        newVerts = data.getInt();

        if (!samelevel) {
            if (orgVerts + newVerts == numvertexes) {
                newvertarray = vertexes;
            } else {
                newvertarray = new vertex_t[orgVerts + newVerts];
                // TODO: avoid creating new objects that will be rewritten instantly - Good Sign 2017/05/07
                Arrays.setAll(newvertarray, ii -> new vertex_t());
                System.arraycopy(vertexes, 0, newvertarray, 0, orgVerts);
            }

            //(sizeof(newvertarray[0].x) + sizeof(newvertarray[0].y))
            len = CheckZNodesOverflow(len, newVerts * vertex_t.sizeOf());
            z_vertex_t tmp = new z_vertex_t();

            for (int i = 0; i < newVerts; i++) {
                tmp.unpack(data);
                newvertarray[i + orgVerts].x = tmp.x;
                newvertarray[i + orgVerts].y = tmp.y;
            }

            // Extra vertexes read in
            if (vertexes != newvertarray) {
                for (int i = 0; i < numlines; i++) {
                    //lines[i].v1 = lines[i].v1 - vertexes + newvertarray;
                    //lines[i].v2 = lines[i].v2 - vertexes + newvertarray;
                    // Find indexes of v1 & v2 inside old vertexes array
                    // (.v1-vertexes) and use that index to re-point inside newvertarray              
                    lines[i].v1 = newvertarray[C2JUtils.indexOf(vertexes, lines[i].v1)];
                    lines[i].v2 = newvertarray[C2JUtils.indexOf(vertexes, lines[i].v2)];
                }
                // free(vertexes);
                vertexes = newvertarray;
                numvertexes = orgVerts + newVerts;
            }
        } else {
            // Skip the reading of all these new vertices and the expensive indexOf searches.
            int size = newVerts * z_vertex_t.sizeOf();
            len = CheckZNodesOverflow(len, size);
            data.position(data.position() + size);
        }

        // Read the subsectors
        len = CheckZNodesOverflow(len, 4);
        numSubs = data.getInt();

        numsubsectors = numSubs;
        if (numsubsectors <= 0) {
            DOOM.doomSystem.Error("P_LoadZNodes: no subsectors in level");
        }
        subsectors = calloc_IfSameLevel(subsectors, numsubsectors, subsector_t::new, subsector_t[]::new);

        len = CheckZNodesOverflow(len, numSubs * mapsubsector_znod_t.sizeOf());
        final mapsubsector_znod_t mseg = new mapsubsector_znod_t();
        for (int i = currSeg = 0; i < numSubs; i++) {
            mseg.unpack(data);

            subsectors[i].firstline = currSeg;
            subsectors[i].numlines = (int) mseg.numsegs;
            currSeg += mseg.numsegs;
        }

        // Read the segs
        len = CheckZNodesOverflow(len, 4);
        numSegs = data.getInt();

        // The number of segs stored should match the number of
        // segs used by subsectors.
        if (numSegs != currSeg) {
            DOOM.doomSystem.Error("P_LoadZNodes: Incorrect number of segs in nodes.");
        }

        numsegs = numSegs;
        segs = calloc_IfSameLevel(segs, numsegs, seg_t::new, seg_t[]::new);

        if (glnodes == 0) {
            len = CheckZNodesOverflow(len, numsegs * mapseg_znod_t.sizeOf());
            P_LoadZSegs(data);
        } else {
            //P_LoadGLZSegs (data, glnodes);
            DOOM.doomSystem.Error("P_LoadZNodes: GL segs are not supported.");
        }

        // Read nodes
        len = CheckZNodesOverflow(len, 4);
        numNodes = data.getInt();

        numnodes = numNodes;
        nodes = calloc_IfSameLevel(nodes, numNodes, node_t::new, node_t[]::new);

        len = CheckZNodesOverflow(len, numNodes * mapnode_znod_t.sizeOf());

        mapnode_znod_t znodes[] = malloc(mapnode_znod_t::new, mapnode_znod_t[]::new, numNodes);
        CacheableDoomObjectContainer.unpack(data, znodes);

        for (int i = 0; i < numNodes; i++) {
            int j, k;
            node_t no = nodes[i];
            final mapnode_znod_t mn = znodes[i];

            no.x = mn.x << FRACBITS;
            no.y = mn.y << FRACBITS;
            no.dx = mn.dx << FRACBITS;
            no.dy = mn.dy << FRACBITS;

            for (j = 0; j < 2; j++) {
                no.children[j] = mn.children[j];

                for (k = 0; k < 4; k++) {
                    no.bbox[j].bbox[k] = mn.bbox[j][k] << FRACBITS;
                }
            }
        }

        DOOM.wadLoader.UnlockLumpNum(lump); // cph - release the data
    }
    
    private boolean no_overlapped_sprites;

    private int GETXY(mobj_t mobj) {
        return (mobj.x + (mobj.y >> 16));
    }

    private int dicmp_sprite_by_pos(final Object a, final Object b) {
        mobj_t m1 = (mobj_t) a;
        mobj_t m2 = (mobj_t) b;

        int res = GETXY(m2) - GETXY(m1);
        no_overlapped_sprites = no_overlapped_sprites && (res != 0);
        return res;
    }

    /*
     * P_LoadThings killough 5/3/98: reformatted, cleaned up cph 2001/07/07 -
     * don't write into the lump cache, especially non-idepotent changes like
     * byte order reversals. Take a copy to edit.
     */

    @SourceCode.Suspicious(CauseOfDesyncProbability.LOW)
    @P_Setup.C(P_LoadThings)
    private void P_LoadThings(int lump) {
        int numthings = DOOM.wadLoader.LumpLength(lump) / mapthing_t.sizeOf();
        final mapthing_t[] data = DOOM.wadLoader.CacheLumpNumIntoArray(lump, numthings, mapthing_t::new, mapthing_t[]::new);

        mobj_t mobj;
        int mobjcount = 0;
        mobj_t[] mobjlist = new mobj_t[numthings];
        Arrays.setAll(mobjlist, j -> mobj_t.createOn(DOOM));

        if ((data == null) || (numthings == 0)) {
            DOOM.doomSystem.Error("P_LoadThings: no things in level");
        }

        for (int i = 0; i < numthings; i++) {
            mapthing_t mt = data[i];

            /*
             * Not needed. Handled during unmarshaling. mt.x =
             * LittleShort(mt.x); mt.y = LittleShort(mt.y); mt.angle =
             * LittleShort(mt.angle); mt.type = LittleShort(mt.type); mt.options
             * = LittleShort(mt.options);
             */

            if (!P_IsDoomnumAllowed(mt.type)) {
                continue;
            }

            // Do spawn all other stuff.
            mobj = DOOM.actions.SpawnMapThing(mt/* , i */);
            if (mobj != null && mobj.info.speed == 0) {
                mobjlist[mobjcount++] = mobj;
            }
        }

        DOOM.wadLoader.UnlockLumpNum(lump); // cph - release the data
        /*
         * #ifdef GL_DOOM if (V_GetMode() == VID_MODEGL) { no_overlapped_sprites
         * = true; qsort(mobjlist, mobjcount, sizeof(mobjlist[0]),
         * dicmp_sprite_by_pos); if (!no_overlapped_sprites) { i = 1; while (i <
         * mobjcount) { mobj_t *m1 = mobjlist[i - 1]; mobj_t *m2 = mobjlist[i -
         * 0]; if (GETXY(m1) == GETXY(m2)) { mobj_t *mo = (m1.index < m2.index ?
         * m1 : m2); i++; while (i < mobjcount && GETXY(mobjlist[i]) ==
         * GETXY(m1)) { if (mobjlist[i].index < mo.index) { mo = mobjlist[i]; }
         * i++; } // 'nearest' mo.flags |= MF_FOREGROUND; } i++; } } } #endif
         */

    }

    /*
     * P_IsDoomnumAllowed() Based on code taken from P_LoadThings() in
     * src/p_setup.c Return TRUE if the thing in question is expected to be
     * available in the gamemode used.
     */

    boolean P_IsDoomnumAllowed(int doomnum) {
        // Do not spawn cool, new monsters if !commercial
        if (!DOOM.isCommercial())
            switch (doomnum) {
            case 64: // Archvile
            case 65: // Former Human Commando
            case 66: // Revenant
            case 67: // Mancubus
            case 68: // Arachnotron
            case 69: // Hell Knight
            case 71: // Pain Elemental
            case 84: // Wolf SS
            case 88: // Boss Brain
            case 89: // Boss Shooter
                return false;
            }

        return true;
    }

    //
    // P_LoadLineDefs
    // Also counts secret lines for intermissions.
    // ^^^
    // ??? killough ???
    // Does this mean secrets used to be linedef-based, rather than
    // sector-based?
    //
    // killough 4/4/98: split into two functions, to allow sidedef overloading
    //
    // killough 5/3/98: reformatted, cleaned up

    private void P_LoadLineDefs(int lump) {
        final maplinedef_t[] data; // cph - final*

        numlines = DOOM.wadLoader.LumpLength(lump) / maplinedef_t.sizeOf();
        lines = calloc_IfSameLevel(lines, numlines, line_t::new, line_t[]::new);
        data = DOOM.wadLoader.CacheLumpNumIntoArray(lump, numlines, maplinedef_t::new, maplinedef_t[]::new); // cph
                                                                            // -
                                                                            // wad
                                                                            // lump
                                                                            // handling
                                                                            // updated

        for (int i = 0; i < numlines; i++) {
            final maplinedef_t mld = data[i];
            line_t ld = lines[i];
            ld.id = i;
            vertex_t v1, v2;

            ld.flags = mld.flags;
            ld.special = mld.special;
            ld.tag = mld.tag;
            v1 = ld.v1 = vertexes[mld.v1];
            v2 = ld.v2 = vertexes[mld.v2];
            ld.dx = v2.x - v1.x;
            ld.dy = v2.y - v1.y;
            // Maes: map value semantics.
            ld.assignVertexValues();

            /*
             * #ifdef GL_DOOM // e6y // Rounding the wall length to the nearest
             * integer // when determining length instead of always rounding
             * down // There is no more glitches on seams between identical
             * textures. ld.texel_length = GetTexelDistance(ld.dx, ld.dy);
             * #endif
             */
            ld.tranlump = -1; // killough 4/11/98: no translucency by default

            ld.slopetype = (ld.dx == 0)
                ? slopetype_t.ST_VERTICAL
                : (ld.dy == 0)
                    ? slopetype_t.ST_HORIZONTAL
                        : fixed_t.FixedDiv(ld.dy, ld.dx) > 0
                            ? slopetype_t.ST_POSITIVE
                            : slopetype_t.ST_NEGATIVE;

            if (v1.x < v2.x) {
                ld.bbox[BBox.BOXLEFT] = v1.x;
                ld.bbox[BBox.BOXRIGHT] = v2.x;
            } else {
                ld.bbox[BBox.BOXLEFT] = v2.x;
                ld.bbox[BBox.BOXRIGHT] = v1.x;
            }
            if (v1.y < v2.y) {
                ld.bbox[BBox.BOXBOTTOM] = v1.y;
                ld.bbox[BBox.BOXTOP] = v2.y;
            } else {
                ld.bbox[BBox.BOXBOTTOM] = v2.y;
                ld.bbox[BBox.BOXTOP] = v1.y;
            }

            /* calculate sound origin of line to be its midpoint */
            // e6y: fix sound origin for large levels
            // no need for comp_sound test, these are only used when comp_sound
            // = 0
            ld.soundorg = new degenmobj_t(
                ld.bbox[BBox.BOXLEFT] / 2
                + ld.bbox[BBox.BOXRIGHT] / 2, ld.bbox[BBox.BOXTOP] / 2
                + ld.bbox[BBox.BOXBOTTOM] / 2, 0
            );

            // TODO
            // ld.iLineID=i; // proff 04/05/2000: needed for OpenGL
            ld.sidenum[0] = mld.sidenum[0];
            ld.sidenum[1] = mld.sidenum[1];

            {
                /*
                 * cph 2006/09/30 - fix sidedef errors right away. cph
                 * 2002/07/20 - these errors are fatal if not fixed, so apply
                 * them in compatibility mode - a desync is better than a crash!
                 */
                for (int j = 0; j < 2; j++) {
                    if (ld.sidenum[j] != NO_INDEX && ld.sidenum[j] >= numsides) {
                        ld.sidenum[j] = NO_INDEX;
                        System.err.printf(
                            "P_LoadLineDefs: linedef %d has out-of-range sidedef number\n",
                            numlines - i - 1
                        );
                    }
                }

                // killough 11/98: fix common wad errors (missing sidedefs):
                if (ld.sidenum[0] == NO_INDEX) {
                    ld.sidenum[0] = 0; // Substitute dummy sidedef for missing
                    // right side
                    // cph - print a warning about the bug
                    System.err.printf("P_LoadLineDefs: linedef %d missing first sidedef\n", numlines - i - 1);
                }

                if ((ld.sidenum[1] == NO_INDEX) && flags(ld.flags, ML_TWOSIDED)) {
                    // e6y
                    // ML_TWOSIDED flag shouldn't be cleared for compatibility
                    // purposes
                    // see CLNJ-506.LMP at http://doomedsda.us/wad1005.html
                    // TODO: we don't really care, but still...
                    // if (!demo_compatibility ||
                    // !overflows[OVERFLOW.MISSEDBACKSIDE].emulate)
                    // {
                    ld.flags &= ~ML_TWOSIDED; // Clear 2s flag for missing left
                    // side
                    // }
                    // Mark such lines and do not draw them only in
                    // demo_compatibility,
                    // because Boom's behaviour is different
                    // See OTTAWAU.WAD E1M1, sectors 226 and 300
                    // http://www.doomworld.com/idgames/index.php?id=1651
                    // TODO ehhh?
                    // ld.r_flags = RF_IGNORE_COMPAT;
                    // cph - print a warning about the bug
                    System.err.printf(
                        "P_LoadLineDefs: linedef %d has two-sided flag set, but no second sidedef\n",
                        numlines - i - 1
                    );
                }
            }

            // killough 4/4/98: support special sidedef interpretation below
            // TODO:
            // if (ld.sidenum[0] != NO_INDEX && ld.special!=0)
            // sides[(ld.sidenum[0]<<16)& (0x0000FFFF&ld.sidenum[1])].special =
            // ld.special;
        }

        DOOM.wadLoader.UnlockLumpNum(lump); // cph - release the lump
    }

    // killough 4/4/98: delay using sidedefs until they are loaded
    // killough 5/3/98: reformatted, cleaned up

    private void P_LoadLineDefs2(int lump) {
        line_t ld;

        for (int i = 0; i < numlines; i++) {
            ld = lines[i];
            ld.frontsector = sides[ld.sidenum[0]].sector; // e6y: Can't be
                                                          // NO_INDEX here
            ld.backsector =
                ld.sidenum[1] != NO_INDEX ? sides[ld.sidenum[1]].sector : null;
            switch (ld.special) { // killough 4/11/98: handle special types
            case 260: // killough 4/11/98: translucent 2s textures
                // TODO: transparentpresent = true;//e6y
                // int lmp = sides[ld.getSpecialSidenum()].special; //
                // translucency from sidedef
                // if (!ld.tag) // if tag==0,
                // ld.tranlump = lmp; // affect this linedef only
                // else
                // for (int j=0;j<numlines;j++) // if tag!=0,
                // if (lines[j].tag == ld.tag) // affect all matching linedefs
                // lines[j].tranlump = lump;
                // break;
            }
        }
    }

    //
    // P_LoadSideDefs
    //
    // killough 4/4/98: split into two functions

    private void P_LoadSideDefs(int lump) {
        numsides = DOOM.wadLoader.LumpLength(lump) / mapsidedef_t.sizeOf();
        sides = calloc_IfSameLevel(sides, numsides, side_t::new, side_t[]::new);
    }

    // killough 4/4/98: delay using texture names until
    // after linedefs are loaded, to allow overloading.
    // killough 5/3/98: reformatted, cleaned up

    private void P_LoadSideDefs2(int lump) {
        // cph - final*, wad lump handling updated
        final mapsidedef_t[] data = DOOM.wadLoader.CacheLumpNumIntoArray(lump, numsides, mapsidedef_t::new, mapsidedef_t[]::new);
        
        for (int i = 0; i < numsides; i++) {
            final mapsidedef_t msd = data[i];
            side_t sd = sides[i];
            sector_t sec;

            sd.textureoffset = msd.textureoffset << FRACBITS;
            sd.rowoffset = msd.rowoffset << FRACBITS;

            { /*
               * cph 2006/09/30 - catch out-of-range sector numbers; use sector
               * 0 instead
               */
                char sector_num = (char) msd.sector;
                if (sector_num >= numsectors) {
                    System.err.printf("P_LoadSideDefs2: sidedef %i has out-of-range sector num %u\n", i, sector_num);
                    sector_num = 0;
                }
                sd.sector = sec = sectors[sector_num];
            }

            // killough 4/4/98: allow sidedef texture names to be overloaded
            // killough 4/11/98: refined to allow colormaps to work as wall
            // textures if invalid as colormaps but valid as textures.
            switch (sd.special) {
            case 242: // variable colormap via 242 linedef
                /*
                 * sd.bottomtexture = (sec.bottommap =
                 * R.ColormapNumForName(msd.bottomtexture)) < 0 ? sec.bottommap
                 * = 0, R.TextureNumForName(msd.bottomtexture): 0 ;
                 * sd.midtexture = (sec.midmap =
                 * R.ColormapNumForName(msd.midtexture)) < 0 ? sec.midmap = 0,
                 * R.TextureNumForName(msd.midtexture) : 0 ; sd.toptexture =
                 * (sec.topmap = R.ColormapNumForName(msd.toptexture)) < 0 ?
                 * sec.topmap = 0, R.TextureNumForName(msd.toptexture) : 0 ;
                 */

                break;

            case 260: // killough 4/11/98: apply translucency to 2s normal texture
                if (msd.midtexture.compareToIgnoreCase("TRANMAP") == 0) {
                    if ((sd.special = DOOM.wadLoader.CheckNumForName(msd.midtexture)) < 0
                    || DOOM.wadLoader.LumpLength(sd.special) != 65536)
                    {
                        sd.special = 0;
                        sd.midtexture = (short) DOOM.textureManager.TextureNumForName(msd.midtexture);
                    } else {
                        sd.special++;
                        sd.midtexture = 0;
                    }
                } else {
                    sd.midtexture = (short) (sd.special = 0);
                }
                sd.toptexture = (short) DOOM.textureManager.TextureNumForName(msd.toptexture);
                sd.bottomtexture = (short) DOOM.textureManager.TextureNumForName(msd.bottomtexture);
                break;

            /*
             * #ifdef GL_DOOM case 271: case 272: if
             * (R_CheckTextureNumForName(msd.toptexture) == -1) {
             * sd.skybox_index = R_BoxSkyboxNumForName(msd.toptexture); } #endif
             */

            default: // normal cases
                // TODO: Boom uses "SafeTextureNumForName" here. Find out what
                // it does.
                sd.midtexture = (short) DOOM.textureManager.CheckTextureNumForName(msd.midtexture);
                sd.toptexture = (short) DOOM.textureManager.CheckTextureNumForName(msd.toptexture);
                sd.bottomtexture = (short) DOOM.textureManager.CheckTextureNumForName(msd.bottomtexture);
                break;
            }
        }

        DOOM.wadLoader.UnlockLumpNum(lump); // cph - release the lump
    }

    //
    // P_LoadBlockMap
    //
    // killough 3/1/98: substantially modified to work
    // towards removing blockmap limit (a wad limitation)
    //
    // killough 3/30/98: Rewritten to remove blockmap limit,
    // though current algorithm is brute-force and unoptimal.
    //
    private void P_LoadBlockMap(int lump) throws IOException {
        int count = 0;

        if (DOOM.cVarManager.bool(CommandVariable.BLOCKMAP)
        || DOOM.wadLoader.LumpLength(lump) < 8
        || (count = DOOM.wadLoader.LumpLength(lump) / 2) >= 0x10000) // e6y
        {
            CreateBlockMap();
        } else {
            // cph - final*, wad lump handling updated
            final char[] wadblockmaplump;

            DoomBuffer data = DOOM.wadLoader.CacheLumpNum(lump, PU_LEVEL, DoomBuffer.class);
            count = DOOM.wadLoader.LumpLength(lump) / 2;
            wadblockmaplump = new char[count];

            data.setOrder(ByteOrder.LITTLE_ENDIAN);
            data.rewind();
            data.readCharArray(wadblockmaplump, count);

            if (!samelevel) // Reallocate if required.
                blockmaplump = new int[count];

            // killough 3/1/98: Expand wad blockmap into larger internal one,
            // by treating all offsets except -1 as unsigned and zero-extending
            // them. This potentially doubles the size of blockmaps allowed,
            // because Doom originally considered the offsets as always signed.

            blockmaplump[0] = wadblockmaplump[0];
            blockmaplump[1] = wadblockmaplump[1];
            blockmaplump[2] = wadblockmaplump[2] & 0xffff;
            blockmaplump[3] = wadblockmaplump[3] & 0xffff;

            for (int i = 4; i < count; i++) {
                short t = (short) wadblockmaplump[i]; // killough 3/1/98
                blockmaplump[i] = (int) (t == -1 ? -1l : t & 0xffff);
            }

            DOOM.wadLoader.UnlockLumpNum(lump); // cph - unlock the lump

            bmaporgx = blockmaplump[0] << FRACBITS;
            bmaporgy = blockmaplump[1] << FRACBITS;
            bmapwidth = blockmaplump[2];
            bmapheight = blockmaplump[3];

            // haleyjd 03/04/10: check for blockmap problems
            // http://www.doomworld.com/idgames/index.php?id=12935
            if (!VerifyBlockMap(count)) {
                System.err.printf("P_LoadBlockMap: erroneous BLOCKMAP lump may cause crashes.\n");
                System.err.printf("P_LoadBlockMap: use \"-blockmap\" command line switch for rebuilding\n");
            }
        }

        // MAES: blockmap was generated, rather than loaded.
        if (count == 0) {
            count = blockmaplump.length - 4;
        }

        // clear out mobj chains - CPhipps - use calloc
        // blocklinks = calloc_IfSameLevel(blocklinks, bmapwidth *
        // bmapheight.mobj_t.);
        // clear out mobj chains
        // ATTENTION! BUG!!!
        // If blocklinks are "cleared" to void -but instantiated- objects,
        // very bad bugs happen, especially the second time a level is
        // re-instantiated.
        // Probably caused other bugs as well, as an extra object would appear
        // in iterators.

        if (blocklinks != null && samelevel) {
            for (int i = 0; i < bmapwidth * bmapheight; i++) {
                blocklinks[i] = null;
            }
        } else {
            blocklinks = new mobj_t[bmapwidth * bmapheight];
        }

        // IMPORTANT MODIFICATION: no need to have both blockmaplump AND
        // blockmap.
        // If the offsets in the lump are OK, then we can modify them (remove 4)
        // and copy the rest of the data in one single data array. This avoids
        // reserving memory for two arrays (we can't simply alias one in Java)

        blockmap = new int[blockmaplump.length - 4];
        count = bmapwidth * bmapheight;
        // Offsets are relative to START OF BLOCKMAP, and IN SHORTS, not bytes.
        for (int i = 0; i < blockmaplump.length - 4; i++) {
            // Modify indexes so that we don't need two different lumps.
            // Can probably be further optimized if we simply shift everything
            // backwards.
            // and reuse the same memory space.
            if (i < count) {
                blockmaplump[i] = blockmaplump[i + 4] - 4;
            } else {
                blockmaplump[i] = blockmaplump[i + 4];
            }
        }

        
        // MAES: set blockmapxneg and blockmapyneg
        // E.g. for a full 512x512 map, they should be both
        // -1. For a 257*257, they should be both -255 etc.
        if (bmapwidth > 255) {
            blockmapxneg = bmapwidth - 512;
        }
        if (bmapheight > 255) {
            blockmapyneg = bmapheight - 512;
        }
        
        blockmap = blockmaplump;

    }

    //
    // P_LoadReject - load the reject table
    //

    private void P_LoadReject(int lumpnum, int totallines) {
        // dump any old cached reject lump, then cache the new one
        if (rejectlump != -1) {
            DOOM.wadLoader.UnlockLumpNum(rejectlump);
        }
        rejectlump = lumpnum + ML_REJECT;
        rejectmatrix = DOOM.wadLoader.CacheLumpNumAsRawBytes(rejectlump, 0);

        // e6y: check for overflow
        // TODO: g.Overflow.RejectOverrun(rejectlump, rejectmatrix,
        // totallines,numsectors);
    }

    //
    // P_GroupLines
    // Builds sector line lists and subsector sector numbers.
    // Finds block bounding boxes for sectors.
    //
    // killough 5/3/98: reformatted, cleaned up
    // cph 18/8/99: rewritten to avoid O(numlines * numsectors) section
    // It makes things more complicated, but saves seconds on big levels
    // figgi 09/18/00 -- adapted for gl-nodes

    
    // modified to return totallines (needed by P_LoadReject)
    private int P_GroupLines() {
        line_t li;
        sector_t sector;
        int total = numlines;

        // figgi
        for (int i = 0; i < numsubsectors; i++) {
            int seg = subsectors[i].firstline;
            subsectors[i].sector = null;
            for (int j = 0; j < subsectors[i].numlines; j++) {
                if (segs[seg].sidedef != null) {
                    subsectors[i].sector = segs[seg].sidedef.sector;
                    break;
                }
                seg++;
            }
            if (subsectors[i].sector == null) {
                DOOM.doomSystem.Error("P_GroupLines: Subsector a part of no sector!\n");
            }
        }

        // count number of lines in each sector
        for (int i = 0; i < numlines; i++) {
            li = lines[i];
            li.frontsector.linecount++;
            if (li.backsector != null && (li.backsector != li.frontsector)) {
                li.backsector.linecount++;
                total++;
            }
        }

        // allocate line tables for each sector
        // e6y: REJECT overrun emulation code
        // moved to P_LoadReject
        for (int i = 0; i < numsectors; i++) {
            sector = sectors[i];
            sector.lines = malloc(line_t::new, line_t[]::new, sector.linecount);
            // linebuffer += sector.linecount;
            sector.linecount = 0;
            BBox.ClearBox(sector.blockbox);
        }

        // Enter those lines
        for (int i = 0; i < numlines; i++) {
            li = lines[i];
            AddLineToSector(li, li.frontsector);
            if (li.backsector != null && li.backsector != li.frontsector) {
                AddLineToSector(li, li.backsector);
            }
        }

        for (int i = 0; i < numsectors; i++) {
            sector = sectors[i];
            int[] bbox = sector.blockbox; // cph - For convenience, so
            // I can sue the old code unchanged
            int block;

            // set the degenmobj_t to the middle of the bounding box
            // TODO
            if (true/* comp[comp_sound] */) {
                sector.soundorg = new degenmobj_t((bbox[BOXRIGHT] + bbox[BOXLEFT]) / 2, (bbox[BOXTOP] + bbox[BOXBOTTOM]) / 2);
            } else {
                // e6y: fix sound origin for large levels
                sector.soundorg = new degenmobj_t((bbox[BOXRIGHT] / 2 + bbox[BOXLEFT] / 2), bbox[BOXTOP] / 2 + bbox[BOXBOTTOM] / 2);
            }

            // adjust bounding box to map blocks
            block = getSafeBlockY(bbox[BOXTOP] - bmaporgy + Limits.MAXRADIUS);
            block = block >= bmapheight ? bmapheight - 1 : block;
            sector.blockbox[BOXTOP] = block;

            block = getSafeBlockY(bbox[BOXBOTTOM] - bmaporgy - Limits.MAXRADIUS);
            block = block < 0 ? 0 : block;
            sector.blockbox[BOXBOTTOM] = block;

            block = getSafeBlockX(bbox[BOXRIGHT] - bmaporgx + Limits.MAXRADIUS);
            block = block >= bmapwidth ? bmapwidth - 1 : block;
            sector.blockbox[BOXRIGHT] = block;

            block = getSafeBlockX(bbox[BOXLEFT] - bmaporgx - Limits.MAXRADIUS);
            block = block < 0 ? 0 : block;
            sector.blockbox[BOXLEFT] = block;
        }

        return total; // this value is needed by the reject overrun emulation
        // code
    }

    //
    // killough 10/98
    //
    // Remove slime trails.
    //
    // Slime trails are inherent to Doom's coordinate system -- i.e. there is
    // nothing that a node builder can do to prevent slime trails ALL of the
    // time,
    // because it's a product of the integer coodinate system, and just because
    // two lines pass through exact integer coordinates, doesn't necessarily
    // mean
    // that they will intersect at integer coordinates. Thus we must allow for
    // fractional coordinates if we are to be able to split segs with node
    // lines,
    // as a node builder must do when creating a BSP tree.
    //
    // A wad file does not allow fractional coordinates, so node builders are
    // out
    // of luck except that they can try to limit the number of splits (they
    // might
    // also be able to detect the degree of roundoff error and try to avoid
    // splits
    // with a high degree of roundoff error). But we can use fractional
    // coordinates
    // here, inside the engine. It's like the difference between square inches
    // and
    // square miles, in terms of granularity.
    //
    // For each vertex of every seg, check to see whether it's also a vertex of
    // the linedef associated with the seg (i.e, it's an endpoint). If it's not
    // an endpoint, and it wasn't already moved, move the vertex towards the
    // linedef by projecting it using the law of cosines. Formula:
    //
    // 2 2 2 2
    // dx x0 + dy x1 + dx dy (y0 - y1) dy y0 + dx y1 + dx dy (x0 - x1)
    // {---------------------------------, ---------------------------------}
    // 2 2 2 2
    // dx + dy dx + dy
    //
    // (x0,y0) is the vertex being moved, and (x1,y1)-(x1+dx,y1+dy) is the
    // reference linedef.
    //
    // Segs corresponding to orthogonal linedefs (exactly vertical or horizontal
    // linedefs), which comprise at least half of all linedefs in most wads,
    // don't
    // need to be considered, because they almost never contribute to slime
    // trails
    // (because then any roundoff error is parallel to the linedef, which
    // doesn't
    // cause slime). Skipping simple orthogonal lines lets the code finish
    // quicker.
    //
    // Please note: This section of code is not interchangable with TeamTNT's
    // code which attempts to fix the same problem.
    //
    // Firelines (TM) is a Rezistered Trademark of MBF Productions
    //

    private void P_RemoveSlimeTrails() { // killough 10/98
        // Hitlist for vertices
        boolean[] hit = new boolean[numvertexes];

        // Searchlist for

        for (int i = 0; i < numsegs; i++) { // Go through each seg
            final line_t l;

            if (segs[i].miniseg == true) { // figgi -- skip minisegs
                return;
            }

            l = segs[i].linedef; // The parent linedef
            if (l.dx != 0 && l.dy != 0) { // We can ignore orthogonal lines
                vertex_t v = segs[i].v1;
                do {
                    int index = C2JUtils.indexOf(vertexes, v);
                    if (!hit[index]) { // If we haven't processed vertex
                        hit[index] = true; // Mark this vertex as processed
                        if (v != l.v1 && v != l.v2) { // Exclude endpoints of linedefs
                            // Project the vertex back onto the parent linedef
                            long dx2 = (l.dx >> FRACBITS) * (l.dx >> FRACBITS);
                            long dy2 = (l.dy >> FRACBITS) * (l.dy >> FRACBITS);
                            long dxy = (l.dx >> FRACBITS) * (l.dy >> FRACBITS);
                            long s = dx2 + dy2;
                            int x0 = v.x, y0 = v.y, x1 = l.v1.x, y1 = l.v1.y;
                            v.x = (int) ((dx2 * x0 + dy2 * x1 + dxy * (y0 - y1)) / s);
                            v.y = (int) ((dy2 * y0 + dx2 * y1 + dxy * (x0 - x1)) / s);
                        }
                    } // Obsfucated C contest entry: :)
                } while ((v != segs[i].v2) && ((v = segs[i].v2) != null));
            }
            // Assign modified vertex values.
            l.assignVertexValues();
        }
    }

    //
    // P_CheckLumpsForSameSource
    //
    // Are these lumps in the same wad file?
    //

    boolean P_CheckLumpsForSameSource(int lump1, int lump2) {
        int wad1_index, wad2_index;
        wadfile_info_t wad1, wad2;

        if ((unsigned(lump1) >= unsigned(DOOM.wadLoader.NumLumps()))
        || (unsigned(lump2) >= unsigned(DOOM.wadLoader.NumLumps())))
        {
            return false;
        }

        wad1 = DOOM.wadLoader.GetLumpInfo(lump1).wadfile;
        wad2 = DOOM.wadLoader.GetLumpInfo(lump2).wadfile;

        if (wad1 == null || wad2 == null) {
            return false;
        }

        wad1_index = DOOM.wadLoader.GetWadfileIndex(wad1);
        wad2_index = DOOM.wadLoader.GetWadfileIndex(wad2);

        if (wad1_index != wad2_index) {
            return false;
        }

        if ((wad1_index < 0) || (wad1_index >= DOOM.wadLoader.GetNumWadfiles())) {
            return false;
        }

        return !((wad2_index < 0) || (wad2_index >= DOOM.wadLoader.GetNumWadfiles()));
    }

    private static final String[] ml_labels = {
        "ML_LABEL", // A separator, name, ExMx or MAPxx
        "ML_THINGS", // Monsters, items..
        "ML_LINEDEFS", // LineDefs, from editing
        "ML_SIDEDEFS", // SideDefs, from editing
        "ML_VERTEXES", // Vertices, edited and BSP splits generated
        "ML_SEGS", // LineSegs, from LineDefs split by BSP
        "ML_SSECTORS", // SubSectors, list of LineSegs
        "ML_NODES", // BSP nodes
        "ML_SECTORS", // Sectors, from editing
        "ML_REJECT", // LUT, sector-sector visibility
        "ML_BLOCKMAP", // LUT, motion clipping, walls/grid element
    };

    private static final boolean GL_DOOM = false;

    //
    // P_CheckLevelFormat
    //
    // Checking for presence of necessary lumps
    //
    void P_CheckLevelWadStructure(final String mapname) {
        int i, lumpnum;

        if (mapname == null) {
            DOOM.doomSystem.Error("P_SetupLevel: Wrong map name");
            throw new NullPointerException();
        }

        lumpnum = DOOM.wadLoader.CheckNumForName(mapname.toUpperCase());

        if (lumpnum < 0) {
            DOOM.doomSystem.Error("P_SetupLevel: There is no %s map.", mapname);
        }

        for (i = ML_THINGS + 1; i <= ML_SECTORS; i++) {
            if (!P_CheckLumpsForSameSource(lumpnum, lumpnum + i)) {
                DOOM.doomSystem.Error(
                    "P_SetupLevel: Level wad structure is incomplete. There is no %s lump. (%s)",
                    ml_labels[i], DOOM.wadLoader.GetNameForLump(lumpnum));
            }
        }

        // refuse to load Hexen-format maps, avoid segfaults
        i = lumpnum + ML_BLOCKMAP + 1;
        if (P_CheckLumpsForSameSource(lumpnum, i)) {
            if (DOOM.wadLoader.GetLumpInfo(i).name.compareToIgnoreCase("BEHAVIOR") == 0) {
                DOOM.doomSystem.Error("P_SetupLevel: %s: Hexen format not supported", mapname);
            }
        }
    }

    //
    // P_SetupLevel
    //
    // killough 5/3/98: reformatted, cleaned up

    @Override
    @SourceCode.Suspicious(CauseOfDesyncProbability.LOW)
    @P_Setup.C(P_SetupLevel)
    public void SetupLevel(int episode, int map, int playermask, skill_t skill) throws IOException {
        String lumpname;
        int lumpnum;

        String gl_lumpname;
        int gl_lumpnum;

        // e6y
        DOOM.totallive = 0;
        // TODO: transparentpresent = false;

        // R_StopAllInterpolations();

        DOOM.totallive = DOOM.totalkills = DOOM.totalitems = DOOM.totalsecret = DOOM.wminfo.maxfrags = 0;
        DOOM.wminfo.partime = 180;

        for (int i = 0; i < Limits.MAXPLAYERS; i++) {
            DOOM.players[i].killcount = DOOM.players[i].secretcount = DOOM.players[i].itemcount = 0;
            // TODO DM.players[i].resurectedkillcount = 0;//e6y
        }

        // Initial height of PointOfView
        // will be set by player think.
        DOOM.players[DOOM.consoleplayer].viewz = 1;

        // Make sure all sounds are stopped before Z_FreeTags.
        S_Start: {
            DOOM.doomSound.Start();
        }

        Z_FreeTags:; // Z_FreeTags(PU_LEVEL, PU_PURGELEVEL-1);
        
        if (rejectlump != -1) { // cph - unlock the reject table
            DOOM.wadLoader.UnlockLumpNum(rejectlump);
            rejectlump = -1;
        }

        P_InitThinkers: {
            DOOM.actions.InitThinkers();
        }

        // if working with a devlopment map, reload it
        W_Reload:; // killough 1/31/98: W.Reload obsolete

        // find map name
        if (DOOM.isCommercial()) {
            lumpname = String.format("map%02d", map); // killough 1/24/98:
                                                      // simplify
            gl_lumpname = String.format("gl_map%02d", map); // figgi
        } else {
            lumpname = String.format("E%dM%d", episode, map); // killough
                                                              // 1/24/98:
                                                              // simplify
            gl_lumpname = String.format("GL_E%dM%d", episode, map); // figgi
        }

        W_GetNumForName: {
            lumpnum = DOOM.wadLoader.GetNumForName(lumpname);
            gl_lumpnum = DOOM.wadLoader.CheckNumForName(gl_lumpname); // figgi
        }

        // e6y
        // Refuse to load a map with incomplete pwad structure.
        // Avoid segfaults on levels without nodes.
        P_CheckLevelWadStructure(lumpname);

        DOOM.leveltime = 0;
        DOOM.totallive = 0;

        // note: most of this ordering is important

        // killough 3/1/98: P_LoadBlockMap call moved down to below
        // killough 4/4/98: split load of sidedefs into two parts,
        // to allow texture names to be used in special linedefs

        // figgi 10/19/00 -- check for gl lumps and load them
        P_GetNodesVersion(lumpnum, gl_lumpnum);

        // e6y: speedup of level reloading
        // Most of level's structures now are allocated with PU_STATIC instead
        // of PU_LEVEL
        // It is important for OpenGL, because in case of the same data in
        // memory
        // we can skip recalculation of much stuff

        samelevel = (map == current_map) && (episode == current_episode) && (nodesVersion == current_nodesVersion);

        current_episode = episode;
        current_map = map;
        current_nodesVersion = nodesVersion;

        if (!samelevel) {

            /*
             * if (GL_DOOM){ // proff 11/99: clean the memory from textures etc.
             * gld_CleanMemory(); }
             */

            // free(segs);
            // free(nodes);
            // free(subsectors);
            /*
             * #ifdef GL_DOOM free(map_subsectors); #endif
             */

            // free(blocklinks);
            // free(blockmaplump);

            // free(lines);
            // free(sides);
            // free(sectors);
            // free(vertexes);
        }

        if (nodesVersion > 0) {
            this.P_LoadVertexes2(lumpnum + ML_VERTEXES, gl_lumpnum + ML_GL_VERTS);
        } else {
            P_LoadVertexes(lumpnum + ML_VERTEXES);
        }
        
        P_LoadSectors(lumpnum + ML_SECTORS);
        P_LoadSideDefs(lumpnum + ML_SIDEDEFS);
        P_LoadLineDefs(lumpnum + ML_LINEDEFS);
        P_LoadSideDefs2(lumpnum + ML_SIDEDEFS);
        P_LoadLineDefs2(lumpnum + ML_LINEDEFS);

        // e6y: speedup of level reloading
        // Do not reload BlockMap for same level,
        // because in case of big level P_CreateBlockMap eats much time
        if (!samelevel) {
            P_LoadBlockMap(lumpnum + ML_BLOCKMAP);
        } else {
            // clear out mobj chains
            if (blocklinks != null && blocklinks.length == bmapwidth * bmapheight) {
                for (int i = 0; i < bmapwidth * bmapheight; i++) {
                    blocklinks[i] = null;
                }
            } else {
                blocklinks = new mobj_t[bmapwidth * bmapheight];
                Arrays.setAll(blocklinks, i -> mobj_t.createOn(DOOM));
            }
        }

        if (nodesVersion > 0) {
            P_LoadSubsectors(gl_lumpnum + ML_GL_SSECT);
            P_LoadNodes(gl_lumpnum + ML_GL_NODES);
            // TODO: P_LoadGLSegs(gl_lumpnum + ML_GL_SEGS);
        } else {
            if (P_CheckForZDoomUncompressedNodes(lumpnum, gl_lumpnum)) {
                P_LoadZNodes(lumpnum + ML_NODES, 0);
            } else if (P_CheckForDeePBSPv4Nodes(lumpnum, gl_lumpnum)) {
                P_LoadSubsectors_V4(lumpnum + ML_SSECTORS);
                P_LoadNodes_V4(lumpnum + ML_NODES);
                P_LoadSegs_V4(lumpnum + ML_SEGS);
            } else {
                P_LoadSubsectors(lumpnum + ML_SSECTORS);
                P_LoadNodes(lumpnum + ML_NODES);
                P_LoadSegs(lumpnum + ML_SEGS);
            }
        }

        /*
         * if (GL_DOOM){ map_subsectors = calloc_IfSameLevel(map_subsectors,
         * numsubsectors); }
         */

        // reject loading and underflow padding separated out into new function
        // P_GroupLines modified to return a number the underflow padding needs
        // P_LoadReject(lumpnum, P_GroupLines());
        P_GroupLines();
        super.LoadReject(lumpnum+ML_REJECT);

        /**
         * TODO: try to fix, since it seems it doesn't work
         *  - Good Sign 2017/05/07
         */
        
        // e6y
        // Correction of desync on dv04-423.lmp/dv.wad
        // http://www.doomworld.com/vb/showthread.php?s=&postid=627257#post627257
        // if (DoomStatus.compatibility_level>=lxdoom_1_compatibility ||
        // Compatibility.prboom_comp[PC.PC_REMOVE_SLIME_TRAILS.ordinal()].state)
        P_RemoveSlimeTrails(); // killough 10/98: remove slime trails from wad

        // Note: you don't need to clear player queue slots --
        // a much simpler fix is in g_game.c -- killough 10/98

        DOOM.bodyqueslot = 0;

        /* cph - reset all multiplayer starts */

        for (int i = 0; i < playerstarts.length; i++) {
            DOOM.playerstarts[i] = null;
        }

        deathmatch_p = 0;

        for (int i = 0; i < Limits.MAXPLAYERS; i++) {
            DOOM.players[i].mo = null;
        }
        // TODO: TracerClearStarts();

        // Hmm? P_MapStart();

        P_LoadThings: {
            P_LoadThings(lumpnum + ML_THINGS);
        }

        // if deathmatch, randomly spawn the active players
        if (DOOM.deathmatch) {
            for (int i = 0; i < Limits.MAXPLAYERS; i++) {
                if (DOOM.playeringame[i]) {
                    DOOM.players[i].mo = null; // not needed? - done before P_LoadThings
                    G_DeathMatchSpawnPlayer: {
                        DOOM.DeathMatchSpawnPlayer(i);
                    }
                }
            }
        } else { // if !deathmatch, check all necessary player starts actually exist
            for (int i = 0; i < Limits.MAXPLAYERS; i++) {
                if (DOOM.playeringame[i] && !C2JUtils.eval(DOOM.players[i].mo)) {
                    DOOM.doomSystem.Error("P_SetupLevel: missing player %d start\n", i + 1);
                }
            }
        }

        // killough 3/26/98: Spawn icon landings:
        // TODO: if (DM.isCommercial())
        // P.SpawnBrainTargets();

        if (!DOOM.isShareware()) {
            // TODO: S.ParseMusInfo(lumpname);
        }

        // clear special respawning que
        DOOM.actions.ClearRespawnQueue();

        // set up world state
        P_SpawnSpecials: {
            DOOM.actions.SpawnSpecials();
        }

        // TODO: P.MapEnd();

        // preload graphics
        if (DOOM.precache) {
            /* @SourceCode.Compatible if together */
            R_PrecacheLevel: {
                DOOM.textureManager.PrecacheLevel();

                // MAES: thinkers are separate than texture management. Maybe split
                // sprite management as well?
                DOOM.sceneRenderer.PreCacheThinkers();
            }
        }

        /*
         * if (GL_DOOM){ if (V_GetMode() == VID_MODEGL) { // e6y // Do not
         * preprocess GL data during skipping, // because it potentially will
         * not be used. // But preprocessing must be called immediately after
         * stop of skipping. if (!doSkip) { // proff 11/99: calculate all OpenGL
         * specific tables etc. gld_PreprocessLevel(); } } }
         */
        // e6y
        // TODO P_SyncWalkcam(true, true);
        // TODO R_SmoothPlaying_Reset(NULL);
    }

}
