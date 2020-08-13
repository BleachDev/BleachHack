package bleach.hack.utils;

import bleach.hack.event.events.EventTick;
import com.google.common.eventbus.Subscribe;

import java.awt.*;

public class Rainbow {

    public static int rgb;
    public static float hue = 0.0F;
    public static int speed = 2;

    @Subscribe
    public void onTick(EventTick event) {
        this.rgb = Color.HSBtoRGB(this.hue, 1.0F, 1.0F);
        this.hue += this.speed / 2000.0F;
    }

    public static int getInt(){
        return Rainbow.rgb;
    }
}
