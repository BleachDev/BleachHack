package org.bleachhack.util.doom.savegame;

import org.bleachhack.util.doom.p.ThinkerList;

public interface ILoadSaveGame {
    void setThinkerList(ThinkerList li);
    void doSave(ThinkerList li);
    void doLoad(ThinkerList li);
}
