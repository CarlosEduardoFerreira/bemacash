package com.kaching123.tcr.fragment.tendering;

import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.wireless.BarcodeReceiver;
import com.kaching123.tcr.model.CustomerModel;

import org.androidannotations.annotations.EFragment;

/**
 * Created by vkompaniets on 04.07.2016.
 */
@EFragment
public class ChooseCustomerDialog extends ChooseCustomerBaseDialog implements BarcodeReceiver {

    private static final String DIALOG_NAME = ChooseCustomerDialog.class.getSimpleName();

    private CustomerPickListener pickListener;

    public void setPickListener(CustomerPickListener pickListener) {
        this.pickListener = pickListener;
    }

    @Override
    public void onBarcodeReceived(String barcode) {
        customerFilter.setText(barcode);
    }

    @Override
    protected void onCustomerPicked(CustomerModel customer) {
        if (pickListener != null)
            pickListener.onCustomerPicked(customer);
        dismiss();
    }

    public static void show(FragmentActivity activity, String orderGuid, CustomerPickListener listener){
        DialogUtil.show(activity, DIALOG_NAME, ChooseCustomerDialog_.builder().orderGuid(orderGuid).build()).setPickListener(listener);
    }

    public interface CustomerPickListener{
        void onCustomerPicked(CustomerModel customer);
    }
}
