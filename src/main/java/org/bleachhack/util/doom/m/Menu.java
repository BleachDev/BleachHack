package org.bleachhack.util.doom.m;

import static org.bleachhack.util.doom.data.Defines.HU_FONTSIZE;
import static org.bleachhack.util.doom.data.Defines.HU_FONTSTART;
import static org.bleachhack.util.doom.data.Defines.PU_CACHE;
import static org.bleachhack.util.doom.data.Defines.SAVESTRINGSIZE;
import static org.bleachhack.util.doom.data.dstrings.NUM_QUITMESSAGES;
import static org.bleachhack.util.doom.data.dstrings.SAVEGAMENAME;
import static org.bleachhack.util.doom.data.dstrings.endmsg;
import org.bleachhack.util.doom.data.sounds.sfxenum_t;
import org.bleachhack.util.doom.defines.*;
import org.bleachhack.util.doom.doom.CommandVariable;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.SourceCode;
import org.bleachhack.util.doom.doom.SourceCode.M_Menu;
import static org.bleachhack.util.doom.doom.SourceCode.M_Menu.M_Responder;
import static org.bleachhack.util.doom.doom.SourceCode.M_Menu.M_StartControlPanel;
import static org.bleachhack.util.doom.doom.SourceCode.M_Menu.M_Ticker;
import org.bleachhack.util.doom.doom.englsh;
import static org.bleachhack.util.doom.doom.englsh.DOSY;
import static org.bleachhack.util.doom.doom.englsh.EMPTYSTRING;
import static org.bleachhack.util.doom.doom.englsh.ENDGAME;
import static org.bleachhack.util.doom.doom.englsh.GAMMALVL0;
import static org.bleachhack.util.doom.doom.englsh.GAMMALVL1;
import static org.bleachhack.util.doom.doom.englsh.GAMMALVL2;
import static org.bleachhack.util.doom.doom.englsh.GAMMALVL3;
import static org.bleachhack.util.doom.doom.englsh.GAMMALVL4;
import static org.bleachhack.util.doom.doom.englsh.LOADNET;
import static org.bleachhack.util.doom.doom.englsh.MSGOFF;
import static org.bleachhack.util.doom.doom.englsh.MSGON;
import static org.bleachhack.util.doom.doom.englsh.NETEND;
import static org.bleachhack.util.doom.doom.englsh.NEWGAME;
import static org.bleachhack.util.doom.doom.englsh.NIGHTMARE;
import static org.bleachhack.util.doom.doom.englsh.QLOADNET;
import static org.bleachhack.util.doom.doom.englsh.QLPROMPT;
import static org.bleachhack.util.doom.doom.englsh.QSAVESPOT;
import static org.bleachhack.util.doom.doom.englsh.QSPROMPT;
import static org.bleachhack.util.doom.doom.englsh.SAVEDEAD;
import static org.bleachhack.util.doom.doom.englsh.SWSTRING;
import org.bleachhack.util.doom.doom.event_t;
import org.bleachhack.util.doom.doom.evtype_t;
import org.bleachhack.util.doom.g.Signals.ScanCode;
import static org.bleachhack.util.doom.g.Signals.ScanCode.*;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import org.bleachhack.util.doom.rr.patch_t;
import org.bleachhack.util.doom.timing.DelegateTicker;
import org.bleachhack.util.doom.utils.C2JUtils;
import static org.bleachhack.util.doom.v.renderers.DoomScreen.*;
import org.bleachhack.util.doom.wad.DoomIO;

public class Menu<T, V> extends AbstractDoomMenu<T, V> {

	////////////////// CONSTRUCTOR ////////////////
    
    public Menu(DoomMain<T, V> DOOM){
    	super(DOOM);
    }

    /** The fonts  ... must "peg" them to those from HU */
    patch_t[] hu_font = new patch_t[HU_FONTSIZE];

    /** WTF?! */

    boolean message_dontfuckwithme;
    
    // int mouseSensitivity; // has default

    /** Show messages has default, 0 = off, 1 = on */

    private boolean showMessages=false;

    /**
     * showMessages can be read outside of Menu, but not modified. Menu has the
     * actual C definition (not declaration)
     */
    
    @Override
    public boolean getShowMessages() {
        return showMessages;
    }

    @Override
    public void setShowMessages(boolean val) {
        this.showMessages=val;
    }
    
    /** Blocky mode, has default, 0 = high, 1 = normal */
    int detailLevel;

    int screenblocks=10; // has default

    /** temp for screenblocks (0-9) */
    int screenSize;

    /** -1 = no quicksave slot picked! */
    int quickSaveSlot;

    /** 1 = message to be printed */
    boolean messageToPrint;

    /** ...and here is the message string! */
    String messageString;

    /** message x & y */
    int messx, messy;

    boolean messageLastMenuActive;

    /** timed message = no input from user */
    boolean messageNeedsInput;

    /** Probably I need some MessageRoutine interface at this point? */
    public MenuRoutine messageRoutine;


    /** we are going to be entering a savegame string */
    boolean saveStringEnter;

    int saveSlot; // which slot to save in

    int saveCharIndex; // which char we're editing

    /** old save description before edit */
    char[] saveOldString = new char[SAVESTRINGSIZE];

    boolean inhelpscreens;

    //int menuactive;

    protected static final int SKULLXOFF = -32;

    protected static final int LINEHEIGHT = 16;

    char[][] savegamestrings = new char[10][SAVESTRINGSIZE];

    String endstring = new String();

    //
    // MENU TYPEDEFS
    //

    /** menu item skull is on */
    short itemOn;

    /** skull animation counter */
    short skullAnimCounter;

    /** which skull to draw */
    short whichSkull;

    /**
     * graphic name of skulls warning: initializer-string for array of chars is
     * too long
     */
    private static String[] skullName = { "M_SKULL1", "M_SKULL2" };

    /** current menudef */
    // MAES: pointer? array?
    menu_t currentMenu;

    //
    // DOOM MENU
    //

    // MAES: was an enum called "main_e" used purely as numerals. No need for
    // strong typing.

    /**
     * MenuRoutine class definitions, replacing "function pointers".
     */
    MenuRoutine ChangeDetail, ChangeMessages, ChangeSensitivity, ChooseSkill,
            EndGame, EndGameResponse, Episode, FinishReadThis, LoadGame,
            LoadSelect, MusicVol, NewGame, Options, VerifyNightmare,
            SaveSelect, SfxVol, SizeDisplay, SaveGame, Sound, QuitDOOM,
            QuitResponse, QuickLoadResponse, QuickSaveResponse, ReadThis, ReadThis2;

    /** DrawRoutine class definitions, replacing "function pointers". */

    DrawRoutine DrawEpisode, DrawLoad,DrawMainMenu,DrawNewGame,DrawOptions,
    			DrawReadThis1, DrawReadThis2, DrawSave, DrawSound;

    /** Initialize menu routines first */
    
