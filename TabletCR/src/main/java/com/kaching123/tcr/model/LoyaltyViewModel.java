package com.kaching123.tcr.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by vkompaniets on 05.07.2016.
 */
public class LoyaltyViewModel implements Serializable {

    public LoyaltyPlanModel planModel;
    public List<IncentiveExModel> incentiveExModels;

    public LoyaltyViewModel(LoyaltyPlanModel planModel, List<IncentiveExModel> incentiveExModels) {
        this.planModel = planModel;
        this.incentiveExModels = incentiveExModels;
    }

    public static class IncentiveExModel extends LoyaltyIncentiveModel {
        public IncentiveItemExModel incentiveItemExModel;

        public IncentiveExModel(LoyaltyIncentiveModel incentiveModel, IncentiveItemExModel incentiveItemExModel) {
            super(incentiveModel.guid, incentiveModel.name, incentiveModel.type, incentiveModel.rewardType, incentiveModel.birthdayOffset, incentiveModel.pointThreshold, incentiveModel.rewardValue, incentiveModel.rewardValueType);
            this.incentiveItemExModel = incentiveItemExModel;
        }
    }

    public static class IncentiveItemExModel extends LoyaltyIncentiveItemModel {
        public ItemModel item;

        public IncentiveItemExModel(LoyaltyIncentiveItemModel incentiveItemModel, ItemModel item) {
            super(incentiveItemModel.guid, incentiveItemModel.incentiveGuid, incentiveItemModel.itemGuid, incentiveItemModel.price, incentiveItemModel.qty);
            this.item = item;
        }
    }
}
