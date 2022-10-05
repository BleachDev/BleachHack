package org.bleachhack.util.doom.defines;

import static org.bleachhack.util.doom.defines.DoomVersion.*;
import org.bleachhack.util.doom.doom.CommandVariable;

/**
 * Game mode handling - identify IWAD version to handle IWAD dependend animations etc.
 */
public enum GameMode {
    shareware("data_se", DOOM1_WAD, CommandVariable.SHDEV), // DOOM 1 shareware, E1, M9
    registered("data_se", DOOM_WAD, CommandVariable.REGDEV), // DOOM 1 registered, E3, M27
    commercial("cdata", DOOM2_WAD, CommandVariable.COMDEV), // DOOM 2 retail, E1 M34
    // DOOM 2 german edition not handled
    retail("data_se", DOOMU_WAD, CommandVariable.REGDEV), // DOOM 1 retail, E4, M36
    pack_tnt("cdata", TNT_WAD, CommandVariable.COMDEV), // TNT mission pack
    pack_plut("cdata", PLUTONIA_WAD, CommandVariable.COMDEV), // Plutonia pack
    pack_xbla("cdata", XBLA_WAD, CommandVariable.COMDEV), // XBLA Doom. How you got hold of it, I don't care :-p
    freedm("cdata", FREEDM_WAD, CommandVariable.FRDMDEV), // FreeDM
    freedoom1("data_se", FREEDOOM1_WAD, CommandVariable.FR1DEV), // Freedoom phase 1 
    freedoom2("cdata", FREEDOOM2_WAD, CommandVariable.FR2DEV), // Freedoom phase 2
    indetermined("data_se", null, null);  // Well, no IWAD found.  
    
    public final String devDir;
    public final DoomVersion version;
    public final CommandVariable devVar;
    
    public static GameMode forVersion(DoomVersion v) {
        switch(v) {
            case DOOM1_WAD:
                return shareware;
            case DOOM2F_WAD:
            case DOOM2_WAD:
                return commercial;
            case DOOMU_WAD:
            case UDOOM_WAD:
                return retail;
            case DOOM_WAD:
                return registered;
            case FREEDM_WAD:
                return freedm;
            case FREEDOOM1_WAD:
                return freedoom1;
            case FREEDOOM2_WAD:
                return freedoom2;
            case PLUTONIA_WAD:
                return pack_plut;
            case TNT_WAD:
                return pack_tnt;
            case XBLA_WAD:
                return pack_xbla;
        }
        return null;
    }

    private GameMode(String devDir, DoomVersion version, CommandVariable devVar) {
        this.devDir = devDir;
        this.version = version;
        this.devVar = devVar;
    }
    
    public boolean hasTexture2() {
        return !(this == GameMode.shareware || this == GameMode.freedoom2 || this == GameMode.commercial);
    }
};
