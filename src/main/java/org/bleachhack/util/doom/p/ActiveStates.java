/*
 * Copyright (C) 1993-1996 Id Software, Inc.
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

import org.bleachhack.util.doom.doom.SourceCode.D_Think;
import org.bleachhack.util.doom.doom.SourceCode.D_Think.actionf_t;
import org.bleachhack.util.doom.doom.SourceCode.actionf_p1;
import org.bleachhack.util.doom.doom.SourceCode.actionf_p2;
import org.bleachhack.util.doom.doom.SourceCode.actionf_v;
import org.bleachhack.util.doom.doom.player_t;
import org.bleachhack.util.doom.doom.thinker_t;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bleachhack.util.doom.mochadoom.Loggers;

/**
 * In vanilla doom there is union called actionf_t that can hold
 * one of the three types: actionf_p1, actionf_v and actionf_p2
 * 
 * typedef union
 * {
 *   actionf_p1	acp1;
 *   actionf_v	acv;
 *   actionf_p2	acp2;
 *
 * } actionf_t;
 * 
 * For those unfamiliar with C, the union can have only one value
 * assigned with all the values combined solving the behavior of
 * logical and of all of them)
 * 
 * actionf_p1, actionf_v and actionf_p2 are defined as these:
 * 
 * typedef  void (*actionf_v)();
 * typedef  void (*actionf_p1)( void* );
 * typedef  void (*actionf_p2)( void*, void* );
 * 
 * As you can see, they are pointers, so they all occupy the same space
 * in the union: the length of the memory pointer.
 * 
 * Effectively, this means that you can write to any of the three fields
 * the pointer to the function correspoding to the field, and
 * it will completely overwrite any other function assigned in other
 * two fields. Even more: the other fields will have the same pointer,
 * just with wrong type.
 * 
 * In Mocha Doom, this were addressed differently. A special helper enum
 * was created to hold possible names of the functions, and they were checked
 * by name, not by equality of the objects (object == object if point the same)
 * assigned to one of three fields. But, not understanding the true nature
 * of C's unions, in Mocha Doom all three fields were preserved and threated
 * like they can hold some different information at the same time.
 * 
 * I present hereby the solution that will both simplify the definition
 * and usage of the action functions, and provide a way to achieve the
 * exact same behavior as would be in C: if you assign the function,
 * you will replace the old one (virtually, "all the three fields")
 * and you can call any function with 0 to 2 arguments.
 * 
 * Also to store the functions in the same place where we declare them,
 * an Command pattern is implemented, requiring the function caller
 * to provide himself or any sufficient class that implements the Client
 * contract to provide the information needed for holding the state
 * of action functions.
 * 
 * - Good Sign 2017/04/28
 * 
 * Thinkers can either have one parameter of type (mobj_t),
 * Or otherwise be sector specials, flickering lights etc.
 * Those are atypical and need special handling.
 */
