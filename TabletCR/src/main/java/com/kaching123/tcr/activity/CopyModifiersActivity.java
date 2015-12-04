package com.kaching123.tcr.activity;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.editmodifiers.ModifiersCopyDialog;
import com.kaching123.tcr.fragment.editmodifiers.SearchFragment;
import com.kaching123.tcr.fragment.editmodifiers.SearchFragment.IItemListener;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.util.KeyboardUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OptionsMenu;

/**
 * Created by vkompaniets on 7/2/2015.
 */
@EActivity(R.layout.modifier_copy_activity)
@OptionsMenu(R.menu.modifier_copy_activity)
public class CopyModifiersActivity extends SuperBaseActivity {

    @Extra
    protected String itemName;

    @Extra
    protected String itemGuid;

    @FragmentById
    protected SearchFragment searchFragment;

    private MenuItem searchItem;

    @AfterViews
    protected void init(){
        setTitle(getString(R.string.modifiers_copy_activity_title, itemName));
        searchFragment.setListener(new IItemListener() {
            @Override
            public void onItemSelected(long id, ItemExModel model) {
                ModifiersCopyDialog.show(self(), "fromItem", model,new ModifiersCopyDialog.OnClosedListener() {
                    @Override
                    public void onDialogSuccessClosed() {
                        closeSearch();
                    }
                });
            }
        });

        searchFragment.setItemGuid(itemGuid);
        searchFragment.setSearchText("");
    }

    private void closeSearch() {
        if (searchItem == null)
            return;
        searchItem.collapseActionView();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        searchItem = menu.findItem(R.id.action_search);
        initSearchView();
        return super.onCreateOptionsMenu(menu);
    }

    private void initSearchView() {
        final SearchView searchView = (SearchView) searchItem.getActionView();
        assert searchView != null;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                KeyboardUtils.hideKeyboard(self(), searchView);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSearchFragment(newText);
                return true;
            }
        });
    }

    private void filterSearchFragment(String newText) {
        searchFragment.setSearchText(newText);
    }

    public static void start(Context context, String itemGuid, String itemName) {
        CopyModifiersActivity_.intent(context).itemGuid(itemGuid).itemName(itemName).start();
    }

}
