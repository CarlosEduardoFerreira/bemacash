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
        EmployeeJdbcConverter employeeJdbc = (EmployeeJdbcConverter) JdbcFactory.getConverter(EmployeeTable.TABLE_NAME);
        RegisterJdbcConverter registerJdbc = (RegisterJdbcConverter) JdbcFactory.getConverter(RegisterTable.TABLE_NAME);
        try {
            EmployeeModel employee = entity.isNull(KEY_EMPLOYEE) ? null : employeeJdbc.toValues(entity.getJSONObject(KEY_EMPLOYEE));

            if(employee!=null) {
                employee.setInventoryItemCount(new BigDecimal(entity.getJSONObject(KEY_EMPLOYEE).getString("ITEMS_COUNT")));
            }

            return new AuthInfo(
                    entity.isNull(KEY_REGISTER) ? null : registerJdbc.toValues(entity.getJSONObject(KEY_REGISTER)),employee);

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
