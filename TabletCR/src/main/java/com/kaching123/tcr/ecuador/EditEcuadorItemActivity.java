package com.kaching123.tcr.ecuador;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.EditItemActivity;
import com.kaching123.tcr.fragment.taxgroup.ChooseTaxGroupsDialog;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.store.ShopStore.TaxGroupTable;

import org.androidannotations.annotations.EActivity;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by idyuzheva on 16.03.2016.
 */
@EActivity(R.layout.inventory_item_activity)
public class EditEcuadorItemActivity extends EditItemActivity {

    public static void start(Context context, ItemExModel model) {
        EditEcuadorItemActivity_.intent(context).model(model).start();
    }

    protected Loader<Cursor> onCreateTax2Loader() {
        if (TextUtils.isEmpty(model.taxGroupGuid) && !TextUtils.isEmpty(model.taxGroupGuid2)) {
            return CursorLoaderBuilder.forUri(TAX_GROUP_URI)
                    .where(TaxGroupTable.GUID + " = ? ", model.taxGroupGuid2)
                    .build(this);
        }
        return null;
    }

    @Override
    protected Loader<Cursor> onCreateTaxLoader() {
        if (!TextUtils.isEmpty(model.taxGroupGuid)) {
            if (!TextUtils.isEmpty(model.taxGroupGuid2)) {
                return CursorLoaderBuilder.forUri(TAX_GROUP_URI)
                        .where(TaxGroupTable.GUID + " = ? OR " + TaxGroupTable.GUID + " = ?",
                                model.taxGroupGuid, model.taxGroupGuid2)
                        .build(this);
            } else
                return CursorLoaderBuilder.forUri(TAX_GROUP_URI)
                        .where(TaxGroupTable.GUID + " = ?", model.taxGroupGuid)
                        .build(this);
        } else if (TextUtils.isEmpty(model.taxGroupGuid2)) {
            taxGroupDefault.setText(TaxHelper.getItemTaxGroupDisplayText(null));
            return null;
        } else return null;
    }

    @Override
    protected void initTaxes() {
        taxGroupDefault.setVisibility(View.VISIBLE);
        taxGroupDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hasStoreTaxOnly = TextUtils.isEmpty(model.taxGroupGuid) && TextUtils.isEmpty(model.taxGroupGuid2);
                ChooseTaxGroupsDialog.show(EditEcuadorItemActivity.this, model.taxGroupGuid,
                        model.taxGroupGuid2, hasStoreTaxOnly, new ChooseTaxGroupsDialog.ChooseTaxCallback() {
                            @Override
                            public void onTaxGroupsChosen(TaxGroupModel model1, TaxGroupModel model2) {
                                if (model1 != null) {
                                    model.taxGroupGuid = model1.guid;
                                }
                                String displayText = "(" + _decimal(model1.tax) + " %) " + model1.title;
                                if (model2 != null) {
                                    model.taxGroupGuid2 = model2.guid;
                                    displayText += "\n" + "(" + _decimal(model2.tax) + " %) " + model2.title;
                                } else {
                                    model.taxGroupGuid2 = null;
                                }
                                taxGroupDefault.setText(displayText);
                            }
                        });
            }
        });
        taxGroup.setVisibility(View.GONE);
    }

    @Override
    protected void onTaxLoaded(Cursor cursor) {
        if (cursor.moveToFirst()) {
            model.taxGroupGuid = new TaxGroupModel(cursor).guid;
            if (cursor.moveToNext()) {
                model.taxGroupGuid2 = new TaxGroupModel(cursor).guid;
            }
        }
        taxGroupDefault.setText(TaxHelper.getItemTaxGroupDisplayText(cursor));
    }

    protected void onTax2Loaded(Cursor cursor) {
        String displayText = "(" + ContentValuesUtil._decimal(TcrApplication.get().getShopInfo().taxVat) + " %) "
                + TcrApplication.get().getString(R.string.item_tax_group_default);
        if (cursor.moveToFirst()) {
            TaxGroupModel model = new TaxGroupModel(cursor);
            displayText += "\n(" + _decimal(model.tax) + " %) " + model.title;
        }
        taxGroupDefault.setText(displayText);
    }

    @Override
    protected void adjustTaxGroup() {
        if (!salableChBox.isChecked()) {
            model.taxGroupGuid = null;
            model.taxGroupGuid2 = null;
        }
    }

    @Override
    protected void onTaxLoaderReset() {
        taxGroupDefault.setText("");
    }

    @Override
    protected void onTax2LoaderReset() {
        taxGroupDefault.setText("");
    }
}
