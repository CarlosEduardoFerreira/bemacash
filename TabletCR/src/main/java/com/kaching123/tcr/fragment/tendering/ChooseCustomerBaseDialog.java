package com.kaching123.tcr.fragment.tendering;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.EditCustomerActivity;
import com.kaching123.tcr.activity.SuperBaseActivity.BaseTempLoginListener;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleOrderCustomerCommand;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleOrderCustomerCommand.BaseUpdateOrderCustomerCallback;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.user.PermissionFragment;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.PrepaidReleaseResult;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CustomerTable;
import com.kaching123.tcr.websvc.api.prepaid.IVULotoDataResponse;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

/**
 * Created by gdubina on 03/03/14.
 */
@EFragment
public abstract class ChooseCustomerBaseDialog extends StyledDialogFragment implements LoaderCallbacks<Cursor> {

    private static final Uri CUSTOMERS_URI = ShopProvider.getContentUri(CustomerTable.URI_CONTENT);

    private static final int ADD_CUSTOMER_REQUEST_CODE = 0x0100;

    protected emailSenderListener listener;

    @ViewById(android.R.id.list)
    protected ListView listView;

    @ViewById
    protected EditText customerFilter;

    @ViewById(android.R.id.empty)
    protected TextView emptyView;

    protected ResourceCursorAdapter adapter;

    @FragmentArg
    protected ArrayList<PaymentTransactionModel> transactions;

    @FragmentArg
    protected String amount;

    @FragmentArg
    protected boolean isGiftCard;

    @FragmentArg
    protected String orderGuid;

    @FragmentArg
    protected ArrayList<PrepaidReleaseResult> releaseResultList;

    @FragmentArg
    protected boolean IVULotoActivated;

    @FragmentArg
    protected IVULotoDataResponse ivuLotoDataResponse;

    @InstanceState
    protected int positionClicked;

    protected String email;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.tendering_choose_customer_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.tendering_choose_customer_dialog_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected boolean hasPositiveButton() {
        return false;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_ok;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }

