package org.bleachhack.util.doom.p;

import static org.bleachhack.util.doom.data.Defines.*;
import static org.bleachhack.util.doom.data.Limits.MAXPLAYERS;
import static org.bleachhack.util.doom.data.Limits.MAXRADIUS;
import org.bleachhack.util.doom.data.maplinedef_t;
import org.bleachhack.util.doom.data.mapnode_t;
import org.bleachhack.util.doom.data.mapsector_t;
import org.bleachhack.util.doom.data.mapseg_t;
import org.bleachhack.util.doom.data.mapsidedef_t;
import org.bleachhack.util.doom.data.mapsubsector_t;
import org.bleachhack.util.doom.data.mapthing_t;
import org.bleachhack.util.doom.data.mapvertex_t;
import org.bleachhack.util.doom.defines.*;
import org.bleachhack.util.doom.doom.CommandVariable;
import org.bleachhack.util.doom.doom.DoomMain;
import java.io.IOException;
import java.nio.ByteOrder;
import org.bleachhack.util.doom.m.BBox;
import static org.bleachhack.util.doom.m.BBox.BOXBOTTOM;
import static org.bleachhack.util.doom.m.BBox.BOXLEFT;
import static org.bleachhack.util.doom.m.BBox.BOXRIGHT;
import static org.bleachhack.util.doom.m.BBox.BOXTOP;
import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;
import static org.bleachhack.util.doom.m.fixed_t.FixedDiv;
import org.bleachhack.util.doom.rr.line_t;
import static org.bleachhack.util.doom.rr.line_t.ML_TWOSIDED;
import org.bleachhack.util.doom.rr.node_t;
import org.bleachhack.util.doom.rr.sector_t;
import org.bleachhack.util.doom.rr.seg_t;
import org.bleachhack.util.doom.rr.side_t;
import org.bleachhack.util.doom.rr.subsector_t;
import org.bleachhack.util.doom.rr.vertex_t;
import org.bleachhack.util.doom.s.degenmobj_t;
import static org.bleachhack.util.doom.utils.C2JUtils.flags;
import static org.bleachhack.util.doom.utils.GenericCopy.malloc;
import org.bleachhack.util.doom.wad.DoomBuffer;

//Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: LevelLoader.java,v 1.44 2012/09/24 17:16:23 velktron Exp $
//
// Copyright (C) 1993-1996 by id Software, Inc.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// DESCRIPTION:
//  Do all the WAD I/O, get map description,
//  set up initial state and misc. LUTs.
//
//-----------------------------------------------------------------------------
public class LevelLoader extends AbstractLevelLoader {

    public static final String rcsid = "$Id: LevelLoader.java,v 1.44 2012/09/24 17:16:23 velktron Exp $";

    public LevelLoader(DoomMain<?, ?> DM) {
        super(DM);
        // Traditional loader sets limit.
        deathmatchstarts = new mapthing_t[MAX_DEATHMATCH_STARTS];
    }

    /**
     * P_LoadVertexes
     *
     * @throws IOException
     */
    public void LoadVertexes(int lump) throws IOException {
        // Make a lame-ass attempt at loading some vertexes.

        // Determine number of lumps:
        //  total lump length / vertex record length.
        numvertexes = DOOM.wadLoader.LumpLength(lump) / mapvertex_t.sizeOf();

        // Load data into cache.
        // MAES: we now have a mismatch between memory/disk: in memory, we need an array.
        // On disk, we have a single lump/blob. Thus, we need to find a way to deserialize this...
        vertexes = DOOM.wadLoader.CacheLumpNumIntoArray(lump, numvertexes, vertex_t::new, vertex_t[]::new);

        // Copy and convert vertex coordinates,
        // MAES: not needed. Intermediate mapvertex_t struct skipped.
    }

