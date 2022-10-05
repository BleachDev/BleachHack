package org.bleachhack.util.doom.savegame;

import static org.bleachhack.util.doom.data.Limits.*;

import org.bleachhack.util.doom.data.info;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.SourceCode.P_SaveG;
import static org.bleachhack.util.doom.doom.SourceCode.P_SaveG.P_ArchivePlayers;
import static org.bleachhack.util.doom.doom.SourceCode.P_SaveG.P_ArchiveSpecials;
import static org.bleachhack.util.doom.doom.SourceCode.P_SaveG.P_ArchiveThinkers;
import static org.bleachhack.util.doom.doom.SourceCode.P_SaveG.P_ArchiveWorld;
import static org.bleachhack.util.doom.doom.SourceCode.P_SaveG.P_UnArchivePlayers;
import static org.bleachhack.util.doom.doom.SourceCode.P_SaveG.P_UnArchiveSpecials;
import static org.bleachhack.util.doom.doom.SourceCode.P_SaveG.P_UnArchiveThinkers;
import static org.bleachhack.util.doom.doom.SourceCode.P_SaveG.P_UnArchiveWorld;
import org.bleachhack.util.doom.doom.player_t;
import org.bleachhack.util.doom.doom.thinker_t;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bleachhack.util.doom.m.Settings;
import org.bleachhack.util.doom.mochadoom.Engine;
import org.bleachhack.util.doom.mochadoom.Loggers;
import org.bleachhack.util.doom.p.*;
import org.bleachhack.util.doom.p.Actions.ActionsLights.glow_t;
import org.bleachhack.util.doom.p.Actions.ActionsLights.lightflash_t;
import static org.bleachhack.util.doom.p.ActiveStates.*;

import org.bleachhack.util.doom.rr.line_t;
import org.bleachhack.util.doom.rr.sector_t;
import org.bleachhack.util.doom.rr.side_t;
import org.bleachhack.util.doom.utils.C2JUtils;

public class VanillaDSG<T, V> implements IDoomSaveGame {

    VanillaDSGHeader header;
    final DoomMain<T, V> DOOM;

    public VanillaDSG(DoomMain<T, V> DOOM) {
        this.DOOM = DOOM;
    }

    @Override
    public void setThinkerList(ThinkerList li) {
        // TODO Auto-generated method stub

    }

    @Override
    public IDoomSaveGameHeader getHeader() {
        return header;
    }

    @Override
    public void setHeader(IDoomSaveGameHeader header) {
        this.header = (VanillaDSGHeader) header;

    }

    private DataInputStream f;
    private DataOutputStream fo;
    private int maxsize;

    @Override
    public boolean doLoad(DataInputStream f) {
        try {
            this.f = f;
            maxsize = f.available();
            System.out.println("Max size " + maxsize);
            this.header = new VanillaDSGHeader();
            header.read(f);
            UnArchivePlayers();
            UnArchiveWorld();
            UnArchiveThinkers();
            UnArchiveSpecials();
            byte terminator = f.readByte();
            return terminator == 0x1D;
        } catch (IOException e) {
            Loggers.getLogger(VanillaDSG.class.getName()).log(Level.WARNING, e, () -> 
                String.format("Error while loading savegame! Cause: %s", e.getMessage()));
            return false; // Needed to shut up compiler.
        }

    }

    /**
     * P_UnArchivePlayers
     *
     * @throws IOException
     */
    @P_SaveG.C(P_UnArchivePlayers)
    protected void UnArchivePlayers() throws IOException {
        int i;
        int j;

        for (i = 0; i < MAXPLAYERS; i++) {
            // Multiplayer savegames are different!
            if (!DOOM.playeringame[i]) {
                continue;
            }
            PADSAVEP(f, maxsize); // this will move us on the 52th byte, instead of 50th.
            DOOM.players[i].read(f);

            //memcpy (&players[i],save_p, sizeof(player_t));
            //save_p += sizeof(player_t);
            // will be set when unarc thinker
            DOOM.players[i].mo = null;
            DOOM.players[i].message = null;
            DOOM.players[i].attacker = null;

            for (j = 0; j < player_t.NUMPSPRITES; j++) {
                if (C2JUtils.eval(DOOM.players[i].psprites[j].state)) {
                    // MAES HACK to accomoadate state_t type punning a-posteriori
                    DOOM.players[i].psprites[j].state
                        = info.states[DOOM.players[i].psprites[j].readstate];
                }
            }
        }
    }

