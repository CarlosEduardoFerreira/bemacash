package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.TBPTable;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by vkompaniets on 12.08.2016.
 */
public class TBPModel implements IValueModel, Serializable {

    public String id;
    public String description;
    public int priceLevel;
    public boolean isActive;
    public Date startDate;
    public Date endDate;
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

    public TBPModel(String id, String description, int priceLevel, boolean isActive, Date startDate, Date endDate, String monStart, String monEnd, String tueStart, String tueEnd, String wedStart, String wedEnd, String thuStart, String thuEnd, String friStart, String friEnd, String satStart, String satEnd, String sunStart, String sunEnd) {
        this.id = id;
        this.description = description;
        this.priceLevel = priceLevel;
        this.isActive = isActive;
        this.startDate = startDate;
        this.endDate = endDate;
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
        cv.put(TBPTable.START_DATE, startDate == null ? null : startDate.getTime());
        cv.put(TBPTable.END_DATE, endDate == null ? null : endDate.getTime());
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
}
