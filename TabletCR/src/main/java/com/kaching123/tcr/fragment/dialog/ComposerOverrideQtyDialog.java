package com.kaching123.tcr.fragment.dialog;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.saleorder.CompositionItemsCalculationCommand.CantSaleComposerModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by mboychenko on 01.09.2016.
 */
@EFragment
public class ComposerOverrideQtyDialog extends StyledDialogFragment implements LoaderManager.LoaderCallbacks{

    private static final String DIALOG_NAME = ComposerOverrideQtyDialog.class.getName();

    private HashMap<String, List<CantSaleComposerModel>> cantSaleCompositions;
    private List<SaleOrderItemViewModel> saleOrderItemViewModelList;
    private DialogButtonListener listener;

    @ViewById
    TableLayout tableLayoutId;
    @ViewById
    ProgressBar progress;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow()
                .setLayout(getResources().getDimensionPixelOffset(R.dimen.compos_override_qty_dialog_width),
                        getDialog().getWindow().getAttributes().height);
    }

    @AfterViews
    protected void init(){
        progress.setVisibility(View.VISIBLE);
        tableLayoutId.removeAllViews();
        getPositiveButton().setEnabled(false);
        getNegativeButton().setEnabled(false);
        getLoaderManager().initLoader(0, null, this).forceLoad();
    }


    @Override
    protected int getDialogContentLayout() {
        return R.layout.composer_override_qty_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.sale_composer_without_composition;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.button_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.button_override;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                listener.onOverrideButtonPress(cantSaleCompositions.keySet());
                return true;
            }
        };
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                listener.onCancelButtonPress(cantSaleCompositions.keySet());
                return true;
            }
        };
    }

    public static void show(FragmentActivity activity, HashMap<String, List<CantSaleComposerModel>> itemsCantBeSold,
                                       List<SaleOrderItemViewModel> list, DialogButtonListener onNegativeButtonPressListener) {
        ComposerOverrideQtyDialog fragment = ComposerOverrideQtyDialog_.builder().build();
        fragment.setCancelable(false);
        fragment.cantSaleCompositions = itemsCantBeSold;
        fragment.saleOrderItemViewModelList = list;
        DialogUtil.show(activity, DIALOG_NAME, fragment).setListener(onNegativeButtonPressListener);
    }

    public void setListener(DialogButtonListener listener){
        this.listener = listener;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader(getActivity()) {
            @Override
            public Object loadInBackground() {
                for (Map.Entry<String, List<CantSaleComposerModel>> cantSaleComposition : cantSaleCompositions.entrySet()) {
                    for (SaleOrderItemViewModel viewItem : saleOrderItemViewModelList) {
                        if(viewItem.itemModel.itemGuid.equals(cantSaleComposition.getKey())){
                            for (CantSaleComposerModel composer : cantSaleComposition.getValue()) {
                                composer.composerHostName = viewItem.description;
                            }
                        }
                    }
                }
                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        progress.setVisibility(View.GONE);
        getPositiveButton().setEnabled(true);
        getNegativeButton().setEnabled(true);
        for (List<CantSaleComposerModel> cantSaleComposerModels : cantSaleCompositions.values()) {
            StringBuilder builder = new StringBuilder();
            if(!cantSaleComposerModels.isEmpty()) {
                TableRow view = (TableRow)layoutInflater.inflate(R.layout.composer_cant_sale_table_row, tableLayoutId, false);
                ((TextView)view.findViewById(R.id.parent_column_id)).setText(cantSaleComposerModels.get(0).composerHostName + ":");
                for (CantSaleComposerModel cantSaleComposerModel : cantSaleComposerModels) {
                    builder.append(getString(R.string.sale_composer_without_composition_row, cantSaleComposerModel.composerChildName, cantSaleComposerModel.totalNeededQty, cantSaleComposerModel.availableSourceItemQty));
                }
                ((TextView)view.findViewById(R.id.childs_column_id)).setText(builder.toString());
                tableLayoutId.addView(view);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

    public interface DialogButtonListener{
        void onCancelButtonPress(Set<String> listItems);
        void onOverrideButtonPress(Set<String> listItems);
    }
}
