package org.bleachhack.util.doom.i;

import org.bleachhack.util.doom.doom.ticcmd_t;

public class DummySystem implements IDoomSystem{

    @Override
    public void AllocLow(int length) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void BeginRead() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void EndRead() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void WaitVBL(int count) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public byte[] ZoneBase(int size) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int GetHeapSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void Tactile(int on, int off, int total) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void Quit() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ticcmd_t BaseTiccmd() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void Error(String error, Object... args) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void Error(String error) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void Init() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean GenerateAlert(String title, String cause) {
        // TODO Auto-generated method stub
        return false;
    }

}