public enum ActiveStates implements ThinkerStates{
    NOP(ActiveStates::nop, ThinkerConsumer.class),
    A_Light0(ActionFunctions::A_Light0, PlayerSpriteConsumer.class),
    A_WeaponReady(ActionFunctions::A_WeaponReady, PlayerSpriteConsumer.class),
    A_Lower(ActionFunctions::A_Lower, PlayerSpriteConsumer.class),
    A_Raise(ActionFunctions::A_Raise, PlayerSpriteConsumer.class),
    A_Punch(ActionFunctions::A_Punch, PlayerSpriteConsumer.class),
    A_ReFire(ActionFunctions::A_ReFire, PlayerSpriteConsumer.class),
    A_FirePistol(ActionFunctions::A_FirePistol, PlayerSpriteConsumer.class),
    A_Light1(ActionFunctions::A_Light1, PlayerSpriteConsumer.class),
    A_FireShotgun(ActionFunctions::A_FireShotgun, PlayerSpriteConsumer.class),
    A_Light2(ActionFunctions::A_Light2, PlayerSpriteConsumer.class),
    A_FireShotgun2(ActionFunctions::A_FireShotgun2, PlayerSpriteConsumer.class),
    A_CheckReload(ActionFunctions::A_CheckReload, PlayerSpriteConsumer.class),
    A_OpenShotgun2(ActionFunctions::A_OpenShotgun2, PlayerSpriteConsumer.class),
    A_LoadShotgun2(ActionFunctions::A_LoadShotgun2, PlayerSpriteConsumer.class),
    A_CloseShotgun2(ActionFunctions::A_CloseShotgun2, PlayerSpriteConsumer.class),
    A_FireCGun(ActionFunctions::A_FireCGun, PlayerSpriteConsumer.class),
    A_GunFlash(ActionFunctions::A_GunFlash, PlayerSpriteConsumer.class),
    A_FireMissile(ActionFunctions::A_FireMissile, PlayerSpriteConsumer.class),
    A_Saw(ActionFunctions::A_Saw, PlayerSpriteConsumer.class),
    A_FirePlasma(ActionFunctions::A_FirePlasma, PlayerSpriteConsumer.class),
    A_BFGsound(ActionFunctions::A_BFGsound, PlayerSpriteConsumer.class),
    A_FireBFG(ActionFunctions::A_FireBFG, PlayerSpriteConsumer.class),
    A_BFGSpray(ActionFunctions::A_BFGSpray, MobjConsumer.class),
    A_Explode(ActionFunctions::A_Explode, MobjConsumer.class),
    A_Pain(ActionFunctions::A_Pain, MobjConsumer.class),
    A_PlayerScream(ActionFunctions::A_PlayerScream, MobjConsumer.class),
    A_Fall(ActionFunctions::A_Fall, MobjConsumer.class),
    A_XScream(ActionFunctions::A_XScream, MobjConsumer.class),
    A_Look(ActionFunctions::A_Look, MobjConsumer.class),
    A_Chase(ActionFunctions::A_Chase, MobjConsumer.class),
    A_FaceTarget(ActionFunctions::A_FaceTarget, MobjConsumer.class),
    A_PosAttack(ActionFunctions::A_PosAttack, MobjConsumer.class),
    A_Scream(ActionFunctions::A_Scream, MobjConsumer.class),
    A_SPosAttack(ActionFunctions::A_SPosAttack, MobjConsumer.class),
    A_VileChase(ActionFunctions::A_VileChase, MobjConsumer.class),
    A_VileStart(ActionFunctions::A_VileStart, MobjConsumer.class),
    A_VileTarget(ActionFunctions::A_VileTarget, MobjConsumer.class),
    A_VileAttack(ActionFunctions::A_VileAttack, MobjConsumer.class),
    A_StartFire(ActionFunctions::A_StartFire, MobjConsumer.class),
    A_Fire(ActionFunctions::A_Fire, MobjConsumer.class),
    A_FireCrackle(ActionFunctions::A_FireCrackle, MobjConsumer.class),
    A_Tracer(ActionFunctions::A_Tracer, MobjConsumer.class),
    A_SkelWhoosh(ActionFunctions::A_SkelWhoosh, MobjConsumer.class),
    A_SkelFist(ActionFunctions::A_SkelFist, MobjConsumer.class),
    A_SkelMissile(ActionFunctions::A_SkelMissile, MobjConsumer.class),
    A_FatRaise(ActionFunctions::A_FatRaise, MobjConsumer.class),
    A_FatAttack1(ActionFunctions::A_FatAttack1, MobjConsumer.class),
    A_FatAttack2(ActionFunctions::A_FatAttack2, MobjConsumer.class),
    A_FatAttack3(ActionFunctions::A_FatAttack3, MobjConsumer.class),
    A_BossDeath(ActionFunctions::A_BossDeath, MobjConsumer.class),
    A_CPosAttack(ActionFunctions::A_CPosAttack, MobjConsumer.class),
    A_CPosRefire(ActionFunctions::A_CPosRefire, MobjConsumer.class),
    A_TroopAttack(ActionFunctions::A_TroopAttack, MobjConsumer.class),
    A_SargAttack(ActionFunctions::A_SargAttack, MobjConsumer.class),
    A_HeadAttack(ActionFunctions::A_HeadAttack, MobjConsumer.class),
    A_BruisAttack(ActionFunctions::A_BruisAttack, MobjConsumer.class),
    A_SkullAttack(ActionFunctions::A_SkullAttack, MobjConsumer.class),
    A_Metal(ActionFunctions::A_Metal, MobjConsumer.class),
    A_SpidRefire(ActionFunctions::A_SpidRefire, MobjConsumer.class),
    A_BabyMetal(ActionFunctions::A_BabyMetal, MobjConsumer.class),
    A_BspiAttack(ActionFunctions::A_BspiAttack, MobjConsumer.class),
    A_Hoof(ActionFunctions::A_Hoof, MobjConsumer.class),
    A_CyberAttack(ActionFunctions::A_CyberAttack, MobjConsumer.class),
    A_PainAttack(ActionFunctions::A_PainAttack, MobjConsumer.class),
    A_PainDie(ActionFunctions::A_PainDie, MobjConsumer.class),
    A_KeenDie(ActionFunctions::A_KeenDie, MobjConsumer.class),
    A_BrainPain(ActionFunctions::A_BrainPain, MobjConsumer.class),
    A_BrainScream(ActionFunctions::A_BrainScream, MobjConsumer.class),
    A_BrainDie(ActionFunctions::A_BrainDie, MobjConsumer.class),
    A_BrainAwake(ActionFunctions::A_BrainAwake, MobjConsumer.class),
    A_BrainSpit(ActionFunctions::A_BrainSpit, MobjConsumer.class),
    A_SpawnSound(ActionFunctions::A_SpawnSound, MobjConsumer.class),
    A_SpawnFly(ActionFunctions::A_SpawnFly, MobjConsumer.class),
    A_BrainExplode(ActionFunctions::A_BrainExplode, MobjConsumer.class),
    P_MobjThinker(ActionFunctions::P_MobjThinker, MobjConsumer.class),
    T_FireFlicker(ActionFunctions::T_FireFlicker, ThinkerConsumer.class),
    T_LightFlash(ActionFunctions::T_LightFlash, ThinkerConsumer.class),
    T_StrobeFlash(ActionFunctions::T_StrobeFlash, ThinkerConsumer.class),
    T_Glow(ActionFunctions::T_Glow, ThinkerConsumer.class),
    T_MoveCeiling(ActionFunctions::T_MoveCeiling, ThinkerConsumer.class),
    T_MoveFloor(ActionFunctions::T_MoveFloor, ThinkerConsumer.class),
    T_VerticalDoor(ActionFunctions::T_VerticalDoor, ThinkerConsumer.class),
    T_PlatRaise(ActionFunctions::T_PlatRaise, ThinkerConsumer.class),
    T_SlidingDoor(ActionFunctions::T_SlidingDoor, ThinkerConsumer.class);
    
