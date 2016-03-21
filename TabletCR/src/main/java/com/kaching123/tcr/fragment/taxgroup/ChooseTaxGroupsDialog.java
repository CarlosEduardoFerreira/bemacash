package com.kaching123.tcr.fragment.taxgroup;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.ResourceCursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment_;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.util.ContentValuesUtilBase._decimal;

/**
 * Created by idyuzheva on 03.03.2016.
 */
@EFragment
public class ChooseTaxGroupsDialog extends StyledDialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String DIALOG_NAME = "ChooseTaxGroupsDialog";

    private static final Uri TAX_GROUP_URI = ShopProvider.contentUri(ShopStore.TaxGroupTable.URI_CONTENT);

    private static final int LOADER_ID = 102;

    @FragmentArg
    protected String modelGuidFirst;

    @FragmentArg
    protected String modelGuidSecond;

    @FragmentArg
    protected boolean hasStoreTaxOnly;

    private List<TaxGroupModel> models = new ArrayList<>(2);

    @ViewById(R.id.list)
    protected ListView listView;

    private ChooseTaxCallback callback;

    private ResourceCursorAdapter adapter;

    private boolean existDefauls = false;

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(TAX_GROUP_URI);
        return builder.build(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            do {
                TaxGroupModel model = new TaxGroupModel(cursor);
                if (model.isDefault) {
                    existDefauls = true;
                    break;
                }
            } while (cursor.moveToNext());
        }
        adapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.changeCursor(null);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.choose_tax_groups_dialog;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.tax_group_choose_tax_goups;
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
    protected boolean hasPositiveButton() {
        return true;
    }

    @AfterViews
    protected void init() {
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources()
                        .getDimensionPixelOffset(R.dimen.default_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth)
        );
        listView.setAdapter(adapter = new TaxGroupsAdapter(getActivity()));
    }

    private class TaxGroupsAdapter extends ResourceCursorAdapter {

        public TaxGroupsAdapter(Context context) {
            super(context, R.layout.tax_group_choose_item_view, null, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            TaxGroupModel taxModel = new TaxGroupModel(cursor);
            View v = super.newView(context, cursor, parent);
            v.setTag(new UIHolder((CheckBox) v.findViewById(R.id.tax_group_item)));
            return v;
        }

        @Override
        public void changeCursor(Cursor cursor) {
            if (cursor == null) {
                super.changeCursor(cursor);
                return;
            }
            MatrixCursor extras = new MatrixCursor(new String[]{ShopStore.TaxGroupTable.ID, ShopStore.TaxGroupTable.GUID, ShopStore.TaxGroupTable.TITLE,
                    ShopStore.TaxGroupTable.TAX, ShopStore.TaxGroupTable.IS_DEFAULT});
            extras.addRow(new String[]{"0", null, getString(R.string.item_tax_group_default), ContentValuesUtil._decimal(getApp().getShopInfo().taxVat), "0"});
            Cursor[] cursors = {extras, cursor};
            Cursor extendedCursor = new MergeCursor(cursors);
            super.changeCursor(extendedCursor);
        }

        @Override
        public void bindView(View v, Context context, Cursor c) {
            final TaxGroupModel taxModel = new TaxGroupModel(c);
            final UIHolder holder = (UIHolder) v.getTag();
            holder.taxGroup.setChecked(false);
            holder.taxGroup.setText("(" + _decimal(taxModel.tax) + "%) " + taxModel.title);
            holder.taxGroup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        models.add(taxModel);
                    } else {
                        models.remove(taxModel);
                    }
                }
            });
            if (isStoreTax(taxModel)) {
                if (TextUtils.isEmpty(modelGuidFirst)) {
                    if (!TextUtils.isEmpty(modelGuidSecond)) {
                        holder.taxGroup.setChecked(true);
                    } else if (!existDefauls || hasStoreTaxOnly) {
                        holder.taxGroup.setChecked(true);
                    }
                }
            } else {
                if (!TextUtils.isEmpty(modelGuidFirst) && taxModel.getGuid().equals(modelGuidFirst)) {
                    holder.taxGroup.setChecked(true);
                }
                if (!TextUtils.isEmpty(modelGuidSecond) && taxModel.getGuid().equals(modelGuidSecond)) {
                    holder.taxGroup.setChecked(true);
                }
                if (TextUtils.isEmpty(modelGuidFirst) && TextUtils.isEmpty(modelGuidSecond) && taxModel.isDefault && !hasStoreTaxOnly) {
                    holder.taxGroup.setChecked(true);
                }
            }
        }
    }

    private boolean isStoreTax(TaxGroupModel model) {
        return TextUtils.isEmpty(model.getGuid());
    }

    private static class UIHolder {
        CheckBox taxGroup;

        private UIHolder(CheckBox taxGroup) {
            this.taxGroup = taxGroup;
        }
    }

    public static void show(FragmentActivity activity, String modelId1, String modelId2, boolean hasStoreTaxOnly, ChooseTaxCallback callback) {
        DialogUtil.show(activity, DIALOG_NAME, ChooseTaxGroupsDialog_
                .builder()
                .modelGuidFirst(modelId1)
                .modelGuidSecond(modelId2)
                .hasStoreTaxOnly(hasStoreTaxOnly)
                .build()
                .setCallback(callback));
    }

    protected ChooseTaxGroupsDialog setCallback(ChooseTaxCallback callback) {
        this.callback = callback;
        return this;
    }

    public interface ChooseTaxCallback {
        void onTaxGroupsChosen(TaxGroupModel model1, TaxGroupModel model2);
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (models.size() == 0) {
                    AlertDialogFragment_.showAlert(getActivity(), R.string.warning_dialog_title, getString(R.string.tax_group_dialog_msg));
                } else if (models.size() == 1) {
                    callback.onTaxGroupsChosen(models.get(0), null);
                    dismiss();
                    return true;
                } else if (models.size() == 2) {
                    if (isStoreTax(models.get(1))) {
                        callback.onTaxGroupsChosen(models.get(1), models.get(0));
                    } else {
                        callback.onTaxGroupsChosen(models.get(0), models.get(1));
                    }
                    dismiss();
                    return true;
                } else if (models.size() > 2) {
                    AlertDialogFragment_.showAlert(getActivity(), R.string.warning_dialog_title, getString(R.string.tax_group_dialog_msg));
                }
                return false;
            }
        };
    }
}