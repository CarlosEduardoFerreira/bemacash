package com.kaching123.tcr.fragment.editmodifiers;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.StringRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsArrayAdapter;
import com.kaching123.tcr.commands.store.inventory.DeleteModifierCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelListener;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.editmodifiers.EditDialog.ActionType;
import com.kaching123.tcr.fragment.editmodifiers.EditDialog.OnEditListener;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.converter.ModifierFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ModifierTable;
import com.mobeta.android.dslv.DragSortListView;

import java.util.List;
import java.util.Locale;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by vkompaniets on 09.12.13.
 */
@EFragment(R.layout.editmodifiers_list_fragment)
public class EditModifiersFragment extends Fragment implements LoaderCallbacks<List<ModifierModel>> {

    protected static final Uri URI_MODIFIERS = ShopProvider.getContentUri(ModifierTable.URI_CONTENT);

    @ViewById
    protected TextView containerTitle;

    @ViewById
    protected DragSortListView list;

    private String itemGuid;

    private OnEditListener editListener;

    private String defaultModifierGuid;

    private ModifierType type;

    private Adapter adapter;

    @StringRes(R.string.inventory_modifiers_list_default_tpl)
    protected String defaultFormatString;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new Adapter(getActivity());
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                EditDialog.show(getActivity(), defaultModifierGuid, adapter.getItem(i), ActionType.EDIT, editListener);
            }
        });
    }

    @Override
    public Loader<List<ModifierModel>> onCreateLoader(int i, Bundle bundle) {
        return CursorLoaderBuilder.forUri(URI_MODIFIERS)
                .where(ModifierTable.ITEM_GUID + " = ?", itemGuid)
                .where(ModifierTable.TYPE + " = ?", type.ordinal())
                .transform(new ModifierFunction()).build(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<ModifierModel>> modifierModelLoader, List<ModifierModel> modifierModels) {
        adapter.changeCursor(modifierModels);
        setContainerTitle(modifierModels.size());
    }

    @Override
    public void onLoaderReset(Loader<List<ModifierModel>> modifierModelLoader) {
        adapter.changeCursor(null);
        setContainerTitle(0);
    }

    private void setContainerTitle(int count) {
        if (this.type == null)
            return;

        String title = null;
        switch (this.type) {
            case MODIFIER:
                title = getString(R.string.modifiers_list_title);
                break;
            case ADDON:
                title = getString(R.string.addons_list_title);
                break;
            case OPTIONAL:
                title = getString(R.string.options_list_title);
                break;
        }
        containerTitle.setText(String.format(title, count));
    }

    public void setArgs(String itemGuid, ModifierType type, OnEditListener editListener) {
        this.editListener = editListener;
        this.itemGuid = itemGuid;
        this.type = type;
        getLoaderManager().restartLoader(0, null, this);
    }

    public void setDefaultModifierGuid(String defaultModifierGuid) {
        this.defaultModifierGuid = defaultModifierGuid;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void handleRemove(final ModifierModel model) {
        if (type == null || model == null) {
            return;
        }

        int titleId = 0;
        String message = null;
        switch (type) {
            case MODIFIER:
                titleId = R.string.modifiers_delete_modifier_dialog_title;
                message = String.format(Locale.US, getString(R.string.modifiers_delete_modifier_dialog_message), model.title);
                break;
            case ADDON:
                titleId = R.string.modifiers_delete_addon_dialog_title;
                message = String.format(Locale.US, getString(R.string.modifiers_delete_addon_dialog_message), model.title);
                break;
            case OPTIONAL:
                titleId = R.string.modifiers_delete_option_dialog_title;
                message = String.format(Locale.US, getString(R.string.modifiers_delete_option_dialog_message), model.title);
                break;
        }

        AlertDialogWithCancelListener.show(getActivity(), titleId, message, R.string.btn_confirm, new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                DeleteModifierCommand.start(getActivity(), model);
                return true;
            }
        }, adapter);
    }

    private class Adapter extends ObjectsArrayAdapter<ModifierModel> implements DragSortListView.RemoveListener {

        public Adapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View convertView = LayoutInflater.from(getContext()).inflate(R.layout.editmodifiers_modifier_list_item_view, parent, false);
            assert convertView != null;

            ViewHolder holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.price = (TextView) convertView.findViewById(R.id.price);

            convertView.setTag(holder);
            return convertView;
        }

        @Override
        protected View bindView(View convertView, int position, ModifierModel item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();

            ModifierModel i = getItem(position);
            if (i == null)
                return convertView;

            if (item.modifierGuid.equals(defaultModifierGuid)) {
                holder.title.setText(String.format(defaultFormatString, i.title));
            } else {
                holder.title.setText(i.title);
            }
            showPrice(holder.price, i.cost);

            return convertView;
        }

        @Override
        public void remove(int i) {
            handleRemove(getItem(i));
        }

        private class ViewHolder {
            TextView title;
            TextView price;
        }
    }


}
