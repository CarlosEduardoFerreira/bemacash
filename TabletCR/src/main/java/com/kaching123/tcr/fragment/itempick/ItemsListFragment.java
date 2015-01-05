package com.kaching123.tcr.fragment.itempick;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.catalog.BaseItemsPickFragment;
import com.kaching123.tcr.model.ItemExModel;

@EFragment(R.layout.itempick_items_list_fragment)
public class ItemsListFragment extends BaseItemsPickFragment {

    @ViewById(android.R.id.list)
    protected ListView listView;

    @Override
    protected ObjectsCursorAdapter<ItemExModel> createAdapter() {
        ItemPickAdapter adapter = new ItemPickAdapter(getActivity());
        listView.setAdapter(adapter);
        return adapter;
    }

    @Override
    public void setListener(final IItemListener listener) {
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener == null) {
                    return;
                }
                ItemExModel model = (ItemExModel) parent.getItemAtPosition(position);
                assert model != null;
                listener.onItemSelected(id, model);
            }
        });
    }
}
