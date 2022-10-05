package org.bleachhack.util.doom.p;

import org.bleachhack.util.doom.doom.SourceCode.P_Tick;
import static org.bleachhack.util.doom.doom.SourceCode.P_Tick.*;
import org.bleachhack.util.doom.doom.thinker_t;

public interface ThinkerList {

    @P_Tick.C(P_AddThinker)
    void AddThinker(thinker_t thinker);
    @P_Tick.C(P_RemoveThinker)
    void RemoveThinker(thinker_t thinker);
    @P_Tick.C(P_InitThinkers)
    void InitThinkers();
    
    thinker_t getRandomThinker();
    thinker_t getThinkerCap();
}
