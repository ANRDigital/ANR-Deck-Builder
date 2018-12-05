package com.shuneault.netrunnerdeckbuilder.db;

import com.shuneault.netrunnerdeckbuilder.game.Cycle;

class CycleBuilder {
    private String code;
    private boolean rotation;

    public CycleBuilder withCode(String code) {
        this.code = code;
        return this;
    }

    public CycleBuilder withRotation(boolean value) {
        this.rotation = value;
        return this;
    }

    public Cycle Build() {
        Cycle cycle = new Cycle();
        cycle.setCode(code);
        cycle.setRotation(rotation);
        return cycle;
    }
}
