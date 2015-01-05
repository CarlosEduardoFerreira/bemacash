package com.kaching123.tcr.commands.payment.pax;

import com.kaching123.tcr.websvc.api.pax.model.payment.request.MIRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.MIResponse;

/**
 * Created by mayer
 */
public class PaxMICommand extends BasePaxMICommand<MIRequest> {

    @Override
    protected boolean shouldStoreData() {
        return false;
    }

    @Override
    protected MIRequest getRequest() {
        return new MIRequest();
    }

    @Override
    protected MIResponse getResponse(PaxWebApi api, MIRequest request) {
        return api.mi(request);
    }
}
