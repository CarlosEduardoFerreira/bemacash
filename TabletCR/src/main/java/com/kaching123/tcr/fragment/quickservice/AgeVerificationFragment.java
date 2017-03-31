package com.kaching123.tcr.fragment.quickservice;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.R;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.edit.KeyboardDialogFragment;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.converter.ItemExFunction;
import com.kaching123.tcr.store.ShopSchema2;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;
import java.util.regex.Pattern;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


/**
 * Created by mboychenko on 3/28/2017.
 */

@EFragment
public class AgeVerificationFragment extends KeyboardDialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String DIALOG_NAME = "AgeVerificationFragment";

    private final Pattern YEAR_REGEXP = Pattern.compile("(19|20)\\d\\d");
    private final Pattern DAY_REGEXP = Pattern.compile("0?[1-9]|[12][0-9]|3[01]");
    private final Pattern MONTH_REGEXP = Pattern.compile("0?[1-9]|1[012]");

    @FragmentArg
    protected String orderGuid;

    @ViewById protected CustomEditBox mmAgeField;
    @ViewById protected CustomEditBox ddAgeField;
    @ViewById protected CustomEditBox yyyyAgeField;
    @ViewById protected TextView ageWarning;
    @ViewById protected TextView firstSlash;
    @ViewById protected TextView secondSlash;

    private AgeVerifiedListener ageVerifiedListener;
    private ItemExModel itemExModel;
    private int customerAge;

    public void setConfirmListener(AgeVerifiedListener listener) {
        this.ageVerifiedListener = listener;
    }

    private void checkConfirmAbility(int year, int monthOfYear, int dayOfMonth) {
        monthOfYear--;                                                                              //calendar use 0-based month value
        Calendar calendar = Calendar.getInstance();
        int curYear = calendar.get(Calendar.YEAR);
        int curMonth = calendar.get(Calendar.MONTH);
        int curDay = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.setTimeInMillis(0);
        calendar.set(curYear, curMonth, curDay);

        Calendar ageRestrictionForItem = Calendar.getInstance();
        ageRestrictionForItem.setTimeInMillis(0);
        ageRestrictionForItem.set(year, monthOfYear, dayOfMonth);

        int diff = calendar.get(Calendar.YEAR) - ageRestrictionForItem.get(Calendar.YEAR);
        if (ageRestrictionForItem.get(Calendar.MONTH) > calendar.get(Calendar.MONTH)
                || (calendar.get(Calendar.MONTH) == ageRestrictionForItem.get(Calendar.MONTH)
                        && ageRestrictionForItem.get(Calendar.DATE) > calendar.get(Calendar.DATE))) {
            diff--;
        }

        if (diff >= itemExModel.ageVerification) {
            customerAge = diff;
            enablePositiveButton(true, greenBtnColor);
            ageWarning.setVisibility(GONE);
        } else {
            customerAge = 0;
            enablePositiveButton(false, disabledBtnColor);
            ageWarning.setVisibility(View.VISIBLE);
            ageWarning.setText(getString(R.string.age_verification_customer_must_be_older, itemExModel.ageVerification));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources()
                        .getDimensionPixelOffset(R.dimen.age_ver_dlg_width),
                getDialog().getWindow().getAttributes().height);
        getLoaderManager().restartLoader(0, null, this);

        CustomEditBox[] ageViews = new CustomEditBox[] {mmAgeField, ddAgeField, yyyyAgeField};
        for (CustomEditBox ageView : ageViews) {
            ageView.setKeyboardSupportConteiner(AgeVerificationFragment.this);
        }

        initViews();
    }

    private void initViews() {
        enablePositiveButtons(false);
        keyboard.setDotEnabled(false);


        mmAgeField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    firstSlash.setTextColor(getResources().getColor(R.color.mint_color));
                    secondSlash.setTextColor(getResources().getColor(R.color.dashboard_module_text));
                    if (mmAgeField.getText().length() == 0) {
                        keyboard.setEnterEnabled(false);
                    }
                } else {
                    String month = mmAgeField.getText().toString();
                    if(month.length() == 1 && month.charAt(0) == '0') {
                        mmAgeField.setText(month.concat("1"));
                    } else if (month.length() == 1) {
                        mmAgeField.setText("0".concat(month));
                    }
                }
            }
        });
        ddAgeField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    firstSlash.setTextColor(getResources().getColor(R.color.mint_color));
                    secondSlash.setTextColor(getResources().getColor(R.color.mint_color));
                    if (((CustomEditBox)v).getText().length() == 0) {
                        keyboard.setEnterEnabled(false);
                    } else {
                        String day = ddAgeField.getText().toString();
                        if(day.length() == 1 && day.charAt(0) == '0') {
                            ddAgeField.setText(day.concat("1"));
                        } else if (day.length() == 1) {
                            ddAgeField.setText("0".concat(day));
                        }
                    }
                }
            }
        });
        yyyyAgeField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    firstSlash.setTextColor(getResources().getColor(R.color.dashboard_module_text));
                    secondSlash.setTextColor(getResources().getColor(R.color.mint_color));
                    if (((CustomEditBox)v).getText().length() == 0) {
                        keyboard.setEnterEnabled(false);
                    }
                }
            }
        });


        yyyyAgeField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkWarningVisibility();
                if (s.length() < 4) {
                    keyboard.setEnterEnabled(false);
                    enablePositiveButton(false, disabledBtnColor);
                }

