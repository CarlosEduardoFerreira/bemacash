package com.kaching123.tcr.websvc.result.blackstone.pinserve;

import com.kaching123.tcr.model.payment.blackstone.payment.response.DoSettlementResponse;
import com.kaching123.tcr.websvc.handler.blackstone.pinserve.DoSettlementResultHandler;
import com.mayer.framework.web.model.rest.RESTResult;
import com.mayer.framework.web.model.rest.RESTResultHandler;

import org.apache.http.HttpStatus;

/**
 * Created by pkabakov on 23.05.2014.
 */
public class DoSettlementResult extends RESTResult<DoSettlementResponse> {

    public DoSettlementResult() {
        super(HttpStatus.SC_OK, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Override
    protected RESTResultHandler getDefaultHandler() {
        return new DoSettlementResultHandler(this);
    }

}
