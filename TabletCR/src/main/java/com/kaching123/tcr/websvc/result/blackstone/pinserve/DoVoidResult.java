package com.kaching123.tcr.websvc.result.blackstone.pinserve;

import com.kaching123.tcr.model.payment.blackstone.payment.response.DoVoidResponse;
import com.kaching123.tcr.websvc.handler.blackstone.pinserve.DoVoidResultHandler;
import com.mayer.framework.web.model.rest.RESTResult;
import com.mayer.framework.web.model.rest.RESTResultHandler;

import org.apache.http.HttpStatus;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to hold blackstone.pinserve parsed results
 */
public class DoVoidResult extends RESTResult<DoVoidResponse> {


    public DoVoidResult() {
        super(HttpStatus.SC_OK, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Override
    protected RESTResultHandler getDefaultHandler() {
        return new DoVoidResultHandler(this);
    }
}
