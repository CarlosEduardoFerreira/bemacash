package com.kaching123.tcr.activity.PrepaidActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.store.saleorder.AddBillPaymentOrderCommand;
import com.kaching123.tcr.commands.store.saleorder.RemoveSaleOrderCommand;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidBillPaymentAdditionalDataFragment;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidBillPaymentAdditionalDataFragment_;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidBillPaymentProductComfirmationFragment;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidBillPaymentProductComfirmationFragment_;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidBillPaymentProductFragment;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidBillPaymentProductFragment_;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidLongDistanceHeadFragment;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidLongDistanceHeadFragment.LongDistanceHeadInterface;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidLongDistanceHeadFragment_;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidLongDistanceProductAllSearchFragment;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidLongDistanceProductAllSearchFragment.LongDistanceSearchCallback;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidLongDistanceProductAllSearchFragment_;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidLongDistanceProductComfirmationFragment;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidLongDistanceProductComfirmationFragment_;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidLongDistanceProductCountrySearchFragment;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidLongDistanceProductCountrySearchFragment_;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidLongDistanceProductFragment;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidLongDistanceProductFragment_;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidLongDistanceProductInfoMenuFragment;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidLongDistanceProductPopularSearchFragment;
import com.kaching123.tcr.fragment.prepaid.LongDistance.PrepaidLongDistanceProductPopularSearchFragment_;
import com.kaching123.tcr.fragment.tendering.PrintAndFinishFragmentDialogBase;
import com.kaching123.tcr.fragment.tendering.history.HistoryDetailedOrderItemListFragment;
import com.kaching123.tcr.fragment.tendering.payment.INotificationConfirmListener;
import com.kaching123.tcr.fragment.tendering.payment.PayNotificationFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.billpayment.BillingFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.billpayment.PaymentFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless.DoTopUpFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless.PrepaidBillPaymentPrintAndFinishFragmentDialog;
import com.kaching123.tcr.fragment.tendering.pinserve.prepaid.wireless.PrepaidWirelessPrintAndFinishFragmentDialog;
import com.kaching123.tcr.model.BillPaymentDescriptionModel;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.blackstone.prepaid.Broker;
import com.kaching123.tcr.model.payment.blackstone.prepaid.IPrePaidInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.BillPaymentRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessItem;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessPinInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessTopupInfo;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.BillPaymentItem;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.DoTopUpRequest;
import com.kaching123.tcr.processor.MoneybackProcessor;
import com.kaching123.tcr.processor.PaymentProcessor;
import com.kaching123.tcr.websvc.api.prepaid.BillPaymentResponse;
import com.kaching123.tcr.websvc.api.prepaid.BillerLoadRecord;
import com.kaching123.tcr.websvc.api.prepaid.Category;
import com.kaching123.tcr.websvc.api.prepaid.PIN;
import com.kaching123.tcr.websvc.api.prepaid.PaymentOption;
import com.kaching123.tcr.websvc.api.prepaid.ProductAccessPhone;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by teli.yin on 10/28/2014.
 */
@EActivity(R.layout.prepaid_long_distance_activity)
public class PrepaidLongDistanceActivity extends PrepaidBaseFragmentActivity {

    @Extra
    protected int prepaidMode;
    private static final int WIRELESS_PROFILE_ID = 15;
    public static final int COUNTRY_SEARCH = 0;
    public static final int ALL_SEARCH = 1;
    public static final int MOST_POPULAR = 2;
    private int profileId = WIRELESS_PROFILE_ID;
    private WirelessItem chosenCategory;
    private BigDecimal changeAmount;
    private BillPaymentItem billPaymentItem;

    protected PrepaidLongDistanceProductFragment productFragment;
    protected PrepaidBillPaymentProductFragment billPaymentProductFragment;

    protected PrepaidLongDistanceHeadFragment longDistanceHead;

    private PrepaidLongDistanceProductPopularSearchFragment productPopularSearchFragment;

    private PrepaidLongDistanceProductAllSearchFragment productSearchFragment;

    private PrepaidLongDistanceProductComfirmationFragment productComfirmationFragment;

