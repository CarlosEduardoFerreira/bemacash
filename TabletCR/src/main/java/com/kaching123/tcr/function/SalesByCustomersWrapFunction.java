package com.kaching123.tcr.function;

import android.database.Cursor;

import com.google.common.base.Function;
import com.kaching123.tcr.model.SaleOrderViewModel;
import com.kaching123.tcr.model.SalesByCustomerModel;
import com.kaching123.tcr.model.converter.SaleOrderViewFunction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by pkabakov on 18.02.14.
 */
public class SalesByCustomersWrapFunction implements Function<Cursor, List<SalesByCustomerModel>> {

    private final SaleOrderViewFunction saleOrderViewFunction = new SaleOrderViewFunction();

    @Override
    public List<SalesByCustomerModel> apply(Cursor c) {
        if (!c.moveToFirst())
            return new ArrayList<SalesByCustomerModel>();

        LinkedHashMap<String, SalesByCustomerModel> salesByCustomerMap = new LinkedHashMap<String, SalesByCustomerModel>();
        do {
            SaleOrderViewModel saleOrderViewModel =  saleOrderViewFunction.apply(c);

            SalesByCustomerModel salesByCustomerModel = salesByCustomerMap.get(saleOrderViewModel.customerGuid);
            if (salesByCustomerModel == null) {
                salesByCustomerModel = new SalesByCustomerModel(saleOrderViewModel.customerName, saleOrderViewModel.customerPhone, saleOrderViewModel.customerEmail);
                salesByCustomerMap.put(saleOrderViewModel.customerGuid, salesByCustomerModel);
            }
            salesByCustomerModel.totalAmount = salesByCustomerModel.totalAmount.add(saleOrderViewModel.tmpTotalPrice);
        } while (c.moveToNext());

        return new ArrayList<SalesByCustomerModel>(salesByCustomerMap.values());
    }

}
