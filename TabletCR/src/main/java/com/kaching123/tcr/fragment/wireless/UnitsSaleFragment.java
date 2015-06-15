package com.kaching123.tcr.fragment.wireless;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.wireless.UnitOrderDoubleCheckCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.Unit;

/**
 * Created by idyuzheva on 07.07.2014.
 */
@EFragment
public class UnitsSaleFragment extends UnitEditFragmentBase {

    private static final String DIALOG_NAME = "UnitsSaleFragment";

    @FragmentArg
    protected UnitActionType actionType;

    @ViewById
    protected TextView descriptionTextview;

    protected UnitSaleCallback callback;

    public enum UnitActionType {ADD_TO_ORDER, REMOVE_FROM_ORDER};

    @Override
    protected int getDialogContentLayout() {
        return R.layout.edit_unit_dialog_fragment;
    }

    public void setCallback(UnitSaleCallback callback) {
        this.callback = callback;
    }

    @Override
    protected boolean hasToPlayTune() {
        return true;
    }

    @AfterViews
    protected void attachViews() {
        status = Unit.Status.NEW;
        super.attachViews();
        warrEditbox.setText("1");
        warrEditbox.setVisibility(View.GONE);
        warrTextview.setVisibility(View.GONE);
        purposeSwitch.setVisibility(View.GONE);
        if (item != null) {
            descriptionTextview.setVisibility(View.VISIBLE);
            descriptionTextview.setText(item.description);
        }
    }

    public void onBarcodeReceived(String barcode) {
        super.onBarcodeReceived(barcode);
        onSubmitForm();
    }

    @Override
    protected void updateMsgWithValues(int qty) {
        if (actionType == UnitActionType.REMOVE_FROM_ORDER) {
            msg.setText(getString(R.string.dlg_sn_remove_descr, type == null ? "unknown" : type.toString()));
        } else {
            msg.setText(getString(R.string.dlg_sn_add_descr, qty == 0 ? "no" : qty, type == null ? "unknown" : type.toString()));
        }
    }

    public static void show(FragmentActivity activity, ItemExModel model, Unit unit, UnitActionType actionType, Unit.CodeType type, UnitSaleCallback callback) {
        DialogUtil.show(activity, DIALOG_NAME, UnitsSaleFragment_.builder().unit(unit).actionType(actionType).type(type).item(model).build()).setCallback(callback);
    }

    private boolean isSaleAvailable(Unit unitItem, String currentSerialCode) {
        return unitItem.serialCode.equals(currentSerialCode);
    }

    private void handleItemEligible() {
        callback.handleSuccess(unit);
    }

    private void handleItemNotEligible() {
        AlertDialogWithCancelFragment.showWithTwo(getActivity(),
                R.string.wireless_already_item_title,
                "This item has been marked as " + unit.status.toString() + ". Would you like to sell it again?",
                R.string.btn_confirm,
                new AlertDialogWithCancelFragment.OnDialogListener() {
                    @Override
                    public boolean onClick() {
                        callback.handleSuccess(unit);
                        return true;
                    }

                    @Override
                    public boolean onCancel() {
//                        callback.handleCancelling();
                        return true;
                    }
                }
        );
    }

    @Override
    protected boolean onSubmitForm() {
        unit = null;
        final String currentSerialCode = etSerial.getText().toString();
        for (Unit currentUnit : units) {
            if (isSaleAvailable(currentUnit, currentSerialCode)) {
                Logger.d("Item was found in stock: " + currentUnit.toString());
                unit = currentUnit;
            }
        }
        if (UnitActionType.ADD_TO_ORDER == actionType) {
            if (unit != null) {
                if (unit.orderId == null) {
                    handleItem(unit);
                } else {
                    doubleCheck();
                }
            } else {
                callback.handleError(getString(R.string.wireless_item_not_found_label));
            }
        } else if (UnitActionType.REMOVE_FROM_ORDER == actionType) {
            if (unit != null) {
                callback.handleSuccess(unit);
            } else {
                callback.handleError(getString(R.string.wireless_item_not_found_label));
            }
        }
        return true;
    }

    @Override
    protected String getSelectionOrderId() {
        if (UnitActionType.REMOVE_FROM_ORDER == actionType)
            return getApp().getCurrentOrderGuid();

        return super.getSelectionOrderId();
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener(){
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                callback.handleCancelling();
                return true;
            }
        };
    }

    public interface UnitSaleCallback {

        void handleSuccess(Unit unit);
        void handleError(String message);
        void handleCancelling();
    }

    private void handleItem(Unit unit) {
        unit.serialCode = etSerial.getText().toString();
        unit.codeType = type;
        if (!unit.status.equals(Unit.Status.NEW)) {
            handleItemNotEligible();
        } else {
            handleItemEligible();
        }
    }

    private void doubleCheck() {
        if (unit.orderId.equals(getApp().getCurrentOrderGuid())) {
            callback.handleError("This serial is already added to this particular order.");
            return;
        }
        UnitOrderDoubleCheckCommand.start(getActivity(), unit, getApp().getCurrentOrderGuid(), new UnitOrderDoubleCheckCommand.UnitCallback() {
            @Override
            protected void handleSuccess() {
                AlertDialogWithCancelFragment.showWithTwo(getActivity(), R.string.wireless_already_item_title,
                    "This item was bound to some other order, which is currently not completed. Would you like to sell it anyway?",
                    R.string.btn_ok,
                    new AlertDialogWithCancelFragment.OnDialogListener() {
                        @Override
                        public boolean onClick() {
                            handleItem(unit);
                            return true;
                        }

                        @Override
                        public boolean onCancel() {
                            return true;
                        }
                    }
                );

            }

            @Override
            protected void handleError() {
                callback.handleError(getString(R.string.wireless_item_used_label));
            }

            @Override
            protected void handleFeelFreeToAdd() {
                if (!unit.status.equals(Unit.Status.NEW)) {
                    handleItemNotEligible();
                } else {
                    handleItemEligible();
                }
            }
        });

    }
}