    private PrepaidLongDistanceProductCountrySearchFragment productCountrySearchFragment;

    private PrepaidBillPaymentAdditionalDataFragment billPaymentAdditionalDataFragment;

    private PrepaidDistanceBackInterface callback;
    private final AllSearchCallback allSearchCallback = new AllSearchCallback();
    private final HeadFragmentCallback headFragmentCallback = new HeadFragmentCallback();

    private final ProductComfirmationFragmentCallback productComfirmationFragmentCallback = new ProductComfirmationFragmentCallback();

    private final ProductProductFragmentCallback productProductFragmentCallback = new ProductProductFragmentCallback();

    private String customService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    public void init() {
        initFragment();
    }

    private void initFragment() {
        longDistanceHead = PrepaidLongDistanceHeadFragment_.builder().prepaidMode(prepaidMode).build();
        longDistanceHead.setCallback(headFragmentCallback);
        getSupportFragmentManager().beginTransaction().replace(R.id.long_distance_head, longDistanceHead).commit();

        productPopularSearchFragment = PrepaidLongDistanceProductPopularSearchFragment_.builder().prepaidMode(prepaidMode).transactionMode(transactionMode).user(user).cashierId(cashierId).build();
        productPopularSearchFragment.setCallback(allSearchCallback);
        getSupportFragmentManager().beginTransaction().
                add(R.id.long_distance_body, productPopularSearchFragment).
                commit();
        this.setCallback(productPopularSearchFragment);
        getSupportFragmentManager().addOnBackStackChangedListener(getListener());
    }

    public static void start(Context context, int prepaidMode) {
        PrepaidLongDistanceActivity_.intent(context).prepaidMode(prepaidMode).startForResult(PrepaidProcessorActivity.REQUEST_CODE);
    }


    class HeadFragmentCallback implements LongDistanceHeadInterface {
        @Override
        public void onHomePressed() {
            finish();
        }

        @Override
        public void onBackButtonPressed() {
            callback.onBackPressed();
        }
    }


    public void setCallback(PrepaidDistanceBackInterface callback) {
        this.callback = callback;
    }

    class ProductProductFragmentCallback implements PrepaidLongDistanceProductFragment.ProductFragmentCallback {
        @Override
        public void menuSelected(int position) {
            longDistanceHead.setInstroTextView(position);
        }

        @Override
        public void conditionSelected(BigDecimal amount, String phoneNumber, BigDecimal feeAmount, Broker broker) {
            productComfirmationFragment = new PrepaidLongDistanceProductComfirmationFragment_().builder().broker(broker).amount(amount).feeAmount(feeAmount).phoneNumber(phoneNumber).chosenCategory(chosenCategory).build();
            productComfirmationFragment.setCallback(productComfirmationFragmentCallback);
            setCallback(productComfirmationFragment);
            getSupportFragmentManager().beginTransaction().addToBackStack("PrepaidLongDistanceProductComfirmationFragment").hide(productFragment).add(R.id.long_distance_body, productComfirmationFragment).commit();

        }

        @Override
        public void headMessage(int errorCode) {
            longDistanceHead.setInstroTextView(errorCode);
        }

        @Override
        public void popUpFragment(int searchMode) {
            backToPreviousLayout();
            switch (searchMode) {
                case ALL_SEARCH:
                    setCallback(productSearchFragment);
                    break;
                case COUNTRY_SEARCH:
                    setCallback(productCountrySearchFragment);
                    break;
                case MOST_POPULAR:
                    setCallback(productPopularSearchFragment);
                    break;
            }
        }

        @Override
        public void additionalDataRequired(Category chosenCategory, BillPaymentItem chosenBillPaymentItem, PaymentOption chosenOption, String cashierId, PrepaidUser user, String transactionMode, BigDecimal fee, BigDecimal chosenAmount, BigDecimal total, BillerLoadRecord billerData, String accountNumber) {
            billPaymentAdditionalDataFragment = PrepaidBillPaymentAdditionalDataFragment_.builder()
                    .chosenCategory(chosenCategory)
                    .chosenBillPaymentItem(chosenBillPaymentItem)
                    .chosenOption(chosenOption)
                    .cashierId(cashierId)
                    .user(user)
                    .transactionMode(transactionMode)
                    .fee(fee).chosenAmount(chosenAmount)
                    .total(total)
                    .billerData(billerData)
                    .accountNumber(accountNumber)
                    .build();
            billPaymentAdditionalDataFragment.setCallback(new BillpyamnetAddtionalDataCallback());
            getSupportFragmentManager().beginTransaction().addToBackStack("PrepaidBillPaymentAdditionalDataFragment").hide(billPaymentProductFragment).add(R.id.long_distance_body, billPaymentAdditionalDataFragment).commit();

        }

