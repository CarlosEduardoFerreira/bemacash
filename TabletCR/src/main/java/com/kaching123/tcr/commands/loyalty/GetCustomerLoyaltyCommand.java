package com.kaching123.tcr.commands.loyalty;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.function.OrderTotalPriceCalculator;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.SaleOrderCostInfo;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.SaleOrderInfo;
import com.kaching123.tcr.function.OrderTotalPriceLoaderCallback;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.LoyaltyRewardType;
import com.kaching123.tcr.model.LoyaltyType;
import com.kaching123.tcr.model.LoyaltyViewModel;
import com.kaching123.tcr.model.LoyaltyViewModel.IncentiveExModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.converter.LoyaltyViewWrapFunction;
import com.kaching123.tcr.model.converter.StringFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.LoyaltyView2.LoyaltyIncentivePlanTable;
import com.kaching123.tcr.store.ShopSchema2.LoyaltyView2.LoyaltyIncentiveTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.LoyaltyView;
import com.kaching123.tcr.store.ShopStore.SaleIncentiveTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderItemsView;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.kaching123.tcr.model.ContentValuesUtil._castAsReal;
import static com.kaching123.tcr.util.DateUtils.cutTime;

/**
 * Created by vkompaniets on 06.07.2016.
 */
public class GetCustomerLoyaltyCommand extends PublicGroundyTask {

    private static final String ARG_CUSTOMER_ID = "ARG_CUSTOMER_ID";
    private static final String ARG_ORDER_ID = "ARG_ORDER_ID";
    private static final String EXTRA_LOYALTY = "EXTRA_LOYALTY";

    private static final Uri URI_LOYALTY_VIEW = ShopProvider.contentUri(LoyaltyView.URI_CONTENT);

    @Override
    protected TaskResult doInBackground() {

        String customerId = getStringArg(ARG_CUSTOMER_ID);
        String orderId = getStringArg(ARG_ORDER_ID);

        if (customerId == null || orderId == null)
            return succeeded();

        CustomerModel customer = CustomerModel.loadSync(getContext(), customerId);
        Log.d("BemaCarl23","GetCustomerLoyaltyCommand.doInBackground.customer.birthdayRewardReceivedDate: " + customer.birthdayRewardApplyDate);
        SaleOrderModel order = SaleOrderModel.loadSync(getContext(), orderId);

        if (customer.loyaltyPlanId == null)
            return succeeded();

        SaleOrderCostInfo orderCostInfo = loadSaleOrderCostInfo(getContext(), orderId);

        Query query = ProviderAction.query(URI_LOYALTY_VIEW);
        query.where(LoyaltyIncentivePlanTable.PLAN_GUID + " = ?", customer.loyaltyPlanId);
        query.where("(" + _castAsReal(LoyaltyIncentiveTable.POINT_THRESHOLD) + " <= ? OR " + LoyaltyIncentiveTable.TYPE + " = ?)", customer.loyaltyPoints, LoyaltyType.BIRTHDAY.ordinal());
        if (customer.birthday == null){
            query.where(LoyaltyIncentiveTable.TYPE + " = ?", LoyaltyType.POINTS.ordinal());
        }
        //do not apply discount reward if order already has discount
        if (order.discount != null && order.discount.compareTo(BigDecimal.ZERO) != 0){
            query.where(LoyaltyIncentiveTable.REWARD_TYPE + " <> ?", LoyaltyRewardType.DISCOUNT.ordinal());
        }
        //do not apply discount reward if order total = 0
        if (orderCostInfo.totalOrderPrice.compareTo(BigDecimal.ZERO) == 0){
            query.where(LoyaltyIncentiveTable.REWARD_TYPE + " <> ?", LoyaltyRewardType.DISCOUNT.ordinal());
        }

        Cursor c = query.perform(getContext());
        LoyaltyViewModel loyalty = new LoyaltyViewWrapFunction().apply(c);
        c.close();
        if (loyalty == null)
            return succeeded();

        filterByUsedIncentives(loyalty.incentiveExModels, orderId, getContext());
        Log.d("BemaCarl23","GetCustomerLoyaltyCommand.doInBackground.customer.birthday:                     " + customer.birthday);
        Log.d("BemaCarl23","GetCustomerLoyaltyCommand.doInBackground.customer.birthdayRewardApplyDate:      " + customer.birthdayRewardApplyDate);
        filterByBirthday(loyalty.incentiveExModels, customer.birthday, customer.birthdayRewardApplyDate);
        filterByOrderValue(loyalty.incentiveExModels, orderCostInfo.totalDiscountableItemTotal);

        return succeeded().add(EXTRA_LOYALTY, loyalty);
    }

