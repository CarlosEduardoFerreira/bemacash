package com.kaching123.tcr.fragment.wireless;

import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.wireless.AddUnitsCommand;
import com.kaching123.tcr.commands.wireless.EditUnitCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.Unit.CodeType;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import java.util.UUID;

/**
 * Created by mayer
 */
@EFragment
public class UnitsEditFragment extends UnitEditFragmentBase {

    private static final String DIALOG_NAME = "UnitsEditFragment";


    protected UnitCallback callback;

    @FragmentArg
    protected String predefSerial;

    public void setCallback(UnitCallback callback) {
        this.callback = callback;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.edit_unit_dialog_fragment;
    }

    @AfterViews
    protected void attachViews() {
        super.attachViews();
        status = Unit.Status.NEW;
        if (predefSerial != null) {
            etSerial.setText(predefSerial);
        }
    }

    @Override
    protected boolean onSubmitForm() {
        if (unit != null) {
            try {
                unit.warrantyPeriod = Integer.parseInt(warrEditbox.getText().toString());
            } catch (NumberFormatException ignore) {
            } catch (NullPointerException ignore) {
            }
            String dataInField = etSerial.getText().toString();
            EditUnitCommand.start(getActivity(), unit, dataInField, new EditUnitCommand.UnitCallback() {
                @Override
                protected void handleSuccess() {
                    callback.handleSuccess();
                }

                @Override
                protected void handleError(String message) {
                    callback.handleScannedSwitch(false);
                    AlertDialogWithCancelFragment.show(getActivity(),
                            R.string.wireless_already_item_title,
                            message,
                            R.string.btn_ok,
                            new AlertDialogWithCancelFragment.OnDialogListener() {
                                @Override
                                public boolean onClick() {
                                    callback.handleScannedSwitch(true);
                                    return true;
                                }

                                @Override
                                public boolean onCancel() {
                                    callback.handleScannedSwitch(true);
                                    return true;
                                }
                            }
                    );
                }
            });
        } else {
            final boolean add;
            Unit u = new Unit();
            u.codeType = type;
            try {
                u.warrantyPeriod = Integer.parseInt(warrEditbox.getText().toString());
            } catch (NumberFormatException ignore) {
            } catch (NullPointerException ignore) {
            }
            u.serialCode = etSerial.getText().toString();
            u.itemId = item.guid;
            u.status = Unit.Status.NEW;
            u.guid = UUID.randomUUID().toString();
            AddUnitsCommand.start(getActivity(),
                    add = purposeSwitch.isChecked(),
                    u, item,
                    new AddUnitsCommand.UnitCallback() {
                        @Override
                        protected void handleSuccess(ItemExModel model) {
                            callback.handleSuccess(add, model);
                        }

                        @Override
                        protected void handleError(String message) {
                            callback.handleScannedSwitch(false);

                            AlertDialogWithCancelFragment.show(getActivity(),
                                    R.string.wireless_already_item_title,
                                    message,
                                    R.string.btn_ok,
                                    new AlertDialogWithCancelFragment.OnDialogListener() {
                                        @Override
                                        public boolean onClick() {
                                            callback.handleScannedSwitch(true);
                                            return true;
                                        }

                                        @Override
                                        public boolean onCancel() {
                                            callback.handleScannedSwitch(true);
                                            return true;
                                        }
                                    }
                            );
//                            callback.handleError(message);
                        }
                    }
            );
        }
        return true;
    }

    @Override
    protected int getDialogTitle() {
        if (type != null) switch (type) {
            case ICCID:
                return R.string.dlg_unit_iccid_edit;
            case IMEI:
                return R.string.dlg_unit_imei_edit;
            case SN:
            default:
                return R.string.dlg_unit_sn_edit;
        }
        else return R.string.dlg_unit_sn_edit;
    }

    @Override
    protected boolean hasToPlayTune() {
        return false;
    }


    protected int getPreferredContentWidth() {
        return R.dimen.sn_dialog_width;
    }

    public static void show(FragmentActivity activity, ItemExModel model, Unit unit, CodeType type, String predefSerial, UnitCallback callback) {
        DialogUtil.show(activity, DIALOG_NAME, UnitsEditFragment_
                .builder()
                .unit(unit)
                .type(type)
                .item(model)
                .predefSerial(predefSerial)
                .build())
                .setCallback(callback);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                callback.handleCancel();
                return true;
            }
        };
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        callback.handleCancel();
        super.onCancel(dialog);
    }

    public interface UnitCallback {

        void handleSuccess(boolean add, ItemExModel parent);

        void handleSuccess();

        void handleError(String message);

        void handleCancel();

        void handleScannedSwitch(boolean on);
    }
}
