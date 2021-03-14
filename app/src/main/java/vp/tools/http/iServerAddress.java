package vp.tools.http;

import android.text.TextUtils;

import java.util.Locale;

import static java.lang.String.format;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.executionMode;

public class iServerAddress
{
    private final String key = "ServerAddresses";
    private String[] Addresses;

    public iServerAddress()
    {
        if (executionMode.equals("release"))
        {
            loadAddresses();

            if (Addresses == null)
            {

                Addresses = new String[] {
                        "met.greatstarvision.com/%s.php",
                        "met.greatstarvision.com/%s.php"
                };
                saveAddresses();
            }
        }
        else
        {
            Addresses = new String[] {
                    "met.greatstarvision.com/%s.php",
                    "met.greatstarvision.com/%s.php"
            };
        }
    }

    public String getServerAddress(iEndPoints endPoints)
    {
        String result = Addresses[0];

        if (result.contains("mtg.redsearing.com"))
        {
            result = "met.redsearing.com/%s.php";
        }

        if (result.contains("met.redsearing.com"))
        {
            result = "met.greatstarvision.com/%s.php";
        }

        if (result.contains("ec2-18-216-248-102.us-east"))
        {
            result = "met.greatstarvision.com/%s.php";
        }

        switch (endPoints)
        {
            case ins:
                result = format(Locale.ENGLISH,result,"ins");
                break;
            case gen:
                result = format(Locale.ENGLISH,result,"gen");
                break;
            case reg:
                result = format(Locale.ENGLISH,result,"reg");
                break;
            case ser:
                result = format(Locale.ENGLISH,result,"ser");
                break;
        }


        return result;
    }

    public void nextAddress()
    {
        String tmp = Addresses[0];
        Addresses[0] = Addresses[1];
        Addresses[1] = tmp;

        saveAddresses();

    }

    public void setNewAddresses(String[] newAddresses)
    {
        Addresses = newAddresses;
        saveAddresses();
    }

    public void saveAddresses()
    {

        try
        {
            dbMetagram.setPair(key,TextUtils.join(",", Addresses));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void loadAddresses()
    {

        String result;

        try
        {
            result = dbMetagram.getPair(key);
            Addresses = result.split(",");

            if (Addresses.length < 2)
            {
                Addresses = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}
