package com.kaching123.tcr.fragment.modify;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.BaseItemExAdapter;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.commands.store.inventory.AddModifierCommand;
import com.kaching123.tcr.commands.store.inventory.EditModifiersCommand;
import com.kaching123.tcr.component.BrandTextWatcher;
import com.kaching123.tcr.component.CurrencyTextWatcher;
import com.kaching123.tcr.component.RegexpFormatInputFilter;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.wireless.BarcodeReceiver;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ModifierExModel;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ModifierGroupTable;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.util.StringUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.kaching123.tcr.fragment.UiHelper.parseBigDecimal;
import static com.kaching123.tcr.fragment.UiHelper.parseBrandQtyInput;
import static com.kaching123.tcr.fragment.UiHelper.priceFormat;
import static com.kaching123.tcr.fragment.UiHelper.showBrandQty;
import static com.kaching123.tcr.fragment.UiHelper.showBrandQtyInteger;
import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by irikhmayer on 06.05.2015.
 */
@EFragment(R.layout.modifier_edit_dialog_fragment)
public class ModifierEditFragment extends StyledDialogFragment implements BarcodeReceiver {

    private static final String DIALOG_NAME = "ModifierEditFragment";
    public final static String ITEM_KEY = "ITEM_KEY";

    @ViewById
    protected Switch free;

    @ViewById
    protected CheckBox childSelected, autoApplySelected;

    @ViewById(R.id.spinner)
    protected Spinner itemGroupSpinner;

    @ViewById(R.id.spinner2)
    protected Spinner itemGroupSpinner2;

    @ViewById
    protected TextView qtyTextview;

    @ViewById(R.id.price_type)
    protected TextView type;

    @ViewById(R.id.cost_label)
    protected TextView costLbl;

    @ViewById(R.id.price_type_msg)
    protected TextView messagePrice;

    @ViewById
    protected EditText qtyEditbox;

    @ViewById
    protected EditText description;

    @ViewById
    protected EditText priceEditbox;

    @ViewById
    protected AutoCompleteTextView itemChooser;

    @ViewById
    protected LinearLayout llAutoApply;

    @FragmentArg
    protected String groupGuid;

    @FragmentArg
    protected String itemGuid;

    @FragmentArg
    protected ModifierType modType;

    @FragmentArg
    protected ModifierExModel model;

    protected boolean firstLoad = true;

    protected ItemGroupAdapter groupAdapter;

    protected ModifierCallback callback;
    protected ModifiersAdapter customerAdapter;

    @ColorRes(R.color.light_gray) protected int normalTextColor;
    @ColorRes(R.color.gray_dark) protected int badTextColor;

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

    protected void setUpPriceTag() {
        try {
            priceEditbox.setText(priceFormat(parseBrandQtyInput(qtyEditbox.getText().toString()).multiply(model.getItem().price)));
        } catch (NullPointerException pokemon) {

        }
    }

