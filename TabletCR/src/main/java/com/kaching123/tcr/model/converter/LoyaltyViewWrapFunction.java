package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.google.common.base.Function;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.LoyaltyIncentiveItemModel;
import com.kaching123.tcr.model.LoyaltyIncentiveModel;
import com.kaching123.tcr.model.LoyaltyPlanModel;
import com.kaching123.tcr.model.LoyaltyRewardType;
import com.kaching123.tcr.model.LoyaltyType;
import com.kaching123.tcr.model.LoyaltyViewModel;
import com.kaching123.tcr.model.LoyaltyViewModel.IncentiveExModel;
import com.kaching123.tcr.model.LoyaltyViewModel.IncentiveItemExModel;
import com.kaching123.tcr.store.ShopSchema2.LoyaltyView2.ItemTable;
import com.kaching123.tcr.store.ShopSchema2.LoyaltyView2.LoyaltyIncentiveItemTable;
import com.kaching123.tcr.store.ShopSchema2.LoyaltyView2.LoyaltyIncentiveTable;
import com.kaching123.tcr.store.ShopSchema2.LoyaltyView2.LoyaltyPlanTable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by vkompaniets on 05.07.2016.
 */
public class LoyaltyViewWrapFunction implements Function<Cursor, LoyaltyViewModel> {

    //ShopStore.LoyaltyView

    @Override
    public LoyaltyViewModel apply(Cursor c) {
        if (c == null || c.getCount() == 0) {
            return null;
        }

        LoyaltyPlanModel plan = null;
        List<IncentiveExModel> incentivesEx = new ArrayList<>(c.getCount());
        while (c.moveToNext()){
            if (plan == null){
                plan = planFromView(c);
            }
            LoyaltyIncentiveModel incentive = incentiveFromView(c);
            if (c.isNull(c.getColumnIndex(LoyaltyIncentiveItemTable.GUID))){
                incentivesEx.add(new IncentiveExModel(incentive, null));
            }else{
                LoyaltyIncentiveItemModel incentiveItem = incentiveItemFromView(c);
                IncentiveItemExModel incentiveItemEx;
                if (c.isNull(c.getColumnIndex(ItemTable.GUID))){
                    incentiveItemEx = new IncentiveItemExModel(incentiveItem, null);
                }else{
                    ItemModel item = itemFromView(c);
                    incentiveItemEx = new IncentiveItemExModel(incentiveItem, item);
                }
                incentivesEx.add(new IncentiveExModel(incentive, incentiveItemEx));
            }
        }

        return new LoyaltyViewModel(plan, incentivesEx);
    }

    public static LoyaltyPlanModel planFromView(Cursor c){
        return new LoyaltyPlanModel(
                c.getString(c.getColumnIndex(LoyaltyPlanTable.GUID)),
                c.getString(c.getColumnIndex(LoyaltyPlanTable.NAME))
        );
    }

    public static LoyaltyIncentiveModel incentiveFromView(Cursor c){
        return new LoyaltyIncentiveModel(
                c.getString(c.getColumnIndex(LoyaltyIncentiveTable.GUID)),
                c.getString(c.getColumnIndex(LoyaltyIncentiveTable.NAME)),
                LoyaltyType.valueOf(c.getInt(c.getColumnIndex(LoyaltyIncentiveTable.TYPE))),
                LoyaltyRewardType.valueOf(c.getInt(c.getColumnIndex(LoyaltyIncentiveTable.REWARD_TYPE))),
                c.getInt(c.getColumnIndex(LoyaltyIncentiveTable.BIRTHDAY_OFFSET)),
                _decimal(c, c.getColumnIndex(LoyaltyIncentiveTable.POINT_THRESHOLD), BigDecimal.ZERO),
                _decimal(c, c.getColumnIndex(LoyaltyIncentiveTable.REWARD_VALUE), BigDecimal.ZERO),
                DiscountType.valueOf(c.getInt(c.getColumnIndex(LoyaltyIncentiveTable.REWARD_VALUE_TYPE)))
        );
    }

    public static LoyaltyIncentiveItemModel incentiveItemFromView(Cursor c){
        return new LoyaltyIncentiveItemModel(
                c.getString(c.getColumnIndex(LoyaltyIncentiveItemTable.GUID)),
                c.getString(c.getColumnIndex(LoyaltyIncentiveItemTable.INCENTIVE_GUID)),
                c.getString(c.getColumnIndex(LoyaltyIncentiveItemTable.ITEM_GUID)),
                _decimal(c, c.getColumnIndex(LoyaltyIncentiveItemTable.PRICE), BigDecimal.ZERO),
                _decimalQty(c, c.getColumnIndex(LoyaltyIncentiveItemTable.QTY), BigDecimal.ZERO)
        );
    }

    public static ItemModel itemFromView(Cursor c){
        ItemModel item = new ItemModel(c.getString(c.getColumnIndex(ItemTable.GUID)));
        item.description = c.getString(c.getColumnIndex(ItemTable.DESCRIPTION));
        item.priceType = ContentValuesUtil._priceType(c, c.getColumnIndex(ItemTable.PRICE_TYPE));
        return item;
    }
}
