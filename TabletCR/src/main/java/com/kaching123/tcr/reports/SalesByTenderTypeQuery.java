package com.kaching123.tcr.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.PaymentTransactionView2.PaymentTransactionTable;
import com.kaching123.tcr.store.ShopSchema2.PaymentTransactionView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionView;

import java.math.BigDecimal;
import java.util.HashMap;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._paymentGateway;

/**
 * Created by gdubina on 28.01.14.
 */
public class SalesByTenderTypeQuery {

    private static final Uri URI_PAYMENTS = ShopProvider.getContentUri(PaymentTransactionView.URI_CONTENT);

    public static PaymentStat getItems(Context context, long startTime, long endTime, long resisterId) {
        Query query = ProviderAction.query(URI_PAYMENTS)
                .where(PaymentTransactionTable.CREATE_TIME + " >= ? and " + PaymentTransactionTable.CREATE_TIME + " <= ?", startTime, endTime)
                //.where(PaymentTransactionTable.TYPE + " = ?", PaymentType.SALE.ordinal())
                .where("(" + PaymentTransactionTable.STATUS + " = ? OR " + PaymentTransactionTable.STATUS + " = ?)", PaymentStatus.PRE_AUTHORIZED.ordinal(), PaymentStatus.SUCCESS.ordinal());

        if (resisterId > 0) {
            query.where(SaleOrderTable.REGISTER_ID + " = ?", resisterId);
        }
        Cursor c = query.perform(context);
        PaymentStat stat = new PaymentStat();

        HashMap<String, BigDecimal> cards = new HashMap<String, BigDecimal>();
        while (c.moveToNext()) {
            BigDecimal amount = _decimal(c.getString(c.getColumnIndex(PaymentTransactionTable.AMOUNT)), BigDecimal.ZERO);
//            BigDecimal tipAmount = _decimal(c.getString(c.getColumnIndex(PaymentTransactionView2.EmployeeTipsTable.AMOUNT)), BigDecimal.ZERO);
//            amount = amount.subtract(tipAmount);
            PaymentGateway gateway = _paymentGateway(c, c.getColumnIndex(PaymentTransactionTable.GATEWAY));
            if(gateway.isCreditCard()){
                String card = c.getString(c.getColumnIndex(PaymentTransactionTable.CARD_NAME));
                BigDecimal value = cards.get(card);
                if (value == null) {
                    value = amount;
                } else {
                    value = value.add(amount);
                }
                cards.put(card, value);
            }

            if (gateway.isTrueCreditCard()) {
                stat.creditCard = stat.creditCard.add(amount);
            } else if (gateway == PaymentGateway.CASH){
                stat.cash = stat.cash.add(amount);
            }else if (gateway == PaymentGateway.CREDIT){
                stat.creditReceipt = stat.creditReceipt.add(amount);
            } else if (gateway == PaymentGateway.OFFLINE_CREDIT) {
                stat.offlineCredit = stat.offlineCredit.add(amount);
            } else if (gateway == PaymentGateway.CHECK) {
                stat.check = stat.check.add(amount);
            } else if (gateway == PaymentGateway.PAX_EBT_CASH) {
                stat.ebtCash = stat.ebtCash.add(amount);
            } else if (gateway == PaymentGateway.PAX_EBT_FOODSTAMP) {
                stat.ebtFoodstamp = stat.ebtFoodstamp.add(amount);
            } else if (gateway == PaymentGateway.PAX_DEBIT) {
                stat.debit = stat.debit.add(amount);
            }
        }
        c.close();
        return stat;
    }

    public static class PaymentStat {
        public BigDecimal cash = BigDecimal.ZERO;
        public BigDecimal creditCard = BigDecimal.ZERO;
        public BigDecimal creditReceipt = BigDecimal.ZERO;
        public BigDecimal offlineCredit = BigDecimal.ZERO;
        public BigDecimal check = BigDecimal.ZERO;
        public BigDecimal ebtCash = BigDecimal.ZERO;
        public BigDecimal ebtFoodstamp = BigDecimal.ZERO;
        public BigDecimal debit = BigDecimal.ZERO;
        /*public BigDecimal gift = BigDecimal.ZERO;*/
    }
}
