package com.kaching123.tcr.fragment.item;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.KDSAliasActivity;
import com.kaching123.tcr.activity.PrinterAliasActivity.PrinterAliasConverter;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.KDSAliasModel;
import com.kaching123.tcr.model.PrinterAliasModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;
import com.thomashaertel.widget.MultiSpinner;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by vkompaniets on 21.07.2016.
 */
@EFragment(R.layout.item_printer_fragment)
public class ItemPrintFragment extends ItemBaseFragment {

    @ViewById
    protected Spinner kitchen;
    @ViewById
    protected MultiSpinner kdsAlias;

    private PrinterAliasAdapter kitchenAdapter;

    protected KDSAliasAdapter kdsAliasAdapter;

    protected boolean[] selectedKds;
    protected static final int KDS_ALIAS_LOADER_ID = 9;
    protected static final Uri KDS_ALIAS_URI = ShopProvider.contentUri(ShopStore.KDSAliasTable.URI_CONTENT);
    private static final Uri KDS_URI = ShopProvider.getContentUri(ShopStore.ItemKDSTable.URI_CONTENT);

    @Override
    protected void newItem(){}

    @Override
    protected void setViews() {
        kitchenAdapter = new PrinterAliasAdapter(getActivity());
        kitchen.setAdapter(kitchenAdapter);

        kdsAliasAdapter = new KDSAliasAdapter(getActivity());
        kdsAlias.setAdapter(kdsAliasAdapter, false, new MultiSpinner.MultiSpinnerListener() {
            @Override
            public void onItemsSelected(boolean[] selected) {
                selectedKds = selected;
            }
        });

        getLoaderManager().restartLoader(0, null, new PrinterAliasLoader());
        getLoaderManager().initLoader(KDS_ALIAS_LOADER_ID, null, new KDSAliasLoader());

    }

    @Override
    protected void setModel() {

    }

    @Override
    public void collectData() {
        final ItemModel model = getModel();
        model.printerAliasGuid = ((PrinterAliasModel) kitchen.getSelectedItem()).guid;
        saveItemKdsAlias(selectedKds);
    }

    protected void saveItemKdsAlias(boolean[] selected) {
        if (selected == null) return;
        final ItemModel model = getModel();
        for (int i = 0; i < selected.length; i++) {
            if (selected[i]) {
                // check if it already exist before saving
                Cursor c = ProviderAction.query(KDS_URI)
                        .where(ShopStore.ItemKDSTable.ITEM_GUID + " = ?", model.guid)
                        .where(ShopStore.ItemKDSTable.KDS_ALIAS_GUID + " = ?", kdsAliasAdapter.getItem(i).guid)
                        .perform(getActivity());
                if (c.moveToFirst()) {
                    c.close();
                    continue;
                }
                ContentValues cv = new ContentValues();
                cv.put(ShopStore.ItemKDSTable.ITEM_GUID, model.guid);
                cv.put(ShopStore.ItemKDSTable.KDS_ALIAS_GUID, kdsAliasAdapter.getItem(i).guid);
                ProviderAction.insert(KDS_URI)
                        .values(cv)
                        .perform(getActivity());
            } else {
                ProviderAction.delete(KDS_URI)
                        .where(ShopStore.ItemKDSTable.ITEM_GUID + " = ?", model.guid)
                        .where(ShopStore.ItemKDSTable.KDS_ALIAS_GUID + " = ?", kdsAliasAdapter.getItem(i).guid)
                        .perform(getActivity());
            }
        }
    }

    private class KDSAliasAdapter extends ObjectsCursorAdapter<KDSAliasModel> {

        public KDSAliasAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            return LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_light, parent, false);
        }

        @Override
        protected View bindView(View convertView, int position, KDSAliasModel item) {
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

    private class KDSAliasLoader implements LoaderCallbacks<List<KDSAliasModel>> {

        @Override
        public Loader<List<KDSAliasModel>> onCreateLoader(int i, Bundle bundle) {
            return CursorLoaderBuilder.forUri(KDS_ALIAS_URI)
                    .transformRow(new KDSAliasActivity.KDSAliasConverter())
                    .build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<KDSAliasModel>> listLoader, List<KDSAliasModel> kdsAliasModels) {
            ArrayList<KDSAliasModel> models = new ArrayList<>(kdsAliasModels.size());
//            models.add(new KDSAliasModel(null, "None"));
            models.addAll(kdsAliasModels);

            kdsAliasAdapter.changeCursor(models);
            final ItemModel model = getModel();
            final String itemGuid = model.guid;
            Cursor c = ProviderAction.query(KDS_URI)
                    .projection(ShopStore.ItemKDSTable.KDS_ALIAS_GUID)
                    .where(ShopStore.ItemKDSTable.ITEM_GUID + " = ?", itemGuid)
                    .perform(getActivity());
            Set<String> kdsAliasGuids = new HashSet<>();
            if (c.moveToFirst()) {
                do {
                    kdsAliasGuids.add(c.getString(0));
                } while (c.moveToNext());
            }
            c.close();
            boolean[] selected = new boolean[models.size()];
//            Arrays.fill(selected, true);
//            selected[0] = false;
            for (int i = 0; i < models.size(); i++) {
                if (kdsAliasGuids.contains(models.get(i).getGuid()))
                    selected[i] = true;
            }
            if (!kdsAliasGuids.isEmpty()) {
                kdsAlias.setSelected(selected);
            }

//            printerAlias.setOnItemSelectedListener(new SpinnerChangeListener(aliasGuid != null ? printerAliasAdapter.getPosition(model.printerAliasGuid) : 0));

        }

        @Override
        public void onLoaderReset(Loader<List<KDSAliasModel>> listLoader) {
            kdsAliasAdapter.changeCursor(null);
        }

    }

    @Override
    public boolean validateData() {
        return true;
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
                    .transformRow(new PrinterAliasConverter())
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
