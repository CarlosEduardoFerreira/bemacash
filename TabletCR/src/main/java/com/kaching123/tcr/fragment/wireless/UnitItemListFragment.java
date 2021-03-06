package com.kaching123.tcr.fragment.wireless;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.wireless.ApplyMultipleWarrantyCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelListener;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.wireless.WarrantyFragment.IWarrantyListener;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.Unit.Status;
import com.kaching123.tcr.model.converter.UnitViewFunction;
import com.kaching123.tcr.service.SyncCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.UnitsView2;
import com.kaching123.tcr.store.ShopStore.UnitsView;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
@OptionsMenu(R.menu.unit_fragment)
public class UnitItemListFragment extends ListFragment implements LoaderCallbacks<List<Unit>>, BarcodeReceiver {

    private static final Uri URI_UNITS = ShopProvider.getContentUri(UnitsView.URI_CONTENT);

    protected UnitItemAdapter adapter;

    protected Unit.Status status;

    protected ItemExModel model;

    protected IUnitCallback callback;

    @ViewById
    protected ProgressBar loadingSpinner;

    @ViewById
    protected RelativeLayout bodyContent;
    @ViewById
    protected EditText usbScannerInput;

    private boolean firstLoad;

    @AfterTextChange
    protected void usbScannerInputAfterTextChanged(Editable s) {
        String newline = System.getProperty("line.separator");
        boolean hasNewline = s.toString().contains(newline);
        if (hasNewline) {
            Logger.d("OrderItemListFragment usbScannerInputAfterTextChanged hasNewline: " + s.toString());
            String result = s.toString().replace("\n", "").replace("\r", "");
            callback.onBarcodeReceivedByUsb(result);
            s.clear();
        }
    }

    private Calendar calendar = Calendar.getInstance();

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


    @Override
    public void onResume() {
        super.onResume();
        if (!firstLoad && model != null)
            getLoaderManager().restartLoader(0, null, this);
        firstLoad = false;
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(syncGapReceiver, new IntentFilter(SyncCommand.ACTION_SYNC_GAP));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(syncGapReceiver);
        super.onPause();
    }

    @AfterViews
    protected void init() {
        firstLoad = true;
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
        Date minCreateTime = TcrApplication.get().getMinSalesHistoryLimitDateDayRounded(calendar);
        CursorLoaderBuilder loader = CursorLoaderBuilder.forUri(URI_UNITS);
        loader.where(UnitsView2.UnitTable.ITEM_ID + " = ?", model.guid);
        if (status != null) {
            loader.where(UnitsView2.UnitTable.STATUS + " = ?", status.ordinal());
            if (minCreateTime != null && status == Status.SOLD)
                loader.where(UnitsView2.SaleOrderTable.CREATE_TIME + " >= ? ", minCreateTime.getTime());
        } else if (minCreateTime != null) {
            loader.where("( " + UnitsView2.UnitTable.STATUS + " != " + Status.SOLD.ordinal() + " or " + UnitsView2.SaleOrderTable.CREATE_TIME + " >= " + minCreateTime.getTime() + " )");
        }
        return loader.orderBy(UnitsView2.UnitTable.UPDATE_TIME + " desc ").transformRow(new UnitViewFunction()).build(getActivity());
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
    protected void actionMultipleSelected() {
        callback.onMultiAdd();
    }

    @OptionsItem
    protected void actionAddSelected() {
        callback.onAdd();
    }

    @OptionsItem
    protected void actionWarrantySelected() {
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

        void onBarcodeReceivedByUsb(String barcode);

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

    private BroadcastReceiver syncGapReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (model == null)
                return;

            Logger.d("[SYNC GAP] Unit Item List Fragment: restart units loader");
            getLoaderManager().restartLoader(0, null, UnitItemListFragment.this);
        }

    };
}
