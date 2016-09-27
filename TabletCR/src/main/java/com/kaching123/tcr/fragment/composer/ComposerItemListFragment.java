package com.kaching123.tcr.fragment.composer;

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
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelListener;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.wireless.BarcodeReceiver;
import com.kaching123.tcr.model.ComposerExModel;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.converter.ComposerFunction;
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
 * Created by idyuzheva on 04.12.2015.
 */
@EFragment(R.layout.composer_list_fragment)
@OptionsMenu(R.menu.composer_fragment)
public class ComposerItemListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<ComposerExModel>>, BarcodeReceiver {

    private static final Uri URI_UNITS = ShopProvider.contentUri(ShopStore.ComposerView.URI_CONTENT);

    protected ComposerItemAdapter adapter;
    protected List<ComposerExModel> composers;

    protected ItemExModel model;

    protected IComposerCallback callback;

    @ViewById
    protected ProgressBar loadingSpinner;

    @ViewById
    protected RelativeLayout bodyContent;

    @ViewById
    protected TextView footerTotal;

    public ComposerItemListFragment setCallback(IComposerCallback callback) {
        this.callback = callback;
        return this;
    }

    public ComposerItemListFragment setModel(ItemExModel model) {
        this.model = model;
        getLoaderManager().restartLoader(0, null, this);
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.composer_list_fragment, container, false);
    }

    @AfterViews
    protected void init() {
        setListAdapter(adapter = new ComposerItemAdapter(getActivity()));
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
                mode.getMenuInflater().inflate(R.menu.composer_contextual, menu);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                SparseBooleanArray checked = getListView().getCheckedItemPositions();
                final List<ComposerExModel> persons = getUnits(checked);
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
                            callback.onEdit(persons.get(0), composers);
                        }
                        return true;
                }
                return false;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                SparseBooleanArray checkedItems = getListView().getCheckedItemPositions();
                List<ComposerExModel> units = getUnits(checkedItems);
                int count = units.size();
                shouldShowDelete = count >= 1;
                shouldShowEdit = count == 1;
                mode.setTitle(getString(R.string.composer_selections, model.description, count));
                mode.invalidate();
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        callback.onEdit(adapter.getItem(position), composers);

    }

    protected List<ComposerExModel> getUnits(SparseBooleanArray checked) {
        List<ComposerExModel> persons = new ArrayList<ComposerExModel>();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (checked.get(i)) {
                ComposerExModel holder = adapter.getItem(i);
                persons.add(holder);
            }
        }
        return persons;
    }

    @Override
    public Loader<List<ComposerExModel>> onCreateLoader(int loaderId, Bundle args) {
        CursorLoaderBuilder loader = CursorLoaderBuilder.forUri(URI_UNITS);
        loader.where(ShopSchema2.ComposerView2.ComposerTable.ITEM_HOST_ID + " = ?", model.guid);
        return loader.orderBy(ShopSchema2.ComposerView2.ItemChildTable.DESCRIPTION + " asc ").transformRow(new ComposerFunction()).build(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<ComposerExModel>> listLoader, List<ComposerExModel> composers) {
        adapter.changeCursor(composers);
        this.composers = composers;
        BigDecimal cost = BigDecimal.ZERO;
        for (ComposerExModel composer : composers) {
            cost = cost.add(composer.getChildItem().cost.multiply(composer.qty));
        }
        footerTotal.setText(getString(R.string.composer_footer_cost_total, UiHelper.valueOf(cost)));
    }

    @Override
    public void onLoaderReset(Loader<List<ComposerExModel>> listLoader) {
        if (getActivity() == null) {
            return;
        }
        adapter.changeCursor(null);
    }

    @OptionsItem
    protected void actionAddSelected() {
        callback.onAdd(composers);
    }

    @Override
    public void onBarcodeReceived(String barcode) {

    }

    public interface IComposerCallback {
        void onAdd(List<ComposerExModel> composers);

        void onEdit(ComposerExModel item, List<ComposerExModel> composers);

        void onDelete(List<ComposerExModel> units);
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