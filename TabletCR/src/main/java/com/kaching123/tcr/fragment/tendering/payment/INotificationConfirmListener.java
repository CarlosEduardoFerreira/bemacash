package com.kaching123.tcr.fragment.tendering.payment;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */

public interface INotificationConfirmListener {

    void onRetry();

    void onCancel();

    void onConfirmed();

    void onReload(Object UIFragent);
}