        @Override
        public void error(String message) {
            Toast.makeText(PrepaidLongDistanceActivity.this, message, Toast.LENGTH_LONG).show();
            finish();
        }
    }


    private class BillpyamnetAddtionalDataCallback implements PrepaidBillPaymentAdditionalDataFragment.ProductAdditionalDataCallback {

        @Override
        public void complete(Category chosenCategory, BillPaymentItem chosenBillPaymentItem, PaymentOption chosenOption, BigDecimal amount, BillerLoadRecord billerData, String accountNumber, BillPaymentRequest formedRequest, BigDecimal total, BigDecimal transactionFee) {
            proceedToConfirmationPage(chosenCategory, chosenBillPaymentItem, chosenOption, amount, billerData, accountNumber, formedRequest, total, transactionFee);
        }

        @Override
        public void popUpFragment() {
            backToPreviousLayout();
        }

        @Override
        public void headMessage(int code) {
            longDistanceHead.setInstroTextView(code);
        }
    }

    ;

    protected void proceedToConfirmationPage(
            final Category chosenCategory,
            final BillPaymentItem chosenBillPaymentItem,
            final PaymentOption chosenOption,
            final BigDecimal chosenAmount,
            final BillerLoadRecord billerData,
            final String accountNumber,
            final BillPaymentRequest formedRequest,
            final BigDecimal total,
            final BigDecimal transactionFee) {
        PrepaidBillPaymentProductComfirmationFragment billPaymentProductComfirmationFragment = PrepaidBillPaymentProductComfirmationFragment_.builder()
                .chosenCategory(chosenCategory)
                .chosenBillPaymentItem(chosenBillPaymentItem)
                .chosenOption(chosenOption)
                .transactionFee(transactionFee)
                .chosenAmount(chosenAmount)
                .formedRequest(formedRequest)
                .total(total)
                .billerData(billerData)
                .accountNumber(accountNumber)
                .build();
        billPaymentProductComfirmationFragment.setCallback(new PrepaidBillPaymentProductComfirmationFragment.BillpaymentConfirmCallback() {

            @Override
            public void comfirm() {
                proceedToPayment(PrepaidLongDistanceActivity.this, chosenAmount, billerData.vendorName, BillPaymentDescriptionModel.PrepaidType.BILL_PAYMENT, transactionFee, Broker.BILL_PAYMENT, null, null, formedRequest, chosenCategory, chosenBillPaymentItem);
            }

            @Override
            public void popUpFragment() {
                backToPreviousLayout();
            }
        });
        getSupportFragmentManager().beginTransaction().addToBackStack("PrepaidBillPaymentProductComfirmationFragment").hide(billPaymentAdditionalDataFragment).add(R.id.long_distance_body, billPaymentProductComfirmationFragment).commit();

    }


    class ProductComfirmationFragmentCallback implements PrepaidLongDistanceProductComfirmationFragment.ProductComfirmationCallback {
        @Override
        public void comfirm(String phoneNumber, BigDecimal amount, WirelessItem chosenCategory, BigDecimal feeAmount,  Broker broker) {
            BillPaymentDescriptionModel.PrepaidType type;
            if (chosenCategory.isPinBased())
                type = BillPaymentDescriptionModel.PrepaidType.WIRELESS_PIN;
            else
                type = BillPaymentDescriptionModel.PrepaidType.WIRELESS_TOPUP;

            proceedToPayment(PrepaidLongDistanceActivity.this, amount, chosenCategory.name, type, feeAmount, broker, chosenCategory.countryCode, phoneNumber, null, null, null);
        }

