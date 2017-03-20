package com.kaching123.tcr.fragment.itempick;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CategoryTable;

@EFragment
public class CategoriesFragment extends ListFragment implements LoaderCallbacks<Cursor>{
	
	private static final Uri URI_CATEGORIES = ShopProvider.getContentUri(CategoryTable.URI_CONTENT);
	
	private ICategoryListener listener;

    private int pressedPos = -1;

	Loader<Cursor> cursorLoader;

    @Bean
    protected CategoriesAdapter adapter;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> v, View view, int pos, long id) {
                if (pressedPos >= 0){
                    getListView().getChildAt(pressedPos).setPressed(false);
                }
				categoryItemClicked(id, (Cursor)v.getItemAtPosition(pos));
                view.setPressed(true);
                pressedPos = pos;
			}
		});
        setListAdapter(adapter);
        getLoaderManager().initLoader(0, null, this);
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.itempick_categories_fragment, container, false);
    }

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
		Log.d("BemaCarl2","CategoriesFragment.onCreateLoader.loaderId: " + loaderId);
		Log.d("BemaCarl2","CategoriesFragment.onCreateLoader.args: " + args);
		cursorLoader = CursorLoaderBuilder.forUri(URI_CATEGORIES)
				.projection(CategoryTable.ID, CategoryTable.GUID, CategoryTable.TITLE)
				.orderBy(CategoryTable.TITLE).build(getActivity());
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		checkFirstRow(cursor);
		adapter.changeCursor(cursor);
	}

	private void checkFirstRow(Cursor cursor) {
		if(cursor == null || this.listener == null)
			return;
		if(cursor.moveToFirst()){
			this.listener.onCategoryChanged( 	
					cursor.getLong(cursor.getColumnIndex(CategoryTable.ID)),
					cursor.getString(cursor.getColumnIndex(CategoryTable.GUID))
			);
					
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.changeCursor(null);
	}
	
	protected void categoryItemClicked(long id, Cursor c){
		String guid = c.getString(c.getColumnIndex(CategoryTable.GUID));
		if(this.listener != null){
			this.listener.onCategoryChanged(id, guid);
		}

	}
	
	public void setListener(ICategoryListener listener) {
		this.listener = listener;
	}
	
	public static interface ICategoryListener{
		void onCategoryChanged(long id, String guid);
	}

}
