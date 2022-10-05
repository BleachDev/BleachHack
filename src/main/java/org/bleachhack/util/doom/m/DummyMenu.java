package org.bleachhack.util.doom.m;

import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.event_t;

/** A dummy menu, useful for testers that do need a defined
 *  menu object.
 * 
 * @author Maes
 *
 */

public class DummyMenu<T, V> extends AbstractDoomMenu<T, V> {
    public DummyMenu(DoomMain<T, V> DOOM) {
        super(DOOM);
    }
    
    @Override
    public boolean Responder(event_t ev) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void Ticker() {
        // TODO Auto-generated method stub

    }

    @Override
    public void Drawer() {
        // TODO Auto-generated method stub

    }

    @Override
    public void Init() {
        // TODO Auto-generated method stub

    }

    @Override
    public void StartControlPanel() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean getShowMessages() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setShowMessages(boolean val) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getScreenBlocks() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setScreenBlocks(int val) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getDetailLevel() {
        // TODO Auto-generated method stub
        return 0;
    }

	@Override
	public void ClearMenus() {
		// TODO Auto-generated method stub
		
	}

}
