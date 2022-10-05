/*
 * Copyright (C) 2017 Good Sign
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bleachhack.util.doom.doom;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Target({})
@Retention(SOURCE)
public @interface SourceCode {
    
    public enum AM_Map {
        AM_Responder,
        AM_Ticker,
        AM_Drawer,
        AM_Stop;
        @Documented
        @Retention(SOURCE) public
        @interface C { AM_Map value(); }
    }
    
    public enum D_Main {
        D_DoomLoop,
        D_ProcessEvents;
        @Documented
        @Retention(SOURCE) public
        @interface C { D_Main value(); }
    }
    
    public enum F_Finale {
        F_Responder,
        F_Ticker,
        F_Drawer,
        F_StartFinale;
        @Documented
        @Retention(SOURCE) public
        @interface C { F_Finale value(); }
    }
    
    public enum G_Game {
        G_BuildTiccmd,
        G_DoCompleted,
        G_DoReborn,
        G_DoLoadLevel,
        G_DoSaveGame,
        G_DoPlayDemo,
        G_PlayerFinishLevel,
        G_DoNewGame,
        G_PlayerReborn,
        G_CheckSpot,
        G_DeathMatchSpawnPlayer,
        G_InitNew,
        G_DeferedInitNew,
        G_DeferedPlayDemo,
        G_LoadGame,
        G_DoLoadGame,
        G_SaveGame,
        G_RecordDemo,
        G_BeginRecording,
        G_PlayDemo,
        G_TimeDemo,
        G_CheckDemoStatus,
        G_ExitLevel,
        G_SecretExitLevel,
        G_WorldDone,
        G_Ticker,
        G_Responder,
        G_ScreenShot;
        @Documented
        @Retention(SOURCE) public
        @interface C { G_Game value(); }
    }

    public enum HU_Lib {
        HUlib_init,
        HUlib_clearTextLine,
        HUlib_initTextLine,
        HUlib_addCharToTextLine,
        HUlib_delCharFromTextLine,
        HUlib_drawTextLine,
        HUlib_eraseTextLine,
        HUlib_initSText,
        HUlib_addLineToSText,
        HUlib_addMessageToSText,
        HUlib_drawSText,
        HUlib_eraseSText,
        HUlib_initIText,
        HUlib_delCharFromIText,
        HUlib_eraseLineFromIText,
        HUlib_resetIText,
        HUlib_addPrefixToIText,
        HUlib_keyInIText,
        HUlib_drawIText,
        HUlib_eraseIText;
        @Documented
        @Retention(SOURCE) public
        @interface C { HU_Lib value(); }
    }    
    
    public enum HU_Stuff {
        HU_Init,
        HU_Start,
        HU_Responder,
        HU_Ticker,
        HU_Drawer,
        HU_queueChatChar,
        HU_dequeueChatChar,
        HU_Erase;
        @Documented
        @Retention(SOURCE) public
        @interface C { HU_Stuff value(); }
    }
    
    public enum I_IBM {
        I_GetTime,
        I_WaitVBL,
        I_SetPalette,
        I_FinishUpdate,
        I_StartTic,
        I_InitNetwork,
        I_NetCmd;
        @Documented
        @Retention(SOURCE) public
        @interface C { I_IBM value(); }
    }

    public enum M_Argv {
        M_CheckParm;
        @Documented
        @Retention(SOURCE) public
        @interface C { M_Argv value(); }
    }
    
    public enum M_Menu {
        M_Responder,
        M_Ticker,
        M_Drawer,
        M_Init,
        M_StartControlPanel;
        @Documented
        @Retention(SOURCE) public
        @interface C { M_Menu value(); }
    }
    
    public enum M_Random {
        M_Random,
        P_Random,
        M_ClearRandom;
        @Documented
        @Retention(SOURCE) public
        @interface C { M_Random value(); }
    }
    
    public enum P_Doors {
        T_VerticalDoor,
        EV_VerticalDoor,
        EV_DoDoor,
        EV_DoLockedDoor,
        P_SpawnDoorCloseIn30,
        P_SpawnDoorRaiseIn5Mins,
        P_InitSlidingDoorFrames,
        P_FindSlidingDoorType,
        T_SlidingDoor,
        EV_SlidingDoor;
        @Documented
        @Retention(SOURCE) public
        @interface C { P_Doors value(); }
    }
    
    public enum P_Map {
        P_CheckPosition,
        PIT_CheckThing,
        PIT_CheckLine,
        PIT_RadiusAttack,
        PIT_ChangeSector,
        PIT_StompThing,
        PTR_SlideTraverse,
        PTR_AimTraverse,
        PTR_ShootTraverse,
        PTR_UseTraverse;
        @Documented
        @Retention(SOURCE) public
        @interface C { P_Map value(); }
    }
    
    public enum P_MapUtl {
        P_BlockThingsIterator,
        P_BlockLinesIterator,
        P_PathTraverse,
        P_UnsetThingPosition,
        P_SetThingPosition,
        PIT_AddLineIntercepts,
        PIT_AddThingIntercepts;
        @Documented
        @Retention(SOURCE) public
        @interface C { P_MapUtl value(); }
    }
    
    public enum P_Mobj {
        G_PlayerReborn,
        P_SpawnMapThing,
        P_SetMobjState,
        P_ExplodeMissile,
        P_XYMovement,
        P_ZMovement,
        P_NightmareRespawn,
        P_MobjThinker,
        P_SpawnMobj,
        P_RemoveMobj,
        P_RespawnSpecials,
        P_SpawnPlayer,
        P_SpawnPuff,
        P_SpawnBlood,
        P_CheckMissileSpawn,
        P_SpawnMissile,
        P_SpawnPlayerMissile;
        @Documented
        @Retention(SOURCE) public
        @interface C { P_Mobj value(); }
    }
    
    public enum P_Enemy {
        PIT_VileCheck;
        @Documented
        @Retention(SOURCE) public
        @interface C { P_Enemy value(); }
    }
    
    public enum P_Lights {
        T_FireFlicker,
        P_SpawnFireFlicker,
        T_LightFlash,
        P_SpawnLightFlash,
        T_StrobeFlash,
        P_SpawnStrobeFlash,
        EV_StartLightStrobing,
        EV_TurnTagLightsOff,
        EV_LightTurnOn,
        T_Glow,
        P_SpawnGlowingLight;
        @Documented
        @Retention(SOURCE) public
        @interface C { P_Lights value(); }
    }
    
    public enum P_SaveG {
        P_ArchivePlayers,
        P_UnArchivePlayers,
        P_ArchiveWorld,
        P_UnArchiveWorld,
        P_ArchiveThinkers,
        P_UnArchiveThinkers,
        P_ArchiveSpecials,
        P_UnArchiveSpecials;
        @Documented
        @Retention(SOURCE) public
        @interface C { P_SaveG value(); }
    }
    
    public enum P_Setup {
        P_SetupLevel,
        P_LoadThings;
        @Documented
        @Retention(SOURCE) public
        @interface C { P_Setup value(); }
    }
    
    public enum P_Spec {
        P_InitPicAnims,
        P_SpawnSpecials,
        P_UpdateSpecials,
        P_UseSpecialLine,
        P_ShootSpecialLine,
        P_CrossSpecialLine,
        P_PlayerInSpecialSector,
        twoSided,
        getSector,
        getSide,
        P_FindLowestFloorSurrounding,
        P_FindHighestFloorSurrounding,
        P_FindNextHighestFloor,
        P_FindLowestCeilingSurrounding,
        P_FindHighestCeilingSurrounding,
        P_FindSectorFromLineTag,
        P_FindMinSurroundingLight,
        getNextSector,
        EV_DoDonut,
        P_ChangeSwitchTexture,
        P_InitSwitchList,
        T_PlatRaise,
        EV_DoPlat,
        P_AddActivePlat,
        P_RemoveActivePlat,
        EV_StopPlat,
        P_ActivateInStasis,
        EV_DoCeiling,
        T_MoveCeiling,
        P_AddActiveCeiling,
        P_RemoveActiveCeiling,
        EV_CeilingCrushStop,
        P_ActivateInStasisCeiling,
        T_MovePlane,
        EV_BuildStairs,
        EV_DoFloor,
        T_MoveFloor,
        EV_Teleport;
        @Documented
        @Retention(SOURCE) public
        @interface C { P_Spec value(); }
    }
    
    public enum P_Ceiling {
        EV_DoCeiling;
        @Documented
        @Retention(SOURCE) public
        @interface C { P_Ceiling value(); }
    }
    
    public enum P_Tick {
        P_InitThinkers,
        P_RemoveThinker,
        P_AddThinker,
        P_AllocateThinker,
        P_RunThinkers,
        P_Ticker;
        @Documented
        @Retention(SOURCE) public
        @interface C { P_Tick value(); }
    }
    
    public enum P_Pspr {
        P_SetPsprite,
        P_CalcSwing,
        P_BringUpWeapon,
        P_CheckAmmo,
        P_FireWeapon,
        P_DropWeapon,
        A_WeaponReady,
        A_ReFire,
        A_CheckReload,
        A_Lower,
        A_Raise,
        A_GunFlash,
        A_Punch,
        A_Saw,
        A_FireMissile,
        A_FireBFG,
        A_FirePlasma,
        P_BulletSlope,
        P_GunShot,
        A_FirePistol,
        A_FireShotgun,
        A_FireShotgun2,
        A_FireCGun,
        A_Light0,
        A_Light1,
        A_Light2,
        A_BFGSpray,
        A_BFGsound,
        P_SetupPsprites,
        P_MovePsprites;
        @Documented
        @Retention(SOURCE) public
        @interface C { P_Pspr value(); }
    }
    
    public enum R_Data {
        R_GetColumn,
        R_InitData,
        R_PrecacheLevel,
        R_FlatNumForName,
        R_TextureNumForName,
        R_CheckTextureNumForName;
        @Documented
        @Retention(SOURCE) public
        @interface C { R_Data value(); }
    }
    
    public enum R_Draw {
        R_FillBackScreen;
        @Documented
        @Retention(SOURCE) public
        @interface C { R_Draw value(); }
    }
    
    public enum R_Main {
        R_PointOnSide,
        R_PointOnSegSide,
        R_PointToAngle,
        R_PointToAngle2,
        R_PointToDist,
        R_ScaleFromGlobalAngle,
        R_PointInSubsector,
        R_AddPointToBox,
        R_RenderPlayerView,
        R_Init,
        R_SetViewSize;
        @Documented
        @Retention(SOURCE) public
        @interface C { R_Main value(); }
    }
    
    public enum ST_Stuff {
        ST_Responder,
        ST_Ticker,
        ST_Drawer,
        ST_Start,
        ST_Init;
        @Documented
        @Retention(SOURCE) public
        @interface C { ST_Stuff value(); }
    }
    
    public enum W_Wad {
        W_InitMultipleFiles,
        W_Reload,
        W_CheckNumForName,
        W_GetNumForName,
        W_LumpLength,
        W_ReadLump,
        W_CacheLumpNum,
        W_CacheLumpName;
        @Documented
        @Retention(SOURCE) public
        @interface C { W_Wad value(); }
    }
    
    public enum WI_Stuff {
        WI_initVariables,
        WI_loadData,
        WI_initDeathmatchStats,
        WI_initAnimatedBack,
        WI_initNetgameStats,
        WI_initStats,
        WI_Ticker,
        WI_Drawer,
        WI_Start;
        @Documented
        @Retention(SOURCE) public
        @interface C { WI_Stuff value(); }
    }
    
    public interface D_Think {
        public enum actionf_t {
            acp1,
            acv,
            acp2
        }
        
        @Documented
        @Retention(SOURCE) public
        @interface C { actionf_t value(); }
    }
    
    public enum Z_Zone {
        Z_Malloc;
        @Documented
        @Retention(SOURCE) public
        @interface C { Z_Zone value(); }
    }
    
    @Documented
    @Retention(SOURCE)
    public @interface Exact {
        String description() default
            "Indicates that the method behaves exactly in vanilla way\n" +
            " and can be skipped when traversing for compatibility";
    }

    @Documented
    @Retention(SOURCE)
    public @interface Compatible {
        String[] value() default "";
        String description() default
            "Indicates that the method can behave differently from vanilla way,\n" +
            " but this behavior is reviewed and can be turned back to vanilla as an option." +
            "A value might be specivied with the equivalent vanilla code";
    }
    
    public enum CauseOfDesyncProbability {
        LOW,
        MEDIUM,
        HIGH
    }

    @Documented
    @Retention(SOURCE)
    public @interface Suspicious  {
        CauseOfDesyncProbability value() default CauseOfDesyncProbability.HIGH;
        String description() default
            "Indicates that the method contains behavior totally different\n" +
            "from vanilla, and by so should be considered suspicious\n" +
            "in terms of compatibility";
    }

    @Documented
    @Retention(SOURCE)
    @Target({METHOD, FIELD, LOCAL_VARIABLE, PARAMETER})
    public @interface angle_t {}
    
    @Documented
    @Retention(SOURCE)
    @Target({METHOD, FIELD, LOCAL_VARIABLE, PARAMETER})
    public @interface fixed_t {}
    
    @Documented
    @Retention(SOURCE)
    public @interface actionf_p1 {}
    
    @Documented
    @Retention(SOURCE)
    public @interface actionf_v {}
    
    @Documented
    @Retention(SOURCE)
    public @interface actionf_p2 {}
    
    @Documented
    @Retention(SOURCE)
    @Target({FIELD, LOCAL_VARIABLE, PARAMETER})
    public @interface thinker_t {}
    
    @Documented
    @Retention(SOURCE)
    @Target({FIELD, LOCAL_VARIABLE, PARAMETER})
    public @interface think_t {}
}
