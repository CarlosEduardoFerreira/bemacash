package com.kaching123.tcr.commands.rest.sync;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.rest.RestCommand;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class DBVersionResponse extends RestCommand.PlainTextResponse {

    public DBVersionResponse(String status, String message, String entity) {
        super(status, message, entity);
    }

    public Integer getDbVersion() {
        try {
            return entity == null ? null : Integer.parseInt(entity);
        } catch (Exception e) {
            Logger.e("DBVersionResponse: parse error", e);
        }
        return null;
    }

}