//                if(s.length() == 0) {
//                    ddAgeField.requestFocusFromTouch();
//                    return;
//                }

                if (s.length() == 4) {
                    keyboard.setEnterEnabled(true);
                    validateInput();
                }
                if (s.length() == 1 && (s.charAt(0) != '1' && s.charAt(0) != '2')) {
                    yyyyAgeField.clear();
                } else if (s.length()== 2) {
                    if (s.charAt(0) == '1' && s.charAt(1) != '9') {
                        yyyyAgeField.setText(String.valueOf(s.charAt(0)));
                    } else if (s.charAt(0) == '2' && s.charAt(1) != '0') {
                        yyyyAgeField.setText(String.valueOf(s.charAt(0)));
                    }
                } else if (s.length() > 4) {
                    yyyyAgeField.setText(String.valueOf(s.subSequence(0,3)));
                }
            }
        });
        ddAgeField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkWarningVisibility();
                if (s.length() == 0) {
                    keyboard.setEnterEnabled(false);
                    enablePositiveButton(false, disabledBtnColor);
//                    mmAgeField.requestFocusFromTouch();
                }
                if (s.length() == 1) {
                    keyboard.setEnterEnabled(true);
                    needRefresh();
                }
                if (s.length() == 2) {
                    if(s.charAt(0) != '0' && s.charAt(0) != '1' && s.charAt(0) != '2' && s.charAt(0) != '3') {
                        ddAgeField.setText(String.valueOf(s.charAt(0)));
                        return;
                    }
                    if(s.charAt(0) == '0' && s.charAt(1) == '0') {
                        ddAgeField.setText(String.valueOf(s.charAt(0)));
                        return;
                    }
                    if (s.charAt(0) == '3' && (s.charAt(1) != '0' && s.charAt(1) != '1')) {
                        ddAgeField.setText(String.valueOf(s.charAt(0)));
                        return;
                    }
                    yyyyAgeField.requestFocusFromTouch();
                    needRefresh();
                } else if (s.length() > 2) {
                    ddAgeField.setText(String.valueOf(s.subSequence(0, 1)));
                    yyyyAgeField.requestFocusFromTouch();
                }
            }
        });
        mmAgeField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkWarningVisibility();
                if (s.length() == 0) {
                    keyboard.setEnterEnabled(false);
                    enablePositiveButton(false, disabledBtnColor);
                }
                if (s.length() == 1) {
                    keyboard.setEnterEnabled(true);
                    needRefresh();
                }
                if (s.length() == 2) {
                    if(s.charAt(0) == '0' && s.charAt(1) == '0') {
                        mmAgeField.setText(String.valueOf(s.charAt(0)));
                        return;
                    }
                    if (s.charAt(0) == '1' && (s.charAt(1) != '0' && s.charAt(1) != '1' && s.charAt(1) != '2')) {
                        mmAgeField.setText(String.valueOf(s.charAt(0)));
                        return;
                    }
                    if (s.charAt(0) != '0' && s.charAt(0) != '1') {
                        mmAgeField.setText(String.valueOf(s.charAt(0)));
                        return;
                    }
                    ddAgeField.requestFocusFromTouch();
                    needRefresh();
                } else if (s.length()> 2) {
                    mmAgeField.setText(String.valueOf(s.subSequence(0, 1)));
                    ddAgeField.requestFocusFromTouch();
                }
            }
        });

        mmAgeField.setEditListener(new CustomEditBox.IEditListener() {
            @Override
            public boolean onChanged(String text) {
                if (text.length() == 1 && text.charAt(0) == '0') {
                    mmAgeField.setText(text.concat("1"));
                } else if (text.length() == 1) {
                    mmAgeField.setText("0".concat(text));
                }
                ddAgeField.requestFocusFromTouch();
                return false;
            }
        });
        ddAgeField.setEditListener(new CustomEditBox.IEditListener() {
            @Override
            public boolean onChanged(String text) {
                if (text.length() == 1 && text.charAt(0) == '0') {
                    ddAgeField.setText(text.concat("1"));
                } else if (text.length() == 1) {
                    ddAgeField.setText("0".concat(text));
                }
                yyyyAgeField.requestFocusFromTouch();
                return false;
            }
        });

        yyyyAgeField.setEditListener(new CustomEditBox.IEditListener() {
            @Override
            public boolean onChanged(String text) {
                validateInput();
                return false;
            }
        });
    }

    private void needRefresh() {
        String month = mmAgeField.getText().toString();
        String day = ddAgeField.getText().toString();
        String year = yyyyAgeField.getText().toString();
        if (YEAR_REGEXP.matcher(year).matches() && MONTH_REGEXP.matcher(month).matches() &&
                DAY_REGEXP.matcher(day).matches()) {
            validateInput(true);
        }
    }

    private void validateInput(){
        validateInput(false);
    }
    private void validateInput(boolean skipRegCheck) {
        customerAge = 0;
        String month = mmAgeField.getText().toString();
        String day = ddAgeField.getText().toString();
        String year = yyyyAgeField.getText().toString();
        if(!skipRegCheck) {
            if (!YEAR_REGEXP.matcher(year).matches()) {
                ageWarning.setText(R.string.wrong_year_format);
                return;
            }

            if (!MONTH_REGEXP.matcher(month).matches()) {
                ageWarning.setText(R.string.wrong_month_format);
                return;
            }

            if (!DAY_REGEXP.matcher(day).matches()) {
                ageWarning.setText(R.string.wrong_day_format);
                return;
            }
        }
        checkConfirmAbility(Integer.valueOf(year), Integer.valueOf(month), Integer.valueOf(day));
    }

    private void checkWarningVisibility(){
        if (ageWarning.getVisibility() == VISIBLE && ageWarning.getText().length() > 0) {
            ageWarning.setText("");
            ageWarning.setVisibility(GONE);
        }
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.age_verification_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.age_verification_title;
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
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                ageVerifiedListener.onAgeVerified(customerAge);
                return true;
            }
        };
    }

    public interface AgeVerifiedListener {
        void onAgeVerified(int customerAge);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(ItemExFunction.VIEW_URI)
                .projection(ItemExFunction.PROJECTION)
                .where(ShopSchema2.ItemExtView2.ItemTable.GUID + " = ?", orderGuid);

        return builder.build(getContext());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()){
            itemExModel = new ItemExFunction().apply(data);
        }
        data.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public static void show(FragmentActivity context, String orderGuid, AgeVerifiedListener listener){
        DialogUtil.show(context, DIALOG_NAME, AgeVerificationFragment_.builder().orderGuid(orderGuid).build()).setConfirmListener(listener);
    }
}
