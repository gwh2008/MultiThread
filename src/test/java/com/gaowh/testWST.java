package com.gaowh;

import cn.com.westone.common.array.ByteArrayUtil;
import com.westone.bouncycastle.asn1.DERNull;
import com.westone.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.westone.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.westone.bouncycastle.asn1.x509.RSAPublicKeyStructure;
import com.westone.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
//import com.westone.bouncycastle.jce.provider.BouncyCastleProvider;
import com.westone.bouncycastle.jce.provider.WstBouncyCastleProvider;
import com.westone.bouncycastle.util.encoders.Base64;
import com.westone.pboc.hsm.HSMConstant;
import com.westone.pboc.hsm.constants.ProtectedBlobFormatEnum;
import com.westone.pboc.hsm.entity.KeyPairs;
import com.westone.pboc.hsm.entity.RSARefPublicKey;
import com.westone.pboc.hsm.entity.SM2RefPublicKey;
import com.westone.pboc.mina.client.Client;
import com.westone.pboc.mina.client.ClientThreadPool;
import com.westone.pboc.service.imp.HSMWSTApiServiceImp;
import com.westone.pboc.util.CommonUtil;
import com.westone.pboc.util.DataConverUtil;


import javax.crypto.Cipher;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.List;

public class testWST {

    private static HSMWSTApiServiceImp imp = null;

    static {
        imp = new HSMWSTApiServiceImp();
    }

    private static PrivateKey priKeySm2 = null;
    private static PublicKey pubKeySm2 = null;

    private static PrivateKey priKeyRsa = null;
    private static PublicKey pubKeyRsa = null;

    private static String pucPassword = "11111111";

    private static byte[] fileName = "测试中文".getBytes(StandardCharsets.UTF_8);

    private static ClientThreadPool connectPool = ClientThreadPool.getInstance("D:\\hsmapi-all\\hsmapi-0901-bc159\\hsm-api\\src\\main\\resources\\conf\\hsminfo0901.properties");

    //索引号  RSA-11  SM2-12  KEK-13

    //#############################################设备管理类函数################################################

    /**
     * 测试产生随机数
     */
    private static void testHsmGenerateRandom() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试产生随机数");
        Client client = null;

