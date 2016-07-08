package com.kaching123.tcr.processor;

import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.loyalty.AddLoyaltyPointsMovementCommand;
import com.kaching123.tcr.commands.loyalty.AddLoyaltyPointsMovementCommand.AddLoyaltyPointsMovementCallback;
import com.kaching123.tcr.commands.loyalty.GetCustomerLoyaltyCommand;
import com.kaching123.tcr.commands.loyalty.GetCustomerLoyaltyCommand.BaseGetCustomerLoyaltyCallback;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleOrderDiscountCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.loyalty.LoyaltyFragmentDialog;
import com.kaching123.tcr.fragment.loyalty.LoyaltyFragmentDialog.LoyaltyDialogListener;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.LoyaltyViewModel;
import com.kaching123.tcr.model.LoyaltyViewModel.IncentiveExModel;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by vkompaniets on 06.07.2016.
 */
public class LoyaltyProcessor {

    private FragmentActivity context;
    private LoyaltyViewModel loyalty;
    private IncentiveExModel currentIncentive;
    private String orderGuid;
    private String customerGuid;
    private Set<String> bannedIncentiveIds = new HashSet<>();

    private LoyaltyProcessorCallback callback;

    private LoyaltyProcessor(String customerGuid, String orderGuid){
        this.customerGuid = customerGuid;
        this.orderGuid = orderGuid;
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
            LoyaltyFragmentDialog.show(context, incentive, new LoyaltyDialogListener() {
                @Override
                public void onApplyRequested(IncentiveExModel incentive) {
                    applyIncentive(incentive);
                }

                @Override
                public void onSkipRequested(IncentiveExModel incentive) {
                    bannedIncentiveIds.add(incentive.guid);
                    processIncentive();
                }
            });
        }
    }

    private IncentiveExModel getNextIncentive(){
        for (IncentiveExModel incentive : loyalty.incentiveExModels){
            if (!bannedIncentiveIds.contains(incentive.guid))
                return incentive;
        }
        return null;
    }

    private void applyIncentive(IncentiveExModel incentive){
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
        addPointsMovement(incentive);
    }

    private void applyGiftCardIncentive(IncentiveExModel incentive){
        //TODO implement gift card incentives
        bannedIncentiveIds.add(incentive.guid);
        processIncentive();
    }

    private void applyItemIncentive(IncentiveExModel incentive){
        currentIncentive = incentive;
        ItemExModel item = ItemExModel.loadSync(context, incentive.incentiveItemExModel.item.guid);
        callback.onAddItemRequest(item, incentive.incentiveItemExModel.price, incentive.incentiveItemExModel.qty);
    }

    public void onItemAddedToOrder(String orderGuid, boolean success){
        if (!this.orderGuid.equals(orderGuid))
            return;

        if (success){
            addPointsMovement(currentIncentive);
        }else{
            Toast.makeText(context, "Failed to add item", Toast.LENGTH_SHORT).show();
            startCircle();
        }
    }

    private void addPointsMovement(final IncentiveExModel incentive) {
        bannedIncentiveIds.add(incentive.guid);
        AddLoyaltyPointsMovementCommand.start(context, customerGuid, incentive.pointThreshold == null ? BigDecimal.ZERO : incentive.pointThreshold.negate(), new AddLoyaltyPointsMovementCallback() {
            @Override
            protected void onPointsApplied(BigDecimal points) {
                showApplySuccessfulDialog(incentive);
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

    public interface LoyaltyProcessorCallback {
        void onAddItemRequest(ItemExModel item, BigDecimal price, BigDecimal qty);
        void onComplete();
    }
}
