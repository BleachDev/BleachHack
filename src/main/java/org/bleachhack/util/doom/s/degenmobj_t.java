package org.bleachhack.util.doom.s;

public final class degenmobj_t
        implements ISoundOrigin {

    private final int x, y, z;

    public degenmobj_t(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public degenmobj_t(int x, int y) {
        this.x = x;
        this.y = y;
        this.z = 0;
    }

    @Override
    public final int getX() {
        return x;
    }

    @Override
    public final int getY() {
        return y;
    }

    @Override
    public final int getZ() {
        return z;
    }

}
