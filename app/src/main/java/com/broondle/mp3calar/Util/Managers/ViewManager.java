package com.broondle.mp3calar.Util.Managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class ViewManager {

    private static ViewManager instance;

    public static synchronized ViewManager shared(){
        if(instance == null)
            instance = new ViewManager();
        return instance;
    }

    private ViewManager(){}

    public Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        Bitmap bitmap = Bitmap.createBitmap(Objects.requireNonNull(drawable).getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void setConsLayParams(View view,double widthPer,double heightPer,DisplayMetrics dp){

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams((int) GenelManager.shared().percentage(dp.widthPixels,widthPer),
                (int) GenelManager.shared().percentage(dp.heightPixels,heightPer));
        layoutParams.setMargins(0,0,0,5);
        view.setLayoutParams(layoutParams);
    }

    public boolean isTouchInsideView(View view, MotionEvent event) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        return rect.contains((int) event.getRawX(), (int) event.getRawY());
    }

}
