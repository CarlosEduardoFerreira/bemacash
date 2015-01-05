package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.model.payment.blackstone.prepaid.Broker;
import com.kaching123.tcr.util.JdbcJSONArray;
import com.kaching123.tcr.util.JdbcJSONObject;

import org.json.JSONException;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by vkompaniets on 26.05.2014.
 */
public class PrepaidTaxJdbcConverter {

    public static final String TABLE_NAME = "PREPAID_ITEM_TAX";

    private static final String CATEGORY = "CATEGORY";
    private static final String TAX = "TAX";

    public static HashMap<Broker, BigDecimal> read(ResultSet rs) throws SQLException {
        HashMap<Broker, BigDecimal> taxes = new HashMap<Broker, BigDecimal>();
        do {
            Broker category = _enum(Broker.class, rs.getString(CATEGORY), null);
            BigDecimal tax = rs.getBigDecimal(TAX);
            taxes.put(category, tax);
        } while(rs.next());

        return taxes;
    }

    public static HashMap<Broker, BigDecimal> read(JdbcJSONArray rs) throws JSONException {
        HashMap<Broker, BigDecimal> taxes = new HashMap<Broker, BigDecimal>();
        for (int i = 0; i < rs.length(); i++){
            JdbcJSONObject json = rs.getJSONObject(i);
            Broker category = _enum(Broker.class, json.getString(CATEGORY), null);
            BigDecimal tax = json.getBigDecimal(TAX);
            taxes.put(category, tax);
        }

        return taxes;
    }
}
