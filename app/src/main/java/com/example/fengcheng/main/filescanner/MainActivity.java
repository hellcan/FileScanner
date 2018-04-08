package com.example.fengcheng.main.filescanner;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    private Button startBtn;
    MyAsyncTask myAsyncTask;
    static List<FileInfo> fileInfoList;
    long pgsCount = 0;
    private ListView resultLv;
    private TextView titleTv, freTv;
    private boolean cancelFlag = false;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileInfoList = new ArrayList<>();

        //find UI component here
        initView();

        //this is click listener
        clickListener();

    }


    private void initView() {
        Log.i(TAG, "initView: ");
        // start button
        startBtn = findViewById(R.id.start_btn);
        // result list view
        resultLv = findViewById(R.id.result_lv);
        // file scan textview
        titleTv = findViewById(R.id.file_scan_tv);
        // frequency textview
        freTv = findViewById(R.id.fre_tv);
    }

    private void clickListener() {
        Log.i(TAG, "clickListener: ");
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileInfoList != null) {
                    //clear list in case if there is something left before
                    fileInfoList.clear();
                }
                //start a asyncTas to execute file scan mission
                myAsyncTask = new MyAsyncTask();
                myAsyncTask.execute();
            }
        });
    }

    //this is asyncTask
    private class MyAsyncTask extends AsyncTask<Void, Integer, Integer> {
        ProgressDialog progressDialog;

        @Override
        protected Integer doInBackground(Void... voids) {
            Log.i(TAG, "doInBackground: ");
            //start scan files
            scan();

            return null;
        }

        //something need do after do in background
        @Override
        protected void onPostExecute(Integer result) {
            Log.i(TAG, "onPostExecute: ");
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();

                //Sort file list by
                Collections.sort(fileInfoList, new MyComparator());
                //Set listView showing Names and sizes of 10 biggest files
                MyListViewAdapter myListViewAdapter = new MyListViewAdapter(MainActivity.this, fileInfoList);
                //bind data adapter to listView
                resultLv.setAdapter(myListViewAdapter);

                //Convert Byte to KB
                String formatAve;
                //if total size != 0
                if (fileInfoList.size() != 0) {
                    formatAve = "Average size: " + pgsCount / fileInfoList.size() / 1024 + "KB";
                } else {//else just make 0
                    formatAve = "Average size: 0KB";
                }
                //set up file average size
                titleTv.setText(formatAve);

                //find all the extension frequency
                List<Map.Entry<String, Integer>> resultList = findFrequentExt();
                StringBuilder sb = new StringBuilder();
                sb.append("5 Most Frequency Extensions" + "\n");
                // if frequency list size > 5,
                if (resultList.size() > 5){
                    //only take first 5 elements
                    for (int i = 0; i < 5; i++){
                        sb.append(resultList.get(i).getKey()).append(": ").append(resultList.get(i).getValue()).append("\n");
                    }
                }else {//else take all
                    for (int i = 0; i < resultList.size(); i++){
                        sb.append(resultList.get(i).getKey()).append(": ").append(resultList.get(i).getValue()).append("\n");
                    }
                }
                //display result list
                freTv.setText(sb);
            }
        }

        //it will execute before do in background
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute: ");
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this, R.style.Base_AlertDialog);
            progressDialog.setButton(getString(R.string.stop_btn), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cancelScan();
                }
            });

            //listen back button clicked
            progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK){
                        //cancel scan mission
                        cancelScan();
                    }
                    return false;
                }
            });
            //set progress Dialog message
            progressDialog.setMessage(getString(R.string.scan_msg));
            //show progressDialog
            progressDialog.show();
            //rest cancel flag and total length
            cancelFlag = false;
            pgsCount = 0;
        }

        void scan() {
            Log.i(TAG, "scan: ");
            //check if we already grant the permission to read external storage
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                //this is external storage path
                String sdPath = Environment.getExternalStorageDirectory().getPath();
                startSearch(sdPath);
            }
        }

        void startSearch(String path) {
            //check flag if we start scan
            if (!cancelFlag) {
                Log.i("scanPath", path);
                //create file instance depend on file
                File sdFile = new File(path);
                //if sdFile is a folder
                if (sdFile.isDirectory()) {
                    //list all the single files under sdFile(folder)
                    if (sdFile.listFiles() != null) {
                        //loop all the file under sdFile(folder)
                        for (File childFile : sdFile.listFiles()) {
                            try {
                                //nothing
                                Thread.sleep(10);
                                //check if there are any folder inside sdFile(Folder)
                                if (childFile.isDirectory()) {
                                    //use recursive to search all the files until we find a folder that contain no folder inside
                                    startSearch(childFile.getPath());
                                } else {//use list to save this single file with its name and size
                                    fileInfoList.add(new FileInfo(childFile.getName(), childFile.length()));
                                    //save file length
                                    pgsCount = pgsCount + childFile.length();
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    //listen to back button click
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cancelScan();
    }

    //cancel scan mission
    public void cancelScan() {
        Log.i(TAG, "cancelScan: ");
        //this is asyncTask flag
        myAsyncTask.cancel(true);
        //set cancel flag to true
        cancelFlag = true;
        Toast.makeText(MainActivity.this, "Scan Cancelled", Toast.LENGTH_SHORT).show();

    }

    //count the extension frequency and return a list with descending order by value(frequency)
    public List<Map.Entry<String, Integer>> findFrequentExt(){
        Log.i(TAG, "findFrequentExt: ");
        Map<String, Integer> resultMap = new TreeMap<>();
        for (FileInfo fileInfo : fileInfoList){
            String key = fileInfo.getFileName().substring(fileInfo.getFileName().lastIndexOf(".") + 1, fileInfo.getFileName().length());
                if (resultMap.containsKey(key)){
                resultMap.put(key, resultMap.get(key) + 1);
            }else{
                resultMap.put(key, 1);
            }
        }

        //this is the result map comparator.
        Comparator<Map.Entry<String, Integer>> valueComparator = new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                //sort map by value with descending order
                return o2.getValue() - o1.getValue();
            }
        };

        //convert resultMap to list
        List<Map.Entry<String, Integer>> resList = new ArrayList<>(resultMap.entrySet());
        //sort map entry set with
        Collections.sort(resList, valueComparator);

        return resList;
    }
}
