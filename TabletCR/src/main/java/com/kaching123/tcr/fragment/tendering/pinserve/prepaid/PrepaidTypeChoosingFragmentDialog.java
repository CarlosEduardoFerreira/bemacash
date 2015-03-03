package com.kaching123.tcr.fragment.tendering.pinserve.prepaid;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ActivationActivity;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless.ActivationTypeChoosingFragmentDialog;
import com.kaching123.tcr.model.ActivationCarrierModel;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.model.payment.blackstone.prepaid.Broker;
import com.kaching123.tcr.store.ShopProvider;

import java.util.List;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
@EFragment
public class PrepaidTypeChoosingFragmentDialog extends StyledDialogFragment implements OnClickListener {

    private static final String DIALOG_NAME = "PrepaidTypeChoosingFragmentDialog";

    private PrepaidTypeChoosingFragmentDialogCallback callback;

    @ViewById protected ImageButton btnWireless;
    @ViewById protected ImageButton btnWirelessInter;
    @ViewById protected ImageButton btnBill;
    @ViewById protected ImageButton btnSunpass;
    @ViewById protected ImageButton btnLongDistance;
    @ViewById protected ImageButton btnPinless;
    @ViewById protected ImageButton activationCenter;

    private List<ActivationCarrierModel> activationCarriers;

    public void setCallback(PrepaidTypeChoosingFragmentDialogCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getNegativeButton().setTextColor(Color.WHITE);
        getNegativeButton().setTextSize(25);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.prepaid_type_dlg_width),
                getDialog().getWindow().getAttributes().height);
    }

    @AfterViews
    protected void init() {
        btnWireless.setOnClickListener(this);
        btnWirelessInter.setOnClickListener(this);
        btnBill.setOnClickListener(this);
        btnSunpass.setOnClickListener(this);
        btnLongDistance.setOnClickListener(this);
        btnPinless.setOnClickListener(this);
        activationCenter.setOnClickListener(this);

//        getLoaderManager().initLoader(0, null, new ActivationLoader());
    }

    @Override
    public void onClick(View v) {
        assert callback != null;
        if (btnWireless.equals(v)) {
            callback.onTypeSelected(Broker.WIRELESS_RECHARGE);
        } else if (btnWirelessInter.equals(v)) {
            callback.onTypeSelected(Broker.INTERNATIONAL_TOPUP);
        } else if (btnBill.equals(v)) {
            callback.onTypeSelected(Broker.BILL_PAYMENT);
        } else if (btnSunpass.equals(v)) {
            callback.onTypeSelected(Broker.SUNPASS);
        } else if (btnLongDistance.equals(v)) {
            callback.onTypeSelected(Broker.LONG_DISTANCE);
        } else if (btnPinless.equals(v)) {
            callback.onTypeSelected(Broker.PINLESS);
        } else if (activationCenter.equals(v)) {
            activationCenterClicked();
        }
    }

    private void activationCenterClicked() {
        if (activationCarriers == null || activationCarriers.isEmpty())
            return;

        if (activationCarriers.size() == 1){
            ActivationActivity.start(getActivity(), activationCarriers.get(0).url);
        }else{
            ActivationTypeChoosingFragmentDialog.show(getActivity(), activationCarriers, true);
        }

        callback.onCancel();
    }

    @Override protected  int getSeparatorColor(){return Color.WHITE;}

    @Override protected  int getTitleTextColor(){return Color.WHITE;}

    @Override protected  int getTitleViewBackgroundColor(){return getResources().getColor(R.color.prepaid_dialog_title_background_color); }

    @Override protected  int getButtonsBackgroundColor(){return getResources().getColor(R.color.prepaid_dialog_buttons_background_color); }

    @Override protected  boolean hasTitleTextTypeface(){ return true; }

    @Override protected  boolean hasNegativeButtonTextTypeface(){ return true; }

//    @Override protected  int getTitleTextTypeface(){ return Typeface.BOLD; }

    @Override protected  int getNegativeButtonTextTypeface(){ return Typeface.BOLD; }

    protected  int getTitleIcon(){return R.drawable.icon_prepaid;};

    protected  int getTitleGravity(){return Gravity.LEFT;};

    @Override
    protected int getDialogContentLayout() {
        return R.layout.prepaid_type_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.prepaid_dialog_type_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return 0;
    }

    @Override
    protected boolean hasNegativeButton() {
        return true;
    }

    @Override
    protected boolean hasPositiveButton() {
        return false;
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
                callback.onCancel();
                return false;
            }
        };
    }

//    private class ActivationLoader implements LoaderCallbacks<List<ActivationCarrierModel>>{
//
//        @Override
//        public Loader<List<ActivationCarrierModel>> onCreateLoader(int id, Bundle args) {
//            return CursorLoaderBuilder.forUri(ShopProvider.getContentUri(ActivationCarrierTable.URI_CONTENT))
//                    .where(ActivationCarrierTable.IS_ACTIVE + " = ?", 1)
//                    .transform(new ListConverterFunction<ActivationCarrierModel>() {
//                        @Override
//                        public ActivationCarrierModel apply(Cursor cursor) {
//                            return new ActivationCarrierModel(cursor);
//                        }
//                    }).build(getActivity());
//        }
//
//        @Override
//        public void onLoadFinished(Loader<List<ActivationCarrierModel>> loader, List<ActivationCarrierModel> data) {
//            activationCarriers = data;
//            activationCenter.setEnabled(data != null && !data.isEmpty());
//        }
//
//        @Override
//        public void onLoaderReset(Loader<List<ActivationCarrierModel>> loader) {}
//    }

    public static void show(FragmentActivity context, PrepaidTypeChoosingFragmentDialogCallback listener) {
        PrepaidTypeChoosingFragmentDialog dialog = PrepaidTypeChoosingFragmentDialog_.builder().build();
        dialog.setCallback(listener);

        DialogUtil.show(context, DIALOG_NAME, dialog);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public interface PrepaidTypeChoosingFragmentDialogCallback {

        public abstract void onCancel();

        public abstract void onTypeSelected(Broker broker);
    }
}
