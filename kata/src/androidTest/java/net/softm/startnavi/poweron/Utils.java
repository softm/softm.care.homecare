package net.softm.data.kata;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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



    public static String getLanguage(Context context) {

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        }else{
            locale = context.getResources().getConfiguration().locale;
        }

        String displayCountry = locale.getDisplayCountry();
        String country = locale.getCountry();
        String language = locale.getLanguage();

        return language;
    }

    public static String getCountry(Context context) {

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        }else{
            locale = context.getResources().getConfiguration().locale;
        }

        String displayCountry = locale.getDisplayCountry();
        String country = locale.getCountry();
        String language = locale.getLanguage();

        return country;
    }
    public static String getDisplayCountry(Context context) {

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        }else{
            locale = context.getResources().getConfiguration().locale;
        }

        String displayCountry = locale.getDisplayCountry();
        String country = locale.getCountry();
        String language = locale.getLanguage();

        return displayCountry;
    }


    public static boolean isEmpty(Object s) {
        if (s == null) {
            return true;
        }
        if ((s instanceof String) && (((String)s).trim().length() == 0)) {
            return true;
        }
        if ((s instanceof String) && ("null".equals((String)s) || "<null>".equals((String)s)) ) {
            return true;
        }
        if (s instanceof Map) {
            return ((Map<?, ?>)s).isEmpty();
        }
        if (s instanceof List) {
            return ((List<?>)s).isEmpty();
        }
        if (s instanceof Object[]) {
            return (((Object[])s).length == 0);
        }
        return false;
    }
    public static boolean isNotEmpty(Object s) {
        return !isEmpty(s);
    }

    public static String loadAssetTextAsString(Context context, String url) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = context.getAssets().open(url);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ( (str = in.readLine()) != null ) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }
            return buf.toString();
        } catch (IOException e) {
            Log.e("TAG", "Error opening asset " + url);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e("TAG", "Error closing asset " + url);
                }
            }
        }

        return null;
    }

}

