package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.util.JdbcJSONArray;
import com.kaching123.tcr.util.JdbcJSONObject;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by pkabakov on 11.03.14.
 */
public class BarcodePrefixJdbcConverter {

    public static final String TABLE_NAME = "BARCODE_PREFIX";

    private static final String NAME = "NAME";
    private static final String CODE = "CODE";

    public static BarcodePrefixes read(ResultSet rs) throws SQLException {
        BarcodePrefixes barcodePrefixes = new BarcodePrefixes();
        do {

            PrefixName name = _enum(PrefixName.class, rs.getString(NAME), null);
            int code = rs.getInt(CODE);

            switch (name) {
                case CODE_10_D_ITEM:
                    barcodePrefixes.code10DItem = code;
                    break;
                case CODE_6_D_ITEM_4_D_PRICE:
                    barcodePrefixes.code6DItem4DPrice = code;
                    break;
                case CODE_5_D_ITEM_5_D_PRICE:
                    barcodePrefixes.code5DItem5DPrice = code;
                    break;
                case CODE_4_D_ITEM_6_D_PRICE:
                    barcodePrefixes.code4DItem6DPrice = code;
                    break;
                case CODE_3_D_ITEM_7_D_PRICE:
                    barcodePrefixes.code3DItem7DPrice = code;
                    break;
                case CODE_6_D_ITEM_4_D_WEIGHT_3_DEC:
                    barcodePrefixes.code6DItem4DWeight3Dec = code;
                    break;
                case CODE_6_D_ITEM_4_D_WEIGHT:
                    barcodePrefixes.code6DItem4DWeight = code;
                    break;
                case CODE_5_D_ITEM_5_D_WEIGHT_3_DEC:
                    barcodePrefixes.code5DItem5DWeight3Dec = code;
                    break;
                case CODE_5_D_ITEM_5_D_WEIGHT:
                    barcodePrefixes.code5DItem5DWeight = code;
                    break;
                case CODE_5_D_ITEM_5_D_WEIGHT_0_DEC:
                    barcodePrefixes.code5DItem5DWeight0Dec = code;
                    break;
            }
        } while (rs.next());

        return barcodePrefixes;
    }

    public static BarcodePrefixes read(JdbcJSONArray rs) throws JSONException {
        BarcodePrefixes barcodePrefixes = new BarcodePrefixes();
        for (int i = 0; i < rs.length(); i++) {
            JdbcJSONObject json = rs.getJSONObject(i);
            PrefixName name = _enum(PrefixName.class, json.getString(NAME), null);
            int code = json.getInt(CODE);

            switch (name) {
                case CODE_10_D_ITEM:
                    barcodePrefixes.code10DItem = code;
                    break;
                case CODE_6_D_ITEM_4_D_PRICE:
                    barcodePrefixes.code6DItem4DPrice = code;
                    break;
                case CODE_5_D_ITEM_5_D_PRICE:
                    barcodePrefixes.code5DItem5DPrice = code;
                    break;
                case CODE_4_D_ITEM_6_D_PRICE:
                    barcodePrefixes.code4DItem6DPrice = code;
                    break;
                case CODE_3_D_ITEM_7_D_PRICE:
                    barcodePrefixes.code3DItem7DPrice = code;
                    break;
                case CODE_6_D_ITEM_4_D_WEIGHT_3_DEC:
                    barcodePrefixes.code6DItem4DWeight3Dec = code;
                    break;
                case CODE_6_D_ITEM_4_D_WEIGHT:
                    barcodePrefixes.code6DItem4DWeight = code;
                    break;
                case CODE_5_D_ITEM_5_D_WEIGHT_3_DEC:
                    barcodePrefixes.code5DItem5DWeight3Dec = code;
                    break;
                case CODE_5_D_ITEM_5_D_WEIGHT:
                    barcodePrefixes.code5DItem5DWeight = code;
                    break;
                case CODE_5_D_ITEM_5_D_WEIGHT_0_DEC:
                    barcodePrefixes.code5DItem5DWeight0Dec = code;
                    break;
            }
        }
        return barcodePrefixes;
    }

    public static final class BarcodePrefixes {

        public int code10DItem;
        public int code6DItem4DPrice;
        public int code5DItem5DPrice;
        public int code4DItem6DPrice;
        public int code3DItem7DPrice;
        public int code6DItem4DWeight3Dec;
        public int code6DItem4DWeight;
        public int code5DItem5DWeight3Dec;
        public int code5DItem5DWeight;
        public int code5DItem5DWeight0Dec;

        public BarcodePrefixes() {

        }

        public BarcodePrefixes(int code10DItem, int code6DItem4DPrice, int code5DItem5DPrice, int code4DItem6DPrice, int code3DItem7DPrice, int code6DItem4DWeight3Dec, int code6DItem4DWeight, int code5DItem5DWeight3Dec, int code5DItem5DWeight, int code5DItem5DWeight0Dec) {
            this.code10DItem = code10DItem;
            this.code6DItem4DPrice = code6DItem4DPrice;
            this.code5DItem5DPrice = code5DItem5DPrice;
            this.code4DItem6DPrice = code4DItem6DPrice;
            this.code3DItem7DPrice = code3DItem7DPrice;
            this.code6DItem4DWeight3Dec = code6DItem4DWeight3Dec;
            this.code6DItem4DWeight = code6DItem4DWeight;
            this.code5DItem5DWeight3Dec = code5DItem5DWeight3Dec;
            this.code5DItem5DWeight = code5DItem5DWeight;
            this.code5DItem5DWeight0Dec = code5DItem5DWeight0Dec;
        }

    }

    public enum PrefixName {
        CODE_10_D_ITEM,
        CODE_6_D_ITEM_4_D_PRICE,
        CODE_5_D_ITEM_5_D_PRICE,
        CODE_4_D_ITEM_6_D_PRICE,
        CODE_3_D_ITEM_7_D_PRICE,
        CODE_6_D_ITEM_4_D_WEIGHT_3_DEC,
        CODE_6_D_ITEM_4_D_WEIGHT,
        CODE_5_D_ITEM_5_D_WEIGHT_3_DEC,
        CODE_5_D_ITEM_5_D_WEIGHT,
        CODE_5_D_ITEM_5_D_WEIGHT_0_DEC
    }

}
