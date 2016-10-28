package com.kaching123.tcr.fragment.prepaid;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ActivationActivity;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless.ActivationTypeChoosingFragmentDialog;
import com.kaching123.tcr.model.ActivationCarrierModel;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by teli.yin on 10/28/2014.
 */
@EFragment
public class PrepaidHomeFragment extends Fragment {
    final public static int WIRELESS = 0;
    final public static int BILLPAYMENT = 1;
    final public static int INTERNATIONAL = 2;
    final public static int PINLESS = 3;
    final public static int SUNPASS = 4;
    final public static int ACTIVATIONCENTER = 5;
    final public static int LONGDISTANCE = 6;
    @ViewById
    protected ImageButton btnWireless;
    @ViewById
    protected ImageButton btnWirelessInter;
    @ViewById
    protected ImageButton btnBill;
    @ViewById
    protected ImageButton btnSunpass;
    @ViewById
    protected ImageButton btnLongDistance;
    @ViewById
    protected ImageButton btnPinless;
    @ViewById
    protected ImageButton activationCenter;
    @FragmentArg
    protected boolean billPaymentActivated;
    @FragmentArg
    protected boolean sunpassActivated;

    private List<ActivationCarrierModel> activationCarriers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.prepaid_type_fragment, container, false);
    }

    prepaidType callback;

    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);
        callback = (prepaidType) a;
    }

    public interface prepaidType {
        public void typeSelected(int type);
    }

    @AfterViews
    public void init() {
        updateSunAndBillButton();
        getLoaderManager().initLoader(0, null, new ActivationLoader());
    }

    protected void updateSunAndBillButton() {
        btnSunpass.setEnabled(sunpassActivated);
        btnBill.setEnabled(billPaymentActivated);
    }

    private class ActivationLoader implements LoaderManager.LoaderCallbacks<List<ActivationCarrierModel>> {

        @Override
        public Loader<List<ActivationCarrierModel>> onCreateLoader(int id, Bundle args) {
            return CursorLoaderBuilder.forUri(ShopProvider.getContentUri(ShopStore.ActivationCarrierTable.URI_CONTENT))
                    .where(ShopStore.ActivationCarrierTable.IS_ACTIVE + " = ?", 1)
                    .transformRow(new ListConverterFunction<ActivationCarrierModel>() {
                        @Override
                        public ActivationCarrierModel apply(Cursor cursor) {
                            return new ActivationCarrierModel(cursor);
                        }
                    }).build(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<ActivationCarrierModel>> loader, List<ActivationCarrierModel> data) {
            activationCarriers = data;
            activationCenter.setEnabled(data != null && !data.isEmpty());
        }

        @Override
        public void onLoaderReset(Loader<List<ActivationCarrierModel>> loader) {
        }
    }

    @Click
    void btnWireless() {
        callback.typeSelected(WIRELESS);
    }

    @Click
    void btnWirelessInter() {
        callback.typeSelected(INTERNATIONAL);
    }

    @Click
    void btnBill() {
        callback.typeSelected(BILLPAYMENT);
    }

    @Click
    void btnSunpass() {
        callback.typeSelected(SUNPASS);
    }

    @Click
    void btnPinless() {
        callback.typeSelected(PINLESS);
    }

    @Click
    void activationCenter() {
        if (activationCarriers == null || activationCarriers.isEmpty())
            return;

        if (activationCarriers.size() == 1) {
            ActivationActivity.start(getActivity(), activationCarriers.get(0).url);
        } else {
            ActivationTypeChoosingFragmentDialog.show(getActivity(), activationCarriers, true);
        }
        callback.typeSelected(ACTIVATIONCENTER);
    }

    @Click
    void btnLongDistance() {

        callback.typeSelected(LONGDISTANCE);
    }
}
