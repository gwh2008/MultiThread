package com.gaowh;

import cn.com.westone.common.array.ByteArrayUtil;
import com.westone.pboc.hsm.HSMConstant;
import com.westone.pboc.hsm.entity.KeyPairs;
import com.westone.pboc.hsm.entity.SM2RefPublicKey;
import com.westone.pboc.mina.client.Client;
import com.westone.pboc.mina.client.ClientThreadPool;
import com.westone.pboc.service.imp.HSMWSTApiServiceImp;
import com.westone.pboc.util.SM2PKUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

/**
 * 通用的应用场景展示程序
 */
public class CommonDemoTest {
    private static HSMWSTApiServiceImp imp = null;

    private static ClientThreadPool connectPool = ClientThreadPool.getInstance();


    @BeforeClass
    public static void init() {
        imp = new HSMWSTApiServiceImp();
        try {
            connectPool.initClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void HowToEncryptOrDecryptBySM4ECB_WithKeyProtectedBySM2() {

        int keyIndex = 1;
        int uiKeyBits = 16;
        int encryptAlgID = HSMConstant.SGD_SM4_ECB;
        Client client = null;
        byte[] data = new byte[16];
        byte[] cipher;
        byte[] plain;

        try {
            client = connectPool.getClient();

            /*前提条件：1号索引存在SM2密钥*/
            /*【系统的初始化阶段】
            * 需要准备用于数据加解密的密钥，这个或者这一些密钥的密文需要在业务系统测进行持久化存储
            * ！！！！产生密钥的动作不需要在每次加密或者解密时调用，加密和解密所用密钥必须是同一个！！！！*/

            /*1、在某个会话通道client中产生一个会话密钥，记为keyA，这个密钥的密文由1号索引的SM2密钥加密保护*/
            List<Object> list = imp.hsmSM2GenerateKeyWithIPK(keyIndex, uiKeyBits, client);

            /*2、获得这个会话密钥的句柄：上述产生会话密钥的接口会在产生的同时
             * 就将该密钥放置在会话空间中形成一个会话密钥句柄，可直接调用该句柄进行数据加解密，
             * 但我们推荐将该句柄直接销毁，后续做加密解密时再重新导入密钥密文拿到新的密钥句柄来使用*/
            byte[] keyHandle = (byte[]) list.get(0);
            /*3、销毁keyHandleA：当会话密钥不再使用时，应及时销毁该句柄，以释放密钥句柄资源，否则造成句柄资源泄漏*/
            imp.hsmDestoryKey(keyHandle, client);

            /*4、获得这个会话密钥的密文：这个密文需要在业务系统中长期存储，
             * 将来需要将该密钥恢复回来进行数据加解密时，需要用该密文进行相应的导入接口的调用，
             * 以形成一个新的会话密钥句柄*/
            byte[] keyCipher = (byte[]) list.get(1);


            /*【加解密处理阶段】
            /*以上，我们准备产生了一个会话密钥密文keyCipher，这个密钥密文是由1号索引的SM2加密的

             *下面的代码展示使用这个会话密钥对数据进行处理时的接口调用
             */

            /*1、将会话密钥密文keyCipher导入到密码机的某个会话通道client中，得到对应的会话密钥句柄keyHandle
             *   这里将使用私钥对会话密钥进行解密，从而获得密钥句柄。
             *   使用私钥进行运算时需要先获取私钥使用权限，本例在通过密码机控制台软件进行SM2密钥产生时设置的访问口令为“11111111”
             */
            imp.hsmGetPrivateKeyAccessRight(keyIndex, "11111111", client);
            keyHandle = imp.hsmSM2ImportKeyWithISK(keyIndex, keyCipher, client);
            imp.hsmReleasePrivateKeyAccessRight(keyIndex, client);

            /*2、使用keyhandle加密明文数据data*/
            cipher = imp.hsmEncrypt(keyHandle, encryptAlgID, null, data, client);
            plain = imp.hsmDecrypt(keyHandle, encryptAlgID, null, cipher, client);

            /*3、销毁keyHandleA：当会话密钥不再使用时，应及时销毁该句柄，以释放密钥句柄资源*/
            imp.hsmDestoryKey(keyHandle, client);

            System.out.println("data length =" + data.length + "\ncipher length =" + cipher.length + "\ndecrypted plain length = " + plain.length);

            Assert.assertArrayEquals(data, plain);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    @Test
    public void HowToCalculateSM3HMAC() {
        Client client = null;
        int sm2KeyIndex = 1;
        /*数据如果有中文，应约定将数据转码成byte数组时的转码方式*/
        byte[] data = "HelloWorld".getBytes();

        try {
            client = connectPool.getClient();
            /*这里是一个初始化动作，目的是产生一个用于计算HMAC的随机密钥，计算HMAC和校验这个HMAC的时候必须确保用到这个相同的随机密钥*/
            List<Object> wk = imp.hsmSM2GenerateKeyWithIPK(sm2KeyIndex, 16, client);
            /*这里取出的密钥句柄，如果不需要马上用于计算HMAC，可以直接调用hsmDestroyKey将其销毁，回收句柄资源*/
            byte[] keyHandle = (byte[]) wk.get(0);
            imp.hsmDestoryKey(keyHandle, client);
            /*这里的随机密钥密文是需要保存的，以便下次再使用这个密钥时用于导入后重新拿到密钥句柄*/
            byte[] keyCipher = (byte[]) wk.get(1);

            /*从这里开始，下面的接口调用序列为计算HMAC的完整的日常过程，
            其中的keyCipher就是初始化阶段产生的随机密钥密文，将其导入后重新得到一个密钥句柄，并用这个句柄进行HMAC运算，
            运算结束后必须回收该句柄资源*/
            imp.hsmGetPrivateKeyAccessRight(sm2KeyIndex, "11111111", client);
            byte[] keyHandleNew = imp.hsmSM2ImportKeyWithISK(sm2KeyIndex, keyCipher, client);
            imp.hsmReleasePrivateKeyAccessRight(sm2KeyIndex, client);

            byte[] context = imp.hsmHashCrossSessionInit(HSMConstant.SGD_HASH_HMAC_SM3, keyHandleNew, null, null, null, null, client);
            byte[] context11 = imp.hsmHashCrossSessionUpdate(context, data, client);
            byte[] hashResult = imp.hsmHashCrossSessionFinal(context11, client);


            /*！！！！注意！！！！*/
            /*计算完成后一定要释放密钥句柄资源*/
            boolean ok = imp.hsmDestoryKey(keyHandleNew, client);

            System.out.println("HMAC = " + ByteArrayUtil.toHexString(hashResult));
        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        } finally {
            connectPool.release(client);
        }
    }

    @Test
    public void HowToBuildSM2PublicKeyFromRefByteArray() {
        int keyIndex = 1;
        Client client = null;

        try {
            client = connectPool.getClient();

            /*一个RefByteArray格式的SM2公钥以16进制字符串形式打印出来看起来像这样：
             *00010000e3b74ed47bbe6dabae26fba66630577a34d66f7e885cc23938db06e321d34f9344c35de1f59ef7164425484faac603df10caa369cfe72e24edfd6b2904e655a5
             * 即00010000||X||Y
             *通常解析X509证书得到的SM2公钥按照16进制字符串形式打印出来像下面这样：
             *04e3b74ed47bbe6dabae26fba66630577a34d66f7e885cc23938db06e321d34f9344c35de1f59ef7164425484faac603df10caa369cfe72e24edfd6b2904e655a5
             * 即04||X||Y
             *本例仅展示从RefByteArray构造一个PublicKey对象，如果是04||X||Y，只需要将前面的04部分替换为00010000 */

            /*前提条件：1号索引存在SM2密钥，且其访问口令为11111111*/
            /*为了方便验证转换的正确性，我们取出了1号索引的公钥，将其转换为00010000||X||Y形式
             *再对00010000||X||Y形式的公钥进行了PublicKey的对象构造动作*/
            PublicKey externalPK = imp.hsmSM2GetPublicKey(keyIndex, client);
            byte[] pkAsRefByteArray = SM2PKUtil.SM2PublicKey2Bytes(externalPK);
            System.out.println("pkAsRefByteArray = " + ByteArrayUtil.toHexString(pkAsRefByteArray));

            PublicKey builtPK = SM2PKUtil.buildPublicKey(pkAsRefByteArray);

            /*验证正确性：
             *由于以上构造出的公钥实际来源于1号索引，因此我们用构造出来的公钥对象进行加密，再用1号索引的SM2私钥解密，
             *如果解密结果与原始明文一致，则认为验证通过 */
            byte[] dataPlain = "1111111111111111".getBytes();
            byte[] dataCipher = imp.hsmSM2ExternalPublicKeyEnc(HSMConstant.WST_ALG_TYPE, builtPK, dataPlain, client);
            imp.hsmGetPrivateKeyAccessRight(keyIndex, "11111111", client);
            byte[] outPlain = imp.hsmSM2InternalPrivateKeyDec(keyIndex, HSMConstant.WST_ALG_TYPE, dataCipher, client);
            imp.hsmReleasePrivateKeyAccessRight(keyIndex, client);
            System.out.println("dataPlain=" + ByteArrayUtil.toHexString(dataPlain) + "\noutPlain =" + ByteArrayUtil.toHexString(outPlain));
            Assert.assertArrayEquals(dataPlain, outPlain);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    @Test
    public void HowToInternalEncryptAndInternalDecryptBySM2() {
        int keyIndex = 1;
        Client client = null;
        try {
            client = connectPool.getClient();

            byte[] data = "11111111".getBytes();
            byte[] result = imp.hsmSM2InternalPublicKeyEnc(HSMConstant.WST_ALG_TYPE, 6, keyIndex, data, client);
            System.out.println("Cipher = " + ByteArrayUtil.toHexString(result));

            byte[] decrypted = imp.hsmSM2InternalPrivateKeyDec(1, keyIndex, "11111111", result, client);
            System.out.println("Plain = " + ByteArrayUtil.toHexString(decrypted));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }

    @Test
    public void HowToInternalSignAndExternalVerifyBySM2() {
        int keyIndex = 1;
        String algType = HSMConstant.WST_ALG_TYPE;
        String src = "1111111111ffffffffff000000000012";
        String userId = "1234567812345678";
        Client client = null;
        try {
            client = connectPool.getClient();

            /*签名方：使用keyIndex索引的私钥对数据进行签名*/
            /*1、用keyIndex索引对应的公钥对原始数据计算SM2-SM3-USERID摘要*/
            byte[] hashData1 = imp.doHash(keyIndex, null, src.getBytes(), userId, client);
            /*2、对摘要结果计算签名*/
            byte[] signData = imp.hsmSM2InternalSign(algType, keyIndex, hashData1, "11111111", client);
            System.out.println("外部SM2签名成功,签名内容：" + ByteArrayUtil.toHexString(signData));

            /*验签方：验签方用签名方的公钥进行验签*/
            /*0、通过某种方式，验签方获得签名方的公钥，例如从签名方的公钥证书中提取，或者从签名方直接拿到公钥明文
            *    这里为了让用例流程往下开展，我们假设性的从相同索引获取其公钥*/
            PublicKey pk = imp.hsmSM2GetPublicKey(keyIndex, client);
            System.out.println("PK = "+ByteArrayUtil.toHexString(pk.getEncoded()));

            /*1、用签名方提供的公钥对原始数据计算SM2-SM3-USERID摘要*/
            byte[] hashData2 = imp.doHash(-1, pk, src.getBytes(), userId, client);
            /*2、对摘要结果计算签名*/
            boolean ret = imp.hsmSM2ExternalVerify(algType, pk, signData, hashData2, client);
            System.out.println("外部SM2验签密成功,结果：" + ret);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectPool.release(client);
        }
    }
}
