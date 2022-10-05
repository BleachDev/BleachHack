package org.bleachhack.util.doom.doom;

import org.bleachhack.util.doom.defines.skill_t;

/** Groups functions formerly in d_game, 
 * in case you want to provide a different implementation 
 */

public interface IDoomGame {

	 void ExitLevel ();
	 void WorldDone ();

     boolean CheckDemoStatus();
     
     /** Can be called by the startup code or M_Responder.
     A normal game starts at map 1,
     but a warp test can start elsewhere */
     
     public void DeferedInitNew (skill_t skill, int episode, int map);
     
     /** Can be called by the startup code or M_Responder,
     calls P_SetupLevel or W_EnterWorld. */
   public void LoadGame (String name);

   /** Called by M_Responder. */
   public void SaveGame (int slot, String description);
   
   /** Takes a screenshot *NOW*
    * 
    */
   public void ScreenShot() ;
   
   public void StartTitle();
   
   public gameaction_t getGameAction();

   public void setGameAction(gameaction_t ga);
   
   // public void PlayerReborn(int player);
   
   void DeathMatchSpawnPlayer(int playernum);

}
