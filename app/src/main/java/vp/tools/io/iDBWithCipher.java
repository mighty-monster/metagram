package vp.tools.io;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;

import javax.security.auth.x500.X500Principal;


import vp.tools.cipher.iAESCipher;
import vp.tools.cipher.iRSACipher;

import static android.database.Cursor.FIELD_TYPE_BLOB;
import static android.database.Cursor.FIELD_TYPE_FLOAT;
import static android.database.Cursor.FIELD_TYPE_INTEGER;
import static android.database.Cursor.FIELD_TYPE_NULL;
import static android.database.Cursor.FIELD_TYPE_STRING;
import static vp.metagram.general.functions.createAESKey;
import static vp.metagram.general.variables.AndroidClientDBIV;
import static vp.metagram.general.variables.isReleaseMode;


public class iDBWithCipher extends iDBManager
{

    public static final String __enc__ANDROID_KEYSTORE = "AndroidKeyStore";
    public static final String __enc__alias = "vpm";
    public static int DBAESKeySize = 32;


    boolean initialized = false;

    private KeyStore keyStore;
    PrivateKey privateKey;
    PublicKey publicKey;
    iRSACipher RSACipher;


    static final String __enc__AESIVStr = "idk83irujflovjdu";
    public byte[] AESKey = new byte[DBAESKeySize];
    byte[] AESIV;
    public iAESCipher AESCipher;

    public iDBWithCipher(Context _context, String _dataBaseName, int _dataBaseVersion, String[] _buildingQueries) throws UnrecoverableEntryException, NoSuchAlgorithmException,
            KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, CertificateException, IOException
    {
        super(_context,_dataBaseName, _dataBaseVersion,_buildingQueries);

        Locale oldLocale = Locale.getDefault();
        Locale locale = new Locale("en");
        Locale.setDefault(locale);

        initKeyStore();
        createRSAKey();
        getPrivateKey();
        getPublicKey();

        RSACipher = new iRSACipher(privateKey,publicKey);

        Locale.setDefault(oldLocale);
    }

    public void init()
    {

        try
        {
            initialized = true;


            Cursor result;
            String __enc__Name = "AESKey";
            String __enc__sqlText = "Select Value from configuration Where name = '%s' ";
            __enc__sqlText = String.format(Locale.ENGLISH,__enc__sqlText,RSACipher.encrypt(__enc__Name.getBytes()).trim());

            result = selectQuery(__enc__sqlText);

            AESIV = __enc__AESIVStr.getBytes();

            if (result.getCount() > 0)
            {   // Database already have a AES Key
                result.moveToFirst();
                String AESKeyStr = result.getString(result.getColumnIndex("Value"));
                byte[] tempKey = RSACipher.decryptToBytes(AESKeyStr);
                for(int i = 0; i< DBAESKeySize; i++)
                {AESKey[i] = tempKey[tempKey.length- DBAESKeySize +i];}

            }
            else
            {
                AESKey = createAESKey();
                __enc__sqlText = "Insert Into configuration(Name,Value) values ('%s','%s')";
                __enc__sqlText = String.format(Locale.ENGLISH,__enc__sqlText,RSACipher.encrypt(__enc__Name.getBytes()).trim(),RSACipher.encrypt(AESKey));
                execQuery(__enc__sqlText);
            }

            AESCipher = new iAESCipher(AESKey,AESIV);


        }
        catch (Exception e)
        {
            initialized = false;
            e.printStackTrace();
        }

    }

    private void initKeyStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException
    {
        keyStore = KeyStore.getInstance(__enc__ANDROID_KEYSTORE);
        keyStore.load(null);
        Enumeration<String> aliases = keyStore.aliases();
        while(aliases.hasMoreElements())
            Log.e("E", "Aliases = " + aliases.nextElement());
    }

