package com.kaching123.tcr.print.processor;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.util.IKitchenPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.device.PrinterInfo;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.ItemInfo;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderView;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.util.DateUtils.dateAndTimeShortFormat;

/**
 * Created by vkompaniets on 17.02.14.
 */
public class PrintItemsForKitchenProcessor {

    private List<ItemInfo> items;
    private PrinterInfo printer;
    private String aliasGuid;
    private String orderGuid;
    private String orderTitle;
    private String orderNumber;
    private boolean isUpdated;
    private boolean printAllItems;

    private final IAppCommandContext appCommandContext;

    public PrintItemsForKitchenProcessor(List<ItemInfo> items, PrinterInfo printer, String aliasGuid, String orderGuid, boolean isUpdated, String orderTitle, boolean printAllItems, IAppCommandContext appCommandContext) {
        this.items = items;
        this.printer = printer;
        this.aliasGuid = aliasGuid;
        this.orderGuid = orderGuid;
        this.isUpdated = isUpdated;
        this.orderTitle = orderTitle;
        this.printAllItems = printAllItems;
        this.appCommandContext = appCommandContext;
    }

    public void print(Context context, TcrApplication app, IKitchenPrinter printer){
        printHeader(context, app, printer);
        printBody(context, app, printer);
        printFooter(context, app, printer);
    }

    private void printHeader(Context context, TcrApplication app, IKitchenPrinter printer) {
        if (app.isTrainingMode()) {
            printer.center(context.getString(R.string.training_mode_receipt_title));
            printer.emptyLine();
        }

        if (isUpdated) {
            printer.center("UPDATED");
            printer.emptyLine();
        }

        Cursor c;
        c = ProviderAction.query(ShopProvider.getContentUri(PrinterAliasTable.URI_CONTENT))
                .projection(PrinterAliasTable.ALIAS)
                .where(PrinterAliasTable.GUID + " = ?", aliasGuid)
                .perform(context);

        String station = c.moveToFirst() ? c.getString(0) : "<Unknown>";
        c.close();

        c = ProviderAction.query(ShopProvider.getContentUri(SaleOrderView.URI_CONTENT))
                .projection(
                        SaleOrderView2.RegisterTable.TITLE,
                        SaleOrderView2.SaleOrderTable.PRINT_SEQ_NUM)
                .where(SaleOrderView2.SaleOrderTable.GUID + " = ?", orderGuid)
                .perform(context);

        String registerTitle = "";
        int seqNum = 0;
        if(c.moveToFirst()){
            registerTitle = c.getString(0);
            seqNum = c.getInt(1);
        }
        c.close();

        orderNumber = registerTitle + "-" + seqNum;
        ShopInfo shopInfo = app.getShopInfo();
        printer.header(
                shopInfo.name,
                registerTitle,
                context.getString(R.string.kitchen_receipt_order_label),
                seqNum,
                context.getString(R.string.kitchen_receipt_operator_label),
                appCommandContext.getEmployeeFullName(),
                context.getString(R.string.kitchen_receipt_station_label),
                station,
                context.getString(R.string.kitchen_receipt_holder_label),
                orderTitle
        );

        printer.drawLine();
    }

    private void printBody(Context context, TcrApplication app, IKitchenPrinter printer) {
        for (ItemInfo item : items){
            String description = item.description;
            if (!TextUtils.isEmpty(item.modifier)){
//                description = description + ", " + item.modifier;
                item.addons.add(item.modifier);
            }

            BigDecimal printQty = printAllItems ? item.qty : item.qty.subtract(item.printedQty);
            printer.add(printQty, description);

            if (!item.addons.isEmpty()){
                for (String addon : item.addons){
                    printer.addAddsOn(addon);
                }
            }

            if (!item.options.isEmpty()){
                for (String option : item.options){
                    printer.addAddsOn(context.getString(R.string.kitchen_receipt_no_option_prefix) + option);
                }
            }

            if (!TextUtils.isEmpty(item.notes)){
                printer.addAddsOn(item.notes);
            }
        }
        printer.drawLine();
    }

    private void printFooter(Context context, TcrApplication app, IKitchenPrinter printer) {
        printer.tabbed(context.getString(R.string.kitchen_receipt_order_label), orderNumber);
        printer.tabbed(context.getString(R.string.kitchen_receipt_date_label), dateAndTimeShortFormat(new Date()));
    }
}
