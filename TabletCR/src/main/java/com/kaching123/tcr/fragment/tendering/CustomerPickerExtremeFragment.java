package com.kaching123.tcr.fragment.tendering;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleOrderCustomerCommand;
import com.kaching123.tcr.commands.store.user.AddCustomerCommand;
import com.kaching123.tcr.commands.store.user.DeleteCustomerCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.function.OrderTotalPriceLoaderCallback;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.DiscountType;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Created by irikhmayer on 06.08.2014.
 */
@EFragment
public class CustomerPickerExtremeFragment extends ChooseCustomerBaseDialog {

    private static final String DIALOG_NAME = CustomerPickerExtremeFragment.class.getSimpleName();
    public static final int LOADER_ITEMS = 1;
    public static final BigDecimal TRESHOLD = new BigDecimal(10000);

    protected ExtremeCallback callback;
    protected boolean threshold;

    public CustomerPickerExtremeFragment setCallback(ExtremeCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
    }

    protected void init() {
        loadOrderData();
        setCancelable(false);
    }

    @Override
    protected Builder build(Builder builder) {
        String customMessage = TcrApplication.get().getShopPref().customerPopupScreenMessage().get();
        if (TextUtils.isEmpty(customMessage)) {
            return super.build(builder).setTitle(getString(R.string.tendering_choose_customer_dialog_title));
        } else {
            Spannable text = new SpannableString(customMessage + "\n" + getString(R.string.tendering_choose_customer_dialog_title));
            text.setSpan(new ForegroundColorSpan(Color.RED), 0, customMessage.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return super.build(builder).setTitle(text);
        }
    }

    public static void show(FragmentActivity context, String orderGuid, ExtremeCallback callback) {
        DialogUtil.show(context, DIALOG_NAME, CustomerPickerExtremeFragment_.builder()
                .orderGuid(orderGuid)
                .build()
                .setCallback(callback));
    }

    @Override
    protected boolean hasPositiveButton() {
        return true;
    }

    public static void hide(FragmentActivity context) {
        DialogUtil.hide(context, DIALOG_NAME);
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_skip;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {

            @Override
            public boolean onClick() {
                callback.onChosen(null);
                return false;
            }
        };
    }

    protected void setupAdapter() {
        listView.setAdapter(adapter = new CustomerAdapter(getActivity()));
    }

    @ItemClick(android.R.id.list)
    protected void listViewItemClicked(int pos) {
        Cursor c = (Cursor) adapter.getItem(pos);
        CustomerModel model = new CustomerModel(c);
        String guid = model.guid;
        String email = model.email;
        if (!threshold || !TextUtils.isEmpty(model.customerIdentification)) {
            UpdateSaleOrderCustomerCommand.start(getActivity(), getCurrentOrderGuid(), guid, null);
            callback.onChosen(email);
        } else {
            Toast.makeText(getActivity(), getString(R.string.user_no_cpf), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void sendDigitalOrder(String email) {
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {

            @Override
            public boolean onClick() {
                callback.onCancel();
                return false;
            }
        };
    }

    @UiThread
    protected void proceedToSelected(String cpfValue, String cnpjValue, CustomerModel customer) {

        if (customer == null) {
            customer = new CustomerModel(UUID.randomUUID().toString(), new Date());
            customer.customerIdentification = cpfValue;
            AddCustomerCommand.start(getActivity(), null, customer);
            DeleteCustomerCommand.start(getActivity(), customer, null);
        }
        UpdateSaleOrderCustomerCommand.start(getActivity(), getCurrentOrderGuid(), customer.guid, null);
        callback.onChosen(customer.email);
        WaitDialogFragment.hide(getActivity());
    }

    protected void loadOrderData() {
        if (TextUtils.isEmpty(orderGuid)) {
            getLoaderManager().destroyLoader(LOADER_ITEMS);
            return;
        }
        getLoaderManager().restartLoader(LOADER_ITEMS, null, new OrderTotalPriceLoaderCallback(getActivity(), orderGuid) {

            @Override
            public void onZeroValue() {
            }

            @Override
            public void onCalcTotal(boolean isTaxableOrder, BigDecimal orderDiscount, DiscountType orderDiscountType,
                                    BigDecimal orderDiscountVal, BigDecimal totalItemTotal,
                                    BigDecimal totalTaxVatValue, BigDecimal totalItemDiscount,
                                    BigDecimal totalOrderPrice, BigDecimal availableDiscount, BigDecimal transactionFee) {
                if (threshold = (totalOrderPrice != null && totalOrderPrice.compareTo(TRESHOLD) >= 0)) {
                    getNeutralButton().setEnabled(false);
                    getNeutralButton().setTextColor(getResources().getColor(R.color.gray_dark));
                }
            }
        });
    }


    private class CustomerAdapter extends ResourceCursorAdapter {

        public CustomerAdapter(Context context) {
            super(context, R.layout.cpf_choose_customer_item_view, null, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = super.newView(context, cursor, parent);
            v.setTag(new UIHolder(
                    (TextView) v.findViewById(R.id.name),
                    (TextView) v.findViewById(R.id.email),
                    (TextView) v.findViewById(R.id.cpf)));
            return v;
        }

        @Override
        public void bindView(View v, Context context, Cursor c) {
            UIHolder holder = (UIHolder) v.getTag();
            CustomerModel model = new CustomerModel(c);
            holder.name.setText(model.getFullName());
            String cpf = model.customerIdentification;
            StringBuilder sb = null;
            if (!TextUtils.isEmpty(cpf)) {
                sb = new StringBuilder(cpf);
            }
            if (sb != null) {
                holder.cpf.setText(sb.toString());
            } else {
                holder.cpf.setText("");
            }
        }
    }

    private static class UIHolder {
        TextView name;
        TextView email;
        TextView cpf;

        private UIHolder(TextView name, TextView email, TextView cpf) {
            this.name = name;
            this.email = email;
            this.cpf = cpf;
        }
    }

    public interface ExtremeCallback {
        void onCancel();

        void onChosen(String email);
    }
}