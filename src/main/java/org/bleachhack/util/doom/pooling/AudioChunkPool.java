package org.bleachhack.util.doom.pooling;

import org.bleachhack.util.doom.s.AudioChunk;

// Referenced classes of package org.bleachhack.util.doom.pooling:
//            ObjectPool

public class AudioChunkPool extends ObjectQueuePool<AudioChunk>
{

    public AudioChunkPool()    
    {
    	// A reasonable time limit for Audio chunks
    	super(10000L);
    }

    protected AudioChunk create()
    {
        return new AudioChunk();
    }

    public void expire(AudioChunk o)
    {
        o.free = true;
    }

    public boolean validate(AudioChunk o)
    {
        return o.free;
    }

}
