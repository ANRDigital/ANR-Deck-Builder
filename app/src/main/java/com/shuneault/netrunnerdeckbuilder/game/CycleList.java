package com.shuneault.netrunnerdeckbuilder.game;

import java.util.ArrayList;

public class CycleList extends ArrayList<Cycle> {

    public Cycle getCycle(String cycleCode) {
        for (Cycle cycle : this)
            if (cycle.getCode().equals(cycleCode))
                return cycle;

        return null;
    }
}
