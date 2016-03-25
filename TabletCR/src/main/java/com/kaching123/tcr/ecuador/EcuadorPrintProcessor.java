package com.kaching123.tcr.ecuador;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.function.OrderTotalPriceCursorQuery;
import com.kaching123.tcr.function.ReadPaymentTransactionsFunction;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PrepaidReleaseResult;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.print.FormatterUtil;
import com.kaching123.tcr.print.processor.PrintOrderProcessor;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.CustomerTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.OperatorTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.RegisterTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.CalculationUtil;
import com.telly.groundy.PublicGroundyTask;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.fragment.UiHelper.concatFullname;
import static com.kaching123.tcr.model.ContentValuesUtil._orderType;
import static com.kaching123.tcr.util.CalculationUtil.negative;
import static com.kaching123.tcr.util.DateUtils.formatEcuador;

public class EcuadorPrintProcessor extends PrintOrderProcessor {

    private static final Uri URI_ORDER = ShopProvider.getContentUri(ShopStore.SaleOrderView.URI_CONTENT);

    private static final String DEFAULT_CUSTOMER_ID = "9999999999999";

    private final String TRANSACTION_FEE = "Transaction Fee";

    private boolean reprint;

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


    public EcuadorPrintProcessor(String orderGuid, PublicGroundyTask.IAppCommandContext appCommandContext) {
        super(orderGuid, appCommandContext);
    }

    public EcuadorPrintProcessor(String orderGuid, boolean reprint, PublicGroundyTask.IAppCommandContext appCommandContext) {
        super(orderGuid, appCommandContext);
        this.reprint = reprint;
    }

    private void printTopOffset(ITextPrinter printerWrapper) {
        printerWrapper.emptyLine(12);// top offset 2.5 - 3 sm 0.4 mm per line
        linesCount += 12;
    }

    private String getCurrentDate(Cursor c) {
        long createTime = c.getLong(c.getColumnIndex(SaleOrderTable.CREATE_TIME));
        return formatEcuador(new Date(createTime));
    }

    private String getCustomerId(Cursor c) {
        String customerIdentification = c.getString(c.getColumnIndex(CustomerTable.CUSTOMER_IDENTIFICATION));
        if (TextUtils.isEmpty(customerIdentification)) {
            customerIdentification = DEFAULT_CUSTOMER_ID;
        }
        return customerIdentification;
    }

    private String getCustomerName(Cursor c) {
        String firstName = c.getString(c.getColumnIndex(CustomerTable.FISRT_NAME));
        String lastName = c.getString(c.getColumnIndex(CustomerTable.LAST_NAME));
        return concatFullname(firstName, lastName);
    }

    private String getOperatorName(Cursor c) {
        String firstName = c.getString(c.getColumnIndex(OperatorTable.FIRST_NAME));
        String lastName = c.getString(c.getColumnIndex(OperatorTable.LAST_NAME));
        return concatFullname(firstName, lastName);
    }

