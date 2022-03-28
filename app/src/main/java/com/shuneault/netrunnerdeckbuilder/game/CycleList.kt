package com.shuneault.netrunnerdeckbuilder.game
import java.util.ArrayList

class CycleList : ArrayList<Cycle>() {
    fun getCycle(cycleCode: String): Cycle? {
        for (cycle in this) if (cycle.code == cycleCode) return cycle
        return null
    }
}