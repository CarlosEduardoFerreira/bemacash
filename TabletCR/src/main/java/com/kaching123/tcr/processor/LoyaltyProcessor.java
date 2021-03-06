package com.kaching123.tcr.processor;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.activity.BaseCashierActivity;
import com.kaching123.tcr.commands.loyalty.AddSaleIncentiveCommand;
import com.kaching123.tcr.commands.loyalty.AddSaleIncentiveCommand.AddSaleIncentiveCallback;
import com.kaching123.tcr.commands.loyalty.GetCustomerLoyaltyCommand;
import com.kaching123.tcr.commands.loyalty.GetCustomerLoyaltyCommand.BaseGetCustomerLoyaltyCallback;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleOrderDiscountCommand;
import com.kaching123.tcr.commands.store.user.UpdateCustomerBirthdayRewardDateCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.loyalty.LoyaltyFragmentDialog;
import com.kaching123.tcr.fragment.loyalty.LoyaltyFragmentDialog.LoyaltyDialogListener;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.LoyaltyType;
import com.kaching123.tcr.model.LoyaltyViewModel;
import com.kaching123.tcr.model.LoyaltyViewModel.IncentiveExModel;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static java.security.AccessController.getContext;

/**
 * Created by vkompaniets on 06.07.2016.
 */
public class LoyaltyProcessor {

    private FragmentActivity context;
    private LoyaltyViewModel loyalty;
    private IncentiveExModel currentIncentive;
    private String orderGuid;
    private String customerGuid;
    private Set<String> bannedIncentives;

    private LoyaltyProcessorCallback callback;

    private LoyaltyProcessor(String customerGuid, String orderGuid){
        this.customerGuid = customerGuid;
        this.orderGuid = orderGuid;
        bannedIncentives = new HashSet<>();
    }

    public static LoyaltyProcessor create(String customerGuid, String orderGuid){
        return new LoyaltyProcessor(customerGuid, orderGuid);
    }

    public LoyaltyProcessor setCallback(LoyaltyProcessorCallback callback){
        this.callback = callback;
        return this;
    }

    public void init(FragmentActivity context){
        this.context = context;

        if (customerGuid == null || orderGuid == null){
            callback.onComplete();
            return;
        }else{
            startCircle();
        }
    }

    private void startCircle() {
        GetCustomerLoyaltyCommand.start(context, customerGuid, orderGuid, new BaseGetCustomerLoyaltyCallback() {
            @Override
            public void onDataLoaded(LoyaltyViewModel loyalty) {
                if (loyalty == null){
                    callback.onComplete();
                }else{
                    LoyaltyProcessor.this.loyalty = loyalty;
                    processIncentive();
                }
            }
        });
    }

    private void processIncentive(){
        IncentiveExModel incentive = getNextIncentive();
        if (incentive == null){
            callback.onComplete();
        }else{
            Log.d("BemaCarl23","LoyaltyProcessor.processIncentive.incentive.type:    1: " + incentive.type);
            if(incentive.type.equals(LoyaltyType.BIRTHDAY)){
                Log.d("BemaCarl23","LoyaltyProcessor.processIncentive.incentive.type:    2: " + incentive.type);
                if(alreadyReceivedBirthdayLoyalty()){
                    Log.d("BemaCarl23","LoyaltyProcessor.processIncentive. alreadyReceivedBirthdayLoyalty:    ");
                    return;
                }
            }
            Log.d("BemaCarl23","LoyaltyProcessor.processIncentive.incentive.type:    3: " + incentive.type);
            LoyaltyFragmentDialog.show(context, incentive, new LoyaltyDialogListener() {
                @Override
                public void onApplyRequested(IncentiveExModel incentive) {
                    Log.d("BemaCarl23","LoyaltyProcessor.processIncentive.incentive.type:    4: " + incentive.type);
                    applyIncentive(incentive);
                }

                @Override
                public void onSkipRequested(IncentiveExModel incentive) {
                    Log.d("BemaCarl23","LoyaltyProcessor.processIncentive.incentive.type:    5: " + incentive.type);
                    banIncentive(incentive.guid);
                    processIncentive();
                }
            });
        }
    }

