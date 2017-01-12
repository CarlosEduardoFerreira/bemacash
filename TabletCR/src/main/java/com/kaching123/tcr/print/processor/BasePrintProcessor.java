package com.kaching123.tcr.print.processor;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.printer.BitmapCarl;
import com.kaching123.pos.printer.BitmapPrintedCarl;
import com.kaching123.pos.util.IHeaderFooterPrinter;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorBaseCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxProcessorSaleCommand;
import com.kaching123.tcr.commands.payment.pax.processor.PaxSignature;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import static com.kaching123.tcr.fragment.UiHelper.concatFullname;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._orderType;

/**
 * Created by gdubina on 23.12.13.
 */
public abstract class BasePrintProcessor<T extends IHeaderFooterPrinter> {

    protected static final Uri URI_ORDER = ShopProvider.getContentUri(SaleOrderView.URI_CONTENT);

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
    protected ArrayList<GiftCardBillingResult> giftCardResults;

    private final IAppCommandContext appCommandContext;
    protected PrintOrderInfo orderInfo;

    public BasePrintProcessor(String orderGuid, IAppCommandContext appCommandContext) {
        this.orderGuid = orderGuid;
        this.appCommandContext = appCommandContext;
    }

    public void updateHasCreditCardPayment(boolean hasCreditCardPayment) {
        this.hasCreditCardPayment |= hasCreditCardPayment;
    }

    public void print(final Context context, final TcrApplication app, final T printerWrapper) {
        if (!isGiftCard())
            orderInfo = loadOrderInfo(context);

        prePrintHeader(context, app, printerWrapper);
        printHeader(context, app, printerWrapper);
        printBody(context, app, printerWrapper);
        printLoyalty(context, app, printerWrapper);
        printFooter(context, app, printerWrapper);
    }

    protected boolean isGiftCard() {
        return false;
    }

    protected abstract void printBody(final Context context, final TcrApplication app, final T printerWrapper);
    protected abstract void printLoyalty(Context context, TcrApplication app, T printerWrapper);


    protected void printFooter(Context context, TcrApplication app, T printerWrapper) {

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

        orderNumber = orderInfo.registerTitle + "-" + orderInfo.seqNum;
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


        printerWrapper.header(context.getString(R.string.printer_order_num), orderInfo.registerTitle, orderInfo.seqNum,
                new Date(orderInfo.createTime), context.getString(R.string.printer_cashier), orderInfo.operatorName != null ? orderInfo.operatorName : "",
                context.getString(R.string.printer_customer_identification), orderInfo.customerIdentification);
        if (orderInfo.customerName != null) {
            printerWrapper.header(context.getString(R.string.printer_ec_customer_name), orderInfo.customerName);
        }
        printMidTid(context, app, printerWrapper, orderInfo.orderType);
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

    private PrintOrderInfo loadOrderInfo(Context context) {
        Cursor c = ProviderAction.query(URI_ORDER)
                .projection(
                        RegisterTable.TITLE,
                        SaleOrderTable.PRINT_SEQ_NUM,
                        SaleOrderTable.CREATE_TIME,
                        OperatorTable.FIRST_NAME,
                        OperatorTable.LAST_NAME,
                        SaleOrderTable.ORDER_TYPE,
                        CustomerTable.CUSTOMER_IDENTIFICATION,
                        CustomerTable.TMP_LOYALTY_POINTS,
                        CustomerTable.FISRT_NAME,
                        CustomerTable.LAST_NAME)
                .where(SaleOrderTable.GUID + " = ?", orderGuid)
                .perform(context);

        if (!c.moveToFirst())
            throw new IllegalStateException("cannot load order info");

        PrintOrderInfo info = new PrintOrderInfo();

        info.registerTitle = "";
        info.seqNum = 0;
        if (c.moveToFirst()) {
            info.registerTitle = c.getString(0);
            info.seqNum = c.getInt(1);
        }
        info.createTime = c.getLong(2);
        info.operatorName = concatFullname(c.getString(3), c.getString(4));
        info.orderType = _orderType(c, 5);
        String customerIdentification = c.getString(6);
        if (TextUtils.isEmpty(customerIdentification)) {
            info.customerIdentification = TcrApplication.getCountryFunctionality().isMultiTaxGroup()//.isCurrentCountryUsesMultiTax()
                    ? "9999999999999" : "";
        }
        String loyaltyPoints = c.getString(7);
        if (loyaltyPoints != null) {
            info.customerLoyaltyPoints = _decimal(loyaltyPoints, BigDecimal.ZERO);
        }
        info.customerName = concatFullname(c.getString(8), c.getString(9));
        c.close();

        return info;
    }

    protected class PrintOrderInfo {
        public String registerTitle;
        public int seqNum;
        public long createTime;
        public String operatorName;
        public OrderType orderType;
        public String customerIdentification;
        public String customerName;
        public BigDecimal customerLoyaltyPoints;
        public BigDecimal earnedLoyaltyPoints = BigDecimal.ZERO;
    }
}
