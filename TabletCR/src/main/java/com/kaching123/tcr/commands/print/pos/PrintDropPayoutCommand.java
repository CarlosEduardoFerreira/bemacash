package com.kaching123.tcr.commands.print.pos;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.util.IReportsPrinter;
import com.kaching123.tcr.print.printer.PosReportsPrinter;
import com.kaching123.tcr.print.processor.PrintDropPayoutProcessor;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CashDrawerMovementTable;

/**
 * Created by vkompaniets on 25.07.2014.
 */
public class PrintDropPayoutCommand extends BasePrintCommand<IReportsPrinter> {

    private static final Uri URI_MOVEMENT = ShopProvider.getContentWithLimitUri(CashDrawerMovementTable.URI_CONTENT, 1);

    private static final String ARG_MOVEMENT_GUID = "ARG_MOVEMENT_GUID";

    @Override
    protected IReportsPrinter createTextPrinter() {
        return new PosReportsPrinter();
    }

    @Override
    protected void printBody(IReportsPrinter printer) {
        String guid = getStringArg(ARG_MOVEMENT_GUID);
        if (guid == null){
            guid = loadLastPayout();
        }

        PrintDropPayoutProcessor processor = new PrintDropPayoutProcessor(guid);
        processor.print(getContext(), getApp(), printer);
    }

    private String loadLastPayout() {
        Cursor c = ProviderAction.query(URI_MOVEMENT)
                .projection(CashDrawerMovementTable.GUID)
                .where(CashDrawerMovementTable.SHIFT_GUID + " = ?", getAppCommandContext().getShiftGuid())
                .orderBy(CashDrawerMovementTable.MOVEMENT_TIME + " DESC")
                .perform(getContext());
        String result = "";
        if (c.moveToFirst()){
            result = c.getString(0);
        }
        c.close();

        return result;
    }

    public static void start(Context context, String movementGuid, boolean skipPaperWarning, boolean searchByMac, BasePrintCallback callback){
        create(PrintDropPayoutCommand.class)
                .arg(ARG_MOVEMENT_GUID, movementGuid)
                .arg(ARG_SKIP_PAPER_WARNING, skipPaperWarning)
                .arg(ARG_SEARCH_BY_MAC, searchByMac)
                .callback(callback)
                .queueUsing(context);
    }
}
