package com.kaching123.tcr.fragment.tendering.pinserve.prepaid;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.res.ColorRes;
import com.googlecode.androidannotations.annotations.res.DrawableRes;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

/**
 * @author Ivan v. Rikhmayer
 */
@EFragment
public abstract class CredentialsDialogBase extends StyledDialogFragment {

    @ColorRes(R.color.prepaid_dialog_white)
    protected int colorOk;
    @ColorRes(R.color.gray_dark)
    protected int colorDisabled;

    @DrawableRes(R.drawable.star_for_required_field)
    protected Drawable star;

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_back;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_next;
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
    }

    protected void enableFinish(Boolean enabled) {
        getPositiveButton().setEnabled(enabled);
        getPositiveButton().setTextColor(enabled ? colorOk : colorDisabled);
    }

    protected class MetaInfo {

        public int max;
        public int min;
        public EditText editable;
        public TextView label;
        public boolean wanted;
        public boolean validated;

        public MetaInfo(EditText editable, TextView label, boolean wanted, boolean validated) {
            this(editable, label, wanted, validated, 0, 0);
        }

        public MetaInfo(EditText editable, TextView label, boolean wanted, boolean validated, int max, int min) {
            this.editable = editable;
            this.label = label;
            this.wanted = wanted;
            this.validated = validated;
            setWanted();
            if (max >= min && max > 0) {
                setMin(min);
                setMax(max);
            }
        }

        public void revalidate() {
            String text = this.editable.getText().toString();
            int length = text != null ? text.length() : 0;
            this.validated = !this.wanted || (length >= this.min && (this.max <= this.min || length <= this.max));
        }

        public void setMin(int min) {
            this.min = min;
            editable.setHint(getString(R.string.bill_payment_edit_min_hint, min));
        }

        public void setMax(int max) {
            this.max = max;
            applyValidation();
        }

        public void setWanted() {
            this.label.setCompoundDrawablesWithIntrinsicBounds(null, null, wanted ? star : null, null);
        }

        public void applyValidation() {
            InputFilter[] filterArray = new InputFilter[1];
            filterArray[0] = new InputFilter.LengthFilter(this.max);
            this.editable.setFilters(filterArray);
        }

        @Override
        public String toString() {
            String text = this.editable.getText().toString();
            return text != null && text.length() > 0 ? text : null;
        }
    }

}
