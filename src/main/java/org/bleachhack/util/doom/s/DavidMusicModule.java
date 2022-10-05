package org.bleachhack.util.doom.s;

import java.io.ByteArrayInputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Transmitter;

/** Concern separated from David Martel's MIDI & MUS player
 *  for Mocha Doom. Greatly improved upon by finnw, perfecting volume changes
 *  and MIDI device detection.
 *  
 * @author David Martel
 * @author velktron
 * @author finnw
 *
 */

public class DavidMusicModule implements IMusic {
	
	public static final int CHANGE_VOLUME=7;
	public static final int CHANGE_VOLUME_FINE=9;
	
	Sequencer sequencer;
	VolumeScalingReceiver receiver;
	Transmitter transmitter;
	boolean songloaded;
	
	public DavidMusicModule(){

	}

	@Override
	public void InitMusic() {
		try {
			
			 int x=-1;
			MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();   
		     for (int i = 0; i < info.length; i++)  {
		    	 MidiDevice mdev=MidiSystem.getMidiDevice(info[i]);
		    	 if (mdev instanceof Sequencer) x=i;
		        //  System.out.println(info[i].getName()+"\t\t\t"+ mdev.isOpen()+"\t"+mdev.hashCode());
		          
		     }
		
		     //System.out.printf("x %d y %d \n",x,y);
		     //--This sets the Sequencer and Synthesizer  
		     //--The indices x and y correspond to the correct entries for the  
		     //--default Sequencer and Synthesizer, as determined above  	       
		      
		    if (x!=-1)
		    	sequencer = (Sequencer) MidiSystem.getMidiDevice(info[x]);
		    else
		    	sequencer = (Sequencer) MidiSystem.getSequencer(false);
			sequencer.open();
			
		    receiver = VolumeScalingReceiver.getInstance();
		    // Configure General MIDI level 1
		    sendSysexMessage(receiver, (byte)0xf0, (byte)0x7e, (byte)0x7f, (byte)9, (byte)1, (byte)0xf7);
		    transmitter = sequencer.getTransmitter();
		    transmitter.setReceiver(receiver);
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}
	}

    private static void sendControlChange(Receiver receiver, int midiChan, int ctrlId, int value) {
        ShortMessage msg = new ShortMessage();
        try {
            msg.setMessage(ShortMessage.CONTROL_CHANGE, midiChan, ctrlId, value);
        } catch (InvalidMidiDataException ex) {
            throw new RuntimeException(ex);
        }
        receiver.send(msg, -1);
    }

	private static void sendSysexMessage(Receiver receiver, byte... message) {
	    SysexMessage msg = new SysexMessage();
	    try {
            msg.setMessage(message, message.length);
        } catch (InvalidMidiDataException ex) {
            throw new RuntimeException(ex);
        }
        receiver.send(msg, -1);
    }

    @Override
	public void ShutdownMusic() {
		sequencer.stop();
		sequencer.close();
	}

	@Override
	public void SetMusicVolume(int volume) {
		
		System.out.println("Midi volume set to "+volume);
		receiver.setGlobalVolume(volume / 127f);

	}

	@Override
	public void PauseSong(int handle) {
		if (songloaded)
		sequencer.stop();
		}

	@Override
	public void ResumeSong(int handle) {		
		if (songloaded){
			System.out.println("Resuming song");
		sequencer.start();
		}

	}

	@Override
	public int RegisterSong(byte[] data) {
		try {
            Sequence sequence;
	        ByteArrayInputStream bis;
	        try {
	            // If data is a midi file, load it directly
	            bis = new ByteArrayInputStream(data);
	            sequence = MidiSystem.getSequence(bis);
	        } catch (InvalidMidiDataException ex) {
	        	// Well, it wasn't. Dude.
                bis = new ByteArrayInputStream(data);
	            sequence = MusReader.getSequence(bis);
	        }
            sequencer.stop(); // stops current music if any
            sequencer.setSequence(sequence); // Create a sequencer for the sequence
            songloaded=true;
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	return -1;
	    } 
		// In good old C style, we return 0 upon success?
		return 0;
	}

	@Override
	public void PlaySong(int handle, boolean looping) {
		if (songloaded){
	        for (int midiChan = 0; midiChan < 16; ++ midiChan) {
	            setPitchBendSensitivity(receiver, midiChan, 2);
	        }
            if (looping)
            	sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            else
            	sequencer.setLoopCount(0);
            sequencer.start(); // Start playing
		}
	}

	private void setPitchBendSensitivity(Receiver receiver, int midiChan, int semitones) {
	    sendRegParamChange(receiver, midiChan, 0, 0, 2);
    }

    private void sendRegParamChange(Receiver receiver, int midiChan, int paramMsb, int paramLsb, int valMsb) {
        sendControlChange(receiver, midiChan, 101, paramMsb);
        sendControlChange(receiver, midiChan, 100, paramLsb);
        sendControlChange(receiver, midiChan, 6, valMsb);
    }

    @Override
	public void StopSong(int handle) {
		sequencer.stop();

	}

	@Override
	public void UnRegisterSong(int handle) {
		// In theory, we should ask the sequencer to "forget" about the song.
		// However since we can register another without unregistering the first,
		// this is practically a dummy.
		
		songloaded=false;

	}

}
