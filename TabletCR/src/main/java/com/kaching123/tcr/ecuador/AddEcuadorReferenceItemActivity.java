package com.kaching123.tcr.ecuador;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.AddReferenceItemActivity;
import com.kaching123.tcr.fragment.taxgroup.ChooseTaxGroupsDialog;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.store.ShopStore;

import org.androidannotations.annotations.EActivity;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by idyuzheva on 16.03.2016.
 */
@EActivity(R.layout.inventory_reference_item_activity)
public class AddEcuadorReferenceItemActivity extends AddReferenceItemActivity {

    private boolean hasStoreTaxOnly;

    @Override
    protected void initTaxes() {
        taxGroupDefault.setVisibility(View.VISIBLE);
        taxGroupDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseTaxGroupsDialog.show(AddEcuadorReferenceItemActivity.this, model.taxGroupGuid,
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
                                hasStoreTaxOnly = TextUtils.isEmpty(model.taxGroupGuid) && TextUtils.isEmpty(model.taxGroupGuid2);
                            }
                        });
            }
        });
        taxGroup.setVisibility(View.GONE);
    }

    @Override
    protected Loader<Cursor> onCreateTaxLoader() {
        return CursorLoaderBuilder.forUri(TAX_GROUP_URI)
                .where(ShopStore.TaxGroupTable.IS_DEFAULT + " = ?", 1)
                .build(this);
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

    @Override
    protected void onTaxLoaderReset() {
        taxGroupDefault.setText("");
    }

    @Override
    protected void adjustTaxGroup() {
    }

    public static void start(Context context, String barcode) {
        ItemExModel item = new ItemExModel();
        item.tmpBarcode = barcode;
        start(context, item);
    }

    public static void start(Context context, ItemExModel item) {
        AddEcuadorReferenceItemActivity_.intent(context).model(item).start();
    }
}