    private void initMenuRoutines() {
        ChangeMessages = new M_ChangeMessages();
        ChangeDetail = new M_ChangeDetail();
        ChangeSensitivity = new M_ChangeSensitivity();
        ChooseSkill = new M_ChooseSkill();
        EndGame = new M_EndGame();
        EndGameResponse = new M_EndGameResponse();
        Episode = new M_Episode();
        FinishReadThis=new M_FinishReadThis();
        LoadGame=new M_LoadGame();
        LoadSelect=new M_LoadSelect();
        MusicVol=new M_MusicVol();
        NewGame = new M_NewGame();
        Options = new M_Options();


        QuitDOOM = new M_QuitDOOM();
        QuickLoadResponse = new M_QuickLoadResponse();
        QuickSaveResponse= new M_QuickSaveResponse();
        QuitResponse = new M_QuitResponse();
        
        ReadThis = new M_ReadThis();
        ReadThis2 = new M_ReadThis2();
         
        SaveGame=new M_SaveGame();
        SaveSelect= new M_SaveSelect();
        SfxVol=new M_SfxVol();
        SizeDisplay = new M_SizeDisplay();
        Sound = new M_Sound();
        VerifyNightmare = new M_VerifyNightmare();
    }

    /** Then drawroutines */
    
    private void initDrawRoutines() {
        DrawEpisode = new M_DrawEpisode();
        DrawNewGame = new M_DrawNewGame();
        DrawReadThis1 = new M_DrawReadThis1();
        DrawReadThis2 = new M_DrawReadThis2();
        DrawOptions = new M_DrawOptions();
        DrawLoad = new M_DrawLoad();
        DrawSave = new M_DrawSave();
        DrawSound=new M_DrawSound();
        DrawMainMenu = new M_DrawMainMenu();
    }

    /** Menuitem definitions. A "menu" can consist of multiple menuitems */
    menuitem_t[] MainMenu,EpisodeMenu,NewGameMenu, OptionsMenu,ReadMenu1,ReadMenu2,SoundMenu,LoadMenu,SaveMenu;
    
    /** Actual menus. Each can point to an array of menuitems */
    menu_t MainDef, EpiDef,NewDef,OptionsDef,ReadDef1, ReadDef2,SoundDef,LoadDef,SaveDef;
    
    /** First initialize those */
    
    private void initMenuItems(){
        MainMenu = new menuitem_t[] {
            new menuitem_t(1, "M_NGAME", NewGame, SC_N),
            new menuitem_t(1, "M_OPTION", Options, SC_O),
            new menuitem_t(1, "M_LOADG", LoadGame, SC_L),
            new menuitem_t(1, "M_SAVEG", SaveGame, SC_S),
            // Another hickup with Special edition.
            new menuitem_t(1, "M_RDTHIS", ReadThis, SC_R),
            new menuitem_t(1, "M_QUITG", QuitDOOM, SC_Q)
        };

        MainDef = new menu_t(main_end, null, MainMenu, DrawMainMenu, 97, 64, 0);

        //
        // EPISODE SELECT
        //
        EpisodeMenu = new menuitem_t[] {
            new menuitem_t(1, "M_EPI1", Episode, SC_K),
            new menuitem_t(1, "M_EPI2", Episode, SC_T),
            new menuitem_t(1, "M_EPI3", Episode, SC_I),
            new menuitem_t(1, "M_EPI4", Episode, SC_T)
        };

        EpiDef = new menu_t(
            ep_end, // # of menu items
            MainDef, // previous menu
            EpisodeMenu, // menuitem_t ->
            DrawEpisode, // drawing routine ->
            48, 63, // x,y
            ep1 // lastOn
        );

        //
        // NEW GAME
        //
        NewGameMenu = new menuitem_t[] {
            new menuitem_t(1, "M_JKILL", ChooseSkill, SC_I),
            new menuitem_t(1, "M_ROUGH", ChooseSkill, SC_H),
            new menuitem_t(1, "M_HURT", ChooseSkill, SC_H),
            new menuitem_t(1, "M_ULTRA", ChooseSkill, SC_U),
            new menuitem_t(1, "M_NMARE", ChooseSkill, SC_N)
        };

        NewDef = new menu_t(
            newg_end, // # of menu items
            EpiDef, // previous menu
            NewGameMenu, // menuitem_t ->
            DrawNewGame, // drawing routine ->
            48, 63, // x,y
            hurtme // lastOn
        );

        //
        // OPTIONS MENU
        //
        OptionsMenu = new menuitem_t[] {
            new menuitem_t(1, "M_ENDGAM", EndGame, SC_3),
            new menuitem_t(1, "M_MESSG", ChangeMessages, SC_M),
            new menuitem_t(1, "M_DETAIL", ChangeDetail, SC_G),
            new menuitem_t(2, "M_SCRNSZ", SizeDisplay, SC_S),
            new menuitem_t(-1, "", null),
            new menuitem_t(2, "M_MSENS", ChangeSensitivity, SC_M),
            new menuitem_t(-1, "", null),
            new menuitem_t(1, "M_SVOL", Sound, SC_S)
        };

        OptionsDef = new menu_t(opt_end, this.MainDef, OptionsMenu, DrawOptions, 60, 37, 0);

        // Read This! MENU 1 
        ReadMenu1 = new menuitem_t[]{new menuitem_t(1, "", ReadThis2, SC_0)};

        ReadDef1 = new menu_t(read1_end, MainDef, ReadMenu1, DrawReadThis1, 280, 185, 0);

        // Read This! MENU 2
        ReadMenu2 = new menuitem_t[]{new menuitem_t(1, "", FinishReadThis, SC_0)};

        ReadDef2 = new menu_t(read2_end, ReadDef1, ReadMenu2, DrawReadThis2, 330, 175, 0);

        //
        // SOUND VOLUME MENU
        //
        SoundMenu = new menuitem_t[] {
            new menuitem_t(2, "M_SFXVOL", SfxVol, SC_S),
            new menuitem_t(-1, "", null),
            new menuitem_t(2, "M_MUSVOL", MusicVol, SC_M),
            new menuitem_t(-1, "", null)
        };

        SoundDef = new menu_t(sound_end, OptionsDef, SoundMenu, DrawSound, 80, 64, 0);

        //
        // LOAD GAME MENU
        //
        LoadMenu = new menuitem_t[]{new menuitem_t(1, "", LoadSelect, SC_1),
            new menuitem_t(1, "", LoadSelect, SC_2),
            new menuitem_t(1, "", LoadSelect, SC_3),
            new menuitem_t(1, "", LoadSelect, SC_4),
            new menuitem_t(1, "", LoadSelect, SC_5),
            new menuitem_t(1, "", LoadSelect, SC_6)};

        LoadDef
                = new menu_t(load_end, MainDef, LoadMenu, DrawLoad, 80, 54, 0);

        //
        // SAVE GAME MENU
        //
        SaveMenu = new menuitem_t[] {
            new menuitem_t(1, "", SaveSelect, SC_1),
            new menuitem_t(1, "", SaveSelect, SC_2),
            new menuitem_t(1, "", SaveSelect, SC_3),
            new menuitem_t(1, "", SaveSelect, SC_4),
            new menuitem_t(1, "", SaveSelect, SC_5),
            new menuitem_t(1, "", SaveSelect, SC_6)
        };

        SaveDef = new menu_t(load_end, MainDef, SaveMenu, DrawSave, 80, 54, 0);
    }
    
