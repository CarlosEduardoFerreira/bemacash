package com.kaching123.tcr.fragment.tendering.payment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.PaymentTransactionModel;

import java.util.ArrayList;
import java.util.List;

@EFragment
public class CloseTransactionsFragmentDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = CloseTransactionsFragmentDialog.class.getSimpleName();

    @ViewById
    protected ListView list;

    @FragmentArg
    protected ArrayList<PaymentTransactionModel> transactions;

    private CloseTransactionsListener listener;

    private PreauthTransactionsAdapter adapter;

    @AfterViews
    protected void init() {
        adapter = new PreauthTransactionsAdapter(getActivity(), transactions);

        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                if (listener != null) {
                    listener.onTransactionSelected(adapter.getItem(pos));
                }
                hide(getActivity());
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.base_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth));
        setCancelable(false);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.close_transactions_list_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.close_transactions_list_fragment_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_abort;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return 0;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }

    @Override
    protected boolean hasNegativeButton() {
        return true;
    }

    @Override
    protected boolean hasPositiveButton() {
        return false;
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (listener != null) {
                    listener.onCancel();
                }
                return true;
            }
        };
    }

    public CloseTransactionsFragmentDialog setListener(CloseTransactionsListener listener) {
        this.listener = listener;
        return this;
    }

    private static class PreauthTransactionsAdapter extends ArrayAdapter<PaymentTransactionModel> {

        public PreauthTransactionsAdapter(Context context, List<PaymentTransactionModel> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = newView(parent, position);
            bindView(parent, convertView, position);
            return convertView;
        }

        private View newView(ViewGroup parent, int position) {
            return CloseTransactionView_.build(getContext());
        }

        private void bindView(ViewGroup parent, View view, int position) {
            ((CloseTransactionView)view).bind(position, getItem(position));
        }

    }

    public static void show(FragmentActivity context, ArrayList<PaymentTransactionModel> transactions, CloseTransactionsListener listener) {
       DialogUtil.show(context, DIALOG_NAME, CloseTransactionsFragmentDialog_.builder().transactions(transactions).build())
                .setListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public interface CloseTransactionsListener {

        void onCancel();

        void onTransactionSelected(PaymentTransactionModel transaction);
    }
}
