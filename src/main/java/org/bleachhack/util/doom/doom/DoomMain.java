package org.bleachhack.util.doom.doom;

import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.Doom;
import org.bleachhack.util.doom.automap.IAutoMap;
import static org.bleachhack.util.doom.data.Defines.*;
import static org.bleachhack.util.doom.data.Limits.*;

import org.bleachhack.util.doom.data.Tables;
import static org.bleachhack.util.doom.data.Tables.*;
import org.bleachhack.util.doom.data.dstrings;
import static org.bleachhack.util.doom.data.dstrings.*;
import static org.bleachhack.util.doom.data.info.mobjinfo;
import static org.bleachhack.util.doom.data.info.states;
import org.bleachhack.util.doom.data.mapthing_t;
import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.data.sounds.musicenum_t;
import org.bleachhack.util.doom.data.sounds.sfxenum_t;
import org.bleachhack.util.doom.defines.*;
import static org.bleachhack.util.doom.defines.gamestate_t.*;
import org.bleachhack.util.doom.demo.IDemoTicCmd;
import org.bleachhack.util.doom.demo.VanillaDoomDemo;
import org.bleachhack.util.doom.demo.VanillaTiccmd;
import static org.bleachhack.util.doom.doom.NetConsts.*;
import org.bleachhack.util.doom.doom.SourceCode.CauseOfDesyncProbability;
import org.bleachhack.util.doom.doom.SourceCode.D_Main;
import static org.bleachhack.util.doom.doom.SourceCode.D_Main.*;
import org.bleachhack.util.doom.doom.SourceCode.G_Game;
import static org.bleachhack.util.doom.doom.SourceCode.G_Game.*;
import static org.bleachhack.util.doom.doom.englsh.*;
import static org.bleachhack.util.doom.doom.evtype_t.*;
import static org.bleachhack.util.doom.doom.gameaction_t.*;
import org.bleachhack.util.doom.f.EndLevel;
import org.bleachhack.util.doom.f.Finale;
import org.bleachhack.util.doom.f.Wiper;
import static org.bleachhack.util.doom.g.Signals.ScanCode.*;
import org.bleachhack.util.doom.hu.HU;
import org.bleachhack.util.doom.i.DiskDrawer;
import org.bleachhack.util.doom.i.DoomSystem;
import org.bleachhack.util.doom.i.IDiskDrawer;
import org.bleachhack.util.doom.i.IDoomSystem;
import org.bleachhack.util.doom.i.Strings;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import org.bleachhack.util.doom.m.DelegateRandom;
import org.bleachhack.util.doom.m.IDoomMenu;
import org.bleachhack.util.doom.m.Menu;
import org.bleachhack.util.doom.m.MenuMisc;
import org.bleachhack.util.doom.m.Settings;
import static org.bleachhack.util.doom.m.fixed_t.FRACBITS;
import static org.bleachhack.util.doom.m.fixed_t.MAPFRACUNIT;
import org.bleachhack.util.doom.mochadoom.Engine;
import org.bleachhack.util.doom.n.DoomSystemNetworking;
import org.bleachhack.util.doom.n.DummyNetworkDriver;
import org.bleachhack.util.doom.p.AbstractLevelLoader;
import org.bleachhack.util.doom.p.ActionFunctions;
import org.bleachhack.util.doom.p.BoomLevelLoader;
import org.bleachhack.util.doom.p.mobj_t;
import org.bleachhack.util.doom.rr.ISpriteManager;
import org.bleachhack.util.doom.rr.SceneRenderer;
import org.bleachhack.util.doom.rr.SpriteManager;
import org.bleachhack.util.doom.rr.TextureManager;
import org.bleachhack.util.doom.rr.ViewVars;
import org.bleachhack.util.doom.rr.patch_t;
import org.bleachhack.util.doom.rr.subsector_t;
import org.bleachhack.util.doom.s.IDoomSound;
import org.bleachhack.util.doom.s.IMusic;
import org.bleachhack.util.doom.s.ISoundDriver;
import org.bleachhack.util.doom.savegame.IDoomSaveGame;
import org.bleachhack.util.doom.savegame.IDoomSaveGameHeader;
import org.bleachhack.util.doom.savegame.VanillaDSG;
import org.bleachhack.util.doom.savegame.VanillaDSGHeader;
import org.bleachhack.util.doom.st.AbstractStatusBar;
import org.bleachhack.util.doom.st.StatusBar;
import org.bleachhack.util.doom.timing.ITicker;
import org.bleachhack.util.doom.timing.MilliTicker;
import org.bleachhack.util.doom.utils.C2JUtils;
import static org.bleachhack.util.doom.utils.C2JUtils.*;
import org.bleachhack.util.doom.v.DoomGraphicSystem;
import org.bleachhack.util.doom.v.renderers.BppMode;
import static org.bleachhack.util.doom.v.renderers.DoomScreen.*;
import org.bleachhack.util.doom.v.renderers.RendererFactory;
import org.bleachhack.util.doom.v.scale.VideoScale;
import org.bleachhack.util.doom.v.scale.VisualSettings;
import org.bleachhack.util.doom.wad.IWadLoader;
import org.bleachhack.util.doom.wad.WadLoader;

// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: DoomMain.java,v 1.109 2012/11/06 16:04:58 velktron Exp $
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
//	DOOM main program (D_DoomMain) and game loop (D_DoomLoop),
//	plus functions to determine game mode (shareware, registered),
//	parse command line parameters, configure game parameters (turbo),
//	and call the startup functions.
//
//  In Mocha Doom, this was unified with d_game and doomstat.c
//
//-----------------------------------------------------------------------------

@SuppressWarnings({
    "UseOfSystemOutOrSystemErr",
    "MalformedFormatString",
    "CallToPrintStackTrace",
    "override",
    "StringBufferMayBeStringBuilder"
})
public class DoomMain<T, V> extends DoomStatus<T, V> implements IDoomGameNetworking, IDoomGame, IDoom {

    public static final String RCSID = "$Id: DoomMain.java,v 1.109 2012/11/06 16:04:58 velktron Exp $";

    //
    // EVENT HANDLING
    //
    // Events are asynchronous inputs generally generated by the game user.
    // Events can be discarded if no responder claims them
    //
    public final event_t[] events = new event_t[MAXEVENTS];
    public int eventhead;
    public int eventtail;

    /**
     * D_PostEvent
     * Called by the I/O functions when input is detected
     */
    public void PostEvent(event_t ev) {
        /**
         * Do not pollute DOOM's internal event queue - clear keys there
         * - Good Sign 2017/04/24
         */
        if (ev == event_t.CANCEL_KEYS) {
            // PAINFULLY and FORCEFULLY clear the buttons.
            memset(gamekeydown, false, gamekeydown.length);
            keysCleared = true;
            return; // Nothing more to do here.
        }

        events[eventhead] = ev;
        eventhead = (++eventhead) & (MAXEVENTS - 1);
    }

