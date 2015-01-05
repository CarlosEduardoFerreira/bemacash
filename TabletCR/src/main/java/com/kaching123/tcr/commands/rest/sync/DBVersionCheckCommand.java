package com.kaching123.tcr.commands.rest.sync;

import android.content.Context;
import android.text.TextUtils;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.support.WebAPICommand;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.store.ShopSchema;
import com.telly.groundy.TaskResult;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class DBVersionCheckCommand extends WebAPICommand {

    private String login;
    private String password;

    private Integer dbVersion;

    public enum DBVersionCheckError {
        NO_VERSION, INVALID_VERSION
    }

    @Override
    protected TaskResult doCommand(SyncApi api) {
        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(password)) {
            EmployeeModel operator = getApp().getOperator();
            if (operator != null) {
                login = operator.login;
                password = operator.password;
            }
        }

        DBVersionResponse response = null;
        try {
            response = api.getDBVersion(getApp().emailApiKey, SyncUploadRequestBuilder.getReqCredentials(login, password, getApp().getRegisterSerial(), getContext()));
        } catch (Exception e) {
            Logger.e("DBVersionCheckCommand: failed", e);
        }

        if (response != null && response.isSuccess())
            dbVersion = response.getDbVersion();
        boolean success = dbVersion != null;

        if (!success) {
            return failed();
        }

        getApp().getShopPref().syncForbidden().put(!isVersionValid(dbVersion));
        return succeeded();
    }

    private static boolean isVersionValid(int dbVersion) {
        return dbVersion == ShopSchema.DB_VERSION;
    }

    public DBVersionCheckError sync(Context context) {
        return sync(context, (String)null, (String)null);
    }

    public DBVersionCheckError sync(Context context, String login, String password) {
        this.login = login;
        this.password = password;

        //no need to access command cache (creds on command start)
        super.sync(context, null, null);

        if (dbVersion == null) {
            return DBVersionCheckError.NO_VERSION;
        }
        if (!isVersionValid(dbVersion)) {
            return DBVersionCheckError.INVALID_VERSION;
        }
        return null;
    }

}
