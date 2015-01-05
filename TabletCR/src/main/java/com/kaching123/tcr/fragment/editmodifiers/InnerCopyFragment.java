package com.kaching123.tcr.fragment.editmodifiers;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.converter.ModifierFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ModifierTable;

import java.util.HashSet;
import java.util.List;

/**
 * Created by vkompaniets on 12.12.13.
 */
@EFragment (R.layout.editmodifiers_copymodifier_inner_fragment)
public class InnerCopyFragment extends Fragment implements LoaderCallbacks<List<ModifierModel>>{

    private static final Uri MODIFIER_URI = ShopProvider.getContentUri(ModifierTable.URI_CONTENT);

    @ViewById
    protected TextView typeTitle;

    @ViewById
    protected ListView list;

    @ViewById
    protected ImageView checkboxAll;

    @FragmentArg
    protected String itemGuid;

    @FragmentArg
    protected ModifierType type;

    private InnerFragmentAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new InnerFragmentAdapter(getActivity());
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                adapter.itemClicked(position);
                setSelectAll(adapter.isAllSelected());
            }
        });
        setTypeTitle();
        getLoaderManager().initLoader(type.ordinal(), null, this);
    }

    @Click
    protected void checkboxAllClicked(){
        Boolean value = Boolean.TRUE.equals(checkboxAll.getTag()) ? Boolean.FALSE : Boolean.TRUE;
        adapter.setSelectAll(value);
        checkboxAll.setTag(value);
        setSelectAll(value);
    }

    private void setSelectAll(boolean selected){
        checkboxAll.setActivated(selected);
    }

    private void setTypeTitle() {
        switch (type){
            case MODIFIER:
                typeTitle.setText(R.string.modifiers_copy_dialog_modifiers);
                break;
            case ADDON:
                typeTitle.setText(R.string.modifiers_copy_dialog_addons);
                break;
            case OPTIONAL:
                typeTitle.setText(R.string.modifiers_copy_dialog_options);
                break;
        }
    }

    @Override
    public Loader<List<ModifierModel>> onCreateLoader(int i, Bundle bundle) {
        return CursorLoaderBuilder.forUri(MODIFIER_URI)
                .where(ModifierTable.ITEM_GUID + " = ?", itemGuid)
                .where(ModifierTable.TYPE + "= ?", type.ordinal())
                .orderBy(ModifierTable.TITLE)
                .transform(new ModifierFunction()).build(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<ModifierModel>> listLoader, List<ModifierModel> modifierModels) {
        adapter.changeCursor(modifierModels);
    }

    @Override
    public void onLoaderReset(Loader<List<ModifierModel>> listLoader) {
        adapter.changeCursor(null);
    }

    public HashSet<String> getSelectedItems(){
        return adapter.getSelectedItems();
    }

}
