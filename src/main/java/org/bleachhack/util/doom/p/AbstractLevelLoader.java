package org.bleachhack.util.doom.p;

import static org.bleachhack.util.doom.data.Defines.BLOCKMAPPADDING;
import static org.bleachhack.util.doom.data.Defines.MAPBLOCKSHIFT;
import static org.bleachhack.util.doom.data.Defines.MAPBLOCKUNITS;
import static org.bleachhack.util.doom.data.Defines.NF_SUBSECTOR;
import static org.bleachhack.util.doom.data.Defines.PU_LEVEL;
import org.bleachhack.util.doom.data.Limits;
import org.bleachhack.util.doom.data.mapthing_t;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.SourceCode;
import org.bleachhack.util.doom.doom.SourceCode.P_MapUtl;
import static org.bleachhack.util.doom.doom.SourceCode.P_MapUtl.P_SetThingPosition;
import org.bleachhack.util.doom.doom.SourceCode.R_Main;
import static org.bleachhack.util.doom.doom.SourceCode.R_Main.*;
import org.bleachhack.util.doom.doom.SourceCode.fixed_t;
import org.bleachhack.util.doom.m.BBox;
import org.bleachhack.util.doom.m.Settings;
import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;
import org.bleachhack.util.doom.mochadoom.Engine;
import static org.bleachhack.util.doom.p.mobj_t.MF_NOBLOCKMAP;
import static org.bleachhack.util.doom.p.mobj_t.MF_NOSECTOR;
import org.bleachhack.util.doom.rr.line_t;
import org.bleachhack.util.doom.rr.node_t;
import org.bleachhack.util.doom.rr.sector_t;
import org.bleachhack.util.doom.rr.seg_t;
import org.bleachhack.util.doom.rr.side_t;
import org.bleachhack.util.doom.rr.subsector_t;
import org.bleachhack.util.doom.rr.vertex_t;
import org.bleachhack.util.doom.utils.C2JUtils;
import static org.bleachhack.util.doom.utils.C2JUtils.flags;

/**
 * The idea is to lump common externally readable properties that need DIRECT
 * ACCESS (aka not behindsetters/getters) here, as well as some common shared
 * internal structures/status objects. If you need access to stuff like the
 * blockmap/reject table etc. then you should ask for this class. If you only
 * need access to some methods like e.g. SetupLevel, you can simply use the
 * ILevelLoader interface.
 * 
 * @author velktron
 */

public abstract class AbstractLevelLoader implements ILevelLoader {

    // ///////////////// Status objects ///////////////////

    final DoomMain<?,?> DOOM;

    //
    // MAP related Lookup tables.
    // Store VERTEXES, LINEDEFS, SIDEDEFS, etc.
    //
    public int numvertexes;
    public vertex_t[] vertexes;
    public int numsegs;
    public seg_t[] segs;
    public int numsectors;
    public sector_t[] sectors;
    public int numsubsectors;
    public subsector_t[] subsectors;
    public int numnodes;
    public node_t[] nodes;
    public int numlines;
    public line_t[] lines;
    public int numsides;
    public side_t[] sides;


    // BLOCKMAP
    // Created from axis aligned bounding box
    // of the map, a rectangular array of
    // blocks of size ...
    // Used to speed up collision detection
    // by spatial subdivision in 2D.
    //
    /** Blockmap size. */
    public int bmapwidth; // size in mapblocks

    public int bmapheight; // size in mapblocks

    /**
     * killough 3/1/98: remove blockmap limit internally. Maes 29/9/2011: Header
     * stripped during loading and pointers pre-modified, so there's no double
     * arraying.
     */
    public int[] blockmap; // was short -- killough

    /**
     * Maes 29/9/2011: Used only during loading. No more dichotomy with
     * blockmap.
     */
    protected int[] blockmaplump; // was short -- killough

    @fixed_t public int bmaporgx, bmaporgy;

    /** for thing chains */
    public mobj_t[] blocklinks;

