package com.kaching123.tcr.countries.peru;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.function.OrderTotalPriceCursorQuery;
import com.kaching123.tcr.function.ReadPaymentTransactionsFunction;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PrepaidReleaseResult;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.print.FormatterUtil;
import com.kaching123.tcr.print.processor.PrintOrderProcessor;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.util.PhoneUtil;
import com.telly.groundy.PublicGroundyTask;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.kaching123.tcr.fragment.UiHelper.concatFullname;
import static com.kaching123.tcr.model.ContentValuesUtil._orderType;
import static com.kaching123.tcr.print.FormatterUtil.percentFormat;
import static com.kaching123.tcr.util.CalculationUtil.negative;
import static com.kaching123.tcr.util.DateUtils.formatPeru;

public class PeruPrintProcessor extends PrintOrderProcessor {
    private static final Uri URI_ORDER = ShopProvider.getContentUri(ShopStore.SaleOrderView.URI_CONTENT);

    private static final String DEFAULT_CUSTOMER_ID = "9999999999999";

    private static final String DEFAULT_CUSTOMER_NAME = "CONSUMIDOR FINAL";

    private final String TRANSACTION_FEE = "Transaction Fee";

    private static final int EC_RECEIPT_LINES_COUNT = 46;

    private boolean reprint;

    private ITextPrinter printerWrapper;

    public void setPrinterWrapper(ITextPrinter printerWrapper) {
        this.printerWrapper = printerWrapper;
    }

    public PeruPrintProcessor(String orderGuid, PublicGroundyTask.IAppCommandContext appCommandContext) {
        super(orderGuid, appCommandContext);
    }

    public PeruPrintProcessor(String orderGuid, boolean reprint, PublicGroundyTask.IAppCommandContext appCommandContext) {
        super(orderGuid, appCommandContext);
        this.reprint = reprint;
    }

    private void printTopOffset(ITextPrinter printerWrapper) {
        printerWrapper.emptyLine(2);
    }

    private String getCurrentDate(Cursor c) {
        long createTime = c.getLong(c.getColumnIndex(ShopSchema2.SaleOrderView2.SaleOrderTable.CREATE_TIME));
        return formatPeru(new Date(createTime));
    }

    private String getCustomerId(Cursor c) {
        String customerIdentification = c.getString(c.getColumnIndex(ShopSchema2.SaleOrderView2.CustomerTable.CUSTOMER_IDENTIFICATION));
        if (TextUtils.isEmpty(customerIdentification)) {
            customerIdentification = DEFAULT_CUSTOMER_ID;
        }
        return customerIdentification;
    }

    private String getCustomerName(Cursor c) {
        String firstName = c.getString(c.getColumnIndex(ShopSchema2.SaleOrderView2.CustomerTable.FISRT_NAME));
        String lastName = c.getString(c.getColumnIndex(ShopSchema2.SaleOrderView2.CustomerTable.LAST_NAME));
        String fullName = concatFullname(firstName, lastName);
        return TextUtils.isEmpty(fullName) ? DEFAULT_CUSTOMER_NAME : fullName;
    }

    private String getOperatorName(Cursor c) {
        String firstName = c.getString(c.getColumnIndex(ShopSchema2.SaleOrderView2.OperatorTable.FIRST_NAME));
        String lastName = c.getString(c.getColumnIndex(ShopSchema2.SaleOrderView2.OperatorTable.LAST_NAME));
        return concatFullname(firstName, lastName);
    }

    private String getOrderNumber(Cursor c) {
        String registerTitle = c.getString(c.getColumnIndex(ShopSchema2.SaleOrderView2.RegisterTable.TITLE));
        int seqNum = c.getInt(c.getColumnIndex(ShopSchema2.SaleOrderView2.SaleOrderTable.PRINT_SEQ_NUM));
        return orderNumber = registerTitle + "-" + seqNum;
    }