    private String getOrderNumber(Cursor c) {
        String registerTitle = c.getString(c.getColumnIndex(RegisterTable.TITLE));
        int seqNum = c.getInt(c.getColumnIndex(SaleOrderTable.PRINT_SEQ_NUM));
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

    private int linesCount = 0;

    @Override
    public void printHeader(Context context, TcrApplication app, ITextPrinter printerWrapper) {
        printTopOffset(printerWrapper);
        if (reprint) {
            printerWrapper.subTitle(context.getString(R.string.print_order_copy_header));
            printerWrapper.emptyLine();
        }
        Cursor c = ProviderAction.query(URI_ORDER)
                .projection(
                        RegisterTable.TITLE,
                        SaleOrderTable.PRINT_SEQ_NUM,
                        SaleOrderTable.CREATE_TIME,
                        OperatorTable.FIRST_NAME,
                        OperatorTable.LAST_NAME,
                        SaleOrderTable.ORDER_TYPE,
                        CustomerTable.CUSTOMER_IDENTIFICATION,
                        CustomerTable.FISRT_NAME,
                        CustomerTable.LAST_NAME)
                .where(SaleOrderTable.GUID + " = ?", orderGuid)
                .perform(context);

        if (c.moveToFirst()) {
            printerWrapper.header(context.getString(R.string.printer_ec_date), getCurrentDate(c));
            printerWrapper.header(context.getString(R.string.printer_ec_customer_id), getCustomerId(c));
            if (!TextUtils.isEmpty(getCustomerName(c))) {
                printerWrapper.header(context.getString(R.string.printer_ec_customer_name), getCustomerName(c));
            }
            Logger.d("printer_ec_store_address: " + getStoreAddress(app));
            printerWrapper.header(context.getString(R.string.printer_ec_store_address), getStoreAddress(app));
            Logger.d("printer_ec_cashier: " + getOperatorName(c));
            printerWrapper.header(context.getString(R.string.printer_ec_cashier), getOperatorName(c));
            if (!TextUtils.isEmpty(app.getShopInfo().email)) {
                printerWrapper.header(context.getString(R.string.printer_ec_email), app.getShopInfo().email);
            }
            Logger.d("printer_ec_order: " + getOrderNumber(c));
            printerWrapper.header(context.getString(R.string.printer_ec_order), getOrderNumber(c));
        }
        OrderType orderType = _orderType(c, c.getColumnIndex(SaleOrderTable.ORDER_TYPE));
        c.close();
        printerWrapper.emptyLine();
        printMidTid(context, app, printerWrapper, orderType);
        printerWrapper.drawLine();
    }

    @Override
    protected void printMidTid(ITextPrinter printer, String label, String value, boolean bold) {
        printer.addWithTab2(label, value, true, bold);
    }

    @Override
    protected void printBody(final Context context, final TcrApplication app, final ITextPrinter printerWrapper) {
        final String changeText = context.getString(R.string.print_order_change_label);
        final String itemDiscountText = context.getString(R.string.print_order_item_discount);
        final List<PaymentTransactionModel> payments = (transactions != null && transactions.size() != 0) ? transactions : ReadPaymentTransactionsFunction.loadByOrderSingle(context, orderGuid);
        OrderTotalPriceCursorQuery.loadSync(context, orderGuid, new OrderTotalPriceCursorQuery.PrintHandler() {
            @Override
            public void handleItem(String saleItemGuid, String description, String unitLabel, PriceType priceType, BigDecimal qty,
                                   BigDecimal itemSubtotal, BigDecimal itemDiscount,
                                   BigDecimal itemTax, BigDecimal singleItemPrice, List<Unit> units, ArrayList<SaleOrderItemViewModel.AddonInfo> addons, BigDecimal transactionFee, BigDecimal itemFullPrice, String note) {
                List<String> unitAsStrings = new ArrayList<String>(units.size());
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
                    printerWrapper.add(description, qty, itemSubtotal, unitAsStrings);
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
                    printerWrapper.addNotes(note);
            }

            @Override
            public void handleTotal(BigDecimal totalSubtotal, BigDecimal totalDiscount, BigDecimal totalTax, BigDecimal tipsAmount, BigDecimal transactionFee) {
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
            }
        });


        for (PaymentTransactionModel p : payments) {
            updateHasCreditCardPayment(p.gateway.isCreditCard());
            boolean isChanged = p.changeAmount != null && BigDecimal.ZERO.compareTo(p.changeAmount) < 0;
            printerWrapper.payment(p.cardName == null ? p.gateway.name() : p.cardName, isChanged ? p.amount.add(p.changeAmount).add(p.cashBack.negate()) : p.amount.add(p.cashBack.negate()));

            if (isChanged) {
                printerWrapper.change(changeText, p.changeAmount);
            }
            //rafael: add isEBTCash
            if (p.balance != null && p.gateway.isEbt()) {
                printerWrapper.orderFooter(context.getString(R.string.printer_balance), new BigDecimal(FormatterUtil.priceFormat(p.balance)), true);
            }
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
        String[] prints = receipt.split("\\n");
        return prints;
    }

}