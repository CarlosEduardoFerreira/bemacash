package com.kaching123.tcr.fragment.wireless;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.googlecode.androidannotations.annotations.AfterTextChange;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.ColorRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.wireless.CollectUnitsCommand;
import com.kaching123.tcr.fragment.barcode.BarCodeEditBox.Formatter;
import com.kaching123.tcr.fragment.barcode.BarCodeEditBox.IFormatter;
import com.kaching123.tcr.fragment.barcode.BarCodeEditBox.IccidInputFilter;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.Unit.CodeType;

import java.util.List;

/**
 * Created by mayer
 */
@EFragment
public abstract class UnitEditFragmentBase extends StyledDialogFragment implements BarcodeReceiver {

    @FragmentArg
    protected CodeType type;

    @FragmentArg
    protected ItemExModel item;

    @FragmentArg
    protected Unit unit;

    @ViewById
    protected TextView msg;

    @ViewById
    protected Switch purposeSwitch;

    @ViewById
    protected TextView tvSerial;

    @ViewById
    protected EditText etSerial;

    @ViewById
    protected EditText warrEditbox;

    @ViewById
    protected TextView warrTextview;

    @ViewById
    protected View progressBar;

    @ColorRes(R.color.light_gray)
    protected int normalTextColor;

    @ColorRes(R.color.subtotal_tax_empty)
    protected int errorTextColor;

    protected List<Unit> units;

    protected Unit.Status status;

    protected IFormatter formatter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelSize(R.dimen.default_dlg_width),
                getDialog().getWindow().getAttributes().height);
    }

    @Override
    protected int getDialogTitle() {
        if (type != null) switch (type) {
            case ICCID: return R.string.dlg_unit_iccid_edit;
            case IMEI: return R.string.dlg_unit_imei_edit;
            case SN:
            default: return R.string.dlg_unit_sn_edit;
        } else return R.string.dlg_unit_sn_edit;
    }

    protected abstract boolean hasToPlayTune();

    @AfterViews
    protected void attachViews() {
        if (hasToPlayTune()) {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone alarmRingtone = RingtoneManager.getRingtone(getApp(), notification);
            if (!alarmRingtone.isPlaying()) {
                alarmRingtone.play();
            }
        }

        formatter = getFormatter();
        etSerial.setInputType(getInputType());
        etSerial.setText("");
        etSerial.setFilters(getFilters());
        etSerial.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvSerial.setText(formatter.format(s.toString()));
                etSerial.setSelection(s.length());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        etSerial.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                etSerial.setSelection(etSerial.getText().length());
            }
        });
        etSerial.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (EditorInfo.IME_ACTION_NEXT == actionId) {
                    warrEditbox.requestFocus();
                    return true;
                }
                return false;
            }
        });
        warrEditbox.setImeOptions(EditorInfo.IME_ACTION_DONE);
        warrEditbox.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
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
        if (unit == null) {
            purposeSwitch.setTextOn("Add");
            purposeSwitch.setTextOff("Remove");
            purposeSwitch.setChecked(true);
            recollect();
        } else {

            etSerial.setText(unit.serialCode);
            msg.setText(R.string.wireless_free_edit);
            purposeSwitch.setVisibility(View.GONE);
            warrEditbox.setText(String.valueOf(unit.warrantyPeriod));
        }
    }

    protected IFormatter getFormatter(){
        if (type != null) switch (type) {
            case SN: {
                return Formatter.SN.formatter;
            }
            case IMEI: {
                return Formatter.IMEI.formatter;
            }
            case ICCID: {
                return Formatter.ICCID.formatter;
            }
            default: return Formatter.BARCODE.formatter;
        } else return Formatter.SN.formatter;
    }

    protected int getInputType(){
        if (CodeType.IMEI == type)
            return InputType.TYPE_CLASS_NUMBER;
        return InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
    }

    protected InputFilter[] getFilters() {
        if (CodeType.ICCID == type)
            return new InputFilter[]{new LengthFilter(formatter.length()), new IccidInputFilter()};
        return new LengthFilter[]{new LengthFilter(formatter.length())};
    }

    protected void recollect () {
        CollectUnitsCommand.start(getActivity(), null, item.guid, getSelectionOrderId(), null, null, true, false, unitsCallback);
    }

    protected String getSelectionOrderId() {
        return null;
    }

    protected void updateMsgWithValues(int qty) {
        msg.setText(getString(R.string.dlg_sn_descr, qty == 0 ? "no" : qty, type == null ? "unknown" : type.toString()));
    }

    @AfterTextChange
    protected void etSerialAfterTextChanged(Editable s) {
        checkPositiveButtonCondition();
    }

    @AfterTextChange
    protected void warrEditboxAfterTextChanged(Editable s) {
        checkPositiveButtonCondition();
    }

    protected void checkPositiveButtonCondition() {
        String value = validateForm();
        boolean warrantyOk = false;
        try {
            int i = Integer.parseInt(warrEditbox.getText().toString());
            warrantyOk = i >= 0;
        } catch (NumberFormatException ignore) {
        } catch (NullPointerException ignore) { }
        warrantyOk |= ignoreWarrantyCondition();

        enablePositiveButtons(additionalCheckPassed() && warrantyOk && value != null);

        tvSerial.setTextColor(value != null ? normalTextColor : errorTextColor);
        warrEditbox.setTextColor(warrantyOk ? normalTextColor : errorTextColor);
    }

    protected boolean ignoreWarrantyCondition() {
        return false;
    }

    protected boolean additionalCheckPassed() {
        return true;
    }

    protected abstract boolean onSubmitForm();

    protected OnDialogClickListener submitListener = new OnDialogClickListener() {
        @Override
        public boolean onClick() {
            onSubmitForm();
            return false;
        }
    };

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                onSubmitForm();
                return false;
            }
        };
    }

    public void onBarcodeReceived(String barcode) {
        etSerial.setText(barcode);
    }

    private String validateForm() {
        String value = etSerial.getText().toString();
        return formatter.valid(new StringBuilder(value)) ? value : null;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_confirm;
    }

    @Override
    protected boolean hasPositiveButton() {
        return true;
    }

    @Override
    protected boolean hasNegativeButton() {
        return true;
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

    private CollectUnitsCommand.UnitCallback unitsCallback = new CollectUnitsCommand.UnitCallback() {
        @Override
        protected void handleSuccess(List<Unit> unitList) {
            if (getActivity() == null)
                return;

            units = unitList;
            if (unitList == null || unitList.size() == 0) {
                updateMsgWithValues(0);
            } else {
                updateMsgWithValues(unitList.size());
                warrEditbox.setText(String.valueOf(unitList.get(unitList.size() - 1).warrantyPeriod));
            }
        }

        @Override
        protected void handleError() {
            if (getActivity() == null)
                return;

            updateMsgWithValues(0);
        }
    };

    public interface UnitCallback {

        void handleSuccess(boolean add, ItemExModel parent);

        void handleSuccess();

        void handleError(String message);
    }

}
