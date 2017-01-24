package com.kaching123.tcr.print.processor;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.pax.processor.PaxSignature;
import com.kaching123.tcr.function.OrderTotalPriceCursorQuery;
import com.kaching123.tcr.function.OrderTotalPriceCursorQuery.PrintHandler;
import com.kaching123.tcr.function.ReadPaymentTransactionsFunction;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PrepaidReleaseResult;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel.AddonComparator;
import com.kaching123.tcr.model.SaleOrderItemViewModel.AddonInfo;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.print.FormatterUtil;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.UnitLabelTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.util.UnitUtil;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.kaching123.tcr.fragment.UiHelper.integralIntegerFormat;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.util.CalculationUtil.negative;

/**
 * Created by gdubina on 23.12.13.
 */
public class PrintOrderProcessor extends BasePrintProcessor<ITextPrinter> {

    private boolean reprint;

    private final String TRANSACTION_FEE = "Transaction Fee";

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubtotal(String subtotal) {
        this.subTotal = subtotal;
    }

    public void setDiscountTotal(String discountTotal) {
        this.discountTotal = discountTotal;
    }

    public void setTaxTotal(String taxTotal) {
        this.taxTotal = taxTotal;
    }

    public void setPaxTransactions(ArrayList<PaymentTransactionModel> transactions) {
        this.transactions = transactions;
    }

    public void setAmountTotal(String amountTotal) {
        this.amountTotal = amountTotal;
    }

    public void setPrepaidReleaseResults(ArrayList<PrepaidReleaseResult> results) {
        this.prepaidReleaseResults = results;
    }

    public void setGiftCardResults(ArrayList<GiftCardBillingResult> giftCardResults) {
        this.giftCardResults = giftCardResults;
    }


    public PrintOrderProcessor(String orderGuid, IAppCommandContext appCommandContext) {
        super(orderGuid, appCommandContext);
    }

    public PrintOrderProcessor(String orderGuid, boolean reprint, IAppCommandContext appCommandContext) {
        super(orderGuid, appCommandContext);
        this.reprint = reprint;
    }

    @Override
    public void printHeader(Context context, TcrApplication app, ITextPrinter printerWrapper) {
        if (reprint) {
            printerWrapper.subTitle(context.getString(R.string.print_order_copy_header));
            printerWrapper.emptyLine();
        }

        super.printHeader(context, app, printerWrapper);
    }

    @Override
    protected void printMidTid(ITextPrinter printer, String label, String value, boolean bold) {
        printer.addWithTab2(label, value, true, bold);
    }

