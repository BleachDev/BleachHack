package org.bleachhack.util.doom.s;

import org.bleachhack.util.doom.p.mobj_t;
import org.bleachhack.util.doom.data.sounds.musicenum_t;
import org.bleachhack.util.doom.data.sounds.sfxenum_t;

/** Does nothing. Just allows me to code without
 *  commenting out ALL sound-related code. Hopefully
 *  it will be superseded by a real sound driver one day.
 * 
 * @author Velktron
 *
 */

public class DummySoundDriver implements IDoomSound{

	@Override
	public void Init(int sfxVolume, int musicVolume) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void StartSound(ISoundOrigin origin, int sound_id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void StartSound(ISoundOrigin origin, sfxenum_t sound_id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void StartSoundAtVolume(ISoundOrigin origin, int sound_id, int volume) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void StopSound(ISoundOrigin origin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ChangeMusic(int musicnum, boolean looping) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void StopMusic() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void PauseSound() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ResumeSound() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void UpdateSounds(mobj_t listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void SetMusicVolume(int volume) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void SetSfxVolume(int volume) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void StartMusic(int music_id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void StartMusic(musicenum_t music_id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ChangeMusic(musicenum_t musicnum, boolean looping) {
		// TODO Auto-generated method stub
		
	}

}