    public void Shutdown() {
        try {
            QuitNetGame();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        soundDriver.ShutdownSound();
        music.ShutdownMusic();
        commit();
        CM.SaveDefaults();

        Engine.closeFrame();
        ModuleManager.getModule(Doom.class).setEnabled(false);
        Doom.future.cancel(true);
    }

    /**
     * D_ProcessEvents
     * Send all the events of the given timestamp down the responder chain
     */ 
    @D_Main.C(D_ProcessEvents)
    public void ProcessEvents() {
        // IF STORE DEMO, DO NOT ACCEPT INPUT
        if ((isCommercial())) {
            W_CheckNumForName: {
                if ((wadLoader.CheckNumForName("MAP01") < 0)) {
                    return; 
                }
            }
        }

        for(; eventtail != eventhead; eventtail = (++eventtail) & (MAXEVENTS - 1)) {
            final event_t ev = events[eventtail];
            ev.withMouse(event_t.mouseevent_t::processedNotify);
            
            M_Responder: {
                if (menu.Responder(ev)) {
                    continue; // menu ate the event
                }
            }
            
            G_Responder: {
                Responder(ev);
            }
        }
    }

    // "static" to Display, don't move.
    private boolean viewactivestate = false;
    private boolean menuactivestate = false;
    private boolean inhelpscreensstate = false;
    private boolean fullscreen = false;
    private gamestate_t oldgamestate = GS_MINUS_ONE;
    private int borderdrawcount;

    /**
     * D_Display
     * draw current display, possibly wiping it from the previous
     * @throws IOException 
     */
    public void Display() throws IOException {
        int nowtime;
        int tics;
        int wipestart;
        int y;
        boolean done;
        boolean wipe;
        boolean redrawsbar;

        // for comparative timing / profiling
        if (nodrawers) {
            return;
        }
        redrawsbar = false;

        // change the view size if needed
        if (sceneRenderer.getSetSizeNeeded()) {
            sceneRenderer.ExecuteSetViewSize ();
            // force background redraw
            oldgamestate = GS_MINUS_ONE;
            borderdrawcount = 3;
        }

        // save the current screen if about to wipe
        wipe = (gamestate != wipegamestate);
        if (wipe) {
            wiper.StartScreen(0, 0, vs.getScreenWidth(), vs.getScreenHeight());
        }

        if (gamestate == GS_LEVEL && eval(gametic)) {
            headsUp.Erase();
        }
        
        // do buffered drawing
        switch (gamestate) {
            case GS_LEVEL:
                if (!eval(gametic)) {
                        break;
                }
                
                if (automapactive) {
                    autoMap.Drawer();
                }
                
                if (wipe
                    || (!sceneRenderer.isFullHeight() && fullscreen)
                    || (inhelpscreensstate && !inhelpscreens)
                    || (diskDrawer.justDoneReading()))
                {
                    redrawsbar = true; // just put away the help screen
                }
                statusBar.Drawer(sceneRenderer.isFullHeight(), redrawsbar);
                fullscreen = sceneRenderer.isFullHeight();
                break;
            case GS_INTERMISSION:
                endLevel.Drawer();
                break;
            case GS_FINALE:
                finale.Drawer();
                break;
            case GS_DEMOSCREEN:
                PageDrawer();
                break;
            default:
            	break;
        }

        // draw the view directly
        if (gamestate == GS_LEVEL && !automapactive && eval(gametic)) {
            if (flashing_hom) {
                graphicSystem.FillRect(FG, new Rectangle(view.getViewWindowX(), view.getViewWindowY(),
                        view.getScaledViewWidth(), view.getScaledViewHeight()), gametic % 256);
            }
            sceneRenderer.RenderPlayerView(players[displayplayer]);
        }

        // Automap was active, update only HU.    
        if (gamestate == GS_LEVEL && eval(gametic)) {
            headsUp.Drawer();
        }

        // clean up border stuff
        if (gamestate != oldgamestate && gamestate != GS_LEVEL) {
            graphicSystem.setPalette(0);
        }

        // see if the border needs to be initially drawn
        if (gamestate == GS_LEVEL && oldgamestate != GS_LEVEL) {
            // view was not active
            viewactivestate = false;
            // draw the pattern into the back screen
            sceneRenderer.FillBackScreen();
        }

        // see if the border needs to be updated to the screen
        if (gamestate == GS_LEVEL && !automapactive && !sceneRenderer.isFullScreen()) {
            if (menuactive || menuactivestate || !viewactivestate) {
                borderdrawcount = 3;
            }

            if (eval(borderdrawcount)) {
                // erase old menu stuff
                sceneRenderer.DrawViewBorder ();
                borderdrawcount--;
            }
        }

        menuactivestate = menuactive;
        viewactivestate = viewactive;
        inhelpscreensstate = inhelpscreens;
        oldgamestate = wipegamestate = gamestate;

        // draw pause pic
        if (paused) {
            if (automapactive) {
                y = 4 * graphicSystem.getScalingY();
            } else {
                y = view.getViewWindowY() + 4 * graphicSystem.getScalingY();
            }
            
            final patch_t pause = wadLoader.CachePatchName("M_PAUSE", PU_CACHE);
            graphicSystem.DrawPatchCenteredScaled(FG, pause , vs, y, DoomGraphicSystem.V_NOSCALESTART);
        }

        // menus go directly to the screen
        menu.Drawer(); // menu is drawn even on top of everything
        NetUpdate(); // send out any new accumulation

        // Disk access goes after everything.
        diskDrawer.Drawer();
        
        // normal update
        if (!wipe) {
            //System.out.print("Tick "+gametic+"\t");
            //System.out.print(players[0]);
            Engine.updateFrame(); // page flip or blit buffer
            return;
        }

        // wipe update. At this point, AT LEAST one frame of the game must have been
        // rendered for this to work. 22/5/2011: Fixed a vexing bug with the wiper.
        // Jesus Christ with a Super Shotgun!
        wiper.EndScreen(0, 0, vs.getScreenWidth(), vs.getScreenHeight());

        wipestart = ticker.GetTime () - 1;

        do {
            do {
                nowtime = ticker.GetTime();
                tics = nowtime - wipestart;
            } while (tics == 0); // Wait until a single tic has passed.
            wipestart = nowtime;
            Wiper.Wipe wipeType = CM.equals(Settings.scale_melt, Boolean.TRUE)
                    ? Wiper.Wipe.ScaledMelt : Wiper.Wipe.Melt;

            done = wiper.ScreenWipe(wipeType, 0, 0, vs.getScreenWidth(), vs.getScreenHeight(), tics);
            soundDriver.UpdateSound();
            soundDriver.SubmitSound();             // update sounds after one wipe tic.
            menu.Drawer();                    // menu is drawn even on top of wipes
            Engine.updateFrame();             // page flip or blit buffer
        } while (!done);
    }

    /**
     * To be able to debug vanilla incompatibilitites, the DoomLoop
     * and all that is called by it that relates to the Loop itself,
     * the ticks, game object modifications, mode changes and so on,
     * ***MUST*** be preceded by a label, containing original
     * underscored naming of the method in Doom Source Code.
     * 
     * Remember the label blocks will retain their name even in case
     * of *automated refactoring*, thus if you rename some method
     * and update it throughout the whole codebase, the named label
     * will still be named the same underscored original method name
     * 
     * Do it the most verbose way you can - preserving both, or all
     * brackets of all blocks containing and contained in the label,
     * and the brackets of the label itself, with one exception:
     * 
     * If there is no more function to do the task was given to the
     * function in original Doom Source Code, the label stull ***MUST***
     * be present, just type a semicolon to end it without actions.
     * The syntax is short and displays clearly that nothing is done.
     *  - Good Sign 2017/04/26
     * 
     * D_DoomLoop()
     * Not a globally visible function,
     *  just included for source reference,
     *  called by D_DoomMain, never exits.
     * Manages timing and IO,
     *  calls all ?_Responder, ?_Ticker, and ?_Drawer,
     *  calls I_GetTime, I_StartFrame, and I_StartTic
     * @throws IOException 
     */
    @D_Main.C(D_DoomLoop)
    public void DoomLoop() throws IOException {
        if (demorecording) {
            G_BeginRecording: {
                BeginRecording();
            }
        }

        M_CheckParm: {
            if (cVarManager.bool(CommandVariable.DEBUGFILE)) {
                String filename = "debug" + consoleplayer + ".txt";
                System.out.println("debug output to: " + filename);
                try {
                    debugfile = new OutputStreamWriter(new FileOutputStream(filename));
                } catch (FileNotFoundException e) {
                    System.err.println("Couldn't open debugfile. Now, that sucks some putrid shit out of John Romero's asshole!");
                    e.printStackTrace();
                }
            }
        }
        
        I_InitGraphics: {
            view = sceneRenderer.getView();
        }
        
        while (true) {
            if (!Doom.running) {
                Shutdown();
                return;
            }

            // frame syncronous IO operations
            I_StartFrame:;
            
            // process one or more tics
            if (singletics) {
                I_StartTic:;
                D_ProcessEvents: {
                    ProcessEvents();
                }
                G_BuildTiccmd: {
                    BuildTiccmd(netcmds[consoleplayer][maketic % BACKUPTICS]);
                }
                if (advancedemo) {
                    D_DoAdvanceDemo: {
                        DoAdvanceDemo();
                    }
                }
                M_Ticker: {
                    menu.Ticker();
                }
                G_Ticker: {
                    Ticker();
                }
                gametic++;
                maketic++;
            } else {
                gameNetworking.TryRunTics(); // will run at least one tic (in NET)
            }
            S_UpdateSounds: {
                doomSound.UpdateSounds(players[consoleplayer].mo); // move positional sounds
            }
            D_Display: { // Update display, next frame, with current state.
                Display();
            }
            //#ifndef SNDSERV
            // Sound mixing for the buffer is snychronous.
            soundDriver.UpdateSound();
            //#endif	
            // Synchronous sound output is explicitly called.
            //#ifndef SNDINTR
            // Update sound output.
            soundDriver.SubmitSound();
            //#endif
        }
    }
    
    // To keep an "eye" on the renderer.
    protected ViewVars view;

    //
    //  DEMO LOOP
    //
    int demosequence;
    int pagetic;
    String pagename;


    /**
     * D_PageTicker
     * Handles timing for warped projection
     */
    public final void PageTicker() {
        if (--pagetic < 0) {
            AdvanceDemo();
        }
    }

    /**
     * D_PageDrawer
     */
    public final void PageDrawer() {
        // FIXME: this check wasn't necessary in vanilla, since pagename was 
        // guaranteed(?) not to be null or had a safe default value.  
        if (pagename != null) {
            graphicSystem.DrawPatchScaled(FG, wadLoader.CachePatchName(pagename, PU_CACHE), vs, 0, 0, DoomGraphicSystem.V_SAFESCALE);
        }
    }


    /**
     * D_AdvanceDemo
     * Called after each demo or intro demosequence finishes
     */
    public void AdvanceDemo ()
    {
        advancedemo = true;
    }

    /**
     * This cycles through the demo sequences.
     * FIXME - version dependant demo numbers?
     */
    public void DoAdvanceDemo() {
        players[consoleplayer].playerstate = PST_LIVE;  // not reborn
        advancedemo = false;
        usergame = false;               // no save / end game here
        paused = false;
        gameaction = ga_nothing;

        if (isRetail()) // Allows access to a 4th demo.
        {
            demosequence = (demosequence + 1) % 7;
        } else {
            demosequence = (demosequence + 1) % 6;
        }

        switch (demosequence) {
            case 0:
                if (isCommercial()) {
                    pagetic = 35 * 11;
                } else {
                    pagetic = 170;
                }
                gamestate = GS_DEMOSCREEN;

                if (wadLoader.CheckNumForName("TITLEPIC") != -1) {
                    pagename = "TITLEPIC";
                } else {
                    if (wadLoader.CheckNumForName("DMENUPIC") != -1) {
                        pagename = "DMENUPIC";
                    }
                }

                if (isCommercial()) {
                    doomSound.StartMusic(musicenum_t.mus_dm2ttl);
                } else {
                    doomSound.StartMusic(musicenum_t.mus_intro);
                }
                break;
            case 1:
                DeferedPlayDemo("demo1");
                break;
            case 2:
                pagetic = 200;
                gamestate = GS_DEMOSCREEN;
                pagename = "CREDIT";
                break;
            case 3:
                DeferedPlayDemo("demo2");
                break;
            case 4:
                gamestate = GS_DEMOSCREEN;
                if (isCommercial()) {
                    pagetic = 35 * 11;
                    pagename = "TITLEPIC";
                    doomSound.StartMusic(musicenum_t.mus_dm2ttl);
                } else {
                    pagetic = 200;

                    if (isRetail()) {
                        pagename = "CREDIT";
                    } else {
                        pagename = "HELP1";
                    }
                }
                break;
            case 5:
                DeferedPlayDemo("demo3");
                break;
            // THE DEFINITIVE DOOM Special Edition demo
            case 6:
                DeferedPlayDemo("demo4");
                break;
        }
    }

    /**
     * D_StartTitle
     */
    public void StartTitle() {
        gameaction = ga_nothing;
        demosequence = -1;
        AdvanceDemo();
    }

    // print title for every printed line
    StringBuffer title = new StringBuffer();

    /**
     * D_AddFile
     *
     * Adds file to the end of the wadfiles[] list.
     * Quite crude, we could use a listarray instead.
     *
     * @param file
     */
    private void AddFile(String file) {
        int numwadfiles;
        for (numwadfiles = 0; eval(wadfiles[numwadfiles]); numwadfiles++) {}
        wadfiles[numwadfiles] = file;
    }


    /**
     * IdentifyVersion
     * Checks availability of IWAD files by name,
     * to determine whether registered/commercial features
     * should be executed (notably loading PWAD's).
     */
    public final String IdentifyVersion() {
        String doomwaddir;
        // By default.
        language = Language_t.english;

        // First, check for -iwad parameter.
        // If valid, then it trumps all others.
        if (cVarManager.present(CommandVariable.IWAD)) {
            System.out.println("-iwad specified. Will be used with priority\n");
            // It might be quoted.
            final String test = C2JUtils.unquoteIfQuoted(cVarManager.get(CommandVariable.IWAD, String.class, 0).get(), '"');
            final String separator = System.getProperty("file.separator");
            final String iwad = test.substring(1 + test.lastIndexOf(separator));
            doomwaddir = test.substring(0, 1 + test.lastIndexOf(separator));
            final GameMode attempt = DoomVersion.tryOnlyOne(iwad, doomwaddir);
            // Note: at this point we can't distinguish between "doom" retail
            // and "doom" ultimate yet.
            if (attempt != null) {
                AddFile(doomwaddir + iwad);
                this.setGameMode(attempt);
                return (doomwaddir + iwad);
            }
        } else {
            // Unix-like checking. Might come in handy sometimes.   
            // This should ALWAYS be activated, else doomwaddir etc. won't be defined.

            doomwaddir = System.getenv("DOOMWADDIR");
            if (doomwaddir != null) {
                System.out.println("DOOMWADDIR found. Will be used with priority\n");
            }

            // None found, using current.
            if (!eval(doomwaddir)) {
                doomwaddir = ".";
            }
        }

        for (GameMode mode: GameMode.values()) {
            if (mode != GameMode.indetermined && cVarManager.bool(mode.devVar)) {
                return devParmOn(mode);
            }
        }
        
        final String wadFullPath = DoomVersion.tryAllWads(this, doomwaddir);
        if (wadFullPath == null) {
            System.out.println("Game mode indeterminate.\n");
            setGameMode(GameMode.indetermined);
            // We don't abort. Let's see what the PWAD contains.
            //exit(1);
            //I_Error ("Game mode indeterminate\n");
        } else {
            AddFile(wadFullPath);
        }
        
        return wadFullPath;
    }

    private String devParmOn(GameMode mode) {
        setGameMode(mode);
        devparm = true;
        AddFile(dstrings.DEVDATA + mode.version);
        AddFile(dstrings.DEVMAPS + mode.devDir + "/texture1.lmp");
        if (mode.hasTexture2()) {
            AddFile(dstrings.DEVMAPS + mode.devDir + "/texture2.lmp");
        }
        AddFile(dstrings.DEVMAPS + mode.devDir + "/pnames.lmp");
        return (dstrings.DEVDATA + mode.version);
    }

    /**
     * 
     */
    protected final void CheckForPWADSInShareware() {
        if (modifiedgame)
        {
            // These are the lumps that will be checked in IWAD,
            // if any one is not present, execution will be aborted.
            String[] name= {
                "e2m1", "e2m2", "e2m3", "e2m4", "e2m5", "e2m6", "e2m7", "e2m8", "e2m9",
                "e3m1", "e3m3", "e3m3", "e3m4", "e3m5", "e3m6", "e3m7", "e3m8", "e3m9",
                "dphoof", "bfgga0", "heada1", "cybra1", "spida1d1"
            };
            int i;

            // Oh yes I can.
            if (isShareware()) {
                System.out.println("\nYou cannot -file with the shareware version. Register!");
            }

            // Check for fake IWAD with right name,
            // but w/o all the lumps of the registered version. 
            if (isRegistered()) {
                for (i = 0;i < 23; i++) {
                    if (wadLoader.CheckNumForName(name[i].toUpperCase())<0) {
                        doomSystem.Error("\nThis is not the registered version: "+name[i]);
                    }
                }
            }
        }
    }

    /** Check whether the "doom.wad" we actually loaded
     *  is ultimate Doom's, by checking if it contains 
     *  e4m1 - e4m9.
     * 
     */
    protected final void CheckForUltimateDoom(WadLoader W) {
        if (isRegistered())
        {
            // These are the lumps that will be checked in IWAD,
            // if any one is not present, execution will be aborted.
            String[] lumps = {"e4m1", "e4m2", "e4m3", "e4m4", "e4m5", "e4m6", "e4m7", "e4m8", "e4m9"};

            // Check for fake IWAD with right name,
            // but w/o all the lumps of the registered version. 
            if (!CheckForLumps(lumps,W)) return;
            // Checks passed, so we can set the mode to Ultimate
            setGameMode(GameMode.retail);
        }

    }


    /** Check if ALL of the lumps exist.
     * 
     * @param name
     * @return
     */
    protected boolean CheckForLumps(String[] name, WadLoader W) {
        for (String name1 : name) {
            if (W.CheckNumForName(name1.toUpperCase()) < 0) {
                // Even one is missing? Not OK.
                return false; 
            }
        }
        return true;
    }


    /**
     * 
     */
    protected final void GenerateTitle() {
        switch ( getGameMode() )
        {
        case retail:
            title.append("                         ");
            title.append("The Ultimate DOOM Startup v");
            title.append(VERSION/100);
            title.append(".");
            title.append(VERSION%100);
            title.append("                           ");
            break;
        case shareware:
            title.append("                            ");
            title.append("DOOM Shareware Startup v");
            title.append(VERSION/100);
            title.append(".");
            title.append(VERSION%100);
            title.append("                           ");
            break;
        case registered:
            title.append("                            ");
            title.append("DOOM Registered Startup v");
            title.append(VERSION/100);
            title.append(".");
            title.append(VERSION%100);
            title.append("                           ");
            break;
        case commercial:
            title.append("                            ");
            title.append("DOOM 2: Hell on Earth v");
            title.append(VERSION/100);
            title.append(".");
            title.append(VERSION%100);
            title.append("                           ");

            break;
        case pack_plut:
            title.append("                            ");
            title.append("DOOM 2: Plutonia Experiment v");
            title.append(VERSION/100);
            title.append(".");
            title.append(VERSION%100);
            title.append("                           ");
            break;
        case pack_tnt:
            title.append("                            ");
            title.append("DOOM 2: TNT - Evilution v");
            title.append(VERSION/100);
            title.append(".");
            title.append(VERSION%100);
            title.append("                           ");
            break;
        case pack_xbla:
            title.append("                            ");
            title.append("DOOM 2: No Rest for the Living v");
            title.append(VERSION/100);
            title.append(".");
            title.append(VERSION%100);
            title.append("                           ");
            break;
        case freedm:
            title.append("                            ");
            title.append("FreeDM                     v");
            title.append(VERSION/100);
            title.append(".");
            title.append(VERSION%100);
            title.append("                           ");
            break;
        case freedoom1:
            title.append("                            ");
            title.append("FreeDoom: Phase 1          v");
            title.append(VERSION/100);
            title.append(".");
            title.append(VERSION%100);
            title.append("                           ");
            break;
        case freedoom2:
            title.append("                            ");
            title.append("FreeDoom: Phase 2          v");
            title.append(VERSION/100);
            title.append(".");
            title.append(VERSION%100);
            title.append("                           ");
            break;
        default:
            title.append("                            ");
            title.append("Public DOOM - v");
            title.append(VERSION/100);
            title.append(".");
            title.append(VERSION%100);
            title.append("                           ");
            break;
        }
    }

    // Used in BuildTiccmd.
    protected ticcmd_t   base=new ticcmd_t();

    /**
     * G_BuildTiccmd
     * Builds a ticcmd from all of the available inputs
     * or reads it from the demo buffer. 
     * If recording a demo, write it out .
     * 
     * The CURRENT event to process is written to the various 
     * gamekeydown etc. arrays by the Responder method.
     * So look there for any fuckups in constructing them.
     * 
     */
    
    @SourceCode.Compatible
    @G_Game.C(G_BuildTiccmd)
    private void BuildTiccmd (ticcmd_t cmd) 
    { 
        int i;
        boolean strafe;
        boolean bstrafe;
        int speed, tspeed, lspeed;
        int forward;
        int side;
        int look;

        I_BaseTiccmd:;     // empty, or external driver
        base.copyTo(cmd);

        cmd.consistancy = consistancy[consoleplayer][maketic % BACKUPTICS];
        
        strafe = gamekeydown[key_strafe] || mousebuttons(mousebstrafe) || joybuttons(joybstrafe);
        speed = ((gamekeydown[key_speed] ^ alwaysrun) || joybuttons(joybspeed)) ? 1 : 0;

        forward = side = look = 0;

        // use two stage accelerative turning
        // on the keyboard and joystick
        if (joyxmove < 0 || joyxmove > 0 || gamekeydown[key_right] || gamekeydown[key_left]) {
            turnheld += ticdup; 
        } else {
            turnheld = 0; 
        }

        tspeed = turnheld < SLOWTURNTICS ? 2 /* slowturn */ : speed;

        if (gamekeydown[key_lookdown] || gamekeydown[key_lookup]) {
            lookheld += ticdup;
        } else {
            lookheld = 0;
        }
        
        lspeed = lookheld < SLOWTURNTICS ? 1 : 2;
        
        // let movement keys cancel each other out
        if (strafe) { 
            if (gamekeydown[key_right]) {
                // fprintf(stderr, "strafe right\n");
                side += sidemove[speed]; 
            }
            
            if (gamekeydown[key_left]) {
                //  fprintf(stderr, "strafe left\n");
                side -= sidemove[speed]; 
            }
            
            if (joyxmove > 0) {
                side += sidemove[speed]; 
            } else if (joyxmove < 0) {
                side -= sidemove[speed];
            }
        } else { 
            if (gamekeydown[key_right]) {
                cmd.angleturn -= angleturn[tspeed]; 
            }
            
            if (gamekeydown[key_left]) {
                cmd.angleturn += angleturn[tspeed]; 
            }
            
            if (joyxmove > 0) {
                cmd.angleturn -= angleturn[tspeed]; 
            } else if (joyxmove < 0) {
                cmd.angleturn += angleturn[tspeed];
            }
        } 

        if (gamekeydown[key_up]) {
            //System.err.print("up\n");
            forward += forwardmove[speed]; 
        }
        
        if (gamekeydown[key_down]) {
            //System.err.print("down\n");
            forward -= forwardmove[speed]; 
        }        
        
        if (joyymove < 0) {
            forward += forwardmove[speed]; 
        } else if (joyymove > 0) {
            forward -= forwardmove[speed]; 
        }
        
        if (gamekeydown[key_straferight]) {
            side += sidemove[speed]; 
        }
        
        if (gamekeydown[key_strafeleft]) {
            side -= sidemove[speed];
        }

    	// Look up/down/center keys
    	if(gamekeydown[key_lookup]) {
    		System.err.print("Look up\n");
    		look = lspeed;
    	}
    	
    	if(gamekeydown[key_lookdown]) {
    		System.err.print("Look down\n");
    		look = -lspeed;
    	}
    	
    	if(gamekeydown[key_lookcenter]) {
    		System.err.print("Center look\n");
    		look = TOCENTER;
    	}
    	
        // buttons
        cmd.chatchar = headsUp.dequeueChatChar(); 

        if (gamekeydown[key_fire] || mousebuttons(mousebfire) || joybuttons(joybfire)) {
            cmd.buttons |= BT_ATTACK; 
        }

        if (gamekeydown[key_use] || joybuttons(joybuse)) { 
            cmd.buttons |= BT_USE;
            // clear double clicks if hit use button 
            dclicks = 0;                   
        } 

        // chainsaw overrides 
        for (i = 0; i < NUMWEAPONS - 1; i++) {
            if (gamekeydown[key_numbers[i]]) {
                //System.out.println("Attempting weapon change (building ticcmd)");
                cmd.buttons |= BT_CHANGE;
                cmd.buttons |= i << BT_WEAPONSHIFT;
                break;
            }
        }

        // mouse
        if (mousebuttons(mousebforward)) {
            forward += forwardmove[speed];
        }

        // forward double click
        if (mousebuttons(mousebforward) != eval(dclickstate) && dclicktime > 1) {
            dclickstate = mousebuttons(mousebforward) ? 1 : 0;
            if (dclickstate != 0) {
                dclicks++;
            }
            if (dclicks == 2) {
                cmd.buttons |= BT_USE;
                dclicks = 0;
            } else {
                dclicktime = 0;
            }
        } else {
            dclicktime += ticdup;
            if (dclicktime > 20) {
                dclicks = 0;
                dclickstate = 0;
            }
        }

        // strafe double click
        bstrafe = mousebuttons(mousebstrafe) || joybuttons(joybstrafe);
        if ((bstrafe != eval(dclickstate2)) && dclicktime2 > 1) {
            dclickstate2 = bstrafe ? 1 : 0;
            if (dclickstate2 != 0) {
                dclicks2++;
            }
            if (dclicks2 == 2) {
                cmd.buttons |= BT_USE;
                dclicks2 = 0;
            } else {
                dclicktime2 = 0;
            }
        } else {
            dclicktime2 += ticdup;
            if (dclicktime2 > 20) {
                dclicks2 = 0;
                dclickstate2 = 0;
            }
        }

        // By default, no vertical mouse movement
        if (!novert) {
            forward += mousey;
        }

        if (strafe) {
            side += mousex * 2;
        } else {
            cmd.angleturn -= mousex * 0x8;
        }

        mousex = mousey = 0; 

        if (forward > MAXPLMOVE()) {
            forward = MAXPLMOVE();
        } else if (forward < -MAXPLMOVE()) {
            forward = -MAXPLMOVE();
        }
        if (side > MAXPLMOVE()) {
            side = MAXPLMOVE();
        } else if (side < -MAXPLMOVE()) {
            side = -MAXPLMOVE();
        }

        cmd.forwardmove += forward; 
        cmd.sidemove += side;

        if (players[consoleplayer].playerstate == PST_LIVE) {
            if (look < 0) {
                look += 16;
            }

            cmd.lookfly = (char) look;
        }

        // special buttons
        if (sendpause) {
            sendpause = false;
            cmd.buttons = BT_SPECIAL | BTS_PAUSE;
        }

        if (sendsave) {
            sendsave = false;
            cmd.buttons = (char) (BT_SPECIAL | BTS_SAVEGAME | (savegameslot << BTS_SAVESHIFT));
        }
    } 


    /**
     * G_DoLoadLevel 
     * 
     * //extern gamestate_t wipegamestate;
     */
    @SourceCode.Suspicious(CauseOfDesyncProbability.LOW)
    @G_Game.C(G_DoLoadLevel)
    public boolean DoLoadLevel() { 
        /**
         * Added a config switch to this fix
         *  - Good Sign 2017/04/26
         * 
         * Fixed R_FlatNumForName was a part of the fix, not vanilla code
         *  - Good Sign 2017/05/07
         * 
         * DOOM determines the sky texture to be used
         * depending on the current episode, and the game version.
         * 
         * @SourceCode.Compatible
         */
        if (Engine.getConfig().equals(Settings.fix_sky_change, Boolean.TRUE) && (isCommercial()
                || ( gamemission == GameMission_t.pack_tnt )
                || ( gamemission == GameMission_t.pack_plut )))
        {
            // Set the sky map.
            // First thing, we have a dummy sky texture name,
            //  a flat. The data is in the WAD only because
            //  we look for an actual index, instead of simply
            //  setting one.
            textureManager.setSkyFlatNum(textureManager.FlatNumForName(SKYFLATNAME));

            textureManager.setSkyTexture(textureManager.TextureNumForName ("SKY3"));
            if (gamemap < 12) {
                textureManager.setSkyTexture(textureManager.TextureNumForName ("SKY1"));
            } else {
                if (gamemap < 21) {
                    textureManager.setSkyTexture(textureManager.TextureNumForName ("SKY2"));
                }
            }
        }

        levelstarttic = gametic;        // for time calculation

        if (wipegamestate == GS_LEVEL) 
            wipegamestate = GS_MINUS_ONE;             // force a wipe 

        gamestate = GS_LEVEL; 

        for (int i = 0; i < MAXPLAYERS; i++) {
            if (playeringame[i] && players[i].playerstate == PST_DEAD) {
                players[i].playerstate = PST_REBORN;
            }

            memset(players[i].frags, 0, players[i].frags.length);
        }

        try {
            P_SetupLevel: {
                levelLoader.SetupLevel(gameepisode, gamemap, 0, gameskill);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Failure loading level.
            return false;
        }
        
        displayplayer = consoleplayer; // view the guy you are playing    
        I_GetTime: {
            starttime = ticker.GetTime();
        }
        gameaction = ga_nothing; 
        Z_CheckHeap:;

        // clear cmd building stuff
        memset(gamekeydown, false, gamekeydown.length);
        keysCleared = true;
        joyxmove = joyymove = 0; 
        mousex = mousey = 0; 
        sendpause = sendsave = paused = false; 
        memset(mousearray, false, mousearray.length);
        memset(joyarray, false, joyarray.length);
        
        /**
         * Probably no desync-effect
         * - GoodSign 2017/05/07
         * 
         * @SourceCode.Suspicious
         */
        
        // killough 5/13/98: in case netdemo has consoleplayer other than green
        statusBar.Start();
        headsUp.Start();
        
        // killough: make -timedemo work on multilevel demos
        // Move to end of function to minimize noise -- killough 2/22/98:

        if (timingdemo) {
            if (first) {
                starttime = RealTime.GetTime();
                first = false;
            }
        }
        
        // Try reclaiming some memory from limit-expanded buffers.
        sceneRenderer.resetLimits();
        return true;
    } 
    
    protected boolean first = true;

    /**
     * G_Responder  
     * Get info needed to make ticcmd_ts for the players.
     */
    @SourceCode.Compatible
    @G_Game.C(G_Responder)
    public boolean Responder(event_t ev) {
        // allow spy mode changes even during the demo
        if (gamestate == GS_LEVEL && ev.isKey(SC_F12, ev_keydown) && (singledemo || !deathmatch)) {
            // spy mode 
            do {
                displayplayer++;
                if (displayplayer == MAXPLAYERS) {
                    displayplayer = 0;
                }
            } while (!playeringame[displayplayer] && displayplayer != consoleplayer);
            return true;
        }

        // any other key pops up menu if in demos
        if (gameaction == ga_nothing && !singledemo && (demoplayback || gamestate == GS_DEMOSCREEN)) {
            if (ev.isType(ev_keydown)
                || ev.ifMouse(ev_mouse, event_t::hasData)
                || ev.ifJoy(ev_joystick, event_t::hasData))
            {
                M_StartControlPanel: {
                    menu.StartControlPanel();
                }
                return true;
            }
            return false;
        }

        if (gamestate == GS_LEVEL) {
            if (devparm && ev.isKey(SC_SEMICOLON, ev_keydown)) {
                G_DeathMatchSpawnPlayer: {
                    DeathMatchSpawnPlayer(0);
                }
                return true;
            }

            HU_Responder: {
                if (headsUp.Responder(ev)) {
                    return true;    // chat ate the event 
                }
            }
            ST_Responder: {
                if (statusBar.Responder(ev)) {
                    return true;    // status window ate it
                }
            }
            AM_Responder: {
                if (autoMap.Responder(ev)) {
                    return true;    // automap ate it 
                }
            }
        }

        if (gamestate == GS_FINALE) {
            F_Responder: {
                if (finale.Responder(ev)) {
                    return true;    // finale ate the event 
                }
            }
        }

        switch (ev.type()) { 
            case ev_keydown:
                if (ev.isKey(SC_PAUSE)) {
                    sendpause = true;
                    return true;
                }

                ev.withKey(sc -> {
                    gamekeydown[sc.ordinal()] = true;
                    if (vanillaKeyBehavior) {
                        switch(sc) {
                            case SC_LSHIFT:
                            case SC_RSHIFT:
                                gamekeydown[SC_RSHIFT.ordinal()] = gamekeydown[SC_LSHIFT.ordinal()] = true;
                                break;
                            case SC_LCTRL:
                            case SC_RCTRL:
                                gamekeydown[SC_RCTRL.ordinal()] = gamekeydown[SC_LCTRL.ordinal()] = true;
                                break;
                            case SC_LALT:
                            case SC_RALT:
                                gamekeydown[SC_RALT.ordinal()] = gamekeydown[SC_LALT.ordinal()] = true;
                                break;
                            default: break;
                        }
                    }
                });
                return true;    // eat key down events 
            case ev_keyup:
                /* CAPS lock will only go through as a keyup event */
                if (ev.isKey(SC_CAPSLK)) {
                    // Just toggle it. It's too hard to read the state.
                    alwaysrun = !alwaysrun;
                    players[consoleplayer].message = String.format("Always run: %s", alwaysrun);
                }

                ev.withKey(sc -> {
                    gamekeydown[sc.ordinal()] = false;
                    if (vanillaKeyBehavior) {
                        switch(sc) {
                            case SC_LSHIFT:
                            case SC_RSHIFT:
                                gamekeydown[SC_RSHIFT.ordinal()] = gamekeydown[SC_LSHIFT.ordinal()] = false;
                                break;
                            case SC_LCTRL:
                            case SC_RCTRL:
                                gamekeydown[SC_RCTRL.ordinal()] = gamekeydown[SC_LCTRL.ordinal()] = false;
                                break;
                            case SC_LALT:
                            case SC_RALT:
                                gamekeydown[SC_RALT.ordinal()] = gamekeydown[SC_LALT.ordinal()] = false;
                                break;
                            default: break;
                        }
                    }
                });
                return false;   // always let key up events filter down 

            case ev_mouse:
                // Ignore them at the responder level
                if (use_mouse) {
                    mousebuttons(0, ev.isMouse(event_t.MOUSE_LEFT));
                    mousebuttons(1, ev.isMouse(event_t.MOUSE_RIGHT));
                    mousebuttons(2, ev.isMouse(event_t.MOUSE_MID));
                    ev.withMouse(mouseEvent -> {
                        mousex = mouseEvent.x * (mouseSensitivity + 5) / 10;
                        mousey = mouseEvent.y * (mouseSensitivity + 5) / 10;
                    });
                }
                return true; // eat events 
            case ev_joystick:
                if (use_joystick) {
                    joybuttons(0, ev.isJoy(event_t.JOY_1));
                    joybuttons(1, ev.isJoy(event_t.JOY_2));
                    joybuttons(2, ev.isJoy(event_t.JOY_3));
                    joybuttons(3, ev.isJoy(event_t.JOY_4));
                    ev.withJoy(joyEvent -> {
                        joyxmove = joyEvent.x;
                        joyymove = joyEvent.y;
                    });
                }
                return true;    // eat events 
            default:
                break;
        }

        return false;
    }

    private final String turbomessage="is turbo!"; 

    /**
     * G_Ticker
     * 
     * Make ticcmd_ts for the players.
     */
    @G_Game.C(G_Ticker)
    public void Ticker() { 
        // do player reborns if needed
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (playeringame[i] && players[i].playerstate == PST_REBORN) {
                G_DoReborn: {
                    DoReborn(i);
                }
            }
        }

        // do things to change the game state
        while (gameaction != ga_nothing) { 
            switch (gameaction) { 
                case ga_loadlevel:
                    G_DoLoadLevel: {
                        DoLoadLevel();
                    }
                    break;
                case ga_newgame:
                    G_DoNewGame: {
                        DoNewGame();
                    }
                    break;
                case ga_loadgame:
                    G_DoLoadGame: {
                        DoLoadGame();
                    }
                    break;
                case ga_savegame:
                    G_DoSaveGame: {
                        DoSaveGame();
                    }
                    break;
                case ga_playdemo:
                    G_DoPlayDemo: {
                        DoPlayDemo();
                    }
                    break;
                case ga_completed:
                    G_DoCompleted: {
                        DoCompleted();
                    }
                    break;
                case ga_victory:
                    finale.StartFinale();
                    break;
                case ga_worlddone:
                    DoWorldDone();
                    break;
                case ga_screenshot:
                    ScreenShot();
                    gameaction = ga_nothing;
                    break;
                case ga_nothing:
                    break;
                default:
                	break;
            }
        }

        // get commands, check consistancy,
        // and build new consistancy check
        final int buf = (gametic / ticdup) % BACKUPTICS;
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (playeringame[i]) {
                final ticcmd_t cmd = players[i].cmd;
                //System.out.println("Current command:"+cmd);

                //memcpy (cmd, &netcmds[i][buf], sizeof(ticcmd_t));
                netcmds[i][buf].copyTo(cmd);

                // MAES: this is where actual demo commands are being issued or created!
                // Essentially, a demo is a sequence of stored ticcmd_t with a header.
                // Knowing that, it's possible to objectify it.
                if (demoplayback) {
                    ReadDemoTiccmd(cmd);
                }
                
                if (demorecording) {
                    WriteDemoTiccmd(cmd);
                }

                // check for turbo cheats
                if (cmd.forwardmove > TURBOTHRESHOLD && ((gametic & 31) == 0) && ((gametic >> 5) & 3) == i) {
                    //extern char *player_names[4];
                    //sprintf (turbomessage, "%s is turbo!",player_names[i]);
                    players[consoleplayer].message = org.bleachhack.util.doom.hu.HU.player_names[i] + turbomessage;
                }

                if (netgame && !netdemo && (gametic % ticdup) == 0) {
                    if (gametic > BACKUPTICS && consistancy[i][buf] != cmd.consistancy) {
                        doomSystem.Error("consistency failure (%d should be %d)", cmd.consistancy, consistancy[i][buf]);
                    }
                    
                    if (players[i].mo != null) {
                        consistancy[i][buf] = (short) players[i].mo.x;
                    } else {
                        consistancy[i][buf] = (short) random.getIndex();
                    }
                }
            }
        }

        // check for special buttons
        for (int i = 0; i < MAXPLAYERS; i++) {
            if (playeringame[i]) {
                if ((players[i].cmd.buttons & BT_SPECIAL) != 0) {
                    switch (players[i].cmd.buttons & BT_SPECIALMASK) {
                        case BTS_PAUSE:
                            // MAES: fixed stupid ^pause bug.
                            paused = !paused;
                            if (paused) {
                                doomSound.PauseSound();
                            } else {
                                doomSound.ResumeSound();
                            }
                            break;
                        case BTS_SAVEGAME:
                            if (savedescription == null) {
                                savedescription = "NET GAME";
                            }
                            savegameslot = (players[i].cmd.buttons & BTS_SAVEMASK) >> BTS_SAVESHIFT;
                            gameaction = ga_savegame;
                            break;
                    }
                }
            }
        }

        // do main actions
        switch (gamestate) {
            case GS_LEVEL:
                actions.Ticker();
                statusBar.Ticker();
                autoMap.Ticker();
                headsUp.Ticker();
                break;

            case GS_INTERMISSION:
                endLevel.Ticker();
                break;

            case GS_FINALE:
                finale.Ticker();
                break;

            case GS_DEMOSCREEN:
                PageTicker();
                break;
                
            default:
            	break;
        }
    } 

