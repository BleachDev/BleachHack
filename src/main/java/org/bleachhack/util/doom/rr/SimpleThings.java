package org.bleachhack.util.doom.rr;

import org.bleachhack.util.doom.v.scale.VideoScale;

/**
 * A very "simple" things class which just does serial rendering and uses all
 * the base methods from AbstractThings.
 * 
 * @author velktron
 * @param <T>
 * @param <V>
 */


public final class SimpleThings<T,V> extends AbstractThings<T,V> {

    public SimpleThings(VideoScale vs, SceneRenderer<T, V> R) {
        super(vs, R);
    }

    @Override
    public void completeColumn() {
        colfunc.invoke();
    }
}