    /**
     * P_ArchivePlayers
     *
     * @throws IOException
     */
    @P_SaveG.C(P_ArchivePlayers)
    protected void ArchivePlayers() throws IOException {
        for (int i = 0; i < MAXPLAYERS; i++) {
            // Multiplayer savegames are different!
            if (!DOOM.playeringame[i]) {
                continue;
            }

            PADSAVEP(fo); // this will move us on the 52th byte, instead of 50th.

            // State will have to be serialized when saving.
            DOOM.players[i].write(fo);

            //System.out.printf("Player %d has mobj hashcode %d",(1+i),DS.players[i].mo.hashCode());
        }
    }

    //
    //P_ArchiveWorld
    //
    @P_SaveG.C(P_ArchiveWorld)
    protected void ArchiveWorld() throws IOException {
        int i;
        int j;
        sector_t sec;
        line_t li;
        side_t si;

        // do sectors (allocate 14 bytes per sector)
        ByteBuffer buffer = ByteBuffer.allocate(DOOM.levelLoader.numsectors * 14);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        deAdaptSectors();
        for (i = 0; i < DOOM.levelLoader.numsectors; i++) {
            sec = DOOM.levelLoader.sectors[i];
            // MAES: sectors are actually carefully
            // marshalled, so we don't just read/write
            // their entire memory footprint to disk.
            sec.pack(buffer);
        }

        adaptSectors();
        fo.write(buffer.array(), 0, buffer.position());

        // do lines 
        // Allocate for the worst-case scenario (6+20 per line)
        buffer = ByteBuffer.allocate(DOOM.levelLoader.numlines * (6 + 20));
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(0);

        //final side_t test1=new side_t(0x11111111,0x11111111,(short) 0x1111,(short)0x1111,(short)0x1111,null);
        //final side_t test2=new side_t(0x22222222,0x22222222,(short) 0x2222,(short)0x2222,(short)0x2222,null);
        for (i = 0; i < DOOM.levelLoader.numlines; i++) {
            li = DOOM.levelLoader.lines[i];
            li.pack(buffer);

            for (j = 0; j < 2; j++) {
                if (li.sidenum[j] == line_t.NO_INDEX) {
                    continue;
                }
                si = DOOM.levelLoader.sides[li.sidenum[j]];
                si.pack(buffer);
                //if (j==0) test1.pack(buffer);
                //else test2.pack(buffer);

            }
        }

        int write = buffer.position();
        fo.write(buffer.array(), 0, write);
    }

    //
    //P_UnArchiveWorld
    //
    @P_SaveG.C(P_UnArchiveWorld)
    protected final void UnArchiveWorld() throws IOException {
        int i;
        int j;
        sector_t sec;
        line_t li;
        side_t si;
        // short      get;
        //get = (short *)save_p;

        //List<sector_t> sectors=new ArrayList<sector_t>();
        // do sectors
        for (i = 0; i < DOOM.levelLoader.numsectors; i++) {
            sec = DOOM.levelLoader.sectors[i];
            // MAES: sectors were actually carefully
            // unmarshalled, so we don't just read/write
            // their entire memory footprint to disk.
            sec.read(f);
            sec.specialdata = null;
            sec.soundtarget = null;
        }
        adaptSectors();
        // do lines
        for (i = 0; i < DOOM.levelLoader.numlines; i++) {
            li = DOOM.levelLoader.lines[i];
            // MAES: something similar occurs with lines, too.
            li.read(f);
            //System.out.println("Line "+i+": "+li);
            //System.out.print(i+ " {");
            for (j = 0; j < 2; j++) {
                //  System.out.print(li.sidenum[j]);
                //  if (j<2) System.out.print(",");
                //   System.out.printf("Skipped sidenum %d for line %d\n",j,i);
                if (li.sidenum[j] == line_t.NO_INDEX) {
                    //        System.out.printf("Skipped sidenum %d for line %d\n",j,i);
                    continue;
                }
                // Similarly, sides also get a careful unmarshalling even
                // in vanilla. No "dumb" block reads here.
                si = DOOM.levelLoader.sides[li.sidenum[j]];
                si.read(f);

            }
            //System.out.printf("Position at end of WORLD: %d\n",f.getFilePointer());
        }

    }

