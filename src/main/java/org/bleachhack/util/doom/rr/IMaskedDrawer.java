package org.bleachhack.util.doom.rr;

/** Draws any masked stuff -sprites, textures, or special 3D floors */

public interface IMaskedDrawer<T,V> extends IDetailAware {

    public static final int BASEYCENTER = 100;
    
    /** Cache the sprite manager, if possible */

    void cacheSpriteManager(ISpriteManager SM);

    void DrawMasked();

    void setPspriteIscale(int i);

    void setPspriteScale(int i);

    /**
     * For serial masked drawer, just complete the column function. For
     * parallel version, store rendering instructions and execute later on.
     * HINT: you need to discern between masked and non-masked draws.
     */

    void completeColumn();
}
