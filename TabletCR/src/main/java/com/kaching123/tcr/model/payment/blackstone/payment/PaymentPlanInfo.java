package com.kaching123.tcr.model.payment.blackstone.payment;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kaching123.tcr.websvc.WebAPI;

/**
 * SOme Payment plan entity, we dont know what it is yet
 *
 * @author Ivan v. Rikhmayer
 */
public class PaymentPlanInfo implements Parcelable {

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_PLANID)
    private int planId;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_SWIPEDISCOUNT)
    private double swipeDiscount;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_SWIPETRANSACTIONFEE)
    private double swipeTransactionFee;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_NONSWIPEDISCOUNT)
    private double nonSwipeDiscount;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_NONSWIPETRANSACTIONFEE)
    private double nonSwipeTransactionFee;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_MONTHLYFEE)
    private double monthlyFee;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_DESCRIPTION)
    private String description;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_PLANNAME)
    private String planName;

    @Expose
    @SerializedName(WebAPI.BlackStoneAPI.RESULT_PARAM_DISPLAYNAME)
    private String displayName;

    public PaymentPlanInfo() {

    }

    public PaymentPlanInfo(int planId,
                           double swipeDiscount,
                           double swipeTransactionFee,
                           double nonSwipeDiscount,
                           double nonSwipeTransactionFee,
                           double monthlyFee,
                           String description,
                           String planName,
                           String displayName) {
        this.planId = planId;
        this.swipeDiscount = swipeDiscount;
        this.swipeTransactionFee = swipeTransactionFee;
        this.nonSwipeDiscount = nonSwipeDiscount;
        this.nonSwipeTransactionFee = nonSwipeTransactionFee;
        this.monthlyFee = monthlyFee;
        this.description = description;
        this.planName = planName;
        this.displayName = displayName;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public double getSwipeDiscount() {
        return swipeDiscount;
    }

    public void setSwipeDiscount(double swipeDiscount) {
        this.swipeDiscount = swipeDiscount;
    }

    public double getSwipeTransactionFee() {
        return swipeTransactionFee;
    }

    public void setSwipeTransactionFee(double swipeTransactionFee) {
        this.swipeTransactionFee = swipeTransactionFee;
    }

    public double getNonSwipeDiscount() {
        return nonSwipeDiscount;
    }

    public void setNonSwipeDiscount(double nonSwipeDiscount) {
        this.nonSwipeDiscount = nonSwipeDiscount;
    }

    public double getNonSwipeTransactionFee() {
        return nonSwipeTransactionFee;
    }

    public void setNonSwipeTransactionFee(double nonSwipeTransactionFee) {
        this.nonSwipeTransactionFee = nonSwipeTransactionFee;
    }

    public double getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(double monthlyFee) {
        this.monthlyFee = monthlyFee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(planId);
        dest.writeDouble(swipeDiscount);
        dest.writeDouble(swipeTransactionFee);
        dest.writeDouble(nonSwipeDiscount);
        dest.writeDouble(nonSwipeTransactionFee);
        dest.writeDouble(monthlyFee);
        dest.writeString(description);
        dest.writeString(planName);
        dest.writeString(displayName);
    }

    public static Creator<PaymentPlanInfo> CREATOR = new Creator<PaymentPlanInfo>() {

        @Override
        public PaymentPlanInfo createFromParcel(Parcel source) {
            return new PaymentPlanInfo(source.readInt(),
                    source.readDouble(),
                    source.readDouble(),
                    source.readDouble(),
                    source.readDouble(),
                    source.readDouble(),
                    source.readString(),
                    source.readString(),
                    source.readString());
        }

        @Override
        public PaymentPlanInfo[] newArray(int size) {
            return new PaymentPlanInfo[size];
        }
    };

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n---\nplanId : ").append(planId)
                .append("\nswipeDiscount : ").append(swipeDiscount)
                .append("\nswipeTransactionFee : ").append(swipeTransactionFee)
                .append("\nnonSwipeDiscount : ").append(nonSwipeDiscount)
                .append("\nnonSwipeTransactionFee : ").append(nonSwipeTransactionFee)
                .append("\nmonthlyFee : ").append(monthlyFee)
                .append("\ndescription : ").append(description)
                .append("\nplanName : ").append(planName)
                .append("\ndisplayName : ").append(displayName).append("\n---\n");
        return sb.toString();
    }
}