    /**
     * M_ReadSaveStrings
     * read the strings from the savegame files
     */
    
    public void ReadSaveStrings() {
        DataInputStream handle;
        int count;
        int i;
        String name;

        for (i = 0; i < load_end; i++) {
            if (DOOM.cVarManager.bool(CommandVariable.CDROM))
                name = "c:\\doomdata\\" + SAVEGAMENAME + (i) + ".dsg";
            else
                name = SAVEGAMENAME + (i) + ".dsg";

            try {
                handle = new DataInputStream(new BufferedInputStream(new FileInputStream(name)));
                savegamestrings[i] =
                    DoomIO.readString(handle,SAVESTRINGSIZE).toCharArray();
                handle.close();
                LoadMenu[i].status = 1;
            } catch (IOException e) {
                savegamestrings[i][0] = 0x00;
                LoadMenu[i].status = 0;
                continue;
            }

        }
    }

    /**
     * Draw border for the savegame description. This is special in that it's
     * not "invokable" like the other drawroutines, but standalone.
     */
    private void DrawSaveLoadBorder(int x, int y) {
        int i;

        DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("M_LSLEFT"), DOOM.vs, x - 8, y + 7);

        for (i = 0; i < 24; i++) {
            DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("M_LSCNTR"), DOOM.vs, x, y + 7);
            x += 8;
        }

        DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("M_LSRGHT"), DOOM.vs, x, y + 7);
    }

    /** Draws slider rail of a specified width (each notch is 8 base units wide)
     *  and with a slider selector at position thermDot.
     * 
     * @param x
     * @param y
     * @param thermWidth
     * @param thermDot
     */
    
    public void DrawThermo(int x, int y, int thermWidth, int thermDot) {
        int xx = x;
        DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("M_THERML"), DOOM.vs, xx, y);
        xx += 8;
        for (int i = 0; i < thermWidth; i++) {
            DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("M_THERMM"), DOOM.vs, xx, y);
            xx += 8;
        }
        DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("M_THERMR"), DOOM.vs, xx, y);
        DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("M_THERMO"), DOOM.vs, (x + 8) + thermDot * 8, y);
    }

    public void DrawEmptyCell(menu_t menu, int item) {
        DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CacheLumpName("M_CELL1", PU_CACHE, patch_t.class), DOOM.vs, menu.x - 10, menu.y + item * LINEHEIGHT - 1);
    }

    public void DrawSelCell(menu_t menu, int item) {
        DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CacheLumpName("M_CELL2", PU_CACHE, patch_t.class), DOOM.vs, menu.x - 10, menu.y + item * LINEHEIGHT - 1);
    }

    //
    // M_SaveGame & Cie.
    //
    public class M_DrawSave implements DrawRoutine {
    	@Override
    	public void invoke(){
        int i;
        DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("M_SAVEG"), DOOM.vs, 72, 28);
        for (i = 0; i < load_end; i++) {
            DrawSaveLoadBorder(LoadDef.x, LoadDef.y + LINEHEIGHT * i);
            WriteText(LoadDef.x, LoadDef.y + LINEHEIGHT * i, savegamestrings[i]);
        }

        if (saveStringEnter) {
            i = StringWidth(savegamestrings[saveSlot]);
            WriteText(LoadDef.x + i, LoadDef.y + LINEHEIGHT * saveSlot, "_");
        }
    	}
    }

    /**
     * M_Responder calls this when user is finished
     * 
     * @param slot
     */

    public void DoSave(int slot) {
        DOOM.SaveGame(slot, new String(savegamestrings[slot]));
        ClearMenus();

        // PICK QUICKSAVE SLOT YET?
        if (quickSaveSlot == -2)
            quickSaveSlot = slot;
    }

    /**
     * User wants to save. Start string input for M_Responder
     */

    class M_SaveSelect implements MenuRoutine {
        @Override
        public void invoke(int choice) {
            // we are going to be intercepting all chars
        	//System.out.println("ACCEPTING typing input");
            saveStringEnter = true;

            saveSlot = choice;
            C2JUtils.strcpy(saveOldString, savegamestrings[choice]);
            if (C2JUtils.strcmp(savegamestrings[choice], EMPTYSTRING))
                savegamestrings[choice][0] = 0;
            saveCharIndex = C2JUtils.strlen(savegamestrings[choice]);
        }
    }

    /**
     * Selected from DOOM menu
     */
    class M_SaveGame implements MenuRoutine {
        @Override
        public void invoke(int choice) {
            if (!DOOM.usergame) {
                StartMessage(SAVEDEAD, null, false);
                return;
            }

            if (DOOM.gamestate != gamestate_t.GS_LEVEL)
                return;

            SetupNextMenu(SaveDef);
            ReadSaveStrings();
        }
    }

    //
    // M_QuickSave
    //
    private String tempstring;

    class M_QuickSaveResponse implements MenuRoutine {
        @Override
        public void invoke(int ch) {
            if (ch == 'y') {
                DoSave(quickSaveSlot);
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_swtchx);
            }
        }
    }

    private void QuickSave() {
        if (!DOOM.usergame) {
            DOOM.doomSound.StartSound(null, sfxenum_t.sfx_oof);
            return;
        }

        if (DOOM.gamestate != gamestate_t.GS_LEVEL)
            return;

        if (quickSaveSlot < 0) {
            StartControlPanel();
            ReadSaveStrings();
            SetupNextMenu(SaveDef);
            quickSaveSlot = -2; // means to pick a slot now
            return;
        }
        tempstring = String.format(QSPROMPT,C2JUtils.nullTerminatedString(savegamestrings[quickSaveSlot]));
        StartMessage(tempstring,this.QuickSaveResponse,true);
    }

    //
    // M_QuickLoad
    //
    class M_QuickLoadResponse implements MenuRoutine {
        @Override
        public void invoke(int ch) {
            if (ch == 'y') {
                LoadSelect.invoke(quickSaveSlot);
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_swtchx);
            }
        }
    }

    class M_QuitResponse implements MenuRoutine {
        @Override
        public void invoke(int ch) {
            if (ch != 'y')
                return;
            if (!DOOM.netgame) {
                if (DOOM.isCommercial())
                    DOOM.doomSound.StartSound(null, quitsounds2[(DOOM.gametic >> 2) & 7]);
                else
                    DOOM.doomSound.StartSound(null, quitsounds[(DOOM.gametic >> 2) & 7]);
                // TI.WaitVBL(105);
            }
            DOOM.doomSystem.Quit();
        }
    }

    public void QuickLoad() {
        if (DOOM.netgame) {
            StartMessage(QLOADNET, null, false);
            return;
        }

        if (quickSaveSlot < 0) {
            StartMessage(QSAVESPOT, null, false);
            return;
        }
        tempstring = String.format(QLPROMPT, C2JUtils.nullTerminatedString(savegamestrings[quickSaveSlot]));
        StartMessage(tempstring, QuickLoadResponse, true);
    }

    class M_Sound implements MenuRoutine {
        @Override
        public void invoke(int choice) {
            SetupNextMenu(SoundDef);
        }
    }

    class M_SfxVol implements MenuRoutine {
        @Override
        public void invoke(int choice) {
            switch (choice) {
            case 0:
                if (DOOM.snd_SfxVolume != 0)
                    DOOM.snd_SfxVolume--;
                break;
            case 1:
                if (DOOM.snd_SfxVolume < 15)
                    DOOM.snd_SfxVolume++;
                break;
            }

           DOOM.doomSound.SetSfxVolume(DOOM.snd_SfxVolume *8);
        }
    }

    class M_MusicVol implements MenuRoutine {
        @Override
        public void invoke(int choice) {
            switch (choice) {
            case 0:
                if (DOOM.snd_MusicVolume != 0)
                    DOOM.snd_MusicVolume--;
                break;
            case 1:
                if (DOOM.snd_MusicVolume < 15)
                    DOOM.snd_MusicVolume++;
                break;
            }

            DOOM.doomSound.SetMusicVolume(DOOM.snd_MusicVolume*8);
        }
    }

    //
    // M_Episode
    //
    private int epi;

    class M_VerifyNightmare implements MenuRoutine {
        @Override
        public void invoke(int ch) {
            if (ch != 'y')
                return;

            DOOM.DeferedInitNew(skill_t.sk_nightmare, epi + 1, 1);
            ClearMenus();
        }
    }

    /**
     * M_ReadThis
     */

    class M_ReadThis implements MenuRoutine {
        @Override
        public void invoke(int choice) {
            choice = 0;
            SetupNextMenu(ReadDef1);
        }
    }

    class M_ReadThis2 implements MenuRoutine {
        @Override
        public void invoke(int choice) {
            choice = 0;
            SetupNextMenu(ReadDef2);
        }
    }

    class M_FinishReadThis implements MenuRoutine {
        @Override
        public void invoke(int choice) {
            choice = 0;
            SetupNextMenu(MainDef);
        }
    }

    //
    // M_QuitDOOM
    //

    class M_QuitDOOM implements MenuRoutine {
        @Override
        public void invoke(int choice) {
            // We pick index 0 which is language sensitive,
            // or one at random, between 1 and maximum number.
            if (DOOM.language != Language_t.english)
                endstring = endmsg[0] + "\n\n" + DOSY;
            else
                endstring =
                    endmsg[(DOOM.gametic % (NUM_QUITMESSAGES - 2)) + 1] + "\n\n"
                            + DOSY;
            StartMessage(endstring, QuitResponse, true);
        }
    }

    class M_QuitGame implements MenuRoutine {
        @Override
        public void invoke(int ch) {
            if (ch != 'y')
                return;
            if (!DOOM.netgame) {
                if (DOOM.isCommercial())
                DOOM.doomSound.StartSound(null,quitsounds2[(DOOM.gametic>>2)&7]);
                else
                DOOM.doomSound.StartSound(null,quitsounds[(DOOM.gametic>>2)&7]);
                DOOM.doomSystem.WaitVBL(105);
            }
           DOOM.doomSystem.Quit ();
        }
    }

    class M_SizeDisplay implements MenuRoutine {
        @Override
        public void invoke(int choice) {
            switch (choice) {
            case 0:
                if (screenSize > 0) {
                    screenblocks--;
                    screenSize--;
                }
                break;
            case 1:
                if (screenSize < 8) {
                    screenblocks++;
                    screenSize++;
                }
                break;
            }

            DOOM.sceneRenderer.SetViewSize (screenblocks, detailLevel);
        }

    }

    class M_Options implements MenuRoutine {
        @Override
        public void invoke(int choice) {
            SetupNextMenu(OptionsDef);
        }

    }

    class M_NewGame implements MenuRoutine {
        @Override
        public void invoke(int choice) {
            if (DOOM.netgame && !DOOM.demoplayback) {
                StartMessage(NEWGAME, null, false);
                return;
            }

            if (DOOM.isCommercial())
                SetupNextMenu(NewDef);
            else
                SetupNextMenu(EpiDef);
        }

    }

    public void StartMessage(String string, MenuRoutine routine, boolean input) {
        messageLastMenuActive = DOOM.menuactive;
        messageToPrint = true;
        messageString = string;
        messageRoutine = routine;
        messageNeedsInput = input;
        DOOM.menuactive = true; // "true"
    }

    public void StopMessage() {
        DOOM.menuactive = messageLastMenuActive;
        messageToPrint = false;
    }

    /**
     * Find string width from hu_font chars
     */
    public int StringWidth(char[] string) {
        int i;
        int w = 0;
        int c;

        for (i = 0; i < C2JUtils.strlen(string); i++) {
            c = Character.toUpperCase(string[i]) - HU_FONTSTART;
            if (c < 0 || c >= HU_FONTSIZE)
                w += 4;
            else
                w += hu_font[c].width;
        }

        return w;
    }

    /**
     * Find string height from hu_font chars.
     * 
     * Actually it just counts occurences of 'n' and adds height to height.
     */
    private int StringHeight(char[] string) {
        int i;
        int h;
        int height = hu_font[0].height;

        h = height;
        for (i = 0; i < string.length; i++)
            if (string[i] == '\n')
                h += height;

        return h;
    }

    /**
     * Find string height from hu_font chars
     */
    private int StringHeight(String string) {
        return this.StringHeight(string.toCharArray());
    }

    /**
     * Write a string using the hu_font
     */

    private void WriteText(int x, int y, char[] string) {
        int w;
        char[] ch;
        int c;
        int cx;
        int cy;

        ch = string;
        int chptr = 0;
        cx = x;
        cy = y;

        while (chptr<ch.length) {
            c = ch[chptr];
            chptr++;
            if (c == 0)
                break;
            if (c == '\n') {
                cx = x;
                cy += 12;
                continue;
            }
            
            c = Character.toUpperCase(c) - HU_FONTSTART;
            if (c < 0 || c >= HU_FONTSIZE) {
                cx += 4;
                continue;
            }

            w = hu_font[c].width;
            if (cx + w > DOOM.vs.getScreenWidth())
                break;
            
            DOOM.graphicSystem.DrawPatchScaled(FG, hu_font[c], DOOM.vs, cx, cy);
            cx += w;
        }

    }

    private void WriteText(int x, int y, String string) {
        if (string == null || string.length() == 0)
            return;

        int w;
        int cx;
        int cy;

        int chptr = 0;
        char c;

        cx = x;
        cy = y;

        while (chptr<string.length()) {
            c = string.charAt(chptr++);
            if (c == 0)
                break;
            if (c == '\n') {
                cx = x;
                cy += 12;
                continue;
            }

            c = (char) (Character.toUpperCase(c) - HU_FONTSTART);
            if (c < 0 || c >= HU_FONTSIZE) {
                cx += 4;
                continue;
            }

            w = hu_font[c].width;
            if (cx + w > DOOM.vs.getScreenWidth())
                break;
            DOOM.graphicSystem.DrawPatchScaled(FG, hu_font[c], DOOM.vs, cx, cy);
            cx += w;
        }

    }

    // These belong to the responder.
    
    private int joywait = 0;

    private int mousewait = 0;

    private int mousey = 0;

    private int lasty = 0;

    private int mousex = 0;

    private int lastx = 0;

    @Override
    @SourceCode.Compatible
    @M_Menu.C(M_Responder)
    public boolean Responder(event_t ev) {
        final ScanCode sc;
        
        if (ev.isType(evtype_t.ev_joystick) && joywait < DOOM.ticker.GetTime()) {
            // Joystick input
            sc = ev.mapByJoy(joyEvent -> {
                ScanCode r = SC_NULL;
                if (joyEvent.y == -1) {
                    r = SC_UP;
                    joywait = DOOM.ticker.GetTime() + 5;
                } else if (joyEvent.y == 1) {
                    r = SC_DOWN;
                    joywait = DOOM.ticker.GetTime() + 5;
                }

                if (joyEvent.x == -1) {
                    r = SC_LEFT;
                    joywait = DOOM.ticker.GetTime() + 2;
                } else if (joyEvent.x == 1) {
                    r = SC_RIGHT;
                    joywait = DOOM.ticker.GetTime() + 2;
                }

                if (joyEvent.isJoy(event_t.JOY_2)) {
                    r = SC_BACKSPACE;
                    joywait = DOOM.ticker.GetTime() + 5;
                } else if (joyEvent.isJoy(event_t.JOY_1)) {
                    r = SC_ENTER;
                    joywait = DOOM.ticker.GetTime() + 5;
                }
                return r;
            });
        } else if (ev.isType(evtype_t.ev_mouse) && mousewait < DOOM.ticker.GetTime()) {
            // Mouse input 
            if ((sc = ev.mapByMouse(mouseEvent -> {
                ScanCode r = SC_NULL;
                mousey += mouseEvent.y;
                if (mousey < lasty - 30) {
                    r = SC_DOWN;
                    mousewait = DOOM.ticker.GetTime() + 5;
                    mousey = lasty -= 30;
                } else if (mousey > lasty + 30) {
                    r = SC_UP;
                    mousewait = DOOM.ticker.GetTime() + 5;
                    mousey = lasty += 30;
                }

                mousex += mouseEvent.x;
                if (mousex < lastx - 30) {
                    r = SC_LEFT;
                    mousewait = DOOM.ticker.GetTime() + 5;
                    mousex = lastx -= 30;
                } else if (mousex > lastx + 30) {
                    r = SC_RIGHT;
                    mousewait = DOOM.ticker.GetTime() + 5;
                    mousex = lastx += 30;
                }

                if (mouseEvent.isMouse(event_t.MOUSE_RIGHT)) {
                    r = SC_BACKSPACE;
                    mousewait = DOOM.ticker.GetTime() + 15;
                } else if (mouseEvent.isMouse(event_t.MOUSE_LEFT)) {
                    r = SC_ENTER;
                    mousewait = DOOM.ticker.GetTime() + 15;
                }
                return r;
            })) == SC_NULL) { return false; }
        } else if (ev.isType(evtype_t.ev_keydown)) {
            sc = ev.getSC();
        } else return false;

        // Save Game string input
        if (saveStringEnter) {
            switch (sc) {
                case SC_BACKSPACE:
                    if (saveCharIndex > 0) {
                        saveCharIndex--;
                        savegamestrings[saveSlot][saveCharIndex] = 0;
                    }
                    break;
                case SC_ESCAPE:
                    saveStringEnter = false;
                    C2JUtils.strcpy(savegamestrings[saveSlot], saveOldString);
                    break;
                case SC_ENTER:            	
                    saveStringEnter = false;
                    if (savegamestrings[saveSlot][0] != 0)
                        DoSave(saveSlot);
                    break;
                default:
                    char ch = Character.toUpperCase(sc.c);
                    if (ch != ' ') {
                        if (ch - HU_FONTSTART < 0 || ch - HU_FONTSTART >= HU_FONTSIZE) {
                            break;
                        }
                    }
                    
                    if (ch >= ' ' && ch <= 0x7F && saveCharIndex < SAVESTRINGSIZE - 1
                        && StringWidth(savegamestrings[saveSlot]) < (SAVESTRINGSIZE - 2) * 8)
                    {
                        savegamestrings[saveSlot][saveCharIndex++] = ch;
                        savegamestrings[saveSlot][saveCharIndex] = 0;
                    }
                    break;
            }
            return true;
        }

        // Take care of any messages that need input
        if (messageToPrint) {
            if (messageNeedsInput == true && !(sc == SC_SPACE || sc == SC_N || sc == SC_Y || sc == SC_ESCAPE)) {
                return false;
            }

            DOOM.menuactive = messageLastMenuActive;
            messageToPrint = false;
            if (messageRoutine != null)
                messageRoutine.invoke(sc.c);

            DOOM.menuactive = false; // "false"
            DOOM.doomSound.StartSound(null, sfxenum_t.sfx_swtchx);
            return true;
        }

        if ((DOOM.devparm && sc == SC_F1) || sc == SC_PRTSCRN) {
            DOOM.ScreenShot();
            return true;
        }

        // F-Keys
        if (!DOOM.menuactive){
            switch (sc) {
            case SC_MINUS: // Screen size down
                if (DOOM.automapactive || DOOM.headsUp.chat_on[0])
                    return false;
                SizeDisplay.invoke(0);
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_stnmov);
                return true;

            case SC_EQUALS: // Screen size up
                if (DOOM.automapactive || DOOM.headsUp.chat_on[0])
                    return false;
                SizeDisplay.invoke(1);
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_stnmov);
                return true;

            case SC_F1: // Help key
                StartControlPanel();

                if (DOOM.isRegistered() || DOOM.isShareware())
                    currentMenu = ReadDef2;
                else
                    currentMenu = ReadDef1;
                itemOn = 0;
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_swtchn);
                return true;

            case SC_F2: // Save
                StartControlPanel();
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_swtchn);
                SaveGame.invoke(0);
                return true;

            case SC_F3: // Load
                StartControlPanel();
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_swtchn);
                LoadGame.invoke(0);
                return true;

            case SC_F4: // Sound Volume
                StartControlPanel();
                currentMenu = SoundDef;
                itemOn = (short) sfx_vol;
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_swtchn);
                return true;

            case SC_F5: // Detail toggle
                ChangeDetail.invoke(0);
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_swtchn);
                return true;

            case SC_F6: // Quicksave
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_swtchn);
                QuickSave();
                return true;

            case SC_F7: // End game
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_swtchn);
                EndGame.invoke(0);
                return true;

            case SC_F8: // Toggle messages
                ChangeMessages.invoke(0);
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_swtchn);
                return true;

            case SC_F9: // Quickload
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_swtchn);
                QuickLoad();
                return true;

            case SC_F10: // Quit DOOM
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_swtchn);
                QuitDOOM.invoke(0);
                return true;

            case SC_F11: // gamma toggle
                int usegamma = DOOM.graphicSystem.getUsegamma();
                usegamma++;
                if (usegamma > 4)
                    usegamma = 0;
                DOOM.players[DOOM.consoleplayer].message = gammamsg[usegamma];
                DOOM.graphicSystem.setUsegamma(usegamma);
                DOOM.autoMap.Repalette();
                return true;
                
            default:
            	break;

            }
        } else if (sc == SC_F5 && DOOM.ticker instanceof DelegateTicker) { // Toggle ticker
            ((DelegateTicker) DOOM.ticker).changeTicker();
            System.err.println("Warning! Ticker changed; time reset");
            DOOM.doomSound.StartSound(null, sfxenum_t.sfx_radio);
            return true;
        }
        
        // Pop-up menu?
        if (!DOOM.menuactive) {
            if (sc == SC_ESCAPE) {
                StartControlPanel();
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_swtchn);
                return true;
            }
            return false;
        }

        // Keys usable within menu
        switch (sc) {
            case SC_DOWN:
                do {
                    if (itemOn + 1 > currentMenu.numitems - 1) {
                        itemOn = 0;
                    } else {
                        itemOn++;
                    }
                    DOOM.doomSound.StartSound(null, sfxenum_t.sfx_pstop);
                } while (currentMenu.menuitems[itemOn].status == -1);
                return true;

            case SC_UP:
                do {
                    if (itemOn == 0) {
                        itemOn = (short) (currentMenu.numitems - 1);
                    } else {
                        itemOn--;
                    }
                    DOOM.doomSound.StartSound(null, sfxenum_t.sfx_pstop);
                } while (currentMenu.menuitems[itemOn].status == -1);
                return true;

            case SC_LEFT:
                if ((currentMenu.menuitems[itemOn].routine != null)
                        && (currentMenu.menuitems[itemOn].status == 2)) {
                    DOOM.doomSound.StartSound(null, sfxenum_t.sfx_stnmov);
                    currentMenu.menuitems[itemOn].routine.invoke(0);
                }
                return true;

            case SC_RIGHT:
                if ((currentMenu.menuitems[itemOn].routine != null)
                        && (currentMenu.menuitems[itemOn].status == 2)) {
                    DOOM.doomSound.StartSound(null, sfxenum_t.sfx_stnmov);
                    currentMenu.menuitems[itemOn].routine.invoke(1);
                }
                return true;

            case SC_NPENTER:
            case SC_ENTER: {
                if ((currentMenu.menuitems[itemOn].routine != null) && currentMenu.menuitems[itemOn].status != 0) {
                    currentMenu.lastOn = itemOn;
                    if (currentMenu.menuitems[itemOn].status == 2) {
                        currentMenu.menuitems[itemOn].routine.invoke(1); // right
                        // arrow
                        DOOM.doomSound.StartSound(null, sfxenum_t.sfx_stnmov);
                    } else {
                        currentMenu.menuitems[itemOn].routine.invoke(itemOn);
                        DOOM.doomSound.StartSound(null, sfxenum_t.sfx_pistol);
                    }
                }
                return true;
            }

            case SC_ESCAPE:
                currentMenu.lastOn = itemOn;
                ClearMenus();
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_swtchx);
                return true;

            case SC_BACKSPACE:
                currentMenu.lastOn = itemOn;
                if (currentMenu.prevMenu != null) {
                    currentMenu = currentMenu.prevMenu;
                    itemOn = (short) currentMenu.lastOn;
                    DOOM.doomSound.StartSound(null, sfxenum_t.sfx_swtchn);
                }
                return true;

            default:
                for (int i = itemOn + 1; i < currentMenu.numitems; i++) {
                    if (currentMenu.menuitems[i].alphaKey == sc) {
                        itemOn = (short) i;
                        DOOM.doomSound.StartSound(null, sfxenum_t.sfx_pstop);
                        return true;
                    }
                }
                for (int i = 0; i <= itemOn; i++) {
                    if (currentMenu.menuitems[i].alphaKey == sc) {
                        itemOn = (short) i;
                        DOOM.doomSound.StartSound(null, sfxenum_t.sfx_pstop);
                        return true;
                    }
                }
                break;

        }

        return false;
    }

    /**
     * M_StartControlPanel
     */
    @Override
    @SourceCode.Exact
    @M_Menu.C(M_StartControlPanel)
    public void StartControlPanel() {
        // intro might call this repeatedly
        if (DOOM.menuactive) {
            return;
        }

        DOOM.menuactive = true;
        currentMenu = MainDef; // JDC
        itemOn = (short) currentMenu.lastOn; // JDC
    }

    /**
     * M_Drawer Called after the view has been rendered, but before it has been
     * blitted.
     */
    public void Drawer() {

        int x;
        int y;
        int max;
        char[] string = new char[40];
        char[] msstring;
        int start;
        inhelpscreens = false; // Horiz. & Vertically center string and print
        // it.
        if (messageToPrint) {
            start = 0;
            y = 100 - this.StringHeight(messageString) / 2;
            msstring = messageString.toCharArray();
            while (start < messageString.length()) {
                int i = 0;
                for (i = 0; i < messageString.length() - start; i++) {
                    if (msstring[start + i] == '\n') {
                        C2JUtils.memset(string, (char) 0, 40);
                        C2JUtils.strcpy(string, msstring, start, i);
                        start += i + 1;
                        break;
                    }
                }

                if (i == (messageString.length() - start)) {
                    C2JUtils.strcpy(string, msstring, start);
                    start += i;
                }
                x = 160 - this.StringWidth(string) / 2;
                this.WriteText(x, y, string);
                y += hu_font[0].height;
            }
            return;
        }
        if (!DOOM.menuactive) {
            return;
        }
        if (currentMenu.routine != null) {
            currentMenu.routine.invoke(); // call Draw routine
        }
        // DRAW MENU
        x = currentMenu.x;
        y = currentMenu.y;
        max = currentMenu.numitems;
        for (int i = 0; i < max; i++) {
            if (currentMenu.menuitems[i].name != null && !"".equals(currentMenu.menuitems[i].name)) {
                DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName(
                        currentMenu.menuitems[i].name, PU_CACHE), DOOM.vs, x, y);
            }
            y += LINEHEIGHT;
        }

        // DRAW SKULL
        DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName(skullName[whichSkull],
                PU_CACHE), DOOM.vs, x + SKULLXOFF, currentMenu.y - 5 + itemOn
                * LINEHEIGHT);
    }

    //
    // M_ClearMenus
    //
    public void ClearMenus() {
        DOOM.menuactive = false;
        //Engine.getEngine().window.setMouseCaptured();
        DOOM.graphicSystem.forcePalette();
        
        // MAES: was commented out :-/
        //if (!DM.netgame && DM.usergame && DM.paused)
        //    DM.setPaused(true);
    }

    /**
     * M_SetupNextMenu
     */
    public void SetupNextMenu(menu_t menudef) {
        currentMenu = menudef;
        itemOn = (short) currentMenu.lastOn;
    }

    /**
     * M_Ticker
     */
    @Override
    @SourceCode.Exact
    @M_Menu.C(M_Ticker)
    public void Ticker() {
        if (--skullAnimCounter <= 0) {
            whichSkull ^= 1;
            skullAnimCounter = 8;
        }
    }

    /**
     * M_Init
     */
    public void Init() {
        
        // Init menus.
        this.initMenuRoutines();
        this.initDrawRoutines();
        this.initMenuItems();
        this.hu_font=DOOM.headsUp.getHUFonts();

        currentMenu = MainDef;
        DOOM.menuactive = false;
        itemOn = (short) currentMenu.lastOn;
        whichSkull = 0;
        skullAnimCounter = 10;
        screenSize = screenblocks - 3;
        messageToPrint = false;
        messageString = null;
        messageLastMenuActive = DOOM.menuactive;
        quickSaveSlot = -1;

        // Here we could catch other version dependencies,
        // like HELP1/2, and four episodes.

        switch (DOOM.getGameMode()) {
        case freedm:
        case freedoom2:
        case commercial:
        case pack_plut:
        case pack_tnt:
            // This is used because DOOM 2 had only one HELP
            // page. I use CREDIT as second page now, but
            // kept this hack for educational purposes.
            MainMenu[readthis] = MainMenu[quitdoom];
            MainDef.numitems--;
            MainDef.y += 8;
            NewDef.prevMenu = MainDef;
            ReadDef1.routine = DrawReadThis1;
            ReadDef1.x = 330;
            ReadDef1.y = 165;
            ReadMenu1[0].routine = FinishReadThis;
            break;
        case shareware:
            // Episode 2 and 3 are handled,
            // branching to an ad screen.
            // We need to remove the fourth episode.
        case registered:
            EpiDef.numitems--;
            break;
        case freedoom1:
        case retail:
            // We are fine.
        default:
            break;
        }

    }

    

    /**
     * M_DrawText Returns the final X coordinate HU_Init must have been called
     * to init the font. Unused?
     * 
     * @param x
     * @param y
     * @param direct
     * @param string
     * @return
     */

    public int DrawText(int x, int y, boolean direct, String string) {
        int c;
        int w;
        int ptr = 0;
        
        while ((c=string.charAt(ptr)) > 0) {
            c = Character.toUpperCase(c) - HU_FONTSTART;
            ptr++;
            if (c < 0 || c > HU_FONTSIZE) {
                x += 4;
                continue;
            }

            w = hu_font[c].width;
            if (x + w > DOOM.vs.getScreenWidth())
                break;
            if (direct)
                DOOM.graphicSystem.DrawPatchScaled(FG, hu_font[c], DOOM.vs, x, y);
            else
                DOOM.graphicSystem.DrawPatchScaled(FG, hu_font[c], DOOM.vs, x, y);
            x += w;
        }

        return x;
    }

    

    // ////////////////////////// DRAWROUTINES
    // //////////////////////////////////

    class M_DrawEpisode
            implements DrawRoutine {

        @Override
        public void invoke() {
            DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("M_EPISOD"), DOOM.vs, 54, 38);
        }

    }

    /**
     * M_LoadGame & Cie.
     */

    class M_DrawLoad
            implements DrawRoutine {
        @Override
        public void invoke() {
            int i;

            DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("M_LOADG"), DOOM.vs, 72, 28);
            for (i = 0; i < load_end; i++) {
                DrawSaveLoadBorder(LoadDef.x, LoadDef.y + LINEHEIGHT * i);
                WriteText(LoadDef.x, LoadDef.y + LINEHEIGHT * i,
                    savegamestrings[i]);
            }

        }
    }

    class M_DrawMainMenu
            implements DrawRoutine {
        @Override
        public void invoke() {
            DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("M_DOOM"), DOOM.vs, 94, 2);
        }
    }

    class M_DrawNewGame
            implements DrawRoutine {

        @Override
        public void invoke() {
            DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("M_NEWG"), DOOM.vs, 96, 14);
            DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("M_SKILL"), DOOM.vs, 54, 38);
        }
    }

    class M_DrawOptions
            implements DrawRoutine {

        private final String detailNames[] = { "M_GDHIGH", "M_GDLOW" };
        private final String msgNames[] = { "M_MSGOFF", "M_MSGON" };

        @Override
        public void invoke() {
            DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("M_OPTTTL"), DOOM.vs, 108, 15);
            DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName(detailNames[detailLevel]), DOOM.vs, OptionsDef.x + 175, OptionsDef.y + LINEHEIGHT * detail);
            DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName(msgNames[showMessages?1:0]), DOOM.vs, OptionsDef.x + 120, OptionsDef.y + LINEHEIGHT * messages);

            DrawThermo(OptionsDef.x, OptionsDef.y + LINEHEIGHT
                    * (mousesens + 1), 10, DOOM.mouseSensitivity);

            DrawThermo(OptionsDef.x,
                OptionsDef.y + LINEHEIGHT * (scrnsize + 1), 9, screenSize);

        }

    }

    /**
     * Read This Menus
     * Had a "quick hack to fix romero bug"
     */

    class M_DrawReadThis1 implements DrawRoutine {

        @Override
        public void invoke() {
            String lumpname;
            int skullx, skully;
            inhelpscreens = true;

            switch (DOOM.getGameMode()) {
                case commercial:
                case freedm:
                case freedoom2:
                case pack_plut:
                case pack_tnt:
                    skullx = 330;
                    skully = 165;
                    lumpname = "HELP";
                    break;
                case shareware:
                    lumpname = "HELP2";
                    skullx = 280;
                    skully = 185;
                    break;
                default:
                    lumpname = "CREDIT";
                    skullx = 330;
                    skully = 165;
                    break;
            }

            ReadDef1.x = skullx;
            ReadDef1.y = skully;
            DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName(lumpname), DOOM.vs, 0, 0);
            // Maes: we need to do this here, otherwide the status bar appears "dirty"
            DOOM.statusBar.forceRefresh();
        }
    }

    /**
     * Read This Menus - optional second page.
     */
    class M_DrawReadThis2 implements DrawRoutine {

        @Override
        public void invoke() {
            String lumpname;
            int skullx, skully;
            inhelpscreens = true;

            lumpname = "HELP1";
            skullx = 330;
            skully = 175;

            ReadDef2.x = skullx;
            ReadDef2.y = skully;
            DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName(lumpname), DOOM.vs, 0, 0);
            // Maes: we need to do this here, otherwide the status bar appears "dirty"
            DOOM.statusBar.forceRefresh();
        }
    }

    /**
     * Change Sfx & Music volumes
     */
    class M_DrawSound
            implements DrawRoutine {

        public void invoke() {
            DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CacheLumpName("M_SVOL", PU_CACHE, patch_t.class), DOOM.vs, 60, 38);

            DrawThermo(SoundDef.x, SoundDef.y + LINEHEIGHT * (sfx_vol + 1), 16,
                DOOM.snd_SfxVolume);

            DrawThermo(SoundDef.x, SoundDef.y + LINEHEIGHT * (music_vol + 1),
                16, DOOM.snd_MusicVolume);
        }
    }

    // /////////////////////////// MENU ROUTINES
    // ///////////////////////////////////

    class M_ChangeDetail implements MenuRoutine {
        @Override
        public void invoke(int choice) {
            choice = 0;
            detailLevel = 1 - detailLevel;

            // FIXME - does not work. Remove anyway?
            //System.err.print("M_ChangeDetail: low detail mode n.a.\n");

            //return;

            DOOM.sceneRenderer.SetViewSize (screenblocks, detailLevel); 
            if (detailLevel==0) DOOM.players[DOOM.consoleplayer].message = englsh.DETAILHI;
             else DOOM.players[DOOM.consoleplayer].message = englsh.DETAILLO;
             

        }
    }

    /**
     * Toggle messages on/off
     */
    class M_ChangeMessages implements MenuRoutine {
        @Override
        public void invoke(int choice) {
            // warning: unused parameter `int choice'
            //choice = 0;
            showMessages = !showMessages;

            if (!showMessages)
                DOOM.players[DOOM.consoleplayer].message = MSGOFF;
            else
                DOOM.players[DOOM.consoleplayer].message = MSGON;

            message_dontfuckwithme = true;
        }
    }

    class M_ChangeSensitivity implements MenuRoutine {
        @Override
        public void invoke(int choice) {
            switch (choice) {
            case 0:
                if (DOOM.mouseSensitivity != 0)
                	DOOM.mouseSensitivity--;
                break;
            case 1:
                if (DOOM.mouseSensitivity < 9)
                	DOOM.mouseSensitivity++;
                break;
            }
        }
    }

    class M_ChooseSkill implements MenuRoutine {
        @Override
        public void invoke(int choice) {
            if (choice == nightmare) {
                StartMessage(NIGHTMARE, VerifyNightmare, true);
                return;
            }

            DOOM.DeferedInitNew(skill_t.values()[choice], epi + 1, 1);
            ClearMenus();
        }

    }

    /**
     * M_EndGame
     */

    class M_EndGame implements MenuRoutine {
        @Override
        public void invoke(int choice) {
            choice = 0;
            if (!DOOM.usergame) {
                 DOOM.doomSound.StartSound(null, sfxenum_t.sfx_oof);
                return;
            }

            if (DOOM.netgame) {
                StartMessage(NETEND, null, false);
                return;
            }

            StartMessage(ENDGAME, EndGameResponse, true);
        }
    }

    class M_EndGameResponse implements MenuRoutine {
        @Override
        public void invoke(int ch) {
            if (ch != 'y')
                return;

            currentMenu.lastOn = itemOn;
            ClearMenus();
            DOOM.StartTitle();
        }
    }

    class M_Episode implements MenuRoutine {
        @Override
        public void invoke(int choice) {

            if (DOOM.isShareware() && (choice != 0)) {
                StartMessage(SWSTRING, null, false);
                SetupNextMenu(ReadDef2);
                return;
            }

            // Yet another hack...
            if (!DOOM.isRetail() && (choice > 2)) {
                System.err
                        .print("M_Episode: 4th episode requires UltimateDOOM\n");
                choice = 0;
            }

            epi = choice;
            SetupNextMenu(NewDef);
        }

    }

    /**
     * User wants to load this game
     */
    class M_LoadSelect implements MenuRoutine {
        @Override
        public void invoke(int choice) {
            String name;

            if (DOOM.cVarManager.bool(CommandVariable.CDROM))
                name = ("c:\\doomdata\\" + SAVEGAMENAME + (choice) + ".dsg");
            else
                name = (SAVEGAMENAME + (choice) + ".dsg");
            DOOM.LoadGame(name);
            ClearMenus();
        }
    }

    /**
     * Selected from DOOM menu
     */
    class M_LoadGame implements MenuRoutine {
        @Override
        public void invoke(int choice) {

            if (DOOM.netgame) {
                StartMessage(LOADNET, null, false);
                return;
            }

            SetupNextMenu(LoadDef);
            ReadSaveStrings();
        }
    }

    // ////////////////////// VARIOUS CONSTS //////////////////////

    private static final sfxenum_t[] quitsounds =
        { sfxenum_t.sfx_pldeth, sfxenum_t.sfx_dmpain, sfxenum_t.sfx_popain,
                sfxenum_t.sfx_slop, sfxenum_t.sfx_telept, sfxenum_t.sfx_posit1,
                sfxenum_t.sfx_posit3, sfxenum_t.sfx_sgtatk };

    private static final sfxenum_t[] quitsounds2 =
        { sfxenum_t.sfx_vilact, sfxenum_t.sfx_getpow, sfxenum_t.sfx_boscub,
                sfxenum_t.sfx_slop, sfxenum_t.sfx_skeswg, sfxenum_t.sfx_kntdth,
                sfxenum_t.sfx_bspact, sfxenum_t.sfx_sgtatk };

    /** episodes_e enum */
    private static final int ep1 = 0, ep2 = 1, ep3 = 2, ep4 = 3, ep_end = 4;

    /** load_e enum */
    private static final int load1 = 0, load2 = 1, load3 = 2, load4 = 3, load5 = 4,
            load6 = 5, load_end = 6;

    /** options_e enum; */

    private static final int endgame = 0, messages = 1, detail = 2, scrnsize = 3,
            option_empty1 = 4, mousesens = 5, option_empty2 = 6, soundvol = 7,
            opt_end = 8;

    /** main_e enum; */
    private static final int  newgame = 0, options = 1, loadgam = 2, savegame = 3,
            readthis = 4, quitdoom = 5, main_end = 6;

    /** read_e enum */
    private static final int rdthsempty1 = 0, read1_end = 1;

    /** read_2 enum */
    private static final int rdthsempty2 = 0, read2_end = 1;

    /**  newgame_e enum;*/
    public static final int killthings = 0, toorough = 1, hurtme = 2, violence = 3,
            nightmare = 4, newg_end = 5;
    
    private static final String[] gammamsg = { GAMMALVL0,

        GAMMALVL1, GAMMALVL2, GAMMALVL3, GAMMALVL4 };

    /** sound_e enum */
    static final int sfx_vol = 0, sfx_empty1 = 1, music_vol = 2, sfx_empty2 = 3,
            sound_end = 4;

    @Override
    public void setScreenBlocks(int val) {
        this.screenblocks=val;
    }
    
	@Override
	public int getScreenBlocks() {
		return this.screenblocks;
	}

	@Override
	public int getDetailLevel() {
		return this.detailLevel;
	}
}