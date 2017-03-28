package com.kaching123.tcr.fragment.quickservice;

import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DatePickerFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.converter.ItemExFunction;
import com.kaching123.tcr.store.ShopSchema2;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by mboychenko on 3/28/2017.
 */

@EFragment
public class AgeVerificationFragment extends StyledDialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String DIALOG_NAME = "AgeVerificationFragment";

    @FragmentArg
    protected String orderGuid;

    private OnDialogClickListener listener;
    private ItemExModel itemExModel;
    private int year, month, day;

    @ViewById protected TextView ageVerField;

    public void setListener(OnDialogClickListener listener) {
        this.listener = listener;
    }

    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            AgeVerificationFragment.this.year = year;
            AgeVerificationFragment.this.month = monthOfYear;
            AgeVerificationFragment.this.day = dayOfMonth;

            String day = String.valueOf(dayOfMonth);
            String month = String.valueOf(monthOfYear);
            if (dayOfMonth < 10) {
                day = "0" + String.valueOf(dayOfMonth);
            }
            if (monthOfYear < 10) {
                month = "0" + String.valueOf(monthOfYear);
            }
            ageVerField.setText(String.format(Locale.US, "%s/%s/%d", month, day, year));
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources()
                        .getDimensionPixelOffset(R.dimen.age_ver_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.age_ver_dlg_heigth));
        getLoaderManager().restartLoader(0, null, this);

        ageVerField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment.show(getActivity(), Calendar.getInstance(), dateSetListener);
            }
        });

    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.age_verification_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.age_verification_title;
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
        return listener;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(ItemExFunction.VIEW_URI)
                .projection(ItemExFunction.PROJECTION)
                .where(ShopSchema2.ItemExtView2.ItemTable.GUID + " = ?", orderGuid);

        return builder.build(getContext());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()){
            itemExModel = new ItemExFunction().apply(data);
        }
        data.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public static void show(FragmentActivity context, String orderGuid, OnDialogClickListener listener){
        DialogUtil.show(context, DIALOG_NAME, AgeVerificationFragment_.builder().orderGuid(orderGuid).build()).setListener(listener);
    }
}