    /**
     * P_LoadSegs
     *
     * @throws IOException
     */
    public void LoadSegs(int lump) throws IOException {

        mapseg_t[] data;
        mapseg_t ml;
        seg_t li;
        line_t ldef;
        int linedef;
        int side;

        // Another disparity between disk/memory. Treat it the same as VERTEXES.
        numsegs = DOOM.wadLoader.LumpLength(lump) / mapseg_t.sizeOf();
        segs = malloc(seg_t::new, seg_t[]::new, numsegs);
        data = DOOM.wadLoader.CacheLumpNumIntoArray(lump, numsegs, mapseg_t::new, mapseg_t[]::new);

        // We're not done yet!
        for (int i = 0; i < numsegs; i++) {
            li = segs[i];
            ml = data[i];
            li.v1 = vertexes[ml.v1];
            li.v2 = vertexes[ml.v2];
            li.assignVertexValues();

            li.angle = ((ml.angle) << 16) & 0xFFFFFFFFL;
            li.offset = (ml.offset) << 16;
            linedef = ml.linedef;
            li.linedef = ldef = lines[linedef];
            side = ml.side;
            li.sidedef = sides[ldef.sidenum[side]];
            li.frontsector = sides[ldef.sidenum[side]].sector;
            if (flags(ldef.flags, ML_TWOSIDED)) {
                // MAES: Fix double sided without back side. E.g. Linedef 16103 in Europe.wad
                if (ldef.sidenum[side ^ 1] != line_t.NO_INDEX) {
                    li.backsector = sides[ldef.sidenum[side ^ 1]].sector;
                }
                // Fix two-sided with no back side.
                //else {
                //li.backsector=null;
                //ldef.flags^=ML_TWOSIDED;
                //}
            } else {
                li.backsector = null;
            }
        }

    }

    /**
     * P_LoadSubsectors
     *
     * @throws IOException
     */
    public void LoadSubsectors(int lump) throws IOException {
        mapsubsector_t ms;
        subsector_t ss;
        mapsubsector_t[] data;

        numsubsectors = DOOM.wadLoader.LumpLength(lump) / mapsubsector_t.sizeOf();
        subsectors = malloc(subsector_t::new, subsector_t[]::new, numsubsectors);

        // Read "mapsubsectors"
        data = DOOM.wadLoader.CacheLumpNumIntoArray(lump, numsubsectors, mapsubsector_t::new, mapsubsector_t[]::new);

        for (int i = 0; i < numsubsectors; i++) {
            ms = data[i];
            ss = subsectors[i];
            ss.numlines = ms.numsegs;
            ss.firstline = ms.firstseg;
        }

    }

    /**
     * P_LoadSectors
     *
     * @throws IOException
     */
    public void LoadSectors(int lump) throws IOException {
        mapsector_t[] data;
        mapsector_t ms;
        sector_t ss;

        numsectors = DOOM.wadLoader.LumpLength(lump) / mapsector_t.sizeOf();
        sectors = malloc(sector_t::new, sector_t[]::new, numsectors);

        // Read "mapsectors"
        data = DOOM.wadLoader.CacheLumpNumIntoArray(lump, numsectors, mapsector_t::new, mapsector_t[]::new);

        for (int i = 0; i < numsectors; i++) {
            ms = data[i];
            ss = sectors[i];
            ss.floorheight = ms.floorheight << FRACBITS;
            ss.ceilingheight = ms.ceilingheight << FRACBITS;
            ss.floorpic = (short) DOOM.textureManager.FlatNumForName(ms.floorpic);
            ss.ceilingpic = (short) DOOM.textureManager.FlatNumForName(ms.ceilingpic);
            ss.lightlevel = ms.lightlevel;
            ss.special = ms.special;
            ss.tag = ms.tag;
            ss.thinglist = null;
            ss.id = i;
            ss.TL = this.DOOM.actions;
            ss.RND = this.DOOM.random;
        }

    }

