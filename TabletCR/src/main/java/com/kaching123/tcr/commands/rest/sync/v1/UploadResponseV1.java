package com.kaching123.tcr.commands.rest.sync.v1;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.rest.RestCommand.JsonResponse;
import com.kaching123.tcr.util.JdbcJSONObject;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gdubina on 19/03/14.
 */
public class UploadResponseV1 extends JsonResponse {

    private static final int SKIPPED = 0;

    private static final int FAILED = -1;

    public UploadResponseV1(String status, String message, JdbcJSONObject entity) {
        super(status, message, entity);
    }

    public long optFailedId(long fallback) {
        return entity == null ? fallback : entity.optLong("error_id", fallback);
    }

    public List<Long> getFailedTransactions(ArrayList<Long> currentTransactionsIds) {
        if (entity != null) {
            final List<Long> failedTransactionIds = new ArrayList<>();
            for (Long transactionId : currentTransactionsIds) {
                try {
                    JSONArray value = entity.getJSONArray(String.valueOf(transactionId));
                    for (int i = 0; i < value.length(); i++) {
                        int code;
                        try {
                            code = value.getInt(i);
                        } catch (JSONException e) {
                            return currentTransactionsIds;
                        }
                        Logger.d(String.format("[UploadValidation] Transaction %d\'s  %d child has been marked as %s",
                                transactionId, i, value.get(i)));
                        if (code > 0) {
                            Logger.d("[UploadValidation] Transaction has successfully updated %d rows", code);
                        } else if (code == SKIPPED) {
                            Logger.d("[UploadValidation] Local db row = server db row, nothing to update");
                        } else if (code == FAILED) {
                            failedTransactionIds.add(transactionId);
                            Logger.d("[UploadValidation] Transaction was not updated. It will be kept sending");
                            break;
                        } else {
                            failedTransactionIds.add(transactionId);
                            Logger.d("[UploadValidation] Unexpected behavior");
                            break;
                        }
                    }
                } catch (JSONException e) {
                    failedTransactionIds.add(transactionId);
                    Logger.e("[UploadValidation] Server has not included this token to the answer. " +
                            "Transaction will be sent again");
                }
            }
            return failedTransactionIds;
        } else {
            Logger.e("[UploadValidation] Server didn't include \"data\" object to the answer. " +
                    "All transactions are considered as failed and will be sent again.");
            return currentTransactionsIds;
        }
    }
}
