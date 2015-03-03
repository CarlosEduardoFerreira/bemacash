package com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ActivationActivity;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.ActivationCarrierModel;
import com.kaching123.tcr.processor.PrepaidProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vkompaniets on 01.07.2014.
 */
@EFragment
public class ActivationTypeChoosingFragmentDialog extends StyledDialogFragment {

    private static final String DIALOG_NAME = ActivationTypeChoosingFragmentDialog.class.getSimpleName();

    @FragmentArg
    protected boolean fromPrepaid;

    @FragmentArg
    protected ArrayList<ActivationCarrierModel> activationCarriers;

    @ViewById
    protected ListView carrierList;

    private CarrierAdapter carrierAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getNegativeButton().setTextColor(Color.WHITE);
        getNegativeButton().setTextSize(25);
        getNegativeButton().setTypeface(Typeface.DEFAULT_BOLD);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.activation_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.activation_dlg_height)
        );
    }

    @AfterViews
    protected void init(){
        carrierAdapter = new CarrierAdapter(getActivity());
        carrierAdapter.changeCursor(activationCarriers);
        carrierList.setAdapter(carrierAdapter);
        carrierList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = carrierAdapter.getItem(position).url;
                ActivationActivity.start(getActivity(), url);
                dismiss();
            }
        });
    }

    @Override protected  int getSeparatorColor(){return Color.WHITE;}

    @Override protected  int getTitleTextColor(){return Color.WHITE;}

    @Override protected  int getTitleViewBackgroundColor(){return getResources().getColor(R.color.prepaid_dialog_title_background_color); }

    @Override protected  int getButtonsBackgroundColor(){return getResources().getColor(R.color.prepaid_dialog_buttons_background_color); }

    @Override protected  boolean hasTitleTextTypeface(){ return true; }

    @Override protected  boolean hasNegativeButtonTextTypeface(){ return true; }

    @Override protected  int getNegativeButtonTextTypeface(){ return Typeface.BOLD; }

    protected  int getTitleIcon(){return R.drawable.icon_activation_center;};

    protected  int getTitleGravity(){return Gravity.LEFT;};

    @Override
    protected int getDialogContentLayout() {
        return R.layout.activation_type_choosing_fragment_dialog;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.prepaid_dialog_activation_center_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return fromPrepaid ? R.string.btn_back : R.string.btn_close;
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
//                if (fromPrepaid){
//                    PrepaidProcessor.create().init(getActivity());
//                }
                return true;
            }
        };
    }

    private class CarrierAdapter extends ObjectsCursorAdapter<ActivationCarrierModel> {

        public CarrierAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View view = View.inflate(getContext(), R.layout.activation_carrier_list_item, null);
            view.setTag(new ViewHolder((TextView)view.findViewById(android.R.id.text1)));
            return view;
        }

        @Override
        protected View bindView(View convertView, int position, ActivationCarrierModel item) {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.name.setText(item.name);
            return convertView;
        }

        private class ViewHolder {
            TextView name;

            private ViewHolder(TextView name) {
                this.name = name;
            }
        }
    }

    public static void show(FragmentActivity activity, List<ActivationCarrierModel> activationCarriers, boolean fromPrepaid){
        DialogUtil.show(activity, DIALOG_NAME, ActivationTypeChoosingFragmentDialog_.builder().activationCarriers(new ArrayList<ActivationCarrierModel>(activationCarriers)).fromPrepaid(fromPrepaid).build());
    }

    public static void hide (FragmentActivity activity){
        DialogUtil.hide(activity, DIALOG_NAME);
    }
}