    /**
     * Convert loaded sectors from vanilla savegames into the internal,
     * continuous index progression, by intercepting breaks corresponding to markers.
     */
    protected void adaptSectors() {
        sector_t sec;
        switch (DOOM.getGameMode()) {
            case registered:
            case shareware:
                for (int i = 0; i < DOOM.levelLoader.numsectors; i++) {
                    sec = DOOM.levelLoader.sectors[i];
                    // Between the F1_START and F1_END mark (in vanilla)
                    if (sec.floorpic <= 54) {
                        sec.floorpic -= 1;
                    } else {
                        // Between the F2_START and F2_END mark (in vanilla)
                        sec.floorpic -= 3;
                    }
                    if (sec.ceilingpic <= 54) {
                        sec.ceilingpic -= 1;
                    } else {
                        // Between the F2_START and F2_END mark (in vanilla)
                        sec.ceilingpic -= 3;
                    }

                }
                break;
            case commercial:
            case pack_plut:
            case pack_tnt:
                for (int i = 0; i < DOOM.levelLoader.numsectors; i++) {
                    sec = DOOM.levelLoader.sectors[i];
                    // Between the F1_START and F1_END mark (in vanilla)
                    if (sec.floorpic <= 54) {
                        sec.floorpic -= 1;
                    } else if (sec.floorpic <= 99) {
                        // Between the F2_START and F2_END mark (in vanilla)
                        sec.floorpic -= 3;
                    } else {
                        sec.floorpic -= 5;
                    }

                    if (sec.ceilingpic <= 54) {
                        sec.ceilingpic -= 1;
                    } else if (sec.ceilingpic <= 99) {
                        // Between the F2_START and F2_END mark (in vanilla)
                        sec.ceilingpic -= 3;
                    } else {
                        sec.ceilingpic -= 5;
                    }

                }
            default:
            	break;
        }
    }

    /**
     * De-convert sectors from an absolute to a vanilla-like index
     * progression, by adding proper skips
     */
    protected void deAdaptSectors() {
        sector_t sec;
        switch (DOOM.getGameMode()) {
            case registered:
            case shareware:
                for (int i = 0; i < DOOM.levelLoader.numsectors; i++) {
                    sec = DOOM.levelLoader.sectors[i];
                    // Between the F1_START and F1_END mark (in vanilla)
                    if (sec.floorpic < 54) {
                        sec.floorpic += 1;
                    } else {
                        // Between the F2_START and F2_END mark (in vanilla)
                        sec.floorpic += 3;
                    }
                    if (sec.ceilingpic < 54) {
                        sec.ceilingpic += 1;
                    } else {
                        // Between the F2_START and F2_END mark (in vanilla)
                        sec.ceilingpic += 3;
                    }

                }
                break;
            case commercial:
            case pack_plut:
            case pack_tnt:
                for (int i = 0; i < DOOM.levelLoader.numsectors; i++) {
                    sec = DOOM.levelLoader.sectors[i];
                    // Between the F1_START and F1_END mark (in vanilla)
                    if (sec.floorpic < 54) {
                        sec.floorpic += 1;
                    } else if (sec.floorpic < 99) {
                        // Between the F2_START and F2_END mark (in vanilla)
                        sec.floorpic += 3;
                    } else {
                        sec.floorpic += 5;
                    }

                    if (sec.ceilingpic < 54) {
                        sec.ceilingpic += 1;
                    } else if (sec.ceilingpic < 99) {
                        // Between the F2_START and F2_END mark (in vanilla)
                        sec.ceilingpic += 3;
                    } else {
                        sec.ceilingpic += 5;
                    }

                }
            default:
            	break;
        }
    }

