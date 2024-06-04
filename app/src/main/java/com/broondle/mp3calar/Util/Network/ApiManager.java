package com.broondle.mp3calar.Util.Network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.broondle.mp3calar.BuildConfig;
import com.broondle.mp3calar.Model.YoutubeDataModel;
import com.broondle.mp3calar.Util.Managers.FileManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class ApiManager {


    public static class CallableReadJsonOnlineAPI implements Callable<String> {
        private final String input;

        public CallableReadJsonOnlineAPI(String input) {
            this.input = input;
        }
        @Override
        public String call() {
            String result = null;
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(input);
                System.out.println(url);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                }
                result = buffer.toString();

            }catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(connection != null){
                    connection.disconnect();
                }
                try {
                    if(reader != null){
                        reader.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return result;
        }
    }


    public static class CallableDownloadMp3Api implements Callable<Boolean>{
        private final String url,title,savePath;
        private final YoutubeDataModel ytAudioModel;
        Context context;

        public CallableDownloadMp3Api(YoutubeDataModel ytAudioModel, String url, String title, String savePath, Context context){
            this.url = url;
            this.title = title;
            this.savePath = savePath;
            this.context = context;
            this.ytAudioModel = ytAudioModel;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                return FileManager.shared().downloadFile(ytAudioModel,url,title,savePath,context);
            } catch (Exception e) {
                if(BuildConfig.DEBUG)
                    e.printStackTrace();
                return false;
            }

        }
    }


    public static class CallableThumbAPI implements Callable<Bitmap> {
        final String input;

        public CallableThumbAPI(String input) {
            this.input = input;
        }
        @Override
        public Bitmap call() {
            Bitmap bitmap = null;
            try {
                URL url = new URL(input);
                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
    }

}
