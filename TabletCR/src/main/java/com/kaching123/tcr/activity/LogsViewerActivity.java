package com.kaching123.tcr.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.FileManagerAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@EActivity(R.layout.logs_list)
public class LogsViewerActivity extends Activity {
    private FileManagerAdapter adapter;
    private ArrayList<Map<String, Object>> infos = null;

    @ViewById
    protected TextView logsPath;

    @ViewById
    protected View filePathLine;

    @ViewById
    protected ListView logsList;

    @AfterViews
    protected void init() {
        Bundle bundle = getIntent().getExtras();
        String path = bundle.getString("LogsPath");
        logsList.setOnItemClickListener(clickListener);
        initView(path);
    }

    private void initView(String initPath) {
        initList(initPath);
    }

    private void initList(String path) {
        File file = new File(path);
        File[] fileList = file.listFiles();
        infos = new ArrayList<Map<String, Object>>();
        Map<String, Object> item;

        logsPath.setText(file.getPath());
        logsPath.setVisibility(View.VISIBLE);
        filePathLine.setVisibility(View.VISIBLE);

        try {
            for (File i : fileList) {
                item = new HashMap<String, Object>();
                if (!i.getName().startsWith(".") && i.getName().endsWith(".log")) {
                    item.put("icon", R.drawable.file);
                    item.put("name", i.getName());
                    item.put("path", i.getAbsolutePath());
                    infos.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!infos.isEmpty()) {
            Collections.sort(infos, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> object1, Map<String, Object> object2) {
                    return ((String) object1.get("name")).toLowerCase().compareTo(((String) object2.get("name")).toLowerCase());
                }
            });
        }

        adapter = new FileManagerAdapter(this, this.getWindowManager().getDefaultDisplay().getHeight() / 10);
        adapter.setFileListInfo(infos);
        logsList.setAdapter(adapter);
    }

    private AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromFile(new File((String) (infos.get(position).get("path"))));
            intent.setDataAndType(uri, "text/plain");
            startActivity(intent);
        }
    };
}
