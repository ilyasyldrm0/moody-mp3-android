package com.broondle.mp3calar.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.broondle.mp3calar.Constants;
import com.broondle.mp3calar.Util.Managers.AdManager;
import com.broondle.mp3calar.Util.Managers.MediaStoreManager;
import com.broondle.mp3calar.Util.Network.ApiManager;
import com.broondle.mp3calar.Util.Network.ExecutorRunner;
import com.broondle.mp3calar.Model.AudioModel;
import com.broondle.mp3calar.Model.YoutubeDataModel;
import com.broondle.mp3calar.R;
import com.broondle.mp3calar.Service.DownloadService;
import com.broondle.mp3calar.Util.Managers.GenelManager;
import com.broondle.mp3calar.Util.Managers.JsonManager;

import java.util.List;
import java.util.function.Consumer;

public class youtubeListAdapter extends RecyclerView.Adapter<youtubeListAdapter.youtubeListViewHolder> {

    String HOST_ID = "youtube-search-and-download.p.rapidapi.com";

    Context context;
    List<YoutubeDataModel> youtubeDataModelList;
    DisplayMetrics dp;
    ExecutorRunner executorRunner = new ExecutorRunner();
    GenelManager genelUtil = GenelManager.shared();

    public youtubeListAdapter(Context mContext, List<YoutubeDataModel> list, DisplayMetrics dp) {
        this.context = mContext;
        this.youtubeDataModelList = list;
        this.dp = dp;

    }

    @NonNull
    @Override
    public youtubeListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //view oluştur
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.youtubelistadapterlayout, parent, false);
        int height = dp.heightPixels;
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) genelUtil.percentage(height,10.0));

        layoutParams.setMargins(0,0,0,10);
        itemView.setLayoutParams(layoutParams);

        return new youtubeListViewHolder(itemView);
    }

    public void updateDataSet(List<YoutubeDataModel> newDataSet){
        youtubeDataModelList = newDataSet;
        notifyDataSetChanged();
    }

    public void nextUpdate(List<YoutubeDataModel> newList) {
        int oldSize = youtubeDataModelList.size();
        youtubeDataModelList = newList;
        int itemsAdded = newList.size() - oldSize;
        notifyItemRangeInserted(oldSize, itemsAdded);
    }

    List<AudioModel> audioModels;
    @Override
    public void onBindViewHolder(@NonNull youtubeListViewHolder holder, int position) {
        //işlevler
        YoutubeDataModel youtubeDataModel = youtubeDataModelList.get(position);


        String videoDownloadLink = "https://youtube-search-and-download.p.rapidapi.com/video?id="+youtubeDataModel.getVideoID()+
                "&rapidapi-key="+ Constants.RAPID_API_KEY+"&rapidapi-host="+HOST_ID;

        holder.downloadImageButton.setOnClickListener(view ->{
            AdManager.shared().checkAds((Activity) context,() -> {
                //ad check closure
                if(!genelUtil.isMyServiceRunning(context,DownloadService.class)){

                    MediaStoreManager.shared().getAudioListLocal(context, new Consumer<List<AudioModel>>() {
                        @Override
                        public void accept(List<AudioModel> audioModelList) {
                            audioModels = audioModelList;
                        }
                    });

                    if(audioModels != null)
                        for (AudioModel audioModel : audioModels){
                            if(youtubeDataModel.getTitle().equals(audioModel.getName())){
                                Toast.makeText(context, "You've already downloaded this!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                    Intent downloadService = new Intent(context, DownloadService.class);
                    downloadService.putExtra("youtubeDataModel", JsonManager.shared().utubeDataToJson(youtubeDataModel));
                    context.startService(downloadService);

                }else
                    Toast.makeText(context, "You can download one item at same time!", Toast.LENGTH_SHORT).show();
            });
        });

        holder.video_title.setText(youtubeDataModel.getTitle());


        holder.video_views.setText(youtubeDataModel.getViews());

        holder.video_seconds.setText(youtubeDataModel.getLengthSeconds());

        // eğer curve eklenicekse imageview - holder.thumbnail.setClipToOutline(true)
        executorRunner.execute(new ApiManager.CallableThumbAPI(youtubeDataModel.getThumbnail()), new ExecutorRunner.Callback<Bitmap>() {
            @Override
            public void onComplete(Bitmap result) {
                if(result == null)
                    result = BitmapFactory.decodeResource(context.getResources(),R.drawable.audiodefaulticon);

                holder.thumbnail.setImageBitmap(result);

            }

            @Override
            public void onError(Exception e) {
                holder.thumbnail.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.audiomodeldefault));
            }
        });


    }


    @Override
    public int getItemCount() {
        return youtubeDataModelList.size();
    }

    public static class youtubeListViewHolder extends RecyclerView.ViewHolder{
        public ImageView thumbnail;
        public TextView video_title,video_views,video_seconds;
        public ConstraintLayout video_coinstraint;
        public ImageButton downloadImageButton;


        public youtubeListViewHolder(View view) {

            super(view);
            //atamalar
            downloadImageButton = view.findViewById(R.id.imageButtonDownload);
            thumbnail = view.findViewById(R.id.thumbnailView2);
            video_title = view.findViewById(R.id.videoTitle2);
            video_coinstraint = view.findViewById(R.id.constraintforitemvideoyoutube);
            video_views = view.findViewById(R.id.videoViewsText);
            video_seconds = view.findViewById(R.id.videoViewSecondsText);


            //the video_item.xml file is now associated as view object
            //so the view can be called from view's object
        }
    }

    //thumb api





}
