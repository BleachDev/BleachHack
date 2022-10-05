package org.bleachhack.util.doom.i;

import org.bleachhack.util.doom.doom.ticcmd_t;

public interface IDoomSystem {

	public void AllocLow(int length);

	public void BeginRead();

	public void EndRead();

	public void WaitVBL(int count);

	public byte[] ZoneBase(int size);

	public int GetHeapSize();

	public void Tactile(int on, int off, int total);

	public void Quit();

	public ticcmd_t BaseTiccmd();

	public void Error(String error, Object ... args);

	void Error(String error);
	
	void Init();
	
	/** Generate a blocking alert with the intention of continuing or aborting
	 * a certain game-altering action. E.g. loading PWADs, or upon critical 
	 * level loading failures. This can be either a popup panel or console 
	 * message.
	 *  
	 * @param cause Provide a clear string explaining why the alert was generated
	 * @return true if we should continue, false if an alternate action should be taken.
	 */
	boolean GenerateAlert(String title,String cause);


}
