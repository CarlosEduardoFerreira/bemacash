package com.kaching123.tcr.fragment.composer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.collect.FluentIterable;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.BaseItemExAdapter;
import com.kaching123.tcr.component.BrandTextWatcher;
import com.kaching123.tcr.component.RegexpFormatInputFilter;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.wireless.BarcodeReceiver;
import com.kaching123.tcr.model.ComposerExModel;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.composer.AddComposerCommand;
import com.kaching123.tcr.store.composer.EditComposerCommand;
import com.kaching123.tcr.util.StringUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.kaching123.tcr.fragment.UiHelper.parseBrandQtyInput;
import static com.kaching123.tcr.fragment.UiHelper.showBrandQty;
import static com.kaching123.tcr.fragment.UiHelper.showBrandQtyInteger;

/**
 * Created by irikhmayer on 06.05.2015.
 */
@EFragment(R.layout.composer_edit_dialog_fragment)
public class ComposerEditFragment extends StyledDialogFragment implements BarcodeReceiver {

    private static final String DIALOG_NAME = "ComposerEditFragment";

    @ViewById
    protected CheckBox track;

    @ViewById
    protected CheckBox free;

    @ViewById
    protected CheckBox childSelected;

    @ViewById
    protected TextView qtyTextview;

    @ViewById(R.id.price_type)
    protected TextView type;

    @ViewById
    protected EditText qtyEditbox;

    @ViewById
    protected AutoCompleteTextView itemChooser;

    @FragmentArg
    protected String itemGuid;

    @FragmentArg
    protected ComposerExModel model;

    @FragmentArg
    protected ArrayList<String> composerList;

    protected boolean firstLoad = true;

    protected ComposerCallback callback;
    protected ComposersAdapter customerAdapter;

    @InstanceState
    protected MODE mode;