    private String getStoreAddress(TcrApplication app) {
        ShopInfoViewJdbcConverter.ShopInfo shopInfo = app.getShopInfo();
        StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(shopInfo.address1)) {
            builder.append(shopInfo.address1).append(" ");
        }
        String cityStateZip = getCityStateZip(shopInfo);
        if (!TextUtils.isEmpty(cityStateZip)) {
            builder.append(cityStateZip);
        }
        return builder.toString();
    }

    private void printNullable(Context context, int resId, String value) {
        if (!TextUtils.isEmpty(value)) {
            printNotNull(context, resId, value);
        }
    }

    private void printNotNull(Context context, int resId, String value) {
        printerWrapper.header(context.getString(resId), value);
    }

    @Override
    public void printHeader(Context context, TcrApplication app, ITextPrinter printerWrapper) {
        setPrinterWrapper(printerWrapper);
        printTopOffset(printerWrapper);
        if (reprint) {
            printerWrapper.subTitle(context.getString(R.string.print_order_copy_header));
            printerWrapper.emptyLine();
        }
       //super.printHeader(context, app, printerWrapper);

        printHeader(context, app);

        Cursor c = ProviderAction.query(URI_ORDER)
                .projection(
                        ShopSchema2.SaleOrderView2.RegisterTable.TITLE,
                        ShopSchema2.SaleOrderView2.SaleOrderTable.PRINT_SEQ_NUM,
                        ShopSchema2.SaleOrderView2.SaleOrderTable.CREATE_TIME,
                        ShopSchema2.SaleOrderView2.OperatorTable.FIRST_NAME,
                        ShopSchema2.SaleOrderView2.OperatorTable.LAST_NAME,
                        ShopSchema2.SaleOrderView2.SaleOrderTable.ORDER_TYPE,
                        ShopSchema2.SaleOrderView2.CustomerTable.CUSTOMER_IDENTIFICATION,
                        ShopSchema2.SaleOrderView2.CustomerTable.FISRT_NAME,
                        ShopSchema2.SaleOrderView2.CustomerTable.LAST_NAME)
                .where(ShopSchema2.SaleOrderView2.SaleOrderTable.GUID + " = ?", orderGuid)
                .perform(context);

        if (c.moveToFirst()) {
            printNotNull(context, R.string.printer_ec_date, getCurrentDate(c));
            printNotNull(context, R.string.printer_ec_customer_id, getCustomerId(c));
            printNullable(context, R.string.printer_ec_customer_name, getCustomerName(c));
//            printNullable(context, R.string.printer_ec_store_address, getStoreAddress(app));
            printNotNull(context, R.string.printer_ec_cashier, getOperatorName(c));
            printNullable(context, R.string.printer_ec_email, app.getShopInfo().email);
            printNotNull(context, R.string.printer_ec_order, getOrderNumber(c));
        }
        OrderType orderType = _orderType(c, c.getColumnIndex(ShopSchema2.SaleOrderView2.SaleOrderTable.ORDER_TYPE));
        c.close();
        printerWrapper.emptyLine();
        printMidTid(context, app, printerWrapper, orderType);
    }

    private void printHeader(Context context, TcrApplication app) {
        ShopInfoViewJdbcConverter.ShopInfo shopInfo = app.getShopInfo();

        printerWrapper.header(shopInfo.name);

        if (!TextUtils.isEmpty(shopInfo.address1)) {
            printerWrapper.footer(shopInfo.address1);
        }

        String cityStateZip = getCityStateZip(shopInfo);
        if (!TextUtils.isEmpty(cityStateZip)) {
            printerWrapper.footer(cityStateZip);
        }

        String phone = PhoneUtil.parse(shopInfo.phone);
        if (!TextUtils.isEmpty(phone)) {
            printerWrapper.footer(phone);
        }
        if (title != null && !title.equalsIgnoreCase("ARG_ORDER_TITLE")) {
            printerWrapper.emptyLine();
            printerWrapper.header(context.getString(R.string.printer_check));
        }

        printerWrapper.emptyLine();
    }

    @Override
    protected void printMidTid(ITextPrinter printer, String label, String value, boolean bold) {
        printer.addWithTab2(label, value, true, bold);
    }


    @Override
    protected void printBody(final Context context, final TcrApplication app, final ITextPrinter printerWrapper) {
        final String changeText = context.getString(R.string.print_order_change_label);
        final String itemDiscountText = context.getString(R.string.print_order_item_discount);
        final List<PaymentTransactionModel> payments = (transactions != null && transactions.size() != 0) ?
                transactions : ReadPaymentTransactionsFunction.loadByOrderSingle(context, orderGuid);

        ((PosPeruOrderTextPrinter) printerWrapper).addHeaderTitle(context.getString(R.string.printer_ec_header_description),
                context.getString(R.string.printer_ec_header_qty),
               // context.getString(R.string.printer_ec_header_iva),
                context.getString(R.string.printer_ec_header_dto),
                context.getString(R.string.printer_ec_header_total),
                context.getString(R.string.printer_ec_header_unit_price));

        printerWrapper.drawLine();

        OrderTotalPriceCursorQuery.loadSync(context, orderGuid, new OrderTotalPriceCursorQuery.PrintHandler() {
            @Override
            public void handleItem(String saleItemGuid, String description, String unitLabel,
                                   PriceType priceType, BigDecimal qty, BigDecimal itemSubtotal,
                                   BigDecimal itemDiscount, BigDecimal itemTax, BigDecimal singleItemPrice,
                                   List<Unit> units, ArrayList<SaleOrderItemViewModel.AddonInfo> addons,
                                   BigDecimal transactionFee, BigDecimal itemFullPrice, String note, TaxGroupModel model1, TaxGroupModel model2, BigDecimal loyaltyPoints) {
                List<String> unitAsStrings = new ArrayList<>(units.size());
                for (Unit unit : units) {
                    unitAsStrings.add(unit.serialCode);
                }

                BigDecimal itemPrice = itemFullPrice;
                if (addons != null && addons.size() != 0)
                    for (SaleOrderItemViewModel.AddonInfo addon : addons) {
                        itemPrice = itemPrice.subtract(addon.addon.extraCost);
                    }
                itemSubtotal = CalculationUtil.getSubTotal(qty, itemPrice);
                if (app.getShopPref().printDetailReceipt().get()) {
                    printerWrapper.add(description, qty, itemSubtotal, itemPrice, unitLabel, priceType == PriceType.UNIT_PRICE, unitAsStrings);
                } else {
                    /*String isIva;
                    if (model1 != null && model1.title != null && (model1.title.toUpperCase().contains("IVA"))
                    || ((model2 != null && model2.title != null && model2.title.toUpperCase().contains("IVA"))))
                        isIva = context.getString(R.string.printer_tax_is_iva);
                    else
                        isIva = context.getString(R.string.printer_tax_is_not_iva);
                    */
                    ((PosPeruOrderTextPrinter) printerWrapper).addPeru(description, qty.toString(),
                            /*isIva,*/ itemDiscount, itemSubtotal.subtract(itemDiscount), itemPrice, unitAsStrings);
//                    printerWrapper.add(description, qty, itemSubtotal, itemPrice, unitAsStrings);
                }
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
                if (note != null) {
                    printerWrapper.addNotes(note, context.getString(R.string.notes_edit_fragment_title) + ": ");
                }
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

                /*for (TaxGroupModel key : taxes.keySet()) {
                    if (!TextUtils.isEmpty(key.title)) {
                        printerWrapper.orderFooter(key.title.toUpperCase() + " " + percentFormat(key.tax), taxes.get(key));
                    } else {
                        printerWrapper.orderFooter(context.getString(R.string.item_tax_group_default).toUpperCase()
                                + " " + percentFormat(TcrApplication.get().getTaxVat()), taxes.get(key));
                    }
                }*/

                for (TaxGroupModel key : taxes.keySet()) {
                    if (!TextUtils.isEmpty(key.title)) {
                        printerWrapper.orderFooter(
                                String.format( "%1$s %2$s", key.title, percentFormat(key.tax)),
                                taxes.get(key));
                    } else {
                        printerWrapper.orderFooter(context.getString(R.string.item_tax_group_peru_default)
                                + " " + percentFormat(TcrApplication.get().getTaxVat()), taxes.get(key));
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

            /*public void handleTotal(BigDecimal totalSubtotal, Map<TaxGroupModel, BigDecimal> subtotals, BigDecimal totalDiscount,
                                    BigDecimal totalTax, BigDecimal totalLoyaltyPoints, BigDecimal tipsAmount,
                                    BigDecimal transactionFee, Map<TaxGroupModel, BigDecimal> taxes) {
                BigDecimal totalCashBack = BigDecimal.ZERO;
                printerWrapper.drawLine();
                for (PaymentTransactionModel p : payments) {
                    totalCashBack = totalCashBack.add(p.cashBack.negate());
                }
                if (totalCashBack.compareTo(BigDecimal.ZERO) > 0) {
                    printerWrapper.orderFooter(context.getString(R.string.printer_cash_back), totalCashBack);
                }
                if (BigDecimal.ZERO.compareTo(totalDiscount) != 0) {
                    printerWrapper.orderFooter(context.getString(R.string.printer_discount), totalDiscount);
                }
                for (TaxGroupModel key : subtotals.keySet()) {
                    if (!TextUtils.isEmpty(key.title)) {
                        printerWrapper.orderFooter(context.getString(R.string.printer_subtotal) + " " +
                                percentFormat(key.tax), subtotals.get(key));
                    } else {
                        printerWrapper.orderFooter(context.getString(R.string.printer_subtotal) + " "
                                + percentFormat(TcrApplication.get().getTaxVat()), subtotals.get(key));
                    }
                }
                printerWrapper.orderFooter(context.getString(R.string.printer_subtotal_iva), BigDecimal.ZERO);
                printerWrapper.orderFooter(context.getString(R.string.printer_subtotal_p_total), totalSubtotal.subtract(totalDiscount));

                for (TaxGroupModel key : taxes.keySet()) {
                    if (!TextUtils.isEmpty(key.title)) {
                        printerWrapper.orderFooter(key.title.toUpperCase() + " " + percentFormat(key.tax), taxes.get(key));
                    } else {
                        printerWrapper.orderFooter(context.getString(R.string.item_tax_group_default).toUpperCase()
                                + " " + percentFormat(TcrApplication.get().getTaxVat()), taxes.get(key));
                    }
                }
                printerWrapper.orderFooter(context.getString(R.string.printer_tips), tipsAmount);

                BigDecimal totalOrderPrice = totalSubtotal.add(totalTax).subtract(totalDiscount);
                totalOrderPrice = totalOrderPrice.setScale(2, BigDecimal.ROUND_UP);
                if (amountTotal == null) {
                    printerWrapper.orderFooter(context.getString(R.string.printer_total), totalOrderPrice.add(tipsAmount).add(transactionFee).add(totalCashBack), true);
                } else {
                    printerWrapper.orderFooter(context.getString(R.string.printer_total), new BigDecimal(amountTotal).add(transactionFee).add(totalCashBack), true);
                }
                printerWrapper.drawLine();
            }*/
        });

        boolean isEbtPaymentExists = false;
        BigDecimal ebtBalance = BigDecimal.ZERO;

        for (PaymentTransactionModel p : payments) {
            updateHasCreditCardPayment(p.gateway.isCreditCard());
            boolean isChanged = p.changeAmount != null && BigDecimal.ZERO.compareTo(p.changeAmount) < 0;

            printerWrapper.payment(p.cardName == null ? p.gateway == PaymentGateway.CASH ? context.getString(R.string.printer_cash) : p.gateway.name() : p.cardName.equalsIgnoreCase(context.getString(R.string.printer_check)) ? context.getString(R.string.printer_check_ecu) : p.cardName.equalsIgnoreCase(context.getString(R.string.printer_offline_credit))? context.getString(R.string.printer_offline_credit_ecu) : p.cardName.equalsIgnoreCase("Cash") ? context.getString(R.string.printer_cash) : p.cardName, isChanged ? p.amount.add(p.changeAmount).add(p.cashBack.negate()) : p.amount.add(p.cashBack.negate()));
            if (isChanged) {
                printerWrapper.change(changeText, p.changeAmount);
            }
            //rafael: add isEBTCash
            if (p.balance != null && p.gateway.isEbt()) {
                isEbtPaymentExists = true;
                ebtBalance = p.balance;
            }

            BigDecimal counts = getSaleItemAmount(orderGuid, context);
//            if (counts.compareTo(BigDecimal.ZERO) > 0) {
//                printerWrapper.header(context.getString(R.string.printer_sale_item_amount), String.valueOf(counts));
//            }
        }

        if (isEbtPaymentExists) {
            printerWrapper.orderFooter(context.getString(R.string.printer_balance), new BigDecimal(FormatterUtil.priceFormat(ebtBalance)), true);
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
    }

    private String[] getFormattedLine(String receipt) {
        return receipt.split("\\n");
    }
}
