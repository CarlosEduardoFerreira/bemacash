package com.kaching123.tcr.commands.rest.sync;

import com.kaching123.tcr.APKupdaterJDBCConverter;
import com.kaching123.tcr.APKupdaterJDBCConverter.APKUpdater;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.rest.RestCommand;
import com.kaching123.tcr.util.JdbcJSONObject;

/**
 * Created by teli on 6/2/2015.
 */
public class GetAPKUpdates extends RestCommand.JsonResponse {
    public static final String EXTRA_APK_UPDATE = "apk_update";

    public GetAPKUpdates(String status, String message, JdbcJSONObject entity) {
        super(status, message, entity);
    }


    public APKUpdater getResponse() {
        try {
            return
                    APKupdaterJDBCConverter.read(entity);

        } catch (Exception e) {
            Logger.e("GetAPKUpdates error", e);
        }
        return null;
    }

}
