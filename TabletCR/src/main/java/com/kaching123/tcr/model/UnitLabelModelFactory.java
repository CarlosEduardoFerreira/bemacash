package com.kaching123.tcr.model;

import java.util.UUID;

/**
 * Created by alboyko 07.12.2015
 */
public class UnitLabelModelFactory {

    public static final UnitLabelModel getNewModel(String description, String shortCut) {
        return new UnitLabelModel(UUID.randomUUID().toString(), description, shortCut, null);
    }

    public static final UnitLabelModel getSimpleModel(String shortcut) {
        return new UnitLabelModel(null, null, shortcut, null);
    }
}