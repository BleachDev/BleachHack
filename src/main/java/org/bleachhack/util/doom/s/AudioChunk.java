package org.bleachhack.util.doom.s;

public class AudioChunk{
    public AudioChunk() {
        buffer=new byte[org.bleachhack.util.doom.s.ISoundDriver.MIXBUFFERSIZE];
        setStuff(0,0);
        this.free=true;
    }
    
    public void setStuff(int chunk, int time){
        this.chunk = chunk;
        this.time = time;
    }
    
    public int chunk;
    public int time;
    public byte[] buffer;
    public boolean free;
    

}