package com.byagowi.DroidMonthDaysIcons;

import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;
import android.os.Environment;
import com.byagowi.common.Range;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        for (int i : new Range(1, 31)) {
            Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
            int w = 64;//getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
            int h = 64;//getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
            Bitmap bmp = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap

            Canvas canvas = new Canvas(bmp);
            drawRectangleCenter(canvas, 0, 0, w, h, toPersianDigit(i));

            String root = Environment.getExternalStorageDirectory().toString();
            File dir = new File(root + "/saved_icons");
            dir.mkdirs();

            File file = new File(dir, "day" + i + ".png");
            if (file.exists ()) file.delete ();
            try {
                FileOutputStream out = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    int digitDiff = (int) '۰' - (int) '0';

    String toPersianDigit(int num) {
        StringBuilder res = new StringBuilder();
        for (char c : Integer.toString(num).toCharArray()) {
            res.append((char)((int)c + digitDiff));
        }
        return res.toString();
    }

    // borrowed and modified from http://stackoverflow.com/a/8553604/1414809
    void drawRectangleCenter(Canvas c, int topLeftX, int topLeftY, int width, int height, String textToDraw) {
        // height of 'Hello World'; height * 0.7 looks good
        int fontHeight = (int) (height * 0.9); // but I modified it

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(fontHeight);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        Rect bounds;
        bounds = new Rect();
        paint.getTextBounds(textToDraw, 0, textToDraw.length(), bounds);
        c.drawText(textToDraw, topLeftX + width / 2, topLeftY + height / 2 + (bounds.bottom - bounds.top) / 2, paint);
    }
}
