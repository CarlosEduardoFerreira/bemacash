package com.kaching123.tcr.fragment.wireless;

import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.googlecode.androidannotations.annotations.AfterTextChange;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.wireless.AddUnitsCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogWithCancelFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.Unit.CodeType;

import java.util.UUID;

/**
 * Created by mayer
 */
@EFragment
public class UnitsRecursiveAddFragment extends UnitEditFragmentBase {

    private static final String DIALOG_NAME = "UnitsRecursiveAddFragment";

    @ViewById
    protected EditText counterEditbox;

    protected UnitCallback callback;

    public void setCallback(UnitCallback callback) {
        this.callback = callback;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.add_recursive_dialog_fragment;
    }

    @AfterViews
    protected void attachViews() {
        super.attachViews();
        status = Unit.Status.NEW;
        msg.setText(R.string.wireless_recursive_edit);
        warrEditbox.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        warrEditbox.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    counterEditbox.requestFocus();
                    return true;
                }
                return false;
            }
        });
        counterEditbox.setImeOptions(EditorInfo.IME_ACTION_DONE);
        counterEditbox.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    if (getPositiveButton().isEnabled()){
                        onSubmitForm();
                        return true;
                    }
                    return false;
                }
                return false;
            }
        });
    }

    @Override
    protected void checkPositiveButtonCondition() {
        super.checkPositiveButtonCondition();
        counterEditbox.setTextColor(additionalCheckPassed() ? normalTextColor : errorTextColor);
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                callback.handleSuccess(item);
                return false;
            }
        };
    }

    @AfterTextChange
    protected void counterEditboxAfterTextChanged(Editable s) {
        checkPositiveButtonCondition();
    }

    @Override
    protected boolean additionalCheckPassed() {
        String text = counterEditbox.getText().toString();
        if (text.length() == 0) {
            return true;
        }
        int currentCounter = getTestOrNull(counterEditbox);
        return currentCounter > 0;
    }

    private boolean resetAfterResult() {
        recollect();
        String text = counterEditbox.getText().toString();
        int currentCounter = getTestOrNull(counterEditbox);
        currentCounter--;
        if (text.length() == 0 || currentCounter > 0) {
            if (currentCounter > 0) {
                counterEditbox.setText(String.valueOf(currentCounter));
            }
            etSerial.setText("");
            etSerial.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private int getTestOrNull(EditText view) {
        int currentCounter;
        try {
            currentCounter = Integer.parseInt(counterEditbox.getText().toString());
        } catch (NumberFormatException ignore) {
            currentCounter = 0;
        }
        return currentCounter;
    }

    @Override
    protected boolean onSubmitForm() {
        Unit u = new Unit();
        u.codeType = type;
        try {
            u.warrantyPeriod = Integer.parseInt(warrEditbox.getText().toString());
        } catch (NumberFormatException ignore) {
            // http://194.79.22.58:8080/browse/ACR-1191 - we forbid
            return false;
        }
        catch (NullPointerException ignore) { }
        u.serialCode = etSerial.getText().toString();
        u.itemId = item.guid;
        u.status = Unit.Status.NEW;
        u.guid = UUID.randomUUID().toString();
        AddUnitsCommand.start(getActivity(), purposeSwitch.isChecked(), u, item, new AddUnitsCommand.UnitCallback() {

            @Override protected void handleSuccess(ItemExModel model) {
                item = model;
                if (resetAfterResult()) {
                    callback.handleSuccess(item);
                }
            }

            @Override protected void handleError(String message) {
                callback.turnScanner(false);
                AlertDialogWithCancelFragment.show(getActivity(),
                        R.string.wireless_already_item_title,
                        message,
                        R.string.btn_ok,
                        new AlertDialogWithCancelFragment.OnDialogListener() {
                            @Override
                            public boolean onClick() {
                                callback.turnScanner(true);
                                return true;
                            }

                            @Override
                            public boolean onCancel() {
                                callback.turnScanner(true);
                                return true;
                            }
                        }
                );
            }
        });
        return true;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        callback.handleSuccess(item);
        super.onCancel(dialog);
    }

    @Override
    protected int getDialogTitle() {
        if (type != null) switch (type) {
            case ICCID: return R.string.dlg_unit_iccid_add;
            case IMEI: return R.string.dlg_unit_imei_add;
            case SN:
            default: return R.string.dlg_unit_sn_add;
        } else return R.string.dlg_unit_sn_add;
    }

    @Override
    protected boolean hasToPlayTune() {
        return false;
    }

    public void onBarcodeReceived(String barcode) {
        super.onBarcodeReceived(barcode);
        onSubmitForm();
    }

    protected int getPreferredContentWidth() {
        return R.dimen.sn_dialog_width;
    }

    public static void show(FragmentActivity activity, ItemExModel model, Unit unit, CodeType type, UnitCallback callback) {
        DialogUtil.show(activity, DIALOG_NAME, UnitsRecursiveAddFragment_.builder().unit(unit).type(type).item(model).build()).setCallback(callback);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public interface UnitCallback {
        void handleSuccess(ItemExModel parent);
        void handleError(String message);
        void turnScanner(boolean on);
    }
}