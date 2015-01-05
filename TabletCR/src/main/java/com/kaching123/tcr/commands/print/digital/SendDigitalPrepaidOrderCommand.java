package com.kaching123.tcr.commands.print.digital;

import android.content.Context;

import com.kaching123.tcr.model.payment.blackstone.prepaid.IPrePaidInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.SunpassInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessInfo;
import com.kaching123.tcr.print.processor.PrintDigitalSunpassPrepaidProcessor;
import com.kaching123.tcr.print.processor.PrintDigitalWirelessPrepaidProcessor;
import com.kaching123.tcr.print.processor.PrintOrderProcessor;

/**
 * Created by pkabakov on 21.12.13.
 */
public class SendDigitalPrepaidOrderCommand extends SendDigitalOrderCommand {

    protected static final String ARG_PREPAID_INFO = "ARG_PREPAID_INFO";

    @Override
    protected PrintOrderProcessor getPrintDigitalOrderProcessor(String orderGuid, IAppCommandContext appCommandContext) {
        IPrePaidInfo prePaidInfo = (IPrePaidInfo) getArgs().getSerializable(ARG_PREPAID_INFO);

        if (prePaidInfo instanceof WirelessInfo) {
            return new PrintDigitalWirelessPrepaidProcessor(orderGuid, (WirelessInfo) prePaidInfo, appCommandContext);
        } else if (prePaidInfo instanceof SunpassInfo) {
            return new PrintDigitalSunpassPrepaidProcessor(orderGuid, (SunpassInfo) prePaidInfo, appCommandContext);
        } else {
            return super.getPrintDigitalOrderProcessor(orderGuid, appCommandContext);
        }
    }

    public static void start(Context context, String orderGuid, String email, IPrePaidInfo info, BaseSendDigitalOrderCallback callback) {
        create(SendDigitalPrepaidOrderCommand.class)
                .arg(ARG_ORDER_GUID, orderGuid)
                .arg(ARG_EMAIL, email)
                .arg(ARG_PREPAID_INFO, info)
                .callback(callback)
                .queueUsing(context);
    }

}
