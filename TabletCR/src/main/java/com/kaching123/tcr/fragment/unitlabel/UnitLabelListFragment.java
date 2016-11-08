package com.kaching123.tcr.fragment.unitlabel;

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
import android.widget.Toast;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.unitlabel.UnitLabelCommandUtils;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelListener;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.function.ItemWrapFunction;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.UnitLabelModel;
import com.kaching123.tcr.model.converter.UnitLabelFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.util.CursorUtil._wrap;


/**
 * Created by alboyko 07.12.2015
 */
@EFragment
@OptionsMenu(R.menu.unit_label_fragment)
public class UnitLabelListFragment extends ListFragment implements LoaderCallbacks<List<UnitLabelModel>> {

    private static final Uri URI_UNIT_LABELS = ShopProvider.contentUri(ShopStore.UnitLabelTable.URI_CONTENT);

    protected UnitLabelItemAdapter adapter;

    protected UnitLabelModel model;

    protected IUnitLabelCallback callback;

    public UnitLabelListFragment setCallback(IUnitLabelCallback callback) {
        this.callback = callback;
        return this;
    }

    @ViewById
    protected ProgressBar loadingSpinner;

    @ViewById
    protected RelativeLayout bodyContent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.unit_labels_list_fragment, container, false);
    }

    @AfterViews
    protected void init() {
        setListAdapter(adapter = new UnitLabelItemAdapter(getActivity()));
        getActivity().getSupportLoaderManager().restartLoader(0, null, UnitLabelListFragment.this);
        getListView().setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            private boolean shouldShowEdit = true;
            private boolean shouldShowDelete = true;
            private boolean shouldEnableDelete = true;

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                menu.findItem(R.id.action_edit).setVisible(shouldShowEdit);
                menu.findItem(R.id.action_delete).setVisible(shouldShowDelete);
                menu.findItem(R.id.action_delete).setEnabled(shouldEnableDelete);
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.units_label_contextual, menu);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                SparseBooleanArray checked = getListView().getCheckedItemPositions();
                final List<UnitLabelModel> unitLabels = getUnitLabels(checked);
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        if (unitLabels != null && !unitLabels.isEmpty()) {
                            AlertDialogWithCancelListener.show(getActivity(),
                                    R.string.unit_label_remove_dialog_title,
                                    getString(R.string.unit_label_remove_dialog_body),
                                    R.string.btn_confirm,
                                    new StyledDialogFragment.OnDialogClickListener() {
                                        @Override
                                        public boolean onClick() {
                                            showProgress();
                                            callback.onDelete(unitLabels);
                                            return true;
                                        }
                                    },
                                    null
                            );
                        }
                        mode.finish();
                        getListView().clearChoices();
                        return true;
                    case R.id.action_edit:
                        if (unitLabels != null && !unitLabels.isEmpty()) {
                            callback.onEdit(unitLabels.get(0));
                        }
                        return true;
                }
                return false;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                SparseBooleanArray checkeds = getListView().getCheckedItemPositions();
                List<UnitLabelModel> units = getUnitLabels(checkeds);
                int count = units.size();

                shouldShowDelete = count >= 1;
                shouldShowEdit = count == 1;

                shouldEnableDelete = shouldEnableDelete(units);

                mode.setTitle(getResources().getQuantityString(R.plurals.unit_labels_selected, count, count));
                mode.invalidate();
            }
        });
    }

    protected List<UnitLabelModel> getUnitLabels(SparseBooleanArray checked) {
        List<UnitLabelModel> labelModels = new ArrayList<UnitLabelModel>();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (checked.get(i)) {
                UnitLabelModel holder = adapter.getItem(i);
                labelModels.add(holder);
            }
        }
        return labelModels;
    }

    // Add
    @OptionsItem
    protected void actionAddSelected() {
        callback.onAdd();
    }

    // LoaderCallbacks<List<UnitLabelModel>> block
    @Override
    public Loader<List<UnitLabelModel>> onCreateLoader(int id, Bundle args) {
        return CursorLoaderBuilder
                .forUri(URI_UNIT_LABELS)
                .orderBy(ShopStore.UnitLabelTable.SHORTCUT)
                //filter deleted for BEMA-1019
                /*.where(ShopStore.UnitLabelTable.SHORTCUT + " <> \'" +
                        TcrApplication.get().getShopInfo().defUnitLabelShortcut + "\'")*/
                .transform(new UnitLabelFunction())
                .build(getActivity());
    } // select * from UNIT_LABEL where shortcut <> 'und' and

    @Override
    public void onLoadFinished(Loader<List<UnitLabelModel>> loader, List<UnitLabelModel> unitLabelModels) {
        adapter.changeCursor(unitLabelModels);
    }

    @Override
    public void onLoaderReset(Loader<List<UnitLabelModel>> loader) {
        if (getActivity() == null)
            return;

        adapter.changeCursor(null);
    }

    private boolean shouldEnableDelete(List<UnitLabelModel> units) {
        List<ItemModel> items;
        for (UnitLabelModel unitLabelModel: units) {
            items = _wrap(UnitLabelCommandUtils.unitForeignKeyQuery(unitLabelModel.getGuid()).perform(getActivity()), new ItemWrapFunction());
            if (!items.isEmpty()) {
                Toast.makeText(getActivity(), R.string.unit_label_remove_used_msg, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        callback.onEdit(adapter.getItem(position));
    }

    // Inner interface
    public interface IUnitLabelCallback {
        void onAdd();
        void onEdit(UnitLabelModel item);
        void onDelete(List<UnitLabelModel> items);
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
