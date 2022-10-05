package org.bleachhack.util.doom.hu;

import net.minecraft.client.gui.screen.Screen;
import org.bleachhack.util.doom.doom.SourceCode.HU_Stuff;
import static org.bleachhack.util.doom.doom.SourceCode.HU_Stuff.HU_Responder;
import org.bleachhack.util.doom.doom.event_t;
import org.bleachhack.util.doom.rr.patch_t;

public interface IHeadsUp {

	void Ticker();

	void Erase();

	void Drawer();

    @HU_Stuff.C(HU_Responder)
	boolean Responder(event_t ev);

	patch_t[] getHUFonts();

	char dequeueChatChar();

	void Init();

	void setChatMacro(int i, String s);

	void Start();

	void Stop();

}