    /**
     * P_LoadNodes
     *
     * @throws IOException
     */
    public void LoadNodes(int lump) throws IOException {
        mapnode_t[] data;
        int i;
        int j;
        int k;
        mapnode_t mn;
        node_t no;

        numnodes = DOOM.wadLoader.LumpLength(lump) / mapnode_t.sizeOf();
        nodes = malloc(node_t::new, node_t[]::new, numnodes);

        // Read "mapnodes"
        data = DOOM.wadLoader.CacheLumpNumIntoArray(lump, numnodes, mapnode_t::new, mapnode_t[]::new);

        for (i = 0; i < numnodes; i++) {
            mn = data[i];
            no = nodes[i];

            no.x = mn.x << FRACBITS;
            no.y = mn.y << FRACBITS;
            no.dx = mn.dx << FRACBITS;
            no.dy = mn.dy << FRACBITS;
            for (j = 0; j < 2; j++) {
                // e6y: support for extended nodes
                no.children[j] = (char) mn.children[j];

                // e6y: support for extended nodes
                if (no.children[j] == 0xFFFF) {
                    no.children[j] = 0xFFFFFFFF;
                } else if (flags(no.children[j], NF_SUBSECTOR_CLASSIC)) {
                    // Convert to extended type
                    no.children[j] &= ~NF_SUBSECTOR_CLASSIC;

                    // haleyjd 11/06/10: check for invalid subsector reference
                    if (no.children[j] >= numsubsectors) {
                        System.err
                            .printf(
                                "P_LoadNodes: BSP tree references invalid subsector %d.\n",
                                no.children[j]);
                        no.children[j] = 0;
                    }

                    no.children[j] |= NF_SUBSECTOR;
                }

                for (k = 0; k < 4; k++) {
                    no.bbox[j].set(k, mn.bbox[j][k] << FRACBITS);
                }
            }
        }

    }

    /**
     * P_LoadThings
     *
     * @throws IOException
     */
    public void LoadThings(int lump) throws IOException {
        mapthing_t[] data;
        mapthing_t mt;
        int numthings;
        boolean spawn;

        numthings = DOOM.wadLoader.LumpLength(lump) / mapthing_t.sizeOf();
        // VERY IMPORTANT: since now caching is near-absolute,
        // the mapthing_t instances must be CLONED rather than just
        // referenced, otherwise missing mobj bugs start  happening.

        data = DOOM.wadLoader.CacheLumpNumIntoArray(lump, numthings, mapthing_t::new, mapthing_t[]::new);

        for (int i = 0; i < numthings; i++) {
            mt = data[i];
            spawn = true;

            // Do not spawn cool, new monsters if !commercial
            if (!DOOM.isCommercial()) {
                switch (mt.type) {
                    case 68:  // Arachnotron
                    case 64:  // Archvile
                    case 88:  // Boss Brain
                    case 89:  // Boss Shooter
                    case 69:  // Hell Knight
                    case 67:  // Mancubus
                    case 71:  // Pain Elemental
                    case 65:  // Former Human Commando
                    case 66:  // Revenant
                    case 84: // Wolf SS
                        spawn = false;
                        break;
                }
            }
            if (spawn == false) {
                break;
            }

            // Do spawn all other stuff.
            // MAES: we have loaded the shit with the proper endianness, so no fucking around, bitch.
            /*mt.x = SHORT(mt.x);
      mt.y = SHORT(mt.y);
      mt.angle = SHORT(mt.angle);
      mt.type = SHORT(mt.type);
      mt.options = SHORT(mt.options);*/
            //System.out.printf("Spawning %d %s\n",i,mt.type);
            DOOM.actions.SpawnMapThing(mt);
        }

        // Status may have changed. It's better to release the resources anyway
        //W.UnlockLumpNum(lump);
    }

