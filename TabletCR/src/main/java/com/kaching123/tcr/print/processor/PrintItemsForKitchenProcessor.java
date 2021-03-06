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
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.SimpleModifier;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.OnHoldStatus;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderView;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._onHoldStatus;
import static com.kaching123.tcr.util.DateUtils.dateAndTimeShortFormat;
import static com.kaching123.tcr.util.PhoneUtil.parseDigitsToFormattedPhone;

/**
 * Created by vkompaniets on 17.02.14.
 */
public class PrintItemsForKitchenProcessor {

    private List<ItemInfo> items;
    private PrinterInfo printer;
    private OnHoldStatus onHoldStatus;
    private String aliasGuid;
    private String orderGuid;
    private String orderTitle;
    private String orderNumber;
    private String onHoldPhone;
    private boolean isUpdated;
    private boolean printAllItems;
    private boolean isVoid;
    private boolean itComesFromPay;

    private final IAppCommandContext appCommandContext;

    public PrintItemsForKitchenProcessor(List<ItemInfo> items, PrinterInfo printer, String aliasGuid, String orderGuid,
                                         boolean isUpdated, String orderTitle, boolean printAllItems, boolean isVoid,
                                         String onHoldPhone, OnHoldStatus onHoldStatus, boolean itComesFromPay, IAppCommandContext appCommandContext) {
        this.items = items;
        this.printer = printer;
        this.aliasGuid = aliasGuid;
        this.orderGuid = orderGuid;
        this.isUpdated = isUpdated;
        this.orderTitle = orderTitle;
        this.printAllItems = printAllItems;
        this.appCommandContext = appCommandContext;
        this.isVoid = isVoid;
        this.onHoldPhone = onHoldPhone;
        this.onHoldStatus = onHoldStatus;
        this.itComesFromPay = itComesFromPay;
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
        String station = app.getRegisterDescription();
        if(TextUtils.isEmpty(station)) {
            station = app.getRegisterTitle();
        }

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

        if (onHoldPhone == null && onHoldStatus == null) {
            c = ProviderAction.query(ShopProvider.getContentUri(ShopStore.SaleOrderTable.URI_CONTENT))
                    .projection(
                            ShopStore.SaleOrderTable.HOLD_TEL,
                            ShopStore.SaleOrderTable.HOLD_NAME,
                            ShopStore.SaleOrderTable.HOLD_STATUS)
                    .where(ShopStore.SaleOrderTable.GUID + " = ?", orderGuid)
                    .perform(context);

            if (c.moveToFirst()) {
                onHoldPhone = parseDigitsToFormattedPhone(c.getString(0));
                onHoldStatus = _onHoldStatus(c, 2);
                if (TextUtils.isEmpty(orderTitle)) {
                    orderTitle = c.getString(1);
                }
            }
            c.close();
        }

        String customer = null;
        if(!isUpdated && itComesFromPay) {
            orderTitle = null;
            SaleOrderModel saleOrderModel = SaleOrderModel.getById(context, orderGuid);
            if (!TextUtils.isEmpty(saleOrderModel.customerGuid)) {
                CustomerModel customerModel = CustomerModel.loadSync(context, saleOrderModel.customerGuid);
                if (customerModel != null) {
                    customer = customerModel.getFullName();
                }
            }
        }

        String holdStatus = null;
        if(onHoldStatus != null) {
            switch (onHoldStatus) {
                case TO_STAY:
                    holdStatus = context.getString(R.string.to_stay);
                    break;
                case TO_GO:
                    holdStatus = context.getString(R.string.to_go);
                    break;
            }
        }

        orderNumber = registerTitle + "-" + seqNum;
        ShopInfo shopInfo = app.getShopInfo();
        printer.header(
                shopInfo.name,
                registerTitle,
                context.getString(R.string.kitchen_receipt_order_type_label),
                holdStatus,
                context.getString(R.string.kitchen_receipt_order_label),
                seqNum,
                context.getString(R.string.kitchen_receipt_operator_label),
                appCommandContext.getEmployeeFullName(),
                context.getString(R.string.kitchen_receipt_station_label),
                station,
                context.getString(R.string.kitchen_receipt_holder_label),
                orderTitle,
                context.getString(R.string.phone),
                onHoldPhone,
                context.getString(R.string.customer_colon),
                customer
        );

        printer.drawLine();
    }

    private void printBody(Context context, TcrApplication app, IKitchenPrinter printer) {
        for (ItemInfo item : items){
            String description = item.description;

            BigDecimal printQty = printAllItems ? item.qty : item.qty.subtract(item.printedQty);

            if (isVoid){
                printQty = item.printedQty;
            }
            printer.add(printQty, description);

            if (!item.modifier.isEmpty()){
                for (String modifier : item.modifier){
                    printer.addAddsOn(modifier);
                }
            }

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

            if (item.modifiers != null && !item.modifiers.isEmpty()){
                String no = context.getString(R.string.kitchen_receipt_no_option_prefix);
                for (SimpleModifier modifier : item.modifiers){
                    String modifierTitle = modifier.type == ModifierType.OPTIONAL ? no + modifier.title : modifier.title;
                    printer.addAddsOn(modifierTitle);
                }
            }

            if (!TextUtils.isEmpty(item.notes)){
                printer.addAddsOn(item.notes);
            }
        }
        printer.drawLine();
    }

    private void printFooter(Context context, TcrApplication app, IKitchenPrinter printer) {
        printer.tabbed(context.getString(R.string.kitchen_receipt_date_label), dateAndTimeShortFormat(new Date()));
    }
}
