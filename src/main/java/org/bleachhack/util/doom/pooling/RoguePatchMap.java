package org.bleachhack.util.doom.pooling;

public class RoguePatchMap extends GenericIntMap<byte[][]> {
	
	public RoguePatchMap(){
		super();
		 patches = new byte[DEFAULT_CAPACITY][][];
	}
}