    //
    //Thinkers
    //
    protected enum thinkerclass_t {
        tc_end,
        tc_mobj;
    }

    List<mobj_t> TL = new ArrayList<>();

    //
    //P_ArchiveThinkers
    //
    @P_SaveG.C(P_ArchiveThinkers)
    protected void ArchiveThinkers() throws IOException {
        thinker_t th;
        mobj_t mobj;

        // save off the current thinkers
        for (th = DOOM.actions.getThinkerCap().next; th != DOOM.actions.getThinkerCap(); th = th.next) {
            if (th.thinkerFunction == P_MobjThinker) {
                // Indicate valid thinker
                fo.writeByte(thinkerclass_t.tc_mobj.ordinal());
                // Pad...
                PADSAVEP(fo);
                mobj = (mobj_t) th;
                mobj.write(fo);

                // MAES: state is explicit in state.id
                // save_p += sizeof(*mobj);
                // mobj->state = (state_t *)(mobj->state - states);
                // MAES: player is automatically generated at runtime and handled by the writer.
                //if (mobj->player)
                //mobj->player = (player_t *)((mobj->player-players) + 1);
            }

        // I_Error ("P_ArchiveThinkers: Unknown thinker function");
        }

        // add a terminating marker
        fo.writeByte(thinkerclass_t.tc_end.ordinal());

    }

    //
    //P_UnArchiveThinkers
    //
    @P_SaveG.C(P_UnArchiveThinkers)
    protected void UnArchiveThinkers() throws IOException {
        thinkerclass_t tclass; // was "byte", therefore unsigned
        thinker_t currentthinker;
        thinker_t next;
        mobj_t mobj;
        int id = 0;

        // remove all the current thinkers
        currentthinker = DOOM.actions.getThinkerCap().next;
        while (currentthinker != null && currentthinker != DOOM.actions.getThinkerCap()) {
            next = currentthinker.next;

            if (currentthinker.thinkerFunction == P_MobjThinker) {
                DOOM.actions.RemoveMobj((mobj_t) currentthinker);
            }// else {
                //currentthinker.next.prev=currentthinker.prev;
                //currentthinker.prev.next=currentthinker.next;
                //currentthinker = null;
            //}

            currentthinker = next;
        }

        DOOM.actions.InitThinkers();

        // read in saved thinkers
        boolean end = false;
        while (!end) {
            int tmp = f.readUnsignedByte();
            tclass = thinkerclass_t.values()[tmp];
            switch (tclass) {
                case tc_end:
                    // That's how we know when to stop.
                    end = true;
                    break;     // end of list

                case tc_mobj:
                    PADSAVEP(f, maxsize);
                    mobj = mobj_t.createOn(DOOM);
                    mobj.read(f);
                    mobj.id = ++id;
                    TL.add(mobj);
                    mobj.mobj_state = info.states[mobj.stateid];
                    mobj.target = null;
                    if (mobj.playerid != 0) {
                        mobj.player = DOOM.players[mobj.playerid - 1];
                        mobj.player.mo = mobj;

                    }
                    DOOM.levelLoader.SetThingPosition(mobj);
                    mobj.info = info.mobjinfo[mobj.type.ordinal()];
                    mobj.floorz = mobj.subsector.sector.floorheight;
                    mobj.ceilingz = mobj.subsector.sector.ceilingheight;
                    mobj.thinkerFunction = P_MobjThinker;
                    DOOM.actions.AddThinker(mobj);
                    break;

                default:
                    DOOM.doomSystem.Error("Unknown tclass %d in savegame", tclass);
            }
        }
        
        if (Engine.getConfig().equals(Settings.reconstruct_savegame_pointers, Boolean.TRUE)) {
            reconstructPointers();
            rewirePointers();
        }
    }

