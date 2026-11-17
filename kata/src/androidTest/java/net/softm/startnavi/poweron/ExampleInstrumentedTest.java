package net.softm.data.kata;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("net.softm.data.kata", appContext.getPackageName());

    }

    @Test
    public void assetXmlpath() throws InterruptedException {
//        Context ctx = InstrumentationRegistry.getTargetContext();
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        Context context = ApplicationProvider.getApplicationContext();
//
        InputStream is = null;
        //    is = ctx.getResources().getAssets().open("res/values/strings.xml");
//            is = ctx.getResources().getAssets().open("res/values/strings.xml");
//            String v = Utils.readTextStream(is,"UTF8");
        String lng = Utils.getLanguage(ctx);
        String n = "res/values";
        if ( lng.equals("en") ) {
            n = n + "";
        } else {
            n = n + "-" + lng;
        }
        n = n +  "/strings.xml";
        Log.d("filename",n);
        String v = Utils.loadAssetTextAsString(ctx,n);
        Log.d("filevalue",v);

//        Builder parse = new Builder();
//        Document xml = parse.build("/var/www/JAVA/toForum.xml");

////        System.out.println(xml.query("//location[@id=83]/*").get(0).getParent().toXML())
//       Log.d("TAG", "dir list en count : " + String.valueOf(getStringForName( ctx, "force_stop", "en","")));
//       Log.d("TAG", "dir list en - " + Utils.getCountry(ctx) + " count : " + String.valueOf(getStringForName( ctx, "force_stop", "en",Utils.getCountry(ctx))));
//       Log.d("TAG", "dir list en - AU count : " + String.valueOf(getStringForName( ctx, "force_stop", "en","AU")));
//String language = "en";
//String country = "AU";
        String language = Utils.getLanguage(ctx);
        String country = Utils.getCountry(ctx);

//        StringsXmlValueUtil.getValues( ctx, "force_stop", "en","");
//        StringsXmlValueUtil.getValues( ctx, "force_stop", language,country);

//        ArrayList<String> values = (ArrayList<String>) StringsXmlValueUtil.getValuesNEnValues(ctx, "airplane_mode", language,country);
//        ArrayList<String> values2 = (ArrayList<String>) StringsXmlValueUtil.getValuesNEnValues(ctx, "force_stop", language,country);
//       // Locale.ENGLISH.getLanguage()
//        Log.d("TAG",values.size() + "");
//        Log.d("TAG",values2.size() + "");


//        StringsXmlValueUtil.getValues( ctx, "airplane_mode", "en","");
        StringsXmlValueUtil.getValues( ctx, "airplane_mode", "ko","");

    }

//Object[] m_args;
    public void getUniqueValueForStringsXml(Object... args) {
//        m_args = args;
    }

}