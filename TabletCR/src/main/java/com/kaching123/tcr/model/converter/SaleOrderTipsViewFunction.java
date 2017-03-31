package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.kaching123.tcr.model.SaleOrderTipsViewModel;
import com.kaching123.tcr.model.SaleOrderTipsViewModel.TenderType;
import com.kaching123.tcr.model.SaleOrderTipsViewModel.TransactionsState;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.CustomerTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.OperatorTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.RegisterTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.TipsTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTipsQuery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import static com.kaching123.tcr.fragment.UiHelper.concatFullname;
import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;
import static com.kaching123.tcr.model.ContentValuesUtil._kdsSendStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._kitchenPrintStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._onHoldStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._orderStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._orderType;

/**
 * Created by vkompaniets on 11.07.2016.
 */
public class SaleOrderTipsViewFunction extends ListConverterFunction<SaleOrderTipsViewModel> {

    @Override
    public SaleOrderTipsViewModel apply(Cursor c) {
        super.apply(c);

        boolean hasPreauthTransactions = c.getInt(indexHolder.get(SaleOrderTipsQuery.HAS_PREAUTH_TRANSACTIONS)) > 0;
        boolean hasOpenedTransactions = c.getInt(indexHolder.get(SaleOrderTipsQuery.HAS_OPENED_TRANSACTIONS)) > 0;

        boolean hasCashTransactions = c.getInt(indexHolder.get(SaleOrderTipsQuery.CASH_TRANSACTION_CNT)) > 0;
        boolean hasCreditTransactions = c.getInt(indexHolder.get(SaleOrderTipsQuery.CREDIT_TRANSACTION_CNT)) > 0;
        boolean hasDebitTransactions = c.getInt(indexHolder.get(SaleOrderTipsQuery.DEBIT_TRANSACTION_CNT)) > 0;
        boolean hasEbtTransactions = c.getInt(indexHolder.get(SaleOrderTipsQuery.EBT_TRANSACTION_CNT)) > 0;
        boolean hasOtherTransactions = c.getInt(indexHolder.get(SaleOrderTipsQuery.OTHER_TRANSACTION_CNT)) > 0;

        TenderType tenderType;
        ArrayList<TenderType> tenderTypes = new ArrayList<TenderType>();
        if (hasCashTransactions) {
            tenderTypes.add(TenderType.CASH);
        }
        if (hasCreditTransactions) {
            tenderTypes.add(TenderType.CREDIT_CARD);
        }
        if (hasDebitTransactions) {
            tenderTypes.add(TenderType.DEBIT_CARD);
        }
        if (hasEbtTransactions) {
            tenderTypes.add(TenderType.EBT);
        }
        if (hasOtherTransactions) {
            tenderTypes.add(TenderType.OTHER);
        }

        if (tenderTypes.isEmpty()) {
            tenderType = null;
        } else if (tenderTypes.size() == 1) {
            tenderType = tenderTypes.get(0);
        } else {
            tenderType = TenderType.MULTIPLE;
        }

        TransactionsState transactionState;
        if (hasOpenedTransactions)
            transactionState = TransactionsState.OPEN;
        else
            transactionState = TransactionsState.CLOSED;

        return new SaleOrderTipsViewModel(
                c.getString(indexHolder.get(SaleOrderTable.GUID)),
                new Date(c.getLong(indexHolder.get(SaleOrderTable.CREATE_TIME))),
                c.getString(indexHolder.get(SaleOrderTable.OPERATOR_GUID)),
                c.getString(indexHolder.get(SaleOrderTable.SHIFT_GUID)),
                c.getString(indexHolder.get(SaleOrderTable.CUSTOMER_GUID)),
                _decimal(c, indexHolder.get(SaleOrderTable.DISCOUNT), BigDecimal.ZERO),
                _discountType(c, indexHolder.get(SaleOrderTable.DISCOUNT_TYPE)),
                _orderStatus(c, indexHolder.get(SaleOrderTable.STATUS)),
                c.getString(indexHolder.get(SaleOrderTable.HOLD_NAME)),
                c.getString(indexHolder.get(SaleOrderTable.DEFINED_ON_HOLD_ID)),
                c.getString(indexHolder.get(SaleOrderTable.HOLD_TEL)),
                _onHoldStatus(c, indexHolder.get(SaleOrderTable.HOLD_STATUS)),
                _bool(c, indexHolder.get(SaleOrderTable.TAXABLE)),
                _decimal(c, indexHolder.get(SaleOrderTable.TML_TOTAL_PRICE), BigDecimal.ZERO),
                _decimal(c, indexHolder.get(SaleOrderTable.TML_TOTAL_TAX), BigDecimal.ZERO),
                _decimal(c, indexHolder.get(SaleOrderTable.TML_TOTAL_DISCOUNT), BigDecimal.ZERO),
                c.getInt(indexHolder.get(SaleOrderTable.PRINT_SEQ_NUM)),
                c.getInt(indexHolder.get(SaleOrderTable.REGISTER_ID)),
                c.getString(indexHolder.get(SaleOrderTable.PARENT_ID)),
                _orderType(c, indexHolder.get(SaleOrderTable.ORDER_TYPE)),
                _bool(c, indexHolder.get(SaleOrderTable.IS_TIPPED)),
                concatFullname(c.getString(indexHolder.get(OperatorTable.FIRST_NAME)), c.getString(indexHolder.get(OperatorTable.LAST_NAME))),
                c.getString(indexHolder.get(RegisterTable.TITLE)),
                concatFullname(c.getString(indexHolder.get(CustomerTable.FISRT_NAME)), c.getString(indexHolder.get(CustomerTable.LAST_NAME))),
                c.getString(indexHolder.get(CustomerTable.PHONE)),
                c.getString(indexHolder.get(CustomerTable.EMAIL)),
                _decimal(c, indexHolder.get(TipsTable.AMOUNT), BigDecimal.ZERO),
                transactionState,
                tenderType,
                _kitchenPrintStatus(c, indexHolder.get(SaleOrderTable.KITCHEN_PRINT_STATUS)),
                _kdsSendStatus(c, c.getColumnIndex(ShopSchema2.SaleOrderView2.SaleOrderTable.KDS_SEND_STATUS)),
                _bool(c, indexHolder.get(SaleOrderTable.ON_REGISTER)),
                _decimal(c, indexHolder.get(SaleOrderTable.TRANSACTION_FEE), BigDecimal.ZERO),
                c.getInt(indexHolder.get(SaleOrderTable.AGE_VERIFIED))
        );
    }


}
