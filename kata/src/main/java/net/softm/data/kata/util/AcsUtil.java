package net.softm.data.kata.util;


import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static net.softm.data.kata.common.Const.USING_SET_NOTNAVING;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import androidx.annotation.RequiresApi;
import net.softm.lib.common.Connectivity;
import net.softm.data.kata.ConnectAccessibilityService;
import net.softm.data.kata.R;
import net.softm.data.kata.common.ReceiverType;
import net.softm.data.kata.common.RunnableArg;
import net.softm.data.kata.common.StartStopType;
import net.softm.data.kata.job.WorkQueue;
import net.softm.data.kata.kt.Callbackkt;
import net.softm.data.kata.kt.Callkt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class AcsUtil {
    private static final String TAG = AcsUtil.class.getSimpleName();
    private Context mContext;

    public static HashMap<String,BlockingQueue<RunnableArg>> eventCallbackQueues = new HashMap<String,BlockingQueue<RunnableArg>>();
    public static HashMap<String,Integer> eventCallbackDelayTimes = new HashMap<String,Integer>();
    public static HashMap<String,Boolean> eventCallbackOnlyAddQueue = new HashMap<String,Boolean>();
    public static HashMap<String,BlockingQueue<RunnableArg>> execQueues = new HashMap<String,BlockingQueue<RunnableArg>>();

    private String currentEvent = null;

    /**
     * sendEvent Delay Time
     */
    public static final long SEND_EVENT_DELAY_TIME = 1000;
    /**
     * Force Stop Button Click Delay Time
     */
    public static final long FORCE_STOP_OK_DELAY_TIME = 500;

    /**
     * Force Stop Button Click Call Interval Time
     */
    public static final long FORCE_STOP_OK_PREVENT_INTERVAL_TIME = 1000;


    /**
     * OK Click Call Interval Time
     */
    public static final long CHANGE_SETTINGS_OK_PREVENT_INTERVAL_TIME = 1000;

    public static HashMap<String,String[][]> FORCE_STOP_INFO = new HashMap<String, String[][]>();

    static {
        FORCE_STOP_INFO.put("af"     ,      new String[][]{ {"Afrikaans"}, {"Force stop" }});
        FORCE_STOP_INFO.put("am"     ,      new String[][]{ {"Amharic"}, {"በኃይል ማቆም" }});
        FORCE_STOP_INFO.put("as"     ,      new String[][]{ {"AMERICAN SAMOA"}, {"বলেৰে ৰখাওক" }});
        FORCE_STOP_INFO.put("ar"     ,      new String[][]{ {"Arabic"}, {"فرض الإيقاف" }});
        FORCE_STOP_INFO.put("ar_eg"  ,      new String[][]{ {"Arabic (Egypt)"}, {"فرض الإيقاف" }});
        FORCE_STOP_INFO.put("ar_sa"  ,      new String[][]{ {"Arabic (Saudi Arabia)"}, {"فرض الإيقاف" }});
        FORCE_STOP_INFO.put("az"     ,      new String[][]{ {"AZERBAIJAN"}, {"Dayandırılsın" }});
        FORCE_STOP_INFO.put("eu"     ,      new String[][]{ {"Basque"}, {"Behartu gelditzera"}});
        FORCE_STOP_INFO.put("be"     ,      new String[][]{ {"Belarusian"}, {"Прымусовае спыненне"}});
        FORCE_STOP_INFO.put("bn"     ,      new String[][]{ {"Bengali"}, {"জোর করে বন্ধ করুন" }});
        FORCE_STOP_INFO.put("bg"     ,      new String[][]{ {"Bulgarian"}, {"Принудител­но спиране"}});
        FORCE_STOP_INFO.put("ca"     ,      new String[][]{ {"Catalan"}, {"Força l\'aturada"}});
        FORCE_STOP_INFO.put("zh"     ,      new String[][]{ {"Chinese (Simplified)"}, {"强行停止" }});
        FORCE_STOP_INFO.put("zh_cn"  ,      new String[][]{ {"Chinese (Simplified, China)"}, {"强行停止" }});
        FORCE_STOP_INFO.put("zh_hk"  ,      new String[][]{ {"Chinese (Traditional, Hong Kong)"}, {"强行停止" }});
        FORCE_STOP_INFO.put("zh_tw"  ,      new String[][]{ { "Chinese (Traditional, Taiwan)"}, {"强行停止" }});
        FORCE_STOP_INFO.put("hr"     ,      new String[][]{ {"Croatian"}, {"Prisilno zaustavi" }});
        FORCE_STOP_INFO.put("cs"     ,      new String[][]{ {"Czech"}, {"Vynutit ukončení" }});
        FORCE_STOP_INFO.put("da"     ,      new String[][]{ {"Danish"}, {"Tving til at standse" }});
        FORCE_STOP_INFO.put("nl"     ,      new String[][]{ {"Dutch"}, {"Gedwongen stoppen" }});
        FORCE_STOP_INFO.put("en"     ,      new String[][]{ {"English"}, {"Force stop" }});
        FORCE_STOP_INFO.put("en_au"  ,      new String[][]{ {"English (Australia)"}, {"Force stop", }});
        FORCE_STOP_INFO.put("en_ca"  ,      new String[][]{ {"English (Canada)"}, {"Force stop" }});
        FORCE_STOP_INFO.put("en_gb"  ,      new String[][]{ {"English (United Kingdom)"}, {"Force stop" }});
        FORCE_STOP_INFO.put("en_ie"  ,      new String[][]{ {"English (Ireland)"}, {"Force stop" }});
        FORCE_STOP_INFO.put("en_in"  ,      new String[][]{ {"English (India)"}, {"Force stop" }});
        FORCE_STOP_INFO.put("en_sg"  ,      new String[][]{ {"English (Singapore)"}, {"Force stop" }});
        FORCE_STOP_INFO.put("en_za"  ,      new String[][]{ {"English (South Africa)"}, {"Force stop" }});
        FORCE_STOP_INFO.put("et"     ,      new String[][]{ {"Estonian"}, {"Sunni peatuma" }});
        FORCE_STOP_INFO.put("fa"     ,      new String[][]{ {"Farsi"}, {"توقف اجباری" }});
        FORCE_STOP_INFO.put("fil"    ,      new String[][]{ {"Filipino"}, {"Force stop" }});
        FORCE_STOP_INFO.put("fi"     ,      new String[][]{ {"Finnish"}, {"Pakota lopetus" }});
        FORCE_STOP_INFO.put("fr"     ,      new String[][]{ {"French"}, {"Forcer l\'arrêt" }});
        FORCE_STOP_INFO.put("fr_ca"  ,      new String[][]{ {"French (Canada)"}, {"Forcer l\'arrêt" }});
        FORCE_STOP_INFO.put("fr_ch"  ,      new String[][]{ {"French (Switzerland)"}, {"Forcer l\'arrêt" }});
        FORCE_STOP_INFO.put("gl"     ,      new String[][]{ {"Galician"}, {"Forzar parada" }});
        FORCE_STOP_INFO.put("de"     ,      new String[][]{ {"German"}, {"Beenden erzwingen" }});
        FORCE_STOP_INFO.put("de_at"  ,      new String[][]{ {"German (Austria)"}, {"Beenden erzwingen" }});
        FORCE_STOP_INFO.put("el"     ,      new String[][]{ {"Greek"}, {"Αναγκαστική διακοπή" }});
        FORCE_STOP_INFO.put("gu"     ,      new String[][]{ {"Gujarati"}, {"ફરજિયાત બંધ કરો" }});
        FORCE_STOP_INFO.put("he"     ,      new String[][]{ {"Hebrew"}, {"Force stop" }});
        FORCE_STOP_INFO.put("hi"     ,      new String[][]{ {"Hindi"}, {"ज़बरदस्ती रोकें" }});
        FORCE_STOP_INFO.put("hu"     ,      new String[][]{ {"Hungarian"}, {"Kényszerített leállítás" }});
        FORCE_STOP_INFO.put("is"     ,      new String[][]{ {"Icelandic"}, {"Þvinga lokun" }});
        FORCE_STOP_INFO.put("id"     ,      new String[][]{ {"Indonesian"}, {"Force stop" }});
        FORCE_STOP_INFO.put("it"     ,      new String[][]{ {"Italian"}, {"Forza interruzione" }});
        FORCE_STOP_INFO.put("ja"     ,      new String[][]{ {"Japanese"}, {"強制停止" }});
        FORCE_STOP_INFO.put("kn"     ,      new String[][]{ {"Kannada"}, {"ಬಲವಂತವಾಗಿ ನಿಲ್ಲಿಸಿ" }});
        FORCE_STOP_INFO.put("ko"     ,      new String[][]{ {"Korean"}, {"강제 종료", "강제 중지"}});
        FORCE_STOP_INFO.put("lo"     ,      new String[][]{ {"Lao"}, {"ບັງ​ຄັບ​ປິດ" }});
        FORCE_STOP_INFO.put("lv"     ,      new String[][]{ {"Latvian"}, {"Forsēt apturēšanu" }});
        FORCE_STOP_INFO.put("ln"     ,      new String[][]{ {"Lingala"}, {"Force stop" }});
        FORCE_STOP_INFO.put("lt"     ,      new String[][]{ {"Lithuanian"}, {"Sustabdyti" }});;
        FORCE_STOP_INFO.put("ms"     ,      new String[][]{ {"Malay"}, {"Henti paksa" }});
        FORCE_STOP_INFO.put("ml"     ,      new String[][]{ {"Malayalam"}, {"നിർബന്ധിതമായി നിർത്തുക" }});
        FORCE_STOP_INFO.put("mr"     ,      new String[][]{ {"Marathi"}, {"सक्तीने थांबवा" }});
        FORCE_STOP_INFO.put("no"     ,      new String[][]{ {"Norwegian"}, {"Force stop" }});
        FORCE_STOP_INFO.put("pl"     ,      new String[][]{ {"Polish"}, {"Wymuś zatrzymanie" }});
        FORCE_STOP_INFO.put("pt"     ,      new String[][]{ {"Portuguese"}, {"Forçar fechamento" }});
        FORCE_STOP_INFO.put("pt_br"  ,      new String[][]{ {"Portuguese (Brazil)"}, {"Forçar fechamento" }});
        FORCE_STOP_INFO.put("pt_pt"  ,      new String[][]{ {"Portuguese (Portugal)"}, {"Forçar paragem" }});
        FORCE_STOP_INFO.put("ro"     ,      new String[][]{ {"Romanian"}, {"Oprește forțat" }});
        FORCE_STOP_INFO.put("ru"     ,      new String[][]{ {"Russian"}, {"Остановить" }});
        FORCE_STOP_INFO.put("sr"     ,      new String[][]{ {"Serbian"}, {"Принудно заустави" }});
        FORCE_STOP_INFO.put("sk"     ,      new String[][]{ {"Slovak"}, {"Vynútiť zastavenie" }});
        FORCE_STOP_INFO.put("sl"     ,      new String[][]{ {"Slovenian"}, {"Prisilno ustavi" }});
        FORCE_STOP_INFO.put("es"     ,      new String[][]{ {"Spanish"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_419" ,      new String[][]{ {"Spanish (Latin America)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_ar"  ,      new String[][]{ {"Spanish (Argentina)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_cl"  ,      new String[][]{ {"Spanish (Chile)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_co"  ,      new String[][]{ {"Spanish (Colombia)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_cr"  ,      new String[][]{ {"Spanish (Costa Rica)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_do"  ,      new String[][]{ {"Spanish (Dominican Republic)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_ec"  ,      new String[][]{ {"Spanish (Ecuador)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_sv"  ,      new String[][]{ {"Spanish (El Salvador)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_gt"  ,      new String[][]{ {"Spanish (Guatemala)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_hn"  ,      new String[][]{ {"Spanish (Honduras)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_mx"  ,      new String[][]{ {"Spanish (Mexico)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_ni"  ,      new String[][]{ {"Spanish (Nicaragua)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_pa"  ,      new String[][]{ {"Spanish (Panama)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_pe"  ,      new String[][]{ {"Spanish (Peru)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_pr"  ,      new String[][]{ {"Spanish (Puerto Rico)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_py"  ,      new String[][]{ {"Spanish (Paraguay)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_us"  ,      new String[][]{ {"Spanish (United States)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_uy"  ,      new String[][]{ {"Spanish (Uruguay)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("es_ve"  ,      new String[][]{ {"Spanish (Venezuela)"}, {"Forzar detención" }});
        FORCE_STOP_INFO.put("sw"     ,      new String[][]{ {"Swahili"}, {"Lazimisha kuzima" }});
        FORCE_STOP_INFO.put("sv"     ,      new String[][]{ {"Swedish"}, {"Tvinga att avsluta" }});
        FORCE_STOP_INFO.put("gsw"    ,      new String[][]{ {"Swiss German"}, {"Force stop" }});
        FORCE_STOP_INFO.put("tl"     ,      new String[][]{ {"Tagalog"}, {"Sapilitang itigil" }});
        FORCE_STOP_INFO.put("ta"     ,      new String[][]{ {"Tamil"}, {"உடனே நிறுத்து" }});
        FORCE_STOP_INFO.put("te"     ,      new String[][]{ {"Telugu"}, {"ఫోర్స్ స్టాప్" }});
        FORCE_STOP_INFO.put("th"     ,      new String[][]{ {"Thai"}, {"บังคับให้หยุด" }});
        FORCE_STOP_INFO.put("tr"     ,      new String[][]{ {"Turkish"}, {"Durmaya zorla" }});
        FORCE_STOP_INFO.put("uk"     ,      new String[][]{ {"Ukrainian"}, {"Зупинити" }});
        FORCE_STOP_INFO.put("ur"     ,      new String[][]{ {"Urdu"}, {"زبردستی روکیں" }});
        FORCE_STOP_INFO.put("vi"     ,      new String[][]{ {"Vietnamese"}, {"Buộc dừng" }});
        FORCE_STOP_INFO.put("zu"     ,      new String[][]{ {"Zulu"}, {"Phoqelela ukuma" }});
    }



    public static HashMap<String,String[][]> STRING_INFO = new HashMap<String, String[][]>();

    static {
        STRING_INFO.put("af"     ,      new String[][]{ {"Afrikaans"}, {"Done" }});
        STRING_INFO.put("am"     ,      new String[][]{ {"Amharic"}, {"ተከናውኗል" }});
        STRING_INFO.put("as"     ,      new String[][]{ {"AMERICAN SAMOA"}, {"সম্পন্ন হ’ল" }});
        STRING_INFO.put("ar"     ,      new String[][]{ {"Arabic"}, {"تم" }});
        STRING_INFO.put("ar_eg"  ,      new String[][]{ {"Arabic (Egypt)"}, {"تم" }});
        STRING_INFO.put("ar_sa"  ,      new String[][]{ {"Arabic (Saudi Arabia)"}, {"تم" }});
        STRING_INFO.put("az"     ,      new String[][]{ {"AZERBAIJAN"}, {"Hazırdır" }});
        STRING_INFO.put("eu"     ,      new String[][]{ {"Basque"}, {"Eginda"}});
        STRING_INFO.put("be"     ,      new String[][]{ {"Belarusian"}, {"Гатова"}});
        STRING_INFO.put("bn"     ,      new String[][]{ {"Bengali"}, {"সম্পন্ন" }});
        STRING_INFO.put("bg"     ,      new String[][]{ {"Bulgarian"}, {"Готово"}});
        STRING_INFO.put("ca"     ,      new String[][]{ {"Catalan"}, {"Fet"}});
        STRING_INFO.put("zh"     ,      new String[][]{ {"Chinese (Simplified)"}, {"完成" }});
        STRING_INFO.put("zh_cn"  ,      new String[][]{ {"Chinese (Simplified, China)"}, {"完成" }});
        STRING_INFO.put("zh_hk"  ,      new String[][]{ {"Chinese (Traditional, Hong Kong)"}, {"完成" }});
        STRING_INFO.put("zh_tw"  ,      new String[][]{ { "Chinese (Traditional, Taiwan)"}, {"完成" }});
        STRING_INFO.put("hr"     ,      new String[][]{ {"Croatian"}, {"Gotovo" }});
        STRING_INFO.put("cs"     ,      new String[][]{ {"Czech"}, {"Hotovo" }});
        STRING_INFO.put("da"     ,      new String[][]{ {"Danish"}, {"Udfør" }});
        STRING_INFO.put("nl"     ,      new String[][]{ {"Dutch"}, {"Klaar" }});
        STRING_INFO.put("en"     ,      new String[][]{ {"English"}, {"Done" }});
        STRING_INFO.put("en_au"  ,      new String[][]{ {"English (Australia)"}, {"Done", }});
        STRING_INFO.put("en_ca"  ,      new String[][]{ {"English (Canada)"}, {"Done" }});
        STRING_INFO.put("en_gb"  ,      new String[][]{ {"English (United Kingdom)"}, {"Done" }});
        STRING_INFO.put("en_ie"  ,      new String[][]{ {"English (Ireland)"}, {"Done" }});
        STRING_INFO.put("en_in"  ,      new String[][]{ {"English (India)"}, {"Done" }});
        STRING_INFO.put("en_sg"  ,      new String[][]{ {"English (Singapore)"}, {"Done" }});
        STRING_INFO.put("en_za"  ,      new String[][]{ {"English (South Africa)"}, {"Done" }});
        STRING_INFO.put("et"     ,      new String[][]{ {"Estonian"}, {"Valmis" }});
        STRING_INFO.put("fa"     ,      new String[][]{ {"Farsi"}, {"تمام" }});
        STRING_INFO.put("fil"    ,      new String[][]{ {"Filipino"}, {"Done" }});
        STRING_INFO.put("fi"     ,      new String[][]{ {"Finnish"}, {"Valmis" }});
        STRING_INFO.put("fr"     ,      new String[][]{ {"French"}, {"Terminé" }});
        STRING_INFO.put("fr_ca"  ,      new String[][]{ {"French (Canada)"}, {"Terminé" }});
        STRING_INFO.put("fr_ch"  ,      new String[][]{ {"French (Switzerland)"}, {"Terminé" }});
        STRING_INFO.put("gl"     ,      new String[][]{ {"Galician"}, {"Feito" }});
        STRING_INFO.put("de"     ,      new String[][]{ {"German"}, {"Fertig" }});
        STRING_INFO.put("de_at"  ,      new String[][]{ {"German (Austria)"}, {"Fertig" }});
        STRING_INFO.put("el"     ,      new String[][]{ {"Greek"}, {"Τέλος" }});
        STRING_INFO.put("gu"     ,      new String[][]{ {"Gujarati"}, {"થઈ ગયું" }});
        STRING_INFO.put("he"     ,      new String[][]{ {"Hebrew"}, {"Done" }});
        STRING_INFO.put("hi"     ,      new String[][]{ {"Hindi"}, {"हो गया" }});
        STRING_INFO.put("hu"     ,      new String[][]{ {"Hungarian"}, {"Kész" }});
        STRING_INFO.put("is"     ,      new String[][]{ {"Icelandic"}, {"Lokið" }});
        STRING_INFO.put("id"     ,      new String[][]{ {"Indonesian"}, {"Done" }});
        STRING_INFO.put("it"     ,      new String[][]{ {"Italian"}, {"Fine" }});
        STRING_INFO.put("ja"     ,      new String[][]{ {"Japanese"}, {"完了" }});
        STRING_INFO.put("kn"     ,      new String[][]{ {"Kannada"}, {"ಮುಗಿದಿದೆ" }});
        STRING_INFO.put("ko"     ,      new String[][]{ {"Korean"}, {"완료", "완료"}});
        STRING_INFO.put("lo"     ,      new String[][]{ {"Lao"}, {"ແລ້ວໆ" }});
        STRING_INFO.put("lv"     ,      new String[][]{ {"Latvian"}, {"Gatavs" }});
        STRING_INFO.put("ln"     ,      new String[][]{ {"Lingala"}, {"Done" }});
        STRING_INFO.put("lt"     ,      new String[][]{ {"Lithuanian"}, {"Atlikta" }});;
        STRING_INFO.put("ms"     ,      new String[][]{ {"Malay"}, {"Selesai" }});
        STRING_INFO.put("ml"     ,      new String[][]{ {"Malayalam"}, {"പൂർത്തിയായി" }});
        STRING_INFO.put("mr"     ,      new String[][]{ {"Marathi"}, {"पूर्ण झाले" }});
        STRING_INFO.put("no"     ,      new String[][]{ {"Norwegian"}, {"Done" }});
        STRING_INFO.put("pl"     ,      new String[][]{ {"Polish"}, {"Gotowe" }});
        STRING_INFO.put("pt"     ,      new String[][]{ {"Portuguese"}, {"Concluído" }});
        STRING_INFO.put("pt_br"  ,      new String[][]{ {"Portuguese (Brazil)"}, {"Concluído" }});
        STRING_INFO.put("pt_pt"  ,      new String[][]{ {"Portuguese (Portugal)"}, {"Concluído" }});
        STRING_INFO.put("ro"     ,      new String[][]{ {"Romanian"}, {"Terminat" }});
        STRING_INFO.put("ru"     ,      new String[][]{ {"Russian"}, {"Готово" }});
        STRING_INFO.put("sr"     ,      new String[][]{ {"Serbian"}, {"Готово" }});
        STRING_INFO.put("sk"     ,      new String[][]{ {"Slovak"}, {"Hotovo" }});
        STRING_INFO.put("sl"     ,      new String[][]{ {"Slovenian"}, {"Končano" }});
        STRING_INFO.put("es"     ,      new String[][]{ {"Spanish"}, {"Hecho" }});
        STRING_INFO.put("es_419" ,      new String[][]{ {"Spanish (Latin America)"}, {"Hecho" }});
        STRING_INFO.put("es_ar"  ,      new String[][]{ {"Spanish (Argentina)"}, {"Hecho" }});
        STRING_INFO.put("es_cl"  ,      new String[][]{ {"Spanish (Chile)"}, {"Hecho" }});
        STRING_INFO.put("es_co"  ,      new String[][]{ {"Spanish (Colombia)"}, {"Hecho" }});
        STRING_INFO.put("es_cr"  ,      new String[][]{ {"Spanish (Costa Rica)"}, {"Hecho" }});
        STRING_INFO.put("es_do"  ,      new String[][]{ {"Spanish (Dominican Republic)"}, {"Hecho" }});
        STRING_INFO.put("es_ec"  ,      new String[][]{ {"Spanish (Ecuador)"}, {"Hecho" }});
        STRING_INFO.put("es_sv"  ,      new String[][]{ {"Spanish (El Salvador)"}, {"Hecho" }});
        STRING_INFO.put("es_gt"  ,      new String[][]{ {"Spanish (Guatemala)"}, {"Hecho" }});
        STRING_INFO.put("es_hn"  ,      new String[][]{ {"Spanish (Honduras)"}, {"Hecho" }});
        STRING_INFO.put("es_mx"  ,      new String[][]{ {"Spanish (Mexico)"}, {"Hecho" }});
        STRING_INFO.put("es_ni"  ,      new String[][]{ {"Spanish (Nicaragua)"}, {"Hecho" }});
        STRING_INFO.put("es_pa"  ,      new String[][]{ {"Spanish (Panama)"}, {"Hecho" }});
        STRING_INFO.put("es_pe"  ,      new String[][]{ {"Spanish (Peru)"}, {"Hecho" }});
        STRING_INFO.put("es_pr"  ,      new String[][]{ {"Spanish (Puerto Rico)"}, {"Hecho" }});
        STRING_INFO.put("es_py"  ,      new String[][]{ {"Spanish (Paraguay)"}, {"Hecho" }});
        STRING_INFO.put("es_us"  ,      new String[][]{ {"Spanish (United States)"}, {"Hecho" }});
        STRING_INFO.put("es_uy"  ,      new String[][]{ {"Spanish (Uruguay)"}, {"Hecho" }});
        STRING_INFO.put("es_ve"  ,      new String[][]{ {"Spanish (Venezuela)"}, {"Hecho" }});
        STRING_INFO.put("sw"     ,      new String[][]{ {"Swahili"}, {"Nimemaliza" }});
        STRING_INFO.put("sv"     ,      new String[][]{ {"Swedish"}, {"Klar" }});
        STRING_INFO.put("gsw"    ,      new String[][]{ {"Swiss German"}, {"Done" }});
        STRING_INFO.put("tl"     ,      new String[][]{ {"Tagalog"}, {"Tapos na" }});
        STRING_INFO.put("ta"     ,      new String[][]{ {"Tamil"}, {"முடிந்தது" }});
        STRING_INFO.put("te"     ,      new String[][]{ {"Telugu"}, {"పూర్తయింది" }});
        STRING_INFO.put("th"     ,      new String[][]{ {"Thai"}, {"เสร็จสิ้น" }});
        STRING_INFO.put("tr"     ,      new String[][]{ {"Turkish"}, {"Bitti" }});
        STRING_INFO.put("uk"     ,      new String[][]{ {"Ukrainian"}, {"Готово" }});
        STRING_INFO.put("ur"     ,      new String[][]{ {"Urdu"}, {"ہوگیا" }});
        STRING_INFO.put("vi"     ,      new String[][]{ {"Vietnamese"}, {"Xong" }});
        STRING_INFO.put("zu"     ,      new String[][]{ {"Zulu"}, {"Kwenziwe" }});
    }

    public static String PREFIX_EVENT  = "SOFTM:EVENT_";
    public static String UNCAUGHT_EXCEPTION  = PREFIX_EVENT + "SOFTM_UNCAUGHT_EXCEPTION";

    public static String OPEN_OVERLAY_VIEW_SHOWONLY  = PREFIX_EVENT + "SOFTM_OPEN_OVERLAY_VIEW_SHOWONLY";

    public static String SHOW_WAITING_OVERLAY = PREFIX_EVENT +"SOFTM_SHOW_WAITTING_OVERLAY";
    public static String SHOW_WAITING_OVERLAY_UPDATE_STATUS = PREFIX_EVENT +"SOFTM_SHOW_WAITING_OVERLAY_UPDATE_STATUS";

    public static String REGISTER_QUEUE  = PREFIX_EVENT + "SOFTM_REGISTER_QUEUE";
    public static String TURN_ON_SCREEN  = PREFIX_EVENT + "SOFTM_TURN_ON_SCREEN";
    public static String TURN_OFF_SCREEN  = PREFIX_EVENT + "SOFTM_TURN_OFF_SCREEN";
    public static String GESTURE_SWIPE  = PREFIX_EVENT + "SOFTM_GESTURE_SWIPE";
    public static String GESTURE_DOUBLE_TAP  = PREFIX_EVENT + "SOFTM_GESTURE_DOUBLE_TAP";
    public static String GESTURE_SWIPE_TEST  = PREFIX_EVENT + "SOFTM_GESTURE_SWIPE_TEST";
    public static String GESTURE_TAP  = PREFIX_EVENT + "SOFTM_GESTURE_TAP";
    public static String GESTURE_FAST_SWIPE_FOWARD = PREFIX_EVENT + "SOFTM_GESTURE_FAST_SWIPE_FOWARD";

    public static String EVENT_DUMMY = PREFIX_EVENT + "SOFTM_DUMMY";
    public static String EVENT_REGISTER_CALLBACK_TRIGGER = PREFIX_EVENT + "SOFTM_REGISTER_CALLBACK_TRIGGER";
//    public static String EVENT_REGISTER_CALLBACK = PREFIX_EVENT + "REGISTER_CALLBACK";

    public static String EVENT_FORCE_STOP_CLICK = PREFIX_EVENT + "SOFTM_FORCE_STOP_CLICK";

    public static String EVENT_FORCE_STOP_DIALOG_OK_CLICK = PREFIX_EVENT + "SOFTM_FORCE_STOP_DIALOG_OK_CLICK";

    public static String EVENT_QUICK_SETTINGS_CLICK = PREFIX_EVENT + "SOFTM_QUICK_SETTINGS_CLICK";
    public static String EVENT_SETTINGS_SWITCH_CLICK = PREFIX_EVENT + "SOFTM_SETTINGS_SWITCH_CLICK";

    public static String EVENT_REGISTER_RECEIVER = PREFIX_EVENT + "SOFTM_REGISTER_RECEIVER";
    public static String EVENT_UNREGISTER_RECEIVER = PREFIX_EVENT + "SOFTM_UNREGISTER_RECEIVER";
    public static String EVENT_STOP_RECENTS = PREFIX_EVENT + "SOFTM_STOP_RECENTS";
    public static String EVENT_STOP_RECENTS_ALL = PREFIX_EVENT + "SOFTM_STOP_RECENTS_ALL";

    // GLOBAL KEY
    public static String KEY_GLOBAL_ACTION_BACK = PREFIX_EVENT + "GLOBAL_ACTION_BACK";
    public static String KEY_GLOBAL_ACTION_HOME = PREFIX_EVENT + "GLOBAL_ACTION_HOME";
    public static String KEY_GLOBAL_ACTION_RECENTS = PREFIX_EVENT + "GLOBAL_ACTION_RECENTS";
    public static String KEY_GLOBAL_ACTION_NOTIFICATIONS = PREFIX_EVENT + "GLOBAL_ACTION_NOTIFICATIONS";
    public static String KEY_GLOBAL_ACTION_QUICK_SETTINGS = PREFIX_EVENT + "GLOBAL_ACTION_QUICK_SETTINGS";
    public static String KEY_GLOBAL_ACTION_POWER_DIALOG = PREFIX_EVENT + "GLOBAL_ACTION_POWER_DIALOG";
    public static String KEY_GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN = PREFIX_EVENT + "GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN";
    public static String KEY_GLOBAL_ACTION_LOCK_SCREEN = PREFIX_EVENT + "GLOBAL_ACTION_LOCK_SCREEN";
    public static String KEY_GLOBAL_ACTION_TAKE_SCREENSHOT = PREFIX_EVENT + "GLOBAL_ACTION_TAKE_SCREENSHOT";
    public static String KEY_GLOBAL_ACTION_KEYCODE_HEADSETHOOK = PREFIX_EVENT + "GLOBAL_ACTION_KEYCODE_HEADSETHOOK";
    public static String KEY_GLOBAL_ACTION_ACCESSIBILITY_BUTTON = PREFIX_EVENT + "GLOBAL_ACTION_ACCESSIBILITY_BUTTON";
    public static String KEY_GLOBAL_ACTION_ACCESSIBILITY_BUTTON_CHOOSER = PREFIX_EVENT + "GLOBAL_ACTION_ACCESSIBILITY_BUTTON_CHOOSER";
    public static String KEY_GLOBAL_ACTION_ACCESSIBILITY_SHORTCUT = PREFIX_EVENT + "GLOBAL_ACTION_ACCESSIBILITY_SHORTCUT";
    public static String KEY_GLOBAL_ACTION_ACCESSIBILITY_ALL_APPS = PREFIX_EVENT + "GLOBAL_ACTION_ACCESSIBILITY_ALL_APPS";
    public static String KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE = PREFIX_EVENT + "GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE";
    ConnectAccessibilityService mAcsService = null;
    private Runnable runnable;

    public AcsUtil(Context context)
    {
        mContext = context;

        mAcsService = ConnectAccessibilityService.mContext;
        // Reset Settings Click Config
        settingsIsCompleted = false;
        settingsRecallCnt = 0L;

    }
    public static void logViewHierarchy(AccessibilityNodeInfo nodeInfo, final int depth) {

        if (nodeInfo == null) return;

        String spacerString = "";

        for (int i = 0; i < depth; ++i) {
            spacerString += '-';
        }
        Fc.i(ConnectAccessibilityService.mContext,TAG,"logViewHierarchy","ACS.nodeInfo : " + spacerString + nodeInfo.getClassName() + " " + nodeInfo.getText() + " / " + nodeInfo.getContentDescription() + " - " + nodeInfo.getViewIdResourceName());
        //Log the info you care about here... I choce classname and view resource name, because they are simple, but interesting.
        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
            logViewHierarchy(nodeInfo.getChild(i), depth+1);
        }
    }

    public static List<AccessibilityNodeInfo> findNodesByDescription(AccessibilityNodeInfo rootNode, String text) {
        List<AccessibilityNodeInfo> matchingNodes = new ArrayList<>();

        // 텍스트를 포함한 현재 노드를 찾기
        if (rootNode.getContentDescription() != null && rootNode.getContentDescription().toString().contains(text)) {
            matchingNodes.add(rootNode);
        }

        // 자식 노드들을 재귀적으로 탐색
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = rootNode.getChild(i);
            if (childNode != null) {
                matchingNodes.addAll(findNodesByText(childNode, text));  // 자식 노드에서 재귀 호출
            }
        }

        return matchingNodes;
    }


    public static List<AccessibilityNodeInfo> findNodesByText(AccessibilityNodeInfo rootNode, String text) {
        List<AccessibilityNodeInfo> matchingNodes = new ArrayList<>();

        // 현재 노드에서 텍스트를 검색
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(text);
        if (nodes != null) {
            matchingNodes.addAll(nodes);  // 텍스트가 일치하는 노드를 리스트에 추가
        }

        // 자식 노드들을 재귀적으로 탐색
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = rootNode.getChild(i);
            if (childNode != null) {
                matchingNodes.addAll(findNodesByText(childNode, text));  // 자식 노드에서 재귀 호출
            }
        }

        return matchingNodes;
    }

    public AccessibilityNodeInfo findNodeByDescription(AccessibilityNodeInfo rootNode, String description) {
        // Traverse the accessibility tree to find the button node
        // You can use different methods to find the button, such as by text, ID, or other characteristics

        // Example: Find the button by text
        if (rootNode != null) {
            for (int i = 0; i < rootNode.getChildCount(); i++) {
                AccessibilityNodeInfo child = rootNode.getChild(i);
//                android.widget.Switch
                if (child != null && child.getContentDescription() != null && child.getContentDescription().toString().equals(description)) {
                    return child;
                }
                AccessibilityNodeInfo foundNode = findNodeByDescription(child, description);
                if (foundNode != null) {
                    return foundNode;
                }
            }
        }
        return null;
    }


    public AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo rootNode, String buttonText) {
        // Traverse the accessibility tree to find the button node
        // You can use different methods to find the button, such as by text, ID, or other characteristics

        // Example: Find the button by text
        if (rootNode != null) {
            for (int i = 0; i < rootNode.getChildCount(); i++) {
                AccessibilityNodeInfo child = rootNode.getChild(i);
//                android.widget.Switch
                if (child != null && child.getText() != null && child.getText().toString().equals(buttonText)) {
                    return child;
                }
                AccessibilityNodeInfo foundNode = findNodeByText(child, buttonText);
                if (foundNode != null) {
                    return foundNode;
                }
            }
        }
        return null;
    }

    public AccessibilityNodeInfo findButtonByTextChild(AccessibilityNodeInfo rootNode, String buttonText) {
        // Traverse the accessibility tree to find the button node
        // You can use different methods to find the button, such as by text, ID, or other characteristics

        // Example: Find the button by text
        if (rootNode != null) {
            for (int i = 0; i < rootNode.getChildCount(); i++) {
                AccessibilityNodeInfo child = rootNode.getChild(i);
//                android.widget.Switch
//              if (child != null && child.getClassName().equals("android.widget.Button")
                if (child != null && "android.widget.Button".equals(child.getClassName())
                        && child.getText() != null && child.getText().toString().equals(buttonText)) {
                    return child;
                }
                if (child != null && "android.widget.Switch".equals(child.getClassName())
                        && child.getText() != null && child.getText().toString().equals(buttonText)) {
                    return child;
                }
                AccessibilityNodeInfo foundNode = findButtonByTextChild(child, buttonText);
                if (foundNode != null) {
                    return foundNode;
                }
            }
        }
        return null;
    }
    public AccessibilityNodeInfo findButtonByText(AccessibilityNodeInfo rootNode, String buttonText) {
        if (rootNode != null) {
            for ( AccessibilityNodeInfo child : rootNode.findAccessibilityNodeInfosByText(buttonText)) {
//              if ( child != null && child.getClassName().equals("android.widget.Button") ) {
                if (child != null && "android.widget.Button".equals(child.getClassName())) {
                    return child;
                }
            }
            for (int i = 0; i < rootNode.getChildCount(); i++) {
                AccessibilityNodeInfo child = rootNode.getChild(i);
//                android.widget.Switch
//              if (child != null && child.getClassName().equals("android.widget.Button")
                if (child != null && "android.widget.Button".equals(child.getClassName())
                        && child.getText() != null && child.getText().toString().equals(buttonText)) {
                    return child;
                }
                AccessibilityNodeInfo foundNode = findButtonByTextChild(child, buttonText);
                if (foundNode != null) {
                    return foundNode;
                }
            }

            for (int i = 0; i < rootNode.getChildCount(); i++) {
                AccessibilityNodeInfo child = rootNode.getChild(i);
//                android.widget.Switch
                if (child != null && child.getText() != null && child.getText().toString().equals(buttonText)) {
                    return child;
                }
                AccessibilityNodeInfo foundNode = findNodeByText(child, buttonText);
                if (foundNode != null) {
                    return foundNode;
                }
            }
        }
        return null;
    }

    public AccessibilityNodeInfo findSwitchByText(AccessibilityNodeInfo rootNode, String buttonText) {
        AccessibilityNodeInfo nodeInfo = null;
        if (rootNode != null) {
            for ( AccessibilityNodeInfo child : rootNode.findAccessibilityNodeInfosByText(buttonText)) {
                if ( child != null && child.getClassName().equals("android.widget.Switch") ) {
                    nodeInfo = child;
                    break;
                }
            }
        }
        return nodeInfo;
    }

    public AccessibilityNodeInfo findButtonByClassName(AccessibilityNodeInfo rootNode, String className) {
        // Traverse the accessibility tree to find the button node
        // You can use different methods to find the button, such as by text, ID, or other characteristics

        // Example: Find the button by text
        if (rootNode != null) {
            for (int i = 0; i < rootNode.getChildCount(); i++) {
                AccessibilityNodeInfo child = rootNode.getChild(i);
//                android.widget.Switch
                if (child != null && className.equals(child.getClassName()) ) {
                    return child;
                }
                AccessibilityNodeInfo foundNode = findButtonByClassName(child, className);
                if (foundNode != null) {
                    return foundNode;
                }
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean invokeForceStopOkClick(AccessibilityNodeInfo nodeInfo) {
        boolean clicked = false;
        if ( nodeInfo != null ) {
//            if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event.getEventType()) {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/left_button");
            for (AccessibilityNodeInfo node : list) {
//                            Log.i("ACS", "force_stop.Button.left_button " + node);
                Fc.i(mContext,TAG,"forceStop.invokeForceStopOkClick","Dialog.ok.Button.left_button Clicked");
                clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                appDetailSettingsForceClickedOkTime = System.currentTimeMillis();
//                            clicked = true;
                break;
            }
            if ( clicked ) return true;
            list = nodeInfo.findAccessibilityNodeInfosByViewId("android:id/button1");
            for (AccessibilityNodeInfo node : list) {
                Fc.i(mContext,TAG,"forceStop.invokeForceStopOkClick","Dialog.ok.Button.button1 Clicked");
                clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                appDetailSettingsForceClickedOkTime = System.currentTimeMillis();
//                            clicked = true;
                break;
            }
            if ( clicked ) return true;
            List<String> okInfos = StringsXmlValueUtil.getValuesNEnValues(mContext, "dlg_ok");
            for (String btnStr : okInfos) {
                AccessibilityNodeInfo node = findButtonByText(nodeInfo, btnStr);
                if ( node != null ) {
                    clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                               clicked = true;
                    if ( !clicked ) {
                        Rect bounds = findButtonAndPrintCoordinates(node);
                        if (bounds != null) {
                            int centerX = (bounds.left + bounds.right) / 2;
                            int centerY = (bounds.top + bounds.bottom) / 2;
                            sendEvent(GESTURE_TAP, new String[]{String.valueOf(bounds.centerX()), String.valueOf(bounds.centerY()*0.95 )}, 0, () -> {
                                // Perform the click action
                                appDetailSettingsForceClickedOkTime = System.currentTimeMillis();
                            }); // Click : GESTURE_TAP

                            // TODO SOFTM : 버튼의 위치가 상이한 조건에대해서 보정값을 뺀 위치로 한번더 탭을 전송함
                            sendEvent(GESTURE_TAP, new String[]{String.valueOf(bounds.centerX()), String.valueOf(bounds.centerY() )}, 0, () -> {
                                // Perform the click action
                                appDetailSettingsForceClickedOkTime = System.currentTimeMillis();
                            }); // Click : GESTURE_TAP
                            // TODO SOFTM ACS Delay Point
                            Utils.sleepMicoSeconds(800); // 딜레이 줘야 1~2번에 클릭됨.
                            Fc.i(mContext,TAG,"forceStop.invokeForceStopOkClick","Dialog.ok.Button[" + btnStr + "][" + centerX + "," + centerY + "].click");
                            clicked = true; // TODO SOFTM : 클릭이 안되었을 수 있으나 검증할 수 있는 방법이 애매하므로 스킵
                        }
                    } else {
                        appDetailSettingsForceClickedOkTime = System.currentTimeMillis();
                    }
                    break;
                }
            }

//                    }
//                }, FORCE_STOP_OK_DELAY_TIME);
//            }
        }
        return clicked;
    }

    private void addEvent(String eventGubun,Runnable runnable) {
        addEvent(eventGubun,runnable,0);
    }
    private void addEvent(String eventGubun,RunnableArg runnableArg) {
        addEvent(eventGubun,runnableArg,0);
    }

    private void addEvent(String eventGubun,Runnable runnable,int delayTime) {
        if ( delayTime > 0 ) Utils.sleepMicoSeconds(delayTime);
        // addEventQueue
        BlockingQueue queue = new LinkedBlockingQueue<>();
        queue.offer(runnable);
        execQueues.put(eventGubun,queue);
    }

    private void addEvent(String eventGubun,RunnableArg runnableArg,int delayTime) {
        if ( delayTime > 0 ) Utils.sleepMicoSeconds(delayTime);
        // addEventQueue
        BlockingQueue queue = new LinkedBlockingQueue<>();
        queue.offer(runnableArg);
        execQueues.put(eventGubun,queue);
    }

    public void forceStop() {
        forceStop(false);
    }
    public synchronized void forceStop(boolean workQueueNext) {
        try {

            Fc.i(mContext,TAG,"forceStop","workQueueNext : " + workQueueNext);
            if ( USING_SET_NOTNAVING || !MapUtil.isOnlyStopAfterNaving() ) {
                MapUtil.setNotNavingForNotiListener();
                MapUtil.setNotNaving();
            }
            Fc.i(mContext,TAG,"forceStop","start");
            if ( !PermissionUtil.isAccessibilityServiceRunning(mContext) ) {
                if (workQueueNext) {
                    Fc.i(mContext,TAG, "forceStop","AcesssibilityService.not.running.nextTask");
                    WorkQueue.nextTask();
                }
                return;
            }
            sendEvent(KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE, () -> {
                /** 앱 상세 설정 이동 */
                Utils.goAppDetailSettings(mContext);
                // TODO SOFTM ACS Delay Point
//            Utils.sleepMicoSeconds(100);

                /** EVENT_FORCE_STOP_CLICK 이벤트 처리를 접근성서비스 내부에서 안함으로 사용안함 */
                /** 제거하려 했으나 : sendEvent(EVENT_FORCE_STOP_CLICK,()->{ */
                /** 갤럭시 S10 Note+ 에서 작동 안함 : 접근성 콜백으로 구현해야 하는 듯 함*/
                sendEvent(EVENT_FORCE_STOP_CLICK,()->{
                    boolean clickedForceStop = false;
                    boolean clickedOk= false;

                    List<String> forceStopInfos = StringsXmlValueUtil.getValuesNEnValues(mContext,"force_stop");
                    List<String> okInfos = StringsXmlValueUtil.getValuesNEnValues(mContext, "ok");
                    boolean isAlreadyClicked = false;
                    Fc.i(mContext,TAG, "forceStop","start.entered");
                    int limitExec = 0;
                    while(!settingsIsCompleted && !clickedOk) {
                        AccessibilityNodeInfo rootNode = mAcsService.getRootInActiveWindow();
                        limitExec++;
                        if ( limitExec > settingsLimitExec || settingsIsCompleted || clickedOk ) {
                            Fc.i(mContext,TAG, "forceStop","limit.loop." + limitExec);
                            break;
                        }

                        Fc.i(mContext,TAG,"forceStop","finds => forceStopInfos = [" + (forceStopInfos.size()>0?forceStopInfos.toString():"") + "]");
                        // if ( "com.android.settings".equals(event.getPackageName()) && nodeInfo != null ) {
                        // }
                        if (rootNode != null) {
                            AccessibilityNodeInfo forceStopButton = null;
                            if ( !clickedForceStop ) { // 강제 중지 버튼이 클릭후 다이얼로그가 열리는데 거기에 존재함. Galaxy A7(Nuga)
                                boolean isUnableClick = true;
                                boolean isEnabled = false;
                                for (String btnStr : forceStopInfos) {
//                                rootNode = mAcsService.getRootInActiveWindow();
                                    forceStopButton = findButtonByText(rootNode, btnStr);
                                    if (forceStopButton != null) {
                                        isEnabled = forceStopButton.isEnabled();
                                        isUnableClick = !forceStopButton.isClickable();
                                        if ( isUnableClick ) {
                                            Rect bounds = findButtonAndPrintCoordinates(forceStopButton);
                                            if (bounds != null) {
                                                int centerX = (bounds.left + bounds.right) / 2;
                                                int centerY = (bounds.top + bounds.bottom) / 2;
                                                sendEvent(GESTURE_TAP, new String[]{String.valueOf(bounds.centerX()), String.valueOf(bounds.centerY() * 0.95)}, 0, () -> {
                                                    // Perform the click action
                                                    appDetailSettingsForceClickedTime = System.currentTimeMillis();
                                                }); // Click : GESTURE_TAP
                                                // TODO SOFTM ACS Delay Point
                                                Utils.sleepMicoSeconds(800); // 딜레이 줘야 1~2번에 클릭됨.
                                                clickedForceStop = true;
                                                Fc.i(mContext, TAG, "forceStop", "Button.rect.click[" + btnStr + "][" + centerX + "," + centerY + "].click");
                                            }
                                        } else {
                                            // Perform the click action
                                            appDetailSettingsForceClickedTime = System.currentTimeMillis();
                                            // buttonNode.isEnabled();
                                            if (isEnabled) {
                                                //                                forceStopButton.getDrawingOrder()
                                                //                                forceStopButton.getParent().getDrawingOrder()
                                                clickedForceStop = forceStopButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                                Fc.i(mContext, TAG, "forceStop", "Button Clicked");
                                                //                                    clickedForceStop = true;
//                                        Utils.sleepMicoSeconds(100);
                                            }
                                        }
                                    }
                                }
                            }

                            Utils.sleepMicoSeconds(500); // 확인/취소 다이얼로그 화면에 나오도록 대기

                            if ( !clickedOk && clickedForceStop ) {
                                if ( MapUtil.isNaviApp() ) {
                                    Utils.toast(mContext,net.softm.data.kata.R.string.force_stop_navi);
                                } else {
                                    Utils.toast(mContext,net.softm.data.kata.R.string.force_stop_app);
                                }
                                //                        Fc.i(mContext,TAG,"force_stop.Dialog.ok.Button Clicked");
                                clickedOk = invokeForceStopOkClick(mAcsService.getRootInActiveWindow());
                                Fc.i(mContext,TAG,"forceStop","Button - clickedOk : " + clickedOk);
                            } else {
                                Fc.i(mContext,TAG,"forceStop","Button - Disabled - Not Clicked");
                                clickedOk = true;
                            }
                        }
                    }
                    Fc.i(mContext,TAG,"forceStop","ing : clickedOk : " + clickedOk);

                    if ( !clickedOk ) {
                        if ( checkerForDoneHandler != null ) {
                            checkerForDoneHandler.removeCallbacksAndMessages(null);
                            checkerForDoneHandler = null;
                        }

                        checkerForDoneHandler = (new Handler(Looper.getMainLooper()));
                        checkerForDoneHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!settingsIsCompleted) {
                                    //                    sendEvent(KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE, () -> {
                                    //                    },0,0);
                                    if ( settingsRecallCnt < settingsRecallLimit ) {
                                        settingsRecallCnt++;
                                        Fc.i(mContext,TAG,"forceStop","recall[" + settingsRecallCnt + "]");
                                        forceStop(workQueueNext);
                                    } else {
                                        settingsIsCompleted = true;
                                        sendEvent(KEY_GLOBAL_ACTION_BACK, new RunnableArg() {
                                            @Override
                                            public void run() {
                                                if (!settingsIsCompleted) {
                                                    settingsIsCompleted = true;
                                                    Fc.i(mContext,TAG,"forceStop","recallLimited.nextTask.forceStopInfos", String.format("[%s]", forceStopInfos.isEmpty() ? "" : forceStopInfos));

                                                    if (workQueueNext) {
                                                        WorkQueue.nextTask();
                                                    }
                                                }
                                            }
                                        }, settingsCompleteCallbackDelay);
                                    }
                                }
                            }
                        }, settingsRecallDelay);
                        sendEvent(KEY_GLOBAL_ACTION_BACK); // force_stop.recall로 다시 실행하는경우 다른 처리 없이 뒤로가기만
                    } else {
                        settingsIsCompleted = true;
                        if (checkerForDoneHandler != null) {
                            checkerForDoneHandler.removeCallbacksAndMessages(null);
                            checkerForDoneHandler = null;
                            Fc.i(mContext,TAG, "forceStop","remove checkerForDoneHandler");
                        }
                        sendEvent(KEY_GLOBAL_ACTION_BACK, new RunnableArg() {
                            @Override
                            public void run() {
                                if (workQueueNext) {
                                    Fc.i(mContext,TAG, "forceStop","clicked.completed.nextTask");
                                    WorkQueue.nextTask();
                                }
                            }
                        }, settingsCompleteCallbackDelay);
                    }

                    /** EVENT_FORCE_STOP_CLICK 이벤트 처리를 접근성서비스 내부에서 안함으로 사용안함 */
                },forceStopCallbackDelay);
                appDetailSettingsEnteredTime = System.currentTimeMillis();
            },0,500);
        } catch (Exception ex) {
            Fc.e(mContext,TAG,"forceStop","ex");
        }
    } // end forceStop

    public void recentsStop() {
        forceStop(false);
    }
    public void recentsStop(boolean workQueueNext) {
        sendEvent(KEY_GLOBAL_ACTION_RECENTS, ()->{
            sendEvent(EVENT_STOP_RECENTS, ()->{
                sendEvent(KEY_GLOBAL_ACTION_BACK,()->{
                    if (workQueueNext) {
                        Fc.i(mContext,TAG, "recents_stop.completed.nextTask");
                        WorkQueue.nextTask();
                    }
                },0,500);
            },0,1000);
        },0,1000);

//        sendEvent(KEY_GLOBAL_ACTION_BACK, new RunnableArg() {
//            @Override
//            public void run() {
//                if (!settingsIsCompleted) {
//                    settingsIsCompleted = true;
//
//                    Fc.i(mContext,TAG, "force_stop.recallLimited.nextTask");
//                    if (workQueueNext) {
//                        WorkQueue.nextTask();
//                    }
//                }
//            }
//        }, settingsCompleteCallbackDelay);

    }
    public void recentsAllStop(boolean workQueueNext) {
        try {

            Fc.i(mContext,TAG,"recentsAllStop.start","workQueueNext : " + workQueueNext);

            sendEvent(KEY_GLOBAL_ACTION_RECENTS, () -> {
                boolean clickedForceStop = false;
                Fc.i(mContext,TAG, "recentsAllStop.start.entered");
                int limitExec = 0;
                List<String> forceStopInfos = StringsXmlValueUtil.getValuesNEnValues(mContext,"clear_all");
                while(!clickedForceStop) {
                    AccessibilityNodeInfo rootNode = mAcsService.getRootInActiveWindow();
                    limitExec++;
                    if ( limitExec > settingsLimitExec || clickedForceStop ) {
                        Fc.i(mContext,TAG, "recentsAllStop.limit.loop." + limitExec);
                        break;
                    }
                    if (rootNode != null) {
                        AccessibilityNodeInfo forceStopButton = null;
                        if ( !clickedForceStop ) { // 강제 중지 버튼이 클릭후 다이얼로그가 열리는데 거기에 존재함. Galaxy A7(Nuga)
                            if ( limitExec > 3 ) {
                                sendEvent(GESTURE_FAST_SWIPE_FOWARD, () -> {
                                });
                                Utils.sleepMicoSeconds(1000);
                            }

                            if ( !MapUtil.isTest() ) {
                                for (String btnStr : forceStopInfos) {
                                    rootNode = mAcsService.getRootInActiveWindow();
                                    forceStopButton = findButtonByText(rootNode, btnStr);
                                    if (forceStopButton != null) {
                                        if (forceStopButton.isEnabled()) {
                                            clickedForceStop = forceStopButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                            if (clickedForceStop)
                                                Fc.i(mContext,TAG, "recentsAllStop.Button Clicked");
                                            //                                        Utils.sleepMicoSeconds(100);
                                        }
                                    }
                                }
                            }
                        }
                        Utils.sleepMicoSeconds(500); // 확인/취소 다이얼로그 화면에 나오도록 대기
                    }
//                        R.string.dismiss_all

                }

                sendEvent(KEY_GLOBAL_ACTION_BACK, new RunnableArg() {
                    @Override
                    public void run() {
                        if (workQueueNext) {
                            Fc.i(mContext,TAG, "recentsAllStop.clicked.completed.nextTask");
                            WorkQueue.nextTask();
                        }
                    }
                }, settingsCompleteCallbackDelay);
//                    Fc.i(mContext,TAG,"force_stop.ing : clickedOk : " + clickedOk);

            },0,500);
        } catch (Exception ex) {
            Fc.e(mContext,TAG,"recentsAllStop",ex.toString());
        }

    }
    public static long appDetailSettingsEnteredTime = 0L;

    public static long acsEnteredTime = 0L;

    public static long appDetailSettingsForceClickedTime = 0L;
    public static long appDetailSettingsForceClickedOkTime = 0L;
    public static boolean isValidForceButtonClicked() {
        // Settings 진입후 2초 지나지 않았다면
        return AcsUtil.appDetailSettingsForceClickedTime + ( FORCE_STOP_OK_PREVENT_INTERVAL_TIME +2000 ) > System.currentTimeMillis();
//        return ((SEND_EVENT_DELAY_TIME+2000) * 1000) +( (FORCE_STOP_OK_DELAY_TIME+2000) * 1000 ) + AcsUtil.appDetailSettinsEnteredTime > System.currentTimeMillis();
    }

    public static long appDetailSettingsSwitchClickedTime = 0L;
    public static boolean isValidClickedControlSetting() {
        // Settings 진입후 2초 지나지 않았다면
        return AcsUtil.appDetailSettingsSwitchClickedTime + ( CHANGE_SETTINGS_OK_PREVENT_INTERVAL_TIME ) > System.currentTimeMillis();
//        return ((SEND_EVENT_DELAY_TIME+2000) * 1000) +( (FORCE_STOP_OK_DELAY_TIME+2000) * 1000 ) + AcsUtil.appDetailSettinsEnteredTime > System.currentTimeMillis();
    }
    public void setSettingsControl(Context context, StartStopType startStopType, boolean enabled) {
        setSettingsControl(context,startStopType,enabled, false);
    }
    public synchronized void setSettingsControl(Context context, StartStopType startStopType, boolean enabled, boolean workQueueNext) {
        try {

            Fc.i(mContext,TAG,"setSettingsControl");
            Fc.i(mContext,TAG,"setSettingsControl","button.settings.control." + startStopType.getId().toString() + ".start : " + isEnabledForStartStopType(startStopType) + " -> " + enabled);

            if (enabled == isEnabledForStartStopType(startStopType)) {
                Fc.i(mContext,TAG, "setSettingsControl","button.settings.control." + startStopType.getId().toString() + " : " + enabled + " is same " + isEnabledForStartStopType(startStopType));
                if (!settingsIsCompleted) {
                    Fc.i(mContext,TAG, "setSettingsControl","button.settings.control." + startStopType.getId().toString() + ".is same.nextTask");
                    sendEvent(KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE, () -> {
                        if (checkerForDoneHandler != null) {
                            checkerForDoneHandler.removeCallbacksAndMessages(null);
                            checkerForDoneHandler = null;
                            Fc.i(mContext,TAG, "setSettingsControl","button.settings.settings." + startStopType.getId().toString() + ".stop.same.recallHandler : " + isEnabledForStartStopType(startStopType) + " -> " + enabled);
                        }
                        if (workQueueNext) {
                            WorkQueue.nextTask();
                        }
                        settingsIsCompleted = true;
                    }, 0, settingsCompleteCallbackDelay);
                }
                return;
            } else {
                settingsIsCompleted = false;
            }

            sendEvent(KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE, () -> {
                if (startStopType.equals(StartStopType.WIFI)) {
                    Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                    panelIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(panelIntent);
                } else if (startStopType.equals(StartStopType.MOBILE_DATA)) {
                    Intent panelIntent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
                    panelIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    PackageManager pm = context.getPackageManager();
                    if (panelIntent.resolveActivity(pm) != null) {
                        context.startActivity(panelIntent);
                    } else {
                        L.d(TAG,"MOBILE_DATA : dose not start");
                    }
                } else if (startStopType.equals(StartStopType.AIRPLANE_MODE)) {
                    Intent panelIntent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                    panelIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(panelIntent);
                }

                appDetailSettingsEnteredTime = System.currentTimeMillis();

                List<String> stringsXmlValues = DeviceUtil.getStringsXmlValuesForStartStopType(mContext, startStopType);
                /** EVENT_SETTINGS_SWITCH_CLICK 이벤트 처리를 접근성서비스 내부에서 안함으로 사용안함 */
                //        sendEvent(EVENT_SETTINGS_SWITCH_CLICK, new String[]{startStopType.getId(),String.valueOf(enabled)},0,()->{
                boolean clickedSwitch = false;
                boolean clickedDone = false;
                boolean isAlreadyClicked = true;
                List<String> doneInfos = StringsXmlValueUtil.getValuesNEnValues(mContext, "done");
                AccessibilityNodeInfo rootNode = mAcsService.getRootInActiveWindow();
                int limitExec = 0;
                while (!settingsIsCompleted && enabled != isEnabledForStartStopType(startStopType)) {
                    Utils.sleepMicoSeconds(250);
                    rootNode = mAcsService.getRootInActiveWindow();
                    isAlreadyClicked = false;
                    Fc.i(mContext,TAG,"setSettingsControl", "button.settings.control." + startStopType.getId().toString() + " : " + isEnabledForStartStopType(startStopType) + " -> " + enabled);
                    limitExec++;
                    if (limitExec > settingsLimitExec || settingsIsCompleted) {
                        Fc.i(mContext,TAG,"setSettingsControl", "button.settings.control." + startStopType.getId().toString() + ".limit.loop." + limitExec);
                        break;
                    }
                    // if ( "com.android.settings".equals(event.getPackageName()) && nodeInfo != null ) {
                    // }
                    if (rootNode != null) {
                        clickedSwitch = enabled == isEnabledForStartStopType(startStopType);
                        //                    rootNode.findAccessibilityNodeInfosByText("Wi-Fi").get(0).getParent().getChild(1)
                        //                    rootNode.findAccessibilityNodeInfosByText("모바일 데이터").get(0).getParent().getChild(1)
                        //                    rootNode.findAccessibilityNodeInfosByText("모바일 데이터").size()
                        //                    rootNode.findAccessibilityNodeInfosByText("모바일 데이터").get(0).getParent().getChildCount()
                        //                    rootNode.findAccessibilityNodeInfosByText("모바일 데이터").get(0).getParent().getChild(2)
                        //TODO SOFTM : 스위치가 하나만 존재할경우 올바르나. 두개 이상일 경우 문제발생.
                        // 텍스트를 찾은후, 부모에서 스위치를 찾는 로직.
                        AccessibilityNodeInfo switchButton = null;
                        if (!clickedSwitch) {
                            if (startStopType.equals(StartStopType.WIFI) || startStopType.equals(StartStopType.AIRPLANE_MODE)) {
                                switchButton = findButtonByClassName(rootNode, "android.widget.Switch");
                                if (switchButton != null) {
                                    switchButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    Fc.i(mContext,TAG,"setSettingsControl", "button.settings.control." + startStopType.getId().toString() + ".Switch Button Click-WIFI");
                                    Utils.sleepMicoSeconds(500);
                                    break;
                                }
                            } else { // Mobile Data. rootNode.findAccessibilityNodeInfosByText(findStr).get(0).getParent().getChildCount()
                                for (String findStr : stringsXmlValues) {
                                    List<AccessibilityNodeInfo> findRootNodes = new ArrayList<>();
                                    //TODO SOFTM getText() 값이 null이면 스위치를 포함한 노드가 아님.
                                    for (AccessibilityNodeInfo node : rootNode.findAccessibilityNodeInfosByText(findStr)) {
                                        if (node.getText() != null) {
                                            findRootNodes.add(node);
                                        }
                                    }
                                    Fc.i(mContext,TAG,"setSettingsControl","startStopType : ", startStopType.getId().toString());

                                    if (findRootNodes.size() > 0) {
                                        AccessibilityNodeInfo findRootNode = findRootNodes.get(findRootNodes.size() - 1);
                                        AccessibilityNodeInfo group = findRootNode.getParent();
                                        switchButton = group.getChild(group.getChildCount() - 1);
                                        if (switchButton != null) {
                                            if (switchButton.isClickable()) {
                                                switchButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                                Fc.i(mContext,TAG, "setSettingsControl","button.settings.control." + startStopType.getId().toString() + "[" + findStr + "].Switch Button Click");
                                                clickedSwitch = true;
                                                Utils.sleepMicoSeconds(800);
                                                break;
                                            }
                                        }
                                    } else {
                                        Fc.i(mContext,TAG,"setSettingsControl","버튼(스위치)를 찾지 못함");
                                        logViewHierarchy(rootNode, 0);
                                    }
                                }
                            }
                        }

                        if (clickedSwitch) {
                            break;
                        }
                    }
                    //  Utils.sleepMicoSeconds(250);
                }

                //            Utils.sleepMicoSeconds(500);
                for (String doneStr : doneInfos) {
                    //                    rootNode = mAcsService.getRootInActiveWindow();
                    // if ( "com.android.settings".equals(event.getPackageName()) && nodeInfo != null ) {
                    // }
                    if (rootNode != null) {
                        //                  AccessibilityNodeInfo buttonNode = findButtonByText(rootNode, doneStr);
                        //                  List<AccessibilityNodeInfo> buttonNodes = rootNode.findAccessibilityNodeInfosByText(doneStr);
                        for (AccessibilityNodeInfo buttonNode : rootNode.findAccessibilityNodeInfosByText(doneStr)) {
                            if (buttonNode != null) {
                                clickedDone = buttonNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                Fc.i(mContext,TAG,"setSettingsControl", "    button.settings.control." + startStopType.getId().toString() + ".Done Button Click");
                                Utils.sleepMicoSeconds(500);
                                //                            clickedDone = true;
                                break;
                            }
                        }
                        if (clickedDone) break;
                    }
                }

                if (enabled == isEnabledForStartStopType(startStopType)) {
                    if (checkerForDoneHandler != null) {
                        checkerForDoneHandler.removeCallbacksAndMessages(null);
                        checkerForDoneHandler = null;
                        Fc.i(mContext,TAG,"setSettingsControl", "button.settings.settings." + startStopType.getId().toString() + ".stop.recallHandler : " + isEnabledForStartStopType(startStopType) + " -> " + enabled);
                    }
                    Fc.i(mContext,TAG, "setSettingsControl", "button.settings.control." + startStopType.getId().toString() + ".clicked." + (isAlreadyClicked ? "already." : "") + "completed : " + isEnabledForStartStopType(startStopType) + " -> " + enabled);
                    if (!settingsIsCompleted) {
                        Fc.i(mContext,TAG,"setSettingsControl",  "button.settings.control." + startStopType.getId().toString() + ".clicked.completed.nextTask");
                        settingsIsCompleted = true;
                        if (workQueueNext) {
                            WorkQueue.nextTask();
                        }
                    }
                } else {
                    Fc.i(mContext,TAG,"setSettingsControl", "button.settings.control." + startStopType.getId().toString() + ".ing : " + isEnabledForStartStopType(startStopType) + " -> " + enabled);
                }
                /** EVENT_SETTINGS_SWITCH_CLICK 이벤트 처리를 접근성서비스 내부에서 안함으로 사용안함 */
                //        },settingsControlExecuteCallbackDelay);

                if (checkerForDoneHandler != null)
                    checkerForDoneHandler.removeCallbacksAndMessages(null);
                checkerForDoneHandler = (new Handler(Looper.getMainLooper()));
                checkerForDoneHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!settingsIsCompleted) {
                            if (settingsRecallCnt < settingsRecallLimit) {
                                settingsRecallCnt++;
                                sendEvent(KEY_GLOBAL_ACTION_BACK, () -> {
                                }, 0, 0);
                                Fc.i(mContext,TAG,"setSettingsControl", "button.settings.control." + startStopType.getId().toString() + ".recall[" + settingsRecallCnt + "]");
                                setSettingsControl(context, startStopType, enabled, workQueueNext);
                            } else {
                                settingsIsCompleted = true;
                                Fc.i(mContext,TAG,"setSettingsControl", String.format("button.settings.control.recallLimited.nextTask.startStopType %s", startStopType.getId().toString()));
                                Fc.i(mContext,TAG,"setSettingsControl", String.format("button.settings.control.recallLimited.nextTask.stringsXmlValues [%s]", String.format("%s", stringsXmlValues.isEmpty() ? "" : stringsXmlValues)));
                                if (workQueueNext) {
                                    WorkQueue.nextTask();
                                }
                            }
                        }
                    }
                }, settingsRecallDelay);
            }, 0, 500);
        } catch (Exception ex) {
            Fc.e(mContext,TAG,"setSettingsControl",ex.toString());
        }
    } // end setSettingsControl

    public boolean isEnabledForStartStopType(StartStopType startStopType) {
        if ( startStopType.equals(StartStopType.WIFI) ) {
            return DeviceUtil.isWifiEnable(mContext);
        } else if ( startStopType.equals(StartStopType.AIRPLANE_MODE) ) {
            return DeviceUtil.isAirplaneModeOn(mContext);
        } else if ( startStopType.equals(StartStopType.TETHERING) ) {
            return DeviceUtil.isApOn(mContext);
        } else if ( startStopType.equals(StartStopType.MOBILE_DATA) ) {
            return Connectivity.isMobileDataConnected(mContext);
        } else if ( startStopType.equals(StartStopType.BLUETOOTH) ) {
            return DeviceUtil.isBluetoothEnable(mContext);
        }
        return false;
    }
    //
    public void setQuickSettings(Context context, StartStopType startStopType, boolean enabled) {
        setQuickSettings(context,startStopType,enabled,false);
    }

    Handler checkerForDoneHandler = null;
    boolean settingsIsCompleted = false;

    public final static long settingsLimitExec = 10L;
    public final static long settingsRecallDelay = 3000L;
    public final static int settingsExecuteCallbackDelay = 500;
    public final static int settingsControlExecuteCallbackDelay = 1000;
    public final static int forceStopCallbackDelay = 500;
    public final static int settingsCompleteCallbackDelay = 500;
    public final static int settingsControlCompleteCallbackDelay = 500;
    public static long settingsRecallCnt = 0L;
    public final static long settingsRecallLimit = 3L;

    public synchronized void setQuickSettings(Context context, StartStopType startStopType, boolean enabled, boolean workQueueNext) {
        try {
            Fc.i(mContext,TAG, "setQuickSettings", "button.quick.setting." + startStopType.getId().toString() + ".start : " + isEnabledForStartStopType(startStopType) + " -> " + enabled);

            Fc.i(mContext,TAG,"setQuickSettings");

            if ( !PermissionUtil.isAccessibilityServiceRunning(mContext) ) {
                if (workQueueNext) {
                    Fc.i(mContext,TAG, "setQuickSettings","AcesssibilityService.not.running.nextTask");
                    WorkQueue.nextTask();
                }
                return;
            }

            //        if ( eventCallbackQueues.get(EVENT_QUICK_SETTINGS_CLICK) != null ) return;
            if (enabled == isEnabledForStartStopType(startStopType)) {
                Fc.i(mContext,TAG, "setQuickSettings","button.quick.setting." + startStopType.getId().toString() + " : " + enabled + " is same " + isEnabledForStartStopType(startStopType));

                if (!settingsIsCompleted) {
                    Fc.i(mContext,TAG,"setQuickSettings", "button.quick.setting." + startStopType.getId().toString() + ".is same.nextTask");
                    sendEvent(KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE, () -> {
                        if (checkerForDoneHandler != null) {
                            checkerForDoneHandler.removeCallbacksAndMessages(null);
                            checkerForDoneHandler = null;
                            Fc.i(mContext,TAG,"setQuickSettings", "button.quick.setting." + startStopType.getId().toString() + ".stop.same.recallHandler : " + isEnabledForStartStopType(startStopType) + " -> " + enabled);
                        }
                        if (workQueueNext) {
                            Fc.i(mContext,TAG,"setQuickSettings", "button.quick.setting." + startStopType.getId().toString() + ".WorkQueue.nextTask()");
                            WorkQueue.nextTask();
                        }
                        settingsIsCompleted = true;
                    }, 0, settingsCompleteCallbackDelay);
                }
                return;
            } else {
                settingsIsCompleted = false;
            }

            sendEvent(KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE, new RunnableArg() {
                @Override
                public void run() {
//                Utils.toast(mContext,"Callback Test");
                    sendEvent(KEY_GLOBAL_ACTION_QUICK_SETTINGS, new RunnableArg() {
                        @Override
                        public void run() {
                            boolean clicked = false;
                            List<String> stringsXmlValues = DeviceUtil.getStringsXmlValuesForStartStopType(mContext, startStopType);
                            int limitExec = 0;
                            while (!settingsIsCompleted && enabled != isEnabledForStartStopType(startStopType)) {
//                            Utils.sleepMicoSeconds(250);
                                Fc.i(mContext,TAG,"setQuickSettings", "button.quick.setting." + startStopType.getId().toString() + " : " + isEnabledForStartStopType(startStopType) + " -> " + enabled);
                                limitExec++;
                                if (limitExec > settingsLimitExec || settingsIsCompleted) {
                                    Fc.i(mContext,TAG,"setQuickSettings", "button.quick.setting." + startStopType.getId().toString() + ".limit.loop." + limitExec);
                                    break;
                                }

                                AccessibilityNodeInfo rootNode = mAcsService.getRootInActiveWindow();
                                if (rootNode != null) {
                                    //                rootNode.findAccessibilityNodeInfosByText("Airplane mode");
                                    AccessibilityNodeInfo sysUiNodeInfo = null;
                                    //                findSwitchByText(rootNode,findStr)
                                    //                            logViewHierarchy(rootNode.getChild(),0)
                                    int idx = 0;
                                    Fc.i(mContext,TAG,"setQuickSettings", "button.quick.setting.finds => stringsXmlValues = [" + (stringsXmlValues.size() > 0 ? stringsXmlValues : "") + "]");
                                    for (String findStr : stringsXmlValues) {
                                        List<AccessibilityNodeInfo> childs = rootNode.findAccessibilityNodeInfosByText(findStr);
                                        boolean isUnableClick = true;
                                        for (AccessibilityNodeInfo child : childs) {
                                            if (child.isClickable()) isUnableClick = false;
                                        }
                                        for (AccessibilityNodeInfo child : childs) {
                                            sysUiNodeInfo = child;
                                            Fc.i(mContext,TAG,"setQuickSettings","isUnableClick", isUnableClick);
                                            if (isUnableClick) { // 클릭요소 없을경우 node영역 찾아 클릭
                                                if (enabled != isEnabledForStartStopType(startStopType)) {
//                                                sysUiNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                                    Rect bounds = findButtonAndPrintCoordinates(sysUiNodeInfo);
                                                    if (bounds != null) {
                                                        int centerX = (bounds.left + bounds.right) / 2;
                                                        int centerY = (bounds.top + bounds.bottom) / 2;
//                                                    Utils.sleepMicoSeconds(300);
//                                                        sendEvent(GESTURE_TAP, new String[]{String.valueOf(bounds.centerX()), String.valueOf(bounds.centerY()*0.95 )}, 0, new RunnableArg() {
                                                        // TODO SOFTM : ANR new RunnableArg() { --> () -> {
                                                        sendEvent(GESTURE_TAP, new String[]{String.valueOf(bounds.centerX()), String.valueOf(bounds.centerY()*0.95 )}, 0, () -> {
//                                                            @Override
//                                                            public void run() {
                                                            if (enabled == isEnabledForStartStopType(startStopType)) {

                                                            }
//                                                            }
                                                        }); // Click : GESTURE_TAP
                                                        // TODO SOFTM ACS Delay Point
                                                        Utils.sleepMicoSeconds(800); // 딜레이 줘야 1~2번에 클릭됨.
                                                        Fc.i(mContext,TAG,"setQuickSettings","button.quick.setting.rect.click." + startStopType.getId().toString() + "[" + findStr + "](" + idx + ")[" + centerX + "," + centerY + "].click : " + isEnabledForStartStopType(startStopType) + " -> " + enabled);
                                                        Fc.i(mContext,TAG,"setQuickSettings", "rec.click");
                                                        Fc.i(mContext,TAG,"setQuickSettings", String.format("AcsUtil.setQuickSettings. : %s", sysUiNodeInfo.getText()));
                                                        Fc.i(mContext,TAG,"setQuickSettings","button.quick.setting.rect.click." + startStopType.getId().toString() + "[" + findStr + "](" + idx + ")[" + centerX + "," + centerY + "].click : " + isEnabledForStartStopType(startStopType) + " -> " + enabled);
                                                    }
                                                }
                                            } else {
                                                if (sysUiNodeInfo.isClickable()) { // && sysUiNodeInfo.isCheckable()
                                                    if (enabled != isEnabledForStartStopType(startStopType) &&
                                                            child.isFocusable() &&
                                                            (
                                                                    (
                                                                            child.isCheckable() && enabled != child.isChecked()
                                                                    ) ||
                                                                            !child.isCheckable()
                                                            )
                                                        //                                                    && findStr.equals(sysUiNodeInfo.getContentDescription())
                                                    ) { // 숨어있거나, 시스템 노드로 여러개가 존재하여.
                                                        // child가 여러개 일 경우 누른후 다시 변경하는것을 방지함.
                                                        Fc.i(mContext,TAG,"setQuickSettings","sysUiNodeInfo.performAction", "button.quick.setting." + startStopType.getId().toString() + "[" + findStr + "](" + idx + ").click : " + isEnabledForStartStopType(startStopType) + " -> " + enabled);
                                                        sysUiNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                                        Utils.sleepMicoSeconds(300);
                                                        Fc.i(mContext,TAG, "setQuickSettings", "button.quick.setting." + startStopType.getId().toString() + "[" + findStr + "](" + idx + ").click : " + isEnabledForStartStopType(startStopType) + " -> " + enabled);
                                                        //                                                break;
                                                    } else {
                                                        Utils.sleepMicoSeconds(300);
                                                    }
                                                }
                                            }
                                            idx++;
                                        }
                                    }
                                }
//                            Utils.sleepMicoSeconds(250);
                            }

                            Fc.i(mContext,TAG, "setQuickSettings","button.quick.setting." + startStopType.getId().toString() + ".ing : " + isEnabledForStartStopType(startStopType) + " -> " + enabled);

                            if (enabled == isEnabledForStartStopType(startStopType)) {
                                if (checkerForDoneHandler != null) {
                                    checkerForDoneHandler.removeCallbacksAndMessages(null);
                                    checkerForDoneHandler = null;
                                    Fc.i(mContext,TAG, "setQuickSettings","button.quick.setting." + startStopType.getId().toString() + ".remove checkerForDoneHandler : " + isEnabledForStartStopType(startStopType) + " -> " + enabled);
                                    sendEvent(KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE, 500); // 노티가 유지되는 경우 발생으로 한번더 없어지게 실행함
                                }
                                sendEvent(KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE, () -> {
//                        sendEvent(KEY_GLOBAL_ACTION_BACK, () -> {
//                            sendEvent(KEY_GLOBAL_ACTION_BACK, () -> {
                                    if (!settingsIsCompleted) {
                                        settingsIsCompleted = true;
                                        Fc.i(mContext,TAG, "setQuickSettings", "button.quick.setting." + startStopType.getId().toString() + ".clicked.completed.nextTask");
                                        if (workQueueNext) {
                                            WorkQueue.nextTask();
                                        }
                                    }
                                }, 0, settingsCompleteCallbackDelay);
//                            }
                            }
                            if (checkerForDoneHandler != null)
                                checkerForDoneHandler.removeCallbacksAndMessages(null);

                            checkerForDoneHandler = (new Handler(Looper.getMainLooper()));
                            checkerForDoneHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!settingsIsCompleted) {
//                    sendEvent(KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE, () -> {
//                    },0,0);
                                        if (settingsRecallCnt < settingsRecallLimit) {
                                            settingsRecallCnt++;
                                            Fc.i(mContext,TAG, "setQuickSettings", "button.quick.setting." + startStopType.getId().toString() + ".recall[" + settingsRecallCnt + "]");
                                            setQuickSettings(context, startStopType, enabled, workQueueNext);
                                        } else {
                                            settingsIsCompleted = true;
                                            Fc.i(mContext,TAG, "setQuickSettings", String.format("button.quick.setting.recallLimited.nextTask.startStopType %s", startStopType.getId().toString()));
                                            Fc.i(mContext,TAG, "setQuickSettings", String.format("button.quick.setting.recallLimited.nextTask.stringsXmlValues [%s]", String.format("%s", stringsXmlValues.isEmpty() ? "" : stringsXmlValues)));
                                            sendEvent(KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE, () -> {
                                            }, 0, 0);
                                            if (workQueueNext) {
                                                WorkQueue.nextTask();
                                            }
                                        }
                                    }
                                }
//      }, settingsRecallDelay+100000000);
                            }, settingsRecallDelay); // 기존에 settingsRecallDelay+100000000 로직으로 실행이 안된다고 보면됨.
                            // TODO SOFTM : 환경설정에서 리콜딜레이 타임을 설정하면 좋을 듯.
                        }
                    }, 0, settingsExecuteCallbackDelay);
//                sendEvent(EVENT_QUICK_SETTINGS_CLICK,new RunnableArg() {
//                    @Override
//                    public void run() {
//
//                    }
//                },true);
                }
            }, 0, 500);
        } catch (Exception ex) {
            Fc.e(mContext,TAG,"setQuickSettings",ex.toString());
        }
    } // end setQuickSettings


    public void registerReceiver() {
        sendEvent(EVENT_REGISTER_RECEIVER);
    } // end register receiver

    public void unregisterReceiver() {
        sendEvent(EVENT_UNREGISTER_RECEIVER);
    } // end register receiver

    public void sendEvent(String gubun, Runnable runnable, int eventDelaytime) {
        sendEvent(gubun,null,eventDelaytime,runnable,0);
    }
    public void sendEvent(String gubun, Runnable runnable, int eventDelaytime, int callbackDelaytime) {
        sendEvent(gubun,null,eventDelaytime,runnable,callbackDelaytime);
    }

    private void sendEvent(String gubun, int eventDelaytime, Runnable runnable, int callbackDelaytime) {
        sendEvent(gubun,null,eventDelaytime,runnable,callbackDelaytime);
    }

    private void sendEvent(String gubun, String[] text, int eventDelaytime, Runnable runnable, int callbackDelaytime) {
        sendEvent(gubun,text,eventDelaytime,runnable,callbackDelaytime,false);
    }
    private void sendEvent(String gubun, String[] texts, int eventDelaytime,Runnable runnable) {
        sendEvent(gubun,texts,eventDelaytime, runnable,0,false);
    }
    private void sendEvent(String gubun, String[] texts, int eventDelaytime, Runnable runnable, int callbackDelaytime, boolean onlyAddQueue) {
        if ( PermissionUtil.isAccessibilityServiceRunning(mContext) ) {
            BlockingQueue queue = new LinkedBlockingQueue<>();
            queue.offer(runnable);
            eventCallbackQueues.put(gubun, queue);
            eventCallbackDelayTimes.put(gubun, callbackDelaytime);
            eventCallbackOnlyAddQueue.put(gubun, onlyAddQueue);
//        String[] newTexts = null;
//        if ( texts != null ) {
//            newTexts = new String[texts.length + 1];
//            newTexts[0] = String.valueOf(onlyAddQueue);
//            for(int idx = 0; idx < texts.length; idx++) {
//                newTexts[idx+1] = texts[idx];
//            }
//        } else {
//            newTexts = new String[]{String.valueOf(onlyAddQueue)};
//        }

            sendEvent(gubun, REGISTER_QUEUE, texts, eventDelaytime);
        } else {
            Callbackkt callbackkt = new Callbackkt(eventDelaytime + callbackDelaytime,"AcsUtil.sendEvent('" + gubun + "')");
            callbackkt.run((param)->{
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(runnable);
                executor.shutdown();
                return null;
            });
        }
    }

    public void sendEvent(String gubun, Runnable runnable, boolean onlyAddQueue) {
        sendEvent(gubun, null,0,runnable,0,onlyAddQueue);
    }

    public void sendEvent(String gubun, Runnable runnable) {
        sendEvent(gubun, null,0,runnable,0);
    }

    public void sendEvent(String gubun) {
        sendEvent(gubun, (String) null, (String) null,0);
    }
    public void sendEvent(String gubun,String key) {
        sendEvent(gubun,key, (String) null, 0);

    }
    public void sendEvent(String gubun,String key,long delayTime) {
        sendEvent(gubun,key, (String) null, delayTime);
    }

    public void sendEvent(String gubun,long delayTime) {
        sendEvent(gubun, (String) null, (String) null,delayTime);
    }
    public void sendEvent(String gubun,String key,String text,long delayTime) {
        sendEvent(gubun,key,new String[]{text},delayTime);
    }
    public void sendEvent(String gubun,String key,String[] texts,long delayTime) {
        AccessibilityManager manager = (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);

        if (manager.isEnabled() && ConnectAccessibilityService.mContext != null) {
            AccessibilityEvent e = AccessibilityEvent.obtain();
            e.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
            e.setClassName(getClass().getName());
            e.setPackageName(mContext.getPackageName());
            e.setContentDescription(gubun);
            e.getText().add(key==null?"SOFTM ACS":key);
            currentEvent = gubun;
            if ( texts != null ) {
                for (String text : texts) {
                    if (text != null) e.getText().add(text);
                }
            }
            if ( delayTime == 0 ) {
                manager.sendAccessibilityEvent(e);
            } else {
/** ANR 발생 으로 슬립걸고 그냥 호출 : 202409212044 */

                Utils.sleepMicoSeconds(delayTime > 0 ? delayTime : 0);
                manager.sendAccessibilityEvent(e);

                /** ANR 발생 Handler.postDelayed 제거
                 (new Handler(Looper.getMainLooper())).postDelayed(new Runnable() {
                @Override
                public void run() {
                manager.sendAccessibilityEvent(e);
                }
                //                }, delayTime >= 0 ? SEND_EVENT_DELAY_TIME + delayTime : SEND_EVENT_DELAY_TIME);
                 }, delayTime > 0 ? delayTime : 0);
                 */

//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        manager.sendAccessibilityEvent(e);
//                    }
//                }, delayTime >= 0 ? SEND_EVENT_DELAY_TIME + delayTime : SEND_EVENT_DELAY_TIME);
////                }, delayTime >= 0 ? delayTime : SEND_EVENT_DELAY_TIME);

            }
        } else {
//            WorkerExeckt.delayCallback((params) -> {
//                return null;
//            },delayTime);
        }
    }

    public void sendEventWaitingOverlay(String action, ReceiverType receiverType, long waitingSec,Runnable runnable) {
//        sendEvent(SHOW_WAITING_OVERLAY,null, new String[]{action,receiverType.getId(),String.valueOf(waitingSec)},0);
        sendEvent(SHOW_WAITING_OVERLAY, new String[]{action,receiverType.getId(),String.valueOf(waitingSec)}, 0, runnable, (int) waitingSec*1000);
    }

    public void sendEventWaitingOverlay(String action, ReceiverType receiverType, long waitingSec,String adviewUserEarnedReward, Runnable runnable) {
//        sendEvent(SHOW_WAITING_OVERLAY,null, new String[]{action,receiverType.getId(),String.valueOf(waitingSec)},0);
        sendEvent(SHOW_WAITING_OVERLAY, new String[]{action,receiverType.getId(),String.valueOf(waitingSec),String.valueOf(adviewUserEarnedReward)}, 0, runnable, (int) waitingSec*1000);
    }

    public void sendEventWaitingOverlay(String action, ReceiverType receiverType, long waitingSec,String adviewUserEarnedReward,String doAppReview, Runnable runnable) {
//        sendEvent(SHOW_WAITING_OVERLAY,null, new String[]{action,receiverType.getId(),String.valueOf(waitingSec)},0);
        sendEvent(SHOW_WAITING_OVERLAY, new String[]{action,receiverType.getId(),String.valueOf(waitingSec),String.valueOf(adviewUserEarnedReward),String.valueOf(doAppReview)}, 0, runnable, (int) waitingSec*1000);
    }

    public void sendEventWaitingOverlayUpdateStatus(String action,  ReceiverType receiverType, long delayTime) {
        sendEvent(SHOW_WAITING_OVERLAY_UPDATE_STATUS,null,new String[]{action,receiverType.getId()},delayTime);
    }

    private boolean isAvaliableClick = true;
    private Runnable mRunnable = null;
    public static BlockingQueue<RunnableArg> execQueue = null;

    /**
     *
     * @param event
     * @param delayTime
     * @param runnable
     *
     * using
     *
     *
    //            if ( EVENT_QUICK_SETTINGS_CALL_CLICK.equals(eventContentDescription) ) {
    ////                if ( "com.android.systemui".equals(event.getPackageName()) ) {
    ////                    acsUtil = new AcsUtil(this);
    //                    AccessibilityNodeInfo source = null;
    //                    acsUtil.exec(event,0,()->{
    //                        AccessibilityNodeInfo rootNode = mContext.getRootInActiveWindow();
    //                        if (rootNode != null) {
    //                            rootNode.findAccessibilityNodeInfosByText("Airplane mode");
    //                            AccessibilityNodeInfo sysUiNodeInfo = null;
    //                            for ( AccessibilityNodeInfo child : rootNode.findAccessibilityNodeInfosByText("Airplane mode") ) {
    //                                if ( child.getClassName().equals("android.widget.Switch") && child.getContentDescription().equals("Airplane mode")) {
    //                                    sysUiNodeInfo = child;
    //                                    break;
    //                                }
    //                            }
    //                            if ( sysUiNodeInfo != null ) {
    //                                Utils.toast(mContext,"findNode");
    //                                sysUiNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    //
    //                                acsUtil.sendEvent(KEY_GLOBAL_ACTION_BACK,100);
    //                                acsUtil.sendEvent(KEY_GLOBAL_ACTION_BACK,500);
    ////                                Utils.sleepMicoSeconds(500);
    ////                                acsUtil.sendEvent(KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE,"SOFTM GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE");
    ////                                acsUtil.completeRunnable();
    //                            }
    //                        }
    //                    });
    ////                }
    //            }
     */
    public void exec(AccessibilityEvent event, int delayTime, RunnableArg runnable) {

        execQueue = new LinkedBlockingQueue<>();
        int eventType = event.getEventType();
//        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//            if (isAvaliableClick) {
        Handler rHandler = new Handler(Looper.getMainLooper());
//                isAvaliableClick = false;
        rHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Fc.i(mContext,TAG, "exec","reset isAvaliableClick");
                execQueue.offer(runnable);
                sendEvent(EVENT_DUMMY);
//                        isAvaliableClick = true;
            }
        }, delayTime);
