package com.kaching123.tcr.processor;

import com.kaching123.tcr.model.Unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dot on 20.07.2014.
 */
public class UnitItemCache {

    private Map<String, List<Unit>> scannedUnitsReady = new HashMap<String, List<Unit>>();

    public static UnitItemCache get() {
        return Holder.instance;
    }

    public Map<String, List<Unit>> getUnits() {
        return scannedUnitsReady;
    }

    public ArrayList<Unit> getUnitsList() {
        ArrayList<Unit> unitsList = new ArrayList<Unit>();
        for (List<Unit> units : scannedUnitsReady.values()) {
            for (Unit unit : units) {
                unitsList.add(unit);
            }
        }
        return unitsList;
    }

    public void add(String key, List<Unit> units) {
        scannedUnitsReady.put(key, units);
    }

    public void reset() {
        scannedUnitsReady.clear();
    }

    private UnitItemCache() {}

    private static final class Holder {
        private static final UnitItemCache instance = new UnitItemCache();
    }
}
