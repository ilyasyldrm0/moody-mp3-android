package com.broondle.mp3calar.Util.Managers;

public class FormatManager {

    private static FormatManager instance;

    public static synchronized FormatManager shared(){
        if(instance == null)
            instance = new FormatManager();

        return instance;
    }

    private FormatManager(){}


    // 05.06 = 5*60+6;
    public long lengthTextToLong(String text){
        String[] splitText = text.split(":");

        int first = Integer.parseInt(splitText[0]);
        first = first*60;

        int second = Integer.parseInt(splitText[1]);

        return (long) (second+first)*1000;
    }

    //milliseconds to text
    public String longToLenghtText(long value){
        value = value/1000;

        long seconds = value%60;
        long minute = value/60;

        String secString = String.valueOf(seconds);
        String minString = String.valueOf(minute);

        if (seconds < 10){
            secString = "0"+secString;
        }
        if (minute < 10){
            minString = "0"+minString;
        }

        return minString+":"+secString;
    }
}
