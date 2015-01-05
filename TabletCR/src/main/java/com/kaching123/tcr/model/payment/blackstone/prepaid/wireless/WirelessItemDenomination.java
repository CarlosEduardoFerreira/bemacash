package com.kaching123.tcr.model.payment.blackstone.prepaid.wireless;

import com.kaching123.tcr.fragment.UiHelper;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class WirelessItemDenomination implements Serializable{
    public BigDecimal min;
    public BigDecimal max;
    public BigDecimal denomination;

    @Override
    public String toString() {
        return denomination == null || denomination.compareTo(BigDecimal.ZERO) <= 0
                ? String.format("%s - %s", UiHelper.valueOf(min), UiHelper.valueOf(max))
                : UiHelper.valueOf(denomination);
    }
}
