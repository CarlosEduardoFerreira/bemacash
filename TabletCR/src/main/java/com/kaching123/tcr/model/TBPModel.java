package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopSchema2.TBPRegisterView2.TbpTable;
import com.kaching123.tcr.store.ShopStore.TBPTable;

import java.io.Serializable;

import static com.kaching123.tcr.util.ContentValuesUtilBase._bool;

/**
 * Created by vkompaniets on 12.08.2016.
 */
public class TBPModel implements IValueModel, Serializable {

    public String id;
    public String description;
    public int priceLevel;
    public boolean isActive;
    public String monStart;
    public String monEnd;
    public String tueStart;
    public String tueEnd;
    public String wedStart;
    public String wedEnd;
    public String thuStart;
    public String thuEnd;
    public String friStart;
    public String friEnd;
    public String satStart;
    public String satEnd;
    public String sunStart;
    public String sunEnd;

    public TBPModel(String id, String description, int priceLevel, boolean isActive, String monStart, String monEnd, String tueStart, String tueEnd, String wedStart, String wedEnd, String thuStart, String thuEnd, String friStart, String friEnd, String satStart, String satEnd, String sunStart, String sunEnd) {
        this.id = id;
        this.description = description;
        this.priceLevel = priceLevel;
        this.isActive = isActive;
        this.monStart = monStart;
        this.monEnd = monEnd;
        this.tueStart = tueStart;
        this.tueEnd = tueEnd;
        this.wedStart = wedStart;
        this.wedEnd = wedEnd;
        this.thuStart = thuStart;
        this.thuEnd = thuEnd;
        this.friStart = friStart;
        this.friEnd = friEnd;
        this.satStart = satStart;
        this.satEnd = satEnd;
        this.sunStart = sunStart;
        this.sunEnd = sunEnd;
    }

    //ShopStore.TBPRegisterView
    public static TBPModel fromView(Cursor c){
        return new TBPModel(
                c.getString(c.getColumnIndex(TbpTable.ID)),
                c.getString(c.getColumnIndex(TbpTable.DESCRIPTION)),
                c.getInt(c.getColumnIndex(TbpTable.PRICE_LEVEL)),
                _bool(c, c.getColumnIndex(TbpTable.IS_ACTIVE)),
                c.getString(c.getColumnIndex(TbpTable.MON_START)),
                c.getString(c.getColumnIndex(TbpTable.MON_END)),
                c.getString(c.getColumnIndex(TbpTable.TUE_START)),
                c.getString(c.getColumnIndex(TbpTable.TUE_END)),
                c.getString(c.getColumnIndex(TbpTable.WED_START)),
                c.getString(c.getColumnIndex(TbpTable.WED_END)),
                c.getString(c.getColumnIndex(TbpTable.THU_START)),
                c.getString(c.getColumnIndex(TbpTable.THU_END)),
                c.getString(c.getColumnIndex(TbpTable.FRI_START)),
                c.getString(c.getColumnIndex(TbpTable.FRI_END)),
                c.getString(c.getColumnIndex(TbpTable.SAT_START)),
                c.getString(c.getColumnIndex(TbpTable.SAT_END)),
                c.getString(c.getColumnIndex(TbpTable.SUN_START)),
                c.getString(c.getColumnIndex(TbpTable.SUN_END))
        );
    }

    @Override
    public String getGuid() {
        return String.valueOf(id);
    }

    @Override
    public ContentValues toValues() {
        ContentValues cv = new ContentValues();
        cv.put(TBPTable.ID, id);
        cv.put(TBPTable.DESCRIPTION, description);
        cv.put(TBPTable.PRICE_LEVEL, priceLevel);
        cv.put(TBPTable.IS_ACTIVE, isActive);
        cv.put(TBPTable.MON_START, monStart);
        cv.put(TBPTable.MON_END, monEnd);
        cv.put(TBPTable.TUE_START, tueStart);
        cv.put(TBPTable.TUE_END, tueEnd);
        cv.put(TBPTable.WED_START, wedStart);
        cv.put(TBPTable.WED_END, wedEnd);
        cv.put(TBPTable.THU_START, thuStart);
        cv.put(TBPTable.THU_END, thuEnd);
        cv.put(TBPTable.FRI_START, friStart);
        cv.put(TBPTable.FRI_END, friEnd);
        cv.put(TBPTable.SAT_START, satStart);
        cv.put(TBPTable.SAT_END, satEnd);
        cv.put(TBPTable.SUN_START, sunStart);
        cv.put(TBPTable.SUN_END, sunEnd);
        return cv;
    }

    @Override
    public String getIdColumn() {
        return null;
    }

    public boolean isOverlapping(TBPModel other){
        if (!isActive || !other.isActive)
            return false;

        if (isOverlapping(monStart, monEnd, other.monStart, other.monEnd))
            return true;
        if (isOverlapping(tueStart, tueEnd, other.tueStart, other.tueEnd))
            return true;
        if (isOverlapping(wedStart, wedEnd, other.wedStart, other.wedEnd))
            return true;
        if (isOverlapping(thuStart, thuEnd, other.thuStart, other.thuEnd))
            return true;
        if (isOverlapping(friStart, friEnd, other.friStart, other.friEnd))
            return true;
        if (isOverlapping(satStart, satEnd, other.satStart, other.satEnd))
            return true;
        if (isOverlapping(sunStart, sunEnd, other.sunEnd, other.sunEnd))
            return true;

        return false;
    }

    private static <T extends Comparable<T>> boolean isOverlapping(T start1, T end1, T start2, T end2){
        if (start1 == null || end1 == null || start2 == null || end2 == null)
            return false;

        return start1.compareTo(end2) < 0 && start2.compareTo(end1) < 0;
    }
}
