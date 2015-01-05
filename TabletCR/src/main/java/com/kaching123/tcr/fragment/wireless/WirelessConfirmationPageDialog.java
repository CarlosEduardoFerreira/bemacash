package com.kaching123.tcr.fragment.wireless;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.PrepaidReceiptAdapter;
import com.kaching123.tcr.commands.wireless.WirelessConfirmationCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
@EFragment
public class WirelessConfirmationPageDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = "SunpassAmountFragmentDialog";

    private final static BigDecimal FEE_AMOUNT = new BigDecimal(1.5);
    private final String DollarAmpsand = "$";

    protected WirelessConfirmationPageDialogCallback callback;
    @FragmentArg
    protected boolean international;
    @FragmentArg
    protected String phoneNumber;
    @FragmentArg
    protected int profileId;
    @FragmentArg
    protected WirelessItem chosenCategory;
    @FragmentArg
    protected BigDecimal mAmount;
    @FragmentArg
    protected String orderGuid;


    @ViewById
    protected ListView listView;
    @ViewById
    protected LinearLayout linearLayout;
    @ViewById
    protected TextView productName, productOrdernum, productNameDisplay, total, telephoneNumberDisplay;
    @ViewById
    protected ImageView productImageview;

    @ColorRes(R.color.prepaid_dialog_white)
    protected int colorOk;
    @ColorRes(R.color.gray_dark)
    protected int colorDisabled;

    private PrepaidReceiptAdapter adapter;

    public void setCallback(WirelessConfirmationPageDialogCallback callback) {
        this.callback = callback;
    }


    @AfterViews
    void afterViewInitList() {
        if (!chosenCategory.isPinBased()) {
            linearLayout.setVisibility(View.VISIBLE);
            telephoneNumberDisplay.setText(phoneNumber);
        }
        productName.setText(chosenCategory.name);
        UrlImageViewHelper.setUrlDrawable(productImageview, chosenCategory.iconUrl, R.drawable.operator_default_icon, 60000);
        productNameDisplay.setText(chosenCategory.name);
        total.setText(mAmount.toString());


    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getNegativeButton().setTextColor(Color.WHITE);
        getPositiveButton().setTextColor(colorOk);
        getNegativeButton().setTextSize(25);
        getPositiveButton().setTextSize(25);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.prepaid_supass_choice_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.default_dlg_heigth));
    }

    @Override
    protected int getTitleGravity() {
        return Gravity.LEFT;
    }

    ;

    @Override
    protected int getSeparatorColor() {
        return Color.WHITE;
    }

    @Override
    protected int getTitleTextColor() {
        return Color.WHITE;
    }

    @Override
    protected int getTitleViewBackgroundColor() {
        return getResources().getColor(R.color.prepaid_dialog_title_background_color);
    }

    @Override
    protected int getButtonsBackgroundColor() {
        return getResources().getColor(R.color.prepaid_dialog_buttons_background_color);
    }

    @Override
    protected int getTitleIcon() {
        switch (chosenCategory.type) {
            case NATIONAL_TOP_UP:
                return R.drawable.icon_wireless_recharge;
            case INTERNATIONAL_WIRELESS_PIN:
                return R.drawable.icon_wireless_recharge;
            case PINLESS:
                return R.drawable.icon_pinless;
            case INTERNATIONAL_TOP_UP:
                return R.drawable.icon_international_topup;
            case LONG_DISTANCE_PIN:
                return R.drawable.icon_long_distance;
            case NATIONAL_WIRELESS_PIN:
                return R.drawable.icon_wireless_recharge;
        }
        return 0;
    }

    @Override
    protected int getDialogTitle() {
        switch (chosenCategory.type) {
            case NATIONAL_TOP_UP:
                return R.string.prepaid_dialog_wireless_recharge_title;
            case INTERNATIONAL_WIRELESS_PIN:
                return R.string.prepaid_dialog_wireless_recharge_title;
            case PINLESS:
                return R.string.prepaid_dialog_pinless_recharge_title;
            case INTERNATIONAL_TOP_UP:
                return R.string.prepaid_dialog_wireless_recharge_international_title;
            case LONG_DISTANCE_PIN:
                return R.string.prepaid_dialog_long_distance_title;
            case NATIONAL_WIRELESS_PIN:
                return R.string.prepaid_dialog_wireless_recharge_title;
        }
        return R.string.blackstone_pay_confirm_title;
    }


    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_back;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_confirm;
    }

    @Override
    protected boolean hasNegativeButton() {
        return true;
    }

    @Override
    protected boolean hasPositiveButton() {
        return true;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.wireless_payment_confirm;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                complete();
                return false;
            }
        };
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                callback.onCancel();
                return false;
            }
        };
    }

    protected boolean complete() {
        callback.onComplete();
        return true;
    }


    public static void show(FragmentActivity context, final boolean international, final String phoneNumber, final int profileId, WirelessItem chosenCategory, BigDecimal mAmount,
                            String orderGuid, WirelessConfirmationPageDialogCallback listener) {
        WirelessConfirmationPageDialog dialog = WirelessConfirmationPageDialog_.builder()
                .international(international)
                .phoneNumber(phoneNumber)
                .profileId(profileId)
                .chosenCategory(chosenCategory)
                .mAmount(mAmount)
                .orderGuid(orderGuid)
                .build();
        dialog.setCallback(listener);
        DialogUtil.show(context,
                DIALOG_NAME,
                dialog);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }


    public interface WirelessConfirmationPageDialogCallback {

        public abstract void onCancel();

        public abstract void onError(String message);

        public abstract void onComplete();
    }

}
