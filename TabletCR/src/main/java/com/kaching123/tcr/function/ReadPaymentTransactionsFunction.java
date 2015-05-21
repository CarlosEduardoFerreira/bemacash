package com.kaching123.tcr.function;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.Loader;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.PaymentTransactionView2.PaymentTransactionTable;
import com.kaching123.tcr.store.ShopSchema2.PaymentTransactionView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._paymentGateway;
import static com.kaching123.tcr.model.ContentValuesUtil._paymentStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._paymentType;

/**
 * Created by gdubina on 15/01/14.
 */
public final class ReadPaymentTransactionsFunction {

    private static final Uri URI_TRANSACTIONS = ShopProvider.getContentUri(PaymentTransactionView.URI_CONTENT);

    public static Loader<ArrayList<PaymentTransactionModel>> createLoaderOnlySaleOrderByAmount(Context context, String orderGuid) {
        orderGuid = orderGuid == null ? "" : orderGuid;
        return CursorLoaderBuilder.forUri(URI_TRANSACTIONS)
                .where("(" + PaymentTransactionTable.STATUS + " = ? OR " + PaymentTransactionTable.STATUS + " = ?)", PaymentStatus.PRE_AUTHORIZED.ordinal(), PaymentStatus.SUCCESS.ordinal())
                .where("(" + SaleOrderTable.GUID + " = ? or " + SaleOrderTable.PARENT_ID + " = ?)", orderGuid, orderGuid)
                .wrap(new Function<Cursor, ArrayList<PaymentTransactionModel>>() {
                    @Override
                    public ArrayList<PaymentTransactionModel> apply(Cursor c) {
                        ArrayList<PaymentTransactionModel> result = readTransactions(c);
                        result = filter(result, new Predicate<PaymentTransactionModel>() {
                            @Override
                            public boolean apply(PaymentTransactionModel p) {
                                return p.paymentType == PaymentType.SALE && BigDecimal.ZERO.compareTo(p.availableAmount) == -1;
                            }
                        });
                        // DISABLED TO http://194.79.22.58:8080/browse/ACR-378
                        Collections.sort(result, new Comparator<PaymentTransactionModel>() {
                            @Override
                            public int compare(PaymentTransactionModel l, PaymentTransactionModel r) {
                                return l.createTime == null ? -1 : r.createTime == null ? 1 : l.createTime.compareTo(r.createTime);
                            }
                        });
                        //            Collections.sort(result, new Comparator<PaymentTransactionModel>() {
                        //                @Override
                        //                public int compare(PaymentTransactionModel l, PaymentTransactionModel r) {
                        //                    return l.availableAmount.compareTo(r.availableAmount);
                        //                }
                        //            });
                        return result;
                    }
                })
                .build(context);
    }

    private static ArrayList<PaymentTransactionModel> filterSaleOnly(ArrayList<PaymentTransactionModel> list) {
        return filter(list, new Predicate<PaymentTransactionModel>() {
            @Override
            public boolean apply(PaymentTransactionModel p) {
                return p.paymentType == PaymentType.SALE;
            }
        });
    }

    private static ArrayList<PaymentTransactionModel> filter(ArrayList<PaymentTransactionModel> list, Predicate<PaymentTransactionModel> predicate) {
        if (list == null)
            return null;
        Iterator<PaymentTransactionModel> it = list.iterator();
        while (it.hasNext()) {
            PaymentTransactionModel m = it.next();
            if (!predicate.apply(m)) {
                it.remove();
            }
        }
        return list;
    }

    public static ArrayList<PaymentTransactionModel> loadByOrderOnlySale(Context context, String orderGuid) {
        Cursor c = ProviderAction.query(URI_TRANSACTIONS)
                .where("(" + PaymentTransactionTable.STATUS + " = ? OR " + PaymentTransactionTable.STATUS + " = ?)", PaymentStatus.PRE_AUTHORIZED.ordinal(), PaymentStatus.SUCCESS.ordinal())
                .where("(" + SaleOrderTable.GUID + " = ? or " + SaleOrderTable.PARENT_ID + " = ?)", orderGuid, orderGuid)
                .perform(context);

        return filterSaleOnly(readTransactions(c));
    }

    public static ArrayList<PaymentTransactionModel> loadByOrder(Context context, String orderGuid) {
        Cursor c = ProviderAction.query(URI_TRANSACTIONS)
                .where("(" + PaymentTransactionTable.STATUS + " = ? OR " + PaymentTransactionTable.STATUS + " = ?)", PaymentStatus.PRE_AUTHORIZED.ordinal(), PaymentStatus.SUCCESS.ordinal())
                .where("(" + SaleOrderTable.GUID + " = ? or " + SaleOrderTable.PARENT_ID + " = ?)", orderGuid, orderGuid)
                .perform(context);
        return readTransactions(c);
    }

    public static ArrayList<PaymentTransactionModel> loadByOrderSingle(Context context, String orderGuid) {
        Cursor c = ProviderAction.query(URI_TRANSACTIONS)
                .where("(" + PaymentTransactionTable.STATUS + " = ? OR " + PaymentTransactionTable.STATUS + " = ?)", PaymentStatus.PRE_AUTHORIZED.ordinal(), PaymentStatus.SUCCESS.ordinal())
                .where(SaleOrderTable.GUID + " = ?", orderGuid)
                .perform(context);
        return readTransactions(c);
    }

    public static ArrayList<PaymentTransactionModel> load(Context context, List<String> transactionsGuids) {
        Cursor c = ProviderAction.query(URI_TRANSACTIONS)
                .whereIn(PaymentTransactionTable.GUID, transactionsGuids)
                .perform(context);
        return readTransactions(c);
    }

