package com.kaching123.tcr.model.payment;

import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 */
public class HistoryDetailedOrderItemModel {

    public SaleOrderItemViewModel saleItemModel;
    public BigDecimal wantedQty;
    public BigDecimal scannedQty;

    public boolean wanted;
    public BigDecimal availableQty;

    public HistoryDetailedOrderItemModel(SaleOrderItemViewModel saleItemModel) {
        this.saleItemModel = saleItemModel;
        SaleOrderItemModel itemModel = saleItemModel.itemModel;
        this.availableQty = itemModel.tmpRefundQty == null ? itemModel.qty : itemModel.qty.add(itemModel.tmpRefundQty);//returned is negative
        if (saleItemModel.isSerializable)
            this.wantedQty = this.availableQty.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.ONE : BigDecimal.ZERO;
        else
            this.wantedQty = this.availableQty;
        this.wanted = !isFinished();
    }

    public boolean isFinished(){
        return availableQty.compareTo(BigDecimal.ZERO) <= 0;
    }
}
