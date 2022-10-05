/*
 * Copyright (C) 1993-1996 by id Software, Inc.
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
package org.bleachhack.util.doom.p;

import org.bleachhack.util.doom.automap.IAutoMap;
import org.bleachhack.util.doom.data.sounds;
import org.bleachhack.util.doom.defines.skill_t;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.player_t;
import org.bleachhack.util.doom.hu.IHeadsUp;
import org.bleachhack.util.doom.i.IDoomSystem;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bleachhack.util.doom.p.Actions.ActionsAttacks;
import org.bleachhack.util.doom.p.Actions.ActionsEnemies;
import org.bleachhack.util.doom.p.Actions.ActionsThinkers;
import org.bleachhack.util.doom.p.Actions.ActiveStates.Ai;
import org.bleachhack.util.doom.p.Actions.ActiveStates.Attacks;
import org.bleachhack.util.doom.p.Actions.ActiveStates.Thinkers;
import org.bleachhack.util.doom.p.Actions.ActiveStates.Weapons;
import org.bleachhack.util.doom.rr.SceneRenderer;
import org.bleachhack.util.doom.s.ISoundOrigin;
import org.bleachhack.util.doom.st.IDoomStatusBar;
import org.bleachhack.util.doom.utils.TraitFactory;
import org.bleachhack.util.doom.utils.TraitFactory.SharedContext;

public class ActionFunctions extends UnifiedGameMap implements
    ActionsThinkers, ActionsEnemies, ActionsAttacks, Ai, Attacks, Thinkers, Weapons
{
    private final SharedContext traitsSharedContext;
    
    public ActionFunctions(final DoomMain<?, ?> DOOM) {
        super(DOOM);
        this.traitsSharedContext = buildContext();
    }
    
    private SharedContext buildContext() {
        try {
            return TraitFactory.build(this, ACTION_KEY_CHAIN);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(ActionFunctions.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public AbstractLevelLoader levelLoader() {
        return DOOM.levelLoader;
    }

    @Override
    public IHeadsUp headsUp() {
        return DOOM.headsUp;
    }

    @Override
    public IDoomSystem doomSystem() {
        return DOOM.doomSystem;
    }

    @Override
    public IDoomStatusBar statusBar() {
        return DOOM.statusBar;
    }

    @Override
    public IAutoMap<?, ?> autoMap() {
        return DOOM.autoMap;
    }

    @Override
    public SceneRenderer<?, ?> sceneRenderer() {
        return DOOM.sceneRenderer;
    }

    @Override
    public UnifiedGameMap.Specials getSpecials() {
        return SPECS;
    }

    @Override
    public UnifiedGameMap.Switches getSwitches() {
        return SW;
    }

    @Override
    public void StopSound(ISoundOrigin origin) {
        DOOM.doomSound.StopSound(origin);
    }

    @Override
    public void StartSound(ISoundOrigin origin, sounds.sfxenum_t s) {
        DOOM.doomSound.StartSound(origin, s);
    }

    @Override
    public void StartSound(ISoundOrigin origin, int s) {
        DOOM.doomSound.StartSound(origin, s);
    }

    @Override
    public player_t getPlayer(int number) {
        return DOOM.players[number];
    }

    @Override
    public skill_t getGameSkill() {
        return DOOM.gameskill;
    }

    @Override
    public mobj_t createMobj() {
        return mobj_t.createOn(DOOM);
    }

    @Override
    public int LevelTime() {
        return DOOM.leveltime;
    }

    @Override
    public int P_Random() {
        return DOOM.random.P_Random();
    }

    @Override
    public int ConsolePlayerNumber() {
        return DOOM.consoleplayer;
    }

    @Override
    public int MapNumber() {
        return DOOM.gamemap;
    }

    @Override
    public boolean PlayerInGame(int number) {
        return DOOM.playeringame[number];
    }

    @Override
    public boolean IsFastParm() {
        return DOOM.fastparm;
    }

    @Override
    public boolean IsPaused() {
        return DOOM.paused;
    }

    @Override
    public boolean IsNetGame() {
        return DOOM.netgame;
    }

    @Override
    public boolean IsDemoPlayback() {
        return DOOM.demoplayback;
    }

    @Override
    public boolean IsDeathMatch() {
        return DOOM.deathmatch;
    }

    @Override
    public boolean IsAutoMapActive() {
        return DOOM.automapactive;
    }

    @Override
    public boolean IsMenuActive() {
        return DOOM.menuactive;
    }

    /**
     * TODO: avoid, deprecate
     */
    @Override
    public DoomMain<?, ?> DOOM() {
        return DOOM;
    }

    @Override
    public SharedContext getContext() {
        return traitsSharedContext;
    }

    @Override
    public ActionsThinkers getThinkers() {
        return this;
    }

    @Override
    public ActionsEnemies getEnemies() {
        return this;
    }

    @Override
    public ActionsAttacks getAttacks() {
        return this;
    }
}
