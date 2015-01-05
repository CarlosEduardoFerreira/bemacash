package com.kaching123.tcr.websvc.api.pax.model.payment.result.response;

import com.kaching123.tcr.websvc.api.pax.model.payment.result.handler.HelloResultHandler;
import com.mayer.framework.web.model.rest.RESTResult;
import com.mayer.framework.web.model.rest.RESTResultHandler;

import org.apache.http.HttpStatus;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class HelloResult extends RESTResult<HelloResponse> {



    public HelloResult() {
        super(HttpStatus.SC_OK, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Override
    protected RESTResultHandler getDefaultHandler() {
        return new HelloResultHandler(this);
    }

}