    private static ArrayList<PaymentTransactionModel> readTransactions(Cursor c) {
        return readTransactions(c, true);
    }

    private static ArrayList<PaymentTransactionModel> readTransactions(Cursor c, boolean closeCursor) {
        HashMap<String, List<PaymentTransactionModel>> childrenTransactions = new HashMap<String, List<PaymentTransactionModel>>();

        ArrayList<PaymentTransactionModel> result = new ArrayList<PaymentTransactionModel>();
        ArrayList<PaymentTransactionModel> parents = new ArrayList<PaymentTransactionModel>();
        if (c.moveToFirst()) {
            do {
                PaymentTransactionModel model = new PaymentTransactionModel(
                        c.getString(c.getColumnIndex(PaymentTransactionTable.GUID)),
                        c.getString(c.getColumnIndex(PaymentTransactionTable.PARENT_GUID)),
                        c.getString(c.getColumnIndex(PaymentTransactionTable.ORDER_GUID)),
                        _decimal(c, c.getColumnIndex(PaymentTransactionTable.AMOUNT)),
                        _paymentType(c, c.getColumnIndex(PaymentTransactionTable.TYPE)),
                        _paymentStatus(c, c.getColumnIndex(PaymentTransactionTable.STATUS)),
                        c.getString(c.getColumnIndex(PaymentTransactionTable.OPERATOR_GUID)),
                        _paymentGateway(c, c.getColumnIndex(PaymentTransactionTable.GATEWAY)),
                        c.getString(c.getColumnIndex(PaymentTransactionTable.GATEWAY_PAYMENT_ID)),
                        c.getString(c.getColumnIndex(PaymentTransactionTable.GATEWAY_PREAUTH_PAYMENT_ID)),
                        c.getString(c.getColumnIndex(PaymentTransactionTable.GATEWAY_CLOSED_PERAUTH_GUID)),
                        c.getString(c.getColumnIndex(PaymentTransactionTable.DECLINE_REASON)),
                        new Date(c.getLong(c.getColumnIndex(PaymentTransactionTable.CREATE_TIME))),
                        c.getString(c.getColumnIndex(PaymentTransactionTable.SHIFT_GUID)),
                        c.getString(c.getColumnIndex(PaymentTransactionTable.CARD_NAME)),
                        _decimal(c, c.getColumnIndex(PaymentTransactionTable.CHANGE_AMOUNT)),
                        _bool(c, c.getColumnIndex(PaymentTransactionTable.IS_PREAUTH)),
                        _decimal(c, c.getColumnIndex(PaymentTransactionTable.CASH_BACK)),
                        _decimal(c, c.getColumnIndex(PaymentTransactionTable.BALANCE))
                );
                model.balance = _decimal(c, c.getColumnIndex(PaymentTransactionTable.BALANCE));
                String parentGuid = c.getString(c.getColumnIndex(PaymentTransactionTable.PARENT_GUID));
                if (parentGuid != null) {
                    List<PaymentTransactionModel> list = childrenTransactions.get(parentGuid);
                    if (list == null) {
                        childrenTransactions.put(parentGuid, list = new ArrayList<PaymentTransactionModel>());
                    }
                    list.add(model);
                } else {
                    parents.add(model);
                }
                result.add(model);
            } while (c.moveToNext());
        }
        if (closeCursor)
            c.close();
        for (PaymentTransactionModel m : parents) {
            m.availableAmount = m.amount.add(calcAmount(childrenTransactions.get(m.guid)));
        }
        return result;
    }

    /**
     * will return sum by children transaction. it will be negative, because refund transaction has negative biller
     *
     * @param children
     * @return
     */
    public static BigDecimal calcAmount(List<PaymentTransactionModel> children) {
        BigDecimal result = BigDecimal.ZERO;
        if (children == null)
            return result;
        for (PaymentTransactionModel m : children) {
            result = result.add(m.amount);
        }
        return result;
    }

    public static Loader<ArrayList<PaymentTransactionModel>> createLoaderOnlyOpenedPreauth(Context context, String orderGuid) {
        orderGuid = orderGuid == null ? "" : orderGuid;
        CursorLoaderBuilder builder = CursorLoaderBuilder.forUri(URI_TRANSACTIONS)
                .where("(" + PaymentTransactionTable.STATUS + " = ? OR " + PaymentTransactionTable.STATUS + " = ?)", PaymentStatus.PRE_AUTHORIZED.ordinal(), PaymentStatus.SUCCESS.ordinal())
                .where("(" + SaleOrderTable.GUID + " = ? or " + SaleOrderTable.PARENT_ID + " = ?)", orderGuid, orderGuid)
                .where(PaymentTransactionTable.IS_PREAUTH + " = 1");

        return builder.wrap(new Function<Cursor, ArrayList<PaymentTransactionModel>>() {
            @Override
            public ArrayList<PaymentTransactionModel> apply(Cursor c) {
                ArrayList<PaymentTransactionModel> result = readTransactions(c, false);
                result = filter(result, new Predicate<PaymentTransactionModel>() {
                    @Override
                    public boolean apply(PaymentTransactionModel p) {
                        return p.status == PaymentStatus.PRE_AUTHORIZED;
                    }
                });

                Collections.sort(result, new Comparator<PaymentTransactionModel>() {
                    @Override
                    public int compare(PaymentTransactionModel l, PaymentTransactionModel r) {
                        return l.createTime == null ? -1 : r.createTime == null ? 1 : l.createTime.compareTo(r.createTime);
                    }
                });

                return result;
            }
        })
                .build(context);
    }

}
