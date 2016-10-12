package com.kaching123.tcr.countries.puertorico;

import android.content.Context;
import android.text.TextUtils;

import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.function.OrderTotalPriceCursorQuery;
import com.kaching123.tcr.function.ReadPaymentTransactionsFunction;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PrepaidReleaseResult;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.print.FormatterUtil;
import com.kaching123.tcr.print.processor.GiftCardBillingResult;
import com.kaching123.tcr.print.processor.PrintOrderProcessor;
import com.kaching123.tcr.util.CalculationUtil;
import com.telly.groundy.PublicGroundyTask;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.kaching123.tcr.util.CalculationUtil.negative;

/**
 * Created by alboyko on 23.09.2016.
 */

public class PuertoricoPrintProcessor extends PrintOrderProcessor {
    private final String TRANSACTION_FEE = "Transaction Fee";
    private boolean reprint;
    private ITextPrinter printerWrapper;

    public void setPrinterWrapper(ITextPrinter printerWrapper) {
        this.printerWrapper = printerWrapper;
    }

    public PuertoricoPrintProcessor(String orderGuid, PublicGroundyTask.IAppCommandContext appCommandContext) {
        super(orderGuid, appCommandContext);
    }

    public PuertoricoPrintProcessor(String orderGuid, boolean reprint, PublicGroundyTask.IAppCommandContext appCommandContext) {
        super(orderGuid, appCommandContext);
        this.reprint = reprint;
    }

    private String[] getFormattedLine(String receipt) {
        return receipt.split("\\n");
    }

    @Override
    protected void printBody(final Context context, final TcrApplication app, final ITextPrinter printerWrapper) {
        final String changeText = context.getString(R.string.print_order_change_label);
        final String itemDiscountText = context.getString(R.string.print_order_item_discount);
        final List<PaymentTransactionModel> payments =  ReadPaymentTransactionsFunction.loadByOrderSingle(context, orderGuid);
        OrderTotalPriceCursorQuery.loadSync(context, orderGuid, new OrderTotalPriceCursorQuery.PrintHandler() {

            @Override
            public void handleItem(String saleItemGuid, String description, String unitLabel, PriceType priceType, BigDecimal qty,
                                   BigDecimal itemSubtotal, BigDecimal itemDiscount,
                                   BigDecimal itemTax, BigDecimal singleItemPrice, List<Unit> units, ArrayList<SaleOrderItemViewModel.AddonInfo> addons, BigDecimal transactionFee, BigDecimal itemFullPrice, String note, TaxGroupModel model1, TaxGroupModel model2, BigDecimal loyaltyPoints) {
                List<String> unitAsStrings = new ArrayList<>(units.size());
                if (addons != null)
                    Collections.sort(addons, new SaleOrderItemViewModel.AddonComparator());
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

                for (TaxGroupModel key : taxes.keySet()) {
                    if (!TextUtils.isEmpty(key.title)) {
                        printerWrapper.orderFooter(
                                //String.format( context.getString(R.string.item_multi_tax_group), key.title.toUpperCase()),//, percentFormat(key.tax)),
                                String.format( context.getString(R.string.item_multi_tax_group), key.title),//, percentFormat(key.tax)),
                                taxes.get(key));
                    } else {
                       // printerWrapper.orderFooter(context.getString(R.string.item_tax_group_default).toUpperCase()
                            //    + " " + percentFormat(TcrApplication.get().getTaxVat()), taxes.get(key));
                        printerWrapper.orderFooter(context.getString(R.string.item_tax_group_puerto_rico_default), taxes.get(key));
                    }
                }


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

        for (PaymentTransactionModel p : payments) {
            updateHasCreditCardPayment(p.gateway.isCreditCard());
            boolean isChanged = p.changeAmount != null && BigDecimal.ZERO.compareTo(p.changeAmount) < 0;
            printerWrapper.payment(p.cardName == null ? p.gateway.name() : p.cardName, isChanged ? p.amount.add(p.cashBack.negate()) : p.amount.add(p.cashBack.negate()));
            if (isChanged) {
                printerWrapper.change(changeText, p.changeAmount);
            }
            if (p.balance != null && p.gateway.isEbt()) {
                isEbtPaymentExists = true;
                ebtBalance = p.balance;
            }
            if (p.balance != null && p.gateway.isGiftCard()) {
                isGiftCardPaymnetExists = true;
                giftCardBalance = p.balance;
            }
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

        if (prepaidReleaseResults != null)
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
        if (giftCardResults != null)
            for (GiftCardBillingResult result : giftCardResults) {
                printerWrapper.addAddsOn(result.model.description, result.model.finalPrice);
            }
    }
}

