package com.kaching123.tcr.commands.payment;

import android.content.ContentProviderOperation;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.service.ISqlCommand;
import com.mayer.framework.web.model.rest.RESTResult;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to be parent to all web commands
 *         // TODO proly there's a way to merge with AsynCommand.class, I see no profit doing so ATM tho
 */
public abstract class RESTWebCommandBase<TypeResponse, TypeResult extends RESTResult<TypeResponse>> extends WebCommand {

    protected abstract TypeResult getEmptyResult();

    protected TypeResult result;

    @Override
    protected boolean performAction() {
        result = getEmptyResult();
        try {
            return doCommand(result);
        } catch (IOException e) {
            handleIOException(result, e);
            return false;
        }
    }

    @Override
    protected boolean validateAppCommandContext() {
        return false;
    }
    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return null;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return null;
    }

    protected abstract boolean doCommand(TypeResult result) throws IOException;

    /**
     * Logs the error, and populates the error fields of the result accordingly
     */
    protected final void handleIOException(TypeResult result, IOException ex) {
        Logger.e("IOException when parsing http response", ex);
        result.setResultCode(RESTResult.ERROR_IO_EXCEPTION);
        result.setErrorReason(ex.getMessage());
    }
}
