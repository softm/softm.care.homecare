package net.softm.data.kata;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import net.softm.lib.BaseActivity;
import net.softm.lib.Constant;
import net.softm.lib.common.AsyncHttp;
import net.softm.lib.common.AsyncHttpParam;
import net.softm.lib.common.WLog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    /**
     * read a text input stream
     */
    public static String readTextStream(InputStream input, String encoding) throws IOException {
        InputStreamReader in = new InputStreamReader(input, encoding);
        StringWriter out = new StringWriter();
        copyReader(in, out);
        in.close();
        out.close();

        return out.toString();
    }

    /**
     * copies the contens of a stream
     */
    public static void copyReader(Reader in, Writer out) throws IOException {
        char[] buffer = new char[4096];
        for (;;) {
            int count = in.read(buffer, 0, 4096);
            if (count < 0)
                break;
            out.write(buffer, 0, count);
        }
    }


}

