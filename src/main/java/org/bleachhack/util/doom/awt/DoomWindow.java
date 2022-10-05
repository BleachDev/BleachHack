package org.bleachhack.util.doom.awt;

import org.bleachhack.util.doom.doom.CommandVariable;
import org.bleachhack.util.doom.doom.event_t;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.JPanel;
import org.bleachhack.util.doom.mochadoom.Engine;

/** 
 *  Methods specific to Doom-System video interfacing. 
 *  In essence, whatever you are using as a final system-specific way to display
 *  the screens, should be able to respond to these commands. In particular,
 *  screen update requests must be honored, and palette/gamma request changes
 *  must be intercepted before they are forwarded to the renderers (in case they
 *  are system-specific, rather than renderer-specific).
 *  
 *  The idea is that the final screen rendering module sees/handles as less as
 *  possible, and only gets a screen to render, no matter what depth it is.
 */
public interface DoomWindow<E extends Component & DoomWindow<E>> {
    /**
     * Get current graphics device
     */
    static GraphicsDevice getDefaultDevice() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    }

    /**
     * Get an instance of JFrame to draw anything. This will try to create compatible Canvas and
     * will bing all AWT listeners
     */
    static DoomWindowController<CanvasWindow, EventHandler> createCanvasWindowController(
        final Supplier<Image> imageSource,
        final Consumer<? super event_t> doomEventConsume,
        final int width, final int height
    ) {
        final GraphicsDevice device = getDefaultDevice();
        return new DoomWindowController<>(EventHandler.class, device, imageSource, doomEventConsume,
            new CanvasWindow(getDefaultDevice().getDefaultConfiguration()), width, height);
    }
    
    /**
     * Get an instance of JFrame to draw anything. This will try to create compatible Canvas and
     * will bing all AWT listeners
     */
    static DoomWindowController<JPanelWindow, EventHandler> createJPanelWindowController(
        final Supplier<Image> imageSource,
        final Consumer<? super event_t> doomEventConsume,
        final int width, final int height
    ) {
        return new DoomWindowController<>(EventHandler.class, getDefaultDevice(), imageSource,
            doomEventConsume, new JPanelWindow(), width, height);
    }
    
    /**
     * Incomplete. Only checks for -geom format
     */
    @SuppressWarnings("UnusedAssignment")
    default boolean handleGeom() {
        int x = 0;
        int y = 0;
        
        // warning: char format, different type arg
        int xsign = ' ';
        int ysign = ' ';
        /*
        String displayname;
        String d;
        int n;
        int pnum;
        
        boolean oktodraw;
        long attribmask;
        
        // Try setting the locale the US, otherwise there will be problems
        // with non-US keyboards.
        if (this.getInputContext() == null || !this.getInputContext().selectInputMethod(java.util.Locale.US)) {
            System.err.println("Could not set the input context to US! Keyboard input will be glitchy!");
        } else {
            System.err.println("Input context successfully set to US.");
        }
        
        // check for command-line display name
        displayname = Game.getCVM().get(CommandVariable.DISP, String.class, 0).orElse(null);
        
        // check for command-line geometry*/
        if (Engine.getCVM().present(CommandVariable.GEOM)) {
            try {
                String eval = Engine.getCVM().get(CommandVariable.GEOM, String.class, 0).get().trim();
                // warning: char format, different type arg 3,5
                //n = sscanf(myargv[pnum+1], "%c%d%c%d", &xsign, &x, &ysign, &y);
                // OK, so we have to read a string that may contain
                // ' '/'+'/'-' and a number. Twice.
                StringTokenizer tk = new StringTokenizer(eval, "-+ ");
                // Signs. Consider positive.
                xsign = 1;
                ysign = 1;
                for (int i = 0; i < eval.length(); i++) {
                    if (eval.charAt(i) == '-') {
                        // First '-' on trimmed string: negagive
                        if (i == 0) {
                            xsign = -1;
                        } else {
                            ysign = -1;
                        }
                    }
                }
                
                //this should parse two numbers.
                if (tk.countTokens() == 2) {
                    x = xsign * Integer.parseInt(tk.nextToken());
                    y = ysign * Integer.parseInt(tk.nextToken());
                }
                
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        return true;
    }
    
    final static class JPanelWindow extends JPanel implements DoomWindow<JPanelWindow> {
		private static final long serialVersionUID = 4031722796186278753L;

		private JPanelWindow() {
            init();
        }
        
        private void init() {
            setDoubleBuffered(true);
            setOpaque(true);
            setBackground(Color.BLACK);
        }
        
        @Override
        public boolean isOptimizedDrawingEnabled() {
            return false;
        }
    }
    
    final static class CanvasWindow extends Canvas implements DoomWindow<CanvasWindow> {
		private static final long serialVersionUID = 1180777361390303859L;

		private CanvasWindow(GraphicsConfiguration config) {
            super(config);
        }
    }
}
