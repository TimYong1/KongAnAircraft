package com.tourcoo.lib;


import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;

import cn.hutool.core.codec.Base64;

import static com.tourcoo.lib.RsaUtils.KEY;
import static com.tourcoo.lib.RsaUtils.PUBLISH_KEY;

public class MyTestClass {
    public static void main(String[] args) {
        try {
            String enCode = RsaUtils.encryptByPublicKey(PUBLISH_KEY, "123456");
            System.out.println("enCode="+enCode);
            String str = "$10$ybZRtfTqlJCGI3Ua78Nk/uQr7OlnfJjQtxGMaO.u0c/DdnddHJjXu";
        String result =     RsaUtils.decryptByPrivateKey(KEY,str);
            System.out.println("result===="+result);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        test3();
    }

    private static void test3() {
        String str = "$10$ybZRtfTqlJCGI3Ua78Nk/uQr7OlnfJjQtxGMaO.u0c/DdnddHJjXu";
        try {
            String result = RsaUtils.decryptByPrivateKey(KEY, str);
            System.out.println("result=" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 私钥解密
     *
     * @param privateKeyText 私钥
     * @param text           待解密的文本
     * @return /
     * @throws Exception /
     */
    public static String decryptByPrivateKey(String privateKeyText, String text) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec5 = new PKCS8EncodedKeySpec(Base64.decode(privateKeyText));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec5);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] result = cipher.doFinal(Base64.decode(text));
        return new String(result);
    }
}