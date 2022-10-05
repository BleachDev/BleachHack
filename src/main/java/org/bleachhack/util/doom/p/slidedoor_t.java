package org.bleachhack.util.doom.p;

import static org.bleachhack.util.doom.p.ActiveStates.T_SlidingDoor;
import org.bleachhack.util.doom.rr.SectorAction;
import org.bleachhack.util.doom.rr.line_t;
import org.bleachhack.util.doom.rr.sector_t;

public class slidedoor_t extends SectorAction {
    public sdt_e type;
    public line_t line;
    public int frame;
    public int whichDoorIndex;
    public int timer;
    public sector_t frontsector;
    public sector_t backsector;
    public sd_e status;

    public slidedoor_t() {
        type = sdt_e.sdt_closeOnly;
        status = sd_e.sd_closing;
        thinkerFunction = T_SlidingDoor;
    }
}
