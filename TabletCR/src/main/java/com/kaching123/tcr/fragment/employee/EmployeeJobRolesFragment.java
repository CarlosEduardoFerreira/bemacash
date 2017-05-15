package com.kaching123.tcr.fragment.employee;

import android.widget.ListView;
import com.kaching123.tcr.R;
import com.kaching123.tcr.model.EmployeeModel;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by mboychenko on 5/11/2017.
 */
@EFragment(R.layout.employee_job_roles_fragment)
public class EmployeeJobRolesFragment extends EmployeeBaseFragment implements EmployeeView {

    @Override
    protected void setEmployee() {
    }

    @Override
    protected void setViews() {

    }

    @ViewById
    protected ListView jobRolesList;

    @Override
    public void collectDataToModel(EmployeeModel model) {

    }

    @Override
    public void setFieldsEnabled(boolean enabled) {

    }

    @Override
    public boolean validateView() {
        return true;
    }

    @Override
    public boolean hasChanges(EmployeeModel initModel) {
        return false;
    }

//    private class JobRolesAdapter extends ObjectsCursorAdapter<JobRoles> {
//
//        public JobRolesAdapter(Context context) {
//            super(context);
//        }
//
//        @Override
//        protected View newView(int position, ViewGroup parent) {
//            View convertView = LayoutInflater.from(getContext()).inflate(R.layout.job_role_list_item, parent, false);
//            assert convertView != null;
//
//            JobRolesAdapter.ViewHolder holder = new JobRolesAdapter.ViewHolder();
//            holder.name = (TextView) convertView.findViewById(R.id.name);
//            holder.login = (TextView) convertView.findViewById(R.id.login);
//            holder.email = (TextView) convertView.findViewById(R.id.email);
//
//            convertView.setTag(holder);
//            return convertView;
//        }
//
//        @Override
//        protected View bindView(View convertView, int position, EmployeeModel item) {
//            JobRolesAdapter.ViewHolder holder = (JobRolesAdapter.ViewHolder) convertView.getTag();
//            JobRole i = getItem(position);
//
//            if (i == null) {
//                return convertView;
//            }
//
//            holder.role.setText(i.fullName());
//            holder.hRate.setText(i.login);
//            holder.oRate.setText(i.email);
//
//            return convertView;
//        }
//
//        private class ViewHolder {
//            TextView role;
//            TextView hRate;
//            TextView oRate;
//        }
//    }
}
