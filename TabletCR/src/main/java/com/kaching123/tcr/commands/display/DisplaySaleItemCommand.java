package com.kaching123.tcr.commands.display;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.display.printers.DisplayPrinterWrapper;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.converter.SaleOrderItemViewModelWrapFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderItemsView;
import com.kaching123.tcr.util.CalculationUtil;

import java.util.List;

import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * Created by pkabakov on 25.02.14.
 */
public class DisplaySaleItemCommand extends BaseDisplayCommand<DisplayPrinterWrapper> {

    private static final Uri URI_SALE_ITEMS = ShopProvider.getContentUri(SaleOrderItemsView.URI_CONTENT);

    private final String saleItemGuid;

    public DisplaySaleItemCommand(String saleItemGuid) {
        this.saleItemGuid = saleItemGuid;
    }

    @Override
    protected DisplayPrinterWrapper getPrinterWrapper() {
        return new DisplayPrinterWrapper();
    }

    @Override
    protected void printBody(Context context, DisplayPrinterWrapper printerWrapper) {

        Logger.d("trace--DisplaySaleItemCommand: 0");
        SaleOrderItemViewModel saleItem = getSaleItem(context, saleItemGuid);
        Logger.d("trace--DisplaySaleItemCommand: 1");
        printerWrapper.add(saleItem.itemModel.qty, saleItem.description, CalculationUtil.getSubTotal(saleItem.itemModel.qty, saleItem.fullPrice, saleItem.itemModel.discount, saleItem.itemModel.discountType));
        Logger.d("trace--DisplaySaleItemCommand: 2");
    }

    private SaleOrderItemViewModel getSaleItem(Context context, String saleItemGuid) {

        Cursor cursor = ProviderAction.query(URI_SALE_ITEMS)
                .where(SaleItemTable.SALE_ITEM_GUID + " = ?", saleItemGuid)
                .perform(context);
        List<SaleOrderItemViewModel> saleItemsList = _wrap(cursor, new SaleOrderItemViewModelWrapFunction(context, false));
        return saleItemsList == null || saleItemsList.isEmpty() ? null : saleItemsList.get(0);
    }

}
