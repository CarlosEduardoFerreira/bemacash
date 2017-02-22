package com.kaching123.tcr.model;

import com.kaching123.tcr.commands.print.digital.PrintOrderToKdsCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.KitchenPrintStatus;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by gdubina on 27.12.13.
 */
public class SaleOrderViewModel extends SaleOrderModel{

    public String operatorName;
    public String registerTitle;
    public String customerName;
    public String customerPhone;
    public String customerEmail;
    public BigDecimal tipsAmount;

    public SaleOrderViewModel(String guid, Date createTime, String operatorGuid, String shiftGuid, String customerGuid, BigDecimal discount, DiscountType discountType, OrderStatus orderStatus, String holdName, boolean taxable,
                              BigDecimal tmpTotalPrice, BigDecimal tmpTotalTax, BigDecimal tmpTotalDiscount,
                              int printSeqNum, long registerId, String parentId, OrderType type, boolean isTipped, String operatorName, String registerTitle,
                              String customerName, String customerPhone, String customerEmail, BigDecimal tipsAmount, KitchenPrintStatus kitchenPrintStatus, PrintOrderToKdsCommand.KDSSendStatus kdsSendStatus, BigDecimal transactionFee) {
        super(guid, createTime, operatorGuid, shiftGuid, customerGuid, discount, discountType, orderStatus, holdName, taxable, tmpTotalPrice, tmpTotalTax, tmpTotalDiscount, printSeqNum, registerId, parentId, type, isTipped, kitchenPrintStatus, kdsSendStatus, transactionFee, null);
        this.operatorName = operatorName;
        this.registerTitle = registerTitle;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.tipsAmount = tipsAmount;
    }
}
