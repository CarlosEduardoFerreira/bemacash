package com.kaching123.tcr.model;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.print.digital.PrintOrderToKdsCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.KitchenPrintStatus;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by pkabakov on 30.05.2014.
 */
public class SaleOrderTipsViewModel extends SaleOrderViewModel {

    public TransactionsState transactionsState;
    public TenderType tenderType;

    public SaleOrderTipsViewModel(String guid, Date createTime, String operatorGuid, String shiftGuid, String customerGuid, BigDecimal discount, DiscountType discountType, OrderStatus orderStatus, String holdName, String definedOnHoldGuid, String holdPhone, OnHoldStatus holdStatus, boolean taxable, BigDecimal tmpTotalPrice, BigDecimal tmpTotalTax, BigDecimal tmpTotalDiscount, int printSeqNum, long registerId, String parentId, OrderType type, boolean isTipped, String operatorName, String registerTitle, String customerName, String customerPhone, String customerEmail, BigDecimal tipsAmount, TransactionsState transactionsState, TenderType tenderType, KitchenPrintStatus kitchenPrintStatus, PrintOrderToKdsCommand.KDSSendStatus kdsSendStatus, boolean onRegister, BigDecimal transactionFee) {
        super(guid, createTime, operatorGuid, shiftGuid, customerGuid, discount, discountType, orderStatus, holdName, definedOnHoldGuid, holdPhone, holdStatus, taxable, tmpTotalPrice, tmpTotalTax, tmpTotalDiscount, printSeqNum, registerId, parentId, type, isTipped, operatorName, registerTitle, customerName, customerPhone, customerEmail, tipsAmount, kitchenPrintStatus, kdsSendStatus, onRegister, transactionFee);
        this.transactionsState = transactionsState;
        this.tenderType = tenderType;
    }

    public enum TransactionsState {
        NA, OPEN, CLOSED
    }

    public enum TenderType {
        CASH(R.string.order_tender_type_cash),
        CREDIT_CARD(R.string.order_tender_type_credit_card),
        DEBIT_CARD(R.string.order_tender_type_debit_card),
        EBT(R.string.order_tender_type_ebt),
        OTHER(R.string.order_tender_type_other),
        MULTIPLE(R.string.order_tender_type_multiple);

        public final int label;

        TenderType(int label) {
            this.label = label;
        }
    }

}
