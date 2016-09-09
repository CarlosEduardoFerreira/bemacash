package com.kaching123.tcr.store;

import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.kaching123.tcr.store.ShopStore.MaxUpdateTableTimeParentRelationsQuery;
import com.kaching123.tcr.store.ShopStore.MaxUpdateTableTimeQuery;

import java.util.Locale;

/**
 * Created by pkabakov on 25.09.2014.
 */
public class ProviderQueryHelper {

    private final static int MATCH_MAX_UPDATE_TABLE_TIME_QUERY_URI_CONTENT = 0x5555;
    private final static int MATCH_MAX_UPDATE_TABLE_PARENT_TIME_QUERY_URI_CONTENT = 0x5556;

    private final UriMatcher matcher;
    private final SQLiteOpenHelper dbHelper;

    public ProviderQueryHelper(String authority, SQLiteOpenHelper dbHelper) {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(authority, MaxUpdateTableTimeQuery.URI_CONTENT, MATCH_MAX_UPDATE_TABLE_TIME_QUERY_URI_CONTENT);
        matcher.addURI(authority, MaxUpdateTableTimeParentRelationsQuery.URI_CONTENT, MATCH_MAX_UPDATE_TABLE_PARENT_TIME_QUERY_URI_CONTENT);
        this.dbHelper = dbHelper;
    }

    Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int match = matcher.match(uri);
        if (match == MATCH_MAX_UPDATE_TABLE_TIME_QUERY_URI_CONTENT) {
            return getMaxUpdateTime(dbHelper, selectionArgs);
        }
        if (match == MATCH_MAX_UPDATE_TABLE_PARENT_TIME_QUERY_URI_CONTENT) {
            return getMaxUpdateParentTime(dbHelper, selectionArgs);
        }
        return null;
    }

    static Cursor getMaxUpdateTime(final SQLiteOpenHelper dbHelper, String[] selectionArgs) {
        return dbHelper.getReadableDatabase().rawQuery(String.format(Locale.US, MaxUpdateTableTimeQuery.QUERY, selectionArgs), null);
    }

    static Cursor getMaxUpdateParentTime(SQLiteOpenHelper dbHelper, String[] selectionArgs) {
        return dbHelper.getReadableDatabase().rawQuery(String.format(Locale.US, MaxUpdateTableTimeParentRelationsQuery.QUERY, selectionArgs), null);
    }

}
