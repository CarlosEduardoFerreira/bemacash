package com.kaching123.tcr.print.processor;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.util.IHeaderFooterPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PrepaidReleaseResult;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.CustomerTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.OperatorTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.RegisterTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderView;
import com.kaching123.tcr.util.PhoneUtil;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.util.ArrayList;
import java.util.Date;

import static com.kaching123.tcr.fragment.UiHelper.concatFullname;
import static com.kaching123.tcr.model.ContentValuesUtil._orderType;

/**
 * Created by gdubina on 23.12.13.
 */
public abstract class BasePrintProcessor<T extends IHeaderFooterPrinter> {

    private static final Uri URI_ORDER = ShopProvider.getContentUri(SaleOrderView.URI_CONTENT);

    protected String orderGuid;
    protected boolean hasCreditCardPayment;
    protected String orderNumber;
    protected String title;
    protected String subTotal;
    protected String discountTotal;
    protected String taxTotal;
    protected ArrayList<PaymentTransactionModel> transactions;
    protected String amountTotal;
    protected ArrayList<PrepaidReleaseResult> prepaidReleaseResults;

    private final IAppCommandContext appCommandContext;

    public BasePrintProcessor(String orderGuid, IAppCommandContext appCommandContext) {
        this.orderGuid = orderGuid;
        this.appCommandContext = appCommandContext;
    }

    public void updateHasCreditCardPayment(boolean hasCreditCardPayment) {
        this.hasCreditCardPayment |= hasCreditCardPayment;
    }

    public void print(final Context context, final TcrApplication app, final T printerWrapper) {
        prePrintHeader(context, app, printerWrapper);
        printHeader(context, app, printerWrapper);
        printBody(context, app, printerWrapper);
        printFooter(app, printerWrapper);
    }

    protected abstract void printBody(final Context context, final TcrApplication app, final T printerWrapper);

    protected void printFooter(TcrApplication app, T printerWrapper) {

        if (title == null || title.equalsIgnoreCase("ARG_ORDER_TITLE"))
            printerWrapper.barcode(orderNumber);

        ShopInfo shopInfo = app.getShopInfo();

        if (!TextUtils.isEmpty(shopInfo.footerMsg1)) {
            String text = shopInfo.footerMsg1;
            String[] strs = text.split("\n");
            for (String line : strs) {
                printerWrapper.subTitle(line);
            }
        }

        if (!TextUtils.isEmpty(shopInfo.footerMsg2)) {
            String text = shopInfo.footerMsg2;
            String[] strs = text.split("\n");
            for (String line : strs) {
                printerWrapper.subTitle(line);
            }
        }


        if (!TextUtils.isEmpty(shopInfo.email)) {
            printerWrapper.footer(shopInfo.email, true);
        }
        if (!TextUtils.isEmpty(shopInfo.site)) {
            printerWrapper.footer(shopInfo.site, true);
        }
        if (!TextUtils.isEmpty(shopInfo.thanksPhrase)) {
            printerWrapper.footer(shopInfo.thanksPhrase);
        }
    }

    protected void prePrintHeader(Context context, TcrApplication app, T printerWrapper) {
        if (app.isTrainingMode()) {
            printerWrapper.subTitle(context.getString(R.string.training_mode_receipt_title));
            printerWrapper.emptyLine();
        }
    }

