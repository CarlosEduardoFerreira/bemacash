package com.kaching123.tcr.store;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.SqlUpdateVersionMatcher;

/**
 * Created by pkabakov on 03.07.2014.
 */
public abstract class BaseOpenHelper extends SQLiteOpenHelper {

    private static Handler handler = new Handler(Looper.getMainLooper());

    private static final String PRAGMA_ENABLE_FOREIGN_KEYS = "PRAGMA foreign_keys = ON;";
    private static final String PRAGMA_DISABLE_FOREIGN_KEYS = "PRAGMA foreign_keys = OFF;";

    protected static String getDbName() {
        return ShopSchema.DB_NAME;
    }

    protected static int getDbVersion() {
        return ShopSchema.DB_VERSION;
    }

    protected final Context mContext;

    public BaseOpenHelper(Context context) {
        super(context, getDbName(), null, getDbVersion());
        mContext = context;
    }

    protected BaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN)
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        if (!isForeignKeysEnabled())
            return;
        if (!db.isReadOnly()) {
            db.execSQL(PRAGMA_ENABLE_FOREIGN_KEYS);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (isWriteAheadLoggingEnabled())
            db.enableWriteAheadLogging();
        if (!isForeignKeysEnabled())
            return;
        if (Build.VERSION.SDK_INT != VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            return;
        }
        if (!db.isReadOnly()) {
            db.execSQL(PRAGMA_ENABLE_FOREIGN_KEYS);
        }
    }

    protected boolean isWriteAheadLoggingEnabled() {
        return false;
    }

    protected boolean isForeignKeysEnabled() {
        return true;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ShopSchemaEx.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (isForeignKeysEnabled()) {
            enableForeignKeys(db, false);
        }

        IUpdateContainer updater = SqlUpdateVersionMatcher.get().getUpdater(oldVersion, newVersion);

        if (updater == null) {
            Logger.e("BaseOpenHelper.onUpgrade(): database is going to be recreated!");
            upgradeDrop(db);
            return;
        }

        Logger.d("BaseOpenHelper.onUpgrade(): database is going to be updated");
        try {
            updater.onUpdate(db);
            if (isForeignKeysEnabled()) {
                enableForeignKeys(db, true);
            }
            if (oldVersion == IUpdateContainer.VERSION5 && newVersion == IUpdateContainer.VERSION5_1) {
                TcrApplication.get().getShopPref().prepaidVersionId().put(null);
            } else if (oldVersion < IUpdateContainer.VERSION5_3 && newVersion >= IUpdateContainer.VERSION5_3) {
                TcrApplication.get().getShopPref().prepaidVersionId().put(null);
            }
            if (oldVersion != IUpdateContainer.VERSION5_4 && newVersion == IUpdateContainer.VERSION5_4) {
                TcrApplication.get().getShopPref().prepaidVersionId().put(null);
            }
            if (oldVersion != IUpdateContainer.VERSION5_5 && newVersion == IUpdateContainer.VERSION5_5) {
                TcrApplication.get().getShopPref().prepaidVersionId().put(null);
            }
            if (oldVersion != IUpdateContainer.VERSION5_6 && newVersion == IUpdateContainer.VERSION5_6) {
                TcrApplication.get().getShopPref().prepaidVersionId().put(null);
            }
            if (oldVersion != IUpdateContainer.VERSION5_7 && newVersion == IUpdateContainer.VERSION5_7) {
                TcrApplication.get().getShopPref().prepaidVersionId().put(null);
            }
            if (newVersion == IUpdateContainer.VERSION5_7) {
                TcrApplication.get().getShopPref().prepaidVersionId().put(null);
            }
            if (newVersion == IUpdateContainer.VERSION5_8) {
                TcrApplication.get().getShopPref().prepaidVersionId().put(null);
            }
            //build 160
            if (newVersion == IUpdateContainer.VERSION5_9) {
                TcrApplication.get().getShopPref().prepaidVersionId().put(null);
            }
            //build 167+
            if (newVersion == IUpdateContainer.VERSION6_1) {
                TcrApplication.get().getShopPref().prepaidVersionId().put(null);
            }
            //build 170+
            if (newVersion == IUpdateContainer.VERSION6_2) {
                TcrApplication.get().getShopPref().prepaidVersionId().put(null);
            }
            Logger.d("BaseOpenHelper.onUpgrade(): database was successfully updated");
        } catch (UnsupportedOperationException e) {
            Logger.e("BaseOpenHelper.onUpgrade(): failed to update database, its going to be recreated!", e);
            upgradeDrop(db);
        } catch (SQLiteConstraintException e) {
            Logger.e("BaseOpenHelper.onUpgrade(): failed, constraints violated", e);
            onUpgradeConstraintError(db);
        }
    }

    private static void enableForeignKeys(SQLiteDatabase db, boolean on){
        String statement = on ? PRAGMA_ENABLE_FOREIGN_KEYS : PRAGMA_DISABLE_FOREIGN_KEYS;
        if (db.inTransaction()){
            db.setTransactionSuccessful();
            db.endTransaction();
            db.execSQL(statement);
            db.beginTransaction();
        }else{
            db.execSQL(statement);
        }
    }

    protected void onUpgradeConstraintError(SQLiteDatabase db) {
        Logger.e("BaseOpenHelper.onUpgradeConstraintError(): constraints violated - clear data, except sync");
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, R.string.database_error_message_constraint_violation, Toast.LENGTH_LONG).show();
            }
        });
        upgradeDropKeepSync(db);
    }

    protected void upgradeDrop(SQLiteDatabase db) {
        drop(db);
        onCreate(db);
        clearDbRelatedPreferences();
    }

    private void upgradeDropKeepSync(SQLiteDatabase db) {
        ShopSchemaEx.onDrop(db, true);
        ShopSchemaEx.onCreate(db, true);
        clearDbRelatedPreferences();
    }

    protected void drop(SQLiteDatabase db) {
        ShopSchemaEx.onDrop(db);
    }

    public synchronized boolean clearDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            drop(db);
            onCreate(db);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Logger.e(this.getClass().getSimpleName() + ".clearTrainingDatabase(): failed", e);
        } finally {
            db.endTransaction();
        }
        return false;
    }

    protected void clearDbRelatedPreferences() {
        TcrApplication.get().clearDbRelatedPreferences();
    }
}
