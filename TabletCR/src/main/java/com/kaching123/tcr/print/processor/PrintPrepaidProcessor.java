package com.kaching123.tcr.print.processor;

import android.text.TextUtils;

import com.kaching123.tcr.model.payment.blackstone.prepaid.IPrePaidInfo;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

/**
 * Created by vkompaniets on 20.03.14.
 */
public abstract class PrintPrepaidProcessor<T extends IPrePaidInfo> extends PrintOrderProcessor{

    protected T info;

    public PrintPrepaidProcessor(String orderGuid, T info, IAppCommandContext appCommandContext) {
        super(orderGuid, appCommandContext);
        this.info = info;
    }

    public PrintPrepaidProcessor(String orderGuid, boolean reprint, T info, IAppCommandContext appCommandContext) {
        super(orderGuid, reprint, appCommandContext);
        this.info = info;
    }

    protected static String trimOpt(String value) {
        if (TextUtils.isEmpty(value))
            return value;
        return value.trim();
    }

}
