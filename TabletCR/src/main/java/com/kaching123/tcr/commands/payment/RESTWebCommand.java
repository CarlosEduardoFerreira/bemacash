package com.kaching123.tcr.commands.payment;

import com.kaching123.tcr.model.payment.blackstone.payment.ResponseBase;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.mayer.framework.web.model.rest.RESTResult;
import com.telly.groundy.TaskResult;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to be parent to all web commands
 */
public abstract class RESTWebCommand<TypeResponse extends ResponseBase, TypeResult extends RESTResult<TypeResponse>>
        extends RESTWebCommandBase<TypeResponse, TypeResult> {

    @Override
    protected TaskResult afterAction() {
        if (result.getData() != null) {
            if (TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY.equals(result.getData().getResponseCode())) {
                return succeeded().add(RESULT_DATA, result.getData());
            } else {
                return failed().add(RESULT_DATA, result.getData());
            }
        } else {
            return failed().add(RESULT_REASON, ErrorReason.DUE_TO_MALFUNCTION);
        }
    }

    protected boolean isResultSuccessful() {
        return result != null && result.isValid() && result.getData() != null && TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY.equals(result.getData().getResponseCode());
    }
}