    public interface emailSenderListener {
        void onComplete();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources()
                        .getDimensionPixelOffset(R.dimen.holdon_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth));
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setupAdapter();
        getLoaderManager().restartLoader(0, null, this);
    }

    protected void setupAdapter() {
        listView.setAdapter(adapter = new CustomerAdapter(getActivity()));
        listView.setEmptyView(emptyView);
    }

    protected ChooseCustomerBaseDialog setListener(emailSenderListener listener) {
        this.listener = listener;
        return this;
    }

    @ItemClick(android.R.id.list)
    protected void listViewItemClicked(int pos) {
        positionClicked = pos;
        Cursor c = (Cursor) adapter.getItem(pos);
        String guid = c.getString(c.getColumnIndex(CustomerTable.GUID));
        String email = c.getString(c.getColumnIndex(CustomerTable.EMAIL));
        if (!UiHelper.isValidEmail(email)) {
            AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(R.string.customer_email_invalid_msg));
            return;
        }
        this.email = email;
        UpdateSaleOrderCustomerCommand.start(getActivity(), getCurrentOrderGuid(), guid, isGiftCard, updateOrderCustomerCallback);
    }

    protected String getCurrentOrderGuid() {
        return orderGuid;
    }

    protected abstract void sendDigitalOrder(String email);

    protected abstract void onCustomerPicked(CustomerModel customer);

    protected void onOrderAdded(String orderGuid){

    };

    protected abstract void sendDigitalOrderForGiftCard(String email, String amount);

    @AfterTextChange
    protected void customerFilterAfterTextChanged(Editable s) {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(CUSTOMERS_URI)
                .projection("0 as _id, *");

        String filter = customerFilter.getText().toString();
        if (!TextUtils.isEmpty(filter)) {
            filter = "%" + filter + "%";
            builder.where(CustomerTable.FISRT_NAME + " LIKE ? OR "
                            + CustomerTable.LAST_NAME + " LIKE ? OR "
                            + CustomerTable.EMAIL + " LIKE ? OR "
                            + CustomerTable.PHONE + " LIKE ? OR "
                            + CustomerTable.CUSTOMER_IDENTIFICATION + " LIKE ? OR "
                            + CustomerTable.LOYALTY_BARCODE + " = ?",
                    filter, filter, filter, filter, filter, customerFilter.getText().toString());
        }
        return builder.build(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
//        emptyView.setVisibility(cursor == null || cursor.getCount() == 0 ? View.VISIBLE : View.GONE);
        adapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.changeCursor(null);
    }

    @Click
    protected void btnAddClicked() {
        if (!getApp().hasPermission(Permission.CUSTOMER_MANAGEMENT)) {
            PermissionFragment.showCancelable(getActivity(), new BaseTempLoginListener(getActivity()) {
                @Override
                public void onLoginComplete() {
                    super.onLoginComplete();
                    btnAddClicked();
                }
            }, Permission.CUSTOMER_MANAGEMENT);
            return;
        }
        EditCustomerActivity.startForResult(getActivity(), null, ADD_CUSTOMER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_CUSTOMER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            CustomerModel model = (CustomerModel) data.getSerializableExtra(EditCustomerActivity.EXTRA_CUSTOMER);
            if (!TextUtils.isEmpty(model.email))
                customerFilter.setText(model.email);
            else if (!TextUtils.isEmpty(model.loyaltyBarcode))
                customerFilter.setText(model.loyaltyBarcode);
            else if (!TextUtils.isEmpty(model.lastName))
                customerFilter.setText(model.lastName);
        }
    }

    private BaseUpdateOrderCustomerCallback updateOrderCustomerCallback = new BaseUpdateOrderCustomerCallback() {

        @Override
        protected void onOrderCustomerUpdated(boolean isGiftCard, String amount) {
            if(!isGiftCard)
                sendDigitalOrder(email);
            else
                sendDigitalOrderForGiftCard(email,amount);
            CustomerModel customer = new CustomerModel((Cursor) adapter.getItem(positionClicked));
            onCustomerPicked(customer);
        }

        @Override
        protected void onOrderCustomerUpdateError() {

        }

        @Override
        protected void onOrderAdded(String orderGuid) {
            ChooseCustomerBaseDialog.this.onOrderAdded(orderGuid);
        }
    };

    private class CustomerAdapter extends ResourceCursorAdapter {

        public CustomerAdapter(Context context) {
            super(context, R.layout.tendering_choose_customer_item_view, null, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = super.newView(context, cursor, parent);
            v.setTag(new UIHolder(
                            (TextView) v.findViewById(R.id.name),
                            (TextView) v.findViewById(R.id.email),
                            (TextView) v.findViewById(R.id.phone))
            );
            return v;
        }

        @Override
        public void bindView(View v, Context context, Cursor c) {
            UIHolder holder = (UIHolder) v.getTag();
            holder.name.setText(UiHelper.concatFullname(c.getString(c.getColumnIndex(CustomerTable.FISRT_NAME)), c.getString(c.getColumnIndex(CustomerTable.LAST_NAME))));

            String email = c.getString(c.getColumnIndex(CustomerTable.EMAIL));
            if (UiHelper.isValidEmail(email)) {
                holder.email.setText(email);
                holder.email.setVisibility(View.VISIBLE);
            } else {
                holder.email.setVisibility(View.GONE);
            }

            String phone = c.getString(c.getColumnIndex(CustomerTable.PHONE));
            if (!TextUtils.isEmpty(phone)) {
                holder.phone.setVisibility(View.VISIBLE);
                UiHelper.showPhone(holder.phone, phone);
            } else {
                holder.phone.setVisibility(View.GONE);
            }
        }
    }

    private static class UIHolder {
        TextView name;
        TextView email;
        TextView phone;

        private UIHolder(TextView name, TextView email, TextView phone) {
            this.name = name;
            this.email = email;
            this.phone = phone;
        }
    }
}
