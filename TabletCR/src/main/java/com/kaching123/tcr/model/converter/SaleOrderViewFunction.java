package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.kaching123.tcr.model.SaleOrderViewModel;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.CustomerTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.OperatorTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.RegisterTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.TipsTable;
import com.kaching123.tcr.store.ShopStore;

import java.util.Date;

import static com.kaching123.tcr.fragment.UiHelper.concatFullname;
import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;
import static com.kaching123.tcr.model.ContentValuesUtil._kdsSendStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._kitchenPrintStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._orderStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._orderType;

/**
 * Created by gdubina on 27/12/13.
 */
public class SaleOrderViewFunction extends ListConverterFunction<SaleOrderViewModel> {

    @Override
    public SaleOrderViewModel apply(Cursor c) {
        super.apply(c);
        return new SaleOrderViewModel(
                c.getString(indexHolder.get(SaleOrderTable.GUID)),
                new Date(c.getLong(indexHolder.get(SaleOrderTable.CREATE_TIME))),
                c.getString(indexHolder.get(SaleOrderTable.OPERATOR_GUID)),
                c.getString(indexHolder.get(SaleOrderTable.SHIFT_GUID)),
                c.getString(indexHolder.get(SaleOrderTable.CUSTOMER_GUID)),
                _decimal(c, indexHolder.get(SaleOrderTable.DISCOUNT)),
                _discountType(c, indexHolder.get(SaleOrderTable.DISCOUNT_TYPE)),
                _orderStatus(c, indexHolder.get(SaleOrderTable.STATUS)),
                c.getString(indexHolder.get(SaleOrderTable.HOLD_NAME)),
                _bool(c, indexHolder.get(SaleOrderTable.TAXABLE)),
                _decimal(c, indexHolder.get(SaleOrderTable.TML_TOTAL_PRICE)),
                _decimal(c, indexHolder.get(SaleOrderTable.TML_TOTAL_TAX)),
                _decimal(c, indexHolder.get(SaleOrderTable.TML_TOTAL_DISCOUNT)),
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
                _decimal(c, indexHolder.get(TipsTable.AMOUNT)),
                _kitchenPrintStatus(c, indexHolder.get(SaleOrderTable.KITCHEN_PRINT_STATUS)),
                _kdsSendStatus(c, c.getColumnIndex(ShopStore.SaleOrderTable.KDS_SEND_STATUS)),
                _decimal(c, indexHolder.get(SaleOrderTable.TRANSACTION_FEE))
        );
    }
}
