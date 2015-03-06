package com.kaching123.tcr.fragment.filemanager;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.io.Files;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by gdubina on 14/01/14.
 */
@EFragment
public class FileChooserFragment extends StyledDialogFragment {

    public static enum Type{FILE, FOLDER};

    private static final String CSV = "csv";
    private static final String DIALOG_NAME = "FILE_CHOOSER";
    public static final String FILE_UP = "..";

    @ViewById
    protected GridView filesGrid;

    private File curFile;
    private FileAdapter adapter;
    private FileChooseListener fileChooseListener;

    @FragmentArg
    protected Type type;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = getResources().getDimensionPixelOffset(R.dimen.filemanager_width);
        params.height = getResources().getDimensionPixelOffset(R.dimen.filemanager_height);

        curFile = Environment.getExternalStorageDirectory();
        filesGrid.setAdapter(adapter = new FileAdapter(getActivity()));
        filesGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = adapter.getItem(position);
                if(file == null){
                    return;
                }
                curFile = file;
                if(curFile.isDirectory()){
                    new LoadFileList().execute();
                }else if(fileChooseListener != null){
                    fileChooseListener.fileChosen(curFile);
                    dismiss();
                }
            }
        });
        new LoadFileList().execute();

    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.filemanager_chooser_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.file_chooser_open;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return type == Type.FOLDER ? R.string.btn_save : 0;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return type == Type.FOLDER ?
                new OnDialogClickListener(){
                    @Override
                    public boolean onClick() {
                        if(fileChooseListener != null){
                            fileChooseListener.fileChosen(curFile);
                        }
                        return true;
                    }
                }
                :  null;
    }

    @Override
    protected boolean hasPositiveButton() {
        return type == Type.FOLDER;
    }

    public void setFileChooseListener(FileChooseListener fileChooseListener) {
        this.fileChooseListener = fileChooseListener;
    }

    private class FileAdapter extends ObjectsCursorAdapter<File> {

        public FileAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View v = View.inflate(getContext(), R.layout.filemanager_chooser_item, null);
            v.setTag(new UiHolder(
                    (ImageView)v.findViewById(R.id.icon),
                    (TextView)v.findViewById(R.id.title))
            );

            return v;
        }

        @Override
        protected View bindView(View convertView, int position, File item) {
            UiHolder holder = (UiHolder)convertView.getTag();
            if(position == 0){
                holder.icon.setImageLevel(2);
                holder.title.setText(FILE_UP);
            }else{
                holder.icon.setImageLevel(item.isDirectory() ? 0 : 1);
                holder.title.setText(item.getName());
            }
            return convertView;
        }

        @Override
        public File getItem(int position) {
            if(position == 0)
                return curFile.getParentFile();
            return super.getItem(position - 1);
        }

        @Override
        public int getCount() {
            return super.getCount() + 1;
        }
    }

    private static class UiHolder{
        final ImageView icon;
        final TextView title;

        private UiHolder(ImageView icon, TextView title) {
            this.icon = icon;
            this.title = title;
        }
    }

    private class LoadFileList extends AsyncTask<Void, Void, List<File>>{


        @Override
        protected List<File> doInBackground(Void... params) {
            if(!curFile.isDirectory())
                return null;
            File[] files = curFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory() || CSV.equals(Files.getFileExtension(pathname.getPath()));
                }
            });
            if(files == null)
                return new ArrayList<File>(0);
            Arrays.sort(files, fileNameComparator);
            return Arrays.asList(files);
        }

        @Override
        protected void onPostExecute(List<File> files) {
            adapter.changeCursor(files);
        }
    }

    private Comparator<File> fileNameComparator = new Comparator<File>() {
        @Override
        public int compare(File l, File r) {
            return l.getName().compareToIgnoreCase(r.getName());
        }
    };

    public static void show(FragmentActivity context, Type type, FileChooseListener listener){
        DialogUtil.show(context, DIALOG_NAME, FileChooserFragment_.builder().type(type).build()).setFileChooseListener(listener);
    }

    public static interface FileChooseListener{
        void fileChosen(File file);
    }
}