    private IncentiveExModel getNextIncentive(){
        for (IncentiveExModel incentive : loyalty.incentiveExModels){
            Log.d("BemaCarl23","LoyaltyProcessor.getNextIncentive.incentive.guid:           " + incentive.guid);
            Log.d("BemaCarl23","LoyaltyProcessor.getNextIncentive.incentive.name:           " + incentive.name);
            Log.d("BemaCarl23","LoyaltyProcessor.getNextIncentive.incentive.birthdayoffset: " + incentive.birthdayOffset);
            Log.d("BemaCarl23","LoyaltyProcessor.getNextIncentive.incentive.type:           " + incentive.type);
            Log.d("BemaCarl23","LoyaltyProcessor.getNextIncentive.incentive.rewardType:     " + incentive.rewardType);
            Log.d("BemaCarl23","LoyaltyProcessor.getNextIncentive.incentive.rewardValue:    " + incentive.rewardValue);
            if (!checkIncentiveBanned(incentive.guid)) {
                return incentive;
            }
        }
        return null;
    }


    private boolean alreadyReceivedBirthdayLoyalty(){
        boolean ret = new LoyaltyBirthdayReceivedCheck().checkIfBirthdayWasAppliedOnCurrentYear(customerGuid);
        Log.d("BemaCarl23","LoyaltyProcessor.alreadyReceivedBirthdayLoyalty.return:    " + ret);
        return ret;
    }


    private void applyIncentive(IncentiveExModel incentive){
        if(incentive.type.equals(LoyaltyType.BIRTHDAY)) {
            Log.d("BemaCarl23","LoyaltyProcessor.applyIncentive.customerGuid:    " + customerGuid);
            UpdateCustomerBirthdayRewardDateCommand.start(TcrApplication.get().getApplicationContext(), customerGuid);
        }
        switch (incentive.rewardType){
            case DISCOUNT:
                applyDiscountIncentive(incentive);
                break;
            case GIFT_CARD:
                applyGiftCardIncentive(incentive);
                break;
            case ITEM:
                applyItemIncentive(incentive);
        }
    }

    private void applyDiscountIncentive(IncentiveExModel incentive){
        UpdateSaleOrderDiscountCommand.start(context, orderGuid, incentive.rewardValue, incentive.rewardValueType);
        addSaleIncentive(incentive);
    }

    private void applyGiftCardIncentive(IncentiveExModel incentive){
        currentIncentive = incentive;
        ItemExModel item = new ItemExModel(incentive.rewardValue);
        item.isIncentive = true;
        item.discountType = DiscountType.VALUE;
        item.discount = incentive.rewardValue;
        item.isDiscountable = true;
        banIncentive(incentive.guid);
        callback.onAddItemRequest(item, null, null, true);
    }

    private void applyItemIncentive(IncentiveExModel incentive){
        currentIncentive = incentive;
        ItemExModel item = ItemExModel.loadSync(context, incentive.incentiveItemExModel.item.guid);
        item.isIncentive = true;
        callback.onAddItemRequest(item, incentive.incentiveItemExModel.price, incentive.incentiveItemExModel.qty, false);
    }

    public void onItemAddedToOrder(String orderGuid, boolean success){
        if (!this.orderGuid.equals(orderGuid))
            return;

        if (success){
            addSaleIncentive(currentIncentive);
        }else{
            Toast.makeText(context, R.string.item_add_fail_msg, Toast.LENGTH_SHORT).show();
            startCircle();
        }
    }

    private void addSaleIncentive(final IncentiveExModel incentive){
        BaseCashierActivity.runLoyaltyBirthday = false;
        AddSaleIncentiveCommand.start(context, incentive.guid, customerGuid, orderGuid, new AddSaleIncentiveCallback() {
            @Override
            protected void onAdded(String saleIncentiveId) {
                showApplySuccessfulDialog(incentive);
                Log.d("BemaCarl23","LoyaltyProcessor.addSaleIncentive.runLoyaltyBirthday: " + BaseCashierActivity.runLoyaltyBirthday);
                BaseCashierActivity.runLoyaltyBirthday = true;
            }
        });
    }

    private void showApplySuccessfulDialog(IncentiveExModel incentive){
        String msg = context.getString(R.string.loyalty_applied_msg, context.getString(incentive.rewardType.getLabel()));
        AlertDialogFragment.showNotification(context, R.string.dlg_completed_tialog, msg, new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                startCircle();
                return true;
            }
        });
    }

    private void banIncentive(String incentiveId){
        bannedIncentives.add(incentiveId);
    }

    private boolean checkIncentiveBanned(String incentiveId){
        return bannedIncentives.contains(incentiveId);
    }

    public interface LoyaltyProcessorCallback {
        void onAddItemRequest(ItemExModel item, BigDecimal price, BigDecimal qty, boolean isGiftCard);
        void onComplete();
    }
}
