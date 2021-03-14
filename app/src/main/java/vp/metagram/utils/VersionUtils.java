package vp.metagram.utils;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import static vp.metagram.utils.VersionUtils.LanguageType.persian;


public class VersionUtils
{
    final static String appName_enName_en = "Metagram";

    final static String appName_faName_en = "Metagram";
    final static String appName_faName_fa = "متاگرام";

    final int noOfAppNoDigits = 3;
    final int noOfLanguagePartDigits = 4;
    final int noOfPaymentPartDigits = 4;
    final int noOfSourceVersionDigits = 5;


    String versionString;
    long versionNo;

    String appNoPartString;
    long appNoPartNo;

    String languagePartString;
    public long languagePartNo;

    String paymentPartString;
    long paymentPartNo;

    String sourceVersionString;
    long sourceVersionNo;

    String fullVersionString;

    ApplicationType applicationType;

    public List<LanguageType> supportedLanguages = new ArrayList<>();
    public List<PaymentType> supportedPayments = new ArrayList<>();


    public static VersionUtils createFromVersionString(String versionString)
    {
        VersionUtils versionUtils = new VersionUtils();

        String[] versionParts = versionString.split("\\.");

        versionUtils.appNoPartNo = Long.parseLong(versionParts[0]);
        versionUtils.appNoPartString = Long.toString(versionUtils.appNoPartNo);

        versionUtils.languagePartNo = Long.parseLong(versionParts[1]);
        versionUtils.languagePartString = Long.toString(versionUtils.languagePartNo);

        versionUtils.paymentPartNo = Long.parseLong(versionParts[2]);
        versionUtils.paymentPartString = Long.toString(versionUtils.paymentPartNo);

        versionUtils.sourceVersionNo = Long.parseLong(versionParts[3]);
        versionUtils.sourceVersionString = Long.toString(versionUtils.sourceVersionNo);

        versionUtils.applicationType = ApplicationType.createFromOrdinal((int)versionUtils.appNoPartNo);

        versionUtils.versionString = versionString;

        versionUtils.createLanguageList(versionUtils.languagePartNo);
        versionUtils.createPaymentList(versionUtils.paymentPartNo);
        versionUtils.createFullVersion();
        versionUtils.createVersionNo();


        return versionUtils;
    }

    public static VersionUtils createFromVersionNo(long versionNo)
    {
        VersionUtils versionUtils = new VersionUtils();
        versionUtils.versionNo = versionNo;
        versionUtils.decodeVersionNo();

        versionUtils.applicationType = ApplicationType.createFromOrdinal((int)versionUtils.appNoPartNo);

        versionUtils.createLanguageList(versionUtils.languagePartNo);
        versionUtils.createPaymentList(versionUtils.paymentPartNo);
        versionUtils.createFullVersion();
        versionUtils.createVersionString();


        return versionUtils;
    }

    public String get_appName_en()
    {
        String result = "";
        if (languagePartNo == 1)
        {
            result = appName_enName_en;
        }
        else if ( languagePartNo == 2 )
        {
            result = appName_faName_en;
        }

        return result;
    }

    public String get_appName_fa()
    {
        String result = appName_faName_fa;

        return result;
    }

    public String getFullVersionString()
    {
        return fullVersionString;
    }

    public String getMinimisedVersionString()
    {
        return versionString;
    }

    public long getVersionNo()
    {
        return versionNo;
    }

    public void decodeVersionNo()
    {
        long tempNo = versionNo;
        long powerNo;
        long noOfDigits;
        long noOfDigitsPrevious;
        long powerNoPrevious;

        noOfDigits = noOfSourceVersionDigits;
        powerNo = (long)Math.pow(10,noOfDigits);

        sourceVersionNo = tempNo % powerNo;
        sourceVersionString = Long.toString(sourceVersionNo);

        tempNo = tempNo - (tempNo % powerNo);

        noOfDigitsPrevious = noOfDigits;
        noOfDigits = noOfSourceVersionDigits + noOfPaymentPartDigits;
        powerNoPrevious = (long)Math.pow(10,noOfDigitsPrevious);
        powerNo = (long)Math.pow(10,noOfDigits);

        paymentPartNo = (tempNo % powerNo)/powerNoPrevious;
        paymentPartString = Long.toString(paymentPartNo);

        noOfDigitsPrevious = noOfDigits;
        noOfDigits = noOfSourceVersionDigits + noOfPaymentPartDigits + noOfLanguagePartDigits;
        powerNoPrevious = (long)Math.pow(10,noOfDigitsPrevious);
        powerNo = (long)Math.pow(10,noOfDigits);

        languagePartNo = (tempNo % powerNo)/powerNoPrevious;
        languagePartString = Long.toString(languagePartNo);


        noOfDigitsPrevious = noOfDigits;
        noOfDigits = noOfSourceVersionDigits + noOfPaymentPartDigits + noOfLanguagePartDigits + noOfAppNoDigits;
        powerNoPrevious = (long)Math.pow(10,noOfDigitsPrevious);
        powerNo = (long)Math.pow(10,noOfDigits);

        appNoPartNo = (tempNo % powerNo)/powerNoPrevious;
        appNoPartString = Long.toString(appNoPartNo);
    }