    protected enum MODE {
        EDIT,
        ADD
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.default_dlg_width),
                getDialog().getWindow().getAttributes().height);
    }

    @AfterViews
    protected void attachViews() {
        customerAdapter = new ComposersAdapter(getActivity());


        qtyEditbox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshEnabled();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        if (model != null) {
            mode = MODE.EDIT;
            track.setChecked(model.tracked);
            free.setChecked(model.restricted);
            itemChooser.setText(model.getChildItem().description);
            childSelected.setChecked(true);
            setQtyBox(model.getChildItem());
        } else {
            mode = MODE.ADD;
            model = new ComposerExModel();
            model.guid = UUID.randomUUID().toString();
            model.itemHostId = itemGuid;
            childSelected.setChecked(false);
            setQtyBox(null);
            refreshEnabled();
        }

        qtyEditbox.addTextChangedListener(new BrandTextWatcher(qtyEditbox));

        itemChooser.setAdapter(customerAdapter);
        itemChooser.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ItemExModel item = ((ComposersAdapter) itemChooser.getAdapter()).getItem(position);
                model.itemChildId = item.getGuid();
                childSelected.setChecked(true);
                refreshEnabled();
                setQtyBox(item);
            }
        });
    }

    private void setQtyBox(ItemExModel model) {
        if (model == null) {
            resetQtyLabelField("...");

            qtyEditbox.setEnabled(false);
            type.setText("?");
        } else {
            resetQtyLabelField(model.shortCut);

            qtyEditbox.setEnabled(true);
            type.setText(StringUtils.valueOf(model.priceType, getActivity()));
            if (model.priceType == PriceType.UNIT_PRICE) {
                qtyEditbox.setInputType(InputType.TYPE_CLASS_PHONE);
                qtyEditbox.setFilters(new InputFilter[]{new QtyFormatInputFilter()});
                showBrandQty(qtyEditbox, this.model.qty);
            } else {
                qtyEditbox.setInputType(InputType.TYPE_CLASS_NUMBER);
                qtyEditbox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
                showBrandQtyInteger(qtyEditbox, this.model.qty);
            }
        }
    }

    protected void resetQtyLabelField(String res) {
        qtyTextview.setText(getString(R.string.composer_dialog_qty, res));
    }

    protected void refreshEnabled() {
        boolean enabled = true;
        enabled &= !TextUtils.isEmpty(model.itemChildId);
        BigDecimal qty = parseBrandQtyInput(qtyEditbox.getText().toString());
        enabled &= qty.compareTo(BigDecimal.ZERO) > 0;
        enablePositiveButton(enabled, greenBtnColor);
    }

    protected ComposerModel collectData() {
        model.qty = parseBrandQtyInput(qtyEditbox.getText().toString());
        model.restricted = free.isChecked();
        model.tracked = track.isChecked();
        return model;
    }

    public void setCallback(ComposerCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onBarcodeReceived(String barcode) {

    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.composer_edit_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return model == null
                ? R.string.composer_dialog_add_title
                : R.string.composer_dialog_edit_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return model == null ? R.string.btn_add : R.string.btn_adjust;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {

            @Override
            public boolean onClick() {
                final ComposerModel dataModel = collectData();
                if (mode == MODE.EDIT) {
                    EditComposerCommand.start(getActivity(), dataModel, new EditComposerCommand.ComposerCallback() {

                        @Override
                        protected void handleSuccess() {
                            callback.handleSuccess(dataModel);
                        }

                        @Override
                        protected void handleError(String message) {
                            callback.handleError(message);
                        }
                    });
                } else if (mode == MODE.ADD) {
                    AddComposerCommand.start(getActivity(), dataModel, new AddComposerCommand.ComposerCallback() {

                        @Override
                        protected void handleSuccess() {
                            callback.handleSuccess(dataModel);
                        }

                        @Override
                        protected void handleError(String message) {
                            callback.handleError(message);
                        }
                    });
                }
                return false;
            }
        };
    }

    public static void show(FragmentActivity activity,
                            String itemGuid,
                            ComposerExModel model,
                            List<ComposerExModel> composers,
                            ComposerCallback callback) {
        ArrayList<String> composerList = new ArrayList<String>(composers.size());
        for (int i = 0; i < composers.size(); i++) {
            composerList.add(composers.get(i).getChildItem().guid);
        }
        DialogUtil.show(activity, DIALOG_NAME, ComposerEditFragment_
                .builder()
                .itemGuid(itemGuid)
                .model(model)
                .composerList(composerList)
                .build())
                .setCallback(callback);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public interface ComposerCallback {

        void handleSuccess(ComposerModel parent);

        void handleSuccess();

        void handleError(String message);

        void handleCancel();
    }

    private class ComposersAdapter extends BaseItemExAdapter {

        public ComposersAdapter(Context context) {
            super(context);
        }

        @Override
        protected String getCustomSelection() {
            return ShopSchema2.ItemExtView2.ItemTable.GUID + " <> ? AND "
                    + ShopSchema2.ItemExtView2.ItemTable.CODE_TYPE + " IS NULL AND "
                    + ShopSchema2.ItemExtView2.ChildComposerTable.ID + " IS NULL";
        }

        protected String whereInColumn() {
            return ShopSchema2.ItemExtView2.ItemTable.GUID;
        }

        protected Collection<String> whereInCollection() {
            return composerList;
        }

        @Override
        protected String[] getCustomSelectionArgs() {
            return new String[]{model.itemHostId};
        }

        @Override
        protected void publishResults(FluentIterable<ItemExModel> cursor) {
            if (model.itemChildId != null && model.getChildItem() != null && mode == MODE.EDIT && firstLoad) {
                // initial edit
                firstLoad = false;
            } else {
                model.itemChildId = null;
                childSelected.setChecked(false);
            }
            refreshEnabled();
        }
    }

    public class QtyFormatInputFilter extends RegexpFormatInputFilter {

        private static final String REGEXP = "^(\\d{0,7}(.)*)?(\\,[0-9]{1,2})?$";

        public QtyFormatInputFilter() {
            super(REGEXP);
        }
    }
}