        try {
            client = connectPool.getClient();
            byte[] random = imp.hsmGenerateRandom(10, client);
            System.out.println("产生随机数为：" + ByteArrayUtil.toHexString(random));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    private static void testHsmEncryptAndDecrypt() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试对称加解密");
        int keyIndex = 1;
        int uiKeyBits = 16;
        int algID = HSMConstant.SGD_SM4_ECB;
        Client client = null;

        try {

            client = connectPool.getClient();

            byte[] pucData = "1234567890abcdef".getBytes();

//            //使用keyindex以keyhandle形式传入
//            byte[] kh = ByteArrayUtil.BigEndian.toByteArray(keyIndex,2);

            List<Object> wk = imp.hsmGenerateKeyWithKEK(keyIndex, uiKeyBits, client);
            byte[] keyHandle = (byte[]) wk.get(0);
            byte[] cipher = imp.hsmEncrypt(keyHandle, algID, null, pucData, client);
            byte[] plain = imp.hsmDecrypt(keyHandle, algID, null, cipher, client);
            boolean ok = imp.hsmDestoryKey(keyHandle, client);

            imp.hsmDestoryKey(keyHandle, client);

            System.out.println("密文为：" + ByteArrayUtil.toHexString(cipher));
            System.out.println("明文为：" + ByteArrayUtil.toHexString(plain));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    /**
     * 测试获取、释放私钥权限
     */
    private static void testGetPrivateKeyAccessRight() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试获取私钥权限");
        int keyIndex = 11;
        Client client = null;

        try {

            client = connectPool.getClient();

            boolean right = imp.hsmGetPrivateKeyAccessRight(keyIndex, pucPassword, client);
            System.out.println("获取私钥权限结果为：" + right);

            boolean releaseRight = imp.hsmReleasePrivateKeyAccessRight(keyIndex, client);

            System.out.println("释放私钥权限结果为：" + releaseRight);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    //#############################################密钥管理类函数#########################################################

    /**
     * 测试获取RSA公钥 ok
     */
    private static void testHsmRSAGetPublicKey() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试获取RSA公钥");
        int keyIndex = 1;
        Client client = null;

        try {
            client = connectPool.getClient();
            PublicKey pubKey = imp.hsmRSAGetPublicKey(keyIndex, client);
            System.out.println("获取RSA2公钥成功,公私钥：" + ByteArrayUtil.toHexString(pubKey.getEncoded()));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    /**
     * 测试RSA公钥转x509、私钥转PKCS8格式
     */
    private static void testRSATransferFomat() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试产生RSA密钥对");
        int uiKeyBits = 2048;
        String input = "1111111111111111111111111111111111111111111111111111111111111111";
        System.out.println("input is " + ByteArrayUtil.toHexString(input.getBytes()));
        Client client = null;

        try {
            client = connectPool.getClient();
            KeyPairs keypairs = imp.hsmRSAGenerateKeyPair(uiKeyBits, client);
            priKeyRsa = keypairs.getPrikey();
            pubKeyRsa = keypairs.getPubkey();
            System.out.println("产生RSA密钥对成功,新生成私钥：" + ByteArrayUtil.toHexString(keypairs.getPrikey().getEncoded()));
            System.out.println("产生RSA密钥对成功,新生成公钥:" + ByteArrayUtil.toHexString(keypairs.getPubkey().getEncoded()));

            RSARefPublicKey rsaRefPublicKey = DataConverUtil.RSAPublicKey2Refrel(pubKeyRsa);

            SubjectPublicKeyInfo publicKeyInfo = new SubjectPublicKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, new DERNull()), new RSAPublicKeyStructure(new BigInteger(rsaRefPublicKey.getM()), new BigInteger(rsaRefPublicKey.getE())).toASN1Primitive());

            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKeyInfo.getEncoded());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey x509RSAPublicKey = keyFactory.generatePublic(x509EncodedKeySpec);
            System.out.println("X509 RSAPublicKey is " + ByteArrayUtil.toHexString(publicKeyInfo.getEncoded()));

            byte[] pkcs8Bytes = DataConverUtil.RSAPrivateKey2Ref(priKeyRsa).ref2Pkcs8();

            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec((pkcs8Bytes));

            PrivateKey pkcs8PrivateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            System.out.println("pkcs8 RSAPrivateKey is " + ByteArrayUtil.toHexString(pkcs8PrivateKey.getEncoded()));

            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<加解密测试<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

            Cipher cipher = Cipher.getInstance("RSA");

            cipher.init(Cipher.ENCRYPT_MODE, x509RSAPublicKey, new SecureRandom());
            byte[] cipherBytes = cipher.doFinal(input.getBytes(), 0, input.getBytes().length);

            System.out.println("cipherBytes is " + ByteArrayUtil.toHexString(cipherBytes));

            cipher.init(Cipher.DECRYPT_MODE, pkcs8PrivateKey, new SecureRandom());
            byte[] plain = cipher.doFinal(cipherBytes, 0, cipherBytes.length);

            System.out.println("plain is " + ByteArrayUtil.toHexString(plain));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    /**
     * 测试产生RSA密钥对  ok
     */
    private static void testHsmRSAGenerateKeyPair() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试产生RSA密钥对");
        int uiKeyBits = 2048;
        Client client = null;

        try {
            client = connectPool.getClient();
            KeyPairs keypairs = imp.hsmRSAGenerateKeyPair(uiKeyBits, client);
            priKeyRsa = keypairs.getPrikey();
            pubKeyRsa = keypairs.getPubkey();
            System.out.println("产生RSA密钥对成功,新生成私钥：" + ByteArrayUtil.toHexString(keypairs.getPrikey().getEncoded()));
            System.out.println("产生RSA密钥对成功,新生成公钥:" + ByteArrayUtil.toHexString(keypairs.getPubkey().getEncoded()));

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    /**
     * 测试基于RSA算法的数字信封转换
     */
    private static void testHsmRSAExchangeDigitEnvelope() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试基于RSA算法的数字信封转换");
        int keyIndex = 11;
        byte[] inputData = "1234abcd1234abcd".getBytes();
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        try {

            client = pool.getClient();
            PublicKey publicKey = imp.hsmRSAGetPublicKey(keyIndex, client);
            imp.hsmGetPrivateKeyAccessRight(keyIndex + 500, pucPassword, client);
            byte[] keyData = imp.hsmRSAExchangeDigitEnvelope(keyIndex, publicKey, inputData, client);
            System.out.println("基于RSA算法的数字信封转换成功,生成数据：" + ByteArrayUtil.toHexString(keyData));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            pool.release(client);
        }
    }


    /**
     * 测试产生SM2密钥对  ok
     */
    private static void testHsmSM2GenerateKeyPair() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试产生SM2密钥对");
        int uiKeyBits = 256;
        int uiAlgID = 6;
        Client client = null;

        try {
            client = connectPool.getClient();
            KeyPairs keypairs = imp.hsmSM2GenerateKeyPair(uiKeyBits, uiAlgID, client);
            priKeySm2 = keypairs.getPrikey();
            pubKeySm2 = keypairs.getPubkey();

            System.out.println("产生SM2密钥对成功,新生成私钥：" + ByteArrayUtil.toHexString(keypairs.getPrikey().getEncoded()));
            System.out.println("产生SM2密钥对成功,新生成公钥:" + ByteArrayUtil.toHexString(keypairs.getPubkey().getEncoded()));

//            byte[] publicKeyDecode =  DataConverUtil.decodePublicKeyDer(keypairs.getPubkey().getEncoded());
//
//            System.out.println("解码数据，公钥为： " + ByteArrayUtil.toHexString(publicKeyDecode));
//
//            PublicKey newPubKey = DataConverUtil.byte2PublicKey(publicKeyDecode);
//
//            System.out.println("构造公钥成功,新生成公钥:" + ByteArrayUtil.toHexString(newPubKey.getEncoded()));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    /**
     * 测试获取SM2公钥  ok
     */
    private static void testHsmSM2GetPublicKey() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试获取SM2公钥");
        int keyIndex = 1;
        Client client = null;

        try {
            client = connectPool.getClient();
            PublicKey pubKey = imp.hsmSM2GetPublicKey(keyIndex, client);

            System.out.println("获取SM2公钥成功,公钥：" + ByteArrayUtil.toHexString(pubKey.getEncoded()));
            byte[] pkDecode = DataConverUtil.decodePublicKeyDer(pubKey.getEncoded());
            System.out.println("解码为：" + ByteArrayUtil.toHexString(pkDecode));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    /**
     * 测试生成会话密钥并用内部RSA公钥加密输出
     */
    private static void testHsmRSAGenerateKeyWithIPK() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试生成会话密钥并用内部RSA公钥加密输出");
        int keyIndex = 11;
        int uiKeyBits = 512;
        Client client = null;

        try {
            client = connectPool.getClient();
            List<Object> list = imp.hsmRSAGenerateKeyWithIPK(keyIndex, uiKeyBits, client);
            byte[] keyHandle = (byte[]) list.get(0);
            System.out.println("生成会话密钥并用内部RSA公钥加密输出成功,生成会话密钥：" + ByteArrayUtil.toHexString((byte[]) list.get(1)));

            imp.hsmDestoryKey(keyHandle, client);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    /**
     * 测试生成会话密钥并用外部RSA公钥加密输出
     */
    private static void testHsmRSAGenerateKeyWithEPK() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试生成会话密钥并用外部RSA公钥加密输出");
        int keyIndex = 11;
        int uiKeyBits = 512;
        Client client = null;

        try {
            client = connectPool.getClient();
            PublicKey publicKey = imp.hsmRSAGetPublicKey(keyIndex, client);

            List<Object> list = imp.hsmRSAGenerateKeyWithEPK(uiKeyBits, publicKey, client);
            byte[] keyHandle = (byte[]) list.get(0);
            System.out.println("生成会话密钥并用外部RSA公钥加密输出成功,生成会话密钥：" + ByteArrayUtil.toHexString((byte[]) list.get(1)));

            imp.hsmDestoryKey(keyHandle, client);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    /**
     * 测试生成会话密钥并用外部ECC公钥加密输出
     */
    private static void testHsmSM2GenerateKeyWithEPK() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试生成会话密钥并用外部ECC公钥加密输出");
        int keyIndex = 1;
        int algID = 6;
        int uiKeyBits = 256;
        Client client = null;

        try {
            client = connectPool.getClient();
            PublicKey publicKey = imp.hsmSM2GetPublicKey(keyIndex, client);
            List<Object> list = imp.hsmSM2GenerateKeyWithEPK(uiKeyBits, algID, publicKey, client);
            byte[] keyHandle = (byte[]) list.get(0);
            System.out.println("生成会话密钥并用外部ECC公钥加密输出成功,生成会话密钥：" + ByteArrayUtil.toHexString((byte[]) list.get(1)));
            imp.hsmDestoryKey(keyHandle, client);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    /**
     * 测试生成会话密钥并用内部ECC公钥加密输出
     */
    private static void testHsmSM2GenerateKeyWithIPK() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试生成会话密钥并用内部ECC公钥加密输出");
        int keyIndex = 1;
        int uiKeyBits = 256;
        Client client = null;

        try {
            client = connectPool.getClient();

//            //使用keyindex以keyhandle形式传入
//            byte[] kh = ByteArrayUtil.BigEndian.toByteArray(keyIndex,2);

            List<Object> list = imp.hsmSM2GenerateKeyWithIPK(keyIndex, uiKeyBits, client);
            byte[] keyHandle = (byte[]) list.get(0);
            System.out.println("生成会话密钥并用内部ECC公钥加密输出成功,生成会话句柄：" + ByteArrayUtil.toHexString(keyHandle));
            System.out.println("生成会话密钥并用内部ECC公钥加密输出成功,生成会话密钥：" + ByteArrayUtil.toHexString((byte[]) list.get(1)));
            imp.hsmDestoryKey(keyHandle, client);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    /**
     * 测试生成会话密钥并用密钥加密密钥加密输出
     */
    private static void testHsmGenerateKeyWithKEK() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试生成会话密钥并用密钥加密密钥加密输出");
        int keyIndex = 13;
        int uiKeyBits = 256;
        Client client = null;

        try {
            client = connectPool.getClient();

//            //使用keyindex以keyhandle形式传入
//            byte[] kh = ByteArrayUtil.BigEndian.toByteArray(keyIndex,2);

            List<Object> list = imp.hsmGenerateKeyWithKEK(keyIndex, uiKeyBits, client);
            byte[] keyHandle = (byte[]) list.get(0);
            byte[] keyData = (byte[]) list.get(1);
            System.out.println("生成会话密钥并用密钥加密密钥加密输出成功,生成会话密钥：" + ByteArrayUtil.toHexString(keyData));

            imp.hsmDestoryKey(keyHandle, client);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    /**
     * 测试导入明文会话密钥
     */
    private static void testHsmImportKey() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试导入明文会话密钥");
        byte[] keyData = "12345678abcdabcd".getBytes();
        Client client = null;

        try {
            client = connectPool.getClient();
            byte[] keyHandle = imp.hsmImportKey(keyData, client);

            System.out.println("导入明文会话密钥成功,密钥Handle为：" + ByteArrayUtil.toHexString(keyHandle));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    /**
     * 测试导入会话密钥并用内部RSA私钥解密
     */
    private static void testHsmRSAImportKeyWithISK() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试导入会话密钥并用内部RSA私钥解密");
        int keyIndex = 1;

        int uiKeyBits = 512;
        Client client = null;

        try {
            client = connectPool.getClient();
            imp.hsmGetPrivateKeyAccessRight(keyIndex + 500, pucPassword, client);
            List<Object> list = imp.hsmRSAGenerateKeyWithIPK(keyIndex, uiKeyBits, client);
            byte[] keyHandle = (byte[]) list.get(0);
            byte[] keyData = (byte[]) list.get(1);

//            byte[] kh = ByteArrayUtil.BigEndian.toByteArray(keyIndex,2);

            byte[] keyHandle1 = imp.hsmRSAImportKeyWithISK(keyIndex, keyData, client);

            System.out.println("导入会话密钥并用内部RSA私钥解密成功,密钥handle为：" + ByteArrayUtil.toHexString(keyHandle1));

            imp.hsmDestoryKey(keyHandle, client);
            imp.hsmDestoryKey(keyHandle1, client);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    /**
     * 测试导入会话密钥并用密钥加密密钥解密
     */
    private static void testHsmImportKeyWithKEK() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试导入会话密钥并用密钥加密密钥解密");
        int keyIndex = 3;
        byte[] keyData = "12345678abcdabcd".getBytes();
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        try {
            client = pool.getClient();

            //使用keyindex以keyhandle形式传入
//            byte[] kh = ByteArrayUtil.BigEndian.toByteArray(keyIndex,2);

            byte[] keyHandle = imp.hsmImportKeyWithKEK(keyIndex, keyData, client);

            System.out.println("导入会话密钥并用密钥加密密钥解密成功,密钥Handle为：" + ByteArrayUtil.toHexString(keyHandle));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            pool.release(client);
        }
    }

    /**
     * 测试导入会话密钥并用内部ECC私钥解密
     */
    private static void testHsmSM2ImportKeyWithISK() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试导入会话密钥并用内部ECC私钥解密");
        int keyIndex = 1;
        int uiKeyBits = 256;
        Client client = null;

        try {
            client = connectPool.getClient();
            imp.hsmGetPrivateKeyAccessRight(keyIndex, pucPassword, client);

            //使用keyindex以keyhandle形式传入
//            byte[] kh = ByteArrayUtil.BigEndian.toByteArray(keyIndex,2);

            List<Object> list = imp.hsmSM2GenerateKeyWithIPK(keyIndex, uiKeyBits, client);
            byte[] keyHandle = (byte[]) list.get(0);
            byte[] keyData = (byte[]) list.get(1);
            System.out.println("生成会话密钥并用内部ECC公钥加密输出成功,生成会话句柄：" + ByteArrayUtil.toHexString(keyHandle));

            byte[] keyHandle1 = imp.hsmSM2ImportKeyWithISK(keyIndex, keyData, client);

            System.out.println("导入会话密钥并用内部ECC私钥解密成功,密钥handle为：" + ByteArrayUtil.toHexString(keyHandle));

            imp.hsmDestoryKey(keyHandle, client);
            imp.hsmDestoryKey(keyHandle1, client);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    /**
     * 测试生成密钥协商参数并输出
     */
    private static void testHsmSM2GenerateAgreementData() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试生成密钥协商参数并输出");
        int keyIndex = 12;
        int uiKeyBits = 256;
        byte[] sponsorID = "1".getBytes();
        Client client = null;

        try {
            client = connectPool.getClient();
            List<Object> result = imp.hsmSM2GenerateAgreementData(keyIndex, uiKeyBits, sponsorID, client);

            System.out.println("生成密钥协商参数并输出成功,密钥参数为：" + result.get(0) + ",pubKey is :" + result.get(1));

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    /**
     * 测试计算会话密钥
     */
    private static void testHsmSM2GenerateKey() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试计算会话密钥");
        int keyIndex = 12;

        byte[] agreementHandle = ByteArrayUtil.BigEndian.toByteArray(12, 2);
        byte[] responseID = "1".getBytes();
        Client client = null;

        try {
            client = connectPool.getClient();
            PublicKey publicKey = imp.hsmSM2GetPublicKey(keyIndex, client);
            PublicKey tmpPublicKey = imp.hsmSM2GetPublicKey(keyIndex, client);

            byte[] handle = imp.hsmSM2GenerateKey(keyIndex, agreementHandle, responseID, publicKey, tmpPublicKey, client);
            System.out.println("计算会话密钥成功,生成会话密钥Handle：" + ByteArrayUtil.toHexString(handle));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    /**
     * 测试产生协商参数并计算会话密钥
     */
    private static void testHsmSM2GenerateAgreementDataAndKey() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试产生协商参数并计算会话密钥");
        int keyIndex = 12;
        int uiKeyBits = 256;
        byte[] sponsorID = "1".getBytes();
        byte[] responseID = "1".getBytes();
        Client client = null;

        try {
            client = connectPool.getClient();
            boolean right = imp.hsmGetPrivateKeyAccessRight(keyIndex, pucPassword, client);
            PublicKey publicKey = imp.hsmSM2GetPublicKey(keyIndex, client);
            PublicKey tmpPublicKey = imp.hsmSM2GetPublicKey(keyIndex, client);

            List<Object> result = imp.hsmSM2GenerateAgreementDataAndKey(keyIndex, uiKeyBits, sponsorID, responseID, publicKey, tmpPublicKey, client);

            System.out.println("产生协商参数并计算会话密钥成功,密钥参数为：" + result.get(0) + ",pubKey:" + result.get(1));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    /**
     * 测试基于ECC算法的数字信封转换
     */
    private static void testHsmSM2ExchangeDigitEnvelope() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试基于ECC算法的数字信封转换");
        int keyIndex = 2;
        int algID = 6;
        byte[] inData = "abcd1234abcd1234abcd1234abcd1234".getBytes();
        String algType = HSMConstant.WST_ALG_TYPE;
        Client client = null;

        try {
            client = connectPool.getClient();
            KeyPairs keypairs = imp.hsmSM2GenerateKeyPair(256, 6, client);
            priKeySm2 = keypairs.getPrikey();
            pubKeySm2 = keypairs.getPubkey();

            PublicKey publicKey = imp.hsmSM2GetPublicKey(keyIndex, client);
            boolean right = imp.hsmGetPrivateKeyAccessRight(keyIndex, pucPassword, client);
            byte[] encData = imp.hsmSM2ExternalPublicKeyEnc(algType, publicKey, inData, client);

            byte[] outData = imp.hsmSM2ExchangeDigitEnvelope(keyIndex, algID, publicKey, encData, client);
            System.out.println("基于ECC算法的数字信封转换成功,转换生成数据：" + ByteArrayUtil.toHexString(outData));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    /**
     * 测试销毁会话密钥
     */
    private static void testHsmDestoryKey() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试销毁会话密钥");
        byte[] keyData = "abcd1234abcd1234".getBytes();
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        try {
            client = pool.getClient();
            byte[] keyHandle = imp.hsmImportKey(keyData, client);
            boolean ret = imp.hsmDestoryKey(keyHandle, client);
            System.out.println("销毁会话密钥操作成功,结果为：" + ret);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            pool.release(client);
        }
    }

    //######################################################非对称算法##################################

    /**
     * 测试外部公钥RSA加密、私钥解密  ok
     */
    private static void testHsmRSAExternalPublicKeyEnc() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试外部公钥RSA加密");

        int keyBits = 1024;
        String src = "11111111111111111111111111111111111111111111111111111111111111111111111";
        Client client = null;

        try {
            client = connectPool.getClient();
            KeyPairs keypairs = imp.hsmRSAGenerateKeyPair(keyBits, client);
            priKeyRsa = keypairs.getPrikey();
            pubKeyRsa = keypairs.getPubkey();

            byte[] encData = imp.hsmRSAExternalPublicKeyEnc(pubKeyRsa, src.getBytes(), client);
            System.out.println("外部公钥RSA加密成功,密文：" + ByteArrayUtil.toHexString(encData));

            byte[] pucData = imp.hsmRSAExternalPrivateKeyDec(priKeyRsa, encData, client);
            System.out.println("外部公钥RSA解密成功,明文：" + ByteArrayUtil.toHexString(pucData));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    /**
     * 测试内部公钥RSA加密、私钥解密  ok 解密 no
     */
    private static void testHsmRSAInternalPublicKeyEnc() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试外部公钥RSA加密");
        int keyIndex = 2;
//        String src = "11111111ffffffff000000000" +
//                "11111111ffffffff000000000" +
//                "11111111ffffffff000000000" +
//                "11111111ffffffff000000000" +
//                "11111111ffffffff000000000" +
//                "11111111ffffffff000000000" +
//                "11111111ffffffff000000000" +
//                "11111111ffffffff000000000" +
//                "11111111ffffffff000000000" +
//                "11111111ffffffff000000000123456";
        String src = "11111111111111111111111111111111111111111111111111111111111111111111111";
        System.out.println("内部公钥RSA加，加密明文：" + src);
        Client client = null;

        try {
            client = connectPool.getClient();
            byte[] encData = imp.hsmRSAInternalPublicKeyEnc(keyIndex, src.getBytes(), client);
            System.out.println("内部公钥RSA加密成功,密文：" + encData.length + ":" + ByteArrayUtil.toHexString(encData));

            imp.hsmGetPrivateKeyAccessRight(keyIndex + 500, pucPassword, client);
            byte[] pucData = imp.hsmRSAInternalPrivateKeyDec(keyIndex, encData, pucPassword, client);
            System.out.println("内部私钥RSA解密成功,明文：" + ByteArrayUtil.toHexString(pucData));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    /**
     * 测试外部SM2签名、验签   ok
     */
    private static void testHsmSM2ExternalSign() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试外部SM2签名、验签");
        String algType = HSMConstant.WST_ALG_TYPE;
        String src = "1111111111ffffffffff000000000012";
        String userId = "1234567812345678";
        Client client = null;

        try {
            client = connectPool.getClient();
            KeyPairs keypairs = imp.hsmSM2GenerateKeyPair(256, 6, client);
            priKeySm2 = keypairs.getPrikey();
            pubKeySm2 = keypairs.getPubkey();

            System.out.println("pk is " + ByteArrayUtil.toHexString(pubKeySm2.getEncoded()));

            byte[] hashData = imp.doHash(0, pubKeySm2, src.getBytes(), userId, client);

            byte[] signData = imp.hsmSM2ExternalSign(algType, priKeySm2, hashData, client);
            System.out.println("外部SM2签名成功,签名内容：" + ByteArrayUtil.toHexString(signData));

            boolean ret = imp.hsmSM2ExternalVerify(algType, pubKeySm2, signData, hashData, client);
            System.out.println("外部SM2验签密成功,结果：" + ret);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    private static void testHsmSM2ExternalVerify() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试外部SM2验签");
        String algType = HSMConstant.WST_ALG_TYPE;
        String src = "1111111111ffffffffff000000000012111111111111111111111111";
        String userId = "1234567812345678";
        Client client = null;

        byte[] publicKeyDer = ByteArrayUtil.hexString2Bytes("3059301306072a8648ce3d020106082a811ccf5501822d0342000487b3fff29af8efac2891c6d0ac5c05993ee40f072d6aed619ce1eebc222b81f7d5935fb1022efb2401b3166a8815bb4f9bc61d2679e45dda493894dfd2ec015c");
        String initData = "348df2151f8a4afa69a38f311a7b88a6f87d7ec9ab78ecf5cbba2efeb1632e16becd090bc1f82f2fa255f4a7fde186e87d1f5afa3cce2e3a45b8e49c86c9ebfe";

        try {
            client = connectPool.getClient();
            //解DER
            byte[] pkDecode = DataConverUtil.decodePublicKeyDer(publicKeyDer);
            System.out.println("解码为：" + ByteArrayUtil.toHexString(pkDecode));
            //构造publicKey对象
//            PublicKey publicKey = DataConverUtil.byte2PublicKey(pkDecode);
//            byte[] hashData = doHash(client, 0, publicKey, src.getBytes(), userId);
//            boolean ret = imp.hsmSM2ExternalVerify(algType, publicKey, ByteArrayUtil.hexString2Bytes(initData),hashData, client);
//            System.out.println("外部SM2验签密成功,结果：" + ret);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    public static void testHsmBuildPublicKey() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试byte[]转换PublicKey对象");
        Client client = null;

        try {
            client = connectPool.getClient();
            PublicKey publicKey = DataConverUtil.byte2PublicKey(ByteArrayUtil.hexString2Bytes("000100003e0afe935fff9cabca67ce92e73a75865f60bbf5104629ad9110284b7ef3d598753269284a06937b686c54150fee9a49f24266d8a45cfb15aa0c05e39f85a755"));
            System.out.println("转换成公钥:" + ByteArrayUtil.toHexString(publicKey.getEncoded()));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    /**
     * 测试内部SM2签名、验签   ok
     */
    private static void testHsmSM2InternalSign() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试内部SM2签名、验签");
        String algType = HSMConstant.WST_ALG_TYPE;
        int keyIndex = 1;
        String userId = "12345678";
        String src = "1111111111ffffffffff000000000012";
        Client client = null;

        try {
            client = connectPool.getClient();
            boolean right = imp.hsmGetPrivateKeyAccessRight(keyIndex, "11111111", client);

            byte[] hashData = imp.doHash(keyIndex, null, src.getBytes(), userId, client);

            byte[] signData = imp.hsmSM2InternalSign(algType, keyIndex, hashData, pucPassword, client);
            System.out.println("内部SM2签名成功,签名内容：" + ByteArrayUtil.toHexString(signData));

            boolean ret = imp.hsmSM2InternalVerify(algType, keyIndex, signData, hashData, client);
            System.out.println("内部SM2验签密成功,结果：" + ret);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    /**
     * 测试外部SM2公钥加密、私钥解密   ok
     */
    private static void testHsmSM2ExternalPublicKeyEnc() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试外部SM2公钥加密、私钥解密");
        String algType = HSMConstant.WST_ALG_TYPE;
        //String src="11111111012";
//        byte[] input = new byte[1024];
//        Arrays.fill(input, (byte) 0x11);
        String src = "1111111111ffffffffff000000000012";
        Client client = null;

        try {
            client = connectPool.getClient();
            KeyPairs keypairs = imp.hsmSM2GenerateKeyPair(256, 6, client);
            priKeySm2 = keypairs.getPrikey();
            pubKeySm2 = keypairs.getPubkey();
            byte[] encData = imp.hsmSM2ExternalPublicKeyEnc(algType, pubKeySm2, src.getBytes(), client);
            System.out.println("外部SM2公钥加密成功,密文内容：" + ByteArrayUtil.toHexString(encData));

            byte[] pucData = imp.hsmSM2ExternalPrivateKeyDec(algType, priKeySm2, encData, client);
            System.out.println("外部SM2公钥解密成功,结果：" + ByteArrayUtil.toHexString(pucData));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    private static void testHsmSM2InternalPrivateKeyDec() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试SM2内部密钥加密、解密");
        Client client = null;
        String algType = HSMConstant.WST_ALG_TYPE;
        String src = "hello";

        try {
            client = connectPool.getClient();

            System.out.println("明文数据为：" + src);

            byte[] encData = imp.hsmSM2InternalPublicKeyEnc(algType, 6, 2, src.getBytes(), client);
            System.out.println("SM2内部加密成功,密文为：" + ByteArrayUtil.toHexString(encData));
            imp.hsmGetPrivateKeyAccessRight(2, pucPassword, client);
            byte[] result = imp.hsmSM2InternalPrivateKeyDec(2, algType, encData, client);
            System.out.println("SM2内部密钥解密成功,结果为：" + new String(result));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    //######################################################对称算法##################################

    /**
     * 测试对称密钥加密、解密   ok
     */
    private static void testHsmEncrypt() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试对称密钥加密、解密");
        //对称密钥加密 AES-密钥16位  des-密钥8位
        byte[] keyData = "abcd1234abcd1234".getBytes();
        int uiAlgID = HSMConstant.SGD_SM4_CBC;
        byte[] iv = "0000000000000000".getBytes();
        Client client = null;

        try {
            client = connectPool.getClient();
            byte[] handle = imp.hsmImportKey(keyData, client);

            //使用keyindex以keyhandle形式传入
//            byte[] kh = ByteArrayUtil.BigEndian.toByteArray(1,2);

//            List<Object> list = imp.hsmGenerateKeyWithKEK(kh, 16, client);
//            byte[] keyHandle = (byte[]) list.get(0);
            byte[] inputData = new byte[10];
//            byte[] inputData = FileUtil.readFile("D:\\ngrinder-ngrinder-3.4.3-20190709.zip");
            Date encStartTime = new Date();
            byte[] encData = imp.hsmEncrypt(handle, uiAlgID, iv, inputData, client);
            System.out.println("对称密钥加密成功,密文内容：" + encData.length + ", 内容：" + ByteArrayUtil.toHexString(encData));
            System.out.println("加密耗时：" + (new Date().getTime() - encStartTime.getTime()) + "毫秒");

            Date decStartTime = new Date();
            byte[] pucData = imp.hsmDecrypt(handle, uiAlgID, iv, encData, client);
            System.out.println("对称密钥解密成功,结果：" + ByteArrayUtil.toHexString(pucData));
            System.out.println("解密耗时：" + (new Date().getTime() - decStartTime.getTime()) + "毫秒");

            imp.hsmDestoryKey(handle, client);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    /**
     * 测试计算MAC
     */
    private static void testHsmCalculateMAC() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试计算MAC");
        //对称密钥加密 AES-密钥16位  des-密钥8位
//		int keyHandle = 0002;
        int uiAlgID = HSMConstant.SGD_SM4_MAC;
        byte[] iv = "0000000000000000".getBytes();
        byte[] keyData = "abcd1234abcd1234abcd1234abcd1234".getBytes();

        byte[] inData = "abcd1234abcd1234abcd1234abcd1234".getBytes();
        Client client = null;

        try {
            client = connectPool.getClient();
            byte[] handle = imp.hsmImportKey(keyData, client);
            byte[] mac = imp.hsmCalculateMAC(handle, uiAlgID, iv, inData, client);
            System.out.println("计算MAC成功,MAC值：" + ByteArrayUtil.toHexString(mac));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    //######################################################杂凑算法  ##################################

    /**
     * 测试计算摘要
     */
    private static void testhsmHash() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试计算摘要");
        int nArithmetic = HSMConstant.SGD_SCH;
        String dataStr = "00000000001111111111";
        byte[] inData = dataStr.getBytes();
        Client client = null;
        byte[] userId = "1234567812345678".getBytes();

        try {
            client = connectPool.getClient();
            byte[] sign = imp.hsmHash(nArithmetic, 1, null, userId, inData, client);
            System.out.println("计算摘要成功,摘要值：" + ByteArrayUtil.toHexString(sign));

            KeyPairs keyPairs = imp.hsmSM2GenerateKeyPair(256, 6, client);

            byte[] sign11 = imp.hsmHash(nArithmetic, 1, keyPairs.getPubkey(), userId, inData, client);
            System.out.println("计算摘要成功,摘要值：" + ByteArrayUtil.toHexString(sign11));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    //######################################################其他  ##################################

    /**
     * 测试私钥RSA签名P7   no
     */
    private static void testP7RSASign() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试私钥RSA签名P7");
        int nArithmetic = 8;
        int keyIndex = 101;//密钥索引
        String dataStr = "00000000001111111111";
        byte[] inData = dataStr.getBytes();
        Client client = null;

        try {
            client = connectPool.getClient();
            byte[] sign = imp.hsmP7RSASign(nArithmetic, keyIndex, inData, client);
            System.out.println("私钥RSA签名P7成功,签名：" + ByteArrayUtil.toHexString(sign));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    /**
     * 测试外部私钥RSA签名、 公钥验签  ok
     */
    private static void testHsmRSAExternalSign() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试外部私钥RSA签名");
        int nArithmetic = HSMConstant.HSM_SHA1_RSA_PKCS;
        String src = "011111111ffffffff000000000" +
                "11111111ffffffff000000000" +
                "11111111ffffffff000000000" +
                "11111111ffffffff000000000" +
                "11111111ffffffff00000000000";
        Client client = null;

        try {
            client = connectPool.getClient();
            KeyPairs keypairs = imp.hsmRSAGenerateKeyPair(1024, client);
            priKeyRsa = keypairs.getPrikey();
            pubKeyRsa = keypairs.getPubkey();

            byte[] signData = imp.hsmRSAExternalSign(nArithmetic, priKeyRsa, src.getBytes(), client);
            System.out.println("外部私钥RSA签名成功,签名内容：" + ByteArrayUtil.toHexString(signData));

            boolean ret = imp.hsmRSAExternalVerify(nArithmetic, pubKeyRsa, signData, src.getBytes(), client);
            System.out.println("外部私钥RSA验签成功,结果：" + ret);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    /**
     * 测试内部私钥RSA签名、 公钥验签   no
     */
    private static void testHsmRSAInternalSign() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试内部私钥RSA签名");
        int algType = HSMConstant.HSM_MD5_RSA_PKCS;
        int index = 2;
        String src = "011111111ffffffff000000000" +
                "11111111ffffffff000000000" +
                "11111111ffffffff000000000" +
                "11111111ffffffff000000000" +
                "11111111ffffffff00000000000";
        Client client = null;

        try {
            client = connectPool.getClient();
            imp.hsmGetPrivateKeyAccessRight(500 + index, "11111111", client);
            byte[] signData = imp.hsmRSAInternalSign(algType, index, src.getBytes(), pucPassword, client);
            System.out.println("内部私钥RSA签名成功,签名内容：" + ByteArrayUtil.toHexString(signData));

            boolean ret = imp.hsmRSAInternalVerify(algType, index, signData, src.getBytes(), client);
            System.out.println("内部私钥RSA验签成功,结果：" + ret);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    /**
     * 测试产生你要并保存在加密机某索引位置
     */
    private static void testHsmGenerateKey() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试产生密钥并保存在加密机中某索引位置");

        //RAS
        int keyType = 0x01;
        int index = 2;
        int RSAKeyModule = 2048;
        int RSAKeyExponent = 3;

        Client client = null;

        try {
            client = connectPool.getClient();
            boolean RSAResult = imp.hsmGenerateKey(keyType, index, RSAKeyModule, RSAKeyExponent,
                    pucPassword, 0, "", 0, 0, client);

            System.out.println("产生RSA结果：" + RSAResult);

            //ECC
            keyType = 0x02;
            int ECCKeyMoudule = 256;

            boolean ECCResult = imp.hsmGenerateKey(keyType, index, RSAKeyModule, RSAKeyExponent,
                    pucPassword, ECCKeyMoudule, pucPassword, 0, 0, client);


            System.out.println("产生ECC结果：" + ECCResult);

            //KEK
            keyType = 0x03;
            int KEKKeyLength = 8;
            int KEYVerify = 0x01;

            boolean result = imp.hsmGenerateKey(keyType, index, RSAKeyModule, RSAKeyExponent,
                    pucPassword, ECCKeyMoudule, pucPassword, KEKKeyLength, KEYVerify, client);

            if (result) {
                System.out.println("产生成功");
            } else {
                System.out.println("产生失败");
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    /**
     * 测试产生ECC密钥对
     */
    private static void testHsmGenerateKeyPairECC() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试产生ECC密钥对");
        int curveId = 6;
        int keyTypeId = 0;
        int keyIndex = 9;
        String password = "11111111";

        Client client = null;

        try {
            client = connectPool.getClient();

//            byte[] wk = imp.hsmGenerateKeyWithKEK(keyIndex, 16, client);   //todo
//            byte[] keyHandle = imp.hsmImportKeyWithKEK(keyIndex, wk, client);

            boolean result = imp.hsmECCGenerateKeyPair(curveId, keyIndex, password, client);

            System.out.println("result is " + result);

//            imp.hsmDestoryKey(keyHandle, client);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    /**
     * 测试用外部ECC公钥保护导出会话密钥
     */
    private static void testHsmECCExportSessionKeyByHandle() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试用外部ECC公钥保护导出会话密钥");

        int keyIndex = 1;
        int algId = 6;
        Client client = null;

        try {
            client = connectPool.getClient();

            //使用keyindex以keyhandle形式传入
//            byte[] kh = ByteArrayUtil.BigEndian.toByteArray(keyIndex,2);

            List<Object> wk = imp.hsmGenerateKeyWithKEK(keyIndex, 16, client);
            byte[] keyHandle = (byte[]) wk.get(0);

            KeyPairs keyPairs = imp.hsmSM2GenerateKeyPair(256, algId, client);

            byte[] sessionKeyByHandle = imp.hsmSM2ExportSessionKeyByHandle(keyHandle, keyPairs.getPubkey(), client);   //todo

            System.out.println("会话密钥：" + ByteArrayUtil.toHexString(sessionKeyByHandle));

            imp.hsmDestoryKey(keyHandle, client);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    /**
     * 测试跨会话hash
     */
    private static void testHsmCrossSessionDoHash() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试会话密钥句柄做HMAC-SM3运算");

        String userId = "12345678";
        byte[] data = "hello world".getBytes();
//        byte[] keyHandle = ByteArrayUtil.BigEndian.toByteArray(1, 2);

        Client client = null;

        try {
            client = connectPool.getClient();

            KeyPairs keyPairs = imp.hsmSM2GenerateKeyPair(256, 6, client);

//            List<Object> wk = imp.hsmGenerateKeyWithKEK(1, 16, client);
//            byte[] keyHandle = (byte[])wk.get(0);

            byte[] keyCipher = ByteArrayUtil.hexString2Bytes("0f4791b63a58cc576d86b0f8bcc86106");

            byte[] keyHandle = imp.hsmImportKeyWithKEK(1, keyCipher, client);

            byte[] context = imp.hsmHashCrossSessionInit(HSMConstant.SGD_HASH_HMAC_SM3, keyHandle, keyPairs.getPubkey(), userId, null, null, client);

            imp.hsmDestoryKey(keyHandle, client);

//            System.out.println("hash one step,context is " + ByteArrayUtil.toHexString(context));

            byte[] context11 = imp.hsmHashCrossSessionUpdate(context, data, client);

//            System.out.println("hash two step,context is " + ByteArrayUtil.toHexString(context11));

            byte[] hashResult = imp.hsmHashCrossSessionFinal(context11, client);

            System.out.println("hash result is " + ByteArrayUtil.toHexString(hashResult));

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    /**
     * 测试删除内部密钥
     */
    private static void testHsmDeleteInternalKey() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试删除内部密钥");

        int keyIndex = 19;

        Client client = null;

        try {
            client = connectPool.getClient();

            boolean result = imp.hsmDeleteInternalKey(HSMConstant.KEY_TYPE_ECC, keyIndex, pucPassword, client);

            System.out.println("删除E内部密钥结果：" + result);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    private static void testSubStepHash() {
        Client client = null;
        String userId = "12345678";
        byte[] data = "hello world".getBytes();
        try {
            client = connectPool.getClient();

            PublicKey publicKey = imp.hsmSM2GetPublicKey(1, client);
            int nArithmetic = HSMConstant.SGD_SCH;
            byte[] byteDeviceID = imp.hsmHashInit(nArithmetic, 6, 0, publicKey, userId.getBytes(), client);

            int packetLength = 1024;//分包大小

            byte[][] packetData = ByteArrayUtil.dataPacketed(data, packetLength);

            for (byte[] pd : packetData) {
                imp.hsmHashUpdate(byteDeviceID, pd, client);
            }

            byte[] hashData = imp.hsmHashFinal(byteDeviceID, client);

            System.out.println("hash data is " + ByteArrayUtil.toHexString(hashData));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectPool.release(client);
        }

    }


    private static void testHsmSignP7SM2Detach() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试用于P7格式的SM2签名运算");

        Client client = null;

        try {
            client = connectPool.getClient();

            byte[] result = imp.hsmSignP7SM2Detach(0, null, "hello".getBytes(), "hello".getBytes(), client);

            System.out.println("删除ECC密钥对结果：" + ByteArrayUtil.toHexString(result));

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    private static void testHsmSignP7SM2DetachNoHahs() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试用于P7格式的SM2签名运算（不做hash）");

        Client client = null;

        try {
            client = connectPool.getClient();

            byte[] result = imp.hsmSignP7SM2DetachNoHash(0, null, "hello".getBytes(), "hello".getBytes(), client);

            System.out.println("删除ECC密钥对结果：" + ByteArrayUtil.toHexString(result));

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    public static void testConfPath() {
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        System.out.println(url);
    }

    public static PublicKey getPublicKeyForCer(String path) {
        PublicKey publicKey = null;
        try {
            File file = new File(path);
            if (file.exists()) {
                FileInputStream in = new FileInputStream(file);
//                Security.addProvider(new BouncyCastleProvider());
                Security.addProvider(new WstBouncyCastleProvider());

                CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
                X509Certificate certificate = (X509Certificate) cf.generateCertificate(in);
                publicKey = certificate.getPublicKey();
                System.out.println("publicKey is " + ByteArrayUtil.toHexString(publicKey.getEncoded()));
            } else {
                System.out.println("file is not exist");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return publicKey;
    }


    public static void testSM2ExternalVerify() {
        String algType = HSMConstant.WST_ALG_TYPE;

        byte[] input = Base64.decode("MTIzNDU2Nzg5MDEyMzQ1Njc4OTA=");

//        String src = "asfsadfsadfsadfasfsadf1557195350";
//
//        System.out.println("src is " + ByteArrayUtil.toHexString(src.getBytes()));

        String userId = "1234567812345678";
        byte[] signData = Base64.decode("MEYCIQDAyEm0/sXI/+7G9AwQ9XMnLnp/ODCnZKDvy6lN2mcILwIhAOcbJhVFmFCXUmZlESRz6EHy\n" +
                "NhkPFRTy6JYH8KAsGwtD");

        System.out.println("length is " + signData.length);

//        byte[] newSignData = Base64.decode("MEQCIBQyDhbWcOez+KMlQgpGwPUSEH3DadRIRRNKBaMyZNMwAiBe3D9U3UPegMVS18wJ9sAWJChm\n" +
//                "Axw3WHXqW/VTra+8MQ==");

//        System.out.println("length is " + newSignData.length);
//
//        System.out.println("new signData is " + ByteArrayUtil.toHexString(newSignData));

        System.out.println("signData is " + ByteArrayUtil.toHexString(signData));

        byte[] derDecode = DataConverUtil.DerDecode(signData);

        derDecode = CommonUtil.SM2SignWst2GB(derDecode);

        System.out.println("derDecode is " + ByteArrayUtil.toHexString(derDecode));

        Client client = null;
        try {
            client = connectPool.getClient();
            PublicKey publicKey = getPublicKeyForCer("D:\\user.cer");

            System.out.println("publicKey is " + ByteArrayUtil.toHexString(publicKey.getEncoded()));

            byte[] hashData = imp.doHash(0, publicKey, input, userId, client);

            boolean ret = imp.hsmSM2ExternalVerify(algType, publicKey, derDecode, hashData, client);

            System.out.println("verify result is " + ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void testHsmSm2EncryptWithSm4() {

        int keyIndex = 1;
        int algId = 6;
        int encryptAlgID = HSMConstant.SGD_SM4_ECB;
        byte[] data = "1111111111111111".getBytes();
        Client client = null;

        try {
            client = connectPool.getClient();
            //1、产生受加密机keyIndex索引密钥保护的临时对称密钥密文

            //使用keyindex以keyhandle形式传入
//            byte[] kh = ByteArrayUtil.BigEndian.toByteArray(keyIndex,2);

            List<Object> wk = imp.hsmGenerateKeyWithKEK(keyIndex, 16, client);

            //2、拿到临时对称密钥的句柄
            byte[] keyHandle = (byte[]) wk.get(0);

            //3、用临时对称密钥句柄对敏感数据做SM4加密
            byte[] cipher = imp.hsmEncrypt(keyHandle, encryptAlgID, null, data, client);

            System.out.println("cipher is " + ByteArrayUtil.toHexString(cipher));

            KeyPairs keyPairs = imp.hsmSM2GenerateKeyPair(256, algId, client);

            //4、用外传的SM2公钥保护导出临时对称密钥
            byte[] sessionKeyByHandle = imp.hsmSM2ExportSessionKeyByHandle(keyHandle, keyPairs.getPubkey(), client);

            System.out.println("会话密钥：" + ByteArrayUtil.toHexString(sessionKeyByHandle));

            //5、拼装第4步和第3步的密文
            byte[] result = new byte[196];

            System.arraycopy(sessionKeyByHandle, 0, result, 0, sessionKeyByHandle.length);

            System.arraycopy(cipher, 0, result, sessionKeyByHandle.length, cipher.length);

            System.out.println("result is " + ByteArrayUtil.toHexString(result));

            //6、销毁临时密钥
            imp.hsmDestoryKey(keyHandle, client);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    //-----------------------------------------------IBK----------------------------------------------------------

    public static void testGenerateKeyPairIBK() {
        Client client = null;
        byte[] userId = "1234567812345678".getBytes();
        byte[] appendData = "1234567812345678".getBytes();
        int uiKeyBits = 256;
        int uiAlgID = 6;

        try {
            client = connectPool.getClient();

            //产生SM2公私钥对
            KeyPairs keyPairs = imp.hsmSM2GenerateKeyPair(uiKeyBits, uiAlgID, client);

            PublicKey publicKey = keyPairs.getPubkey();

            //转换成byte[]
            SM2RefPublicKey refPublic = DataConverUtil.PublicKey2Ref(publicKey);
            refPublic = CommonUtil.SM2Key512to256(refPublic);
            byte[] pubBytes = refPublic.getBytes();

            List<byte[]> keyPairIBK = imp.hsmGenerateKeyPairIBK(1, 6, 256, pubBytes, userId, appendData, client);

            System.out.println("publicKey is " + ByteArrayUtil.toHexString(keyPairIBK.get(0)));
            System.out.println("privateKey cipher is " + ByteArrayUtil.toHexString(keyPairIBK.get(1)));

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    public static void testInternalSignIBK() {
        byte[] data = "hello world".getBytes();
        Client client = null;
        int algId = 6;
        byte[] userId = "12345678123456781234567812345678".getBytes();
        int keyIndex = 1;

        try {
            client = connectPool.getClient();

            byte[] hashData = imp.hsmHash(HSMConstant.SGD_SM3, 1, null, userId, data, client);

            System.out.println("hash data is " + ByteArrayUtil.toHexString(hashData));

            imp.hsmGetPrivateKeyAccessRight(keyIndex, "11111111", client);
            byte[] signData = imp.hsmSM2InternalSignIBK(algId, keyIndex, hashData, userId, client);
            System.out.println("内部IBK签名成功,签名内容：" + ByteArrayUtil.toHexString(signData));

            boolean ret = imp.hsmSM2InternalVerifyIBK(algId, keyIndex, signData, hashData, userId, client);
            System.out.println("内部IBK验签密成功,结果：" + ret);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    public static void testHsmSM2ExternalSignIBK() {
        byte[] data = "hello world".getBytes();
        Client client = null;
        int algId = 6;
        byte[] userId = "12345678123456781234567812345678".getBytes();
        int uiKeyBits = 256;
        int uiAlgID = 6;

        try {
            client = connectPool.getClient();
            KeyPairs keypairs = imp.hsmSM2GenerateKeyPair(uiKeyBits, uiAlgID, client);
            PublicKey pubKeySm2 = keypairs.getPubkey();

            //转换成byte[]
            SM2RefPublicKey refPublic = DataConverUtil.PublicKey2Ref(pubKeySm2);
            refPublic = CommonUtil.SM2Key512to256(refPublic);
            byte[] pubBytes = refPublic.getBytes();

            byte[] hashData = imp.hsmHash(HSMConstant.SGD_SM3, 1, null, userId, data, client);

            List<byte[]> keyPairIBK = imp.hsmGenerateKeyPairIBK(1, 6, 256, pubBytes, userId, null, client);

            byte[] publicKey = keyPairIBK.get(0);
            byte[] privateKeyCipher = keyPairIBK.get(1);

            System.out.println("hash data is " + ByteArrayUtil.toHexString(hashData));

            byte[] signData = imp.hsmSM2ExternalSignIBK(algId, privateKeyCipher, hashData, userId, client);
            System.out.println("外部IBK签名成功,签名内容：" + ByteArrayUtil.toHexString(signData));

            boolean ret = imp.hsmSM2ExternalVerifyIBK(algId, publicKey, signData, hashData, userId, client);
            System.out.println("外部IBK验签密成功,结果：" + ret);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }


    public static void testCreateFile() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        try {
            client = pool.getClient();

            byte[] fileData = Files.readAllBytes(Paths.get("D:\\plain.txt"));

            boolean result = imp.hsmCreateFile(fileName, fileData.length, client);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {

            pool.release(client);
        }
    }

    public static void testWriteFile() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        try {
            client = pool.getClient();

            byte[] fileData = Files.readAllBytes(Paths.get("D:\\plain.txt"));

            boolean result = imp.hsmWriteFile(fileName, fileData, client);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {

            pool.release(client);
        }
    }

    public static void testReadFile() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        try {
            client = pool.getClient();

            byte[] initData = Files.readAllBytes(Paths.get("D:\\plain.txt"));

            byte[] data = imp.hsmReadFile(fileName, 0, initData.length, client);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {

            pool.release(client);
        }
    }


    public static void testDeleteFile() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        try {
            client = pool.getClient();

            boolean result = imp.hsmDeleteFile(fileName, client);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {

            pool.release(client);
        }
    }


    public static void testExportSM2KeyWithGMT1618_Internal() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        int keyIndex = 1;
        int protectedIndex = 1;
        int protectedBlobFormat = ProtectedBlobFormatEnum.GMT0018_SM2_512.getValue();
        String hsmPwd = "11111111";

        try {
            client = pool.getClient();

            byte[] blobData = imp.hsmExportSM2WithGMT1618Internal(hsmPwd, keyIndex, protectedIndex, protectedBlobFormat, hsmPwd, client);

            System.out.println("blobData value is " + ByteArrayUtil.toHexString(blobData));

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            pool.release(client);
        }
    }


    public static void testExportSM2KeyWithGMT1618_External() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        int keyIndex = 1;
        int protectedBlobFormat = ProtectedBlobFormatEnum.GMT0018_SM2_512.getValue();
        String hsmPwd = "11111111";

        try {
            client = pool.getClient();

            PublicKey publicKey = imp.hsmSM2GetPublicKey(keyIndex, client);

            byte[] protectedPk = DataConverUtil.SM2PublicKey2Bytes(publicKey);

            byte[] blobData = imp.hsmExportSM2WithGMT1618External(hsmPwd, keyIndex, protectedBlobFormat, protectedPk, client);

            System.out.println("blobData value is " + ByteArrayUtil.toHexString(blobData));

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            pool.release(client);
        }
    }


    public static void testImportSM2KeyWithGMT1618() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        int keyIndex = 1;
        int protectedIndex = 1;
        int protectedBlobFormat = ProtectedBlobFormatEnum.GMT0016_SM2_512.getValue();
        String hsmPwd = "11111111";

        try {
            client = pool.getClient();

            byte[] blobData = imp.hsmExportSM2WithGMT1618Internal(hsmPwd, keyIndex, protectedIndex, protectedBlobFormat, hsmPwd, client);

            boolean result = imp.hsmImportSM2WithGMT1618(hsmPwd, keyIndex, protectedIndex, protectedBlobFormat, hsmPwd, blobData, client);

            System.out.println("import result is " + result);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            pool.release(client);
        }
    }


    public static void testExportSM2KeyWithGMT0009_Internal() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        int keyIndex = 1;
        int protectedIndex = 1;
        String hsmPwd = "11111111";

        try {
            client = pool.getClient();

            byte[] blobData = imp.hsmExportSM2WithGMT0009Internal(hsmPwd, keyIndex, protectedIndex, hsmPwd, client);

            System.out.println("blobData value is " + ByteArrayUtil.toHexString(blobData));

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            pool.release(client);
        }
    }


    public static void testExportSM2KeyWithGMT0009_External() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        int keyIndex = 1;
        String hsmPwd = "11111111";

        try {
            client = pool.getClient();

            PublicKey publicKey = imp.hsmSM2GetPublicKey(keyIndex, client);

            byte[] protectedPk = DataConverUtil.SM2PublicKey2Bytes(publicKey);

            byte[] blobData = imp.hsmExportSM2WithGMT0009External(hsmPwd, keyIndex, protectedPk, client);

            System.out.println("blobData value is " + ByteArrayUtil.toHexString(blobData));

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            pool.release(client);
        }
    }


    public static void testImportSM2KeyWithGMT0009() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        int keyIndex = 1;
        int protectedIndex = 1;
        String hsmPwd = "11111111";

        try {
            client = pool.getClient();

            byte[] blobData = imp.hsmExportSM2WithGMT0009Internal(hsmPwd, keyIndex, protectedIndex, hsmPwd, client);

            boolean result = imp.hsmImportSM2WithGMT0009(hsmPwd, keyIndex, protectedIndex, hsmPwd, blobData, client);

            System.out.println("import result is " + result);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            pool.release(client);
        }
    }


    public static void testSM2ExternalVerifyEx() {
        Client client = null;

        byte[] data = "12345".getBytes();

        try {
            client = connectPool.getClient();

            //sign
            byte[] signature = imp.hsmSM2InternalSignEx(data, client);

            System.out.println("signature is " + ByteArrayUtil.toHexString(signature));

            PublicKey publicKey = imp.hsmSM2GetPublicKey(1, client);

            boolean ret = imp.hsmSM2ExternalVerifyEx(data, signature, publicKey, client);

            System.out.println("verify result is " + ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] arg0) {

        try {
            //初始化连接
            connectPool.initClient();
            testHsmSm2EncryptWithSm4();
            testHsmSm2EncryptWithSm4();

            testHsmCrossSessionDoHash();

            testSM2ExternalVerify();
            testHsmSM2ExternalSign();
            testConfPath();

            //测试RSA公钥转x509、私钥转pkcs8格式
            testRSATransferFomat();

            testHsmRSAGenerateKeyPair();

            //产生SM2公私钥对
            testHsmSM2GenerateKeyPair();

            //获取SM2公钥
            testHsmSM2GetPublicKey();

            //内部SM2加解密
            testHsmSM2InternalPrivateKeyDec();

            //外部SM2加解密
            testHsmSM2ExternalPublicKeyEnc();

            //内部SM2签名验签
            testHsmSM2InternalSign();

            //外部SM2签名
            testHsmSM2ExternalSign();

            //外部RSA加解密
            testHsmRSAExternalPublicKeyEnc();

            //内部RSA加解密
            testHsmRSAInternalPublicKeyEnc();

            //内部RSA签名验签
            testHsmRSAInternalSign();

            //外部RSA签名验签
            testHsmRSAExternalSign();

            //获取RSA2公钥
            testHsmRSAGetPublicKey();

            //用外部ECC公钥保护导出会话密钥
            testHsmECCExportSessionKeyByHandle();

            //哈希运算
            testHsmCrossSessionDoHash();

            //产生ECC密钥对并输出由会话密钥保护的私钥密文
            testHsmGenerateKeyPairECC();

            //删除密钥对
//            testHsmDeleteInternalKey();

            //分步哈希
            testSubStepHash();

            testHsmSignP7SM2DetachNoHahs();

            testHsmSM2GenerateKeyWithIPK();

            testHsmCalculateMAC();

            testHsmSM2ImportKeyWithISK();

            testHsmEncrypt();

            testHsmSM2ExchangeDigitEnvelope();

            testHsmSM2GenerateKeyWithEPK();

            //内部ECC标识签名验签
            testInternalSignIBK();

            //外部ECC标识签名验签
            testHsmSM2ExternalSignIBK();

            //标识密钥生成
            testGenerateKeyPairIBK();

            testhsmHash();

            //创建文件
            testReadFile();

            //写文件
            testWriteFile();

            //读文件
            testReadFile();

            //删除文件
            testDeleteFile();

            //导出SM2密钥GMT0016/0018（内部索引方式）
            testExportSM2KeyWithGMT1618_Internal();

            //导出SM2密钥GMT0016/0018（外部密钥方式）
            testExportSM2KeyWithGMT1618_External();

            //导入SM2密钥GMT0016/0018
            testImportSM2KeyWithGMT1618();

            //导出SM2密钥GMT0009（内部索引方式）
            testExportSM2KeyWithGMT0009_Internal();

            //导出SM2密钥GMT0009（外部密钥方式）
            testExportSM2KeyWithGMT0009_External();

            //导入SM2密钥GMT0009
            testImportSM2KeyWithGMT0009();

            //签名验签扩展测试
            testSM2ExternalVerifyEx();

            //基于ECC算法的数字信封转换
            testHsmSM2ExchangeDigitEnvelope();

            System.exit(0);


        } catch (Exception e) {
            System.out.println("Exception :" + e);
        }


    }

}