    @AfterViews
    protected void attachViews() {
        if (modType != ModifierType.MODIFIER) {
            itemGroupSpinner.setVisibility(View.GONE);
            itemGroupSpinner2.setVisibility(View.GONE);
        }
        llAutoApply.setVisibility(modType == ModifierType.MODIFIER ? View.VISIBLE : View.INVISIBLE);
        autoApplySelected.setChecked(model == null ? false : model.autoApply);
        customerAdapter = new ModifiersAdapter(modType, getActivity());
        groupAdapter = new ItemGroupAdapter(getActivity());
        qtyEditbox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setUpPriceTag();
                refreshEnabled();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        priceEditbox.addTextChangedListener(new TextWatcher() {
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
        description.addTextChangedListener(new TextWatcher() {
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
        free.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    itemGroupSpinner.setEnabled(true);
                    itemGroupSpinner2.setEnabled(false);
                    itemChooser.setEnabled(false);
                    childSelected.setEnabled(false);
                    qtyEditbox.setEnabled(false);
                    priceEditbox.setEnabled(true);
                    qtyTextview.setTextColor(badTextColor);
                    type.setTextColor(badTextColor);
                    messagePrice.setTextColor(badTextColor);
                    qtyEditbox.setText("");
                    itemChooser.setText("");
                    type.setText("");
                    model.setItem(null);
                    model.childItemGuid = null;
                } else {
                    itemGroupSpinner.setEnabled(false);
                    itemGroupSpinner2.setEnabled(true);
                    itemChooser.setEnabled(true);
                    childSelected.setEnabled(true);
                    priceEditbox.setEnabled(false);
                    qtyTextview.setTextColor(normalTextColor);
                    type.setTextColor(normalTextColor);
                    messagePrice.setTextColor(normalTextColor);
                    if (model.getItem() != null) {
                        setUpPriceTag();
                    }
                }
            }
        });
        if (model != null) {
            mode = MODE.EDIT;
            if (TextUtils.isEmpty(model.childItemGuid)) {
                description.setText(model.title);
                priceEditbox.setText(priceFormat(model.cost));
                autoApplySelected.setChecked(model.autoApply);
            } else {
                free.setChecked(true);
                itemChooser.setText(model.getItem().description);
                childSelected.setChecked(true);
                setQtyBox(model.getItem());
                description.setText(model.title);
            }
        } else {
            mode = MODE.ADD;
            model = new ModifierExModel();
            model.modifierGroupGuid = groupGuid;
            model.modifierGuid = UUID.randomUUID().toString();
            model.itemGuid = itemGuid;
            model.childItemQty = BigDecimal.ZERO;
            model.type = modType;
            model.autoApply = autoApplySelected.isChecked();
            childSelected.setChecked(false);
            setQtyBox(null);
            refreshEnabled();
        }

        qtyEditbox.addTextChangedListener(new BrandTextWatcher(qtyEditbox));
        priceEditbox.addTextChangedListener(new CurrencyTextWatcher(priceEditbox));

        itemChooser.setAdapter(customerAdapter);
        itemChooser.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ItemExModel item = ((ModifiersAdapter) itemChooser.getAdapter()).getItem(position);
                model.childItemGuid = item.getGuid();
                model.setItem(item);
                childSelected.setChecked(true);
                refreshEnabled();
                setQtyBox(item);
                qtyEditbox.setEnabled(true);
            }
        });

        itemGroupSpinner.setAdapter(groupAdapter);
        itemGroupSpinner.setEnabled(!free.isChecked());
        itemGroupSpinner2.setAdapter(groupAdapter);
        itemGroupSpinner2.setEnabled(free.isChecked());

        Bundle b = new Bundle();
        b.putString(ITEM_KEY, itemGuid);

        switch (modType) {
            case ADDON:
                break;
            case MODIFIER:
                getLoaderManager().restartLoader(0, b, itemGroupLoader);
                break;
            case OPTIONAL:
                priceEditbox.setVisibility(View.GONE);
                costLbl.setVisibility(View.GONE);
                break;
            default: throw new IllegalStateException("no mod type");
        }
    }

    private void setQtyBox(ItemExModel model) {
        if (model == null) {
            resetQtyLabelField("...");

            qtyEditbox.setEnabled(false);
            type.setText("");
            showPrice(priceEditbox, this.model.cost);
        } else {
            resetQtyLabelField(model.shortCut);

            qtyEditbox.setEnabled(true);
            type.setText(StringUtils.valueOf(model.priceType, getActivity()));
            BigDecimal childItemQty = this.model.type == ModifierType.OPTIONAL ? CalculationUtil.negativeQty(this.model.childItemQty) : this.model.childItemQty;
            if (model.priceType == PriceType.UNIT_PRICE) {
                qtyEditbox.setInputType(InputType.TYPE_CLASS_PHONE);
                qtyEditbox.setFilters(new InputFilter[]{new QtyFormatInputFilter()});
                showBrandQty(qtyEditbox, childItemQty);

            } else {
                qtyEditbox.setInputType(InputType.TYPE_CLASS_NUMBER);
                qtyEditbox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
                showBrandQtyInteger(qtyEditbox, childItemQty);
            }
            showPrice(priceEditbox, model.price.multiply(childItemQty));
        }
    }

    protected void resetQtyLabelField(String res) {
        qtyTextview.setText(getString(R.string.composer_dialog_qty, res));
    }

    protected void refreshEnabled() {
        boolean enabled = true;
        if (free.isChecked()) {
            enabled &= !TextUtils.isEmpty(model.childItemGuid);
            if (modType != ModifierType.OPTIONAL) {
                BigDecimal qty = parseBrandQtyInput(qtyEditbox.getText().toString());
                enabled &= qty.compareTo(BigDecimal.ZERO) > 0;
            }
        } else {
            enabled &= !TextUtils.isEmpty(description.getText().toString());
        }
        enabled &= modType != ModifierType.MODIFIER || groupAdapter.getCount() > 0;
        enablePositiveButton(enabled, greenBtnColor);
    }

    protected ModifierModel collectData() {
        if (model.getItem() != null) {
            model.childItemGuid = model.getItem().guid;
            if (model.type == ModifierType.OPTIONAL) {
                model.childItemQty = CalculationUtil.negativeQty(parseBrandQtyInput(qtyEditbox.getText().toString()));
            } else {
                model.childItemQty = parseBrandQtyInput(qtyEditbox.getText().toString());
            }
        }
        model.autoApply = autoApplySelected.isChecked();
        model.cost = parseBigDecimal(priceEditbox.getText().toString());
        if (TextUtils.isEmpty(description.getText().toString())) {
            model.title = "";
        } else {
            model.title = description.getText().toString();
        }
        if (modType == ModifierType.MODIFIER) {
            if (!free.isChecked()){
                model.modifierGroupGuid = ((ModifierGroupModel)itemGroupSpinner.getSelectedItem()).getGuid();
            }else{
                model.modifierGroupGuid = ((ModifierGroupModel)itemGroupSpinner2.getSelectedItem()).getGuid();
            }
        }
        return model;
    }

    public void setCallback(ModifierCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onBarcodeReceived(String barcode) {

    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.modifier_edit_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        if (model == null) {
            switch (modType) {
                case ADDON:  return R.string.addon_dialog_add_title;
                case MODIFIER: return R.string.modifier_dialog_add_title;
                case OPTIONAL:return R.string.noop_dialog_add_title;
            }
        } else {
            switch (modType) {
                case ADDON:return R.string.addon_dialog_edit_title;
                case MODIFIER: return R.string.modifier_dialog_edit_title;
                case OPTIONAL:return R.string.noop_dialog_edit_title;
            }
        }
        return R.string.addon_dialog_add_title;
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
                final ModifierModel dataModel = collectData();
                Logger.d("OnDialogClickListener: onClick "+model.autoApply);
                Logger.d("OnDialogClickListener: onClick "+dataModel.autoApply);
                if (mode == MODE.EDIT) {
                    EditModifiersCommand.start(getActivity(), model);
                    callback.handleSuccess();
                } else if (mode == MODE.ADD) {
                    callback.handleSuccess();
                    AddModifierCommand.start(getActivity(), model);
                }
                return false;
            }
        };
    }


    public static void show(FragmentActivity activity,
                            String itemGuid,
                            String groupGuid,
                            ModifierType type,
                            ModifierExModel model,
                            ModifierCallback callback) {
        DialogUtil.show(activity, DIALOG_NAME, ModifierEditFragment_
                .builder()
                .itemGuid(itemGuid)
                .groupGuid(groupGuid)
                .model(model)
                .modType(type)
                .build())
                .setCallback(callback);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public interface ModifierCallback {

        void handleSuccess(ComposerModel parent);

        void handleSuccess();

        void handleError(String message);

        void handleCancel();
    }

    private class ModifiersAdapter extends BaseItemExAdapter {

        private final ModifierType type;

        public ModifiersAdapter(ModifierType type, Context context) {
            super(context);
            this.type = type;
        }

        @Override
        protected String getCustomSelection() {
            return ShopSchema2.ItemExtView2.ItemTable.GUID + " <> ? AND "
                    + ShopSchema2.ModifierView2.ItemTable.CODE_TYPE + " IS NULL AND "
                    + ShopSchema2.ModifierView2.ItemTable.ITEM_REF_TYPE + " == 0";
        }

        @Override
        protected String[] getCustomSelectionArgs() {
            return new String[]{model.itemGuid};
        }

        @Override
        protected void publishResults(FluentIterable<ItemExModel> cursor) {
            if (mode == MODE.EDIT && firstLoad) {
                // initial edit
                firstLoad = false;
            } else {
                childSelected.setChecked(false);
                model.setItem(null);
                qtyEditbox.setText("");
                qtyEditbox.setEnabled(false);
            }
            refreshEnabled();
        }
    }

    public class QtyFormatInputFilter extends RegexpFormatInputFilter {

      //  private static final String REGEXP = "^(\\d{0,7}(.)*)?(\\,[0-9]{1,2})?$";
        private static final String REGEXP = "(^(\\d{0,7})?(\\,[0-9]{0,3})?-?$)|(^(-?\\d{0,7})?(\\,[0-9]{0,3})?(\\.[0-9]{0,3})?$)";
        public QtyFormatInputFilter() {
            super(REGEXP);
        }
    }

    private static class ItemGroupAdapter extends ObjectsCursorAdapter<ModifierGroupModel> {

        public ItemGroupAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newDropDownView(int position, ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_dropdown_item, parent, false);
            return view;
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            return LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_dark, parent, false);
        }

        @Override
        protected View bindView(View view, int position, ModifierGroupModel item) {
            ((TextView) view).setText(item.title);
            return view;
        }
    }

    private LoaderManager.LoaderCallbacks<List<ModifierGroupModel>> itemGroupLoader = new LoaderManager.LoaderCallbacks<List<ModifierGroupModel>>() {

        private final Uri URI_GROUP = ShopProvider.contentUri(ShopStore.ModifierGroupTable.URI_CONTENT);

        @Override
        public Loader<List<ModifierGroupModel>> onCreateLoader(int i, Bundle bundle) {
            String itemGuid = bundle.getString(ITEM_KEY);
            return CursorLoaderBuilder
                    .forUri(URI_GROUP)
                    .where(ShopStore.ModifierGroupTable.ITEM_GUID + " = ?", itemGuid)
                    .orderBy(ModifierGroupTable.ORDER_NUM)
                    .transform(new Function<Cursor, ModifierGroupModel>() {
                        @Override
                        public ModifierGroupModel apply(Cursor c) {
                            return new ModifierGroupModel(c);
                        }
                    }).build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<ModifierGroupModel>> listLoader, List<ModifierGroupModel> groups) {
            groupAdapter.changeCursor(groups);
            if (!TextUtils.isEmpty(model.modifierGroupGuid)) {
                int position = 0;
                for (ModifierGroupModel model : groups) {
                    if (model.guid != null && model.guid.equals(self().model.modifierGroupGuid)) {
                        itemGroupSpinner.setSelection(position);
                        itemGroupSpinner2.setSelection(position);
                        break;
                    }
                    position++;
                }
            }
            refreshEnabled();
        }

        @Override
        public void onLoaderReset(Loader<List<ModifierGroupModel>> listLoader) {
            groupAdapter.changeCursor(null);
        }
    };

    protected ModifierEditFragment self() {
        return this;
    }
}
