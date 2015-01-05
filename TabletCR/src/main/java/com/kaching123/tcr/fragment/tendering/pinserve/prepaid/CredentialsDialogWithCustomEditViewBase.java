package com.kaching123.tcr.fragment.tendering.pinserve.prepaid;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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
public abstract class CredentialsDialogWithCustomEditViewBase extends StyledDialogFragment {

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
        return R.string.btn_check;
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

    public class MetaInfo2 {

        public int max;
        public int min;
        public TextView editable;
        public boolean wanted;
        public boolean validated;

        public MetaInfo2(TextView editable, boolean wanted, boolean validated) {
            this(editable, wanted, validated, 0, 0);
        }

        public MetaInfo2(TextView editable, boolean wanted, boolean validated, int max, int min) {
            this.editable = editable;
            this.wanted = wanted;
            this.validated = validated;
            if (max >= min && max > 0) {
                setMax(max);
            }
        }

        public void setHint(String hint) {
            String mHint = hint;
            if (mHint != null) {
                if (wanted) {
                    SpannableString sshint = new SpannableString(hint + "*");
                    sshint.setSpan(new ForegroundColorSpan(Color.RED), hint.length(), hint.length() + 1, 0);
                    this.editable.setHint(sshint);
                } else
                    this.editable.setHint(hint);
            }

        }

        public void setInputType(int inputType) {
            this.editable.setInputType(inputType);
        }

        public void revalidate() {
            String text = this.editable.getText().toString();
            int length = text != null ? text.length() : 0;
            this.validated = !this.wanted || (length >= this.min && (this.max <= this.min || length <= this.max));
        }

        public void setMax(int max) {
            this.max = max;
            applyValidation();
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
