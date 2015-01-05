package com.kaching123.tcr.commands.print.pos;

import android.content.Context;

import com.kaching123.tcr.model.payment.blackstone.prepaid.IPrePaidInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.SunpassInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessInfo;
import com.kaching123.tcr.print.processor.PrintOrderProcessor;
import com.kaching123.tcr.print.processor.PrintSunpassPrepaidProcessor;
import com.kaching123.tcr.print.processor.PrintWirelessPrepaidProcessor;

/**
 * Created by gdubina on 03.12.13.
 */
public class PrintPrepaidOrderCommand extends PrintOrderCommand {

    protected static final String ARG_PREPAID_INFO = "ARG_PREPAID_INFO";
    protected static final String ARG_REPRINT = "ARG_REPRINT";

    @Override
    protected PrintOrderProcessor getPrintOrderProcessor(String orderGuid, IAppCommandContext appCommandContext) {
        IPrePaidInfo prePaidInfo = (IPrePaidInfo) getArgs().getSerializable(ARG_PREPAID_INFO);
        boolean reprint = getBooleanArg(ARG_REPRINT, false);

        if (prePaidInfo instanceof WirelessInfo) {
            return new PrintWirelessPrepaidProcessor(orderGuid, reprint, (WirelessInfo) prePaidInfo, appCommandContext);
        } else if (prePaidInfo instanceof SunpassInfo) {
            return new PrintSunpassPrepaidProcessor(orderGuid, reprint, (SunpassInfo) prePaidInfo, appCommandContext);
        } else {
            return new PrintOrderProcessor(orderGuid, reprint, appCommandContext);
        }
    }

    public static void start(Context context,
                             boolean reprint,
                             boolean skipPaperWarning,
                             boolean searchByMac,
                             String orderGuid,
                             IPrePaidInfo info,
                             BasePrintCallback callback) {
        create(PrintPrepaidOrderCommand.class)
                .arg(ARG_SKIP_PAPER_WARNING, skipPaperWarning)
                .arg(ARG_SEARCH_BY_MAC, searchByMac)
                .arg(ARG_PREPAID_INFO, info)
                .arg(ARG_ORDER_GUID, orderGuid)
                .arg(ARG_REPRINT, reprint)
                .callback(callback)
                .queueUsing(context);
    }

}