    private void createRSAKey() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyStoreException
    {

        if (!keyStore.containsAlias(__enc__alias))
        {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 20);

            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(__enc__alias)
                    .setSubject(new X500Principal("CN=" + __enc__alias))
                    .setSerialNumber(BigInteger.TEN)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();


            // use the Android keystore

            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA", __enc__ANDROID_KEYSTORE);
            gen.initialize(spec);

            // generates the keypair
            gen.generateKeyPair();

        }

    }

    private void getPublicKey() throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException
    {
        if (isReleaseMode)
        {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(__enc__alias, null);
            publicKey = privateKeyEntry.getCertificate().getPublicKey();
        }
        else
        {
            String pubString = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCldJjrZzHzVjkwAnw/Ot+Xn6UYSyWvUv/TG/ZwDlCccMVTBZmksKTF42cs9TgEhDZFyCsS/fk0MCRh0Ao5kxEhTP9WNc85APwiPSjklBEe8+7N1vMOu/c0gw/QoLX4XvwKoQs0X/+lIcTsKw2KxVjc/XjPqiWAPLfRHoeWh+dgEwIDAQAB";

            KeyFactory kf = KeyFactory.getInstance("RSA");
            try
            {
                X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decode(pubString, Base64.DEFAULT));
                publicKey = kf.generatePublic(spec);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }

    }

    private void getPrivateKey() throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException
    {
        if(isReleaseMode)
        {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(__enc__alias, null);
            privateKey = privateKeyEntry.getPrivateKey();
        }
        else
        {
            String priString = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKV0mOtnMfNWOTACfD8635efpRhLJa9S/9Mb9nAOUJxwxVMFmaSwpMXjZyz1OASENkXIKxL9+TQwJGHQCjmTESFM/1Y1zzkA/CI9KOSUER7z7s3W8w679zSDD9Cgtfhe/AqhCzRf/6UhxOwrDYrFWNz9eM+qJYA8t9Eeh5aH52ATAgMBAAECgYB0zb1u4ik3OjWhGQARu0RBzChG4DY4cYW8yU7OiKyL5GjJVXjD9Rg9w24BJRRoy9VsqgUOoVEecKYejznIr3Q1RX2QpcH4Cq6sqpki6oskS3xgjhdYO83KKiZMuSEX0j5dUpPIYgWGe08I4oHf2UnHOVPDfyuhS3SubtMEnlCBEQJBANGwgroMbhim+cNgI/vAlCGCVR/kSD2JXjJjPtkPqpmKt8TbfTzB5RHOPccA98MaHc31tOnGUNTaZN521+i4vP8CQQDJ/ytJ0IyfPB5McjF8S0f0flrMxUgFLIXwnWR888Cn2ZgWKEjUFV2y/0Gy4dsW0dbRQPeFlKUDjDqeuD/WV5jtAkBrhj2itZkHHyhYHUaWY8wR2slXzzGUDIq2/9lMaRIsJcvSMFLqAus51C4ti7uA2jWKVYJtlfoBC/RJd1uDxKd1AkEAtgyEPhV39zcec3VjhgrvAbozKWQP4aHb4Rxo6XhhxKUGPcn2wTW0adNFqeuGIk3iVls/+aMbCVSDrHKQDiSKjQJADP22GxEUxIhJsDBJr2lNri0TzQ9lv3aXjsSf4HbVlyjTVALW018UKKw+kBcmgqcb7mDE0Y8txtAPaSJw+kgNHA==";
            KeyFactory kf = KeyFactory.getInstance("RSA");
            try
            {
                PKCS8EncodedKeySpec specPriv = new PKCS8EncodedKeySpec(Base64.decode(priString, Base64.DEFAULT));
                privateKey = kf.generatePrivate(specPriv);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void  execQuery(String _Query) throws IOException
    {
        if (!initialized)
            throw new IOException("Cipher is not initialized, run init() function before using Database");
        super.execQuery(_Query);
    }

    @Override
    public MatrixCursor selectQuery(String _Query) throws IOException, GeneralSecurityException
    {
        if (!initialized)
            throw new IOException("Cipher is not initialized, run init() function before using Database");

        SQLiteDatabase db;
        db = getWritableDatabase(AndroidClientDBIV);

        Cursor queryResult = db.rawQuery(_Query, null);
        String[] columns = queryResult.getColumnNames();
        MatrixCursor result = new MatrixCursor(columns);

        try
        {
            if (queryResult.moveToFirst())
            {
                while(!queryResult.isAfterLast())
                {
                    MatrixCursor.RowBuilder rowBuilder = result.newRow();
                    for(String col: columns)
                    {
                        int colType = queryResult.getType(queryResult.getColumnIndex(col));
                        switch (colType)
                        {
                            case FIELD_TYPE_BLOB:
                            {
                                rowBuilder.add(queryResult.getBlob(queryResult.getColumnIndex(col)));
                                break;
                            }
                            case FIELD_TYPE_FLOAT:
                            {
                                rowBuilder.add(queryResult.getFloat(queryResult.getColumnIndex(col)));
                                break;
                            }
                            case FIELD_TYPE_INTEGER:
                            {
                                rowBuilder.add(queryResult.getLong(queryResult.getColumnIndex(col)));
                                break;
                            }
                            case FIELD_TYPE_STRING:
                            {
                                String content = queryResult.getString(queryResult.getColumnIndex(col));

                                if (col.indexOf("__enc__")>=0)
                                {
                                    content = AESCipher.decryptFromHexToString(content);
                                }

                                rowBuilder.add(content);
                                break;
                            }
                            case FIELD_TYPE_NULL:
                            {
                                rowBuilder.add("");
                                break;
                            }
                        }

                    }
                    queryResult.moveToNext();
                }
            }
        }
        finally
        {
            if(queryResult != null)
                queryResult.close();
        }


        return  result;
    }

}
