package net.softm.data.kata;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler
{
    private final Context myContext;
    private final String LINE_SEPARATOR = "\n";

    public ExceptionHandler(Context context) {
        myContext = context;
    }
    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {

        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));

        StringBuilder errorReport = new StringBuilder();

        errorReport
                .append("************ CAUSE OF ERROR ************\n\n")
                .append(stackTrace.toString())
                .append("\n************ DEVICEINFORMATION ***********\n")
                .append("Brand: ").append(Build.BRAND)
                .append(LINE_SEPARATOR)
                .append("Device: ").append(Build.DEVICE)
                .append(LINE_SEPARATOR)
                .append("Model: ").append(Build.MODEL)
                .append(LINE_SEPARATOR)
                .append("Id: ").append(Build.ID)
                .append(LINE_SEPARATOR)
                .append("Product: ").append(Build.PRODUCT)
                .append(LINE_SEPARATOR)
                .append("\n************FIRMWARE ************\n")
                .append("SDK: ").append(Build.VERSION.SDK)
                .append(LINE_SEPARATOR)
                .append("Release:").append(Build.VERSION.RELEASE)
                .append(LINE_SEPARATOR)
                .append("Incremental: ").append(Build.VERSION.INCREMENTAL)
                .append(LINE_SEPARATOR);

//        Intent intent = new Intent(myContext, AnotherActivity.class);
//        intent.putExtra("error",errorReport.toString());
//        myContext.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

}
