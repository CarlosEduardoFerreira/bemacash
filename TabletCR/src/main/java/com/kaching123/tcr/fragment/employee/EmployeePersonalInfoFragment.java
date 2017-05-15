package com.kaching123.tcr.fragment.employee;

import android.text.TextUtils;
import android.text.method.SingleLineTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.BaseEmployeeActivity;
import com.kaching123.tcr.fragment.user.LoginFragment;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeeStatus;
import com.kaching123.tcr.util.KeyboardUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.Date;


/**
 * Created by mboychenko on 5/11/2017.
 */
@EFragment(R.layout.employee_personal_info_fragment)
public class EmployeePersonalInfoFragment extends EmployeeBaseFragment implements EmployeeView {

    public static final int LOGIN_MIN_LEN = 3;
    public static final int PASSWORD_MIN_LEN = 4;

    @ViewById
    protected EditText firstName;
    @ViewById
    protected EditText lastName;
    @ViewById
    protected EditText login;
    @ViewById
    protected EditText password;
    @ViewById
    protected EditText passwordConfirm;
    @ViewById
    protected Spinner status;

    @Override
    protected void setViews() {
        login.setTransformationMethod(SingleLineTransformationMethod.getInstance());
        passwordConfirm.setImeOptions(EditorInfo.IME_ACTION_DONE);
        passwordConfirm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    KeyboardUtils.hideKeyboard(getActivity());
                    return true;
                }
                return false;
            }
        });
        status.setAdapter(new StatusAdapter());
    }

    public Spinner getStatus() {
        return status;
    }

    @Override
    protected void setEmployee(){
        final EmployeeModel employee = getEmployee();
        login.setText(employee.login);
        firstName.setText(employee.firstName);
        lastName.setText(employee.lastName);
        status.setSelection(employee.status.ordinal());
    }

    @Override
    public void collectDataToModel(EmployeeModel model) {
        model.firstName = firstName.getText().toString();
        model.lastName = lastName.getText().toString();
        model.login = login.getText().toString().trim();
        model.status = (EmployeeStatus) status.getSelectedItem();
        if (getMode() == BaseEmployeeActivity.EmployeeMode.EDIT) {
            String passwordText = password.getText().toString().trim();
            if (!TextUtils.isEmpty(passwordText)) {
                model.password = LoginFragment.md5(passwordText);
            }
        } else {
            model.password = LoginFragment.md5(password.getText().toString().trim());
            model.hireDate = new Date();
        }

    }

    public boolean isLoginEnabled() {
        return login.isEnabled();
    }

    @Override
    public void setFieldsEnabled(boolean enabled) {
        login.setEnabled(false);

        login.setBackgroundColor(getResources().getColor(R.color.password_gray));

        password.setEnabled(false);
        password.setBackgroundColor(getResources().getColor(R.color.password_gray));

        passwordConfirm.setEnabled(false);
        passwordConfirm.setBackgroundColor(getResources().getColor(R.color.password_gray));
    }

    @Override
    public boolean validateView() {
        if (TextUtils.isEmpty(firstName.getText())) {
            Toast.makeText(getContext(), R.string.employee_edit_first_name_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(lastName.getText())) {
            Toast.makeText(getContext(), R.string.employee_edit_last_name_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        String loginText = login.getText().toString().trim();
        if (TextUtils.isEmpty(loginText)) {
            Toast.makeText(getContext(), R.string.employee_edit_login_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (loginText.length() < LOGIN_MIN_LEN) {
            Toast.makeText(getContext(), R.string.employee_edit_login_min_error, Toast.LENGTH_SHORT).show();
            return false;
        }

        String passwordText = password.getText().toString().trim();
        String passwordConfirmText = passwordConfirm.getText().toString().trim();

        if (getMode() == BaseEmployeeActivity.EmployeeMode.CREATE) {
            if ((TextUtils.isEmpty(passwordText))) {
                Toast.makeText(getContext(), R.string.employee_edit_password_error, Toast.LENGTH_SHORT).show();
                return false;
            }

            if ((passwordText.length() < PASSWORD_MIN_LEN)) {
                Toast.makeText(getContext(), R.string.employee_edit_password_min_error, Toast.LENGTH_SHORT).show();
                return false;
            }

            if ((!passwordConfirmText.equals(passwordText))) {
                Toast.makeText(getContext(), R.string.employee_edit_password_confirm_error, Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            boolean isPswdEmpty = TextUtils.isEmpty(passwordText);
            if (getEmployee().isSynced || isPswdEmpty && TextUtils.isEmpty(passwordConfirmText))
                return true;

            if (getEmployee().isSynced || isPswdEmpty) {
                Toast.makeText(getContext(), R.string.employee_edit_password_error, Toast.LENGTH_SHORT).show();
                return false;
            }

            if (getEmployee().isSynced || passwordText.length() < PASSWORD_MIN_LEN) {
                Toast.makeText(getContext(), R.string.employee_edit_password_min_error, Toast.LENGTH_SHORT).show();
                return false;
            }

            if (getEmployee().isSynced || !passwordConfirmText.equals(passwordText)) {
                Toast.makeText(getContext(), R.string.employee_edit_password_confirm_error, Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean hasChanges(EmployeeModel initModel) {
        if(!initModel.firstName.equals(firstName.getText().toString())) {
            Log.d("BemaCarl3","EmployeePersonalInfoFragment.employeeHasChanges.firstName"); return true;
        }
        if(!initModel.lastName.equals(lastName.getText().toString())) {
            Log.d("BemaCarl3","EmployeePersonalInfoFragment.employeeHasChanges.lastName"); return true;
        }
        if(!initModel.login.equals(login.getText().toString())) {
            Log.d("BemaCarl3","EmployeePersonalInfoFragment.employeeHasChanges.login"); return true;
        }
        return false;
    }

    private class StatusAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return EmployeeStatus.values().length;
        }

        @Override
        public Object getItem(int i) {
            return EmployeeStatus.values()[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = newDropDownView(parent, position);
            }
            bindView(convertView, parent, position);
            return convertView;
        }

        private View newDropDownView(ViewGroup parent, int position) {
            return LayoutInflater.from(getContext()).inflate(R.layout.spinner_dropdown_item, parent, false);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = newView(parent, position);
            }
            bindView(convertView, parent, position);
            return convertView;
        }

        private View newView(ViewGroup parent, int position) {
            return LayoutInflater.from(getContext()).inflate(R.layout.spinner_item, parent, false);
        }

        private void bindView(View convertView, ViewGroup parent, int position) {
            TextView label = (TextView) convertView;
            EmployeeStatus item = (EmployeeStatus) getItem(position);
            label.setText(item.getLabelRes());
        }
    }
}
