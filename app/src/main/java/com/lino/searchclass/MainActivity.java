package com.lino.searchclass;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    ArrayAdapter<String> arrayAdapter;
    BufferedReader reader;
    String search = ".";
    String TAG = "searchclass";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = new Toolbar(this);
        toolbar.setTitle("Search Class");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setBackgroundResource(R.color.colorPrimary);
//        toolbar.setBackgroundColor(Color.parseColor("#3F51B5"));
//        ViewCompat.setElevation(toolbar, dip2px(5));
        setSupportActionBar(toolbar);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(1);
        layout.addView(toolbar);
        ListView listView = new ListView(this);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int currentVisibleItemCount;
            private int currentScrollState;
            private int currentFirstVisibleItem;
            private int totalItem;
            private LinearLayout lBelow;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
                this.currentScrollState = scrollState;
                this.isScrollCompleted();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                // TODO Auto-generated method stub
                this.currentFirstVisibleItem = firstVisibleItem;
                this.currentVisibleItemCount = visibleItemCount;
                this.totalItem = totalItemCount;
            }

            private void isScrollCompleted() {
                if (totalItem - currentFirstVisibleItem == currentVisibleItemCount
                        && this.currentScrollState == SCROLL_STATE_IDLE) {
                    getItems(search, false);
                }
            }
        });
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        getItems(search, true);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("class", (String)parent.getItemAtPosition(position));
                assert clipboard != null;
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "복사 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(listView);
        setContentView(layout);
    }

    private void getItems(final String str, boolean isFirst) {
        final int[] count = {0};
        try {
            if (isFirst)
                reader = new BufferedReader(new InputStreamReader(getAssets().open("linedAllClass.txt")));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        String line = "";
                        try {
                            if (!((line = reader.readLine()) != null && count[0] != 20)) break;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                            //someString.matches("stores.*store.*product.*");
                        if (!Pattern.compile(".*?"+str+".*?", Pattern.CASE_INSENSITIVE).matcher(line).matches()) {
                            continue;
                        }
                        Log.i(TAG, "run: "+ line);
                        synchronized (this) {
                            final String finalLine = line;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    arrayAdapter.add(finalLine);
                                    arrayAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                        count[0]++;
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private int dip2px(int dips) {
        return (int) Math.ceil(dips * this.getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_view, menu);
        MenuItem searchViewItem = menu.findItem(R.id.action_search);
        final SearchView searchViewAndroidActionBar = (SearchView) MenuItemCompat.getActionView(searchViewItem);

        searchViewAndroidActionBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search = query;
                arrayAdapter.clear();
                getItems(search, true);
                searchViewAndroidActionBar.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                if(newText.length() > 5){
//                    search = newText;
//                    arrayAdapter.clear();
//                    arrayAdapter.addAll(getItems(search, true));
//                    arrayAdapter.notifyDataSetChanged();
//                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

}
