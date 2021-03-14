package vp.tools.cipher;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;




public class iAESCipher
{
    private IvParameterSpec ivSpec;
    private Cipher cipher;
    byte[] AESKey;
    byte[] iv;
    boolean initialized = false;

    public iAESCipher(byte[] _key, byte[] _iv) throws GeneralSecurityException
    {
        AESKey = _key;
        iv = _iv;
        ivSpec = new IvParameterSpec(iv);
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        initialized = true;
    }


    public byte[] encrypt(byte[] _clearInput) throws GeneralSecurityException
    {
        SecretKeySpec sKeySpec = new SecretKeySpec(AESKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, sKeySpec,ivSpec);
        byte[] encrypted = cipher.doFinal(_clearInput);
        return encrypted;
    }

    public byte[] decrypt(byte[] _encryptedInput) throws GeneralSecurityException
    {
        SecretKeySpec sKeySpec = new SecretKeySpec(AESKey, "AES");
        cipher.init(Cipher.DECRYPT_MODE, sKeySpec,ivSpec);
        byte[] decrypted = cipher.doFinal(_encryptedInput);
        return decrypted;
    }

    public String encryptToHex(byte[] _clearInput) throws GeneralSecurityException
    {
        return Base64.encodeToString(encrypt(_clearInput), Base64.NO_WRAP);
    }

    public byte[] decryptFromHex(String _hexedEncryptedInput) throws Exception
    {
        return decrypt(Base64.decode(_hexedEncryptedInput,Base64.NO_WRAP));
    }

    synchronized public String encryptStringToHex(String _clearInput) throws GeneralSecurityException, UnsupportedEncodingException
    {
        return Base64.encodeToString(encrypt(_clearInput.getBytes("utf-8")), Base64.NO_WRAP);
    }

    synchronized public String decryptFromHexToString(String _hexedEncryptedInput) throws GeneralSecurityException, UnsupportedEncodingException
    {
        return new String(decrypt(Base64.decode(_hexedEncryptedInput,Base64.NO_WRAP)), "UTF-8");
    }


}
