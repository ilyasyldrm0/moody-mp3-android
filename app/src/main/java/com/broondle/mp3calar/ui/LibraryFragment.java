package com.broondle.mp3calar.ui;

import static android.app.Activity.RESULT_OK;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.broondle.mp3calar.Adapters.listAdapter;
import com.broondle.mp3calar.Util.Managers.FileManager;
import com.broondle.mp3calar.Util.Managers.ThreadManager;
import com.broondle.mp3calar.Util.Network.ExecutorRunner;
import com.broondle.mp3calar.Model.AudioModel;
import com.broondle.mp3calar.Model.YoutubeDataModel;
import com.broondle.mp3calar.Service.DownloadService;
import com.broondle.mp3calar.Util.Managers.MediaStoreManager;
import com.broondle.mp3calar.Util.Network.ApiManager;
import com.broondle.mp3calar.Util.Managers.GenelManager;
import com.broondle.mp3calar.databinding.FragmentLibraryBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LibraryFragment extends Fragment implements listAdapter.OnDeleteListener {

    private FragmentLibraryBinding binding;

    List<AudioModel> fileList = new ArrayList<>();



    RecyclerView recyclerView;
    TextView textNoDownloaded;
    SwipeRefreshLayout swipeRefreshLayout;
    listAdapter adapter;
    View downloadingView;
    TextView downloadText;
    ImageView downloadImage;
    Intent servisIntent;
    DownloadService downloadService;
    ProgressBar downloadingProgress;
    ImageButton cancelDownloadButton;

    boolean bound = false;

    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e("DownloadService ","Binded!");
            DownloadService.DownloadBinder downloadBinder = (DownloadService.DownloadBinder) service;
            downloadService = downloadBinder.getService();
            bound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            downloadService = null;
            bound = false;
        }
    };


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        servisIntent = new Intent(requireActivity(),DownloadService.class);


        textNoDownloaded = binding.textnoDownloaded;
        cancelDownloadButton = binding.cancelDownloadButton;

        downloadingView = binding.downloadingFileView;
        downloadText = binding.downladingTitle;
        downloadImage = binding.downloadingImage;
        downloadingProgress = binding.downloadProgress;

        recyclerView = binding.recyclerViewLib;
        swipeRefreshLayout = binding.swiperRefreshLib;
        swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
        swipeRefreshLayout.setRefreshing(true);

        DisplayMetrics dp = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dp);

        adapter = new listAdapter(requireActivity(), fileList,dp,this);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        onRefresh();

        downloadCheck();

        if(cancelDownloadButton.getVisibility() == View.VISIBLE)
            cancelDownloadButton.setOnClickListener(view -> downFinishedOrCancelled());

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }

    Handler serviceCheckHandler = new Handler();
    Runnable serviceProgressRunnable = new Runnable() {
        @Override
        public void run() {

            if (!isAdded()){
                return;
            }

            if (GenelManager.shared().isMyServiceRunning(requireActivity(),DownloadService.class)){
                int percent = downloadService.getProgressOfDownload();
                Log.e("Progress ",String.valueOf(percent));
                downloadingProgress.setProgress(percent);

                if(percent == 100){
                    //dosya indilirdiğinde ya da servis durunca
                    downFinishedOrCancelled();
                }else{
                    serviceCheckHandler.postDelayed(this,1000);
                }
            }else{
                downFinishedOrCancelled();
            }
            Log.e("Handler-Progress","Çalışıyor!");
        }
    };

    private void downFinishedOrCancelled(){
        //dosya indilirdiğinde
        if(bound){
            Log.e("bound ","Stopped -test!");
            requireActivity().unbindService(mConnection);

            try {
                requireActivity().stopService(servisIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        serviceCheckHandler.removeCallbacksAndMessages(null);
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();
        downloadingView.setVisibility(View.GONE);
    }


    private void downloadCheck(){
        if(GenelManager.shared().isDownloadServiceRunning(requireActivity()) && !bound){
            requireActivity().bindService(servisIntent,mConnection,0);

            Log.e("LibraryFragment ","Servis running!");
            downloadingView.setVisibility(View.VISIBLE);



            serviceCheckHandler.postDelayed(() -> {

                try{
                    YoutubeDataModel downloadingModel = downloadService.getDownloadingModel();

                    downloadText.setText(downloadingModel.getTitle());

                    new ExecutorRunner().execute(new ApiManager.CallableThumbAPI(downloadingModel.getThumbnail()),
                            new ExecutorRunner.Callback<Bitmap>() {
                                @Override
                                public void onComplete(Bitmap result) {
                                    downloadImage.setImageBitmap(result);
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("Error loading downloading file ","Failed to get thumbnail!");
                                }
                            });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            },550);


            //downloading progress bar
            serviceCheckHandler.postDelayed(serviceProgressRunnable,500);

        }else{
            Log.e("LibraryFragment ","Servis not running!");
            if(downloadingView.getVisibility() == View.VISIBLE){
                downloadingView.setVisibility(View.GONE);
            }
        }
    }

    private void onRefresh() {
        MediaStoreManager.shared().getAudioListLocal(binding.getRoot().getContext(), new Consumer<List<AudioModel>>() {
            @Override
            public void accept(List<AudioModel> audioModels) {
                fileList = audioModels;

                ThreadManager.shared().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if(fileList.isEmpty()){
                            textNoDownloaded.setVisibility(View.VISIBLE);
                        }else{
                            textNoDownloaded.setVisibility(View.GONE);
                        }
                        adapter.updateData(audioModels);
                    }
                });

                new CountDownTimer(1000,1000) {
                    @Override
                    public void onTick(long l) {

                    }

                    @Override
                    public void onFinish() {
                        swipeRefreshLayout.setRefreshing(false);

                    }
                }.start();
            }
        });

    }

    boolean attached = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        attached = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        attached = false;
    }

    ActivityResultLauncher<IntentSenderRequest> deleteResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK){
                        FileManager.shared().deleteActionAudioFile(getActivity(),FileManager.shared().lastFileForDelete,LibraryFragment.this);
                    }
                }
            }
    );


    @Override
    public void requestDeletePermission(IntentSenderRequest request) {
        if(request == null){
            onRefresh();
        }else{
            deleteResultLauncher.launch(request);
        }
    }
}