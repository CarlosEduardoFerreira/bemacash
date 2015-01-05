package com.kaching123.tcr.fragment.wireless;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
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
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.wireless.ApplyMultipleWarrantyCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelListener;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.wireless.WarrantyFragment.IWarrantyListener;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.converter.UnitFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
@OptionsMenu(R.menu.unit_fragment)
public class UnitItemListFragment extends ListFragment implements LoaderCallbacks<List<Unit>>, BarcodeReceiver {

    private static final Uri URI_UNITS = ShopProvider.getContentUri(ShopStore.UnitTable.URI_CONTENT);

    protected UnitItemAdapter adapter;

    protected Unit.Status status;

    protected ItemExModel model;

    protected IUnitCallback callback;

    @ViewById
    protected ProgressBar loadingSpinner;

    @ViewById
    protected RelativeLayout bodyContent;

    public UnitItemListFragment setCallback(IUnitCallback callback) {
        this.callback = callback;
        return this;
    }

    public UnitItemListFragment setStatus(Unit.Status status) {
        this.status = status;
        getLoaderManager().restartLoader(0, null, this);
        return this;
    }

    public UnitItemListFragment setModel(ItemExModel model) {
        this.model = model;
        getLoaderManager().restartLoader(0, null, this);
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wireless_list_fragment, container, false);
    }

    @AfterViews
    protected void init() {
        setListAdapter(adapter = new UnitItemAdapter(getActivity()));
        getListView().setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            private boolean shouldShowEdit = true;
            private boolean shouldShowDelete = true;

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                menu.findItem(R.id.action_edit).setVisible(shouldShowEdit);
                menu.findItem(R.id.action_delete).setVisible(shouldShowDelete);
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.wireless_contextual, menu);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                SparseBooleanArray checked = getListView().getCheckedItemPositions();
                final List<Unit> persons = getUnits(checked);
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        if (persons != null && !persons.isEmpty()) {
                            AlertDialogWithCancelListener.show(getActivity(),
                                    R.string.wireless_remove_item_title,
                                    getString(R.string.wireless_remove_item_body),
                                    R.string.btn_confirm,
                                    new StyledDialogFragment.OnDialogClickListener() {
                                        @Override
                                        public boolean onClick() {
                                            showProgress();
                                            callback.onDelete(persons);
                                            return true;
                                        }
                                    }, null
                            );
                        }
                        mode.finish();
                        getListView().clearChoices();
                        return true;
                    case R.id.action_edit:
                        if (persons != null && !persons.isEmpty()) {
                            callback.onEdit(persons.get(0));
                        }
                        return true;
                }
                return false;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                SparseBooleanArray checkeds = getListView().getCheckedItemPositions();
                List<Unit> units = getUnits(checkeds);
                int count = units.size();
                boolean soldPresent = true;
                for (Unit unit : units) {
                    if (unit.status.equals(Unit.Status.SOLD)) {
                        soldPresent = false;
                    }
                }
                shouldShowDelete = soldPresent;
                shouldShowEdit = count == 1;
                mode.setTitle("Selected \"" + model.description + "\" units: " + count);
                mode.invalidate();
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        callback.onEdit(adapter.getItem(position));
    }

    protected List<Unit> getUnits(SparseBooleanArray checked) {
        List<Unit> persons = new ArrayList<Unit>();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (checked.get(i)) {
                Unit holder = adapter.getItem(i);
                persons.add(holder);
            }
        }
        return persons;
    }

    @Override
    public Loader<List<Unit>> onCreateLoader(int loaderId, Bundle args) {
        CursorLoaderBuilder loader = CursorLoaderBuilder.forUri(URI_UNITS);
        loader.where(ShopStore.UnitTable.ITEM_ID + " = ?", model.guid);
        if (status != null) {
            loader.where(ShopStore.UnitTable.STATUS + " = ?", status.ordinal());
        }
        return loader.orderBy(ShopStore.UnitTable.UPDATE_TIME + " desc ").transform(new UnitFunction()).build(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Unit>> listLoader, List<Unit> saleOrderModels) {
        adapter.changeCursor(saleOrderModels);
    }

    @Override
    public void onLoaderReset(Loader<List<Unit>> listLoader) {
        if (getActivity() == null)
            return;
        adapter.changeCursor(null);
    }

    @OptionsItem
    protected void actionMultipleSelected(){
        callback.onMultiAdd();
    }

    @OptionsItem
    protected void actionAddSelected(){
        callback.onAdd();
    }

    @OptionsItem
    protected void actionWarrantySelected(){
        WarrantyFragment.show(getActivity(), new IWarrantyListener() {
            @Override
            public void onConfirm(int warranty) {
                WarrantyFragment.hide(getActivity());
                ApplyMultipleWarrantyCommand.start(getActivity(), model.guid, warranty);
            }
        });
    }

    @Override
    public void onBarcodeReceived(String barcode) {

    }

    public interface IUnitCallback {
        void onAdd();
        void onMultiAdd();
        void onEdit(Unit item);
        void onDelete(List<Unit> units);
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
}