package com.kaching123.tcr.commands.print.pos;

import android.content.Context;

import com.kaching123.tcr.print.processor.PrintOrderProcessor;

/**
 * Created by pkabakov on 14.02.14.
 */
public class ReprintOrderCommand extends PrintOrderCommand {

    @Override
    protected PrintOrderProcessor getPrintOrderProcessor(String orderGuid, IAppCommandContext appCommandContext) {
        return new PrintOrderProcessor(orderGuid, true, appCommandContext);
    }

    public static void start(Context context, boolean skipPaperWarning, boolean searchByMac, String orderGuid, BasePrintCallback callback) {
        create(ReprintOrderCommand.class).arg(ARG_SKIP_PAPER_WARNING, skipPaperWarning).arg(ARG_SEARCH_BY_MAC, searchByMac).arg(ARG_ORDER_GUID, orderGuid).callback(callback).queueUsing(context);
    }

}
