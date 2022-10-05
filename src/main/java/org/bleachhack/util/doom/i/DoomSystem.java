// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: DoomSystem.java,v 1.18 2013/06/04 11:29:39 velktron Exp $
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
// $Log: DoomSystem.java,v $
// Revision 1.18  2013/06/04 11:29:39  velktron
// Shut up logger
//
// Revision 1.17  2013/06/03 10:54:11  velktron
// System interface allows for a logging subsystem, but implementation details may very well vary.
//
// Revision 1.16.2.1  2012/11/19 22:14:35  velktron
// Sync tooling shutdown.
//
// Revision 1.16  2012/11/06 16:05:29  velktron
// Variables manager now part of Main.
//
// Revision 1.15  2012/09/24 17:16:22  velktron
// Massive merge between HiColor and HEAD. There's no difference from now on, and development continues on HEAD.
//
// Revision 1.14.2.2  2012/09/17 15:57:53  velktron
// Aware of IVariablesManager
//
// Revision 1.14.2.1  2012/06/14 22:38:06  velktron
// Update to handle new disk flasher
//
// Revision 1.14  2011/10/24 02:11:27  velktron
// Stream compliancy
//
// Revision 1.13  2011/09/29 15:16:04  velktron
// Modal popup generation moved here.
//
// Revision 1.12  2011/06/12 21:54:31  velktron
// Separate music + sound closing.
//
// Revision 1.11  2011/06/05 22:52:28  velktron
// Proper audio subsystem shutdown.
//
// Revision 1.10  2011/05/29 22:15:32  velktron
// Introduced IRandom interface.
//
// Revision 1.9  2011/05/26 17:56:32  velktron
// Removed ticker functionality, moved to ITicker interface.
//
// Revision 1.8  2011/05/18 16:53:29  velktron
// Implements IDoomSystem now.
//
// Revision 1.7  2011/05/17 16:54:09  velktron
// Switched to DoomStatus
//
// Revision 1.6  2011/05/13 17:44:24  velktron
// Global error function, shutdown on demos.
//
// Revision 1.5  2011/02/11 00:11:13  velktron
// A MUCH needed update to v1.3.
//
// Revision 1.4  2010/12/15 16:12:19  velktron
// Changes in Wiper code and alternate timing method, hoping to fix the Athlon X2
//
// Revision 1.3  2010/09/24 17:58:39  velktron
// Menus and HU  functional -mostly.
//
// Revision 1.2  2010/09/23 20:36:45  velktron
// *** empty log message ***
//
// Revision 1.1  2010/09/23 15:11:57  velktron
// A bit closer...
//
// Revision 1.3  2010/09/07 16:23:00  velktron
// *** empty log message ***
//
// Revision 1.2  2010/08/30 15:53:19  velktron
// Screen wipes work...Finale coded but untested.
// GRID.WAD included for testing.
//
// Revision 1.1  2010/06/30 08:58:50  velktron
// Let's see if this stuff will finally commit....
//
//
// Most stuff is still  being worked on. For a good place to start and get an idea of what is being done, I suggest checking out the "testers" package.
//
// Revision 1.1  2010/06/29 11:07:34  velktron
// Release often, release early they say...
//
// Commiting ALL stuff done so far. A lot of stuff is still broken/incomplete, and there's still mixed C code in there. I suggest you load everything up in Eclpise and see what gives from there.
//
// A good place to start is the testers/ directory, where you  can get an idea of how a few of the implemented stuff works.
//
//
// DESCRIPTION:
//
//-----------------------------------------------------------------------------
package org.bleachhack.util.doom.i;

import org.bleachhack.module.mods.Doom;
import org.bleachhack.util.doom.awt.MsgBox;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.ticcmd_t;

import java.io.IOException;

public class DoomSystem implements IDoomSystem {

    public static void MiscError(String error, Object... args) {
        System.err.print("Error: ");
        System.err.print(error);
        System.err.print("\n");
    }

    static int mb_used = 6;

    // Even the SYSTEM needs to know about DOOM!!!!
    private final DoomMain<?, ?> DM;
    private final ticcmd_t emptycmd;

    public DoomSystem(DoomMain<?, ?> DM) {
        this.DM = DM;
        this.emptycmd = new ticcmd_t();
    }

    @Override
    public void Tactile(int on, int off, int total) {
        // UNUSED.
        on = off = total = 0;
    }

    @Override
    public ticcmd_t BaseTiccmd() {
        return emptycmd;
    }

    @Override
    public int GetHeapSize() {
        return mb_used * 1024 * 1024;
    }

    @Override
    public byte[] ZoneBase(int size) {
        return (new byte[mb_used * 1024 * 1024]);
    }

    //
    //I_Quit
    //
    @Override
    public void Quit() {
        //DM.CheckDemoStatus();
        /*try {
            DM.QuitNetGame();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
        //DM.debugEnd();
        /**
         * In Java we are not locked for exit until everything is shut down
         */
        //DM.soundDriver.ShutdownSound();
        //DM.music.ShutdownMusic();
        //DM.commit();
        //DM.CM.SaveDefaults();
        Doom.running = false;
    }

    /**
     * I_Init
     */
    @Override
    public void Init() {
        //TODO: InitSound();
        //TODO: InitGraphics();
    }

    @Override
    public void WaitVBL(int count) {
        try {
            Thread.sleep(count * 1000 / 70);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void BeginRead() {
        if (DM.diskDrawer != null) {
            if (!DM.diskDrawer.isReading()) {
                // Set 8 tick reading time
                DM.diskDrawer.setReading(8);
            }
        }

    }

    @Override
    public void EndRead() {
    }

    @Override
    public void AllocLow(int length) {
        ; // Dummy
    }

    //
    // I_Error
    //
    @Override
    public void Error(String error, Object... args) {

        System.err.print("Error: ");
        System.err.printf(error, args);
        System.err.print("\n");
        //va_end (argptr);

        //fflush( stderr );
        // Shutdown. Here might be other errors.
        if (DM.demorecording) {
            DM.CheckDemoStatus();
        }

        try {
            DM.QuitNetGame();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // DM.VI.ShutdownGraphics();

        System.exit(-1);
    }

    @Override
    public void Error(String error) {
        //va_list	argptr;

        // Message first.
        //va_start (argptr,error);
        System.err.print("Error: ");
        System.err.printf(error);
        System.err.print("\n");
        //va_end (argptr);

        //fflush( stderr );
        // Shutdown. Here might be other errors.
        //if (demorecording)
        //G_CheckDemoStatus();
        //D_QuitNetGame ();
        //I_ShutdownGraphics();
        System.exit(-1);
    }

    // This particular implementation will generate a popup box.// 
    @Override
    public boolean GenerateAlert(String title, String cause) {
        MsgBox alert = new MsgBox(null, title, cause, true);
        return alert.isOk();
    }
}