    /**
     * P_LoadLineDefs
     * Also counts secret lines for intermissions.
     *
     * @throws IOException
     */
    public void LoadLineDefs(int lump) throws IOException {
        maplinedef_t[] data;
        maplinedef_t mld;
        line_t ld;
        vertex_t v1;
        vertex_t v2;

        numlines = DOOM.wadLoader.LumpLength(lump) / maplinedef_t.sizeOf();
        lines = malloc(line_t::new, line_t[]::new, numlines);

        // Check those actually used in sectors, later on.
        used_lines = new boolean[numlines];

        // read "maplinedefs"
        data = DOOM.wadLoader.CacheLumpNumIntoArray(lump, numlines, maplinedef_t::new, maplinedef_t[]::new);

        for (int i = 0; i < numlines; i++) {
            mld = data[i];
            ld = lines[i];

            ld.flags = mld.flags;
            ld.special = mld.special;
            ld.tag = mld.tag;
            v1 = ld.v1 = vertexes[(char) mld.v1];
            v2 = ld.v2 = vertexes[(char) mld.v2];
            ld.dx = v2.x - v1.x;
            ld.dy = v2.y - v1.y;
            // Map value semantics.
            ld.assignVertexValues();

            if (ld.dx == 0) {
                ld.slopetype = slopetype_t.ST_VERTICAL;
            } else if (ld.dy == 0) {
                ld.slopetype = slopetype_t.ST_HORIZONTAL;
            } else {
                if (FixedDiv(ld.dy, ld.dx) > 0) {
                    ld.slopetype = slopetype_t.ST_POSITIVE;
                } else {
                    ld.slopetype = slopetype_t.ST_NEGATIVE;
                }
            }

            if (v1.x < v2.x) {
                ld.bbox[BOXLEFT] = v1.x;
                ld.bbox[BOXRIGHT] = v2.x;
            } else {
                ld.bbox[BOXLEFT] = v2.x;
                ld.bbox[BOXRIGHT] = v1.x;
            }

            if (v1.y < v2.y) {
                ld.bbox[BOXBOTTOM] = v1.y;
                ld.bbox[BOXTOP] = v2.y;
            } else {
                ld.bbox[BOXBOTTOM] = v2.y;
                ld.bbox[BOXTOP] = v1.y;
            }

            ld.sidenum[0] = mld.sidenum[0];
            ld.sidenum[1] = mld.sidenum[1];

            // Sanity check for two-sided without two valid sides.      
            if (flags(ld.flags, ML_TWOSIDED)) {
                if ((ld.sidenum[0] == line_t.NO_INDEX) || (ld.sidenum[1] == line_t.NO_INDEX)) {
                    // Well, dat ain't so tu-sided now, ey esse?
                    ld.flags ^= ML_TWOSIDED;
                }
            }

            // Front side defined without a valid frontsector.
            if (ld.sidenum[0] != line_t.NO_INDEX) {
                ld.frontsector = sides[ld.sidenum[0]].sector;
                if (ld.frontsector == null) { // // Still null? Bad map. Map to dummy.
                    ld.frontsector = dummy_sector;
                }

            } else {
                ld.frontsector = null;
            }

            // back side defined without a valid backsector.
            if (ld.sidenum[1] != line_t.NO_INDEX) {
                ld.backsector = sides[ld.sidenum[1]].sector;
                if (ld.backsector == null) { // Still null? Bad map. Map to dummy.
                    ld.backsector = dummy_sector;
                }
            } else {
                ld.backsector = null;
            }

            // If at least one valid sector is defined, then it's not null.
            if (ld.frontsector != null || ld.backsector != null) {
                this.used_lines[i] = true;
            }

        }

    }

    /**
     * P_LoadSideDefs
     */
    public void LoadSideDefs(int lump) throws IOException {
        mapsidedef_t[] data;
        mapsidedef_t msd;
        side_t sd;

        numsides = DOOM.wadLoader.LumpLength(lump) / mapsidedef_t.sizeOf();
        sides = malloc(side_t::new, side_t[]::new, numsides);

        data = DOOM.wadLoader.CacheLumpNumIntoArray(lump, numsides, mapsidedef_t::new, mapsidedef_t[]::new);

        for (int i = 0; i < numsides; i++) {
            msd = data[i];
            sd = sides[i];

            sd.textureoffset = (msd.textureoffset) << FRACBITS;
            sd.rowoffset = (msd.rowoffset) << FRACBITS;
            sd.toptexture = (short) DOOM.textureManager.TextureNumForName(msd.toptexture);
            sd.bottomtexture = (short) DOOM.textureManager.TextureNumForName(msd.bottomtexture);
            sd.midtexture = (short) DOOM.textureManager.TextureNumForName(msd.midtexture);
            if (msd.sector < 0) {
                sd.sector = dummy_sector;
            } else {
                sd.sector = sectors[msd.sector];
            }
        }
    }

    // MAES 22/5/2011 This hack added for PHOBOS2.WAD, in order to
    // accomodate for some linedefs having a sector number of "-1".
    // Any negative sector will get rewired to this dummy sector.
    // PROBABLY, this will handle unused sector/linedefes cleanly?
    sector_t dummy_sector = new sector_t();

