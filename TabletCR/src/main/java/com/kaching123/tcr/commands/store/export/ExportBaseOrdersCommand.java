package com.kaching123.tcr.commands.store.export;

import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.RegisterTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderView;
import com.telly.groundy.TaskResult;

import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._orderStatus;

/**
 * Created by gdubina on 30.01.14.
 */
public abstract class ExportBaseOrdersCommand extends ExportCursorToFileCommand {

    private static final Uri SALE_ORDER_URI = ShopProvider.getContentUri(SaleOrderView.URI_CONTENT);

    private static final String ARG_START_TIME = "ARG_START_TIME";
    private static final String ARG_END_TIME = "ARG_END_TIME";
    private static final String ARG_REGISTER_GUID = "ARG_REGISTER_GUID";
    public static final String TYPE_OTHER = "Other";
    public static final String TYPE_CREDIT_CARD = "Credit Card";
    public static final String TYPE_MULTIPLE_TENDERING = "MULTIPLE TENDERING";
    public static final String TYPE_CASH = "Cash";
    public static final String TYPE_DEBIT = "Debit";
    public static final String TYPE_EBT = "EBT";


    private long startTime;
    private long endTime;
    private long registerId;

    @Override
    protected TaskResult doInBackground() {
        startTime = getLongArg(ARG_START_TIME);
        endTime = getLongArg(ARG_END_TIME);
        registerId = getLongArg(ARG_REGISTER_GUID);
        return super.doInBackground();
    }

    protected abstract boolean needChange();

    @Override
    protected CellProcessor[] getColumns() {
        if (needChange()) {
            return new CellProcessor[]{
                    null,
                    new FmtDate("MM/dd/yyyy HH:mm"),
                    null,
                    null,

                    null,
                    null,
                    null,
                    null,

                    null,
                    null,
                    null,

                    null,
                    null
            };
        } else {
            return new CellProcessor[]{
                    null,
                    new FmtDate("MM/dd/yyyy HH:mm"),
                    null,
                    null,

                    null,
                    null,
                    null,
                    null,

                    null,
                    null,

                    null,
                    null
            };
        }
    }

    @Override
    protected Cursor query() {
        Query query = ProviderAction
                .query(SALE_ORDER_URI)
                .projection(
                        SaleOrderTable.GUID,
                        SaleOrderTable.CREATE_TIME,
                        SaleOrderTable.CUSTOMER_GUID,//null
                        RegisterTable.TITLE,

                        SaleOrderTable.TML_TOTAL_DISCOUNT,
                        SaleOrderTable.TML_TOTAL_TAX,
                        SaleOrderTable.TML_TOTAL_PRICE,
                        SaleOrderTable.PRINT_SEQ_NUM,
                        SaleOrderTable.STATUS
                )
                .where(SaleOrderTable.CREATE_TIME + " >= ? and " + SaleOrderTable.CREATE_TIME + " <= ?", startTime, endTime);
        where(query)/*
                .where(SaleOrderTable.STATUS + " = ?", getOrderType().ordinal())*/
                .orderBy(SaleOrderTable.CREATE_TIME);

        if (registerId > 0) {
            query.where(SaleOrderTable.REGISTER_ID + " = ?", registerId);
        }
        return query.perform(getContext());
    }

    protected abstract Query where(Query query);

