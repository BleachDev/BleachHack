package org.bleachhack.util.doom.data;

import static org.bleachhack.util.doom.data.Defines.TIC_MUL;
import org.bleachhack.util.doom.defines.statenum_t;
import org.bleachhack.util.doom.p.ActiveStates;
import static org.bleachhack.util.doom.p.ActiveStates.NOP;

public class state_t {

    public state_t() {

    }

    public state_t(spritenum_t sprite, int frame, int tics, ActiveStates action, statenum_t nextstate, int misc1, int misc2) {
        this.sprite = sprite;
        this.frame = frame;
        this.tics = tics * TIC_MUL;
        this.action = action == null ? NOP : action;
        this.nextstate = nextstate;
        this.misc1 = misc1;
        this.misc2 = misc2;
    }

    public spritenum_t sprite;
    /**
     * The frame should indicate which one of the frames available in the
     * available spritenum should be used. This can also be flagged with
     * 0x8000 indicating bright sprites.
     */

    public int frame;
    public int tics;
    //TODO: proper implementation of (*action)
    // MAES: was actionp_t... which is typedeffed to ActionFunction anyway,
    // and this is the only place it's invoked explicitly.
    /**
     * OK...this is the most infamous part of Doom to implement in Java.
     * We can't have proper "function pointers" in java without either losing a LOT
     * of speed (through reflection) or cluttering syntax and heap significantly
     * (callback objects, which also need to be aware of context).
     * Therefore, I decided to implement an "action dispatcher".
     * This a
     *
     */
    public ActiveStates action;

    public statenum_t nextstate;
    public int misc1, misc2;
    
    /**
     * relative index in state array. Needed sometimes.
     */
    public int id;

    @Override
    public String toString() {
        sb.setLength(0);
        sb.append(this.getClass().getName());
        sb.append(" sprite ");
        sb.append(this.sprite.name());
        sb.append(" frame ");
        sb.append(this.frame);

        return sb.toString();

    }

    protected static StringBuilder sb = new StringBuilder();

    /*@Override
    public void read(DoomFile f) throws IOException {
        this.sprite = spritenum_t.values()[f.readLEInt()];
        this.frame = f.readLEInt();
        this.tics = f.readLong();
        this.action = ActionFunction.values()[f.readInt()];
        this.nextstate = statenum_t.values()[f.readInt()];
        this.misc1 = f.readInt();
        this.misc2 = f.readInt();
    } */
}
