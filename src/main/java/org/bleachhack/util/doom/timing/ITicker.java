package org.bleachhack.util.doom.timing;

import org.bleachhack.util.doom.doom.CVarManager;
import org.bleachhack.util.doom.doom.CommandVariable;
import org.bleachhack.util.doom.doom.SourceCode.I_IBM;
import static org.bleachhack.util.doom.doom.SourceCode.I_IBM.*;

public interface ITicker {

    static ITicker createTicker(CVarManager CVM) {
        if (CVM.bool(CommandVariable.MILLIS)) {
            return new MilliTicker();
        } else if (CVM.bool(CommandVariable.FASTTIC) || CVM.bool(CommandVariable.FASTDEMO)) {
            return new DelegateTicker();
        } else {
            return new NanoTicker();
        }
    }
    
    @I_IBM.C(I_GetTime)
    public int GetTime();
}