    @Override
    protected List<Object> readRow(Cursor c) {
        String orderGuid = c.getString(0);

        /*ReturnInfo returnInfo;
        if(getOrderType() != OrderStatus.RETURN){
            returnInfo = getReturns(orderGuid);
        }else{
            returnInfo = new ReturnInfo();
        }*/

        ArrayList<Object> result = new ArrayList<Object>(14);
        result.add(orderGuid);
        result.add(new Date(c.getLong(1)));
        result.add(c.getString(2));

        String registerNum = c.getString(3);
        result.add(registerNum);

        BigDecimal discount = _decimal(c.getString(4));
        BigDecimal tax = _decimal(c.getString(5));
        BigDecimal total = _decimal(c.getString(6));

        result.add(_decimal(total.subtract(tax).add(discount)));
        result.add(_decimal(discount));
        result.add(_decimal(tax));
        result.add(_decimal(total));

        boolean needChange = needChange();
        OrderPaymentInfo paymentInfo = getPayments(orderGuid, _orderStatus(c, 8));
        if (paymentInfo.firstPayment == null) {
            result.add("");
            if (needChange) {
                result.add("");
            }
            result.add("");
            addCardInformation(result, null);
        } else if (paymentInfo.isMulty) {
            result.add(_decimal(paymentInfo.totalPaymentAmount));
            if (needChange) {
                result.add(_decimal(paymentInfo.totalChangeReturned));
            }
            result.add(TYPE_MULTIPLE_TENDERING);
            addCardInformation(result, paymentInfo.firstPayment);
        } else if (paymentInfo.firstPayment.gateway == PaymentGateway.CASH) {
            BigDecimal change = paymentInfo.firstPayment.changeAmount == null ? BigDecimal.ZERO : paymentInfo.firstPayment.changeAmount;
            result.add(_decimal(change.add(paymentInfo.firstPayment.amount)));
            if (needChange) {
                result.add(_decimal(paymentInfo.firstPayment.changeAmount));
            }
            result.add(TYPE_CASH);
            addCardInformation(result, paymentInfo.firstPayment);
        } else if (paymentInfo.firstPayment.gateway.isTrueCreditCard()) {
            result.add(_decimal(paymentInfo.firstPayment.amount));
            if (needChange) {
                result.add("0");
            }
            result.add(TYPE_CREDIT_CARD);
            addCardInformation(result, paymentInfo.firstPayment);
        } else if (paymentInfo.firstPayment.gateway == PaymentGateway.PAX_DEBIT) {
            result.add(_decimal(paymentInfo.firstPayment.amount));
            if (needChange) {
                result.add("0");
            }
            result.add(TYPE_DEBIT);
            addCardInformation(result, paymentInfo.firstPayment);
        } else if (paymentInfo.firstPayment.gateway == PaymentGateway.PAX_EBT_CASH || paymentInfo.firstPayment.gateway == PaymentGateway.PAX_EBT_FOODSTAMP) {
            result.add(_decimal(paymentInfo.firstPayment.amount));
            if (needChange) {
                result.add("0");
            }
            result.add(TYPE_EBT);
            addCardInformation(result, paymentInfo.firstPayment);
        } else {
            BigDecimal change = paymentInfo.firstPayment.changeAmount == null ? BigDecimal.ZERO : paymentInfo.firstPayment.changeAmount;
            result.add(_decimal(change.add(paymentInfo.firstPayment.amount)));
            if (needChange) {
                result.add(_decimal(paymentInfo.firstPayment.changeAmount));
            }
            result.add(TYPE_OTHER);
            addCardInformation(result, paymentInfo.firstPayment);
        }

        result.add(registerNum + "-" + c.getString(7));
        return result;
    }

    private void addCardInformation(ArrayList<Object> result, PaymentTransactionModel payment) {
        if (payment != null && payment.gateway.isCreditCard()) {
            result.add(payment.cardName);
        } else {
            result.add("");
        }
    }

    /*private ReturnInfo getReturns(String orderGuid) {
        return getReturns(getContext(), orderGuid);
    }

    public static ReturnInfo getReturns(Context context, String orderGuid) {
        Cursor c = ProviderAction
                .query(SALE_ORDER_URI)
                .projection(
                        SaleOrderTable.GUID,
                        SaleOrderTable.TML_TOTAL_DISCOUNT,
                        SaleOrderTable.TML_TOTAL_TAX,
                        SaleOrderTable.TML_TOTAL_PRICE
                )
                .where(SaleOrderTable.PARENT_ID + " = ?", orderGuid)
                .where(SaleOrderTable.STATUS + " = ?", OrderStatus.RETURN.ordinal())
                .perform(context);

        ReturnInfo info = new ReturnInfo();
        while(c.moveToNext()){
            BigDecimal discount = _decimal(c.getString(1));
            BigDecimal tax = _decimal(c.getString(2));
            BigDecimal total = _decimal(c.getString(3));

            info.totalDiscount = info.totalDiscount.add(discount);
            info.totalTax = info.totalTax.add(tax);
            info.totalPrice = info.totalPrice.add(total);
        }
        c.close();
        return info;
    }

    public static class ReturnInfo{
        public BigDecimal totalPrice = BigDecimal.ZERO;
        public BigDecimal totalDiscount = BigDecimal.ZERO;
        public BigDecimal totalTax = BigDecimal.ZERO;
    }*/

    protected abstract OrderPaymentInfo getPayments(String orderGuid, OrderStatus orderStatus);

    class OrderPaymentInfo {
        BigDecimal totalPaymentAmount;
        BigDecimal totalChangeReturned;

        PaymentTransactionModel firstPayment;
        boolean isMulty;
    }
}