    //
    // PLAYER STRUCTURE FUNCTIONS
    // also see P_SpawnPlayer in P_Things
    //

    /**
     * G_InitPlayer
     * Called at the start.
     * Called by the game initialization functions.
     *
     * MAES: looks like dead code. It's never called.
     *
     */
    protected void InitPlayer(int player) {
        // set up the saved info         
        // clear everything else to defaults 
        players[player].PlayerReborn();
    }

    //
    // G_CheckSpot  
    // Returns false if the player cannot be respawned
    // at the given mapthing_t spot  
    // because something is occupying it 
    //
    //void P_SpawnPlayer (mapthing_t* mthing); 
    @SourceCode.Exact
    @G_Game.C(G_CheckSpot)
    private boolean CheckSpot(int playernum, mapthing_t mthing) {
        if (players[playernum].mo == null) {
            // first spawn of level, before corpses
            for (int i = 0; i < playernum; i++) {
                if (players[i].mo.x == mthing.x << FRACBITS && players[i].mo.y == mthing.y << FRACBITS) {
                    return false;
                }
            }
            return true;
        }

        final int x = mthing.x << FRACBITS, y = mthing.y << FRACBITS;

        P_CheckPosition: {
            if (!actions.CheckPosition(players[playernum].mo, x, y)) {
                return false;
            }
        }

        // flush an old corpse if needed 
        if (bodyqueslot >= BODYQUESIZE) {
            P_RemoveMobj: {
                actions.RemoveMobj(bodyque[bodyqueslot % BODYQUESIZE]);
            }
        }
        bodyque[bodyqueslot % BODYQUESIZE] = players[playernum].mo;
        bodyqueslot++;

        // spawn a teleport fog 
        final subsector_t ss;
        R_PointInSubsector: {
            ss = levelLoader.PointInSubsector(x, y);
        }
        // Angles stored in things are supposed to be "sanitized" against rollovers.
        final int angle = (int) ((ANG45 * (mthing.angle / 45)) >>> ANGLETOFINESHIFT);
        final mobj_t mo;
        P_SpawnMobj: {
             mo = actions.SpawnMobj(x + 20 * finecosine[angle], y + 20 * finesine[angle], ss.sector.floorheight, mobjtype_t.MT_TFOG);
        }

        // FIXME: maybe false fix
        if (players[consoleplayer].viewz != 1) {
            S_StartSound: {
                doomSound.StartSound(mo, sfxenum_t.sfx_telept);  // don't start sound on first frame 
            }
        }
        
        return true;
    }