    final HashMap<Integer, mobj_t> pointindex = new HashMap<>();

    /**
     * Allows reconstructing infighting targets from stored pointers/indices.
     * Works even with vanilla savegames as long as whatever it is that you
     * store is unique. A good choice would be progressive indices or hash values.
     *
     */
    protected void reconstructPointers() {

        int player = 0;

        for (mobj_t th : TL) {

            if (th.player != null) {
                player = th.id;
                // Player found, so that's our first key.
                pointindex.put(th.player.p_mobj, th);
            }
        }

        if (player == 0) {
            Loggers.getLogger(VanillaDSG.class.getName()).log(Level.WARNING,
                "Player not found, cannot reconstruct pointers!");
            return;
        }

        int curr; // next or prev index

        // We start from the player's index, if found.
        // We subtract -1 so it matches that inside the thinkers list.
        for (int i = (player - 1); i < TL.size() - 1; i++) {
            // Get "next" pointer.
            curr = TL.get(i).nextid;
            pointindex.put(curr, TL.get(i + 1));
        }

        // We also search backwards, in case player wasn't first object
        // (can this even happen, in vanilla?)
        // -1 so it matches that of the TL list.
        for (int i = (player - 1); i > 0; i--) {
            // Get "prev" pointer.
            curr = TL.get(i).previd;
            pointindex.put(curr, TL.get(i - 1));
        }

    }

    /**
     * Allows reconstructing infighting targets from stored pointers/indices from
     * the hashtable created by reconstructPointers.
     *
     */
    protected void rewirePointers() {
        TL.forEach(th -> {
            if (th.p_target != 0) {
                th.target = pointindex.get(th.p_target);
                th.tracer = pointindex.get(th.p_tracer);
                // System.out.printf("Object %s has target %s\n",th.type.toString(),th.target.type.toString());
            }
        });
    }

    protected enum specials_e {
        tc_ceiling,
        tc_door,
        tc_floor,
        tc_plat,
        tc_flash,
        tc_strobe,
        tc_glow,
        tc_endspecials

    };