    /**
     * P_LoadBlockMap
     *
     * @throws IOException
     *
     * TODO: generate BLOCKMAP dynamically to
     * handle missing cases and increase accuracy.
     *
     */
    public void LoadBlockMap(int lump) throws IOException {
        int count = 0;

        if (DOOM.cVarManager.bool(CommandVariable.BLOCKMAP) || DOOM.wadLoader.LumpLength(lump) < 8
            || (count = DOOM.wadLoader.LumpLength(lump) / 2) >= 0x10000) // e6y
        {
            CreateBlockMap();
        } else {

            DoomBuffer data = (DoomBuffer) DOOM.wadLoader.CacheLumpNum(lump, PU_LEVEL, DoomBuffer.class);
            count = DOOM.wadLoader.LumpLength(lump) / 2;
            blockmaplump = new int[count];

            data.setOrder(ByteOrder.LITTLE_ENDIAN);
            data.rewind();
            data.readCharArray(blockmaplump, count);

            // Maes: first four shorts are header data.
            bmaporgx = blockmaplump[0] << FRACBITS;
            bmaporgy = blockmaplump[1] << FRACBITS;
            bmapwidth = blockmaplump[2];
            bmapheight = blockmaplump[3];

            // MAES: use killough's code to convert terminators to -1 beforehand
            for (int i = 4; i < count; i++) {
                short t = (short) blockmaplump[i]; // killough 3/1/98
                blockmaplump[i] = (int) (t == -1 ? -1l : t & 0xffff);
            }

            // haleyjd 03/04/10: check for blockmap problems
            // http://www.doomworld.com/idgames/index.php?id=12935
            if (!VerifyBlockMap(count)) {
                System.err
                    .printf("P_LoadBlockMap: erroneous BLOCKMAP lump may cause crashes.\n");
                System.err
                    .printf("P_LoadBlockMap: use \"-blockmap\" command line switch for rebuilding\n");
            }

        }
        count = bmapwidth * bmapheight;

        // IMPORTANT MODIFICATION: no need to have both blockmaplump AND blockmap.
        // If the offsets in the lump are OK, then we can modify them (remove 4)
        // and copy the rest of the data in one single data array. This avoids
        // reserving memory for two arrays (we can't simply alias one in Java)
        blockmap = new int[blockmaplump.length - 4];

        // Offsets are relative to START OF BLOCKMAP, and IN SHORTS, not bytes.
        for (int i = 0; i < blockmaplump.length - 4; i++) {
            // Modify indexes so that we don't need two different lumps.
            // Can probably be further optimized if we simply shift everything backwards.
            // and reuse the same memory space.
            if (i < count) {
                blockmaplump[i] = blockmaplump[i + 4] - 4;
            } else {
                // Make terminators definitively -1, different that 0xffff
                short t = (short) blockmaplump[i + 4];          // killough 3/1/98
                blockmaplump[i] = (int) (t == -1 ? -1l : t & 0xffff);
            }
        }

        // clear out mobj chains
        // ATTENTION! BUG!!!
        // If blocklinks are "cleared" to void -but instantiated- objects,
        // very bad bugs happen, especially the second time a level is re-instantiated.
        // Probably caused other bugs as well, as an extra object would appear in iterators.
        if (blocklinks != null && blocklinks.length == count) {
            for (int i = 0; i < count; i++) {
                blocklinks[i] = null;
            }
        } else {
            blocklinks = new mobj_t[count];
        }

        // Bye bye. Not needed.
        blockmap = blockmaplump;
    }

