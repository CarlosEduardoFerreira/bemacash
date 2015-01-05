package com.kaching123.tcr.service;

import java.math.BigDecimal;
import java.sql.Types;

/**
 * Created by pkabakov on 04/04/14.
 */
public class DecimalQueryParam extends QueryParam {

    private final int decimalScale;

    public DecimalQueryParam(BigDecimal value, int decimalScale) {
        super(Types.DECIMAL, value);
        this.decimalScale = decimalScale;
    }

    //TODO apiv2
    /*@Override
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_TYPE, type);
        jsonObject.put(JSON_OBJECT, ContentValuesUtil._decimal((BigDecimal) value, decimalScale));
        return jsonObject;
    }*/

}
