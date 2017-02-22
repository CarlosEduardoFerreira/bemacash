package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.commands.AsyncTaskCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.TableHistoryTable;

import static com.kaching123.tcr.util.ContentValuesUtilBase._nullableDate;


/**
 * Created by Rodrigo Busata on 10/06/16.
 */
public class TableHistoryModel implements IValueModel, Serializable {

    public String guid;
    public String tableGuid;
    public Date startTime;
    public Date endTime;

    private List<String> mIgnoreFields;

    public TableHistoryModel(Cursor c) {
        this.guid = c.getString(c.getColumnIndex(TableHistoryTable.GUID));
        this.tableGuid = c.getString(c.getColumnIndex(ShopStore.TableHistoryTable.TABLE_GUID));
        this.startTime = _nullableDate(c, c.getColumnIndex(TableHistoryTable.STATUS_START_TIME));
        this.endTime = _nullableDate(c, c.getColumnIndex(TableHistoryTable.STATUS_END_TIME));

    }

    public TableHistoryModel(String guid,
                             String tableGuid,
                             Date startTime,
                             Date endTime,
                             List<String> ignoreFields) {
        this.guid = guid;
        this.tableGuid = tableGuid;
        this.startTime = startTime;
        this.endTime = endTime;

        this.mIgnoreFields = ignoreFields;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        if (mIgnoreFields == null || !mIgnoreFields.contains(TableHistoryTable.GUID)) v.put(TableHistoryTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(TableHistoryTable.TABLE_GUID)) v.put(ShopStore.TableHistoryTable.TABLE_GUID, tableGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(TableHistoryTable.STATUS_START_TIME)) _nullableDate(v, ShopStore.TableHistoryTable.STATUS_START_TIME, startTime);
        if (mIgnoreFields == null || !mIgnoreFields.contains(TableHistoryTable.STATUS_END_TIME)) _nullableDate(v, TableHistoryTable.STATUS_END_TIME, endTime);

        return v;
    }

    @Override
    public String getIdColumn() {
        return TableHistoryTable.GUID;
    }

    public static void keepHistory(SuperBaseActivity context, TableOrderModel tableOrderModel, Date endTime){
        if (tableOrderModel == null) return;

        new TableHistoryModel.TableHistoryTask(context, true, null).execute(
                new TableHistoryModel(UUID.randomUUID().toString(),
                        tableOrderModel.guid, tableOrderModel.statusTime, endTime, null));
    }

    static public class TableHistoryTask extends AsyncTaskCommand<TableHistoryModel> {

        public TableHistoryTask(SuperBaseActivity context, boolean isInsert, CallBackTask callBackTask) {
            super(context, ShopProvider.contentUri(ShopStore.TableHistoryTable.URI_CONTENT), isInsert, callBackTask);
        }
    }
}
