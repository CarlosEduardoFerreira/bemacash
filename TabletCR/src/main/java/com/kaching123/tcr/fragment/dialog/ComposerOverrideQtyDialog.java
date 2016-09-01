package com.kaching123.tcr.fragment.dialog;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
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

/**
 * Created by mboychenko on 01.09.2016.
 */
@EFragment
public class ComposerOverrideQtyDialog extends StyledDialogFragment implements LoaderManager.LoaderCallbacks{

    private static final String DIALOG_NAME = ComposerOverrideQtyDialog.class.getName();

    private HashMap<String, List<CantSaleComposerModel>> cantSaleCompositions;
    private List<SaleOrderItemViewModel> saleOrderItemViewModelList;
    private NegativeButtonListener listener;

    @ViewById
    TextView textContent;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.composer_override_qty_fragment;
    }

    @AfterViews
    protected void initView(){
        for (Map.Entry<String, List<CantSaleComposerModel>> cantSaleComposition : cantSaleCompositions.entrySet()) {
            for (SaleOrderItemViewModel viewItem : saleOrderItemViewModelList) {
                if(viewItem.itemModel.itemGuid.equals(cantSaleComposition.getKey())){
                    for (CantSaleComposerModel composer : cantSaleComposition.getValue()) {
                        composer.composerHostName = viewItem.description;
                    }
                }
            }
        }

        textContent.setText("Following items have no enough composition qty. \n " +
                            "Sale this items without composition? \n");
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
        return null;
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                listener.onNegativeButtonPress();
                return true;
            }
        };
    }

    public static void showCancelable(FragmentActivity activity, NegativeButtonListener onNegativeButtonPressListener, HashMap<String, List<CantSaleComposerModel>> itemsCantBeSold, List<SaleOrderItemViewModel> list) {
        ComposerOverrideQtyDialog fragment = ComposerOverrideQtyDialog_.builder().build();
        fragment.cantSaleCompositions = itemsCantBeSold;
        fragment.saleOrderItemViewModelList = list;
        DialogUtil.show(activity, DIALOG_NAME, fragment).setListener(onNegativeButtonPressListener);
    }

    public void setListener(NegativeButtonListener listener){
        this.listener = listener;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader(getContext()) {
            @Override
            public Object loadInBackground() {
                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {

    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    public interface NegativeButtonListener{
        void onNegativeButtonPress();
    }
}
