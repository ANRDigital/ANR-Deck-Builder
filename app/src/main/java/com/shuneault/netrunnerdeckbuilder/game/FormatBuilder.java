package com.shuneault.netrunnerdeckbuilder.game;

import java.util.ArrayList;

public class FormatBuilder {
    private int id;
    private String name;
    private ArrayList<String> packs = new ArrayList<>();
    private boolean rotation;
    private int coreCount = 3;
    private int mwlId;

    public FormatBuilder withId(int id) {
        this.id = id;
        return this;
    }

    public Format Build() {
        Format format = new Format();
        format.setId(id);
        format.setName(name);
        format.setPacks(packs);
        format.setRotation(rotation);
        format.setCoreCount(coreCount);
        format.setMwlId(this.mwlId);
        return format;
    }

    public FormatBuilder asCoreExperience() {
        return this.withId(1)
                .withName("Core Experience")
                .withPack("sc19")
                .withCoreCount(1);
    }

    private FormatBuilder withCoreCount(int count) {
        this.coreCount = count;
        return this;
    }


    public FormatBuilder asStandard() {
        return this.withId(2)
                .withName("Standard")
                .withRotation(true)
                .withMwl(9);
    }

    private FormatBuilder withMwl(int mwl) {
        this.mwlId = mwl;
        return this;
    }

    private FormatBuilder withPack(String code) {
        packs.add(code);
        return this;
    }

    private FormatBuilder withName(String name) {
        this.name = name;
        return this;
    }


    private FormatBuilder withRotation(boolean rotation) {
        this.rotation = rotation;
        return this;
    }
}
