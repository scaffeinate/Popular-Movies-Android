package dev.learn.movies.app.popularmovies_udacity.util;

import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sudharti on 10/12/17.
 */

public final class DisplayUtils {

    private final static String TAG = DisplayUtils.class.getSimpleName();

    public static void fitImageInto(ImageView imageView, Uri uri, Callback callback) {
        Picasso.with(imageView.getContext()).load(uri)
                .fit().centerCrop()
                .into(imageView, callback);
    }

    public static int getYear(String dateStr, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        try {
            Date date = dateFormat.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.get(Calendar.YEAR);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }

        return -1;
    }
}