    /**
     * P_GroupLines
     * Builds sector line lists and subsector sector numbers.
     * Finds block bounding boxes for sectors.
     */
    public void GroupLines() {
        int total;
        line_t li;
        sector_t sector;
        subsector_t ss;
        seg_t seg;
        int[] bbox = new int[4];
        int block;

        // look up sector number for each subsector
        for (int i = 0; i < numsubsectors; i++) {
            ss = subsectors[i];
            seg = segs[ss.firstline];
            ss.sector = seg.sidedef.sector;
        }

        //linebuffer=new line_t[numsectors][0];
        // count number of lines in each sector
        total = 0;

        for (int i = 0; i < numlines; i++) {
            li = lines[i];
            total++;
            li.frontsector.linecount++;

            if ((li.backsector != null) && (li.backsector != li.frontsector)) {
                li.backsector.linecount++;
                total++;
            }

        }

        // build line tables for each sector    
        // MAES: we don't really need this in Java.
        // linebuffer = new line_t[total];
        // int linebuffercount=0;
        // We scan through ALL sectors.
        for (int i = 0; i < numsectors; i++) {
            sector = sectors[i];
            BBox.ClearBox(bbox);
            //sector->lines = linebuffer;
            // We can just construct line tables of the correct size
            // for each sector.
            int countlines = 0;
            // We scan through ALL lines....

            // System.out.println(i+ ": looking for sector -> "+sector);
            for (int j = 0; j < numlines; j++) {
                li = lines[j];

                //System.out.println(j+ " front "+li.frontsector+ " back "+li.backsector);
                if (li.frontsector == sector || li.backsector == sector) {
                    // This sector will have one more line.
                    countlines++;
                    // Expand bounding box...
                    BBox.AddToBox(bbox, li.v1.x, li.v1.y);
                    BBox.AddToBox(bbox, li.v2.x, li.v2.y);
                }
            }

            // So, this sector must have that many lines.
            sector.lines = new line_t[countlines];

            int addedlines = 0;
            int pointline = 0;

            // Add actual lines into sectors.
            for (int j = 0; j < numlines; j++) {
                li = lines[j];
                // If
                if (li.frontsector == sector || li.backsector == sector) {
                    // This sector will have one more line.
                    sectors[i].lines[pointline++] = lines[j];
                    addedlines++;
                }
            }

            if (addedlines != sector.linecount) {
                DOOM.doomSystem.Error("P_GroupLines: miscounted");
            }

            // set the degenmobj_t to the middle of the bounding box
            sector.soundorg = new degenmobj_t(((bbox[BOXRIGHT] + bbox[BOXLEFT]) / 2),
                ((bbox[BOXTOP] + bbox[BOXBOTTOM]) / 2), (sector.ceilingheight - sector.floorheight) / 2);

            // adjust bounding box to map blocks
            block = (bbox[BOXTOP] - bmaporgy + MAXRADIUS) >> MAPBLOCKSHIFT;
            block = block >= bmapheight ? bmapheight - 1 : block;
            sector.blockbox[BOXTOP] = block;

            block = (bbox[BOXBOTTOM] - bmaporgy - MAXRADIUS) >> MAPBLOCKSHIFT;
            block = block < 0 ? 0 : block;
            sector.blockbox[BOXBOTTOM] = block;

            block = (bbox[BOXRIGHT] - bmaporgx + MAXRADIUS) >> MAPBLOCKSHIFT;
            block = block >= bmapwidth ? bmapwidth - 1 : block;
            sector.blockbox[BOXRIGHT] = block;

            block = (bbox[BOXLEFT] - bmaporgx - MAXRADIUS) >> MAPBLOCKSHIFT;
            block = block < 0 ? 0 : block;
            sector.blockbox[BOXLEFT] = block;
        }

    }

