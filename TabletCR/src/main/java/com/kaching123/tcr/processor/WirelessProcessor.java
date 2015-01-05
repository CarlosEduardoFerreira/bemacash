package com.kaching123.tcr.processor;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.tendering.payment.INotificationConfirmListener;
import com.kaching123.tcr.fragment.tendering.payment.PayNotificationFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless.DoTopUpFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless.DoTopUpFragmentDialog.DoTopUpFragmentDialogCallback;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless.WirelessCategoryFragmentDialog;
import com.kaching123.tcr.fragment.wireless.WirelessConfirmationPageDialog;
import com.kaching123.tcr.model.BillPaymentDescriptionModel.PrepaidType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.blackstone.prepaid.Broker;
import com.kaching123.tcr.model.payment.blackstone.prepaid.IPrePaidInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessPinInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessTopupInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.DoTopUpRequest;
import com.kaching123.tcr.websvc.api.prepaid.PIN;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * @author Ivan v. Rikhmayer
 */
public class WirelessProcessor {

    private static final int WIRELESS_PROFILE_ID = 15;

    private PrepaidProcessorCallback callback;
    private String transactionMode;
    private String cashierId;
    private PrepaidUser user;
    private WirelessItem chosenCategory;
    private boolean international;
    private boolean longDistance;
    private boolean pinless;
    private BigDecimal amount;
    private String phoneNumber;
    private String code;
    private int profileId = WIRELESS_PROFILE_ID;
    private String orderGuid;
    private ArrayList<PaymentTransactionModel> transactions;

    private Broker broker;

    public static WirelessProcessor create(PrepaidUser user, String transactionMode, String cashierId, boolean international, Broker broker) {
        return new WirelessProcessor(user, transactionMode, cashierId, international, broker);
    }

    public WirelessProcessor pinlessMode() {
        this.pinless = true;
        return this;
    }

    public WirelessProcessor longDistanceMode() {
        longDistance = true;
        return this;
    }

    public WirelessProcessor callback(PrepaidProcessorCallback callback) {
        this.callback = callback;
        return this;
    }

    public WirelessProcessor(PrepaidUser user, String transactionMode, String cashierId, boolean international, Broker broker) {
        this.transactionMode = transactionMode;
        this.user = user;
        this.cashierId = cashierId;
        this.international = international;
        this.broker = broker;
    }

    public void init(final FragmentActivity context) {
        proceedToWireless(context);
    }

    protected void proceedToWireless(final FragmentActivity context) {
        WirelessCategoryFragmentDialog.show(context,
                transactionMode, cashierId, user, international, longDistance, pinless, broker,
                new WirelessCategoryFragmentDialog.WirelessCategoryFragmentDialogCallback() {

                    @Override
                    public void onCancel() {
                        hide();
                        callback.onCancel(context);
                    }

                    @Override
                    public void onError(String message) {
                        hide();
                        callback.onCancel(context);
                    }

                    @Override
                    public void onConditionSelected(final WirelessItem chosenCategory, final BigDecimal amount, final String phoneNumber, final int profileId, final BigDecimal transactionFee) {
                        WirelessProcessor.this.chosenCategory = chosenCategory;
                        WirelessProcessor.this.amount = amount;
                        WirelessProcessor.this.code = chosenCategory.countryCode;
                        hide();
                        proceedToConfirmationPage(context, international, phoneNumber, profileId, chosenCategory, amount, transactionFee);
                    }

                    private void hide() {
                        WirelessCategoryFragmentDialog.hide(context);
                    }
                }
        );

    }

    protected void proceedToCredentialsInput(final FragmentActivity context, final boolean international, String phoneNumber, int profileId, final BigDecimal transactionFee) {
//        WirelessCredentialsFragmentDialog.show(context, cashierId, user, transactionMode, chosenCategory, new WirelessCredentialsFragmentDialogCallback() {
//
//            @Override public void onCancel() {
//                hide();
//                callback.onCancel(context);
//            }
//
//            @Override public void onError(String message) {
//                hide();
//                callback.onCancel(context);
//            }
//
//            @Override public void onComplete(String phoneNumber, int profileId) {
//                hide();
        WirelessProcessor.this.phoneNumber = phoneNumber;
//                WirelessProcessor.this.code = code;
        WirelessProcessor.this.profileId = profileId;
        callback.onPaymentRequested(context, amount, chosenCategory.name, PrepaidType.WIRELESS_TOPUP, new SubProcessorDelegate() {

            @Override
            public void onComplete(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, long prepaidOrderId) {
                WirelessProcessor.this.orderGuid = orderGuid;
                WirelessProcessor.this.transactions = transactions;
                proceedToBilling(context, international, prepaidOrderId, transactionFee);
            }
        }, BigDecimal.ZERO);
//            }

//            private void hide() {
//                WirelessCredentialsFragmentDialog.hide(context);
//            }
//        });
    }

