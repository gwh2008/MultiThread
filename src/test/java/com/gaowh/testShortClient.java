package com.gaowh;

import cn.com.westone.common.array.ByteArrayUtil;
import com.westone.pboc.hsm.HSMConstant;
import com.westone.pboc.hsm.constants.ProtectedBlobFormatEnum;
import com.westone.pboc.hsm.entity.KeyPairs;
import com.westone.pboc.hsm.entity.SM2RefPublicKey;
import com.westone.pboc.mina.client.Client;
import com.westone.pboc.mina.client.ClientThreadPool;
import com.westone.pboc.mina.client.ShortClient;
import com.westone.pboc.service.imp.HSMWSTApiServiceImp;
import com.westone.pboc.util.CommonUtil;
import com.westone.pboc.util.DataConverUtil;
import org.junit.Assert;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;


/**
 * 短链接模式测试功能
 */
public class testShortClient {
    private static HSMWSTApiServiceImp imp;

    static {
        imp = new HSMWSTApiServiceImp();
    }

    private static PrivateKey prikeySm2 = null;
    private static PublicKey pubkeySm2 = null;

    private static PrivateKey prikeyRsa = null;
    private static PublicKey pubkeyRsa = null;

    private static String pucPassword = "11111111";

    private static byte[] fileName = "测试中文".getBytes(StandardCharsets.UTF_8);

    private static ShortClient shortClient = ShortClient.getInstance("D:\\hsmapi-all\\hsmapi-0901-bc159\\hsm-api\\src\\main\\resources\\conf\\hsminfo0901.properties");


    //索引号  RSA-11  SM2-12  KEK-13

    //#############################################设备管理类函数################################################

    /**
     * 测试产生随机数
     */
    private static void testHsmGenerateRandom() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试产生随机数");
        int length = 10;
        Client client = null;

        ShortClient shortClient = ShortClient.getInstance();