    /**
     * REJECT For fast sight rejection. Speeds up enemy AI by skipping detailed
     * LineOf Sight calculation. Without special effect, this could be used as a
     * PVS lookup as well.
     */

    public byte[] rejectmatrix;

    // Maintain single and multi player starting spots.

    // 1/11/98 killough: Remove limit on deathmatch starts
    protected mapthing_t[] deathmatchstarts; // killough

    protected int num_deathmatchstarts; // killough

    // mapthing_t* deathmatch_p;
    protected int deathmatch_p;

    protected mapthing_t[] playerstarts = new mapthing_t[Limits.MAXPLAYERS];

    public AbstractLevelLoader(DoomMain<?,?> DOOM) {
        this.DOOM = DOOM;
    }

    /**
     * P_SetThingPosition Links a thing into both a block and a subsector based
     * on it's x y. Sets thing.subsector properly
     */

    @Override
    @SourceCode.Exact
    @P_MapUtl.C(P_SetThingPosition)
    public void SetThingPosition(mobj_t thing) {
        final subsector_t ss;
        final sector_t sec;
        int blockx;
        int blocky;
        final mobj_t link;

        // link into subsector
        R_PointInSubsector: {
            ss = PointInSubsector(thing.x, thing.y);
        }
        thing.subsector = ss;

        if (!flags(thing.flags, MF_NOSECTOR)) {
            // invisible things don't go into the sector links
            sec = ss.sector;

            thing.sprev = null;
            thing.snext = sec.thinglist;

            if (sec.thinglist != null) {
                sec.thinglist.sprev = thing;
            }

            sec.thinglist = thing;
        }

        // link into blockmap
        if (!flags(thing.flags, MF_NOBLOCKMAP)) {
            // inert things don't need to be in blockmap
            blockx = getSafeBlockX(thing.x - bmaporgx);
            blocky = getSafeBlockY(thing.y - bmaporgy);
            
            // Valid block?
            if (blockx >= 0
                && blockx < bmapwidth
                && blocky >= 0
                && blocky < bmapheight
            ) {
                // Get said block.
                link = blocklinks[blocky * bmapwidth + blockx];
                thing.bprev = null; // Thing is put at head of block...
                thing.bnext = link;
                if (link != null) { // block links back at thing...
                    // This will work
                    link.bprev = thing;
                }

                // "thing" is now effectively the new head
                // Iterators only follow "bnext", not "bprev".
                // If link was null, then thing is the first entry.
                blocklinks[blocky * bmapwidth + blockx] = thing;
            } else {
                // thing is off the map
                thing.bnext = thing.bprev = null;
            }
        }

    }

    @Override
    @SourceCode.Exact
    @R_Main.C(R_PointInSubsector)
    public subsector_t PointInSubsector(@fixed_t int x, @fixed_t int y) {
        node_t node;
        int side;
        int nodenum;

        // single subsector is a special case
        if (numnodes == 0) {
            return subsectors[0];
        }
        
        nodenum = numnodes - 1;
        
        while (!C2JUtils.flags(nodenum, NF_SUBSECTOR)) {
            node = nodes[nodenum];
            R_PointOnSide: {
                side = node.PointOnSide(x, y);
            }
            nodenum = node.children[side];
        }

        return subsectors[nodenum & ~NF_SUBSECTOR];
    }

    //
    // jff 10/6/98
    // New code added to speed up calculation of internal blockmap
    // Algorithm is order of nlines*(ncols+nrows) not nlines*ncols*nrows
    //

    /**
     * places to shift rel position for cell num
     */
    protected static final int BLOCK_SHIFT = 7; 

    /**
     * mask for rel position within cell
     */
    protected static final int BLOCK_MASK = ((1 << BLOCK_SHIFT) - 1);

    /**
     * size guardband around map used
     */
    protected static final int BLOCK_MARGIN = 0;

    // jff 10/8/98 use guardband>0
    // jff 10/12/98 0 ok with + 1 in rows,cols

    protected class linelist_t // type used to list lines in each block
    {
        public int num;

        public linelist_t next;
    }

