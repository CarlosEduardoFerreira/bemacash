package com.kaching123.tcr.websvc.result.blackstone.pinserve;

import com.kaching123.tcr.model.payment.blackstone.payment.response.AutomaticBatchCloseResponse;
import com.kaching123.tcr.websvc.handler.blackstone.pinserve.AutomaticBatchCloseResultHandler;
import com.mayer.framework.web.model.rest.RESTResult;
import com.mayer.framework.web.model.rest.RESTResultHandler;

import org.apache.http.HttpStatus;

/**
 * Created by pkabakov on 11.06.2014.
 */
public class AutomaticBatchCloseResult extends RESTResult<AutomaticBatchCloseResponse> {

    public AutomaticBatchCloseResult() {
        super(HttpStatus.SC_OK, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Override
    protected RESTResultHandler getDefaultHandler() {
        return new AutomaticBatchCloseResultHandler(this);
    }

}