    //
    //P_ArchiveSpecials
    //
    @P_SaveG.C(P_ArchiveSpecials)
    protected void ArchiveSpecials() throws IOException {
        ceiling_t ceiling;
        vldoor_t door;
        floormove_t floor;
        plat_t plat;
        lightflash_t flash;
        strobe_t strobe;
        glow_t glow;
        int i;

        // Most of these objects are quite hefty, but estimating 128 bytes tops
        // for each should do (largest one is 56);
        ByteBuffer buffer = ByteBuffer.allocate(128);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // save off the current thinkers
        for (thinker_t th = DOOM.actions.getThinkerCap().next; th != DOOM.actions.getThinkerCap(); th = th.next) {

            // Write out any pending objects.
            if (buffer.position() > 0) {
                fo.write(buffer.array(), 0, buffer.position());
                //System.out.println("Wrote out "+buffer.position()+" bytes");

            }

            // Back to the beginning.
            buffer.position(0);

            // So ceilings don't think?
            if (th.thinkerFunction == NOP) {
                // i maintains status between iterations
                for (i = 0; i < DOOM.actions.getMaxCeilings(); i++) {
                    if ((th instanceof ceiling_t) && (DOOM.actions.getActiveCeilings()[i] == (ceiling_t) th)) {
                        break;
                    }
                }

                if (i < MAXCEILINGS) {
                    fo.writeByte(specials_e.tc_ceiling.ordinal());
                    PADSAVEP(fo);
                    // Set id for saving        
                    ceiling = (ceiling_t) th;
                    ceiling.sectorid = ceiling.sector.id;
                    ceiling.pack(buffer);
                }
                continue;
            }

            // Well, apparently some do.
            if (th.thinkerFunction == T_MoveCeiling) {

                fo.writeByte(specials_e.tc_ceiling.ordinal());
                PADSAVEP(fo);
                ceiling = (ceiling_t) th;
                ceiling.sectorid = ceiling.sector.id;
                ceiling.pack(buffer);
                continue;
            }

            // Well, apparently some do.
            if (th.thinkerFunction == T_VerticalDoor) {

                fo.writeByte(specials_e.tc_door.ordinal());
                PADSAVEP(fo);
                door = (vldoor_t) th;
                door.sectorid = door.sector.id;
                door.pack(buffer);
                continue;
            }

            // Well, apparently some do.
            if (th.thinkerFunction == T_MoveFloor) {
                fo.writeByte(specials_e.tc_floor.ordinal());
                PADSAVEP(fo);
                floor = (floormove_t) th;
                floor.sectorid = floor.sector.id;
                floor.pack(buffer);
                continue;
            }

            // Well, apparently some do.
            if (th.thinkerFunction == T_PlatRaise) {
                fo.writeByte(specials_e.tc_plat.ordinal());
                PADSAVEP(fo);
                plat = (plat_t) th;
                plat.sectorid = plat.sector.id;
                plat.pack(buffer);
                continue;
            }

            // Well, apparently some do.
            if (th.thinkerFunction == T_LightFlash) {
                fo.writeByte(specials_e.tc_flash.ordinal());
                PADSAVEP(fo);
                flash = (lightflash_t) th;
                flash.sectorid = flash.sector.id;
                flash.pack(buffer);
                continue;
            }

            // Well, apparently some do.
            if (th.thinkerFunction == T_StrobeFlash) {
                fo.writeByte(specials_e.tc_strobe.ordinal());
                PADSAVEP(fo);
                strobe = (strobe_t) th;
                strobe.sectorid = strobe.sector.id;
                strobe.pack(buffer);
                continue;
            }

            // Well, apparently some do.
            if (th.thinkerFunction == T_Glow) {
                fo.writeByte(specials_e.tc_glow.ordinal());
                PADSAVEP(fo);
                glow = (glow_t) th;
                glow.sectorid = glow.sector.id;
                glow.pack(buffer);
            }
        }

        if (buffer.position() > 0) {
            fo.write(buffer.array(), 0, buffer.position());
        }

        // Finito!
        fo.writeByte((byte) specials_e.tc_endspecials.ordinal());
    }

