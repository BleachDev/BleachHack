/*
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
package org.bleachhack.util.doom.awt;

import org.bleachhack.util.doom.doom.event_t;
import java.awt.Color;
import java.awt.Component;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.bleachhack.util.doom.m.Settings;
import org.bleachhack.util.doom.mochadoom.Engine;
import org.bleachhack.util.doom.mochadoom.Loggers;

/**
 * Display, its configuration and resolution related stuff,
 * DoomFrame creation, full-screen related code. Window recreation control.
 * That sort of things.
 */
public class DoomWindowController<E extends Component & DoomWindow<E>, H extends Enum<H> & EventBase<H>> implements FullscreenOptions {
    private static final long ALL_EVENTS_MASK = 0xFFFF_FFFF_FFFF_FFFFL;

    final GraphicsDevice device;
    final FullscreenFunction switcher;
    final int defaultWidth, defaultHeight;

    private final E component;
    private final EventObserver<H> observer;
    private DoomFrame<E> doomFrame;

    /**
     * Default window size. It might change upon entering full screen, so don't consider it absolute. Due to letter
     * boxing and screen doubling, stretching etc. it might be different that the screen buffer (typically, larger).
     */
    private final DimensionImpl dimension;
    private boolean isFullScreen;

    DoomWindowController(
        final Class<H> handlerClass,
        final GraphicsDevice device,
        final Supplier<Image> imageSource,
        final Consumer<? super event_t> doomEventConsumer,
        final E component,
        final int defaultWidth,
        final int defaultHeight
    ) {
        this.device = device;
        this.switcher = createFullSwitcher(device);
        this.component = component;
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;
        this.dimension = new DimensionImpl(defaultWidth, defaultHeight);
        this.doomFrame = new DoomFrame<>(dimension, component, imageSource);
        this.observer = new EventObserver<>(handlerClass, component, doomEventConsumer);
        Toolkit.getDefaultToolkit().addAWTEventListener(observer::observe, ALL_EVENTS_MASK);
        sizeInit();
        doomFrame.turnOn();
    }
    
    private void sizeInit() {
        try {
            if (!(Engine.getConfig().equals(Settings.fullscreen, Boolean.TRUE) && switchToFullScreen())) {
                updateSize();
            }
        } catch (Exception e) {
            Loggers.getLogger(DoomWindow.class.getName()).log(Level.SEVERE,
                    String.format("Error creating DOOM AWT frame. Exiting. Reason: %s", e.getMessage()), e);
            throw e;
        }
    }
    
    public void updateFrame() {
        doomFrame.update();
    }

    public void closeFrame() {
        doomFrame.dispose();
    }

    public EventObserver<H> getObserver() {
        return observer;
    }

    public boolean switchFullscreen() {
        Loggers.getLogger(DoomFrame.class.getName()).log(Level.WARNING, "FULLSCREEN SWITHED");
        // remove the frame from view
        doomFrame.dispose();
        doomFrame = new DoomFrame<>(dimension, component, doomFrame.imageSupplier);
        // change all the properties
        final boolean ret = switchToFullScreen();
        // now show back the frame
        doomFrame.turnOn();
        return ret;
    }

    /**
     * FULLSCREEN SWITCH CODE TODO: it's not enough to do this without also switching the screen's resolution.
     * Unfortunately, Java only has a handful of options which depend on the OS, driver, display, JVM etc. and it's not
     * possible to switch to arbitrary resolutions.
     *
     * Therefore, a "best fit" strategy with centering is used.
     */
    public final boolean switchToFullScreen() {
        if (!isFullScreen) {
            isFullScreen = device.isFullScreenSupported();
            if (!isFullScreen) {
                return false;
            }
        } else {
            isFullScreen = false;
        }
        final DisplayMode displayMode = switcher.get(defaultWidth, defaultHeight);
        doomFrame.setUndecorated(isFullScreen);

        // Full-screen mode
        device.setFullScreenWindow(isFullScreen ? doomFrame : null);
        if (device.isDisplayChangeSupported()) {
            device.setDisplayMode(displayMode);
        }

        component.validate();
        dimension.setSize(displayMode);
        updateSize();
        return isFullScreen;
    }

    private void updateSize() {
        doomFrame.setPreferredSize(isFullscreen() ? dimension : null);
        component.setPreferredSize(dimension);
        component.setBounds(0, 0, defaultWidth - 1, defaultHeight - 1);
        component.setBackground(Color.black);
        doomFrame.renewGraphics();
    }

    public boolean isFullscreen() {
        return isFullScreen;
    }
    
    private class DimensionImpl extends java.awt.Dimension implements Dimension {
		private static final long serialVersionUID = 4598094740125688728L;
		private int offsetX, offsetY;
        private int fitWidth, fitHeight;

        DimensionImpl(int width, int height) {
            this.width = defaultWidth;
            this.height = defaultHeight;
            this.offsetX = offsetY = 0;
            this.fitWidth = width;
            this.fitHeight = height;
        }
        
        @Override
        public int width() {
            return width;
        }

        @Override
        public int height() {
            return height;
        }

        @Override
        public int defWidth() {
            return defaultWidth;
        }

        @Override
        public int defHeight() {
            return defaultHeight;
        }

        @Override
        public int fitX() {
            return fitWidth;
        }

        @Override
        public int fitY() {
            return fitHeight;
        }

        @Override
        public int offsX() {
            return offsetX;
        }

        @Override
        public int offsY() {
            return offsetY;
        }
        
        private void setSize(DisplayMode mode) {
            if (isFullScreen) {
                this.width = mode.getWidth();
                this.height = mode.getHeight();
                this.offsetX = Dimension.super.offsX();
                this.offsetY = Dimension.super.offsY();
                this.fitWidth = Dimension.super.fitX();
                this.fitHeight = Dimension.super.fitY();
            } else {
                this.width = defaultWidth;
                this.height = defaultHeight;
                this.offsetX = offsetY = 0;
                this.fitWidth = width;
                this.fitHeight = height;
            }
        }
    }
}