    /**
     * Subroutine to add a line number to a block list It simply returns if the
     * line is already in the block
     * 
     * @param lists
     * @param count
     * @param done
     * @param blockno
     * @param lineno
     */
    private void AddBlockLine(linelist_t[] lists, int[] count, boolean[] done, int blockno, int lineno) {
        long a=System.nanoTime();
        linelist_t l;

        if (done[blockno])
            return;

        l = new linelist_t();
        l.num = lineno;
        l.next = lists[blockno];
        lists[blockno] = l;
        count[blockno]++;
        done[blockno] = true;
        long b=System.nanoTime();
        
        total+=(b-a);
    }

    long total=0;
    
    /**
     * Actually construct the blockmap lump from the level data This finds the
     * intersection of each linedef with the column and row lines at the left
     * and bottom of each blockmap cell. It then adds the line to all block
     * lists touching the intersection. MAES 30/9/2011: Converted to Java. It's
     * important that LINEDEFS and VERTEXES are already read-in and defined, so
     * it is necessary to change map lump ordering for this to work.
     */

    protected final void CreateBlockMap() {
        int xorg, yorg; // blockmap origin (lower left)
        int nrows, ncols; // blockmap dimensions
        linelist_t[] blocklists; // array of pointers to lists of lines
        int[] blockcount; // array of counters of line lists
        boolean[] blockdone; // array keeping track of blocks/line
        int NBlocks; // number of cells = nrows*ncols
        int linetotal; // total length of all blocklists
        int map_minx = Integer.MAX_VALUE; // init for map limits search
        int map_miny = Integer.MAX_VALUE;
        int map_maxx = Integer.MIN_VALUE;
        int map_maxy = Integer.MIN_VALUE;

        long a = System.nanoTime();

        // scan for map limits, which the blockmap must enclose

        for (int i = 0; i < numvertexes; i++) {
            int t;

            if ((t = vertexes[i].x) < map_minx)
                map_minx = t;
            else if (t > map_maxx)
                map_maxx = t;
            if ((t = vertexes[i].y) < map_miny)
                map_miny = t;
            else if (t > map_maxy)
                map_maxy = t;
        }
        map_minx >>= FRACBITS; // work in map coords, not fixed_t
        map_maxx >>= FRACBITS;
        map_miny >>= FRACBITS;
        map_maxy >>= FRACBITS;

        // set up blockmap area to enclose level plus margin

        xorg = map_minx - BLOCK_MARGIN;
        yorg = map_miny - BLOCK_MARGIN;
        ncols = (map_maxx + BLOCK_MARGIN - xorg + 1 + BLOCK_MASK) >> BLOCK_SHIFT; // jff
                                                                         // 10/12/98
        nrows = (map_maxy + BLOCK_MARGIN - yorg + 1 + BLOCK_MASK) >> BLOCK_SHIFT; // +1
                                                                         // needed
                                                                         // for
        NBlocks = ncols * nrows; // map exactly 1 cell

        // create the array of pointers on NBlocks to blocklists
        // also create an array of linelist counts on NBlocks
        // finally make an array in which we can mark blocks done per line

        // CPhipps - calloc's
        blocklists = new linelist_t[NBlocks];
        blockcount = new int[NBlocks];
        blockdone = new boolean[NBlocks];

        // initialize each blocklist, and enter the trailing -1 in all
        // blocklists
        // note the linked list of lines grows backwards

        for (int i = 0; i < NBlocks; i++) {
            blocklists[i] = new linelist_t();
            blocklists[i].num = -1;
            blocklists[i].next = null;
            blockcount[i]++;
        }

        // For each linedef in the wad, determine all blockmap blocks it
        // touches,
        // and add the linedef number to the blocklists for those blocks

        for (int i = 0; i < numlines; i++) {
            int x1 = lines[i].v1x >> FRACBITS; // lines[i] map coords
            int y1 = lines[i].v1y >> FRACBITS;
            int x2 = lines[i].v2x >> FRACBITS;
            int y2 = lines[i].v2y >> FRACBITS;
            int dx = x2 - x1;
            int dy = y2 - y1;
            boolean vert = dx == 0; // lines[i] slopetype
            boolean horiz = dy == 0;
            boolean spos = (dx ^ dy) > 0;
            boolean sneg = (dx ^ dy) < 0;
            int bx, by; // block cell coords
            int minx = x1 > x2 ? x2 : x1; // extremal lines[i] coords
            int maxx = x1 > x2 ? x1 : x2;
            int miny = y1 > y2 ? y2 : y1;
            int maxy = y1 > y2 ? y1 : y2;

            // no blocks done for this linedef yet

            C2JUtils.memset(blockdone, false, NBlocks);

            // The line always belongs to the blocks containing its endpoints

            bx = (x1 - xorg) >> BLOCK_SHIFT;
            by = (y1 - yorg) >> BLOCK_SHIFT;
            AddBlockLine(blocklists, blockcount, blockdone, by * ncols + bx, i);
            bx = (x2 - xorg) >> BLOCK_SHIFT;
            by = (y2 - yorg) >> BLOCK_SHIFT;
            AddBlockLine(blocklists, blockcount, blockdone, by * ncols + bx, i);

            // For each column, see where the line along its left edge, which
            // it contains, intersects the Linedef i. Add i to each
            // corresponding
            // blocklist.

            if (!vert) // don't interesect vertical lines with columns
            {
                for (int j = 0; j < ncols; j++) {
                    // intersection of Linedef with x=xorg+(j<<blkshift)
                    // (y-y1)*dx = dy*(x-x1)
                    // y = dy*(x-x1)+y1*dx;

                    int x = xorg + (j << BLOCK_SHIFT); // (x,y) is intersection
                    int y = (dy * (x - x1)) / dx + y1;
                    int yb = (y - yorg) >> BLOCK_SHIFT; // block row number
                    int yp = (y - yorg) & BLOCK_MASK; // y position within block

                    if (yb < 0 || yb > nrows - 1) // outside blockmap, continue
                        continue;

                    if (x < minx || x > maxx) // line doesn't touch column
                        continue;

                    // The cell that contains the intersection point is always
                    // added

                    AddBlockLine(blocklists, blockcount, blockdone, ncols * yb
                            + j, i);

                    // if the intersection is at a corner it depends on the
                    // slope
                    // (and whether the line extends past the intersection)
                    // which
                    // blocks are hit

                    if (yp == 0) // intersection at a corner
                    {
                        if (sneg) // \ - blocks x,y-, x-,y
                        {
                            if (yb > 0 && miny < y)
                                AddBlockLine(blocklists, blockcount, blockdone,
                                    ncols * (yb - 1) + j, i);
                            if (j > 0 && minx < x)
                                AddBlockLine(blocklists, blockcount, blockdone,
                                    ncols * yb + j - 1, i);
                        } else if (spos) // / - block x-,y-
                        {
                            if (yb > 0 && j > 0 && minx < x)
                                AddBlockLine(blocklists, blockcount, blockdone,
                                    ncols * (yb - 1) + j - 1, i);
                        } else if (horiz) // - - block x-,y
                        {
                            if (j > 0 && minx < x)
                                AddBlockLine(blocklists, blockcount, blockdone,
                                    ncols * yb + j - 1, i);
                        }
                    } else if (j > 0 && minx < x) // else not at corner: x-,y
                        AddBlockLine(blocklists, blockcount, blockdone, ncols
                                * yb + j - 1, i);
                }
            }

            // For each row, see where the line along its bottom edge, which
            // it contains, intersects the Linedef i. Add i to all the
            // corresponding
            // blocklists.

            if (!horiz) {
                for (int j = 0; j < nrows; j++) {
                    // intersection of Linedef with y=yorg+(j<<blkshift)
                    // (x,y) on Linedef i satisfies: (y-y1)*dx = dy*(x-x1)
                    // x = dx*(y-y1)/dy+x1;

                    int y = yorg + (j << BLOCK_SHIFT); // (x,y) is intersection
                    int x = (dx * (y - y1)) / dy + x1;
                    int xb = (x - xorg) >> BLOCK_SHIFT; // block column number
                    int xp = (x - xorg) & BLOCK_MASK; // x position within block

                    if (xb < 0 || xb > ncols - 1) // outside blockmap, continue
                        continue;

                    if (y < miny || y > maxy) // line doesn't touch row
                        continue;

                    // The cell that contains the intersection point is always
                    // added

                    AddBlockLine(blocklists, blockcount, blockdone, ncols * j
                            + xb, i);

                    // if the intersection is at a corner it depends on the
                    // slope
                    // (and whether the line extends past the intersection)
                    // which
                    // blocks are hit

                    if (xp == 0) // intersection at a corner
                    {
                        if (sneg) // \ - blocks x,y-, x-,y
                        {
                            if (j > 0 && miny < y)
                                AddBlockLine(blocklists, blockcount, blockdone,
                                    ncols * (j - 1) + xb, i);
                            if (xb > 0 && minx < x)
                                AddBlockLine(blocklists, blockcount, blockdone,
                                    ncols * j + xb - 1, i);
                        } else if (vert) // | - block x,y-
                        {
                            if (j > 0 && miny < y)
                                AddBlockLine(blocklists, blockcount, blockdone,
                                    ncols * (j - 1) + xb, i);
                        } else if (spos) // / - block x-,y-
                        {
                            if (xb > 0 && j > 0 && miny < y)
                                AddBlockLine(blocklists, blockcount, blockdone,
                                    ncols * (j - 1) + xb - 1, i);
                        }
                    } else if (j > 0 && miny < y) // else not on a corner: x,y-
                        AddBlockLine(blocklists, blockcount, blockdone, ncols
                                * (j - 1) + xb, i);
                }
            }
        }

        // Add initial 0 to all blocklists
        // count the total number of lines (and 0's and -1's)

        C2JUtils.memset(blockdone, false, NBlocks);

        linetotal = 0;

        for (int i = 0; i < NBlocks; i++) {
            AddBlockLine(blocklists, blockcount, blockdone, i, 0);
            linetotal += blockcount[i];
        }

        // Create the blockmap lump

        // blockmaplump = malloc_IfSameLevel(blockmaplump, 4 + NBlocks +
        // linetotal);
        blockmaplump = new int[(4 + NBlocks + linetotal)];
        // blockmap header

        blockmaplump[0] = bmaporgx = xorg << FRACBITS;
        blockmaplump[1] = bmaporgy = yorg << FRACBITS;
        blockmaplump[2] = bmapwidth = ncols;
        blockmaplump[3] = bmapheight = nrows;

        // offsets to lists and block lists

        for (int i = 0; i < NBlocks; i++) {
            linelist_t bl = blocklists[i];
            int offs =
                blockmaplump[4 + i] = // set offset to block's list
                    (i != 0 ? blockmaplump[4 + i - 1] : 4 + NBlocks)
                            + (i != 0 ? blockcount[i - 1] : 0);

            // add the lines in each block's list to the blockmaplump
            // delete each list node as we go

            while (bl != null) {
                linelist_t tmp = bl.next;
                blockmaplump[offs++] = bl.num;
                bl = tmp;
            }
        }

        long b = System.nanoTime();

        System.err.printf("Blockmap generated in %f sec\n", (b - a) / 1e9);
        System.err.printf("Time spend in AddBlockLine : %f sec\n", total / 1e9);
    }

