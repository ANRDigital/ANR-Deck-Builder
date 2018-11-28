package com.shuneault.netrunnerdeckbuilder.db;

import com.shuneault.netrunnerdeckbuilder.game.Pack;

public class PackBuilder {
    private String mCode;
    private String mName;

    public PackBuilder withCode(String code) {
        this.mCode = code;
        return this;
    }

    public Pack Build() {
        Pack p = new Pack();
        p.setCode(mCode);
        p.setName(mName);
        return p;
    }

    public PackBuilder withName(String name) {
        this.mName = name;
        return this;
    }
}
