package com.kaching123.tcr.commands.store.export;

import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.function.ReadPaymentTransactionsFunction;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.SaleOrderTable;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by gdubina on 03.02.14.
 */
public class ExportReturnedOrderCommand extends ExportBaseOrdersCommand {

    @Override
    protected String getFileName() {
        return "Returns_Export";
    }

    @Override
    protected String[] getHeader() {
        return new String[]{
                "Return Record ID",
                "Return Date",
                "Customer Record ID",
                "Register Number",

                "Total Price",
                "Discount",
                "Total Tax",
                "Total",

                "Return Amount",
                "Payment Type",

                "Card Type",
                "Receipt Number"
        };
    }

    @Override
    protected boolean needChange() {
        return false;
    }

    @Override
    protected Query where(Query query) {
        return query.where(SaleOrderTable.STATUS + " = ?", OrderStatus.RETURN.ordinal());
    }

    protected OrderPaymentInfo getPayments(String orderGuid, OrderStatus orderStatus) {
        ArrayList<PaymentTransactionModel> transactions = ReadPaymentTransactionsFunction.loadByOrderSingle(getContext(), orderGuid);

        OrderPaymentInfo result = new OrderPaymentInfo();
        result.isMulty = transactions.size() > 1;

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalAmountWithChange = BigDecimal.ZERO;

        PaymentTransactionModel first = null;
        PaymentTransactionModel firstCredit = null;

        for (PaymentTransactionModel p : transactions) {
            totalAmount = totalAmount.add(p.amount);
            totalAmountWithChange = totalAmountWithChange.add(p.amount.add(p.changeAmount));
            if (first == null) {
                first = p;
            }
            if (firstCredit == null && p.gateway.isCreditCard()) {
                firstCredit = p;
            }
        }

        result.firstPayment = firstCredit == null ? first : firstCredit;
        result.totalPaymentAmount = totalAmountWithChange;
        result.totalChangeReturned = totalAmountWithChange.subtract(totalAmount);
        return result;
    }
}
