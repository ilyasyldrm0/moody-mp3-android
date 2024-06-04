package com.broondle.mp3calar.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.broondle.mp3calar.Model.AudioModel;
import com.broondle.mp3calar.R;
import com.broondle.mp3calar.Util.Managers.FileManager;
import com.broondle.mp3calar.Util.Managers.FormatManager;
import com.broondle.mp3calar.Util.Managers.MediaPlayerManager;
import com.broondle.mp3calar.Util.Managers.MediaStoreManager;
import com.broondle.mp3calar.Util.Managers.ViewManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class listAdapter extends RecyclerView.Adapter<listAdapter.playListViewHolder> {

    public interface OnDeleteListener{
        void requestDeletePermission(IntentSenderRequest request);
    }

    private final WeakReference<Context> mContext;
    List<AudioModel> audioModels;
    DisplayMetrics dp;
    ViewManager viewUtil = ViewManager.shared();

    OnDeleteListener deleteListener;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class playListViewHolder extends RecyclerView.ViewHolder{

        public ImageView thumbnail;
        public TextView video_title,video_second,video_author;
        public ConstraintLayout video_view;
        public View thumbSuperView;
        public ImageButton moreButton;

        public playListViewHolder(View view) {

            super(view);

            //the video_item.xml file is now associated as view object
            //so the view can be called from view's object
            thumbnail = view.findViewById(R.id.thumbnailView);
            thumbSuperView = view.findViewById(R.id.thumbSuperView);
            video_second = view.findViewById(R.id.videoSeconds);
            video_author = view.findViewById(R.id.videoAuthor);
            video_title = view.findViewById(R.id.videoTitle);
            video_view = view.findViewById(R.id.constraintforitemvideo);
            moreButton = view.findViewById(R.id.moreButton);
        }
    }

    public listAdapter(Context mContext, List<AudioModel> playList,DisplayMetrics dp,OnDeleteListener deleteListener) {
        this.mContext = new WeakReference<>(mContext);
        this.audioModels = playList;
        this.dp = dp;
        this.deleteListener = deleteListener;
    }


    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public playListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listadapterlayout, parent, false);

        viewUtil.setConsLayParams(itemView,100,9,dp);

        return new playListViewHolder(itemView);
    }

    public void updateData(List<AudioModel> playList){
        this.audioModels = playList;
        notifyDataSetChanged();
    }

    // Replace the contents of a view (invoked by the layout manager)
    //filling every item of view with respective text and image

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull playListViewHolder holder, @SuppressLint("RecyclerView") int position) {

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        AudioModel audioModel = audioModels.get(position);

        //replace the default text with id, title and description with setText method
        if(audioModel.getName().length() > 26)
            holder.video_title.setText(audioModel.getName().substring(0,24));
        else
            holder.video_title.setText(audioModel.getName());

        //video info yerleştir
        holder.video_second.setText(FormatManager.shared().longToLenghtText(Integer.parseInt(audioModel.getLenghtSeconds())));
        holder.video_author.setText(audioModel.getArtist());

        //bitmap from audiomodel
        GradientDrawable gradientDrawable = (GradientDrawable) ContextCompat.getDrawable(mContext.get(),R.drawable.audiomodeldefault);

        if(gradientDrawable != null)
            gradientDrawable.setCornerRadius(dp.heightPixels/45f);

        holder.thumbSuperView.setBackground(gradientDrawable);
        holder.thumbnail.setImageDrawable(ContextCompat.getDrawable(mContext.get(),R.drawable.audiodefaulticon));

        // ViewUtil.setThumbFromURL(mContext,holder.thumbnail,audioModel.getThumb_url(),executorRunner); sonra eklenecek

        //onClick method called when the view is clicked
        holder.itemView.setOnClickListener(view -> {
            //play music

            MediaPlayerManager.shared(mContext.get()).getService().playSong(position,null);


        });


        holder.moreButton.setOnClickListener(view ->{
            Log.e("BottomSheetDialog ","Opened!");

            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mContext.get());

            View bottomSheetView = View.inflate(mContext.get(),R.layout.listadapterbottomsheet,null);

            viewUtil.setConsLayParams(bottomSheetView,100,10,dp);

            bottomSheetDialog.setContentView(bottomSheetView);

            TextView deleteText = bottomSheetDialog.findViewById(R.id.deleteItemText);

            if (deleteText != null) {
                deleteText.setOnClickListener(view53 -> {
                    if(MediaPlayerManager.shared(mContext.get()).getService().get_selectedmodel() != null ){
                        if(Objects.equals(MediaPlayerManager.shared(mContext.get()).getService().get_selectedmodel().getName(),audioModel.getName())){
                            ((Activity)mContext.get()).runOnUiThread(() -> {
                                Toast.makeText(mContext.get(), "You cant delete while playing song!", Toast.LENGTH_SHORT).show();
                            });
                            bottomSheetDialog.dismiss();
                            return;
                        }
                    }

                    // Start a new Thread for file deletion
                    new Thread(() -> {
                        File mp3File = new File(audioModel.getPath());
                        File thumbFile = new File(mContext.get().getFilesDir() +"/"+audioModel.getName()+".png");
                        if(mp3File.exists())
                            Log.e("silme işlemi","Dosya mevcut!");
                        else
                            Log.e("silme işlemi","Dosya mevcut değil!");

                        try{
                            boolean deleted = FileManager.shared().deleteActionAudioFile(mContext.get(),mp3File,deleteListener);

                            boolean deletedThumb = thumbFile.delete();
                            ((Activity)mContext.get()).runOnUiThread(bottomSheetDialog::dismiss);

                        } catch (Exception e) {
                            e.printStackTrace();
                            ((Activity)mContext.get()).runOnUiThread(() -> {
                                Toast.makeText(mContext.get(), "Error : "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
                            });
                        }
                    }).start();
                });
            }

            bottomSheetDialog.show();
        });
    }


    // Return the size of your dataset (invoked by the layout manager)
    //here the dataset is mVideoList
    @Override
    public int getItemCount() {
        return audioModels.size();
    }


}
