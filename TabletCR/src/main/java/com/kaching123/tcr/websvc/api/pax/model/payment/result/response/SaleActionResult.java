package com.kaching123.tcr.websvc.api.pax.model.payment.result.response;

import com.kaching123.tcr.websvc.api.pax.model.payment.result.handler.SaleActionResultHandler;
import com.mayer.framework.web.model.rest.RESTResult;
import com.mayer.framework.web.model.rest.RESTResultHandler;

import org.apache.http.HttpStatus;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class SaleActionResult extends RESTResult<SaleActionResponse> {



    public SaleActionResult() {
        super(HttpStatus.SC_OK, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Override
    protected RESTResultHandler getDefaultHandler() {
        return new SaleActionResultHandler(this);
    }

}