    //
    // G_DeathMatchSpawnPlayer 
    // Spawns a player at one of the random death match spots 
    // called at level load and each death 
    //
    @Override
    @SourceCode.Exact
    @G_Game.C(G_DeathMatchSpawnPlayer)
    public void DeathMatchSpawnPlayer(int playernum) {
        final int selections = deathmatch_p; 
        if (selections < 4)  {
            I_Error: {
                doomSystem.Error("Only %d deathmatch spots, 4 required", selections);
            } 
        }

        for (int j = 0; j < 20; j++) {
            final int i;
            P_Random: {
                i = random.P_Random() % selections;
            }
            G_CheckSpot: {
                if (CheckSpot(playernum, deathmatchstarts[i])) {
                    deathmatchstarts[i].type = (short) (playernum + 1);
                    P_SpawnPlayer: {
                        actions.SpawnPlayer(deathmatchstarts[i]);
                    }
                    return;
                }
            }
        }

        // no good spot, so the player will probably get stuck
        // MAES: seriously, fuck him.
        P_SpawnPlayer: {
            actions.SpawnPlayer(playerstarts[playernum]);
        }
    }

    /**
     * G_DoReborn 
     */
    @SourceCode.Exact
    @G_Game.C(G_DoReborn)
    public void DoReborn (int playernum) { 
        if (!netgame) {
            // reload the level from scratch
            gameaction = ga_loadlevel;  
        } else {
            // respawn at the start

            // first dissasociate the corpse 
            players[playernum].mo.player = null;   

            // spawn at random spot if in death match 
            if (deathmatch) {
                G_DeathMatchSpawnPlayer: {
                    DeathMatchSpawnPlayer(playernum);
                }
                return;
            }

            G_CheckSpot: {
                if (CheckSpot(playernum, playerstarts[playernum])) {
                    P_SpawnPlayer: {
                        actions.SpawnPlayer(playerstarts[playernum]);
                    }
                    return;
                }
            }

            // try to spawn at one of the other players spots 
            for (int i = 0; i < MAXPLAYERS; i++) {
                G_CheckSpot: {
                    if (CheckSpot(playernum, playerstarts[i])) {
                        playerstarts[i].type = (short) (playernum + 1); // fake as other player 
                        P_SpawnPlayer: {
                            actions.SpawnPlayer(playerstarts[i]);
                        }
                        playerstarts[i].type = (short) (i + 1);     // restore 
                        return;
                    }
                }
                // he's going to be inside something.  Too bad.
                // MAES: Yeah, they're like, fuck him.
            }
            
            P_SpawnPlayer: {
                actions.SpawnPlayer(playerstarts[playernum]);
            }
        } 
    } 

    /** DOOM Par Times [4][10] */
    final int[][] pars = { 
        {0}, 
        {0,30,75,120,90,165,180,180,30,165}, 
        {0,90,90,90,120,90,360,240,30,170}, 
        {0,90,45,90,150,90,90,165,30,135} 
    }; 

    /** DOOM II Par Times */
    final int[] cpars = {
        30,90,120,120,90,150,120,120,270,90,    //  1-10
        210,150,150,150,210,150,420,150,210,150,    // 11-20
        240,150,180,150,150,300,330,420,300,180,    // 21-30
        120,30                  // 31-32
    };


    //
    // G_DoCompleted 
    //
    boolean secretexit;

    public final void ExitLevel() {
        secretexit = false;
        gameaction = ga_completed;
    }

    // Here's for the german edition.
    public void SecretExitLevel() {
        // IF NO WOLF3D LEVELS, NO SECRET EXIT!
        secretexit = !(isCommercial() && (wadLoader.CheckNumForName("MAP31") < 0));
        gameaction = ga_completed;
    }

    @SourceCode.Exact
    @G_Game.C(G_DoCompleted)
    protected void DoCompleted() {
        gameaction = ga_nothing;

        for (int i = 0; i < MAXPLAYERS; i++) {
            if (playeringame[i]) {
                G_PlayerFinishLevel: { // take away cards and stuff 
                    players[i].PlayerFinishLevel();
                }
            }
        }

        if (automapactive) {
            AM_Stop: {
                autoMap.Stop();
            }
        }

        if (!isCommercial()) {
            switch (gamemap) {
                case 8:
                    // MAES: end of episode
                    gameaction = ga_victory;
                    return;
                case 9:
                    // MAES: end of secret level
                    for (int i = 0; i < MAXPLAYERS; i++) {
                        players[i].didsecret = true;
                    }
                    break;
                default:
                    break;
            }
        }

        wminfo.didsecret = players[consoleplayer].didsecret;
        wminfo.epsd = gameepisode - 1;
        wminfo.last = gamemap - 1;

        // wminfo.next is 0 biased, unlike gamemap
        if (isCommercial()) {
            if (secretexit) {
                switch(gamemap) {
                    case 2:
                        wminfo.next = 32; //Fix Doom 3 BFG Edition, MAP02 secret exit to MAP33 Betray 
                        break;
                    case 15:
                        wminfo.next = 30;
                        break;
                    case 31:
                        wminfo.next = 31;
                        break;
                    default:
                        break;
                }
            } else switch(gamemap) {
                case 31:
                case 32:
                    wminfo.next = 15;
                    break;
                case 33:
                    wminfo.next = 2; //Fix Doom 3 BFG Edition, MAP33 Betray exit back to MAP03 
                    break;
                default:
                    wminfo.next = gamemap;
            }
        } else {
            if (secretexit) {
                wminfo.next = 8; // go to secret level 
            } else if (gamemap == 9) {
                // returning from secret level 
                switch (gameepisode) {
                    case 1:
                        wminfo.next = 3;
                        break;
                    case 2:
                        wminfo.next = 5;
                        break;
                    case 3:
                        wminfo.next = 6;
                        break;
                    case 4:
                        wminfo.next = 2;
                        break;
                    default:
                        break;
                }
            } else {
                wminfo.next = gamemap; // go to next level 
            }
        }

        wminfo.maxkills = totalkills;
        wminfo.maxitems = totalitems;
        wminfo.maxsecret = totalsecret;
        wminfo.maxfrags = 0;
        
        if (isCommercial()) {
            wminfo.partime = 35 * cpars[gamemap - 1];
        } else if (gameepisode >= pars.length) {
            wminfo.partime = 0;
        } else {
            wminfo.partime = 35 * pars[gameepisode][gamemap];
        }
        
        wminfo.pnum = consoleplayer; 

        for (int i = 0; i < MAXPLAYERS; i++) {
            wminfo.plyr[i].in = playeringame[i];
            wminfo.plyr[i].skills = players[i].killcount;
            wminfo.plyr[i].sitems = players[i].itemcount;
            wminfo.plyr[i].ssecret = players[i].secretcount;
            wminfo.plyr[i].stime = leveltime;
            memcpy(wminfo.plyr[i].frags, players[i].frags, wminfo.plyr[i].frags.length);
        } 

        gamestate = GS_INTERMISSION; 
        viewactive = false; 
        automapactive = false; 

        if (statcopy != null) {
            memcpy(statcopy, wminfo, 1);
        }

        WI_Start: {
            endLevel.Start(wminfo);
        } 
    } 


    /**
     * G_WorldDone 
     */
    public void WorldDone() {
        gameaction = ga_worlddone; 

        if (secretexit) {
            players[consoleplayer].didsecret = true;
        }

        if (isCommercial()) {
            switch (gamemap) {
                case 15:
                case 31:
                    if (!secretexit) {
                        break;
                    }
                case 6:
                case 11:
                case 20:
                case 30:
                    finale.StartFinale();
                    break;
            }
        }
    } 

    public void DoWorldDone() {
        gamestate = GS_LEVEL;
        gamemap = wminfo.next + 1;
        DoLoadLevel();
        gameaction = ga_nothing;
        viewactive = true;
    } 

    //
    // G_InitFromSavegame
    // Can be called by the startup code or the menu task. 
    //
    //extern boolean setsizeneeded;
    //void R_ExecuteSetViewSize (void);
    String savename;

    public void LoadGame(String name) {
        savename = name;
        gameaction = ga_loadgame;
    }