    public void printHeader(Context context, TcrApplication app, T printerWrapper) {
        if (app.printLogo()) {
            printerWrapper.logo();
            printerWrapper.emptyLine();
        }

        Cursor c = ProviderAction.query(URI_ORDER)
                .projection(
                        RegisterTable.TITLE,
                        SaleOrderTable.PRINT_SEQ_NUM,
                        SaleOrderTable.CREATE_TIME,
                        OperatorTable.FIRST_NAME,
                        OperatorTable.LAST_NAME,
                        SaleOrderTable.ORDER_TYPE,
                        CustomerTable.CUSTOMER_IDENTIFICATION)
                .where(SaleOrderTable.GUID + " = ?", orderGuid)
                .perform(context);

        String registerTitle = "";
        int seqNum = 0;
        if (c.moveToFirst()) {
            registerTitle = c.getString(0);
            seqNum = c.getInt(1);
        }
        long createTime = c.getLong(2);
        String operatorName = concatFullname(c.getString(3), c.getString(4));
        OrderType orderType = _orderType(c, 5);
        String customerIdentification = c.getString(6);
        if (TextUtils.isEmpty(customerIdentification)) {
            customerIdentification = TcrApplication.isEcuadorVersion() ? "9999999999999" : "";
        }
        c.close();

        orderNumber = registerTitle + "-" + seqNum;
        ShopInfo shopInfo = app.getShopInfo();

        printerWrapper.header(shopInfo.name);

        if (!TextUtils.isEmpty(shopInfo.address1)) {
            printerWrapper.footer(shopInfo.address1);
        }

        String cityStateZip = getCityStateZip(shopInfo);
        if (!TextUtils.isEmpty(cityStateZip)) {
            printerWrapper.footer(cityStateZip);
        }

        String phone = PhoneUtil.parse(shopInfo.phone);
        if (!TextUtils.isEmpty(phone)) {
            printerWrapper.footer(phone);
        }
        if (title != null && !title.equalsIgnoreCase("ARG_ORDER_TITLE")) {
            printerWrapper.emptyLine();
            printerWrapper.header(context.getString(R.string.printer_check));
        }

        printerWrapper.emptyLine();

//        if (title == null || title.equalsIgnoreCase("ARG_ORDER_TITLE"))
//            printerWrapper.header(context.getString(R.string.printer_order_num), registerTitle, seqNum, new Date(createTime), operatorName != null ? operatorName : "");
//        else
//            printerWrapper.header("", "", 0, new Date(createTime), operatorName);

        printerWrapper.header(context.getString(R.string.printer_order_num), registerTitle, seqNum,
                new Date(createTime), context.getString(R.string.printer_cashier), operatorName != null ? operatorName : "",
                context.getString(R.string.printer_customer_identification), customerIdentification);
        printMidTid(context, app, printerWrapper, orderType);
        if (title != null && !title.equalsIgnoreCase("ARG_ORDER_TITLE"))
            printerWrapper.header(context.getString(R.string.printer_guest), title);

        printerWrapper.drawLine();
    }

    public static String getCityStateZip(ShopInfo shopInfo) {
        StringBuilder cityStateZip = new StringBuilder();
        if (!TextUtils.isEmpty(shopInfo.address2)) {
            cityStateZip.append(shopInfo.address2).append(",");
        }

        if (!TextUtils.isEmpty(shopInfo.state)) {
            cityStateZip.append(" ").append(shopInfo.state);
        }

        if (!TextUtils.isEmpty(shopInfo.address3)) {
            cityStateZip.append(" ").append(shopInfo.address3);
        }

        if (TextUtils.isEmpty(shopInfo.state) && TextUtils.isEmpty(shopInfo.address3) && cityStateZip.length() > 0) {
            cityStateZip.setLength(cityStateZip.length() - 1);
        }

        return cityStateZip.toString();
    }

    public String getPrintOrderNumber() {
        return orderNumber;
    }

    protected void printMidTid(Context context, TcrApplication app, T printerWrapper, OrderType type) {
        if (type == OrderType.SALE && appCommandContext.getBlackstoneUser().isValid()) {
            printMidTid(printerWrapper, context.getString(R.string.print_order_merchant_id_label), String.valueOf(appCommandContext.getBlackstoneUser().getMid()), false);
        } else if (type == OrderType.PREPAID && appCommandContext.getPrepaidUser().isValid()) {
            printMidTid(printerWrapper, context.getString(R.string.printer_merchant_id), String.valueOf(app.getShopInfo().prepaidMid), false);
            printMidTid(printerWrapper, context.getString(R.string.printer_terminal_id), String.valueOf(appCommandContext.getPrepaidUser().getTid()), false);
        }
    }

    protected abstract void printMidTid(T printer, String label, String value, boolean bold);

    private static int getPaymentTitle(PaymentGateway gateway) {
        switch (gateway) {
            case BLACKSTONE:
                return R.string.print_order_power_by_backstone;
            case PAYPAL:
                return R.string.print_order_power_by_paypal;
        }
        return 0;
    }
}