        @Override
        public void popUpFragment() {
            longDistanceHead.setInstroTextView(PrepaidLongDistanceProductInfoMenuFragment.SELECT_AMOUNT);
            backToPreviousLayout();
        }

    }

    protected void proceedToPayment(final FragmentActivity context, final BigDecimal amount, final String description, final BillPaymentDescriptionModel.PrepaidType type, final BigDecimal transactionFee, final Broker broker, final String countryCode, final String phoneNumber, final BillPaymentRequest formedRequest, final Category chosenCategory, final BillPaymentItem chosenBillPaymentItem) {

        PaymentFragmentDialog.show(context, Boolean.TRUE, amount, description, type, broker, transactionFee, new AddBillPaymentOrderCommand.BaseAddBillPaymentOrderCallback() {

            @Override
            protected void handleSuccess(final String orderGuid, final long prepaidOrderId) {
                hide();
                PaymentProcessor.create(orderGuid, OrderType.PREPAID, null, null).callback(new PaymentProcessor.IPaymentProcessor() {
                    @Override
                    public void onSuccess() {
                        if (broker != Broker.BILL_PAYMENT)
                            proceedToBilling(context, prepaidOrderId, transactionFee, amount, countryCode, orderGuid, null, phoneNumber);
                        else {
                            formedRequest.setOrderId(prepaidOrderId);
                            proceedToBillPaymentBilling(context, formedRequest, prepaidOrderId, chosenCategory, amount, transactionFee, orderGuid, null, chosenBillPaymentItem);
                        }

                    }

                    @Override
                    public void onCancel() {
                        RemoveSaleOrderCommand.start(context, context, orderGuid, OrderType.PREPAID);
                    }

                    @Override
                    public void onPrintValues(String order, ArrayList<PaymentTransactionModel> list, BigDecimal changeAmount) {
                        PrepaidLongDistanceActivity.this.changeAmount = changeAmount;
                        if (broker != Broker.BILL_PAYMENT)
                            proceedToBilling(context, prepaidOrderId, transactionFee, amount, countryCode, orderGuid, list, phoneNumber);
                        else {
                            formedRequest.setOrderId(prepaidOrderId);
                            proceedToBillPaymentBilling(context, formedRequest, prepaidOrderId, chosenCategory, amount, transactionFee, orderGuid, list, chosenBillPaymentItem);
                        }
                    }

                    @Override
                    public void onBilling(ArrayList<PaymentTransactionModel> successfullCCtransactionModels, List<SaleOrderItemViewModel> prepaidList, List<SaleOrderItemViewModel> giftCardList) {

                    }

                    @Override
                    public void onRefund(HistoryDetailedOrderItemListFragment.RefundAmount amount) {

                    }

                    @Override
                    public void onUpdateOrderList() {

                    }

                    @Override
                    public void onPrintComplete() {

                    }

                }).setPrepaidMode().init(context);

            }

            @Override
            protected void handleFailure() {
                Logger.e("PrepaidProcessor: AddBillPaymentOrderCommand failed!");
                hide();
                finish();
            }

            private void hide() {
                PaymentFragmentDialog.hide(context);
            }
        });
    }

