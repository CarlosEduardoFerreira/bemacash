package com.kaching123.tcr.fragment.saleorder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;

import static com.kaching123.tcr.fragment.UiHelper.showInteger;
import static com.kaching123.tcr.fragment.UiHelper.showPercentInBrackets;
import static com.kaching123.tcr.fragment.UiHelper.showQuantity;

@EViewGroup(R.layout.saleorder_items_item_view)
public class ItemView extends FrameLayout {

    @ViewById
    public TextView itemTitle;

    @ViewById
    protected TextView itemTitleHidden;

    @ViewById
    protected TextView itemEan;

    @ViewById
    protected TextView itemSerialCodesTitle;

    @ViewById
    protected TextView itemSerialCodes;

    @ViewById
    protected TextView itemNotes;

    @ViewById
    protected TextView itemEanHidden;

    @ViewById
    protected TextView itemSerialTitleHidden;

    @ViewById
    protected TextView itemSerialHidden;

    @ViewById
    public TextView itemQty;

    @ViewById
    protected TextView itemUnits;

    @ViewById
    protected TextView itemUnitPrice;

    @ViewById
    protected TextView itemDiscountValue;

    @ViewById
    protected TextView itemDiscountPercent;

    @ViewById
    protected TextView itemSubtotal;

    @ViewById
    protected View priceBlock;

    @ViewById
    protected View discountBlock;

    @ViewById
    protected View itemQtyBlock;

    @ViewById
    protected ImageButton notesButton;

    /*private String itemGuid;
    private String orderGuid;

    private BigDecimal qty;
    private BigDecimal price;*/

    private int position;

    private OnItemRemoveClick listener;

    public ItemView(Context context) {
        super(context);
    }

    public ItemView setListener(OnItemRemoveClick listener) {
        this.listener = listener;
        return this;
    }

    @Click
    protected void btnItemRemoveClicked() {
        if (listener != null) {
            listener.onRemoveClicked(this, position);
        }
    }

    @Click
    protected void btnItemCancelClicked() {
        if (listener != null) {
            listener.onCancelClicked(this, position);
        }
    }

    @Click
    protected void itemQtyBlockClicked() {
        if (listener != null) {
            listener.onQtyClicked(this, position);
        }
    }

    @Click
    protected void titleBlockClicked() {
        if (listener != null) {
            listener.onTitleClicked(this, position);
        }
    }

    @Click
    protected void priceBlockClicked() {
        if (listener != null) {
            listener.onPriceClicked(this, position);
        }
    }

    @Click
    protected void discountBlockClicked() {
        if (listener != null) {
            listener.onDiscountClicked(this, position);
        }
    }

    @Click
    protected void notesButtonClicked() {
        if (listener != null) {
            listener.onNotesClicked(this, position);
        }
    }

    private void showHiddenSerialCodes(String codes){
        if (!TextUtils.isEmpty(codes)) {
            this.itemSerialHidden.setVisibility(View.VISIBLE);
            this.itemSerialTitleHidden.setVisibility(View.VISIBLE);
            this.itemSerialHidden.setText(codes);
        } else {
            this.itemSerialHidden.setVisibility(View.GONE);
            this.itemSerialTitleHidden.setVisibility(View.GONE);
        }
    }

    public void bind(int position, Drawable pencilDrawable, Drawable pencilTransparent, String title, String ean, String productCode, String productCodes, BigDecimal qty, String unitsLabel, BigDecimal price, PriceType priceType, boolean discountable, BigDecimal discount, DiscountType discountType, Spannable subTitle, String notes, boolean hasNotes) {
        this.position = position;

        this.itemUnitPrice.setCompoundDrawables(null, null, priceType == PriceType.OPEN ? pencilDrawable : pencilTransparent, null);

        priceBlock.setClickable(priceType == PriceType.OPEN);
        priceBlock.setEnabled(priceType == PriceType.OPEN);

        discountBlock.setClickable(discountable);
        discountBlock.setEnabled(discountable);

        this.itemTitle.setText(title);

        if (TextUtils.isEmpty(subTitle)) {
            this.itemEan.setVisibility(View.GONE);
        } else {
            this.itemEan.setVisibility(View.VISIBLE);
            this.itemEan.setText(subTitle);
        }

        notesButton.setVisibility(hasNotes ? View.VISIBLE : View.INVISIBLE);

        if (TextUtils.isEmpty(notes)) {
            this.itemNotes.setVisibility(View.GONE);
        } else {
            this.itemNotes.setVisibility(View.VISIBLE);
            this.itemNotes.setText(notes);
        }

        this.itemTitleHidden.setText(title);
        if (TextUtils.isEmpty(subTitle)) {
            this.itemEanHidden.setVisibility(View.GONE);
        } else {
            this.itemEanHidden.setVisibility(View.VISIBLE);
            this.itemEanHidden.setText(subTitle);
        }
        showHiddenSerialCodes(productCodes);

        this.itemUnits.setText(unitsLabel);
//        this.itemUnits.setVisibility(unitsLabel == null ? View.GONE : View.VISIBLE);
        if (unitsLabel == null) {
            showInteger(this.itemQty, qty);
        } else {
            showQuantity(this.itemQty, qty);
        }

        showPrice(this.itemUnitPrice, price);

        if (discount == null || BigDecimal.ZERO.compareTo(discount) == 0) {
            this.itemDiscountValue.setVisibility(View.INVISIBLE);
            this.itemDiscountPercent.setVisibility(View.INVISIBLE);
        } else {
            this.itemDiscountValue.setVisibility(View.VISIBLE);
            BigDecimal discountValue = CalculationUtil.getItemDiscountValue(/*qty, */price, discount, discountType);
            showPrice(this.itemDiscountValue, discountValue);
            if (discountType == DiscountType.PERCENT) {
                this.itemDiscountPercent.setVisibility(View.VISIBLE);
                showPercentInBrackets(this.itemDiscountPercent, discount);
            } else {
                this.itemDiscountPercent.setVisibility(View.GONE);
            }
        }

        BigDecimal subTotal = CalculationUtil.getSubTotal(qty, price, discount, discountType);
        showPrice(this.itemSubtotal, subTotal);
    }

    protected void showPrice(TextView label, BigDecimal value) {
        UiHelper.showPrice(label, value);
    }

    public static interface OnItemRemoveClick {
        void onRemoveClicked(View v, int position);

        void onCancelClicked(View v, int position);

        void onQtyClicked(View v, int position);

        void onPriceClicked(View v, int position);

        void onDiscountClicked(View v, int position);

        void onTitleClicked(View v, int position);

        void onNotesClicked(View v, int position);
    }
}
