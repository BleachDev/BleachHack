package org.bleachhack.util.doom.boom;

public class prboom_comp_t {

    public prboom_comp_t(int minver, int maxver, boolean state, String cmd) {
        this.minver = minver;
        this.maxver = maxver;
        this.state = state;
        this.cmd = cmd;
    }

    public int minver;

    public int maxver;

    public boolean state;

    public String cmd;
}