        try {
            client = shortClient.getClient();
            byte[] random = imp.hsmGenerateRandom(length, client);
            System.out.println("产生随机数为：" + ByteArrayUtil.toHexString(random));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void testHsmEncryptAndDecrypt() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试对称加解密");
        int length = 10;
        int keyIndex = 1;
        int uiKeyBits = 16;
        int algID = HSMConstant.SGD_SM4_ECB;
        byte[] iv = null;
        Client client = null;

        try {
            client = shortClient.getClient();

            byte[] pucData = "1234567890abcdef".getBytes();

//            byte[] kh = ByteArrayUtil.BigEndian.toByteArray(keyIndex,2);

            List<Object> wk = imp.hsmGenerateKeyWithKEK(keyIndex, uiKeyBits, client);
            byte[] keyHandle = (byte[]) wk.get(0);
            byte[] cipher = imp.hsmEncrypt(keyHandle, algID, iv, pucData, client);
            byte[] plain = imp.hsmDecrypt(keyHandle, algID, iv, cipher, client);
            boolean ok = imp.hsmDestoryKey(keyHandle, client);

            imp.hsmDestoryKey(keyHandle, client);

            System.out.println("密文为：" + ByteArrayUtil.toHexString(cipher));
            System.out.println("明文为：" + ByteArrayUtil.toHexString(plain));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 测试获取、释放私钥权限
     */
    private static void testGetPrivateKeyAccessRight() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试获取私钥权限");
        int keyIndex = 11;
        String password = "11111111";
        Client client = null;

        try {
            client = shortClient.getClient();

            boolean right = imp.hsmGetPrivateKeyAccessRight(keyIndex, password, client);
            System.out.println("获取私钥权限为：" + right);

            boolean right2 = imp.hsmReleasePrivateKeyAccessRight(keyIndex, client);

            System.out.println("释放私钥权限为：" + right2);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //#############################################密钥管理类函数#########################################################

    /**
     * 测试获取RSA公钥 ok
     */
    private static void testHsmRSAGetPublicKey() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试获取RSA公钥");
        int keyIndex = 0;

        Client client = null;

        try {
            client = shortClient.getClient();
            PublicKey pubKey = imp.hsmRSAGetPublicKey(keyIndex, client);
            System.out.println("获取RSA2公钥成功,公私钥：" + pubKey.getAlgorithm());
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 测试产生RSA密钥对  ok
     */
    private static void testHsmRSAGenerateKeyPair() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试产生RSA密钥对");
        int uiKeyBits = 1024;

        Client client = null;

        try {
            client = shortClient.getClient();
            KeyPairs keypairs = imp.hsmRSAGenerateKeyPair(uiKeyBits, client);
            prikeyRsa = keypairs.getPrikey();
            pubkeyRsa = keypairs.getPubkey();
            System.out.println("产生RSA密钥对成功,新生成私钥：" + keypairs.getPrikey() + ",公钥:" + keypairs.getPubkey());
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 测试基于RSA算法的数字信封转换
     */
    private static void testHsmRSAExchangeDigitEnvelope() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试基于RSA算法的数字信封转换");
        int keyIndex = 11;
        byte[] inputData = "1234abcd1234abcd".getBytes();

        Client client = null;

        try {
            client = shortClient.getClient();
            PublicKey publicKey = imp.hsmRSAGetPublicKey(keyIndex, client);
            imp.hsmGetPrivateKeyAccessRight(keyIndex + 500, "11111111", client);
            byte[] keyData = imp.hsmRSAExchangeDigitEnvelope(keyIndex, publicKey, inputData, client);
            System.out.println("基于RSA算法的数字信封转换成功,生成数据：" + ByteArrayUtil.toHexString(keyData));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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

            client = shortClient.getClient();
            KeyPairs keypairs = imp.hsmSM2GenerateKeyPair(uiKeyBits, uiAlgID, client);
            prikeySm2 = keypairs.getPrikey();
            pubkeySm2 = keypairs.getPubkey();
            System.out.println("产生SM2密钥对成功,新生成私钥：" + keypairs.getPrikey() + ",公钥:" + keypairs.getPubkey());
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
            client = shortClient.getClient();
            PublicKey pubKey = imp.hsmSM2GetPublicKey(keyIndex, client);
            System.out.println("获取SM2公钥成功,公钥：" + ByteArrayUtil.toHexString(pubKey.getEncoded()));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
            client = shortClient.getClient();
            List<Object> list = imp.hsmRSAGenerateKeyWithIPK(keyIndex, uiKeyBits, client);
            byte[] keyHandle = (byte[]) list.get(0);
            byte[] keyData = (byte[]) list.get(1);
            System.out.println("生成会话密钥并用内部RSA公钥加密输出成功,生成会话密钥：" + ByteArrayUtil.toHexString(keyData));

            imp.hsmDestoryKey(keyHandle, client);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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

            client = shortClient.getClient();
            PublicKey publicKey = imp.hsmRSAGetPublicKey(keyIndex, client);

            List<Object> list = imp.hsmRSAGenerateKeyWithEPK(uiKeyBits, publicKey, client);
            byte[] keyHandle = (byte[]) list.get(0);
            byte[] keyData = (byte[]) list.get(1);
            System.out.println("生成会话密钥并用外部RSA公钥加密输出成功,生成会话密钥：" + ByteArrayUtil.toHexString(keyData));

            imp.hsmDestoryKey(keyHandle, client);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 测试生成会话密钥并用外部ECC公钥加密输出
     */
    private static void testHsmSM2GenerateKeyWithEPK() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试生成会话密钥并用外部ECC公钥加密输出");
        int keyIndex = 12;
        int algID = 6;
        int uiKeyBits = 256;

        Client client = null;

        try {

            client = shortClient.getClient();
            PublicKey publicKey = imp.hsmSM2GetPublicKey(keyIndex, client);
            List<Object> list = imp.hsmSM2GenerateKeyWithEPK(uiKeyBits, algID, publicKey, client);
            byte[] keyHandle = (byte[]) list.get(0);
            byte[] keyData = (byte[]) list.get(1);
            System.out.println("生成会话密钥并用外部ECC公钥加密输出成功,生成会话密钥：" + ByteArrayUtil.toHexString(keyData));

            imp.hsmDestoryKey(keyHandle, client);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 测试生成会话密钥并用内部ECC公钥加密输出
     */
    private static void testHsmSM2GenerateKeyWithIPK() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试生成会话密钥并用内部ECC公钥加密输出");
        int keyIndex = 12;
        int uiKeyBits = 256;

        Client client = null;

        try {
            client = shortClient.getClient();

//            byte[] kh = ByteArrayUtil.BigEndian.toByteArray(keyIndex,2);

            List<Object> list = imp.hsmSM2GenerateKeyWithIPK(keyIndex, uiKeyBits, client);
            byte[] keyHandle = (byte[]) list.get(0);
            byte[] keyData = (byte[]) list.get(1);
            System.out.println("生成会话密钥并用内部ECC公钥加密输出成功,生成会话密钥：" + ByteArrayUtil.toHexString(keyData));

            imp.hsmDestoryKey(keyHandle, client);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 测试生成会话密钥并用密钥加密密钥加密输出
     */
    private static void testHsmGenerateKeyWithKEK() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试生成会话密钥并用密钥加密密钥加密输出");
        int keyIndex = 13;
        int algID = 6;
        int uiKeyBits = 256;

        Client client = null;

        try {
            client = shortClient.getClient();

//            byte[] kh = ByteArrayUtil.BigEndian.toByteArray(keyIndex,2);

            List<Object> list = imp.hsmGenerateKeyWithKEK(keyIndex, uiKeyBits, client);
            byte[] keyHandle = (byte[]) list.get(0);
            byte[] keyData = (byte[]) list.get(1);
            System.out.println("生成会话密钥并用密钥加密密钥加密输出成功,生成会话密钥：" + ByteArrayUtil.toHexString(keyData));

            imp.hsmDestoryKey(keyHandle, client);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
            client = shortClient.getClient();
            byte[] keyHandle = imp.hsmImportKey(keyData, client);

            System.out.println("导入明文会话密钥成功,密钥handle为：" + ByteArrayUtil.toHexString(keyHandle));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
            client = shortClient.getClient();
            imp.hsmGetPrivateKeyAccessRight(keyIndex + 500, "11111111", client);
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
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 测试导入会话密钥并用密钥加密密钥解密
     */
    private static void testHsmImportKeyWithKEK() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试导入会话密钥并用密钥加密密钥解密");
        int keyIndex = 3;
        int algID = 6;
        byte[] keyData = "12345678abcdabcd".getBytes();

        Client client = null;

        try {
            client = shortClient.getClient();

//            byte[] kh = ByteArrayUtil.BigEndian.toByteArray(keyIndex,2);

            byte[] keyHandle = imp.hsmImportKeyWithKEK(keyIndex, keyData, client);

            System.out.println("导入会话密钥并用密钥加密密钥解密成功,密钥handle为：" + ByteArrayUtil.toHexString(keyHandle));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 测试导入会话密钥并用内部ECC私钥解密
     */
    private static void testHsmSM2ImportKeyWithISK() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试导入会话密钥并用内部ECC私钥解密");
        int keyIndex = 12;
        int uiKeyBits = 256;

        Client client = null;

        try {
            client = shortClient.getClient();
            imp.hsmGetPrivateKeyAccessRight(keyIndex, "11111111", client);

//            byte[] kh = ByteArrayUtil.BigEndian.toByteArray(keyIndex,2);

            List<Object> list = imp.hsmSM2GenerateKeyWithIPK(keyIndex, uiKeyBits, client);
            byte[] keyHandle = (byte[]) list.get(0);
            byte[] keyData = (byte[]) list.get(1);
            byte[] keyHandle1 = imp.hsmSM2ImportKeyWithISK(keyIndex, keyData, client);

            System.out.println("导入会话密钥并用内部ECC私钥解密成功,密钥handle为：" + ByteArrayUtil.toHexString(keyHandle1));

            imp.hsmDestoryKey(keyHandle, client);
            imp.hsmDestoryKey(keyHandle1, client);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
            client = shortClient.getClient();
            List<Object> result = imp.hsmSM2GenerateAgreementData(keyIndex, uiKeyBits, sponsorID, client);

            System.out.println("生成密钥协商参数并输出成功,密钥参数为：" + result.get(0) + ",pubKey:" + result.get(1));

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
            client = shortClient.getClient();
            PublicKey publicKey = imp.hsmSM2GetPublicKey(keyIndex, client);
            PublicKey tmpPublicKey = imp.hsmSM2GetPublicKey(keyIndex, client);

            byte[] handle = imp.hsmSM2GenerateKey(keyIndex, agreementHandle, responseID, publicKey, tmpPublicKey, client);
            System.out.println("计算会话密钥成功,生成会话密钥handle：" + ByteArrayUtil.toHexString(handle));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
            client = shortClient.getClient();
            boolean right = imp.hsmGetPrivateKeyAccessRight(keyIndex, "11111111", client);
            PublicKey publicKey = imp.hsmSM2GetPublicKey(keyIndex, client);
            PublicKey tmpPublicKey = imp.hsmSM2GetPublicKey(keyIndex, client);

            List<Object> result = imp.hsmSM2GenerateAgreementDataAndKey(keyIndex, uiKeyBits, sponsorID, responseID, publicKey, tmpPublicKey, client);

            System.out.println("产生协商参数并计算会话密钥成功,密钥参数为：" + result.get(0) + ",pubKey:" + result.get(1));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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

        Client client = null;

        try {
            client = shortClient.getClient();
            KeyPairs keypairs = imp.hsmSM2GenerateKeyPair(256, 6, client);
            prikeySm2 = keypairs.getPrikey();
            pubkeySm2 = keypairs.getPubkey();

            PublicKey publicKey = imp.hsmSM2GetPublicKey(keyIndex, client);
            boolean right = imp.hsmGetPrivateKeyAccessRight(keyIndex, "11111111", client);
            byte[] encData = imp.hsmSM2ExternalPublicKeyEnc(HSMConstant.WST_ALG_TYPE, publicKey, inData, client);

            byte[] dd = CommonUtil.SM2CipherGBtoWST(encData);

            byte[] outData = imp.hsmSM2ExchangeDigitEnvelope(keyIndex, algID, publicKey, dd, client);
            System.out.println("基于ECC算法的数字信封转换成功,转换生成数据：" + ByteArrayUtil.toHexString(outData));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 测试销毁会话密钥
     */
    private static void testHsmDestoryKey() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试销毁会话密钥");
        byte[] keyData = "abcd1234abcd1234".getBytes();

        Client client = null;

        try {
            client = shortClient.getClient();
            byte[] keyHandle = imp.hsmImportKey(keyData, client);
            boolean ret = imp.hsmDestoryKey(keyHandle, client);
            System.out.println("销毁会话密钥成功,结果为：" + ret);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //######################################################非对称算法##################################

    /**
     * 测试外部公钥RSA加密、私钥解密  ok
     */
    private static void testHsmRSAExternalPublicKeyEnc() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试外部公钥RSA加密");

        String src = "11111111111111111111111111111111111111111111111111111111111111111111111";

        Client client = null;

        try {
            client = shortClient.getClient();
            KeyPairs keypairs = imp.hsmRSAGenerateKeyPair(1024, client);
            prikeyRsa = keypairs.getPrikey();
            pubkeyRsa = keypairs.getPubkey();

            byte[] encData = imp.hsmRSAExternalPublicKeyEnc(pubkeyRsa, src.getBytes(), client);
            System.out.println("外部公钥RSA加密成功,密文：" + ByteArrayUtil.toHexString(encData));

            byte[] pucData = imp.hsmRSAExternalPrivateKeyDec(prikeyRsa, encData, client);
            System.out.println("外部公钥RSA解密成功,明文：" + ByteArrayUtil.toHexString(pucData));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 测试内部公钥RSA加密、私钥解密  ok 解密 no
     */
    private static void testHsmRSAInternalPublicKeyEnc() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试外部公钥RSA加密");
        int keyIndex = 2;
        String src = "11111111111111111111111111111111111111111111111111111111111111111111111";
        System.out.println("内部公钥RSA加，加密明文：" + src);

        Client client = null;

        try {
            client = shortClient.getClient();
            byte[] encData = imp.hsmRSAInternalPublicKeyEnc(keyIndex, src.getBytes(), client);
            System.out.println("内部公钥RSA加密成功,密文：" + encData.length + ":" + ByteArrayUtil.toHexString(encData));

            imp.hsmGetPrivateKeyAccessRight(keyIndex + 500, "11111111", client);
            byte[] pucData = imp.hsmRSAInternalPrivateKeyDec(keyIndex, encData, pucPassword, client);
            System.out.println("内部私钥RSA解密成功,明文：" + ByteArrayUtil.toHexString(pucData));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 测试外部SM2签名、验签   ok
     */
    private static void testHsmSM2ExternalSign() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试外部SM2签名、验签");
        String src = "1111111111ffffffffff000000000012";

        Client client = null;

        try {
            client = shortClient.getClient();
            KeyPairs keypairs = imp.hsmSM2GenerateKeyPair(256, 6, client);
            prikeySm2 = keypairs.getPrikey();
            pubkeySm2 = keypairs.getPubkey();
            byte[] signData = imp.hsmSM2ExternalSign(HSMConstant.WST_ALG_TYPE, prikeySm2, src.getBytes(), client);
            System.out.println("外部SM2签名成功,签名内容：" + ByteArrayUtil.toHexString(signData));

            boolean ret = imp.hsmSM2ExternalVerify(HSMConstant.WST_ALG_TYPE, pubkeySm2, signData, src.getBytes(), client);
            System.out.println("外部SM2验签密成功,结果：" + ret);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 测试内部SM2签名、验签   ok
     */
    private static void testHsmSM2InternalSign() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试内部SM2签名、验签");
        int keyIndex = 2;
        String src = "1111111111ffffffffff000000000012";

        Client client = null;

        try {
            client = shortClient.getClient();
            boolean right = imp.hsmGetPrivateKeyAccessRight(keyIndex, "11111111", client);
            byte[] signData = imp.hsmSM2InternalSign(HSMConstant.WST_ALG_TYPE, keyIndex, src.getBytes(), pucPassword, client);
            System.out.println("内部SM2签名成功,签名内容：" + ByteArrayUtil.toHexString(signData));

            boolean ret = imp.hsmSM2InternalVerify(HSMConstant.WST_ALG_TYPE, keyIndex, signData, src.getBytes(), client);
            System.out.println("内部SM2验签密成功,结果：" + ret);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 测试外部SM2公钥加密、私钥解密   ok
     */
    private static void testHsmSM2ExternalPublicKeyEnc() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试外部SM2公钥加密、私钥解密");
        //String src="11111111012";
        String src = "1111111111ffffffffff000000000012";

        Client client = null;

        try {
            client = shortClient.getClient();
            KeyPairs keypairs = imp.hsmSM2GenerateKeyPair(256, 6, client);
            prikeySm2 = keypairs.getPrikey();
            pubkeySm2 = keypairs.getPubkey();
            byte[] encData = imp.hsmSM2ExternalPublicKeyEnc(HSMConstant.WST_ALG_TYPE, pubkeySm2, src.getBytes(), client);
            System.out.println("外部SM2公钥加密成功,密文内容：" + ByteArrayUtil.toHexString(encData));

            byte[] pucData = imp.hsmSM2ExternalPrivateKeyDec(HSMConstant.WST_ALG_TYPE, prikeySm2, encData, client);
            System.out.println("外部SM2公钥解密成功,结果：" + new String(pucData));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static void testHsmSM2InternalPrivateKeyDec() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试SM2内部密钥加密、解密");

        Client client = null;
        String src = "hello";

        try {
            client = shortClient.getClient();
            PublicKey pubKey = imp.hsmSM2GetPublicKey(2, client);
            byte[] encData = imp.hsmSM2ExternalPublicKeyEnc(HSMConstant.WST_ALG_TYPE, pubKey, src.getBytes(), client);
            System.out.println("SM2外部加密成功,结果：" + ByteArrayUtil.toHexString(encData));
            imp.hsmGetPrivateKeyAccessRight(2, "11111111", client);
            byte[] result = imp.hsmSM2InternalPrivateKeyDec(2, HSMConstant.WST_ALG_TYPE, encData, client);
            System.out.println("SM2内部密钥解密成功,结果：" + new String(result));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
        byte[] iv = "0000000000000000".getBytes();

        Client client = null;

        try {
            client = shortClient.getClient();
            byte[] handle = imp.hsmImportKey(keyData, client);
            byte[] encData = imp.hsmEncrypt(handle, HSMConstant.SGD_SM4_CBC, iv, "111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220".getBytes(), client);
            System.out.println("对称密钥加密成功,密文内容：" + encData.length + ", 内容：" + new String(encData));

            byte[] pucData = imp.hsmDecrypt(handle, HSMConstant.SGD_SM4_CBC, iv, encData, client);
            System.out.println("对称密钥解密成功,结果：" + new String(pucData));

            if (("111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220111111111111111122222222222222201111111111111111222222222222222011111111111111112222222222222220").equals(new String(pucData))) {
                System.out.println("true");
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 测试计算MAC
     */
    private static void testHsmCalculateMAC() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试计算MAC");
        //对称密钥加密 AES-密钥16位  des-密钥8位
//		int keyHandle = 0002;
        byte[] iv = "0000000000000000".getBytes();
        byte[] keyData = "abcd1234abcd1234abcd1234abcd1234".getBytes();

        byte[] inData = "abcd1234abcd1234abcd1234abcd1234".getBytes();

        Client client = null;

        try {
            client = shortClient.getClient();
            byte[] handle = imp.hsmImportKey(keyData, client);
            byte[] mac = imp.hsmCalculateMAC(handle, HSMConstant.SGD_SM4_MAC, iv, inData, client);
            System.out.println("计算MAC成功,MAC值：" + ByteArrayUtil.toHexString(mac));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //######################################################杂凑算法  ##################################

    /**
     * 测试计算摘要
     */
    private static void testhsmHash() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试计算摘要");
        String dataStr = "00000000001111111111";
        byte[] inData = dataStr.getBytes();
        Client client = null;
        byte[] userId = "1234567812345678".getBytes();

        try {
            client = shortClient.getClient();
            byte[] sign = imp.hsmHash(HSMConstant.SGD_SM3, 1, null, userId, inData, client);
            System.out.println("计算摘要成功,摘要值：" + ByteArrayUtil.toHexString(sign));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
            client = shortClient.getClient();
            byte[] sign = imp.hsmP7RSASign(nArithmetic, keyIndex, inData, client);
            System.out.println("私钥RSA签名P7成功,签名：" + new String(sign));
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 测试外部私钥RSA签名、 公钥验签  ok
     */
    private static void testHsmRSAExternalSign() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试外部私钥RSA签名");
        String src = "011111111ffffffff000000000" +
                "11111111ffffffff000000000" +
                "11111111ffffffff000000000" +
                "11111111ffffffff000000000" +
                "11111111ffffffff00000000000";
        Client client = null;

        try {
            client = shortClient.getClient();
            KeyPairs keypairs = imp.hsmRSAGenerateKeyPair(1024, client);
            prikeyRsa = keypairs.getPrikey();
            pubkeyRsa = keypairs.getPubkey();

            byte[] signData = imp.hsmRSAExternalSign(HSMConstant.HSM_SHA1_RSA_PKCS, prikeyRsa, src.getBytes(), client);
            System.out.println("外部私钥RSA签名成功,签名内容：" + new String(signData));

            boolean ret = imp.hsmRSAExternalVerify(HSMConstant.HSM_SHA1_RSA_PKCS, pubkeyRsa, signData, src.getBytes(), client);
            System.out.println("外部私钥RSA验签成功,结果：" + ret);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 测试内部私钥RSA签名、 公钥验签   no
     */
    private static void testHsmRSAInternalSign() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试内部私钥RSA签名");
        int index = 2;
        String src = "011111111ffffffff000000000" +
                "11111111ffffffff000000000" +
                "11111111ffffffff000000000" +
                "11111111ffffffff000000000" +
                "11111111ffffffff00000000000";
        Client client = null;

        try {
            client = shortClient.getClient();
            imp.hsmGetPrivateKeyAccessRight(500 + index, "11111111", client);
            byte[] signData = imp.hsmRSAInternalSign(HSMConstant.HSM_MD5_RSA_PKCS, index, src.getBytes(), pucPassword, client);
            System.out.println("内部私钥RSA签名成功,签名内容：" + ByteArrayUtil.toHexString(signData));

            boolean ret = imp.hsmRSAInternalVerify(HSMConstant.HSM_MD5_RSA_PKCS, index, signData, src.getBytes(), client);
            System.out.println("内部私钥RSA验签成功,结果：" + ret);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static void testHsmGenerateKey() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 测试产生密钥并保存在加密机中某索引位置");

        //RSA
        int keyType = 0x01;
        int index = 2;
        int RSAKeyModule = 2048;
        int RSAKeyExponent = 3;
        String RSAKeyPassword = "11111111";

        Client client = null;

        try {
            client = shortClient.getClient();
            boolean RSAresult = imp.hsmGenerateKey(keyType, index, RSAKeyModule, RSAKeyExponent,
                    RSAKeyPassword, 0, "", 0, 0, client);

            //ECC
            keyType = 0x02;
            int ECCKeyMoudule = 256;
            String ECCKeyPassword = "11111111";
            boolean ECCresult = imp.hsmGenerateKey(keyType, index, RSAKeyModule, RSAKeyExponent,
                    RSAKeyPassword, ECCKeyMoudule, ECCKeyPassword, 0, 0, client);

            //KEK
            keyType = 0x03;
            int KEKKeyLength = 8;
            int KEYVerify = 0x01;

            boolean result = imp.hsmGenerateKey(keyType, index, RSAKeyModule, RSAKeyExponent,
                    RSAKeyPassword, ECCKeyMoudule, ECCKeyPassword, KEKKeyLength, KEYVerify, client);

            if (result) {
                System.out.println("产生成功");
            } else {
                System.out.println("产生失败");
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
            client = shortClient.getClient();

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
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void testInternalSignIBK() {
        byte[] data = "hello world".getBytes();
        Client client = null;
        int algId = 6;
        byte[] userId = "12345678123456781234567812345678".getBytes();
        int keyIndex = 1;

        try {
            client = shortClient.getClient();

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
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
            client = shortClient.getClient();
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
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void testCreateFile() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        try {
            client = shortClient.getClient();

            byte[] fileData = Files.readAllBytes(Paths.get("D:\\plain.txt"));

            boolean result = imp.hsmCreateFile(fileName, fileData.length, client);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void testWriteFile() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        try {
            client = shortClient.getClient();

            byte[] fileData = Files.readAllBytes(Paths.get("D:\\plain.txt"));

            boolean result = imp.hsmWriteFile(fileName, fileData, client);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void testReadFile() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        try {
            client = shortClient.getClient();

            byte[] initData = Files.readAllBytes(Paths.get("D:\\plain.txt"));

            byte[] data = imp.hsmReadFile(fileName, 0, initData.length, client);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void testDeleteFile() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        try {
            client = shortClient.getClient();

            boolean result = imp.hsmDeleteFile(fileName, client);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
            client = shortClient.getClient();

            byte[] blobData = imp.hsmExportSM2WithGMT1618Internal(hsmPwd, keyIndex, protectedIndex, protectedBlobFormat, hsmPwd, client);

            System.out.println("blobData value is " + ByteArrayUtil.toHexString(blobData));

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void testExportSM2KeyWithGMT1618_External() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        int keyIndex = 1;
        int protectedBlobFormat = ProtectedBlobFormatEnum.GMT0018_SM2_512.getValue();
        String hsmPwd = "11111111";

        try {
            client = shortClient.getClient();

            PublicKey publicKey = imp.hsmSM2GetPublicKey(keyIndex, client);

            byte[] protectedPk = DataConverUtil.SM2PublicKey2Bytes(publicKey);

            byte[] blobData = imp.hsmExportSM2WithGMT1618External(hsmPwd, keyIndex, protectedBlobFormat, protectedPk, client);

            System.out.println("blobData value is " + ByteArrayUtil.toHexString(blobData));

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
            client = shortClient.getClient();

            byte[] blobData = imp.hsmExportSM2WithGMT1618Internal(hsmPwd, keyIndex, protectedIndex, protectedBlobFormat, hsmPwd, client);

            boolean result = imp.hsmImportSM2WithGMT1618(hsmPwd, keyIndex, protectedIndex, protectedBlobFormat, hsmPwd, blobData, client);

            System.out.println("import result is " + result);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void testExportSM2KeyWithGMT0009_Internal() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        int keyIndex = 1;
        int protectedIndex = 1;
        String hsmPwd = "11111111";

        try {
            client = shortClient.getClient();

            byte[] blobData = imp.hsmExportSM2WithGMT0009Internal(hsmPwd, keyIndex, protectedIndex, hsmPwd, client);

            System.out.println("blobData value is " + ByteArrayUtil.toHexString(blobData));

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void testExportSM2KeyWithGMT0009_External() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        int keyIndex = 1;
        String hsmPwd = "11111111";

        try {
            client = shortClient.getClient();

            PublicKey publicKey = imp.hsmSM2GetPublicKey(keyIndex, client);

            byte[] protectedPk = DataConverUtil.SM2PublicKey2Bytes(publicKey);

            byte[] blobData = imp.hsmExportSM2WithGMT0009External(hsmPwd, keyIndex, protectedPk, client);

            System.out.println("blobData value is " + ByteArrayUtil.toHexString(blobData));

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void testImportSM2KeyWithGMT0009() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        int keyIndex = 1;
        int protectedIndex = 1;
        String hsmPwd = "11111111";

        try {
            client = shortClient.getClient();

            byte[] blobData = imp.hsmExportSM2WithGMT0009Internal(hsmPwd, keyIndex, protectedIndex, hsmPwd, client);

            boolean result = imp.hsmImportSM2WithGMT0009(hsmPwd, keyIndex, protectedIndex, hsmPwd, blobData, client);

            System.out.println("import result is " + result);

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (null != client) {
                try {
                    client.disposeConnector();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void main(String[] arg0) {

        try {

            testhsmHash();

            //产生密钥并保存在加密机中某索引位置
            testHsmGenerateKey();

            //外部RSA加解密
            testHsmRSAExternalPublicKeyEnc();

            //内部RSA加解密
            testHsmRSAInternalPublicKeyEnc();

            //内部SM2签名验签
            testHsmSM2InternalSign();

            //内部RSA签名验签
            testHsmRSAInternalSign();

            //获取RSA2公钥
            testHsmRSAGetPublicKey();

            //获取SM2公钥
            testHsmSM2GetPublicKey();

            //外部SM2签名
            testHsmSM2ExternalSign();

            //外部SM2加解密
            testHsmSM2ExternalPublicKeyEnc();

            //内部SM2加解密
            testHsmSM2InternalPrivateKeyDec();

            //内部ECC标识签名验签
            testInternalSignIBK();

            //外部ECC标识签名验签
            testHsmSM2ExternalSignIBK();

            //标识密钥生成
            testGenerateKeyPairIBK();

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

            //基于ECC算法的数字信封转换
            testHsmSM2ExchangeDigitEnvelope();

        } catch (Exception e) {
            System.out.println("Exception :" + e);
        }


    }
}
