package com.kaching123.tcr.print.processor;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.util.PhoneUtil;
import com.telly.groundy.PublicGroundyTask;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.kaching123.tcr.fragment.UiHelper.concatFullname;
import static com.kaching123.tcr.model.ContentValuesUtil._orderType;

/**
 * Created by mboychenko on 25.08.2016.
 */
public class DetailedReportPrintOrdersProcessor extends PrintOrderProcessor {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.US);

    public DetailedReportPrintOrdersProcessor(String orderGuid, PublicGroundyTask.IAppCommandContext appCommandContext) {
        super(orderGuid, appCommandContext);
    }

    private boolean hasAddressInfo;
    private boolean lastReceipt;
    private String reportTotal;
    private long from;
    private long to;

    public void hasAddressInfo(boolean hasAddressInfo, long fromDate, long toDate) {
        this.hasAddressInfo = hasAddressInfo;
        this.from = fromDate;
        this.to = toDate;
    }

    public void lastReceipt(String reportTotal) {
        lastReceipt = true;
        this.reportTotal = reportTotal;
    }

    @Override
    public void printHeader(Context context, TcrApplication app, ITextPrinter printerWrapper) {
        if (app.printLogo()) {
            printerWrapper.logo();
            printerWrapper.emptyLine();
        }

        Cursor c = ProviderAction.query(URI_ORDER)
                .projection(
                        ShopSchema2.SaleOrderView2.RegisterTable.TITLE,
                        ShopSchema2.SaleOrderView2.SaleOrderTable.PRINT_SEQ_NUM,
                        ShopSchema2.SaleOrderView2.SaleOrderTable.CREATE_TIME,
                        ShopSchema2.SaleOrderView2.OperatorTable.FIRST_NAME,
                        ShopSchema2.SaleOrderView2.OperatorTable.LAST_NAME,
                        ShopSchema2.SaleOrderView2.SaleOrderTable.ORDER_TYPE,
                        ShopSchema2.SaleOrderView2.CustomerTable.CUSTOMER_IDENTIFICATION)
                .where(ShopSchema2.SaleOrderView2.SaleOrderTable.GUID + " = ?", orderGuid)
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
        ShopInfoViewJdbcConverter.ShopInfo shopInfo = app.getShopInfo();

        if(!hasAddressInfo){
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

            printerWrapper.drawLine();

            drawSubtitle(printerWrapper, context);

            printerWrapper.drawLine();

        }

        if (title != null && !title.equalsIgnoreCase("ARG_ORDER_TITLE")) {
            printerWrapper.emptyLine();
            printerWrapper.header(context.getString(R.string.printer_check));
        }

        printerWrapper.emptyLine();

        printerWrapper.header(context.getString(R.string.printer_order_num), registerTitle, seqNum,
                new Date(createTime), context.getString(R.string.printer_cashier), operatorName != null ? operatorName : "",
                context.getString(R.string.printer_customer_identification), customerIdentification);
        printMidTid(context, app, printerWrapper, orderType);
        if (title != null && !title.equalsIgnoreCase("ARG_ORDER_TITLE"))
            printerWrapper.header(context.getString(R.string.printer_guest), title);

        printerWrapper.drawLine();
    }

    private void drawSubtitle(ITextPrinter printer, Context context) {
        printer.footer(context.getString(R.string.report_type_detailed_report));
        printer.addWithTab2(context.getString(R.string.zreport_print_time), dateFormat.format(new Date()), true, false);
        printer.footer(context.getString(R.string.detailed_report_filter));
        printer.addWithTab2(context.getString(R.string.detailed_report_filter_from), dateFormat.format(new Date(from)), true, false);
        printer.addWithTab2(context.getString(R.string.detailed_report_filter_to), dateFormat.format(new Date(to)), true, false);
    }

    @Override
    protected void printFooter(TcrApplication app, ITextPrinter printerWrapper) {
        printerWrapper.emptyLine();
        if(lastReceipt) {
            printerWrapper.addWithTab2(app.getString(R.string.report_detailed_sales_total),
                    new DecimalFormat("0.00").format(new BigDecimal(reportTotal)), true, true);

            printerWrapper.emptyLine();

            ShopInfoViewJdbcConverter.ShopInfo shopInfo = app.getShopInfo();

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
    }
}