    private final static Logger LOGGER = Loggers.getLogger(ActiveStates.class.getName());
    
    private final ParamClass<?> actionFunction;
    private final Class<? extends ParamClass<?>> paramType;

    private <T extends ParamClass<?>> ActiveStates(final T actionFunction, final Class<T> paramType) {
        this.actionFunction = actionFunction;
        this.paramType = paramType;
    }
    
    private static void nop(Object... o) {}

    @actionf_p1
    @D_Think.C(actionf_t.acp1)
    public interface MobjConsumer extends ParamClass<MobjConsumer> {
    	void accept(ActionFunctions a, mobj_t m);
    }
    
    @actionf_v
    @D_Think.C(actionf_t.acv)
    public interface ThinkerConsumer extends ParamClass<ThinkerConsumer> {
    	void accept(ActionFunctions a, thinker_t t);
    }
    
    @actionf_p2
    @D_Think.C(actionf_t.acp2)
    public interface PlayerSpriteConsumer extends ParamClass<PlayerSpriteConsumer> {
    	void accept(ActionFunctions a, player_t p, pspdef_t s);
    }

    private interface ParamClass<T extends ParamClass<T>> {}
    
    public boolean isParamType(final Class<?> paramType) {
        return this.paramType == paramType;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends ParamClass<T>> T fun(final Class<T> paramType) {
        if (this.paramType != paramType) {
            LOGGER.log(Level.WARNING, "Wrong paramType for state: {0}", this);
            return null;
        }
        
        // don't believe, it's checked
        return (T) this.actionFunction;
    }
}
