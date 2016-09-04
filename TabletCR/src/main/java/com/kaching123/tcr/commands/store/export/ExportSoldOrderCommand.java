package com.kaching123.tcr.commands.store.export;

import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.function.ReadPaymentTransactionsFunction;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.SaleOrderTable;

import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gdubina on 03.02.14.
 */
public class ExportSoldOrderCommand extends ExportBaseOrdersCommand{

    @Override
    protected String getFileName() {
        return "Sales_Export";
    }

    @Override
    protected String[] getHeader() {
        return new String[]{
                "Sale Record ID",
                "Sale Date",
                "Customer Record ID",
                "Register Number",
                "Cashier",

                "Total Price",
                "Discount",
                "Total Tax",
                "Total",

                "Tendered Amount",
                "Change Returned",
                "Payment Type",

                "Card Type",
                "Receipt Number"
        };
    }

    @Override
    protected boolean needChange() {
        return true;
    }

    @Override
    protected String[] getProjection() {
        return new String[]{SaleOrderTable.GUID,
                SaleOrderTable.CREATE_TIME,
                SaleOrderTable.CUSTOMER_GUID,//null
                ShopSchema2.SaleOrderView2.RegisterTable.TITLE,

                SaleOrderTable.TML_TOTAL_DISCOUNT,
                SaleOrderTable.TML_TOTAL_TAX,
                SaleOrderTable.TML_TOTAL_PRICE,
                SaleOrderTable.PRINT_SEQ_NUM,
                SaleOrderTable.STATUS,

                ShopSchema2.SaleOrderView2.OperatorTable.FIRST_NAME,
                ShopSchema2.SaleOrderView2.OperatorTable.LAST_NAME};
    }

    @Override
    protected Query where(Query query) {
        return query.where("(" + SaleOrderTable.STATUS + " = ? or " + SaleOrderTable.STATUS + " = ?)", OrderStatus.COMPLETED.ordinal(), OrderStatus.RETURN.ordinal());
    }

    protected OrderPaymentInfo getPayments(String orderGuid, OrderStatus status) {
        ArrayList<PaymentTransactionModel> transactions = ReadPaymentTransactionsFunction.loadByOrderSingle(getContext(), orderGuid);
        /*Collection<PaymentTransactionModel> sales = Collections2.filter(transactions, new Predicate<PaymentTransactionModel>() {
            @Override
            public boolean apply(PaymentTransactionModel p) {
                return p.paymentType == PaymentType.SALE;
            }
        });*/

        OrderPaymentInfo result = new OrderPaymentInfo();
        result.isMulty = transactions.size() > 1;

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalAmountWithChange = BigDecimal.ZERO;

        PaymentTransactionModel first = null;
        PaymentTransactionModel firstCredit = null;

        for (PaymentTransactionModel p : transactions) {
            totalAmount = totalAmount.add(p.amount);
            totalAmountWithChange = totalAmountWithChange.add(p.amount.add(p.changeAmount));
            if (first == null && (status == OrderStatus.RETURN || p.paymentType == PaymentType.SALE)) {
                first = p;
            }
            if (firstCredit == null && (status == OrderStatus.RETURN || p.paymentType == PaymentType.SALE) && p.gateway.isCreditCard()) {
                firstCredit = p;
            }
        }

        result.firstPayment = firstCredit == null ? first : firstCredit;
        result.totalPaymentAmount = totalAmountWithChange;
        result.totalChangeReturned = totalAmountWithChange.subtract(totalAmount);
        return result;
    }
}
