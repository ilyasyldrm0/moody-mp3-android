package com.broondle.mp3calar.ui;


import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.broondle.mp3calar.Adapters.youtubeListAdapter;
import com.broondle.mp3calar.Util.Network.ExecutorRunner;
import com.broondle.mp3calar.Model.YoutubeDataModel;
import com.broondle.mp3calar.Util.Network.ApiManager;
import com.broondle.mp3calar.databinding.FragmentSearchBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    ExecutorRunner executorRunner = new ExecutorRunner();
    private FragmentSearchBinding binding;
    SearchView searchView;
    RecyclerView recyclerViewOnline;
    List<YoutubeDataModel> youtubeModelList = new ArrayList<>();
    youtubeListAdapter youtubeListAdapter;
    Handler searchHandler = new Handler();
    ProgressBar progressBar;
    TextView textView;

    final String RAPID_API_KEY = "0252421ca0mshb8ff5ba6f8e6ecbp11e693jsn690c13ab97ad";
    final String HOST_ID = "simple-youtube-search.p.rapidapi.com";
    private String currentQuery = "";
    String NEXT_STRING = "";
    private boolean isLoading = false;
    private int loadCounter = 0;

    LinearLayoutManager linearLayoutManager;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressBar = binding.progressBarOnline;
        searchView = binding.searchOnlineView;
        textView = binding.textHome;
        recyclerViewOnline = binding.recyclerViewOnline;

        searchSet();

        DisplayMetrics dp = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dp);
        youtubeModelList = new ArrayList<YoutubeDataModel>();
        youtubeListAdapter = new youtubeListAdapter(requireActivity(),youtubeModelList,dp);
        recyclerViewOnline.setAdapter(youtubeListAdapter);
        linearLayoutManager = new LinearLayoutManager(requireActivity());
        recyclerViewOnline.setLayoutManager(linearLayoutManager);

        recyclerViewInit(false);

        return root;
    }

    private void searchSet(){

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.e("Search text ","submitted!");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.e("Search text ","changed!");
                    searchHandler.removeCallbacksAndMessages(null);
                    progressBar.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.GONE);

                    searchHandler.postDelayed(() -> {
                        currentQuery = s;
                        recyclerViewSet();
                        progressBar.setVisibility(View.GONE);
                    },1000);
                return false;
            }
        });
    }

    private void recyclerViewSet(){
        //search update
        if(currentQuery.isEmpty()){
            youtubeModelList.clear();
            recyclerViewInit(true);
            recyclerViewOnline.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        }else{
            textView.setVisibility(View.GONE);
            recyclerViewOnline.setVisibility(View.VISIBLE);
            fetchYoutubeData(false);
        }


    }

    private void recyclerViewInit(boolean isVisible){


        int isVisibleInt = isVisible ? View.VISIBLE : View.GONE;
        recyclerViewOnline.setVisibility(isVisibleInt);

        recyclerViewOnline.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!isLoading && linearLayoutManager.findLastCompletelyVisibleItemPosition() == youtubeModelList.size() - 1) {
                    isLoading = true;
                    fetchYoutubeData(true);
                }
            }
        });

    }

    private void fetchYoutubeData(boolean isLoadMore) {
        if(isLoadMore && loadCounter> 9) return;

        if(!isLoadMore) loadCounter = 0;

        progressBar.setVisibility(View.VISIBLE);
        String apiUrl = isLoadMore ? getNextPageUrl() : getInitialPageUrl(currentQuery);

        executorRunner.execute(new ApiManager.CallableReadJsonOnlineAPI(apiUrl), new ExecutorRunner.Callback<String>() {
            @Override
            public void onComplete(String result) {
                // Veri işleme ve liste güncelleme
                if(!isLoadMore)
                    youtubeModelList.clear();

                try {
                    if(TextUtils.isEmpty(result)){
                        Toast.makeText(requireActivity(), "No connection!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JSONObject jsonObjectVideos = new JSONObject(result);
                    JSONArray jsonArray = jsonObjectVideos.getJSONArray("contents");
                    NEXT_STRING = jsonObjectVideos.getString("next");


                    int size = jsonArray.length();

                    for (int i =0;i<size;i++){
                        String description = "";
                        String publishedAt = "";
                        String thumbnail = "";
                        String videoID = "";
                        String title = "";
                        String views = "";
                        String author = "";
                        String lenghtText = "";
                        //jsondan getir
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        if(jsonObject.has("video")){
                            JSONObject videoInfo = jsonObject.getJSONObject("video");


                            if (videoInfo.has("description"))
                                description = videoInfo.getString("description");
                            if(videoInfo.has("publishedTimeText"))
                                publishedAt = videoInfo.getString("publishedTimeText");
                            if(videoInfo.has("thumbnails"))
                                thumbnail = videoInfo.getJSONArray("thumbnails").getJSONObject(0).getString("url");
                            if(videoInfo.has("videoId"))
                                videoID = videoInfo.getString("videoId");
                            if(videoInfo.has("title"))
                                title = videoInfo.getString("title");
                            if(videoInfo.has("viewCountText"))
                                views = videoInfo.getString("viewCountText");
                            if(videoInfo.has("channelName"))
                                author = videoInfo.getString("channelName");
                            if(videoInfo.has("lengthText"))
                                lenghtText = videoInfo.getString("lengthText");

                        }


                        //data model atama ve kaydetme
                        YoutubeDataModel dataModel = new YoutubeDataModel(title,description,publishedAt,thumbnail,videoID,views,author,lenghtText);
                        youtubeModelList.add(dataModel);
                    }

                    if(isLoadMore){
                        youtubeListAdapter.nextUpdate(youtubeModelList);
                        loadCounter++;
                    }
                    else youtubeListAdapter.updateDataSet(youtubeModelList);
                    progressBar.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                isLoading = false;
            }

            @Override
            public void onError(Exception e) {
                isLoading = false;
                Toast.makeText(requireActivity(), "Search error : "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }


    private String getInitialPageUrl(String query) {
        return "https://youtube-search-and-download.p.rapidapi.com/search?query="+ query +"&type=v"+"&sort=r"+
                "&rapidapi-key="+RAPID_API_KEY+"&rapidapi-host="+HOST_ID;
    }

    private String getNextPageUrl() {
        return "https://youtube-search-and-download.p.rapidapi.com/search?next="+ NEXT_STRING +"&type=v"+"&sort=r"+
                "&rapidapi-key="+RAPID_API_KEY+"&rapidapi-host="+HOST_ID;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}