    /** 
     * This is fugly. Making a "savegame object" will make at least certain comparisons easier, and avoid writing code
     * twice.
     */
    @SourceCode.Suspicious(CauseOfDesyncProbability.MEDIUM)
    @G_Game.C(G_DoLoadGame)
    protected void DoLoadGame() {
        try {
            StringBuffer vcheck = new StringBuffer();
            VanillaDSGHeader header = new VanillaDSGHeader();
            IDoomSaveGame dsg = new VanillaDSG<>(this);

            gameaction = ga_nothing;

            DataInputStream f = new DataInputStream(new BufferedInputStream(new FileInputStream(savename)));

            header.read(f);
            f.close();

            // skip the description field 
            vcheck.append("version ");
            vcheck.append(VERSION);

            if (vcheck.toString().compareTo(header.getVersion()) != 0) {
                f.close();
                return; // bad version
            }

            // Ok so far, reopen stream.
            f = new DataInputStream(new BufferedInputStream(new FileInputStream(savename)));
            gameskill = header.getGameskill();
            gameepisode = header.getGameepisode();
            gamemap = header.getGamemap();
            System.arraycopy(header.getPlayeringame(), 0, playeringame, 0, MAXPLAYERS);

            // load a base level 
            G_InitNew: {
                InitNew(gameskill, gameepisode, gamemap);
            }

            if (gameaction == ga_failure) {
                // failure to load. Abort.
                f.close();
                return;
            }

            gameaction = ga_nothing;

            // get the times 
            leveltime = header.getLeveltime();

            boolean ok;
            // dearchive all the modifications
            P_UnArchivePlayers:
            P_UnArchiveWorld: 
            P_UnArchiveThinkers:
            P_UnArchiveSpecials: {
                ok = dsg.doLoad(f);
            }
            f.close();

            // MAES: this will cause a forced exit.
            // The problem is that the status will have already been altered 
            // (perhaps VERY badly) so it makes no sense to progress.
            // If you want it bullet-proof, you could implement
            // a "tentative loading" subsystem, which will only alter the game
            // if everything works out without errors. But who cares :-p
            if (!ok) {
                I_Error: {
                    doomSystem.Error("Bad savegame");
                }
            }

            // done 
            //Z_Free (savebuffer); 
            if (sceneRenderer.getSetSizeNeeded()) {
                R_ExecuteSetViewSize: {
                    sceneRenderer.ExecuteSetViewSize();
                }
            }

            // draw the pattern into the back screen
            R_FillBackScreen: {
                sceneRenderer.FillBackScreen();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //
    // G_SaveGame
    // Called by the menu task.
    // Description is a 24 byte text string 
    //
    public void SaveGame(int slot, String description) {
        savegameslot = slot;
        savedescription = description;
        sendsave = true;
    }

    @SourceCode.Suspicious(CauseOfDesyncProbability.LOW)
    @G_Game.C(G_DoSaveGame)
    protected void DoSaveGame() {

        try {
            String name;
            //char[]    name2=new char[VERSIONSIZE]; 
            String description;
            StringBuffer build = new StringBuffer();
            IDoomSaveGameHeader header = new VanillaDSGHeader();
            IDoomSaveGame dsg = new VanillaDSG<>(this);

            M_CheckParm: {
                if (cVarManager.bool(CommandVariable.CDROM)) {
                    build.append("c:\\doomdata\\");
                }
            }
            
            build.append(String.format("%s%d.dsg", SAVEGAMENAME, savegameslot));
            name = build.toString();

            description = savedescription;

            header.setName(description);
            header.setVersion(String.format("version %d", VERSION));
            header.setGameskill(gameskill);
            header.setGameepisode(gameepisode);
            header.setGamemap(gamemap);
            header.setPlayeringame(playeringame);
            header.setLeveltime(leveltime);
            dsg.setHeader(header);

            // Try opening a save file. No intermediate buffer (performance?)
            try (DataOutputStream f = new DataOutputStream(new FileOutputStream(name))) {
                P_ArchivePlayers:
                P_ArchiveWorld:
                P_ArchiveThinkers:
                P_ArchiveSpecials: {
                    boolean ok = dsg.doSave(f);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Saving is not as destructive as loading.

        gameaction = ga_nothing;
        savedescription = "";

        players[consoleplayer].message = GGSAVED;

        // draw the pattern into the back screen
        R_FillBackScreen: {
            sceneRenderer.FillBackScreen();
        }
    }

    skill_t d_skill;
    int d_episode;
    int d_map;

    public void DeferedInitNew(skill_t skill, int episode, int map) {
        d_skill = skill; 
        d_episode = episode; 
        d_map = map; 
        gameaction = ga_newgame; 
    } 

    @SourceCode.Exact
    @G_Game.C(G_DoNewGame)
    public void DoNewGame() {
        demoplayback = false;
        netdemo = false;
        netgame = false;
        deathmatch = false;
        playeringame[1] = playeringame[2] = playeringame[3] = false;
        respawnparm = false;
        fastparm = false;
        nomonsters = false;
        consoleplayer = 0;
        G_InitNew: {
            InitNew(d_skill, d_episode, d_map);
        }
        gameaction = ga_nothing;
    } 

    /**
     * G_InitNew
     * Can be called by the startup code or the menu task,
     * consoleplayer, displayplayer, playeringame[] should be set. 
     */
    @SourceCode.Compatible
    @G_Game.C(G_InitNew)
    public void InitNew(skill_t skill, int episode, int map) { InitNew(skill, episode, map, false); }
    private void InitNew(skill_t skill, int episode, int map, boolean noSwitchRandom) {
        if (paused) { 
            paused = false; 
            S_ResumeSound: {
                doomSound.ResumeSound();
            } 
        } 

        if (skill.ordinal() > skill_t.sk_nightmare.ordinal()) {
            skill = skill_t.sk_nightmare;
        }

        // This was quite messy with SPECIAL and commented parts.
        // Supposedly hacks to make the latest edition work.
        // It might not work properly.
        if (episode < 1) {
            episode = 1;
        }

        if (isRetail()) {
            if (episode > 4) {
                episode = 4;
            }
        } else if (isShareware()) {
            if (episode > 1) {
                episode = 1; // only start episode 1 on shareware
            }
        } else {
            if (episode > 3) {
                episode = 3;
            }
        }

        if (map < 1) 
            map = 1;

        if ((map > 9) && (!isCommercial())) {
            map = 9;
        }

        /**
         * I wrote it that way. No worries JavaRandom will never be picked on vanilla demo playback
         * - Good Sign 2017/05/08
         * 
         * @SourceCode.Compatible
         */
        if (!noSwitchRandom) {
            if (cVarManager.bool(CommandVariable.JAVARANDOM)) {
                random.requireRandom(VERSION | JAVARANDOM_MASK);
            } else {
                random.requireRandom(VERSION);
            }
        }

        M_ClearRandom: {
            random.ClearRandom ();
        }
        
        respawnmonsters = skill == skill_t.sk_nightmare || respawnparm;

        // If on nightmare/fast monsters make everything MOAR pimp.
        if (fastparm || (skill == skill_t.sk_nightmare && gameskill != skill_t.sk_nightmare) ) { 
            for (int i = statenum_t.S_SARG_RUN1.ordinal(); i <= statenum_t.S_SARG_PAIN2.ordinal(); i++) {
                states[i].tics >>= 1;
            }
            
            mobjinfo[mobjtype_t.MT_BRUISERSHOT.ordinal()].speed = 20 * MAPFRACUNIT;
            mobjinfo[mobjtype_t.MT_HEADSHOT.ordinal()].speed = 20 * MAPFRACUNIT;
            mobjinfo[mobjtype_t.MT_TROOPSHOT.ordinal()].speed = 20 * MAPFRACUNIT;
        } else if (skill != skill_t.sk_nightmare && gameskill == skill_t.sk_nightmare) {
            for (int i = statenum_t.S_SARG_RUN1.ordinal(); i <= statenum_t.S_SARG_PAIN2.ordinal(); i++) {
                states[i].tics <<= 1;
            }
            
            mobjinfo[mobjtype_t.MT_BRUISERSHOT.ordinal()].speed = 15 * MAPFRACUNIT;
            mobjinfo[mobjtype_t.MT_HEADSHOT.ordinal()].speed = 10 * MAPFRACUNIT;
            mobjinfo[mobjtype_t.MT_TROOPSHOT.ordinal()].speed = 10 * MAPFRACUNIT;
        } 

        // force players to be initialized upon first level load         
        for (int i = 0; i < MAXPLAYERS; i++) {
            players[i].playerstate = PST_REBORN;
        }

        // will be set false if a demo 
        usergame = true;
        paused = false; 
        demoplayback = false; 
        automapactive = false; 
        viewactive = true; 
        gameepisode = episode; 
        gamemap = map; 
        gameskill = skill; 
        viewactive = true;

        // set the sky map for the episode
        if (isCommercial()) {
            textureManager.setSkyTexture(textureManager.TextureNumForName("SKY3"));
            if (gamemap < 12) {
                textureManager.setSkyTexture(textureManager.TextureNumForName("SKY1"));
            } else if (gamemap < 21) {
                textureManager.setSkyTexture(textureManager.TextureNumForName("SKY2"));
            }
        } else {
            switch (episode) {
                case 1:
                    textureManager.setSkyTexture(textureManager.TextureNumForName("SKY1"));
                    break;
                case 2:
                    textureManager.setSkyTexture(textureManager.TextureNumForName("SKY2"));
                    break;
                case 3:
                    textureManager.setSkyTexture(textureManager.TextureNumForName("SKY3"));
                    break;
                case 4: // Special Edition sky
                    textureManager.setSkyTexture(textureManager.TextureNumForName("SKY4"));
                    break;
                default:
                    break;
            }
        }

        G_DoLoadLevel: {
            if (!DoLoadLevel()) {
                levelLoadFailure();
            }
        }
    } 

    protected void levelLoadFailure() {
        boolean endgame = doomSystem.GenerateAlert(Strings.LEVEL_FAILURE_TITLE, Strings.LEVEL_FAILURE_CAUSE);

        // Initiate endgame
        if (endgame) {
            gameaction = ga_failure;
            gamestate = GS_DEMOSCREEN;
            menu.ClearMenus();
            StartTitle();
        } else {
            // Shutdown immediately.
            doomSystem.Quit();
        }
    }

    //
    // DEMO RECORDING 
    // 
    public void ReadDemoTiccmd(ticcmd_t cmd) {
        final IDemoTicCmd democmd=demobuffer.getNextTic();
        if (democmd == null)  {
            // end of demo data stream 
            CheckDemoStatus ();

            // Force status resetting
            this.demobuffer.resetDemo();
            return; 
        }

        democmd.decode(cmd); 
    } 

    public void WriteDemoTiccmd(ticcmd_t cmd) {
        // press q to end demo recording 
        if (gamekeydown[key_recordstop]) {
            CheckDemoStatus();
        }
        
        final IDemoTicCmd reccmd = new VanillaTiccmd();
        reccmd.encode(cmd);
        demobuffer.putTic(reccmd);

        // MAES: Useless, we can't run out of space anymore (at least not in theory).

        /*   demobuffer[demo_p++] = cmd.forwardmove; 
     demobuffer[demo_p++] = cmd.sidemove; 
     demobuffer[demo_p++] = (byte) ((cmd.angleturn+128)>>8); 
     demobuffer[demo_p++] = (byte) cmd.buttons; 
     demo_p -= 4; 
     if (demo_p > demoend - 16)
     {
     // no more space 
     CheckDemoStatus (); 
     return; 
     } */ 

        //ReadDemoTiccmd (cmd);         // make SURE it is exactly the same
        // MAES: this is NOT the way to do in Mocha, because we are not manipulating
        // the demo index directly anymore. Instead, decode what we have just saved.     
        reccmd.decode(cmd);
    } 

    /**
     * G_RecordDemo 
     */ 
    public void RecordDemo(String name) {
        StringBuffer buf = new StringBuffer();
        usergame = false;
        buf.append(name);
        buf.append(".lmp");
        demoname = buf.toString();
        demobuffer = new VanillaDoomDemo();
        demorecording = true;
    }

    @G_Game.C(G_BeginRecording)
    public void BeginRecording() {
        demobuffer.setVersion(cVarManager.bool(CommandVariable.JAVARANDOM) ? VERSION | JAVARANDOM_MASK : VERSION);
        demobuffer.setSkill(gameskill);
        demobuffer.setEpisode(gameepisode);
        demobuffer.setMap(gamemap);
        demobuffer.setDeathmatch(deathmatch);
        demobuffer.setRespawnparm(respawnparm);
        demobuffer.setFastparm(fastparm);
        demobuffer.setNomonsters(nomonsters);
        demobuffer.setConsoleplayer(consoleplayer);
        demobuffer.setPlayeringame(playeringame);
    }
    
    String defdemoname;
    
    /**
     * G_PlayDemo 
     */
    public void DeferedPlayDemo(String name) {
        defdemoname = name;
        gameaction = ga_playdemo;
    }

    @SuppressWarnings("UnusedAssignment")
    @SourceCode.Compatible
    @G_Game.C(G_DoPlayDemo)
    public void DoPlayDemo() {

        skill_t skill;
        boolean fail;
        int i, episode, map;

        gameaction = ga_nothing;
        // MAES: Yeah, it's OO all the way now, baby ;-)
        W_CacheLumpName: {
            try {
                demobuffer = wadLoader.CacheLumpName(defdemoname.toUpperCase(), PU_STATIC, VanillaDoomDemo.class);
            } catch (Exception e) {
                fail = true;
            }
        }

        fail = (demobuffer.getSkill() == null);

        final int version;
        if (fail || ((version = demobuffer.getVersion() & 0xFF) & ~JAVARANDOM_MASK) != VERSION) {
            System.err.println("Demo is from a different game version!\n");
            System.err.println("Version code read: " + demobuffer.getVersion());
            gameaction = ga_nothing;
            return;
        }
        
        random.requireRandom(version);

        skill = demobuffer.getSkill();
        episode = demobuffer.getEpisode();
        map = demobuffer.getMap();
        deathmatch = demobuffer.isDeathmatch();
        respawnparm = demobuffer.isRespawnparm();
        fastparm = demobuffer.isFastparm();
        nomonsters = demobuffer.isNomonsters();
        consoleplayer = demobuffer.getConsoleplayer();
        // Do this, otherwise previously loaded demos will be stuck at their end.
        demobuffer.resetDemo();

        boolean[] pigs = demobuffer.getPlayeringame();
        for (i = 0; i < MAXPLAYERS; i++) {
            playeringame[i] = pigs[i];
        }
        if (playeringame[1]) {
            netgame = true;
            netdemo = true;
        }

        // don't spend a lot of time in loadlevel 
        precache = false;
        G_InitNew: {
            InitNew(skill, episode, map, true);
        }
        precache = true;

        usergame = false;
        demoplayback = true;

    }

    //
    // G_TimeDemo 
    //
    public void TimeDemo (String name) 
    {    
        nodrawers = cVarManager.bool(CommandVariable.NODRAW);
        noblit = cVarManager.bool(CommandVariable.NOBLIT);
        timingdemo = true; 
        singletics = true;
        defdemoname = name;
        gameaction = ga_playdemo; 
    } 

    
    /** G_CheckDemoStatus
     * 
     * Called after a death or level completion to allow demos to be cleaned up
     * Returns true if a new demo loop action will take place
     *  
     */ 

    public boolean CheckDemoStatus () 
    { 
        int endtime; 
        
        if (timingdemo) 
        {
            endtime = RealTime.GetTime ();
            // killough -- added fps information and made it work for longer demos:
            long realtics=endtime-starttime;    
            
            this.commit();
            CM.SaveDefaults();
            doomSystem.Error ("timed %d gametics in %d realtics = %f frames per second",gametic 
                , realtics, gametic*(double)(TICRATE)/realtics); 
        }

        if (demoplayback) 
        {
            if (singledemo) 
                doomSystem.Quit ();

            // Z_ChangeTag (demobuffer, PU_CACHE); 
            demoplayback = false; 
            netdemo = false;
            netgame = false;
            deathmatch = false;
            playeringame[1] = playeringame[2] = playeringame[3] = false;
            respawnparm = false;
            fastparm = false;
            nomonsters = false;
            consoleplayer = 0;
            AdvanceDemo (); 
            return true; 
        } 

        if (demorecording) 
        { 
            //demobuffer[demo_p++] = (byte) DEMOMARKER; 

            MenuMisc.WriteFile(demoname, demobuffer); 
            //Z_Free (demobuffer); 
            demorecording = false; 
            doomSystem.Error ("Demo %s recorded",demoname); 
        } 

        return false; 
    } 

    /** This should always be available for real timing */
    protected ITicker RealTime;
    
    // Bookkeeping on players - state.
    public player_t[] players;
    
    public DelegateRandom random;
    public final CVarManager cVarManager;
    public final IWadLoader wadLoader;
    public final IDoomSound doomSound;
    public final ISoundDriver soundDriver;
    public final IMusic music;
    public final AbstractStatusBar statusBar;
    public final DoomGraphicSystem<T, V> graphicSystem;
    public final DoomSystemNetworking systemNetworking;
    public final IDoomGameNetworking gameNetworking;
    public final AbstractLevelLoader levelLoader;
    public final IDoomMenu menu;
    public final ActionFunctions actions;
    public final SceneRenderer<T, V> sceneRenderer;
    public final HU headsUp;
    public final IAutoMap<T, V> autoMap;
    public final Finale<T> finale;
    public final EndLevel<T, V> endLevel;
    public final Wiper wiper;
    public final TextureManager<T> textureManager;
    public final ISpriteManager spriteManager;
    public final ITicker ticker; 
    public final IDiskDrawer diskDrawer;
    public final IDoomSystem doomSystem;
    public final BppMode bppMode;

    /**
     * Since this is a fully OO implementation, we need a way to create
     * the instances of the Refresh daemon, the Playloop, the Wadloader 
     * etc. which however are now completely independent of each other
     * (well, ALMOST), and are typically only passed context when 
     * instantiated.
     * 
     *  If you instantiate one too early, it will have null context.
     *  
     *  The trick is to construct objects in the correct order. Some of
     *  them have Init() methods which are NOT yet safe to call.
     * 
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public DoomMain() throws IOException {
        // Init game status...
        super();

        // Init players
    	players = new player_t[MAXPLAYERS];
        Arrays.setAll(players, i -> new player_t(this));

        // Init objects
        this.cVarManager = Engine.getCVM();

        // Prepare events array with event instances
        Arrays.fill(events, event_t.EMPTY_EVENT);

        // Create DoomSystem
        this.doomSystem = new DoomSystem(this);
        
        // Choose bppMode depending on CVar's
        // TODO: add config options
        this.bppMode = BppMode.chooseBppMode(cVarManager);
        
        // Create real time ticker
        this.RealTime = new MilliTicker();

        // Doommain is both "main" and handles most of the game status.
        this.gameNetworking = this; // DoomMain also handles its own Game Networking.
        
        // Set ticker. It is a shared status object, but not a holder itself.
        this.ticker = ITicker.createTicker(cVarManager);
        
        // Network "driver"
        this.systemNetworking = new DummyNetworkDriver<>(this);
        
        // Random number generator, but we can have others too.
        this.random = new DelegateRandom();
        System.out.print(String.format("M_Random: Using %s.\n", random.getClass().getSimpleName()));
        
        // Sound can be left until later, in Start
        this.wadLoader = new WadLoader(this.doomSystem); // The wadloader is a "weak" status holder.
        
        // TODO: find out if we have requests for a specific resolution,
        // and try honouring them as closely as possible.       

        // 23/5/2011: Experimental dynamic resolution subsystem
        this.vs = VisualSettings.parse(cVarManager);
        this.spriteManager = new SpriteManager<>(this);
        
        // Heads-up, Menu, Level Loader
        this.headsUp = new HU(this);
        this.menu = new Menu<>(this);
        this.levelLoader = new BoomLevelLoader(this);
        
        // Renderer, Actions, StatusBar, AutoMap
        this.sceneRenderer = bppMode.sceneRenderer(this);
        this.actions = new ActionFunctions(this);
        this.statusBar = new StatusBar(this);

        // Let the renderer pick its own. It makes linking easier.
        this.textureManager = sceneRenderer.getTextureManager();
        // Instantiating EndLevel, Finale
        this.endLevel = new EndLevel<>(this);
        this.finale = selectFinale();
        
        readCVars();
        System.out.print("W_Init: Init WADfiles.\n");
        try {
            wadLoader.InitMultipleFiles(wadfiles);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        // Video Renderer
        this.graphicSystem = RendererFactory.<T, V> newBuilder()
            .setVideoScale(vs).setBppMode(bppMode).setWadLoader(wadLoader)
            .build();
        
        System.out.print("V_Init: allocate screens.\n");
        
        // Disk access visualizer
        this.diskDrawer = new DiskDrawer(this, DiskDrawer.STDISK);
        
        // init subsystems
        System.out.print("AM_Init: Init Automap colors - \n");
        this.autoMap = new org.bleachhack.util.doom.automap.Map<>(this);

        this.wiper = graphicSystem.createWiper(random);
        
        // Update variables and stuff NOW.
        this.update();

        // Check for -file in shareware
        CheckForPWADSInShareware();

        printGameInfo();

        System.out.print("Tables.InitTables: Init trigonometric LUTs.\n");
        Tables.InitTables();

        System.out.print("M_Init: Init miscellaneous info.\n");
        menu.Init();

        System.out.print("R_Init: Init DOOM refresh daemon - ");
        sceneRenderer.Init();

        System.out.print("\nP_Init: Init Playloop state.\n");
        actions.Init();

        System.out.print("I_Init: Setting up machine state.\n");
        doomSystem.Init();

        System.out.print("D_CheckNetGame: Checking network game status.\n");
        CheckNetGame();

        System.out.print("S_Init: Setting up sound.\n");

        // Sound "drivers" before the game sound controller.
        this.music = IMusic.chooseModule(cVarManager);
        this.soundDriver = ISoundDriver.chooseModule(this, cVarManager);
        this.doomSound = IDoomSound.chooseSoundIsPresent(this, cVarManager, soundDriver);

        music.InitMusic();
        doomSound.Init(snd_SfxVolume * 8, snd_MusicVolume * 8);

        System.out.print("HU_Init: Setting up heads up display.\n");
        headsUp.Init();

        System.out.print("ST_Init: Init status bar.\n");
        statusBar.Init();

        if (statcopy != null) {
            System.out.print("External statistics registered.\n");
        }

        // NOW it's safe to init the disk reader.
        diskDrawer.Init();
    }

    @Override
    public final void update() {
        super.update();
        // Video...so you should wait until video renderer is active.           
        this.graphicSystem.setUsegamma(CM.getValue(Settings.usegamma, Integer.class));

        // These should really be handled by the menu.
        this.menu.setShowMessages(CM.equals(Settings.show_messages, 1));
        this.menu.setScreenBlocks(CM.getValue(Settings.screenblocks, Integer.class));

        // These should be handled by the HU
        for (int i = 0; i <= 9; i++) {

            String chatmacro = String.format("chatmacro%d", i);
            this.headsUp.setChatMacro(i, CM.getValue(Settings.valueOf(chatmacro), String.class));
        }
    }

    @Override
    public final void commit() {
        super.commit();
        // Video...         
        CM.update(Settings.usegamma, graphicSystem.getUsegamma());

        // These should really be handled by the menu.
        CM.update(Settings.show_messages, this.menu.getShowMessages());
        CM.update(Settings.screenblocks, this.menu.getScreenBlocks());

        // These should be handled by the HU
        for (int i = 0; i <= 9; i++) {
            CM.update(Settings.valueOf(String.format("chatmacro%d", i)), this.headsUp.chat_macros[i]);
        }
    }
    
    public void setupLoop() throws IOException {
        // check for a driver that wants intermission stats
        cVarManager.with(CommandVariable.STATCOPY, 0, (String s) -> {
            // TODO: this should be chained to a logger
            statcopy = s;
            System.out.print("External statistics registered.\n");
        });

        // start the apropriate game based on parms
        cVarManager.with(CommandVariable.RECORD, 0, (String s) -> {
            RecordDemo(s);
            autostart = true;
        });

        //p = CM.CheckParm ("-timedemo");
        ChooseLoop: {
            if (singletics) {
                TimeDemo(loaddemo);
                autostart = true;
                break ChooseLoop; // DoomLoop();  // never returns
            }

            if (fastdemo || normaldemo) {
                singledemo = true;              // quit after one demo
                if (fastdemo) {
                    timingdemo = true;
                }
                InitNew(startskill, startepisode, startmap);
                gamestate = GS_DEMOSCREEN;
                DeferedPlayDemo(loaddemo);
                break ChooseLoop; // DoomLoop();  // never returns
            }

            if (gameaction != ga_loadgame) {
                if (autostart || netgame) {
                    InitNew(startskill, startepisode, startmap);
                } else {
                    StartTitle();                // start up intro loop
                }
            }
        }

        DoomLoop();  // never returns
    }

    private void printGameInfo() {
        // Iff additonal PWAD files are used, print modified banner
        if (modifiedgame) // Generate WAD loading alert. Abort upon denial.
        {
            if (!doomSystem.GenerateAlert(Strings.MODIFIED_GAME_TITLE, Strings.MODIFIED_GAME_DIALOG)) {
                wadLoader.CloseAllHandles();
                System.exit(-2);
            }
        }
        
        // Check and print which version is executed.
        switch (getGameMode()) {
            case shareware:
            case indetermined:
                System.out.print("===========================================================================\n");
                System.out.print("                                Shareware!\n");
                System.out.print("===========================================================================\n");
                break;
            case registered:
            case retail:
            case commercial:
            case pack_tnt:
            case pack_plut:
            case pack_xbla:
                System.out.print("===========================================================================\n");
                System.out.print("                 Commercial product - do not distribute!\n");
                System.out.print("         Please report software piracy to the SPA: 1-800-388-PIR8\n");
                System.out.print("===========================================================================\n");
                break;
            case freedoom1:
            case freedoom2:
            case freedm:
                System.out.print("===========================================================================\n");
                System.out.print("       Copyright © 2001-2017 Contributors to the Freedoom project.\n");
                System.out.print("                            All rights reserved.\n");
                System.out.print("     See: https://github.com/freedoom/freedoom/blob/master/COPYING.adoc\n");
                System.out.print("===========================================================================\n");
                break;
            default:
                // Ouch.
                break;
        }
    }

    private void readCVars() {
        /**
         * D_DoomMain
         *
         * Completes the job started by Init. Here everything priority-critical is called and created in more detail.
         */
        
        final StringBuffer file = new StringBuffer();
        final String iwadfilename = IdentifyVersion();
        nomonsters = cVarManager.bool(CommandVariable.NOMONSTERS);
        respawnparm = cVarManager.bool(CommandVariable.RESPAWN);
        fastparm = cVarManager.bool(CommandVariable.FAST);
        devparm = cVarManager.bool(CommandVariable.DEVPARM);
        
        if (!(altdeath = cVarManager.bool(CommandVariable.ALTDEATH))) {
            deathmatch = cVarManager.bool(CommandVariable.DEATHMATCH);
        }

        // MAES: Check for Ultimate Doom in "doom.wad" filename.
        final WadLoader tmpwad = new WadLoader();
        try {
            tmpwad.InitFile(iwadfilename);
        } catch (Exception e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        // Check using a reloadable hack.
        CheckForUltimateDoom(tmpwad);
        // MAES: better extract a method for this.
        GenerateTitle();
        // Print ticker info. It has already been set at Init() though.
        if (cVarManager.bool(CommandVariable.MILLIS)) {
            System.out.println("ITicker: Using millisecond accuracy timer.");
        } else if (cVarManager.bool(CommandVariable.FASTTIC)) {
            System.out.println("ITicker: Using fastest possible timer.");
        } else {
            System.out.println("ITicker: Using nanosecond accuracy timer.");
        }
        System.out.println(title.toString());
        if (devparm) {
            System.out.println(D_DEVSTR);
        }
        // Running from CDROM?
        if (cVarManager.bool(CommandVariable.CDROM)) {
            System.out.println(D_CDROM);
            //System.get("c:\\doomdata",0);
            //System.out.println (Settings.basedefault+"c:/doomdata/default.cfg");
        }
        // turbo option
        if (cVarManager.specified(CommandVariable.TURBO)) {
            int scale = 200;
            if (cVarManager.present(CommandVariable.TURBO)) {
                scale = cVarManager.get(CommandVariable.TURBO, Integer.class, 0).get();
            }
            if (scale < 10) {
                scale = 10;
            }
            if (scale > 400) {
                scale = 400;
            }
            System.out.println("turbo scale: " + scale);
            forwardmove[0] = forwardmove[0] * scale / 100;
            forwardmove[1] = forwardmove[1] * scale / 100;
            sidemove[0] = sidemove[0] * scale / 100;
            sidemove[1] = sidemove[1] * scale / 100;
        }
        // add any files specified on the command line with -file wadfile
        // to the wad list
        //
        // convenience hack to allow -wart e m to add a wad file
        // prepend a tilde to the filename so wadfile will be reloadable
        if (cVarManager.present(CommandVariable.WART)) {
            final int ep = cVarManager.get(CommandVariable.WART, Integer.class, 0).get();
            final int map = cVarManager.get(CommandVariable.WART, Integer.class, 1).get();
            cVarManager.override(CommandVariable.WARP, new CommandVariable.WarpFormat(ep * 10 + map), 0);
            GameMode gamemode = getGameMode();
            // Map name handling.
            switch (gamemode) {
                case shareware:
                case retail:
                case registered:
                case freedoom1:
                    file.append("~");
                    file.append(DEVMAPS);
                    file.append(String.format("E%dM%d.wad", ep, map));
                    file.append(String.format("Warping to Episode %s, Map %s.\n", ep, map));
                    break;
                case commercial:
                case freedoom2:
                case freedm:
                default:
                    if (ep < 10) {
                        file.append("~");
                        file.append(DEVMAPS);
                        file.append(String.format("cdata/map0%d.wad", ep));
                    } else {
                        file.append("~");
                        file.append(DEVMAPS);
                        file.append(String.format("cdata/map%d.wad", ep));
                    }
                    break;
            }
            AddFile(file.toString());
        }
        
        if (cVarManager.present(CommandVariable.FILE)) {
            // the parms after p are wadfile/lump names,
            // until end of parms or another - preceded parm
            modifiedgame = true; // homebrew levels
            cVarManager.with(CommandVariable.FILE, 0, (String[] a) -> {
                Arrays.stream(a)
                    .map(s -> C2JUtils.unquoteIfQuoted(s, '"'))
                    .forEach(this::AddFile);
            });
        }
        
        if (cVarManager.present(CommandVariable.PLAYDEMO)) {
            normaldemo = true;
            loaddemo = cVarManager.get(CommandVariable.PLAYDEMO, String.class, 0).get();
        } else if (cVarManager.present(CommandVariable.FASTDEMO)) {
            System.out.println("Fastdemo mode. Boundless clock!");
            fastdemo = true;
            loaddemo = cVarManager.get(CommandVariable.FASTDEMO, String.class, 0).get();
        } else if (cVarManager.present(CommandVariable.TIMEDEMO)) {
            singletics = true;
            loaddemo = cVarManager.get(CommandVariable.TIMEDEMO, String.class, 0).get();
        }
        
        // If any of the previous succeeded, try grabbing the filename.
        if (loaddemo != null) {
            loaddemo = C2JUtils.unquoteIfQuoted(loaddemo, '"');
            AddFile(loaddemo + ".lmp");
            System.out.printf("Playing demo %s.lmp.\n", loaddemo);
            autostart = true;
        }
        
        // Subsequent uses of loaddemo use only the lump name.
        loaddemo = C2JUtils.extractFileBase(loaddemo, 0, true);
        // get skill / episode / map from parms
        // FIXME: should get them FROM THE DEMO itself.
        startskill = skill_t.sk_medium;
        startepisode = 1;
        startmap = 1;
        //autostart = false;

        if (cVarManager.present(CommandVariable.NOVERT)) {
            novert = cVarManager.get(CommandVariable.NOVERT, CommandVariable.ForbidFormat.class, 0)
                .filter(CommandVariable.ForbidFormat.FORBID::equals)
                .isPresent();
            
            if (!novert) {
                System.out.println("-novert ENABLED (default)");
            } else {
                System.out.println("-novert DISABLED. Hope you know what you're doing...");
            }
        }

        cVarManager.with(CommandVariable.SKILL, 0, (Integer s) -> {
            startskill = skill_t.values()[s - 1];
            autostart = true;
        });

        cVarManager.with(CommandVariable.EPISODE, 0, (Integer ep) -> {
            startepisode = ep;
            startmap = 1;
            autostart = true;
        });
        
        if (cVarManager.present(CommandVariable.TIMER) && deathmatch) {
            // Good Sign (2017/03/31) How this should work?
            final int time = cVarManager.get(CommandVariable.TIMER, Integer.class, 0).get();
            System.out.print("Levels will end after " + time + " minute");
            if (time > 1) {
                System.out.print("s");
            }
            System.out.print(".\n");
        }
        // OK, and exactly how is this enforced?
        if (cVarManager.bool(CommandVariable.AVG) && deathmatch) {
            System.out.print("Austin Virtual Gaming: Levels will end after 20 minutes\n");
        }
        
        // MAES 31/5/2011: added support for +map variation.
        cVarManager.with(CommandVariable.WARP, 0, (CommandVariable.WarpFormat w) -> {
            final CommandVariable.WarpMetric metric = w.getMetric(isCommercial());
            startepisode = metric.getEpisode();
            startmap = metric.getMap();
            autostart = true;
        });

        // Maes: 1/6/2011 Added +map support
        cVarManager.with(CommandVariable.MAP, 0, (CommandVariable.MapFormat m) -> {
            final CommandVariable.WarpMetric metric = m.getMetric(isCommercial());
            startepisode = metric.getEpisode();
            startmap = metric.getMap();
            autostart = true;
        });

        cVarManager.with(CommandVariable.LOADGAME, 0, (Character c) -> {
            file.delete(0, file.length());
            if (cVarManager.bool(CommandVariable.CDROM)) {
                file.append("c:\\doomdata\\");
            }
            
            file.append(String.format("%s%d.dsg", SAVEGAMENAME, c));
            LoadGame(file.toString());
        });
    }

    /**
     * Since it's so intimately tied, it's less troublesome to merge the "main" and "network" code. 
     */

    /** To be initialized by the DoomNetworkingInterface via a setter */
    //private  doomcom_t   doomcom;   
    //private  doomdata_t  netbuffer;      // points inside doomcom
    protected StringBuilder sb=new StringBuilder();


    //
    // NETWORKING
    //
    // gametic is the tic about to (or currently being) run
    // maketic is the tick that hasn't had control made for it yet
    // nettics[] has the maketics for all players 
    //
    // a gametic cannot be run until nettics[] > gametic for all players
    //

    //ticcmd_t[]  localcmds= new ticcmd_t[BACKUPTICS];

    //ticcmd_t [][]       netcmds=new ticcmd_t [MAXPLAYERS][BACKUPTICS];
    int[] nettics = new int[MAXNETNODES];
    boolean[] nodeingame = new boolean[MAXNETNODES];        // set false as nodes leave game
    boolean[] remoteresend = new boolean[MAXNETNODES];      // set when local needs tics
    int[] resendto = new int[MAXNETNODES];          // set when remote needs tics
    int[] resendcount = new int[MAXNETNODES];

    int[] nodeforplayer = new int[MAXPLAYERS];

    int maketic;
    int lastnettic;
    int skiptics;
    protected int ticdup;

    public int getTicdup() {
        return ticdup;
    }

    public void setTicdup(int ticdup) {
        this.ticdup = ticdup;
    }

    int maxsend; // BACKUPTICS/(2*ticdup)-1;
    
    //void D_ProcessEvents (void); 
    //void G_BuildTiccmd (ticcmd_t *cmd); 
    //void D_DoAdvanceDemo (void);

    // _D_
    boolean     reboundpacket = false;
    doomdata_t  reboundstore = new doomdata_t();



    /** 
     * MAES: interesting. After testing it was found to return the following size:
     *  (8*(netbuffer.numtics+1));
     */

    int NetbufferSize() {
        //    return (int)(((doomdata_t)0).cmds[netbuffer.numtics]);
        return (8 * (netbuffer.numtics + 1));
    }

    protected long NetbufferChecksum() {
        // FIXME -endianess?
        if (NORMALUNIX) {
            return 0; // byte order problems
        }

        /**
         * Here it was trying to get the length of a doomdata_t struct up to retransmit from.
         * l = (NetbufferSize () - (int)&(((doomdata_t *)0)->retransmitfrom))/4;
         * (int)&(((doomdata_t *)0)->retransmitfrom) evaluates to "4"
         * Therefore, l= (netbuffersize - 4)/4
         */
        final int l = (NetbufferSize() - 4) / 4;
        long c = 0x1234567L;
        for (int i = 0; i < l; i++) { // TODO: checksum would be better computer in the netbuffer itself.
        // The C code actually takes all fields into account.
            c += 0;// TODO: (netbuffer->retransmitfrom)[i] * (i+1);
        }
        return c & NCMD_CHECKSUM;
    }
    
    protected int ExpandTics(int low) {
        int delta;

        delta = low - (maketic & 0xff);

        if (delta >= -64 && delta <= 64) {
            return (maketic & ~0xff) + low;
        }
        
        if (delta > 64) {
            return (maketic & ~0xff) - 256 + low;
        }
        
        if (delta < -64) {
            return (maketic & ~0xff) + 256 + low;
        }

        doomSystem.Error("ExpandTics: strange value %d at maketic %d", low, maketic);
        return 0;
    }

    /**
     * HSendPacket
     *
     * Will send out a packet to all involved parties. A special case is the rebound storage, which acts as a local
     * "echo" which is then picked up by the host itself. This is necessary to simulate a 1-node network.
     *
     * @throws IOException
     */
    void HSendPacket(int node, int flags) {
        netbuffer.checksum = (int) (NetbufferChecksum() | flags);

        // Local node's comms are sent to rebound packet, which is 
        // then picked up again. THIS IS VITAL FOR SINGLE-PLAYER
        // SPEED THROTTLING TOO, AS IT RELIES ON NETWORK ACKS/BUSY
        // WAITING.
        if (node == 0) {
            // _D_
            reboundstore.copyFrom(netbuffer);
            reboundpacket = true;
            return;
        }

        if (demoplayback) {
            return;
        }

        if (!netgame) {
            doomSystem.Error("Tried to transmit to another node");
        }

        doomcom.command = CMD_SEND;
        doomcom.remotenode = (short) node;
        doomcom.datalength = (short) NetbufferSize();

        if (debugfile != null) {
            int i;
            int realretrans;
            if (flags(netbuffer.checksum, NCMD_RETRANSMIT)) {
                realretrans = ExpandTics(netbuffer.retransmitfrom);
            } else {
                realretrans = -1;
            }

            logger(debugfile, "send (" + ExpandTics(netbuffer.starttic) + ", " + netbuffer.numtics + ", R "
                    + realretrans + "[" + doomcom.datalength + "]");

            for (i = 0; i < doomcom.datalength; i++) // TODO: get a serialized string representation.
            {
                logger(debugfile, netbuffer.toString() + "\n");
            }
        }

        // This should execute a "send" command for the current stuff in doomcom.
        systemNetworking.NetCmd();
    }

    //
    // HGetPacket
    // Returns false if no packet is waiting
    //
    private boolean HGetPacket () 
    {   
        // Fugly way of "clearing" the buffer.
        sb.setLength(0);
        if (reboundpacket)
        {
            // FIXME: MAES: this looks like a struct copy 
            netbuffer.copyFrom(reboundstore);
            doomcom.remotenode = 0;
            reboundpacket = false;
            return true;
        }

        // If not actually a netgame (e.g. single player, demo) return.
        if (!netgame)
            return false;

        if (demoplayback)
            return false;

        doomcom.command = CMD_GET;
        systemNetworking.NetCmd ();

        // Invalid node?
        if (doomcom.remotenode == -1)
            return false;

        if (doomcom.datalength != NetbufferSize ())
        {
            if (eval(debugfile))
            	logger(debugfile,"bad packet length "+doomcom.datalength+"\n");
            return false;
        }

        if (NetbufferChecksum () != (netbuffer.checksum&NCMD_CHECKSUM) )
        {
            if (eval(debugfile))
            	logger(debugfile,"bad packet checksum\n");
            return false;
        }

        if (eval(debugfile))
        {
            int     realretrans;
            int i;

            if (flags(netbuffer.checksum , NCMD_SETUP))
            	logger(debugfile,"setup packet\n");
            else
            {
                if (flags(netbuffer.checksum , NCMD_RETRANSMIT))
                    realretrans = ExpandTics (netbuffer.retransmitfrom);
                else
                    realretrans = -1;

                sb.append("get ");
                sb.append(doomcom.remotenode);
                sb.append(" = (");
                sb.append(ExpandTics(netbuffer.starttic));
                sb.append(" + ");
                sb.append(netbuffer.numtics);
                sb.append(", R ");
                sb.append(realretrans);
                sb.append(")[");
                sb.append(doomcom.datalength);
                sb.append("]");

                logger(debugfile,sb.toString());

                // Trick: force update of internal buffer.
                netbuffer.pack();

                /**
                 * TODO: Could it be actually writing stuff beyond the boundaries of a single doomdata object?
                 * A doomcom object has a lot of header info, and a single "raw" data placeholder, which by now
                 * should be inside netbuffer....right?
                 **/

                try{
                    for (i = 0; i < doomcom.datalength; i++) {
                        debugfile.write(Integer.toHexString(netbuffer.cached()[i]));
                        debugfile.write('\n');
                    }
                }
                catch( IOException e){} // "Drown" IOExceptions here.
            }
        }
        return true;    
    }


    ////    GetPackets

    StringBuilder exitmsg=new StringBuilder(80);

    public void GetPackets() {
        int netconsole;
        int netnode;
        ticcmd_t src, dest;
        int realend;
        int realstart;

        while (HGetPacket()) {
            if (flags(netbuffer.checksum, NCMD_SETUP)) {
                continue;       // extra setup packet
            }
            netconsole = netbuffer.player & ~PL_DRONE;
            netnode = doomcom.remotenode;

            // to save bytes, only the low byte of tic numbers are sent
            // Figure out what the rest of the bytes are
            realstart = ExpandTics(netbuffer.starttic);
            realend = (realstart + netbuffer.numtics);

            // check for exiting the game
            if (flags(netbuffer.checksum, NCMD_EXIT)) {
                if (!nodeingame[netnode]) {
                    continue;
                }
                nodeingame[netnode] = false;
                playeringame[netconsole] = false;
                exitmsg.insert(0, "Player 1 left the game");
                exitmsg.setCharAt(7, (char) (exitmsg.charAt(7) + netconsole));
                players[consoleplayer].message = exitmsg.toString();
                if (demorecording) {
                    CheckDemoStatus();
                }
                continue;
            }

            // check for a remote game kill
            if (flags(netbuffer.checksum, NCMD_KILL)) {
                doomSystem.Error("Killed by network driver");
            }

            nodeforplayer[netconsole] = netnode;

            // check for retransmit request
            if (resendcount[netnode] <= 0
                    && flags(netbuffer.checksum, NCMD_RETRANSMIT)) {
                resendto[netnode] = ExpandTics(netbuffer.retransmitfrom);
                if (eval(debugfile)) {
                    sb.setLength(0);
                    sb.append("retransmit from ");
                    sb.append(resendto[netnode]);
                    sb.append('\n');
                    logger(debugfile, sb.toString());
                    resendcount[netnode] = RESENDCOUNT;
                }
            } else {
                resendcount[netnode]--;
            }

            // check for out of order / duplicated packet       
            if (realend == nettics[netnode]) {
                continue;
            }

            if (realend < nettics[netnode]) {
                if (eval(debugfile)) {
                    sb.setLength(0);
                    sb.append("out of order packet (");
                    sb.append(realstart);
                    sb.append(" + ");
                    sb.append(netbuffer.numtics);
                    sb.append(")\n");
                    logger(debugfile, sb.toString());
                }
                continue;
            }

            // check for a missed packet
            if (realstart > nettics[netnode]) {
                // stop processing until the other system resends the missed tics
                if (eval(debugfile)) {
                    sb.setLength(0);
                    sb.append("missed tics from ");
                    sb.append(netnode);
                    sb.append(" (");
                    sb.append(realstart);
                    sb.append(" - ");
                    sb.append(nettics[netnode]);
                    sb.append(")\n");
                    logger(debugfile, sb.toString());
                }
                remoteresend[netnode] = true;
                continue;
            }

            // update command store from the packet
            {
                int start;

                remoteresend[netnode] = false;

                start = nettics[netnode] - realstart;
                src = netbuffer.cmds[start];

                while (nettics[netnode] < realend) {
                    dest = netcmds[netconsole][nettics[netnode] % BACKUPTICS];
                    nettics[netnode]++;
                    // MAES: this is a struct copy.
                    src.copyTo(dest);
                    // Advance src
                    start++;

                    //_D_: had to add this (see linuxdoom source). That fixed that damn consistency failure!!!
                    if (start < netbuffer.cmds.length) {
                        src = netbuffer.cmds[start];
                    }

                }
            }
        }
    }

    protected void logger(OutputStreamWriter debugfile, String string) {
        try {
            debugfile.write(string);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    int gametime;

    @Override
    public void NetUpdate() {
        int nowtime;
        int newtics;
        int i, j;
        int realstart;
        int gameticdiv;

        // check time
        nowtime = ticker.GetTime() / ticdup;
        newtics = nowtime - gametime;
        gametime = nowtime;

        if (newtics <= 0) { // nothing new to update
            // listen for other packets
            GetPackets();
        } else {

            if (skiptics <= newtics) {
                newtics -= skiptics;
                skiptics = 0;
            } else {
                skiptics -= newtics;
                newtics = 0;
            }

            netbuffer.player = (byte) consoleplayer;

            // build new ticcmds for console player
            gameticdiv = gametic / ticdup;
            for (i = 0; i < newtics; i++) {
                //videoInterface.StartTic();
                ProcessEvents();
                if (maketic - gameticdiv >= BACKUPTICS / 2 - 1) {
                    break;          // can't hold any more
                }
                //System.out.printf ("mk:%d ",maketic);
                BuildTiccmd(localcmds[maketic % BACKUPTICS]);
                maketic++;
            }

            if (singletics) {
                return;         // singletic update is syncronous
            }
            // send the packet to the other nodes
            for (i = 0; i < doomcom.numnodes; i++) {
                if (nodeingame[i]) {
                    netbuffer.starttic = (byte) (realstart = resendto[i]);
                    netbuffer.numtics = (byte) (maketic - realstart);
                    if (netbuffer.numtics > BACKUPTICS) {
                        doomSystem.Error("NetUpdate: netbuffer.numtics > BACKUPTICS");
                    }

                    resendto[i] = maketic - doomcom.extratics;

                    for (j = 0; j < netbuffer.numtics; j++) {
                        localcmds[(realstart + j) % BACKUPTICS].copyTo(netbuffer.cmds[j]);
                    }
                    // MAES: one of _D_ fixes.
                    //netbuffer.cmds[j] = localcmds[(realstart+j)%BACKUPTICS];

                    if (remoteresend[i]) {
                        netbuffer.retransmitfrom = (byte) nettics[i];
                        HSendPacket(i, NCMD_RETRANSMIT);
                    } else {
                        netbuffer.retransmitfrom = 0;
                        HSendPacket(i, 0);
                    }
                }
            }
            GetPackets();
        }
    }

    //
    // CheckAbort
    //
    private void CheckAbort ()
    {
        event_t ev;
        int     stoptic;

        stoptic = ticker.GetTime () + 2; 
        while (ticker.GetTime() < stoptic) {}
            //videoInterface.StartTic (); 

        //videoInterface.StartTic ();
        for (; eventtail != eventhead; eventtail = (++eventtail) & (MAXEVENTS - 1)) {
            ev = events[eventtail]; 
            if (ev.isKey(SC_ESCAPE, ev_keydown)) {
                doomSystem.Error ("Network game synchronization aborted.");
            }
        } 
    }

    boolean[] gotinfo=new boolean[MAXNETNODES];

    /**
     * D_ArbitrateNetStart
     * @throws IOException 
     *
     * 
     */
    public void ArbitrateNetStart() throws IOException
    {
        int i;
        autostart = true;

        // Clear it up...
        memset(gotinfo, false, gotinfo.length);
        if (doomcom.consoleplayer != 0) {
            // listen for setup info from key player
            System.out.println("listening for network start info...\n");
            while (true) {
                CheckAbort();

                if (!HGetPacket()) {
                    continue;
                }

                if (flags(netbuffer.checksum, NCMD_SETUP)) {
                    if (netbuffer.player != VERSION) {
                        doomSystem.Error("Different DOOM versions cannot play a net game!");
                    }
                    startskill = skill_t.values()[netbuffer.retransmitfrom & 15];

                    if (((netbuffer.retransmitfrom & 0xc0) >> 6) == 1) {
                        // Deathmatch
                        deathmatch = true;
                    } else if (((netbuffer.retransmitfrom & 0xc0) >> 6) == 2) {
                        // Cooperative
                        altdeath = true;
                    }

                    nomonsters = (netbuffer.retransmitfrom & 0x20) > 0;
                    respawnparm = (netbuffer.retransmitfrom & 0x10) > 0;
                    startmap = netbuffer.starttic & 0x3f;
                    startepisode = netbuffer.starttic >> 6;
                    return;
                }
            }
        } else {
            // key player, send the setup info
            System.out.println("sending network start info...\n");
            do {
                CheckAbort();
                for (i = 0; i < doomcom.numnodes; i++) {
                    netbuffer.retransmitfrom = (byte) startskill.ordinal();
                    if (deathmatch) {
                        netbuffer.retransmitfrom |= (1 << 6);
                    } else if (altdeath) {
                        netbuffer.retransmitfrom |= (2 << 6);
                    }
                    
                    if (nomonsters) {
                        netbuffer.retransmitfrom |= 0x20;
                    }
                    
                    if (respawnparm) {
                        netbuffer.retransmitfrom |= 0x10;
                    }
                    
                    netbuffer.starttic = (byte) (startepisode * 64 + startmap);
                    netbuffer.player = VERSION;
                    netbuffer.numtics = 0;
                    HSendPacket(i, NCMD_SETUP);
                }

                //#if 1
                for (i = 10; (i > 0) && HGetPacket(); --i) {
                    if ((netbuffer.player & 0x7f) < MAXNETNODES) {
                        gotinfo[netbuffer.player & 0x7f] = true;
                    }
                }
                /*
                while (HGetPacket ())
                {
                gotinfo[netbuffer.player&0x7f] = true;
                }
                */

                for (i = 1; i < doomcom.numnodes; i++) {
                    if (!gotinfo[i]) {
                        break;
                    }
                }
            } while (i < doomcom.numnodes);
        }
    }

    /**
     * D_CheckNetGame
     * Works out player numbers among the net participants
     **/
    private void CheckNetGame() throws IOException {
        for (int i = 0; i < MAXNETNODES; i++) {
            nodeingame[i] = false;
            nettics[i] = 0;
            remoteresend[i] = false; // set when local needs tics
            resendto[i] = 0; // which tic to start sending
        }

        // I_InitNetwork sets doomcom and netgame
        systemNetworking.InitNetwork();
        if (doomcom.id != DOOMCOM_ID) {
            doomSystem.Error("Doomcom buffer invalid!");
        }

        // Maes: This is the only place where netbuffer is definitively set to something
        netbuffer = doomcom.data;
        consoleplayer = displayplayer = doomcom.consoleplayer;
        if (netgame) {
            ArbitrateNetStart();
        }

        System.out.printf("startskill %s  deathmatch: %s  startmap: %d  startepisode: %d\n",
                startskill.toString(), Boolean.toString(deathmatch), startmap, startepisode);

        // read values out of doomcom
        ticdup = doomcom.ticdup;
        // MAES: ticdup must not be zero at this point. Obvious, no?
        maxsend = BACKUPTICS / (2 * ticdup) - 1;
        if (maxsend < 1) {
            maxsend = 1;
        }

        for (int i = 0; i < doomcom.numplayers; i++) {
            playeringame[i] = true;
        }
        
        for (int i = 0; i < doomcom.numnodes; i++) {
            nodeingame[i] = true;
        }

        System.out.printf("player %d of %d (%d nodes)\n", (consoleplayer + 1), doomcom.numplayers, doomcom.numnodes);
    }

    /**
     * D_QuitNetGame
     * Called before quitting to leave a net game
     * without hanging the other players
     **/
    @Override
    public void QuitNetGame() throws IOException {
        if (eval(debugfile)) {
            try {
                debugfile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!netgame || !usergame || consoleplayer == -1 || demoplayback) {
            return;
        }

        // send a bunch of packets for security
        netbuffer.player = (byte) consoleplayer;
        netbuffer.numtics = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 1; j < doomcom.numnodes; j++) {
                if (nodeingame[j]) {
                    HSendPacket(j, NCMD_EXIT);
                }
            }
            doomSystem.WaitVBL(1);
        }
    }

    /**
     * TryRunTics
     **/
    int[] frametics = new int[4];
    int frameon;
    boolean[] frameskip = new boolean[4];
    int oldnettics;
    int oldentertics;

    @Override
    public void TryRunTics() throws IOException {
        int i;
        int lowtic;
        int entertic;

        int realtics;
        int availabletics;
        int counts;
        int numplaying;

        // get real tics        
        entertic = ticker.GetTime() / ticdup;
        realtics = entertic - oldentertics;
        oldentertics = entertic;

        //System.out.printf("Entertic %d, realtics %d, oldentertics %d\n",entertic,realtics,oldentertics);
        // get available tics
        NetUpdate();

        lowtic = MAXINT;
        numplaying = 0;
        for (i = 0; i < doomcom.numnodes; i++) {
            if (nodeingame[i]) {
                numplaying++;
                if (nettics[i] < lowtic) {
                    lowtic = nettics[i];
                }
            }
        }
        availabletics = lowtic - gametic / ticdup;

        // decide how many tics to run
        if (realtics < availabletics - 1) {
            counts = realtics + 1;
        } else if (realtics < availabletics) {
            counts = realtics;
        } else {
            counts = availabletics;
        }

        if (counts < 1) {
            counts = 1;
        }

        frameon++;

        if (eval(debugfile)) {
            sb.setLength(0);
            sb.append("=======real: ");
            sb.append(realtics);
            sb.append("  avail: ");
            sb.append(availabletics);
            sb.append("  game: ");
            sb.append(counts);
            sb.append("\n");
            debugfile.write(sb.toString());
        }

        if (!demoplayback) {
            // ideally nettics[0] should be 1 - 3 tics above lowtic
            // if we are consistantly slower, speed up time
            for (i = 0; i < MAXPLAYERS; i++) {
                if (playeringame[i]) {
                    break;
                }
            }
            if (consoleplayer == i) {
                // the key player does not adapt
            } else {
                if (nettics[0] <= nettics[nodeforplayer[i]]) {
                    gametime--;
                    System.out.print("-");
                }
                frameskip[frameon & 3] = oldnettics > nettics[nodeforplayer[i]];
                oldnettics = nettics[0];
                if (frameskip[0] && frameskip[1] && frameskip[2] && frameskip[3]) {
                    skiptics = 1;
                    System.out.print("+");
                }
            }
        } // demoplayback

        // wait for new tics if needed
        while (lowtic < gametic / ticdup + counts) {
            NetUpdate();
            lowtic = MAXINT;

            // Finds the node with the lowest number of tics.
            for (i = 0; i < doomcom.numnodes; i++) {
                if (nodeingame[i] && nettics[i] < lowtic) {
                    lowtic = nettics[i];
                }
            }

            if (lowtic < gametic / ticdup) {
                doomSystem.Error("TryRunTics: lowtic < gametic");
            }

            // don't stay in here forever -- give the menu a chance to work
            int time = ticker.GetTime();
            if (time / ticdup - entertic >= 20) {
                menu.Ticker();
                return;
            }
        }

        // run the count * ticdup dics
        while (counts-- > 0) {
            for (i = 0; i < ticdup; i++) {
                if (gametic / ticdup > lowtic) {
                    doomSystem.Error("gametic>lowtic");
                }
                if (advancedemo) {
                    DoAdvanceDemo();
                }
                menu.Ticker();
                Ticker();
                gametic++;

                // modify command for duplicated tics
                if (i != ticdup - 1) {
                    ticcmd_t cmd;
                    int buf;
                    int j;

                    buf = (gametic / ticdup) % BACKUPTICS;
                    for (j = 0; j < MAXPLAYERS; j++) {
                        cmd = netcmds[j][buf];
                        cmd.chatchar = 0;
                        if (flags(cmd.buttons, BT_SPECIAL)) {
                            cmd.buttons = 0;
                        }
                    }
                }
            }
            NetUpdate();   // check for new console commands
        }
    }

    @Override
    public doomcom_t getDoomCom() {
        return this.doomcom;
    }

    @Override
    public void setDoomCom(doomcom_t doomcom) {
        this.doomcom=doomcom;
    }

    @Override
    public void setGameAction(gameaction_t action) {
        this.gameaction=action;
    }

    @Override
    public gameaction_t getGameAction() {       
        return this.gameaction;
    }

    public final VideoScale vs;

    public boolean shouldPollLockingKeys() {
        if (keysCleared) {
            keysCleared = false;
            return true;
        }
        return false;
    }
    
    private String findFileNameToSave() {
        String format = "DOOM%d%d%d%d.png";
        String lbmname = null;
        // find a file name to save it to
        int[] digit = new int[4];
        int i;
        for (i = 0; i <= 9999; i++) {
            digit[0] = ((i / 1000) % 10);
            digit[1] = ((i / 100) % 10);
            digit[2] = ((i / 10) % 10);
            digit[3] = (i % 10);
            lbmname = String.format(format, digit[0], digit[1], digit[2], digit[3]);
            if (!C2JUtils.testReadAccess(lbmname)) {
                break;  // file doesn't exist
            }
        }
        if (i == 10000) {
            doomSystem.Error("M_ScreenShot: Couldn't create a PNG");
        }
        return lbmname;
    }

    protected final Finale<T> selectFinale() {
        return new Finale<>(this);
    }

    /**
    *  M_Screenshot
    *  
    *  Currently saves PCX screenshots, and only in devparm.
    *  Very oldschool ;-)
    *  
    *  TODO: add non-devparm hotkey for screenshots, sequential screenshot
    *  messages, option to save as either PCX or PNG. Also, request
    *  current palette from VI (otherwise gamma settings and palette effects
    *  don't show up).
    *  
    */
    public void ScreenShot() {
        // find a file name to save it to
        final String lbmname = findFileNameToSave(); // file doesn't exist

        if (graphicSystem.writeScreenShot(lbmname, FG)) {
            players[consoleplayer].message = SCREENSHOT;
        }
    }
}

//$Log: DoomMain.java,v $
//Revision 1.109  2012/11/06 16:04:58  velktron
//Variables manager less tightly integrated.
//
//Revision 1.108  2012/11/05 17:25:29  velktron
//Fixed tinting system according to SodaHolic's advice.
//
//Revision 1.107  2012/09/27 16:53:46  velktron
//Stupid brokeness prevented -loadgame from working.
//
//Revision 1.106  2012/09/26 23:15:20  velktron
//Parallel renderer restored...sort of.
//
//Revision 1.105  2012/09/26 15:54:22  velktron
//Spritemanager is set up by renderer.
//
//Revision 1.104  2012/09/24 22:36:49  velktron
//Fixed HOM detection.
//
//Revision 1.103  2012/09/24 17:16:22  velktron
//Massive merge between HiColor and HEAD. There's no difference from now on, and development continues on HEAD.
//
//Revision 1.101.2.11  2012/09/24 16:58:06  velktron
//TrueColor, Generics.
//
//Revision 1.101.2.10  2012/09/21 16:17:25  velktron
//More generic.
//
//Revision 1.101.2.9  2012/09/20 14:25:13  velktron
//Unified DOOM!!!
//
