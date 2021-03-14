package vp.tools.cipher;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;




public class iRSACipher
{
    Boolean initialized = false;
    PublicKey publicKey;
    PrivateKey privateKey;
    Cipher encCipher, decCipher;

    public iRSACipher(PrivateKey _privateKey, PublicKey _publicKey)
    {
        publicKey = _publicKey;
        privateKey = _privateKey;
        initialized = true;
    }

    public iRSACipher(byte[] _privateKey, byte[] _publicKey) throws GeneralSecurityException
    {
        KeyFactory kf = KeyFactory.getInstance("RSA/ECB/NoPadding");

        X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(_publicKey);
        publicKey = kf.generatePublic(X509publicKey);

        PKCS8EncodedKeySpec PKCS8privateKey = new PKCS8EncodedKeySpec(_privateKey);
        privateKey = kf.generatePrivate(PKCS8privateKey);

        initialized = true;
    }


    public String encrypt (String plain) throws GeneralSecurityException
    {
        if (!initialized)
        {throw new GeneralSecurityException("RSA Cipher class not initialized.");}

        byte[] encryptedBytes;
        encCipher = Cipher.getInstance("RSA/ECB/NoPadding");
        encCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        encryptedBytes = encCipher.doFinal(plain.getBytes());

        return(bytesToString(encryptedBytes));

    }

    public String decrypt (String result) throws GeneralSecurityException
    {
        if (!initialized)
        {throw new GeneralSecurityException("RSA Cipher class not initialized.");}

        byte[] decryptedBytes;
        decCipher= Cipher.getInstance("RSA/ECB/NoPadding");
        decCipher.init(Cipher.DECRYPT_MODE, privateKey);
        decryptedBytes = decCipher.doFinal(stringToBytes(result));
        return( new String(decryptedBytes));

    }

    public String encrypt (byte[] _input) throws GeneralSecurityException
    {
        if (!initialized)
        {throw new GeneralSecurityException("RSA Cipher class not initialized.");}

        byte[] encryptedBytes;
        encCipher = Cipher.getInstance("RSA/ECB/NoPadding");
        encCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        encryptedBytes = encCipher.doFinal(_input);

        return(bytesToString(encryptedBytes));

    }

    public byte[] decryptToBytes (String input) throws GeneralSecurityException
    {
        if (!initialized)
        {throw new GeneralSecurityException("RSA Cipher class not initialized.");}

        byte[] decryptedBytes;
        decCipher= Cipher.getInstance("RSA/ECB/NoPadding");
        decCipher.init(Cipher.DECRYPT_MODE, privateKey);
        decryptedBytes = decCipher.doFinal(stringToBytes(input));
        return( decryptedBytes);

    }

    public String bytesToString(byte[] b)
    {
        byte[] b2 = new byte[b.length + 1];
        b2[0] = 1;
        System.arraycopy(b, 0, b2, 1, b.length);
        return new BigInteger(b2).toString(36);
    }

    public  byte[] stringToBytes(String s)
    {
        byte[] b2 = new BigInteger(s, 36).toByteArray();
        return Arrays.copyOfRange(b2, 1, b2.length);
    }
}
