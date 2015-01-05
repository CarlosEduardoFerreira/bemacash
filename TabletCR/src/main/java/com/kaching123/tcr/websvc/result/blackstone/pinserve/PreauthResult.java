package com.kaching123.tcr.websvc.result.blackstone.pinserve;

import com.kaching123.tcr.model.payment.blackstone.payment.response.PreauthResponse;
import com.kaching123.tcr.websvc.handler.blackstone.pinserve.PreauthResultHandler;
import com.mayer.framework.web.model.rest.RESTResult;
import com.mayer.framework.web.model.rest.RESTResultHandler;

import org.apache.http.HttpStatus;

public class PreauthResult extends RESTResult<PreauthResponse> {



    public PreauthResult() {
        super(HttpStatus.SC_OK, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Override
    protected RESTResultHandler getDefaultHandler() {
        return new PreauthResultHandler(this);
    }

}