//            }
//        }
    }

    public void  performRunnable(){
        try {
            if ( !execQueue.isEmpty() ) {
                Runnable task = execQueue.take();
                Thread thread = new Thread(task);
                thread.start();
                acsEnteredTime = 0;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void register(String gubun, AccessibilityEvent event) {
//        if ( execQueues.get(gubun) != null ) return;

        BlockingQueue<RunnableArg> eventQueue = eventCallbackQueues.get(gubun);

        final AccessibilityNodeInfo[] nodeInfo = {event.getSource()};
        List<CharSequence> keys = event.getText();

        if ( eventQueue != null ) {

            Handler rHandler = new Handler(Looper.getMainLooper());
            rHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    sendEventImmediate(EVENT_REGISTER_CALLBACK,new String[]{gubun});

                    BlockingQueue<RunnableArg> eventQueue = eventCallbackQueues.get(gubun);

                    try {
//                        if (eventQueue != null) {
                        RunnableArg task = null;
                        Runnable taskRunnable = null;
                        if ( eventQueue!=null && !eventQueue.isEmpty() ) {
                            if (eventQueue.peek() instanceof RunnableArg) {
                                task = eventQueue.take();
                                BlockingQueue queue = new LinkedBlockingQueue<>();
                                queue.offer(task);
                                execQueues.put(gubun, queue);

                                Fc.i(mContext,TAG,"register", "RunnableArg.register(" + gubun + ") : size : " + eventQueue.size());
                            } else {
                                taskRunnable = eventQueue.take();
                                BlockingQueue queue = new LinkedBlockingQueue<>();
                                queue.offer(taskRunnable);
                                execQueues.put(gubun, queue);
                                Fc.i(mContext,TAG,"register", "Runnable.register(" + gubun + ") : size : " + eventQueue.size());
                            }
                        }
                        if ( eventCallbackOnlyAddQueue.get(gubun) != null && !eventCallbackOnlyAddQueue.get(gubun) ) {
                            Fc.i(mContext,TAG,"register", "Runnable.register.call.trigger(" + gubun + ")" );
                            Handler rHandler = new Handler(Looper.getMainLooper());
                            rHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    sendEvent(EVENT_REGISTER_CALLBACK_TRIGGER);
                                }
                            }, 500);

                        } else {
                            Fc.i(mContext,TAG,"register", "Runnable.register.not.call.trigger(" + gubun + ")" );
                        }
//
//                        }
                        eventCallbackQueues.remove(gubun);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }, eventCallbackDelayTimes.get(gubun));
        }
    }


    public void handleForceStopDialogOk(AccessibilityEvent event){
        if ( AcsUtil.isValidForceButtonClicked() ) {
            AccessibilityNodeInfo nodeInfo = event.getSource();
            if ( "com.android.settings".equals(event.getPackageName()) && nodeInfo != null ) {
                int eventType = event.getEventType();
                if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    if (isAvaliableClick) {
                        Handler rHandler = new Handler(Looper.getMainLooper());
                        isAvaliableClick = false;
                        rHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Fc.i(mContext,TAG,"handleForceStopDialogOk", "ForceStopOkClick");
                                isAvaliableClick = true;
                            }
                        }, AcsUtil.FORCE_STOP_OK_PREVENT_INTERVAL_TIME);

                        Fc.i(mContext,TAG,"FORCE_STOP_OK_PREVENT_INTERVAL_TIME : " + System.currentTimeMillis());
                        Fc.i(mContext,TAG,"(AcsUtil.appDetailSettinsForceClickedTime + SEND_EVENT_DELAY_TIME) + \" / \" + System.currentTimeMillis() : " + (AcsUtil.appDetailSettingsForceClickedTime + SEND_EVENT_DELAY_TIME) + " / " + System.currentTimeMillis());

                        if ( AcsUtil.appDetailSettingsForceClickedTime + SEND_EVENT_DELAY_TIME + 3000 > System.currentTimeMillis()) {
                            //                                                                  3000 보정.
                            if ( MapUtil.isNaviApp() ) {
                                Utils.toast(mContext, R.string.force_stop_navi);
                            } else {
                                Utils.toast(mContext,R.string.force_stop_app);
                            }
                            Fc.i(mContext,TAG,"Click OK");
                            invokeForceStopOkClick(nodeInfo);
                        } else {
                            Fc.i(mContext,TAG,"Click NOT OK");
                        }
                    }
                }
            }
        }
    }

