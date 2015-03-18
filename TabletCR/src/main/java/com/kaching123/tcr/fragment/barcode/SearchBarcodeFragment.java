package com.kaching123.tcr.fragment.barcode;

import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.edit.KeyboardDialogFragment;
import com.kaching123.tcr.model.ItemExModel;

import java.math.BigDecimal;

/**
 * Created by gdubina on 22/11/13.
 */
@EFragment
public class SearchBarcodeFragment extends KeyboardDialogFragment{

    //private static final String ARG_LOADER_BARCODE = "ARG_LOADER_BARCODE";

    private static final String DIALOG_NAME = "searchBarcodeFragment";

    @ViewById
    protected CustomEditBox editText;

    @ViewById
    protected View progressBar;
    private OnSearchListener listener;

    @AfterViews
    protected void attachViews() {
        enablePositiveButtons(false);
        editText.setKeyboardSupportConteiner(this);
        keyboard.setDotEnabled(false);
        keyboard.attachEditView(editText);
        editText.setEditListener(new CustomEditBox.IEditListener() {
            @Override
            public boolean onChanged(String text) {
                callInternalListener(submitListener);
                return false;
            }
        });
    }

    @AfterTextChange
    protected void editTextAfterTextChanged(Editable s) {
        String value = validateForm();
        enablePositiveButtons(value != null);
    }

    private boolean onSubmitForm() {
        String value = validateForm();
        if (value == null)
            return false;
        searchBarcode(value);
        return true;
    }

    private String validateForm() {
        String value = editText.getText().toString();
        return !TextUtils.isEmpty(value) && (value.length() >= TcrApplication.BARCODE_MIN_LEN || value.length() <= TcrApplication.BARCODE_MAX_LEN) ? value : null;
    }

    private void searchBarcode(final String barcode) {
        new SearchBarcodeLoader(getActivity(), 0, barcode){
            @Override
            protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(ItemExModel itemExModel, BigDecimal price, BigDecimal quantity) {
                progressBar.setVisibility(View.INVISIBLE);
                if (listener != null) {
                    listener.onBarcodeSearch(itemExModel, barcode, price, quantity);
                }
                dismiss();
            }
        }.execute();
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.search_barcode_dialog;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.dlg_barcode_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_confirm;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return submitListener;
    }

    public void setListener(OnSearchListener listener) {
        this.listener = listener;
    }

    private OnDialogClickListener submitListener = new OnDialogClickListener() {
        @Override
        public boolean onClick() {
            return onSubmitForm();
        }
    };

    public static interface OnSearchListener {
        void onBarcodeSearch(ItemExModel item, String barcode, BigDecimal price, BigDecimal quantity);
    }

    public static void show(FragmentActivity activity, OnSearchListener onSearchListener) {
        DialogUtil.show(activity, DIALOG_NAME, SearchBarcodeFragment_.builder().build()).setListener(onSearchListener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