    @Override
    public void
        SetupLevel(int episode,
            int map,
            int playermask,
            skill_t skill) {
        int i;
        String lumpname;
        int lumpnum;

        try {
            DOOM.totalkills = DOOM.totalitems = DOOM.totalsecret = DOOM.wminfo.maxfrags = 0;
            DOOM.wminfo.partime = 180;
            for (i = 0; i < MAXPLAYERS; i++) {
                DOOM.players[i].killcount = DOOM.players[i].secretcount
                    = DOOM.players[i].itemcount = 0;
            }

            // Initial height of PointOfView
            // will be set by player think.
            DOOM.players[DOOM.consoleplayer].viewz = 1;

            // Make sure all sounds are stopped before Z_FreeTags.
            DOOM.doomSound.Start();

            /*    
  #if 0 // UNUSED
      if (debugfile)
      {
      Z_FreeTags (PU_LEVEL, MAXINT);
      Z_FileDumpHeap (debugfile);
      }
      else
  #endif
             */
            //  Z_FreeTags (PU_LEVEL, PU_PURGELEVEL-1);
            // UNUSED W_Profile ();
            DOOM.actions.InitThinkers();

            // if working with a development map, reload it
            DOOM.wadLoader.Reload();

            // find map name
            if (DOOM.isCommercial()) {
                if (map < 10) {
                    lumpname = "MAP0" + map;
                } else {
                    lumpname = "MAP" + map;
                }
            } else {
                lumpname = ("E"
                    + (char) ('0' + episode)
                    + "M"
                    + (char) ('0' + map));
            }

            lumpnum = DOOM.wadLoader.GetNumForName(lumpname);

            DOOM.leveltime = 0;

            if (!DOOM.wadLoader.verifyLumpName(lumpnum + ML_BLOCKMAP, LABELS[ML_BLOCKMAP])) {
                System.err.println("Blockmap missing!");
            }

            // note: most of this ordering is important
            this.LoadVertexes(lumpnum + ML_VERTEXES);
            this.LoadSectors(lumpnum + ML_SECTORS);
            this.LoadSideDefs(lumpnum + ML_SIDEDEFS);
            this.LoadLineDefs(lumpnum + ML_LINEDEFS);
            this.LoadSubsectors(lumpnum + ML_SSECTORS);
            this.LoadNodes(lumpnum + ML_NODES);
            this.LoadSegs(lumpnum + ML_SEGS);

            // MAES: in order to apply optimizations and rebuilding, order must be changed.
            this.LoadBlockMap(lumpnum + ML_BLOCKMAP);
            //this.SanitizeBlockmap();
            //this.getMapBoundingBox();

            this.LoadReject(lumpnum + ML_REJECT);

            this.GroupLines();

            DOOM.bodyqueslot = 0;
            // Reset to "deathmatch starts"
            DOOM.deathmatch_p = 0;
            this.LoadThings(lumpnum + ML_THINGS);

            // if deathmatch, randomly spawn the active players
            if (DOOM.deathmatch) {
                for (i = 0; i < MAXPLAYERS; i++) {
                    if (DOOM.playeringame[i]) {
                        DOOM.players[i].mo = null;
                        DOOM.DeathMatchSpawnPlayer(i);
                    }
                }

            }

            // clear special respawning que
            DOOM.actions.ClearRespawnQueue();

            // set up world state
            DOOM.actions.SpawnSpecials();

            // build subsector connect matrix
            //  UNUSED P_ConnectSubsectors ();
            // preload graphics
            if (DOOM.precache) {
                DOOM.textureManager.PrecacheLevel();
                // MAES: thinkers are separate than texture management. Maybe split sprite management as well?
                DOOM.sceneRenderer.PreCacheThinkers();

            }

        } catch (Exception e) {
            System.err.println("Error while loading level");
            e.printStackTrace();
        }
    }

}