    // jff 10/6/98
    // End new code added to speed up calculation of internal blockmap

    //
    // P_VerifyBlockMap
    //
    // haleyjd 03/04/10: do verification on validity of blockmap.
    //
    protected boolean VerifyBlockMap(int count) {
        int x, y;
        int p_maxoffs = count;

        for (y = 0; y < bmapheight; y++) {
            for (x = 0; x < bmapwidth; x++) {
                int offset;
                int p_list;
                int tmplist;
                int blockoffset;

                offset = y * bmapwidth + x;
                blockoffset = offset + 4; // That's where the shit starts.

                // check that block offset is in bounds
                if (blockoffset >= p_maxoffs) {
                    System.err.printf("P_VerifyBlockMap: block offset overflow\n");
                    return false;
                }

                offset = blockmaplump[blockoffset];

                // check that list offset is in bounds
                if (offset < 4 || offset >= count) {
                    System.err.printf("P_VerifyBlockMap: list offset overflow\n");
                    return false;
                }

                p_list = offset;

                // scan forward for a -1 terminator before maxoffs
                for (tmplist = p_list;; tmplist++) {
                    // we have overflowed the lump?
                    if (tmplist >= p_maxoffs) {
                        System.err.printf("P_VerifyBlockMap: open blocklist\n");
                        return false;
                    }
                    if (blockmaplump[tmplist] == -1) // found -1
                        break;
                }

                // scan the list for out-of-range linedef indicies in list
                for (tmplist = p_list; blockmaplump[tmplist] != -1; tmplist++) {
                    if (blockmaplump[tmplist] < 0 || blockmaplump[tmplist] >= numlines) {
                        System.err.printf("P_VerifyBlockMap: index >= numlines\n");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // cph - convenient sub-function
    protected void AddLineToSector(line_t li, sector_t sector) {
        int[] bbox = sector.blockbox;

        sector.lines[sector.linecount++] = li;
        BBox.AddToBox(bbox, li.v1.x, li.v1.y);
        BBox.AddToBox(bbox, li.v2.x, li.v2.y);
    }

    /**
     * Compute density of reject table. Aids choosing LUT optimizations.
     * 
     * @return
     */

    protected float rejectDensity() {
        // float[] rowdensity=new float[numsectors];
        float tabledensity;
        int tcount = 0;

        for (int i = 0; i < numsectors; i++) {
            // int colcount=0;
            for (int j = 0; j < numsectors; j++) {
                // Determine subsector entries in REJECT table.
                int pnum = i * numsectors + j;
                int bytenum = pnum >> 3;
                int bitnum = 1 << (pnum & 7);

                // Check in REJECT table.
                if (!flags(rejectmatrix[bytenum], bitnum)) {
                    tcount++;
                    // colcount++;
                }
            }
            // rowdensity[i]=((float)colcount/numsectors);
        }

        tabledensity = (float) tcount / (numsectors * numsectors);
        return tabledensity;

    }

    protected static int[] POKE_REJECT = new int[] { 1, 2, 4, 8, 16, 32, 64,
            128 };

    /**
     * Updates Reject table dynamically based on what expensive LOS checks say.
     * It does decrease the "reject density" the longer the level runs, however
     * its by no means perfect, and results in many sleeping monsters. When
     * called, visibility between sectors x and y will be set to "false" for the
     * rest of the level, aka they will be rejected based on subsequent sight
     * checks.
     * 
     * @param x
     * @param y
     */

    protected void pokeIntoReject(int x, int y) {
        // Locate bit pointer e.g. for a 4x4 table, x=2 and y=3 give
        // 3*4+2=14
        final int pnum = y * numsectors + x;

        // Which byte?
        // 14= 1110 >>3 = 0001 so
        // Byte 0 Byte 1
        // xxxxxxxx xxxxxxxx
        // ^
        // 0.....bits......16
        // We are writing inside the second Byte 1
        final int bytenum = pnum >> 3;

        // OK, so how we pinpoint that one bit?
        // 1110 & 0111 = 0110 = 6 so it's the sixth bit
        // of the second byte
        final int bitnum = pnum & 7;

        // This sets only that one bit, and the reject lookup will be faster
        // next time.
        rejectmatrix[bytenum] |= POKE_REJECT[bitnum];

        System.out.println(rejectDensity());

    }

    protected void retrieveFromReject(int x, int y, boolean value) {
        // Locate bit pointer e.g. for a 4x4 table, x=2 and y=3 give
        // 3*4+2=14
        final int pnum = y * numsectors + x;

        // Which byte?
        // 14= 1110 >>3 = 0001 so
        // Byte 0 Byte 1
        // xxxxxxxx xxxxxxxx
        // ^
        // 0.....bits......16
        // We are writing inside the second Byte 1
        final int bytenum = pnum >> 3;

        // OK, so how we pinpoint that one bit?
        // 1110 & 0111 = 0110 = 6 so it's the sixth bit
        // of the second byte
        final int bitnum = pnum & 7;

        // This sets only that one bit, and the reject lookup will be faster
        // next time.
        rejectmatrix[bytenum] |= POKE_REJECT[bitnum];

        System.out.println(rejectDensity());

    }

    // Keeps track of lines that belong to a sector, to exclude e.g.
    // orphaned ones from the blockmap.
    protected boolean[] used_lines;

    // MAES: extensions to support 512x512 blockmaps.
    // They represent the maximum negative number which represents
    // a positive offset, otherwise they are left at -257, which
    // never triggers a check.
    // If a blockmap index is ever LE than either, then
    // its actual value is to be interpreted as 0x01FF&x.
    // Full 512x512 blockmaps get this value set to -1.
    // A 511x511 blockmap would still have a valid negative number
    // e.g. -1..510, so they would be set to -2
    
    public static final boolean FIX_BLOCKMAP_512 = Engine.getConfig().equals(Settings.fix_blockmap, Boolean.TRUE);
    public int blockmapxneg = -257;
    public int blockmapyneg = -257;

    /**
     * Returns an int[] array with orgx, orgy, and number of blocks. Order is:
     * orgx,orgy,bckx,bcky
     * 
     * @return
     */

    protected final int[] getMapBoundingBox(boolean playable) {

        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int maxy = Integer.MIN_VALUE;

        // Scan linedefs to detect extremes
        for (int i = 0; i < this.lines.length; i++) {

            if (playable || used_lines[i]) {
                if (lines[i].v1x > maxx) {
                    maxx = lines[i].v1x;
                }
                if (lines[i].v1x < minx) {
                    minx = lines[i].v1x;
                }
                if (lines[i].v1y > maxy) {
                    maxy = lines[i].v1y;
                }
                if (lines[i].v1y < miny) {
                    miny = lines[i].v1y;
                }
                if (lines[i].v2x > maxx) {
                    maxx = lines[i].v2x;
                }
                if (lines[i].v2x < minx) {
                    minx = lines[i].v2x;
                }
                if (lines[i].v2y > maxy) {
                    maxy = lines[i].v2y;
                }
                if (lines[i].v2y < miny) {
                    miny = lines[i].v2y;
                }
            }
        }

        System.err.printf("Map bounding %d %d %d %d\n", minx >> FRACBITS,
            miny >> FRACBITS, maxx >> FRACBITS, maxy >> FRACBITS);

        // Blow up bounding to the closest 128-sized block, adding 8 units as
        // padding.
        // This seems to be the "official" formula.
        int orgx = -BLOCKMAPPADDING + MAPBLOCKUNITS * (minx / MAPBLOCKUNITS);
        int orgy = -BLOCKMAPPADDING + MAPBLOCKUNITS * (miny / MAPBLOCKUNITS);
        int bckx = ((BLOCKMAPPADDING + maxx) - orgx);
        int bcky = ((BLOCKMAPPADDING + maxy) - orgy);

        System.err.printf("%d %d %d %d\n", orgx >> FRACBITS, orgy >> FRACBITS,
            1 + (bckx >> MAPBLOCKSHIFT), 1 + (bcky >> MAPBLOCKSHIFT));

        return new int[] { orgx, orgy, bckx, bcky };
    }

    protected void LoadReject(int lumpnum) {
        byte[] tmpreject = new byte[0];

        // _D_: uncommented the rejectmatrix variable, this permitted changing
        // level to work
        try {
            tmpreject = DOOM.wadLoader.CacheLumpNumAsRawBytes(lumpnum, PU_LEVEL);
        } catch (Exception e) {
            // Any exception at this point means missing REJECT lump. Fuck that,
            // and move on.
            // If everything goes OK, tmpreject will contain the REJECT lump's
            // data
            // BUT, alas, we're not done yet.
        }

        // Sanity check on matrix.
        // E.g. a 5-sector map will result in ceil(25/8)=4 bytes.
        // If the reject table is broken/corrupt, too bad. It will all be
        // zeroes.
        // Much better than overflowing.
        // TODO: build-in a REJECT-matrix rebuilder?
        rejectmatrix =
            new byte[(int) (Math
                    .ceil((this.numsectors * this.numsectors) / 8.0))];
        System.arraycopy(tmpreject, 0, rejectmatrix, 0,
            Math.min(tmpreject.length, rejectmatrix.length));

        // Do warn on atypical reject map lengths, but use either default
        // all-zeroes one,
        // or whatever you happened to read anyway.
        if (tmpreject.length < rejectmatrix.length) {
            System.err.printf("BROKEN REJECT MAP! Length %d expected %d\n",
                tmpreject.length, rejectmatrix.length);
        }

        // Maes: purely academic. Most maps are well above 0.68
        // System.out.printf("Reject table density: %f",rejectDensity());
    }
    
    /**
     * Added config switch to turn on/off support
     * 
     * Gets the proper blockmap block for a given X 16.16 Coordinate, sanitized
     * for 512-wide blockmaps. 
     * 
     * @param blockx
     * @return
     */
    @SourceCode.Compatible("blockx >> MAPBLOCKSHIFT")
    public final int getSafeBlockX(int blockx){
        blockx >>= MAPBLOCKSHIFT;
        return (FIX_BLOCKMAP_512 && blockx <= this.blockmapxneg) ? blockx & 0x1FF : blockx;
    }
    
    @SourceCode.Compatible("blockx >> MAPBLOCKSHIFT")
    public final int getSafeBlockX(long blockx) {
        blockx >>= MAPBLOCKSHIFT;
        return (int) ((FIX_BLOCKMAP_512 && blockx <= this.blockmapxneg) ? blockx & 0x1FF : blockx);
    }
    
    /** Gets the proper blockmap block for a given Y 16.16 Coordinate, sanitized
     *  for 512-wide blockmaps. 
     * 
     * @param blocky
     * @return     */

    
    @SourceCode.Compatible("blocky >> MAPBLOCKSHIFT")
    public final int getSafeBlockY(int blocky) {
        blocky >>= MAPBLOCKSHIFT;
        return (FIX_BLOCKMAP_512 && blocky <= this.blockmapyneg) ? blocky & 0x1FF : blocky;
    }

    @SourceCode.Compatible("blocky >> MAPBLOCKSHIFT")
    public final int getSafeBlockY(long blocky) {
        blocky >>= MAPBLOCKSHIFT;
        return (int) ((FIX_BLOCKMAP_512 && blocky <= this.blockmapyneg) ? blocky & 0x1FF : blocky);
    }

    /// Sector tag stuff, lifted off Boom
    
  /** Hash the sector tags across the sectors and linedefs.
   *  Call in SpawnSpecials.
   */
    
    public void InitTagLists()
    {
      int i;

      for (i=numsectors; --i>=0; )        // Initially make all slots empty.
        sectors[i].firsttag = -1;
      for (i=numsectors; --i>=0; )        // Proceed from last to first sector
        {                                 // so that lower sectors appear first
          int j = sectors[i].tag % numsectors; // Hash func
          sectors[i].nexttag = sectors[j].firsttag;   // Prepend sector to chain
          sectors[j].firsttag = i;
        }

      // killough 4/17/98: same thing, only for linedefs

      for (i=numlines; --i>=0; )        // Initially make all slots empty.
        lines[i].firsttag = -1;
      for (i=numlines; --i>=0; )        // Proceed from last to first linedef
        {                               // so that lower linedefs appear first
          int j = lines[i].tag % numlines; // Hash func
          lines[i].nexttag = lines[j].firsttag;   // Prepend linedef to chain
          lines[j].firsttag = i;
        }
    }
    
}
