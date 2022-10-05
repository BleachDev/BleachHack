package org.bleachhack.util.doom.m;

public class default_t {

    public default_t(String name, int[] location, int defaultvalue) {
        this.name = name;
        this.location = location;
        this.defaultvalue = defaultvalue;
    }

    public String name;

    /** this is supposed to be a pointer */
    public int[] location;

    public int defaultvalue;

    int scantranslate; // PC scan code hack

    int untranslated; // lousy hack
};