//$Log: LevelLoader.java,v $
//Revision 1.44  2012/09/24 17:16:23  velktron
//Massive merge between HiColor and HEAD. There's no difference from now on, and development continues on HEAD.
//
//Revision 1.43.2.2  2012/09/24 16:57:16  velktron
//Addressed generics warnings.
//
//Revision 1.43.2.1  2012/03/26 09:53:44  velktron
//Use line_t.NO_INDEX for good measure, when possible.
//
//Revision 1.43  2011/11/03 15:19:51  velktron
//Adapted to using ISpriteManager
//
//Revision 1.42  2011/10/07 16:05:52  velktron
//Now using line_t for ML_* definitions.
//
//Revision 1.41  2011/10/06 16:44:32  velktron
//Proper support for extended nodes, made reject loading into a separate method.
//
//Revision 1.40  2011/09/30 15:20:24  velktron
//Very modified, useless SanitizeBlockmap method ditched. 
//Common utility methods moved to superclass. Shares blockmap checking and generation 
//with Boom-derived code. Now capable of running Europe.wad. 
//TODO: Blockmap generation can be really slow on large levels. 
//Optimize better for Java, or parallelize.
//
//Revision 1.39  2011/09/29 17:22:08  velktron
//Blockchain terminators are now -1 (extended)
//
//Revision 1.38  2011/09/29 17:11:32  velktron
//Blockmap optimizations.
//
//Revision 1.37  2011/09/29 15:17:48  velktron
//SetupLevel can propagate exceptions.
//
//Revision 1.36  2011/09/29 13:28:01  velktron
//Extends AbstractLevelLoader
//
//Revision 1.35  2011/09/27 18:04:36  velktron
//Fixed major blockmap bug
//
//Revision 1.34  2011/09/27 16:00:20  velktron
//Minor blockmap stuff.
//
//Revision 1.33  2011/08/24 15:52:04  velktron
//Sets proper ISoundOrigin for sectors (height, too)
//
//Revision 1.32  2011/08/24 15:00:34  velktron
//Improved version, now using createArrayOfObjects. Much better syntax.
//
//Revision 1.31  2011/08/23 16:17:22  velktron
//Got rid of Z remnants.
//
//Revision 1.30  2011/07/27 21:26:19  velktron
//Quieted down debugging for v1.5 release
//
//Revision 1.29  2011/07/25 19:56:53  velktron
//reject matrix size bugfix, fron danmaku branch.
//
//Revision 1.28  2011/07/22 15:37:52  velktron
//Began blockmap autogen code...still WIP
//
//Revision 1.27  2011/07/20 16:14:45  velktron
//Bullet-proofing vs missing or corrupt REJECT table. TODO: built-in system to re-compute it.
//
//Revision 1.26  2011/06/18 23:25:33  velktron
//Removed debugginess
//
//Revision 1.25  2011/06/18 23:21:26  velktron
//-id
//
//Revision 1.24  2011/06/18 23:18:24  velktron
//Added sanitization for broken two-sided sidedefs, and semi-support for extended blockmaps.
//
//Revision 1.23  2011/05/24 11:31:47  velktron
//Adapted to IDoomStatusBar
//
//Revision 1.22  2011/05/22 21:09:34  velktron
//Added spechit overflow handling, and unused linedefs (with -1 sector) handling.
//
//Revision 1.21  2011/05/21 14:53:57  velktron
//Adapted to use new gamemode system.
//
//Revision 1.20  2011/05/20 14:52:23  velktron
//Moved several function from the Renderer and Action code in here, since it made more sense.
//
//Revision 1.19  2011/05/18 16:55:44  velktron
//TEMPORARY TESTING VERSION, DO NOT USE
//
//Revision 1.18  2011/05/17 16:51:20  velktron
//Switched to DoomStatus
//
//Revision 1.17  2011/05/10 10:39:18  velktron
//Semi-playable Techdemo v1.3 milestone
//
//Revision 1.16  2011/05/05 17:24:22  velktron
//Started merging more of _D_'s changes.
//
//Revision 1.15  2010/12/20 17:15:08  velktron
//Made the renderer more OO -> TextureManager and other changes as well.
//
//Revision 1.14  2010/11/22 21:41:22  velktron
//Parallel rendering...sort of.It works, but either  the barriers are broken or it's simply not worthwhile at this point :-/
//
//Revision 1.13  2010/11/22 14:54:53  velktron
//Greater objectification of sectors etc.
//
//Revision 1.12  2010/11/22 01:17:16  velktron
//Fixed blockmap (for the most part), some actions implemented and functional, ambient animation/lighting functional.
//
//Revision 1.11  2010/11/14 20:00:21  velktron
//Bleeding floor bug fixed!
//
//Revision 1.10  2010/11/03 16:48:04  velktron
//"Bling" view angles fixed (perhaps related to the "bleeding line bug"?)
//
//Revision 1.9  2010/09/27 02:27:29  velktron
//BEASTLY update
//
//Revision 1.8  2010/09/23 20:36:45  velktron
//*** empty log message ***
//
//Revision 1.7  2010/09/23 15:11:57  velktron
//A bit closer...
//
//Revision 1.6  2010/09/22 16:40:02  velktron
//MASSIVE changes in the status passing model.
//DoomMain and DoomGame unified.
//Doomstat merged into DoomMain (now status and game functions are one).
//
//Most of DoomMain implemented. Possible to attempt a "classic type" start but will stop when reading sprites.
//
//Revision 1.5  2010/09/21 15:53:37  velktron
//Split the Map ...somewhat...
//
//Revision 1.4  2010/09/14 15:34:01  velktron
//The enormity of this commit is incredible (pun intended)
//
//Revision 1.3  2010/09/08 15:22:18  velktron
//x,y coords in some structs as value semantics. Possible speed increase?
//
//Revision 1.2  2010/09/02 15:56:54  velktron
//Bulk of unified renderer copyediting done.
//
//Some changes like e.g. global separate limits class and instance methods for seg_t and node_t introduced.
//
//Revision 1.1  2010/09/01 15:53:42  velktron
//Graphics data loader implemented....still need to figure out how column caching works, though.
//
//Revision 1.4  2010/08/19 23:14:49  velktron
//Automap
//
//Revision 1.3  2010/08/13 14:06:36  velktron
//Endlevel screen fully functional!
//
//Revision 1.2  2010/08/11 16:31:34  velktron
//Map loading works! Check out LevelLoaderTester for more.
//
//Revision 1.1  2010/08/10 16:41:57  velktron
//Threw some work into map loading.
//
