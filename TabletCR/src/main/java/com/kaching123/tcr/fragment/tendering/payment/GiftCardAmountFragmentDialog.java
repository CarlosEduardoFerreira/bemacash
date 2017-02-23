package com.kaching123.tcr.fragment.tendering.payment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.WaitForCashInDrawerCommand;
import com.kaching123.tcr.commands.display.DisplayTenderCommand;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.payment.cash.CashSaleCommand;
import com.kaching123.tcr.component.CashAdjustableNumpadView;
import com.kaching123.tcr.component.CashAdjustableNumpadView.IExactClickListener;
import com.kaching123.tcr.component.CurrencyFormatInputFilter;
import com.kaching123.tcr.component.CurrencyTextWatcher;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.component.GiftCardAmountAdjustableNumpadView;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.tendering.OpenDrawerListener;
import com.kaching123.tcr.fragment.tendering.OpenDrawerListener.IDrawerFriend;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.service.DisplayService.IDisplayBinder;
import com.kaching123.tcr.util.AnimationUtils;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.math.BigDecimal;

import static com.kaching123.tcr.fragment.UiHelper.priceFormat;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public class GiftCardAmountFragmentDialog extends StyledDialogFragment implements CustomEditBox.IKeyboardSupport {

    private static final String DIALOG_NAME = "GiftCardAmountFragmentDialog";

    @ViewById
    protected CustomEditBox charge;

    @ViewById
    protected ViewFlipper flipper;

    @ViewById
    protected GiftCardAmountAdjustableNumpadView containerHolder;

    @ColorRes(R.color.dlg_text_green)
    protected int colorPaymentOk;

    @ColorRes(R.color.dlg_text_red)
    protected int colorPaymentPending;

    @ColorRes(R.color.dlg_btn_text_disabled)
    protected int colorPaymentDisabled;

    protected IGiftCardListener listener;

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void attachMe2Keyboard(CustomEditBox v) {
        containerHolder.attachEditView(v);
    }

    @Override
    public void detachMe4Keyboard(CustomEditBox v) {
        containerHolder.detachEditView();
    }

    @AfterViews
    protected void attachViews() {
        containerHolder.attachEditView(charge);
        containerHolder.setExactClickListener(new GiftCardAmountAdjustableNumpadView.IExactClickListener() {
            @Override
            public void onExactClicked() {
                getPositiveButtonListener().onClick();
            }
        });
        setChargeView();
        AnimationUtils.applyFlippingEffect(getActivity(), flipper);
    }

    private void setChargeView() {
        charge.setKeyboardSupportConteiner(this);
        charge.setFilters(new InputFilter[]{new CurrencyFormatInputFilter()});
        charge.addTextChangedListener(new CurrencyTextWatcher(charge, 2));
//        charge.setEditListener(new CustomEditBox.IEditListener() {
//
//            @Override
//            public boolean onChanged(String text) {
//                return try2GetCash(false);
//            }
//        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.pay_cash_dialog_width),
                getResources().getDimensionPixelOffset(R.dimen.pay_cash_dialog_height));
//        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
        initLabels();
        turnPositiveButton(false);
        setCancelable(false);
    }

    private void initLabels() {
        charge.requestFocus();
        getPositiveButton().setTypeface(null, Typeface.BOLD);
    }

    @AfterTextChange
    protected void chargeAfterTextChanged(Editable text) {
        if (text == null) {
            turnPositiveButton(false);
            return;
        }
        BigDecimal tenderAmount = BigDecimal.ZERO;
        BigDecimal changeAmount = BigDecimal.ZERO;
        String chargeStr = charge.getText().toString();

        try {
            tenderAmount = UiHelper.parseBrandDecimalInput(chargeStr);
            if (tenderAmount.compareTo(BigDecimal.ZERO) == 1)
                turnPositiveButton(true);
        } catch (NumberFormatException e) {
            turnPositiveButton(false);
        }
        if (getDisplayBinder() != null) {
            getDisplayBinder().startCommand(new DisplayTenderCommand(tenderAmount, changeAmount));
        }
    }


    private IDisplayBinder getDisplayBinder() {
        if (getActivity() instanceof IDisplayBinder) {
            return (IDisplayBinder) getActivity();
        }
        return null;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.gift_card_amount_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.blackstone_pay_charge_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_back;
    }

    @Override
    protected boolean hasPositiveButton() {
        return true;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_accept;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                listener.onPaymentAmountSelected(new BigDecimal(charge.getText().toString().replaceAll(",", "")));
                return true;
            }
        };
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                return true;
            }
        };
    }

    public GiftCardAmountFragmentDialog setListener(IGiftCardListener listener) {
        this.listener = listener;
        return this;
    }

    private void turnPositiveButton(boolean on) {
        containerHolder.setEnterEnabled(on);

        getPositiveButton().setEnabled(on);
        getPositiveButton().setTextColor(on ? colorPaymentOk : colorPaymentDisabled);
    }

    private void turnNegativeButton(boolean on) {
        getNegativeButton().setTextColor(on ? colorPaymentOk : colorPaymentDisabled);
        getNegativeButton().setEnabled(on);
    }

    public static interface IGiftCardListener {

        void onPaymentAmountSelected(BigDecimal amount);

        void onCancel();

    }

    public static GiftCardAmountFragmentDialog show(FragmentActivity context, IGiftCardListener listener) {
        Logger.d("About to show second dialog");
        return DialogUtil.show(context, DIALOG_NAME, GiftCardAmountFragmentDialog_.builder().build()).setListener(listener);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