    protected void printBody(final Context context, final TcrApplication app, final ITextPrinter printerWrapper) {
        final String changeText = context.getString(R.string.print_order_change_label);
        final String itemDiscountText = context.getString(R.string.print_order_item_discount);
        final List<PaymentTransactionModel> payments = ReadPaymentTransactionsFunction.loadByOrderSingle(context, orderGuid);

        OrderTotalPriceCursorQuery.loadSync(context, orderGuid, new PrintHandler() {

            @Override
            public void handleItem(String saleItemGuid, String description, String unitLabel, PriceType priceType, BigDecimal qty,
                                   BigDecimal itemSubtotal, BigDecimal itemDiscount,
                                   BigDecimal itemTax, BigDecimal singleItemPrice, List<Unit> units, ArrayList<AddonInfo> addons, BigDecimal transactionFee, BigDecimal itemFullPrice, String note, TaxGroupModel model1, TaxGroupModel model2, BigDecimal loyaltyPoints) {
                List<String> unitAsStrings = new ArrayList<String>(units.size());
                if (addons != null)
                    Collections.sort(addons, new AddonComparator());
                for (Unit unit : units) {
                    unitAsStrings.add(unit.serialCode);
                }

                BigDecimal itemPrice = itemFullPrice;
                if (addons != null && addons.size() != 0)
                    for (SaleOrderItemViewModel.AddonInfo addon : addons) {
                        itemPrice = itemPrice.subtract(addon.addon.extraCost);
                    }
                itemSubtotal = CalculationUtil.getSubTotal(qty, itemPrice);
                if (app.getShopPref().printDetailReceipt().get())
                    printerWrapper.add(description, qty, itemSubtotal, itemPrice, unitLabel, priceType == PriceType.UNIT_PRICE, unitAsStrings);
                else
                    printerWrapper.add(description, qty, itemSubtotal, itemPrice, unitAsStrings);
                if (addons != null && addons.size() != 0)
                    for (SaleOrderItemViewModel.AddonInfo addon : addons) {
                        String title = addon.addonTitle;
                        if (addon.addon.type == ModifierType.OPTIONAL)
                            title = "NO " + addon.addonTitle;
                        BigDecimal newBD = addon.addon.extraCost.setScale(2);
                        printerWrapper.addAddsOn(title, newBD);
                    }
                if (transactionFee.compareTo(BigDecimal.ZERO) > 0) {
                    printerWrapper.addAddsOn(TRANSACTION_FEE, transactionFee);
                }
                if (itemDiscount.compareTo(BigDecimal.ZERO) == 1) {
                    printerWrapper.addItemDiscount(itemDiscountText, negative(itemDiscount));
                }
                if (note != null)
                    printerWrapper.addNotes(note, context.getString(R.string.notes_edit_fragment_title) + ": ");
            }

            @Override
            public void handleTotal(BigDecimal totalSubtotal, Map<TaxGroupModel, BigDecimal> subtotals, BigDecimal totalDiscount, BigDecimal totalTax, BigDecimal totalLoyaltyPoints, BigDecimal tipsAmount, BigDecimal transactionFee, Map<TaxGroupModel, BigDecimal> taxes) {
                BigDecimal totalCashBack = BigDecimal.ZERO;

                printerWrapper.drawLine();
                for (PaymentTransactionModel p : payments) {
                    totalCashBack = totalCashBack.add(p.cashBack.negate());
                }
                if (totalCashBack.compareTo(BigDecimal.ZERO) > 0)
                    printerWrapper.orderFooter(context.getString(R.string.printer_cash_back), totalCashBack);

                if (BigDecimal.ZERO.compareTo(totalDiscount) != 0) {
                    printerWrapper.orderFooter(context.getString(R.string.printer_discount), negative(totalDiscount));
                }
                if (subTotal == null)
                    printerWrapper.orderFooter(context.getString(R.string.printer_subtotal), totalSubtotal.subtract(totalDiscount).add(transactionFee).add(totalCashBack));
                else
                    printerWrapper.orderFooter(context.getString(R.string.printer_subtotal), (new BigDecimal(subTotal)).subtract(new BigDecimal(discountTotal).add(totalCashBack)));

                if (taxTotal == null)
                    printerWrapper.orderFooter(context.getString(R.string.printer_tax), totalTax);
                else
                    printerWrapper.orderFooter(context.getString(R.string.printer_tax), new BigDecimal(taxTotal));

                if (BigDecimal.ZERO.compareTo(tipsAmount) != 0) {
                    printerWrapper.orderFooter(context.getString(R.string.printer_tips), tipsAmount);
                }

                BigDecimal totalOrderPrice = totalSubtotal.add(totalTax).subtract(totalDiscount);
                if (amountTotal == null)
                    printerWrapper.orderFooter(context.getString(R.string.printer_total), totalOrderPrice.add(tipsAmount).add(transactionFee).add(totalCashBack), true);
                else
                    printerWrapper.orderFooter(context.getString(R.string.printer_total), new BigDecimal(amountTotal).add(transactionFee).add(totalCashBack), true);
                printerWrapper.drawLine();

                orderInfo.earnedLoyaltyPoints = totalLoyaltyPoints;
            }
        });


        boolean isEbtPaymentExists = false;
        boolean isGiftCardPaymnetExists = false;
        BigDecimal ebtBalance = BigDecimal.ZERO;
        BigDecimal giftCardBalance = BigDecimal.ZERO;

        BigDecimal totalPax = BigDecimal.ZERO;

        for (PaymentTransactionModel p : payments) {
            updateHasCreditCardPayment(p.gateway.isCreditCard());
            boolean isChanged = p.changeAmount != null && BigDecimal.ZERO.compareTo(p.changeAmount) < 0;
            printerWrapper.payment(p.cardName == null ? p.gateway.name() : p.cardName,
                    isChanged ? p.amount.add(p.changeAmount).add(p.cashBack.negate()) : p.amount.add(p.cashBack.negate()));
            if (isChanged) {
                printerWrapper.change(changeText, p.changeAmount);
            }
            if (p.balance != null && p.gateway.isEbt()) {
                isEbtPaymentExists = true;
                ebtBalance = p.balance;
            }
            if (giftCardResults != null)
                for (GiftCardBillingResult result : giftCardResults) {
                    isGiftCardPaymnetExists = true;
                    giftCardBalance = new BigDecimal(result.balance);
                }
            if ((p.balance != null && p.gateway.isGiftCard())) {
                isGiftCardPaymnetExists = true;
                giftCardBalance = p.balance;
            }

            totalPax = totalPax.add(p.amount);

        }

        if (isEbtPaymentExists) {
            printerWrapper.orderFooter(context.getString(R.string.printer_balance), new BigDecimal(FormatterUtil.priceFormat(ebtBalance)), true);
        }

        if (isGiftCardPaymnetExists) {
            printerWrapper.orderFooter(context.getString(R.string.printer_gift_card_balance), new BigDecimal(FormatterUtil.priceFormat(giftCardBalance)), true);
        }

        BigDecimal counts = getSaleItemAmount(orderGuid, context);
        if (counts.compareTo(BigDecimal.ZERO) > 0) {
            printerWrapper.header(context.getString(R.string.printer_sale_item_amount), String.valueOf(counts));
        }

        if (prepaidReleaseResults != null) {
            for (PrepaidReleaseResult result : prepaidReleaseResults) {
                if (Integer.parseInt(result.error) == 200) {
                    if (result.receipt != null) {
                        String[] prints = getFormattedLine(result.receipt);
                        for (String line : prints) {
                            printerWrapper.add(line);
                        }
                    }
                } else {
                    printerWrapper.header(context.getString(R.string.prepaid_mini_item_description), result.model.description);
                    printerWrapper.header(context.getString(R.string.prepaid_mini_fail_error), result.error);
                    printerWrapper.header(context.getString(R.string.prepaid_mini_fail_error_msg), result.errorMSG);
                }

            }
        }

        if(payments != null) {
            for (PaymentTransactionModel p : payments) {
                printerWrapper.drawLine();
                //if (reprint) {
                if (!TextUtils.isEmpty(p.cardName)) {
                    printerWrapper.header("Card Type:", p.cardName);
                }
                if (!TextUtils.isEmpty(p.lastFour)) {
                    printerWrapper.header("Account Number:", "####-####-####-" + p.lastFour);
                }
                if (!TextUtils.isEmpty(p.entryMethod)) {
                    printerWrapper.header("Entry:", getEntryMethod(p.entryMethod));
                }
                if (!TextUtils.isEmpty(p.applicationIdentifier)) {
                    printerWrapper.header("AID:", p.applicationIdentifier);
                }
                if (!TextUtils.isEmpty(p.applicationCryptogramType)) {
                    printerWrapper.header("ARQC:", p.applicationCryptogramType);
                }
                if (!TextUtils.isEmpty(p.authorizationNumber)) {
                    printerWrapper.header("Approval:", p.authorizationNumber);
                }

                printerWrapper.orderFooter("Total", p.amount, false);

                if (p.paxDigitalSignature != null) {
                    PaxSignature pax = new PaxSignature(null);
                    printerWrapper.printPaxSignature(pax.convertPaxFileStringToPrintedByteArray(p.paxDigitalSignature));
                }
                //}
            }
            printerWrapper.drawLine();
        }else{
            printerWrapper.drawLine();
        }


        /*
        if(transactions != null) {
            for (PaymentTransactionModel t : transactions) {

                printerWrapper.drawLine();

                PaxInformationPrintModel pipm = new PaxInformationPrintModel();

                if (!TextUtils.isEmpty(t.cardName))
                    pipm.Pax_CardType = t.cardName;

                if (!TextUtils.isEmpty(t.lastFour))
                    pipm.Pax_AccountNumber = t.lastFour;

                if (!TextUtils.isEmpty(t.entryMethod)) {
                    int Card_Entry_ID = Integer.parseInt(t.entryMethod);
                    pipm.Pax_Entry = getEntryModeByID(Card_Entry_ID);
                }

                if (!TextUtils.isEmpty(t.authorizationNumber))
                    pipm.Pax_Approval = t.authorizationNumber;

                if (!TextUtils.isEmpty(t.applicationIdentifier))
                    pipm.Pax_AID = t.applicationIdentifier;

                pipm.Pax_Value = t.amount;

                pipm.Pax_DigitalSignature = t.paxDigitalSignature;

                //paxInformationPrintModelList.add(pipm);

                printerWrapper.header("Card Type:", pipm.Pax_CardType);
                printerWrapper.header("Account Number:", "####-####-####-" + pipm.Pax_AccountNumber);
                printerWrapper.header("Entry:", pipm.Pax_Entry);
                if (!TextUtils.isEmpty(pipm.Pax_AID)) {
                    printerWrapper.header("AID:", pipm.Pax_AID);
                }
                if (!TextUtils.isEmpty(t.applicationCryptogramType)) {
                    printerWrapper.header("ARQC:", t.applicationCryptogramType);
                }
                printerWrapper.header("Approval:", pipm.Pax_Approval);
                printerWrapper.orderFooter("Total", pipm.Pax_Value, false);
                if (t.gateway.isTrueCreditCard() && app.getDigitalSignature() &&
                        app.requireSignatureOnTransactionsHigherThan && pipm.Pax_DigitalSignature != null) {
                    printerWrapper.printPaxSignature(pipm.Pax_DigitalSignature);
                }

            }
            printerWrapper.drawLine();
        }else{
            printerWrapper.drawLine();
        }
        /**/


        /** Pax Signature Bitmap Object ***********************************/
        if(TcrApplication.get().paxSignatureEmulator){
            printerWrapper.header("Card Type:", "Visa Test");
            printerWrapper.header("Account Number:", "####-####-####-1234");
            printerWrapper.header("Entry:", "Chip");
            printerWrapper.header("AID:", "ABC123123");
            printerWrapper.header("Approval:", "123456");

            PaxSignature paxSignature = new PaxSignature(null);
            try {
                Thread.sleep(1500);
                if (paxSignature != null) {
                    if (app.getDigitalSignature() && app.requireSignatureOnTransactionsHigherThan && paxSignature.signaturePaxFileString != null) {
                        printerWrapper.printPaxSignature(paxSignature.convertPaxFileStringToPrintedByteArray(paxSignature.signaturePaxFileString));
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        /*********************************** Pax Signature Bitmap Object **/


//        if (giftCardResults != null)
//            for (GiftCardBillingResult result : giftCardResults) {
//                printerWrapper.addAddsOn(result.model.description, result.model.finalPrice);
//            }
    }


    private String getEntryMethod(String entryMethod) {
        int method = Integer.parseInt(entryMethod.equalsIgnoreCase("") ? "6" : entryMethod);
        switch (method) {
            case 0:
                return "Manual";
            case 1:
                return "Swipe";
            case 2:
                return "Contactless";
            case 3:
                return "Scanner";
            case 4:
                return "Chip";
            case 5:
                return "Chip Fall Back Swipe";
            default:
                return "";
        }
    }

    @Override
    protected void printLoyalty(Context context, TcrApplication app, ITextPrinter printerWrapper) {
        if (orderInfo.customerLoyaltyPoints != null) {
            printerWrapper.header(context.getString(R.string.total_bonus_points_available), integralIntegerFormat(orderInfo.customerLoyaltyPoints));

            if (orderInfo.earnedLoyaltyPoints != null && orderInfo.earnedLoyaltyPoints.compareTo(BigDecimal.ZERO) != 0) {
                printerWrapper.header(context.getString(R.string.bonus_points_on_this_sale), integralIntegerFormat(orderInfo.earnedLoyaltyPoints));
            }
        }
    }

    @Override
    protected void printFooter(Context context, TcrApplication app, ITextPrinter printerWrapper) {
        /*if (orderInfo.customerLoyaltyPoints != null){
            printerWrapper.header(context.getString(R.string.total_bonus_points_available), integralIntegerFormat(orderInfo.customerLoyaltyPoints));

            if (orderInfo.earnedLoyaltyPoints != null && orderInfo.earnedLoyaltyPoints.compareTo(BigDecimal.ZERO) != 0){
                printerWrapper.header(context.getString(R.string.bonus_points_on_this_sale), integralIntegerFormat(orderInfo.earnedLoyaltyPoints));
            }
        }*/
        super.printFooter(context, app, printerWrapper);
    }

    private String[] getFormattedLine(String receipt) {
        return receipt.split("\\n");
    }

    private static final Uri SALE_ITEM_ORDER_URI = ShopProvider.getContentUri(ShopStore.SaleOrderItemsView.URI_CONTENT);

    protected BigDecimal getSaleItemAmount(String orderGuid, Context context) {
        BigDecimal saleItemAmount = BigDecimal.ZERO;
        Cursor c = ProviderAction.query(SALE_ITEM_ORDER_URI)
                .where(ShopSchema2.SaleOrderItemsView2.SaleItemTable.ORDER_GUID + " = ?", orderGuid)
                .perform(context);
        BigDecimal itemQty = BigDecimal.ZERO;
        HashMap<String, BigDecimal> list = new HashMap<>();
        if (c.moveToFirst()) {
            do {
                String unitLabel = c.getString(c.getColumnIndex(UnitLabelTable.SHORTCUT));
                itemQty = unitLabel != null && UnitUtil.isUnitLbs(unitLabel) ? BigDecimal.ONE : _decimal(c.getString(c.getColumnIndex(ShopSchema2.SaleOrderItemsView2.SaleItemTable.QUANTITY)), BigDecimal.ZERO);
                String itemGuid = c.getString(c.getColumnIndex(ShopSchema2.SaleOrderItemsView2.SaleItemTable.SALE_ITEM_GUID));
                list.put(itemGuid, itemQty);
//                if(!unitLabel.equalsIgnoreCase("LB"))
//                    saleItemAmount = saleItemAmount.add(itemQty);
//                else
//                    saleItemAmount = saleItemAmount.add(BigDecimal.ONE);
            } while (c.moveToNext());
            c.close();
        }
        Iterator iter = list.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            BigDecimal val = (BigDecimal) entry.getValue();
            saleItemAmount = saleItemAmount.add(val);
        }
        return saleItemAmount;
    }

}
