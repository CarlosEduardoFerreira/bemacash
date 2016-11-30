package com.kaching123.tcr.commands.rest.sync;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.rest.RestCommand.JsonResponse;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.EmployeeJdbcConverter;
import com.kaching123.tcr.jdbc.converters.RegisterJdbcConverter;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.RegisterModel;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;
import com.kaching123.tcr.store.ShopStore.RegisterTable;
import com.kaching123.tcr.util.JdbcJSONObject;

import java.math.BigDecimal;

/**
 * Created by gdubina on 19/03/14.
 */
public class AuthResponse extends JsonResponse {

    public static final String KEY_EMPLOYEE = "employee";
    public static final String KEY_REGISTER = "register";

    public AuthResponse(String status, String message, JdbcJSONObject entity) {
        super(status, message, entity);
    }


    public AuthInfo getResponse() {
        try {
            EmployeeJdbcConverter employeeJdbc = (EmployeeJdbcConverter) JdbcFactory.getConverter(EmployeeTable.TABLE_NAME);
            RegisterJdbcConverter registerJdbc = (RegisterJdbcConverter) JdbcFactory.getConverter(RegisterTable.TABLE_NAME);

            boolean key_register_is_null = KEY_REGISTER == null;
            boolean key_employee_is_null = KEY_EMPLOYEE == null;
            if(key_register_is_null || key_employee_is_null)
                return null;

            RegisterModel registerModel = registerJdbc.toValues( entity.getJSONObject(KEY_REGISTER) );

            JdbcJSONObject rs = entity.getJSONObject(KEY_EMPLOYEE);
            //Long id_long = rs.getLong("SHOP_ID");
            //Long id_string = rs.getLong("SHOP_ID");
            //if( id_long == 0 || id_string == null )
            //    return null;

            EmployeeModel employeeModel = employeeJdbc.toValues(rs);

            return new AuthInfo( key_register_is_null ? null : registerModel, key_employee_is_null ? null : employeeModel);

        } catch (Exception e) {
            Logger.e("AuthResponse error", e);
        }
        return null;
    }

    public static class AuthInfo {
        public final EmployeeModel employee;
        public final RegisterModel register;

        public AuthInfo(RegisterModel register, EmployeeModel employee) {
            this.register = register;
            this.employee = employee;
        }
    }
}
