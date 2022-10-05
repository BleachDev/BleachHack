package org.bleachhack.util.doom.data;

public class musicinfo_t {

    public musicinfo_t() {
    }

    
    public musicinfo_t(String name) {
        this.name = name;
    }
    
    public musicinfo_t(String name, int lumpnum) {
        this.name = name;
        this.lumpnum = lumpnum;
    }

    // up to 6-character name
    public String   name;

    // lump number of music
    public int     lumpnum;
    
    // music data
    public byte[] data;

    // music handle once registered
    public int handle;
}
