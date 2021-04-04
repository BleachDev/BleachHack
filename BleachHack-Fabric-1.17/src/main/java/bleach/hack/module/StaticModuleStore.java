package bleach.hack.module;

import bleach.hack.module.mods.*;

public class StaticModuleStore {
    public static final Class[] MODULE_CLASSES = {
            Ambience.class,
            AntiChunkBan.class,
            AntiHunger.class,
            ArrowJuke.class,
            AutoArmor.class,
            AutoBuild.class,
            AutoDonkeyDupe.class,
            AutoParkour.class,
            AutoReconnect.class,
            AutoRespawn.class,
            AutoSign.class,
            AutoSteal.class,
            AutoThrow.class,
            AutoTool.class,
            AutoTotem.class,
            AutoWalk.class,
            BetterPortal.class,
            BlockParty.class,
            BookCrash.class,
            BowBot.class,
            ClickGui.class,
            ClickTp.class,
            ColorSigns.class,
            Criticals.class,
            CrystalAura.class,
            CustomChat.class,
            DiscordRPCMod.class,
            Dispenser32k.class,
            ElytraFly.class,
            EntityControl.class,
            ElytraReplace.class,
            ESP.class,
            FakeLag.class,
            FastUse.class,
            Flight.class,
            Freecam.class,
            Fullbright.class,
            Ghosthand.class,
            HandProgress.class,
            HoleESP.class,
            Jesus.class,
            Killaura.class,
            MountBypass.class,
            MouseFriend.class,
            Nametags.class,
            Nofall.class,
            NoKeyBlock.class,
            NoRender.class,
            NoSlow.class,
            Notebot.class,
            NotebotStealer.class,
            NoVelocity.class,
            Nuker.class,
            OffhandCrash.class,
            PacketFly.class,
            Peek.class,
            PlayerCrash.class,
            RotationSnap.class,
            SafeWalk.class,
            Scaffold.class,
            Search.class,
            ShaderRender.class,
            Spammer.class,
            Speed.class,
            SpeedMine.class,
            Sprint.class,
            StarGithub.class,
            Step.class,
            StorageESP.class,
            Surround.class,
            Timer.class,
            Tracers.class,
            Trail.class,
            Trajectories.class,
            UI.class,
            Xray.class,
            Zoom.class
    };

    static {
        for (Class clazz : MODULE_CLASSES) {
            if (clazz.getSuperclass() != Module.class) {
                System.err.println("This should never happen. Are your modules proper subclasses? ");
                System.exit(-1);
            }
        }
    }
}