    //
    //P_UnArchiveSpecials
    //
    @P_SaveG.C(P_UnArchiveSpecials)
    protected void UnArchiveSpecials() throws IOException {
        specials_e tclass;
        ceiling_t ceiling;
        vldoor_t door;
        floormove_t floor;
        plat_t plat;
        lightflash_t flash;
        strobe_t strobe;
        glow_t glow;

        //List<thinker_t> A=new ArrayList<thinker_t>();
        DOOM.actions.ClearPlatsBeforeLoading();
        DOOM.actions.ClearCeilingsBeforeLoading();

        // read in saved thinkers
        while (true) {
            int tmp = f.readUnsignedByte();
            //tmp&=0x00ff; // To "unsigned byte"
            tclass = specials_e.values()[tmp];

            switch (tclass) {
                case tc_endspecials:
                    return; // end of list

                case tc_ceiling:
                    PADSAVEP(f, maxsize);
                    ceiling = new ceiling_t();
                    ceiling.read(f);
                    ceiling.sector = DOOM.levelLoader.sectors[ceiling.sectorid];
                    ceiling.sector.specialdata = ceiling;

                    if (ceiling.functionid != 0) {
                        ceiling.thinkerFunction = T_MoveCeiling;
                    }

                    DOOM.actions.AddThinker(ceiling);
                    DOOM.actions.AddActiveCeiling(ceiling);
                    break;

                case tc_door:
                    PADSAVEP(f, maxsize);
                    door = new vldoor_t();
                    door.read(f);
                    door.sector = DOOM.levelLoader.sectors[door.sectorid];
                    door.sector.specialdata = door;
                    door.thinkerFunction = T_VerticalDoor;

                    DOOM.actions.AddThinker(door);
                    break;

                case tc_floor:
                    PADSAVEP(f, maxsize);
                    floor = new floormove_t();
                    floor.read(f);
                    floor.sector = DOOM.levelLoader.sectors[floor.sectorid];
                    floor.sector.specialdata = floor;
                    floor.thinkerFunction = T_MoveFloor;

                    DOOM.actions.AddThinker(floor);
                    break;

                case tc_plat:
                    PADSAVEP(f, maxsize);
                    plat = new plat_t();
                    plat.read(f);
                    plat.sector = DOOM.levelLoader.sectors[plat.sectorid];
                    plat.sector.specialdata = plat;

                    if (plat.functionid != 0) {
                        plat.thinkerFunction = T_PlatRaise;
                    }

                    DOOM.actions.AddThinker(plat);
                    DOOM.actions.AddActivePlat(plat);
                    break;

                case tc_flash:
                    PADSAVEP(f, maxsize);
                    flash = new lightflash_t();
                    flash.read(f);
                    flash.sector = DOOM.levelLoader.sectors[flash.sectorid];
                    flash.thinkerFunction = T_LightFlash;

                    DOOM.actions.AddThinker(flash);
                    break;

                case tc_strobe:
                    PADSAVEP(f, maxsize);
                    strobe = new strobe_t();
                    strobe.read(f);
                    strobe.sector = DOOM.levelLoader.sectors[strobe.sectorid];
                    strobe.thinkerFunction = T_StrobeFlash;

                    DOOM.actions.AddThinker(strobe);
                    break;

                case tc_glow:
                    PADSAVEP(f, maxsize);
                    glow = new glow_t();
                    glow.read(f);
                    glow.sector = DOOM.levelLoader.sectors[glow.sectorid];
                    glow.thinkerFunction = T_Glow;

                    DOOM.actions.AddThinker(glow);
                    break;

                default:
                    DOOM.doomSystem.Error("P_UnarchiveSpecials:Unknown tclass %d in savegame", tmp);
            }
        }

    }

    /**
     * Pads save_p to a 4-byte boundary
     * so that the load/save works on SGI&Gecko.
     *
     * @param save_p
     */
    protected final int PADSAVEP(int save_p) {
        return (save_p + ((4 - (save_p & 3)) & 3));
    }

    //protected final int PADSAVEP(ByteBuffer b, int save_p){
    //    ByteBuffer
    //    return (save_p += (4 - ((int) save_p & 3)) & 3);
    //}
    protected final long PADSAVEP(DataInputStream f, int maxsize) throws IOException {
        long save_p = maxsize - f.available();
        int padding = (4 - ((int) save_p & 3)) & 3;
        // System.out.printf("Current position %d Padding by %d bytes %d\n",save_p,padding,maxsize);        
        f.skip(padding);
        return padding;
    }

    protected final long PADSAVEP(DataOutputStream f) throws IOException {
        long save_p = f.size();
        int padding = (4 - ((int) save_p & 3)) & 3;
        // System.out.printf("Current position %d Padding by %d bytes\n",save_p,padding);
        for (int i = 0; i < padding; i++) {
            f.write(0);
        }
        return padding;
    }

    @Override
    public boolean doSave(DataOutputStream f) {
        try {
            // The header must have been set, at this point.
            this.fo = f;
            //f.setLength(0); // Kill old info.
            header.write(f);

            //header.read(f);
            ArchivePlayers();
            ArchiveWorld();
            ArchiveThinkers();
            ArchiveSpecials();
            // TODO: the rest...
            f.write(0x1D);
        } catch (IOException e) {
            Loggers.getLogger(VanillaDSG.class.getName()).log(Level.WARNING, e, () -> 
                String.format("Error while saving savegame! Cause: %s", e.getMessage()));
            return false; // Needed to shut up compiler.
        }
        return true;
    }

}
