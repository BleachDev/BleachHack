package org.bleachhack.util.doom.i;

public interface IDiskDrawer extends IDrawer {

    /**
     * Set a timeout (in tics) for displaying the disk icon
     *
     * @param timeout
     */
    void setReading(int reading);

    /**
     * Disk displayer is currently active
     *
     * @return
     */
    boolean isReading();

    /**
     * Only call after the Wadloader is instantiated and initialized itself.
     *
     */
    void Init();

    /**
     * Status only valid after the last tic has been drawn. Use to know when to redraw status bar.
     *
     * @return
     */
    boolean justDoneReading();

}
