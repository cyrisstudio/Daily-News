package com.cyris.dailynews.AsyncTasks;

import androidx.appcompat.app.ActionBar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cyris.dailynews.Adapters.FlipAdapter;
import com.cyris.dailynews.Database.TaskEntity;
import com.cyris.dailynews.Adapters.SwipeAdapter;
import com.cyris.dailynews.MainActivity;
import com.cyris.dailynews.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DataFetching extends AsyncTask<String,Void,String> {
    ActionBar actionBar;
    String data;
    List<TaskEntity> newsList;
    HttpURLConnection urlConnection;
    SwipeAdapter swipeAdapter;
    FlipAdapter flipAdapter;
    View view;
    RecyclerView recyclerView;
    String databaseName;
    String type="flip";
    SwipeRefreshLayout swipeRefresh;
    RecyclerView.SmoothScroller smoothScroller;
    static String extraData;
    FetchingAsyncTask fetchingAsyncTask;
    FloatingActionButton fab;
    SnapHelper snapHelper;
    ImageView scrollToTop;


    public DataFetching( View view1, RecyclerView recyclerView1, String databaseName1) {
        this.recyclerView = recyclerView1;
        this.view = view1;
        this.databaseName = databaseName1;
        scrollToTop = view.findViewById(R.id.scrollToTop);

    }

    @Override
    protected String doInBackground(final String... strings) {

        try {
            URL url = new URL(strings[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            Scanner scan = new Scanner(in);
            scan.useDelimiter("\\A");
            if (scan.hasNext())
                return scan.next();
            else
                return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }



      return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

         smoothScroller = new LinearSmoothScroller(view.getContext()) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };


        if(swipeRefresh!=null)
        {
            Log.i("swiperefreshDone","done");
            swipeRefresh.setRefreshing(false);
        }



       // Log.i("checkS",s);
        if(s!=null&&isNetworkConnected())
        {
            data = s;
            try {
                jsonDataManupulation();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText(view.getContext(),"No Internet Connection",Toast.LENGTH_SHORT).show();
            dataFromDatabase();
        }
        ScrollToTop();
        data = "error";

    }




    private void dataFromDatabase() {

        if(type.equals("flip"))
        {


            if(swipeRefresh == null)
            {
                snapHelper = new PagerSnapHelper();
                snapHelper.attachToRecyclerView(recyclerView);
            }

             fetchingAsyncTask = new FetchingAsyncTask(recyclerView,view,databaseName,type);
            fetchingAsyncTask.execute();
        }
        else
        {
             fetchingAsyncTask = new FetchingAsyncTask(swipeAdapter,recyclerView,view,databaseName,type,actionBar);
            fetchingAsyncTask.execute();
        }


    }


    private void jsonDataManupulation() throws JSONException {
        JSONArray jsonArray= new JSONArray(data);
        newsList = new ArrayList<>();

        //    Execute1 execute1 = new Execute1();
        for(int i=0;i<jsonArray.length();i++)
        {
            JSONObject object = jsonArray.getJSONObject(i);
            if(object.getString("description").length()>200||!isNetworkConnected())
            {
                TaskEntity entity = new TaskEntity();
                entity.description = object.getString("description");
                entity.url = object.getString("url");
                entity.urlToImage = object.getString("urlToImage");
                entity.publishedAt = object.getString("publishedAt");
                entity.title = object.getString("title");
                entity.source = object.getString("name");
                Log.i("helloadslj",entity.title);
                newsList.add(entity);
            }

        }
        StorageAsyncTask task = new StorageAsyncTask(view,databaseName);
        task.execute(newsList);
        if(type.equals("flip"))
        {



           // flipAdapter = new FlipAdapter(newsList,view.getContext());
            //recyclerView.setAdapter(flipAdapter);
            if(swipeRefresh == null)
            {
                snapHelper = new PagerSnapHelper();
                snapHelper.attachToRecyclerView(recyclerView);
            }

            fetchingAsyncTask = new FetchingAsyncTask(recyclerView,view,databaseName,type);
            fetchingAsyncTask.execute();


          //  flipAdapter.notifyDataSetChanged();
            if(extraData!=null)
                moveToPosition();
        }
        else
        {
            swipeAdapter = new SwipeAdapter(newsList,view.getContext());
            recyclerView.setAdapter(swipeAdapter);
            swipeAdapter.notifyDataSetChanged();
        }


    }











    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) view.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void RefreshData()
    {
        if(fetchingAsyncTask!=null)
            fetchingAsyncTask.refreshData();
    }

    public void refreshLoading(SwipeRefreshLayout layout1)
    {
        this.swipeRefresh = layout1;
    }

    public void setExtraLink(String s)
    {
        extraData = s;
    }

    public void moveToPosition()
    {
       fetchingAsyncTask.ExtraData(extraData);
    }

    public void ScrollToTop()
    {
        scrollToTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recyclerView != null) {

                           // recyclerView.scrollToPosition(0);
                            smoothScroller.setTargetPosition(0);
                            recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);



                }
            }
        });
    }


}
