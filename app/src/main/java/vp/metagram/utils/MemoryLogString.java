package vp.metagram.utils;

import java.util.Locale;

import static vp.metagram.general.functions.getGregorianDateTimeWithSecondsFromTimeStamp;

public class MemoryLogString
{
    long counter = 1;
    StringBuilder logBuilder = new StringBuilder();
    int MaxSize = 8 *1024;
    int chopSize = 2 * 1024;

    String divider = "--------------------";
    String datetime;

    public void append(String item)
    {
        datetime = getGregorianDateTimeWithSecondsFromTimeStamp(System.currentTimeMillis());
        if (logBuilder.length() + item.length() > MaxSize)
        {
            String log = logBuilder.toString();
            logBuilder = new StringBuilder(log.substring(chopSize,log.length()));
        }
        logBuilder.append(String.format(Locale.ENGLISH,"%s\n%s\n%s\n\n",datetime,item,divider));
        counter++;
    }

    public String read()
    {
        return logBuilder.toString();
    }

}
