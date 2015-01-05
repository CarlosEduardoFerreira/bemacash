package com.kaching123.tcr.commands.payment.pax;

import com.kaching123.tcr.websvc.api.pax.model.payment.request.MIDownloadRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.MIResponse;

/**
 * Created by pkabakov on 25.06.2014.
 */
public class PaxMIDownloadCommand extends BasePaxMICommand<MIDownloadRequest> {

    @Override
    protected MIDownloadRequest getRequest() {
        return new MIDownloadRequest();
    }

    @Override
    protected MIResponse getResponse(PaxWebApi api, MIDownloadRequest request) {
        return api.midownload(request);
    }
}