package com.shuneault.netrunnerdeckbuilder.db;

import com.shuneault.netrunnerdeckbuilder.game.Pack;

public class PackBuilder {
    private String mCode;
    private String mName;
    private String mCycleCode;

    public PackBuilder withCode(String code) {
        this.mCode = code;
        return this;
    }

    public Pack Build() {
        Pack p = new Pack();
        p.setCode(mCode);
        p.setName(mName);
        p.setCycleCode(mCycleCode);
        return p;
    }

    public PackBuilder withName(String name) {
        this.mName = name;
        return this;
    }

    public PackBuilder withCycle(String code) {
        this.mCycleCode = code;
        return this;
    }
}
