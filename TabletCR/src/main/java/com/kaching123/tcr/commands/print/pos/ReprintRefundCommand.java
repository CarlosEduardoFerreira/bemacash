package com.kaching123.tcr.commands.print.pos;

import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Function;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand.BasePrintCallback;
import com.kaching123.tcr.processor.MoneybackProcessor.RefundSaleItemInfo;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.util.CalculationUtil;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;

import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by vkompaniets on 15.05.2014.
 */
public class ReprintRefundCommand extends PublicGroundyTask {

    private static final String ARG_ORDER_GUID = "ARG_ORDER_GUID";
    private static final String ARG_SKIP_PAPER_WARNING = "ARG_SKIP_PAPER_WARNING";
    private static final String ARG_SEARCH_BY_MAC = "ARG_SEARCH_BY_MAC";


    @Override
    protected TaskResult doInBackground() {

        final String orderGuid = getStringArg(ARG_ORDER_GUID);
        final boolean skipPaperWarning = getBooleanArg(ARG_SKIP_PAPER_WARNING);
        final boolean searchByMac = getBooleanArg(ARG_SEARCH_BY_MAC);

        Cursor c = ProviderAction.query(ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT))
                .projection(SaleOrderTable.GUID, SaleOrderTable.ORDER_TYPE)
                .where(SaleOrderTable.PARENT_ID + " = ?", orderGuid)
                .perform(getContext());

        while (c.moveToNext()){
            final String childOrderGuid = c.getString(0);

            List<RefundSaleItemInfo> saleItemInfos = ProviderAction.query(ShopProvider.getContentUri(SaleItemTable.URI_CONTENT))
                    .projection(SaleItemTable.PARENT_GUID, SaleItemTable.QUANTITY)
                    .where(SaleItemTable.ORDER_GUID + " = ?", childOrderGuid)
                    .perform(getContext())
                    .toFluentIterable(new Function<Cursor, RefundSaleItemInfo>() {
                        @Override
                        public RefundSaleItemInfo apply(Cursor input) {
                            return new RefundSaleItemInfo(input.getString(0), CalculationUtil.negative(_decimal(input, 1)));
                        }
                    }).toImmutableList();

            TaskResult result = PrintRefundCommand.sync(getContext(), skipPaperWarning, searchByMac, true, orderGuid, childOrderGuid, saleItemInfos, getAppCommandContext());
            if (isFailed(result)){
                return result;
            }
        }
        c.close();

        return succeeded();
    }

    public static void start(Context context, boolean skipPaperWarning, boolean searchByMac, String orderGuid, BasePrintCallback printCallback){
        create(ReprintRefundCommand.class).arg(ARG_SKIP_PAPER_WARNING, skipPaperWarning).arg(ARG_SEARCH_BY_MAC, searchByMac).arg(ARG_ORDER_GUID, orderGuid).callback(printCallback).queueUsing(context);
    }

}
