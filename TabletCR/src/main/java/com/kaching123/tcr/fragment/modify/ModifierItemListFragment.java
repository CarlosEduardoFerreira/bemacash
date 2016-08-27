package com.kaching123.tcr.fragment.modify;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelListener;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.wireless.BarcodeReceiver;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ModifierExModel;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.converter.ModifierExFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by alboyko on 01.12.2015.
 */

@EFragment(R.layout.modifier_list_fragment)
@OptionsMenu(R.menu.modifier_fragment)
public class ModifierItemListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<List<ModifierExModel>>, BarcodeReceiver {

    protected static final Uri URI_UNITS = ShopProvider.contentUri(ShopStore.ModifierView.URI_CONTENT);

    protected ModifierItemAdapter adapter;

    protected ItemExModel model;

    protected IModifierCallback callback;

    @ViewById
    protected ProgressBar loadingSpinner;

    @ViewById
    protected RelativeLayout bodyContent;

    protected ActionMode mode; // we dont support rotation so its kinda ok.

    protected ModifierType modType;

    public ModifierItemListFragment setModType(ModifierType modType) {
        this.modType = modType;
        return this;
    }

    public ModifierItemListFragment setCallback(IModifierCallback callback) {
        this.callback = callback;
        return this;
    }

    public ModifierItemListFragment setModel(ItemExModel model) {
        this.model = model;
        adapter.setHostItem(model);
        getLoaderManager().restartLoader(0, null, this);
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.modifier_list_fragment, container, false);
    }

    protected void onMenuPrepared(Menu menu, boolean shouldShowEdit, boolean shouldShowDelete) {
        menu.findItem(R.id.action_edit).setVisible(shouldShowEdit);
        menu.findItem(R.id.action_delete).setVisible(shouldShowDelete);
    }

    protected ModifierItemListFragment self() {
        return this;
    }

    @AfterViews
    protected void attachViews() {
        setListAdapter(adapter = new ModifierItemAdapter(getActivity()));
        getListView().setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            private boolean shouldShowEdit = true;
            private boolean shouldShowDelete = true;

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                onMenuPrepared(menu, shouldShowEdit, shouldShowDelete);
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.composer_contextual, menu);
                self().mode = mode;
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                do {
                    SparseBooleanArray checked = getListView().getCheckedItemPositions();
                    final List<ModifierExModel> modifiers = getUnits(checked);
                    switch (item.getItemId()) {
                        case R.id.action_delete:
                            if (modifiers != null && !modifiers.isEmpty()) {
                                AlertDialogWithCancelListener.show(getActivity(),
                                        R.string.wireless_remove_item_title,
                                        getString(R.string.wireless_remove_item_body),
                                        R.string.btn_confirm,
                                        new StyledDialogFragment.OnDialogClickListener() {
                                            @Override
                                            public boolean onClick() {
                                                callback.onDeleteModel(modifiers);
                                                return true;
                                            }
                                        }, null
                                );
                            }

                            break;
                        case R.id.action_edit:
                            if (modifiers != null && !modifiers.isEmpty()) {
                                callback.onEdit(modifiers.get(0));
                            }
                            break;
                    }
                } while (false);
                getListView().clearChoices();
                mode.finish();
                return true;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                SparseBooleanArray checkedItems = getListView().getCheckedItemPositions();
                List<ModifierExModel> units = getUnits(checkedItems);
                int count = units.size();
                shouldShowDelete = count >= 1;
                shouldShowEdit = count == 1;
                int titleId = getTitleId();
                mode.setTitle(getString(titleId, count));
                mode.invalidate();
            }

            private int getTitleId() {
                if (modType == ModifierType.MODIFIER){
                    return R.string.modifier_selection;
                } else if (modType == ModifierType.ADDON){
                    return R.string.addon_selection;
                }else{
                    return R.string.optional_selection;
                }
            }
        });
        setModel(((ItemDataProvider) getActivity()).getItem());
        setCallback((IModifierCallback) getActivity());
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        callback.onEdit(adapter.getItem(position));
    }

    protected List<ModifierExModel> getUnits(SparseBooleanArray checked) {
        List<ModifierExModel> persons = new ArrayList<ModifierExModel>();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (checked.get(i)) {
                ModifierExModel holder = adapter.getItem(i);
                persons.add(holder);
            }
        }
        return persons;
    }

    @OptionsItem
    protected void actionAddGroupSelected() {
        callback.onAddGroup();
    }

    @OptionsItem
    protected void actionAddSelected() {
        callback.onAdd(modType, null);
    }

    @Override
    public void onBarcodeReceived(String barcode) {

    }

    @Override
    public Loader<List<ModifierExModel>> onCreateLoader(int id, Bundle args) {
        CursorLoaderBuilder loader = CursorLoaderBuilder.forUri(URI_UNITS);
        loader.where(ShopSchema2.ModifierView2.ModifierTable.ITEM_GUID + " = ?", model.guid);
        if (modType != null) {
            loader.where(ShopSchema2.ModifierView2.ModifierTable.TYPE + " = ?", modType.ordinal());
        }
        return loader.transform(new ModifierExFunction()).build(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<ModifierExModel>> loader, List<ModifierExModel> data) {
        adapter.changeCursor(data);
        BigDecimal cost = BigDecimal.ZERO;
        for (ModifierExModel composer : data) {
            if (composer.getItem() == null) {
                continue;
            }
            cost = cost.add(composer.getItem().price.multiply(composer.childItemQty));
        }
        if (mode != null) {
            mode.finish();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<ModifierExModel>> loader) {
        if (getActivity() == null) {
            return;
        }
        adapter.changeCursor(null);
    }

    public interface IModifierCallback {
        void onAdd(ModifierType type, String itemGroupGuid);

        void onAddGroup();

        void onDefault(ModifierExModel item);

        void onEdit(ModifierExModel item);

        void onDeleteModel(List<ModifierExModel> units);

        void onEdit(ModifierGroupModel item);

        void onDeleteGroup(ModifierGroupModel units);
    }

    private synchronized void crossfade(final View first, final View second) {
        first.setAlpha(0f);
        first.setVisibility(View.VISIBLE);

        first.animate()
                .alpha(1f)
                .setDuration(200)
                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        second.animate()
                .alpha(0f)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        second.setVisibility(View.GONE);
                    }
                });
    }

    public void showProgress() {
        crossfade(loadingSpinner, bodyContent);
    }

    public void hideProgress() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                crossfade(bodyContent, loadingSpinner);
            }
        }, 501);
    }

    public interface ItemDataProvider {
        ItemExModel getItem();
    }

    public static ModifierItemListFragment build(ModifierType type) {
        return ModifierItemListFragment_.builder().build().setModType(type);
    }
}