    private static SaleOrderCostInfo loadSaleOrderCostInfo(Context context, String orderId){
        Cursor c = ProviderAction.query(ShopProvider.contentUri(SaleOrderItemsView.URI_CONTENT))
                .projection(OrderTotalPriceLoaderCallback.PROJECTION)
                .where(SaleItemTable.ORDER_GUID + " = ? ", orderId)
                .orderBy(SaleItemTable.SEQUENCE)
                .perform(context);

        SaleOrderInfo saleOrderInfo = OrderTotalPriceLoaderCallback.readCursor(c);
        c.close();

        return OrderTotalPriceCalculator.calculate(saleOrderInfo);
    }

    private static void filterByUsedIncentives(List<IncentiveExModel> incentives, String orderId, Context context){
        Set<String> usedIncentives = ProviderAction.query(ShopProvider.contentUri(SaleIncentiveTable.URI_CONTENT))
                .projection(SaleIncentiveTable.INCENTIVE_ID)
                .where(SaleIncentiveTable.ORDER_ID + " = ?", orderId)
                .perform(context)
                .toFluentIterable(new StringFunction())
                .toSet();

        if (usedIncentives.isEmpty())
            return;

        Iterator<IncentiveExModel> it = incentives.iterator();
        while (it.hasNext()){
            IncentiveExModel incentive = it.next();
            if (usedIncentives.contains(incentive.guid))
                it.remove();
        }
    }

    private static void filterByBirthday(List<IncentiveExModel> incentives, Date birthdayDate, Date birthdayRewardDate){
        final long halfYear = TimeUnit.DAYS.toMillis(365 / 2);
        Iterator<IncentiveExModel> it = incentives.iterator();
        while (it.hasNext()){
            IncentiveExModel incentive = it.next();
            if (incentive.type == LoyaltyType.POINTS){
                continue;
            }else if (birthdayDate == null){
                it.remove();
                continue;
            }else if (birthdayRewardDate != null && new Date().getTime() - birthdayRewardDate.getTime() < halfYear){
                it.remove();
                continue;
            }

            Calendar today = Calendar.getInstance();
            cutTime(today);
            Calendar birthday = Calendar.getInstance();
            birthday.setTime(birthdayDate);
            birthday.set(Calendar.YEAR, today.get(Calendar.YEAR));
            cutTime(birthday);

            today.add(Calendar.DATE, -incentive.birthdayOffset);
            boolean before = today.compareTo(birthday) <= 0;
            today.add(Calendar.DATE, 2 * incentive.birthdayOffset);
            boolean after = today.compareTo(birthday) >= 0;

            boolean isBirthdayNear = before && after;

            if (!isBirthdayNear){
                it.remove();
            }
        }
    }

    private static void filterByOrderValue(List<IncentiveExModel> incentives, BigDecimal orderValue){
        Iterator<IncentiveExModel> it = incentives.iterator();
        while (it.hasNext()){
            IncentiveExModel incentive = it.next();
            if (incentive.rewardType == LoyaltyRewardType.DISCOUNT && incentive.rewardValueType == DiscountType.VALUE && incentive.rewardValue.compareTo(orderValue) >= 0)
                it.remove();
        }
    }

    public static abstract class BaseGetCustomerLoyaltyCallback {

        @OnSuccess(GetCustomerLoyaltyCommand.class)
        public void onSuccess(@Param(GetCustomerLoyaltyCommand.EXTRA_LOYALTY) LoyaltyViewModel loyalty){
            onDataLoaded(loyalty);
        }

        public abstract void onDataLoaded(LoyaltyViewModel loyalty);
    }

    public static void start(Context context, String customerId, String orderId, BaseGetCustomerLoyaltyCallback callback){
        create(GetCustomerLoyaltyCommand.class).callback(callback).arg(ARG_CUSTOMER_ID, customerId).arg(ARG_ORDER_ID, orderId).queueUsing(context);
    }
}