    protected void proceedToBillPaymentBilling(final FragmentActivity context,
                                               final BillPaymentRequest formedRequest, final long prepaidOrderId, final Category chosenCategory, final BigDecimal amount, final BigDecimal transactionFee, final String orderGuid, final ArrayList<PaymentTransactionModel> transactions, BillPaymentItem chosenBillPaymentItem) {
        BillingFragmentDialog.show(context, formedRequest, transactionFee, orderGuid,chosenBillPaymentItem, new BillingFragmentDialog.BillingFragmentDialogCallback() {

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
                proceedToNotification(context, false, messageSpannable, orderGuid, transactions);
            }

            @Override
            public void onComplete(BillPaymentResponse response, BigDecimal transactionFee, String orderNum, String total, BillPaymentItem chosenBillPaymentItem) {
                hide();
//                IPrePaidInfo info = new BillPaymentInfo(mChosenOption);
                proceedToBillPaymentPrintFinish(context, orderGuid, transactions, null, chosenCategory, formedRequest, amount, transactionFee, orderNum, total, chosenBillPaymentItem);
            }

            private void hide() {
                BillingFragmentDialog.hide(context);
            }
        });
    }

    private void proceedToBillPaymentPrintFinish(final FragmentActivity context, String orderGuid, final ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info, Category chosenCategory, BillPaymentRequest request, BigDecimal amount, BigDecimal transactionFee, String orderNum, String total, BillPaymentItem chosenBillPaymentItem) {
        PrepaidBillPaymentPrintAndFinishFragmentDialog.show(context, orderGuid, new PrintAndFinishFragmentDialogBase.IFinishConfirmListener() {

            @Override
            public void onConfirmed() {
                finish();
            }
        }, transactions, info, chosenCategory, request, amount, changeAmount, transactionFee, orderNum, total, chosenBillPaymentItem);
    }


    protected void proceedToBilling(final FragmentActivity context, final long orderId, final BigDecimal transactionFee, BigDecimal amount, String countryCode, final String orderGuid, final ArrayList<PaymentTransactionModel> transactions, String phoneNumber) {
        final DoTopUpRequest request = new DoTopUpRequest();
        request.orderID = orderId;
        request.cashier = cashierId;
        request.countryCode = countryCode;
        request.phoneNumber = phoneNumber;
        request.profileID = profileId;
        request.mID = String.valueOf(user.getMid());
        request.tID = String.valueOf(user.getTid());
        request.password = user.getPassword();
        request.productMaincode = chosenCategory.code;
        request.topUpAmount = amount;
        request.transactionMode = transactionMode;
        request.type = chosenCategory.type;
        DoTopUpFragmentDialog.show(context, request, orderGuid, new DoTopUpFragmentDialog.DoTopUpFragmentDialogCallback() {

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
                proceedToNotification(context, false, messageSpannable, orderGuid, transactions);
            }

            @Override
            public void onPrintPin(PIN response, String orderNum, String total) {
                hide();

                IPrePaidInfo info = new WirelessPinInfo(response.controlNumber, customService, response.transactionID, response.localAccessPhones, response.pinNumber, response.expirationDate);
                proceedToWirelessPrintFinish(context, orderGuid, transactions, info, chosenCategory, transactionFee, orderNum, total);

            }

            @Override
            public void onComplete(PIN response, String orderNum, String total) {
                hide();

                IPrePaidInfo info = new WirelessTopupInfo(response.controlNumber, customService, response.transactionID, response.localAccessPhones, request.phoneNumber, response.referenceNumber, response.authorizationCode);
                proceedToWirelessPrintFinish(context, orderGuid, transactions, info, chosenCategory, transactionFee, orderNum, total);

            }

            private void hide() {
                DoTopUpFragmentDialog.hide(context);
            }
        });
    }

    private void proceedToNotification(final FragmentActivity context,
                                       final boolean success,
                                       final Spannable message,
                                       final String orderGuid,
                                       final ArrayList<PaymentTransactionModel> transactions) {

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
                proceedToVoid(context, orderGuid, transactions);
            }

            @Override
            public void onConfirmed() {
                hide();
                proceedToVoid(context, orderGuid, transactions);
            }

            private void hide() {
                PayNotificationFragmentDialog.hide(context);
            }
        });
    }

    private void proceedToVoid(final FragmentActivity context, final String orderGuid, final List<PaymentTransactionModel> transactions) {
        //we leave void for payment process, we can cancel transaction during multiple tendering
        MoneybackProcessor.create(orderGuid, OrderType.PREPAID).callback(new MoneybackProcessor.IVoidCallback() {


            @Override
            public void onVoidComplete(List<PaymentTransactionModel> completed, SaleOrderModel childOrderModel) {
                RemoveSaleOrderCommand.start(context, context, orderGuid, OrderType.PREPAID);
                finish();
            }

            @Override
            public void onVoidFailure(BigDecimal ignore) {
                finish();
            }

            @Override
            public void onVoidCancelled() {
                finish();
            }
        }).childOrderModel(new SaleOrderModel(orderGuid))
                .initVoid(context,
                        null,
                        transactions,
                        TcrApplication.get().getBlackStoneUser(), true, true);
    }

    private void proceedToWirelessPrintFinish(final FragmentActivity context, String orderGuid, final ArrayList<PaymentTransactionModel> transactions, IPrePaidInfo info, WirelessItem chosenCategory, BigDecimal transactionFee, String orderNum, String total) {
        PrepaidWirelessPrintAndFinishFragmentDialog.show(context, orderGuid, new PrintAndFinishFragmentDialogBase.IFinishConfirmListener() {

            @Override
            public void onConfirmed() {
                setResult(PrepaidProcessorActivity.REQUEST_CODE, new Intent().putExtra(PrepaidProcessorActivity.TRANSACTION_COMPLETE, true));
                finish();
            }
        }, transactions, info, chosenCategory, changeAmount, transactionFee, orderNum, total);
    }


    public interface PrepaidDistanceBackInterface {
        void onBackPressed();
    }

    private android.support.v4.app.FragmentManager.OnBackStackChangedListener getListener() {
        android.support.v4.app.FragmentManager.OnBackStackChangedListener result = new android.support.v4.app.FragmentManager.OnBackStackChangedListener() {
            public void onBackStackChanged() {
                if (productCountrySearchFragment != null && !productCountrySearchFragment.isHidden())
                    productCountrySearchFragment.onResume();
            }
        };

        return result;
    }

    private void setCustomService() {
        if (chosenCategory == null || chosenCategory.productAccessPhones == null)
            return;
        for (ProductAccessPhone accessPhone : chosenCategory.productAccessPhones) {
            if (accessPhone == null || accessPhone.city == null)
                return;
            if (accessPhone.city.equalsIgnoreCase(getString(R.string.custom_service)))
                customService = accessPhone.phoneNumber;
            break;
        }

    }

    private class AllSearchCallback implements LongDistanceSearchCallback {
        @Override
        public void productSelected(WirelessItem item, int searchMode) {
            chosenCategory = item;
            setCustomService();
            productFragment = PrepaidLongDistanceProductFragment_.builder().searchMode(searchMode).prepaidMode(prepaidMode).chosenCategory(item).user(user).transactionId(transactionId).build();
            productFragment.setCallback(productProductFragmentCallback);
            switch (searchMode) {
                case ALL_SEARCH:
                    getSupportFragmentManager().beginTransaction().
                            addToBackStack("PrepaidLongDistanceProductFragment").
                            hide(productSearchFragment).
                            add(R.id.long_distance_body, productFragment).
                            commit();
                    break;
                case COUNTRY_SEARCH:
                    getSupportFragmentManager().beginTransaction().
                            addToBackStack("PrepaidLongDistanceProductFragment").
                            hide(productCountrySearchFragment).
                            add(R.id.long_distance_body, productFragment).
                            commit();
                    break;
                case MOST_POPULAR:
                    getSupportFragmentManager().beginTransaction().
                            addToBackStack("PrepaidLongDistanceProductFragment").
                            hide(productPopularSearchFragment).
                            add(R.id.long_distance_body, productFragment).
                            commit();
                    break;
            }
            setCallback(productFragment);
            longDistanceHead.setInstroTextView(PrepaidLongDistanceProductInfoMenuFragment.SELECT_AMOUNT);
        }


        @Override
        public void onBillPaymentItemSelected(BillPaymentItem item, int mode) {
            billPaymentItem = item;
            billPaymentProductFragment = PrepaidBillPaymentProductFragment_.builder().cashierId(cashierId).transactionMode(transactionMode).searchMode(mode).billPaymentItem(billPaymentItem).user(user).transactionId(transactionId).build();
            billPaymentProductFragment.setCallback(productProductFragmentCallback);
            switch (mode) {
                case ALL_SEARCH:
                    getSupportFragmentManager().beginTransaction().
                            addToBackStack("PrepaidLongDistanceBillProductFragment").
                            hide(productSearchFragment).
                            add(R.id.long_distance_body, billPaymentProductFragment).
                            commit();
                    break;
                case COUNTRY_SEARCH:
                    getSupportFragmentManager().beginTransaction().
                            addToBackStack("PrepaidLongDistanceBillProductFragment").
                            hide(productCountrySearchFragment).
                            add(R.id.long_distance_body, billPaymentProductFragment).
                            commit();
                    break;
                case MOST_POPULAR:
                    getSupportFragmentManager().beginTransaction().
                            addToBackStack("PrepaidLongDistanceBillProductFragment").
                            hide(productPopularSearchFragment).
                            add(R.id.long_distance_body, billPaymentProductFragment).
                            commit();
                    break;
            }
            setCallback(billPaymentProductFragment);
            longDistanceHead.setInstroTextView(PrepaidLongDistanceProductInfoMenuFragment.SELECT_AMOUNT);
        }

        @Override
        public void searchModeSelected(int Mode) {
            FragmentManager manager = getSupportFragmentManager();
            switch (Mode) {
                case COUNTRY_SEARCH:

                    if (productSearchFragment != null && productSearchFragment.isAdded())
                        manager.beginTransaction().
                                hide(productSearchFragment).commit();
                    if (productPopularSearchFragment.isAdded())
                        manager.beginTransaction().
                                hide(productPopularSearchFragment).commit();

                    if (productCountrySearchFragment == null || !productCountrySearchFragment.isAdded()) {
                        productCountrySearchFragment = PrepaidLongDistanceProductCountrySearchFragment_.builder().prepaidMode(prepaidMode).build();
                        productCountrySearchFragment.setCallback(allSearchCallback);
                        manager.beginTransaction()
                                .add(R.id.long_distance_body, productCountrySearchFragment).commit();
                    } else {
                        productCountrySearchFragment.setCallback(allSearchCallback);
                        manager.beginTransaction().show(productCountrySearchFragment).commit();
                    }

                    setCallback(productCountrySearchFragment);
                    break;
                case ALL_SEARCH:

                    if (productCountrySearchFragment != null && productCountrySearchFragment.isAdded())
                        manager.beginTransaction().
                                hide(productCountrySearchFragment).commit();
                    if (productPopularSearchFragment.isAdded())
                        manager.beginTransaction().
                                hide(productPopularSearchFragment).commit();
                    if (productSearchFragment == null || !productSearchFragment.isAdded()) {
                        productSearchFragment = PrepaidLongDistanceProductAllSearchFragment_.builder().prepaidMode(prepaidMode).transactionMode(transactionMode).user(user).cashierId(cashierId).build();
                        productSearchFragment.setCallback(allSearchCallback);
                        manager.beginTransaction()
                                .add(R.id.long_distance_body, productSearchFragment)
                                .commit();
                    } else {
                        productSearchFragment.setCallback(allSearchCallback);
                        manager.beginTransaction().show(productSearchFragment).commit();
                    }
                    setCallback(productSearchFragment);
                    break;
                case MOST_POPULAR:
                    if (productSearchFragment != null && productSearchFragment.isAdded())
                        manager.beginTransaction().
                                hide(productSearchFragment).commit();
                    if (productCountrySearchFragment != null && productCountrySearchFragment.isAdded())
                        manager.beginTransaction().
                                hide(productCountrySearchFragment).commit();
                    if (!productPopularSearchFragment.isAdded()) {
                        manager.beginTransaction()
                                .add(R.id.long_distance_body, productPopularSearchFragment).commit();
                    } else {
                        productPopularSearchFragment.setCallback(allSearchCallback);
                        manager.beginTransaction().show(productPopularSearchFragment).commit();
                    }
                    setCallback(productPopularSearchFragment);
                    break;
            }
        }

        @Override
        public void searchProductByCountryName(String name) {
            productCountrySearchFragment.startSearchByCountryName(name);
        }

        @Override
        public void popUpFragment() {
            backToPreviousLayout();
        }

        @Override
        public void headMessage(int messageCode) {
            longDistanceHead.setInstroTextView(messageCode);
        }

        @Override
        public void error(String error) {
            Toast.makeText(PrepaidLongDistanceActivity.this, error, Toast.LENGTH_LONG).show();
            backToPreviousLayout();
        }
    }

    public void backToPreviousLayout() {
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        callback.onBackPressed();
    }
}
