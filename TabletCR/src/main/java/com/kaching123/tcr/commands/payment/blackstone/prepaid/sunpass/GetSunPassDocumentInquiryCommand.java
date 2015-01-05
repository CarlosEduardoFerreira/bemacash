package com.kaching123.tcr.commands.payment.blackstone.prepaid.sunpass;

import android.content.Context;

import com.kaching123.tcr.commands.payment.SOAPWebCommand;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunPassDocumentInquiryRequest;
import com.kaching123.tcr.websvc.api.prepaid.Broker;
import com.kaching123.tcr.websvc.api.prepaid.DocumentInquiryResponse;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class GetSunPassDocumentInquiryCommand extends SOAPWebCommand<SunPassDocumentInquiryRequest> {

    protected final static String ARG_USER = "user";
    protected final static String ARG_REQUEST = "request";
    public final static String ARG_RESULT = "ARG_RESULT";

    protected DocumentInquiryResponse response;

    public  final static TaskHandler start(Context context, Object callback, SunPassDocumentInquiryRequest request) {
        return create(GetSunPassDocumentInquiryCommand.class)
                      .arg(ARG_REQUEST, request)
                      .callback(callback)
                      .queueUsing(context);
    }

    @Override
    protected TaskResult doInBackground() {
        return super.doInBackground().add(ARG_RESULT, response);
    }

    @Override
    protected Long doCommand(Broker brokerApi, SunPassDocumentInquiryRequest request) {
        response = brokerApi.DoSunPassDocumentInquiry(
                request.mID,
                request.tID,
                request.password,
                request.cashier,
                request.accountNumber,
                request.licensePlateNumber,
                request.transactionMode,
                request.transactionId,
                getSign(request.mID,
                        request.tID,
                        request.password,
                        null,
                        request.transactionId)
        );
        return response != null ? (long)response.responseCode : null;
    }

    @Override
    protected SunPassDocumentInquiryRequest getRequest() {
        return (SunPassDocumentInquiryRequest) getArgs().getSerializable(ARG_REQUEST);
    }
}