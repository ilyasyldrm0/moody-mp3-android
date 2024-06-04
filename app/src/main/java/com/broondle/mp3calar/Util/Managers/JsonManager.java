package com.broondle.mp3calar.Util.Managers;

import com.broondle.mp3calar.Model.AudioModel;
import com.broondle.mp3calar.Model.YoutubeDataModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class JsonManager {
    private final Gson gson = new Gson();

    private static JsonManager instance;

    public static synchronized JsonManager shared(){
        if(instance == null)
            instance = new JsonManager();

        return instance;
    }

    private JsonManager(){}

    public String audioListToJson(List<AudioModel> audioModels){
        return gson.toJson(audioModels);
    }

    public List<AudioModel> jsonToAudioList(String jsonString){
        return gson.fromJson(jsonString,new TypeToken<List<AudioModel>>(){}.getType());
    }

    public String utubeDataToJson(YoutubeDataModel dataModel){
        return gson.toJson(dataModel);
    }

    public YoutubeDataModel jsonTouTubeDataModel(String jsonString){
        return gson.fromJson(jsonString,YoutubeDataModel.class);
    }

}
