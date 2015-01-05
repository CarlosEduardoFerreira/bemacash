package com.kaching123.tcr.print.processor;

import android.content.Context;

import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.SunpassInfo;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

/**
 * Created by vkompaniets on 20.03.14.
 */
public class PrintSunpassPrepaidProcessor extends PrintPrepaidProcessor<SunpassInfo> {

    public PrintSunpassPrepaidProcessor(String orderGuid, SunpassInfo info, IAppCommandContext appCommandContext) {
        super(orderGuid, info, appCommandContext);
    }

    public PrintSunpassPrepaidProcessor(String orderGuid, boolean reprint, SunpassInfo info, IAppCommandContext appCommandContext) {
        super(orderGuid, reprint, info, appCommandContext);
    }

    @Override
    protected void printBody(Context context, TcrApplication app, ITextPrinter printerWrapper) {
        super.printBody(context, app, printerWrapper);

        printerWrapper.drawLine();

        if (info.isFieldsEmty())
            return;

        printerWrapper.addWithTab(context.getString(R.string.sunpass_prepaid_print_processor_transponder_id), trimOpt(info.transponderId), true, false);
        printerWrapper.addWithTab(context.getString(R.string.sunpass_prepaid_print_processor_purchase_id), trimOpt(info.purchaseId), true, false);
        printerWrapper.addWithTab(context.getString(R.string.sunpass_prepaid_print_processor_reference_id), String.valueOf(info.referenceId), true, false);

        printerWrapper.drawLine();
    }
}
