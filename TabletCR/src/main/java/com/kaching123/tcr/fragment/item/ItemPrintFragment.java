package com.kaching123.tcr.fragment.item;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.PrinterAliasActivity.PrinterAliasConverter;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.PrinterAliasModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vkompaniets on 21.07.2016.
 */
@EFragment(R.layout.item_printer_fragment)
public class ItemPrintFragment extends ItemBaseFragment{

    @ViewById protected Spinner kitchen;
    @ViewById protected Spinner kds;

    private PrinterAliasAdapter kitchenAdapter;

    @Override
    protected void setViews() {
        kitchenAdapter = new PrinterAliasAdapter(getActivity());
        kitchen.setAdapter(kitchenAdapter);

        getLoaderManager().restartLoader(0, null, new PrinterAliasLoader());
    }

    @Override
    protected void setModel() {

    }

    @Override
    protected void collectData() {
        final ItemModel model = getModel();
        model.printerAliasGuid = ((PrinterAliasModel) kitchen.getSelectedItem()).guid;
    }

    private class PrinterAliasAdapter extends ObjectsCursorAdapter<PrinterAliasModel> {

        public PrinterAliasAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            return LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_light, parent, false);
        }

        @Override
        protected View bindView(View convertView, int position, PrinterAliasModel item) {
            ((TextView) convertView).setText(item.alias);
            return convertView;
        }

        @Override
        protected View newDropDownView(int position, ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_dropdown_item, parent, false);
            return view;
        }

        public int getPosition(String guid) {
            if (guid == null)
                return 0;

            for (int i = 0; i < getCount(); i++) {
                if (guid.equals(getItem(i).guid))
                    return i;
            }
            return 0;
        }

    }

    private class PrinterAliasLoader implements LoaderCallbacks<List<PrinterAliasModel>> {

        @Override
        public Loader<List<PrinterAliasModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(ShopProvider.contentUri(PrinterAliasTable.URI_CONTENT))
                    .transform(new PrinterAliasConverter())
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<PrinterAliasModel>> listLoader, List<PrinterAliasModel> printerAliasModels) {
            ArrayList<PrinterAliasModel> models = new ArrayList<>(printerAliasModels.size() + 1);
            models.add(new PrinterAliasModel(null, "None"));
            models.addAll(printerAliasModels);
            kitchenAdapter.changeCursor(models);

            if (getModel().printerAliasGuid != null) {
                kitchen.setSelection(kitchenAdapter.getPosition(getModel().printerAliasGuid));
            }
        }

        @Override
        public void onLoaderReset(Loader<List<PrinterAliasModel>> listLoader) {
            kitchenAdapter.changeCursor(null);
        }

    }
}