    public void createFullVersion()
    {
        fullVersionString = String.format(Locale.ENGLISH,"%s.%s.%s.%s",
                addZeroBeforeNumber(appNoPartNo, noOfAppNoDigits),
                addZeroBeforeNumber(languagePartNo, noOfLanguagePartDigits),
                addZeroBeforeNumber(paymentPartNo, noOfPaymentPartDigits),
                addZeroBeforeNumber(sourceVersionNo, noOfSourceVersionDigits)
                );
    }

    public void createVersionString()
    {
        versionString = String.format(Locale.ENGLISH,"%s.%s.%s.%s",
                appNoPartNo,
                languagePartNo,
                paymentPartNo,
                sourceVersionNo
        );
    }

    public void createVersionNo()
    {
        versionNo = 0;

        versionNo += (long)Math.pow(10, noOfSourceVersionDigits + noOfPaymentPartDigits + noOfLanguagePartDigits) * appNoPartNo;
        versionNo += (long)Math.pow(10, noOfSourceVersionDigits + noOfPaymentPartDigits) * languagePartNo;
        versionNo += (long)Math.pow(10, noOfSourceVersionDigits) * paymentPartNo;
        versionNo += sourceVersionNo;
    }


    public void createLanguageList(long languagePartNo)
    {
        List<LanguageType> temp = new ArrayList<>(EnumSet.allOf(LanguageType.class));
        for (LanguageType languageType : temp)
        {
            if ((languagePartNo & languageType.getOrdinal()) == languageType.getOrdinal() )
            {
                supportedLanguages.add(languageType);
            }
        }
    }

    public void createPaymentList(long paymentPartNo)
    {
        List<PaymentType> temp = new ArrayList<>(EnumSet.allOf(PaymentType.class));
        for (PaymentType paymentType : temp)
        {
            if ((paymentPartNo & paymentType.getOrdinal()) == paymentType.getOrdinal() )
            {
                supportedPayments.add(paymentType);
            }
        }
    }

    public enum PaymentType
    {
        bit_coin(1),
        bazzar_rial(2),
        bank_zarrinpey_rial(4);

        long ordinal;

        PaymentType(long ordinal) { this.ordinal = ordinal;}

        public long getOrdinal()
        {
            return ordinal;
        }

        static PaymentType createFromOrdinal(long ordinal)
        {
            PaymentType result = null;
            for ( PaymentType paymentType : PaymentType.values() )
            {
                if ( paymentType.ordinal == ordinal )
                {
                    result = paymentType;
                    break;
                }
            }
            return result;
        }
    }

    public enum LanguageType
    {
        english(1),
        persian(2),
        malay(4),
        indonesian(8),
        arabic(16),
        portuguese(32),
        russian(64),
        hindi(128),
        turkish(256);

        long ordinal;

        LanguageType(long ordinal) { this.ordinal = ordinal;}

        public long getOrdinal()
        {
            return ordinal;
        }

        static LanguageType createFromOrdinal(long ordinal)
        {
            LanguageType result = null;
            for ( LanguageType languageType : LanguageType.values() )
            {
                if ( languageType.ordinal == ordinal )
                {
                    result = languageType;
                    break;
                }
            }
            return result;
        }
    }

    public enum ApplicationType
    {
        metagram_android(10);

        long ordinal;

        ApplicationType(long ordinal) { this.ordinal = ordinal;}

        public long getOrdinal()
        {
            return ordinal;
        }

        static ApplicationType createFromOrdinal(long ordinal)
        {
            ApplicationType result = null;
            for ( ApplicationType applicationType : ApplicationType.values() )
            {
                if ( applicationType.ordinal == ordinal )
                {
                    result = applicationType;
                    break;
                }
            }
            return result;
        }
    }

    public String addZeroBeforeNumber(long number, int noOfDigits)
    {
        String zeros = "";
        String numberStr = Long.toString(number);
        long someNumber = (long)Math.pow(10,(noOfDigits-1));

        while ( someNumber > 1 )
        {
            if (number > someNumber)
            {
                break;
            }
            else
            {
                zeros += "0";
                someNumber = someNumber / 10;
            }

        }

        return zeros + numberStr;
    }

    public long getSourceVersion()
    {
        return sourceVersionNo;
    }

    public String getDownloadLink()
    {
        String result = "";
        if (languagePartNo == persian.ordinal)
        {
            result = "https://cafebazaar.ir/app/nava.metagram/?l=fa";
        }

        return result;
    }

}
