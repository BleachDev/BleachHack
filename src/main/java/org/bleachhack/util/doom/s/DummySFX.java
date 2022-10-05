package org.bleachhack.util.doom.s;

import org.bleachhack.util.doom.data.sfxinfo_t;

public class DummySFX implements ISoundDriver {

	@Override
	public boolean InitSound() {
		// Dummy is super-reliable ;-)
		return true;
	}

	@Override
	public void UpdateSound() {
		// TODO Auto-generated method stub

	}

	@Override
	public void SubmitSound() {
		// TODO Auto-generated method stub

	}

	@Override
	public void ShutdownSound() {
		// TODO Auto-generated method stub

	}

	@Override
	public int GetSfxLumpNum(sfxinfo_t sfxinfo) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int StartSound(int id, int vol, int sep, int pitch, int priority) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void StopSound(int handle) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean SoundIsPlaying(int handle) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void UpdateSoundParams(int handle, int vol, int sep, int pitch) {
		// TODO Auto-generated method stub

	}

	@Override
	public void SetChannels(int numChannels) {
		// TODO Auto-generated method stub
		
	}

}
