package com.kaching123.tcr.reports;

import com.kaching123.tcr.function.OrderTotalPriceCalculator.Handler2;

import java.util.Collection;

/**
 * Created by hamsterksu on 27.01.14.
 */
public interface SalesReportHandler<T extends IReportResult> extends Handler2 {
    Collection<T> getResult();
}