//    public void completeRunnable(){
//        mRunnable = null;
//    }

    private Rect findButtonAndPrintCoordinates(AccessibilityNodeInfo node) {
        if (node == null) return null;

        // 현재 노드가 버튼인지 확인 (클래스 이름으로 확인)
//        if ("android.widget.Button".equals(node.getClassName())) {
        // 버튼의 절대 좌표 가져오기
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);

        // 좌표 출력
        Fc.i(mContext,TAG,"findButtonAndPrintCoordinates", "Button found at: " +
                "Left: " + bounds.left +
                ", Top: " + bounds.top +
                ", Right: " + bounds.right +
                ", Bottom: " + bounds.bottom);
//        }
        return bounds;
    }

    public synchronized void safeDrive(Context context, String text, String description, int execLimit, boolean workQueueNext) {
        try {

            Fc.i(mContext,TAG,"safeDrive" + String.format("[%s]",text) + String.format("[%s]",description));
            sendEvent(KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE, () -> {
                PackageManager pm = mContext.getPackageManager();
                Intent it = pm.getLaunchIntentForPackage(MapUtil.getAppPackage()); // "com.skt.skaf.l001mtm091"
                //        it.setData(Uri.parse(uri));
                //        Intent it  = new Intent(Intent.ACTION_VIEW,Uri.parse(uri));
                it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                mContext.startActivity(it);

                boolean clicked = false;
                AccessibilityNodeInfo rootNode = mAcsService.getRootInActiveWindow();
                int limitExec = 0;
                while ( !clicked ) {
                    Utils.sleepMicoSeconds(500);
                    rootNode = mAcsService.getRootInActiveWindow();
                    limitExec++;
                    if (limitExec > execLimit || clicked) {
                        Fc.i(mContext,TAG,"safeDrive" + String.format("[%s]",text) + String.format("[%s]",description), ".limit.loop." + limitExec);
                        break;
                    }
                    clicked = nodeClick(context, description, text,  workQueueNext);

                    if (clicked) {
                        break;
                    }
                }

                if (workQueueNext) {
                    Fc.i(mContext,TAG, "safeDrive" + String.format("[%s]",text) + String.format("[%s]",description) + (clicked?".clicked":".notClicked") + ".completed.nextTask");
                    WorkQueue.nextTask();
                }

            }, 0, 300);
        } catch (Exception ex) {
            Fc.e(mContext,TAG,"safeDrive" + String.format("[%s]",text) + String.format("[%s]",description),ex);
        }
    } // end safeDrive

    public synchronized Rect nodeRectClick(Context context, AccessibilityNodeInfo node) {
        Rect bounds = findButtonAndPrintCoordinates(node);
        if (bounds != null) {
            int centerX = (bounds.left + bounds.right) / 2;
            int centerY = (bounds.top + bounds.bottom) / 2;
            sendEvent(GESTURE_TAP, new String[]{String.valueOf(bounds.centerX()), String.valueOf(bounds.centerY() * 0.95)}, 0, () -> {
                // Perform the click action
                appDetailSettingsForceClickedOkTime = System.currentTimeMillis();
            }); // Click : GESTURE_TAP

            // TODO SOFTM : 버튼의 위치가 상이한 조건에대해서 보정값을 뺀 위치로 한번더 탭을 전송함
            sendEvent(GESTURE_TAP, new String[]{String.valueOf(bounds.centerX()), String.valueOf(bounds.centerY())}, 0, () -> {
                // Perform the click action
                appDetailSettingsForceClickedOkTime = System.currentTimeMillis();
            }); // Click : GESTURE_TAP
            // TODO SOFTM ACS Delay Point
            Utils.sleepMicoSeconds(800); // 딜레이 줘야 1~2번에 클릭됨.
        }
        return bounds;
    }

    public synchronized boolean nodeClick(Context context, String text, String description, boolean workQueueNext) {
        boolean clicked = false;
        try {

            Fc.i(mContext,TAG,"nodeClick" + String.format("[%s]",text) + String.format("[%s]",description));

            appDetailSettingsEnteredTime = System.currentTimeMillis();

            AccessibilityNodeInfo rootNode = mAcsService.getRootInActiveWindow();
            int limitExec = 0;
            Utils.sleepMicoSeconds(250);

            if (rootNode != null) {
                if (  Utils.isNotEmpty(text) ) {
                    List<AccessibilityNodeInfo> findNodes = null;
                    findNodes = findNodesByText(rootNode, text);
                    for (AccessibilityNodeInfo node : findNodes) {
                        if (node != null) {
                            clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            Fc.i(mContext, TAG, "nodeClick" + String.format("[%s]", text), ".click1");
                            if (clicked) break;
                            Utils.sleepMicoSeconds(300);
                            if (!clicked) {
                                (new Callkt("nodeClick.nodeRectClick")).run((param2) -> {
                                    Rect bounds = nodeRectClick(context, node);
                                    if (bounds != null) {
                                        int centerX = (bounds.left + bounds.right) / 2;
                                        int centerY = (bounds.top + bounds.bottom) / 2;
                                        Fc.i(mContext, TAG, "nodeClick" + String.format("[%s]", text), ".Rect", "[" + centerX + "," + centerY + "].click1");
                                    }
                                    return null;
                                });
                            }
                        }

                    }
                }
                if ( !clicked && Utils.isNotEmpty(description) ) {
                    List<AccessibilityNodeInfo> findNodes2 = null;
                    findNodes2 = findNodesByDescription(rootNode,description);
                    for ( AccessibilityNodeInfo node: findNodes2 ) {
                        if (node != null) {
                            clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            Fc.i(mContext, TAG, "nodeClick" + String.format("[%s]", description), ".click2");
                            if ( clicked ) break;
                            Utils.sleepMicoSeconds(300);
                            if (!clicked) {
                                (new Callkt("nodeClick.nodeRectClick2")).run((param2) -> {
                                    Rect bounds = nodeRectClick(context,node);
                                    if (bounds != null) {
                                        int centerX = (bounds.left + bounds.right) / 2;
                                        int centerY = (bounds.top + bounds.bottom) / 2;
                                        Fc.i(mContext, TAG, "nodeClick" + String.format("[%s]", description), ".Rect", "[" + centerX + "," + centerY + "].click2");
                                    }
                                    return null;
                                });
                            }
                        }
                    }
                }
            }

            if (workQueueNext) {
                Fc.i(mContext,TAG, "nodeClick" +String.format("[%s]",text) + String.format("[%s]",description)+ (clicked?".clicked":".notClicked") + ".completed.nextTask");
                WorkQueue.nextTask();
            }

        } catch (Exception ex) {
            Fc.e(mContext,TAG,"nodeClick"+String.format("[%s]",text) + String.format("[%s]",description),ex);
        }
        return clicked;
    } // end nodeClick

}