    protected void proceedToConfirmationPage(final FragmentActivity context, final boolean international, final String phoneNumber, final int profileId, final WirelessItem chosenCategory, final BigDecimal amount, final BigDecimal transactionFee) {
        WirelessConfirmationPageDialog.show(context, international, phoneNumber, profileId, chosenCategory, amount, orderGuid, new WirelessConfirmationPageDialog.WirelessConfirmationPageDialogCallback() {
                    @Override
                    public void onCancel() {
                        hide();
                        callback.onCancel(context);
                    }

                    @Override
                    public void onError(String message) {
                        hide();
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        callback.onCancel(context);
                    }

                    @Override
                    public void onComplete() {
                        hide();
                        if (chosenCategory.isPinBased()) {
                            callback.onPaymentRequested(context, amount, chosenCategory.name, PrepaidType.WIRELESS_PIN, new SubProcessorDelegate() {

                                @Override
                                public void onComplete(FragmentActivity context, String orderGuid, ArrayList<PaymentTransactionModel> transactions, long prepaidOrderId) {
                                    WirelessProcessor.this.orderGuid = orderGuid;
                                    WirelessProcessor.this.transactions = transactions;
                                    proceedToBilling(context, international, prepaidOrderId, transactionFee);
                                }
                            }, BigDecimal.ZERO);
                        } else {
                            proceedToCredentialsInput(context, international, phoneNumber, profileId, transactionFee);
                        }

                    }

                    private void hide() {
                        WirelessConfirmationPageDialog.hide(context);
                    }
                }
        );
    }

    protected void proceedToBilling(final FragmentActivity context, boolean international, final long orderId, final BigDecimal transactionFee) {
        final DoTopUpRequest request = new DoTopUpRequest();
        request.orderID = orderId;
        request.cashier = cashierId;
        request.countryCode = code;
        request.phoneNumber = phoneNumber;
        request.profileID = profileId;
        request.mID = String.valueOf(user.getMid());
        request.tID = String.valueOf(user.getTid());
        request.password = user.getPassword();
        request.productMaincode = chosenCategory.code;
        request.topUpAmount = amount;
        request.transactionMode = transactionMode;
        request.type = chosenCategory.type;
        DoTopUpFragmentDialog.show(context, request, orderGuid, new DoTopUpFragmentDialogCallback() {

            @Override
            public void onError(String message) {
                hide();
                final Spannable messageSpannable;
                String mainString = context.getResources().getString(R.string.blackstone_pay_failure_body_1st);
                messageSpannable = new SpannableString(context.getResources().getString(R.string.blackstone_pay_failure_body_constructor,
                        mainString, "The pre-paid gateway has responded with message : ", message));
                messageSpannable.setSpan(new ForegroundColorSpan(Color.RED),
                        0,
                        mainString.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageSpannable.setSpan(new ForegroundColorSpan(Color.RED),
                        messageSpannable.length() - message.length(),
                        messageSpannable.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                proceedToNotification(context, false, messageSpannable);
            }

            //complete callback for Pin based wireless
            @Override
            public void onPrintPin(PIN response, String orderNum, String total) {
                hide();

                IPrePaidInfo info = new WirelessPinInfo(response.controlNumber, response.customerServiceEnglish, response.transactionID, response.localAccessPhones, response.pinNumber, response.expirationDate);
//                callback.onPrintRequested(context, orderGuid, transactions, info);
                callback.onWirelessPrintRequested(context, orderGuid, transactions, info, chosenCategory, transactionFee, orderNum, total);
            }

            //complete callback for DoTopUp wireless
            @Override
            public void onComplete(PIN response, String orderNum, String total) {
                hide();

                IPrePaidInfo info = new WirelessTopupInfo(response.controlNumber, response.customerServiceEnglish, response.transactionID, response.localAccessPhones, request.phoneNumber, response.referenceNumber, response.authorizationCode);
//                callback.onPrintRequested(context, orderGuid, transactions, info);
                callback.onWirelessPrintRequested(context, orderGuid, transactions, info, chosenCategory, transactionFee, orderNum, total);
            }

            private void hide() {
                DoTopUpFragmentDialog.hide(context);
            }
        });
    }

    private void proceedToNotification(final FragmentActivity context,
                                       final boolean success,
                                       final Spannable message) {

        PayNotificationFragmentDialog.show(context, false, success, false, message, false, new INotificationConfirmListener() {

            @Override
            public void onReload(Object UIFragent) {
                // ignore
            }

            @Override
            public void onRetry() {
                // ignore
            }

            @Override
            public void onCancel() {
                hide();
                callback.onVoidRequested(context, orderGuid, transactions);
            }

            @Override
            public void onConfirmed() {
                hide();
                callback.onVoidRequested(context, orderGuid, transactions);
            }

            private void hide() {
                PayNotificationFragmentDialog.hide(context);
            }
        });
    }
}
