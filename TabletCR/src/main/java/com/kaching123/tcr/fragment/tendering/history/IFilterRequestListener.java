package com.kaching123.tcr.fragment.tendering.history;

import com.kaching123.tcr.model.SaleOrderTipsViewModel.TransactionsState;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by hamsterksu on 11.03.14.
 */
public interface IFilterRequestListener {

    void onFilterRequested(Date from, Date to, String cashierGUID, String customerGUID,
                           TransactionsState transactionsState, ArrayList<String> registerTitle, ArrayList<String> seqNum, String unitSerial, boolean isManual, boolean forceServerSearch);

    void onSearchOrderByUnitFailed(String serialCode);

    void onSearchOrderByUnitOnServer(String serialCode);
}