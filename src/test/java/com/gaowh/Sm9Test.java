package com.gaowh;

import cn.com.westone.common.array.ByteArrayUtil;
import com.westone.pboc.hsm.constants.KeyTypeEnum;
//import com.westone.pboc.serHsmService.BaseTest;
import org.junit.Assert;
import org.junit.Test;

public class Sm9Test extends BaseTest {
    @Test
    public void testGenerateUseSignKey() {
        hsmwstApiService.hsmGetPrivateKeyAccessRight(kgcSignKeyIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_KGC.getValue(), "11111111", client);
        byte[] key = hsmwstApiService.hsmGenerateUseSignKey(kgcSignKeyIndex, userId, client);
        Assert.assertNotNull(key);
        System.out.println("key = [" + ByteArrayUtil.toHexString(key));
        hsmwstApiService.hsmReleasePrivateKeyAccessRight(kgcSignKeyIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_KGC.getValue(), client);
    }

    @Test
    public void testGenerateUseEncKey() {
        hsmwstApiService.hsmGetPrivateKeyAccessRight(kgcEncKeyIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_KGC.getValue(), "11111111", client);
        byte[] key = hsmwstApiService.hsmGenerateUseEncKey(kgcEncKeyIndex, userId, client);
        Assert.assertNotNull(key);
        System.out.println("key = [" + ByteArrayUtil.toHexString(key));
        hsmwstApiService.hsmReleasePrivateKeyAccessRight(kgcEncKeyIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_KGC.getValue(), client);
    }

    @Test
    public void testExportSM9SignMastPublicKey() {
        byte[] key = hsmwstApiService.hsmExportSM9SignMastPublicKey(kgcSignKeyIndex, client);
        Assert.assertNotNull(key);
        System.out.println("key = [" + ByteArrayUtil.toHexString(key));
    }

    @Test
    public void testExportSM9EncMastPublicKey() {
        byte[] key = hsmwstApiService.hsmExportSM9EncMastPublicKey(kgcEncKeyIndex, client);
        Assert.assertNotNull(key);
        System.out.println("key = [" + ByteArrayUtil.toHexString(key));
    }

    @Test
    public void testGenSM9UsrSignKey() {
        hsmwstApiService.hsmGetPrivateKeyAccessRight(kgcSignKeyIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_KGC.getValue(), "11111111", client);
        byte[] keyHandle = ByteArrayUtil.BigEndian.toByteArray(1, 2);
        byte[] key = hsmwstApiService.hsmGenSM9UsrSignKey(algId, keyHandle, kgcSignKeyIndex, userId, client);
        Assert.assertNotNull(key);
        System.out.println("key = [" + ByteArrayUtil.toHexString(key));
        hsmwstApiService.hsmReleasePrivateKeyAccessRight(kgcSignKeyIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_KGC.getValue(), client);
    }

    @Test
    public void testGenSM9UsrEncKey() {
        hsmwstApiService.hsmGetPrivateKeyAccessRight(kgcEncKeyIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_KGC.getValue(), "11111111", client);
        byte[] keyHandle = ByteArrayUtil.BigEndian.toByteArray(1, 2);
        byte[] key = hsmwstApiService.hsmGenSM9UsrEncKey(algId, keyHandle, kgcEncKeyIndex, userId, client);
        Assert.assertNotNull(key);
        System.out.println("key = [" + ByteArrayUtil.toHexString(key));
        hsmwstApiService.hsmReleasePrivateKeyAccessRight(kgcEncKeyIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_KGC.getValue(), client);
    }

    @Test
    public void testGenSessionKeyWithSM9() {
        int encType = 0;
        int keyLen = 16;
        byte[] kgcPublicKey = hsmwstApiService.hsmExportSM9EncMastPublicKey(kgcEncKeyIndex, client);
        byte[] key = hsmwstApiService.hsmGenSessionKeyWithSM9(encType, keyLen, kgcPublicKey, userId, client);
        Assert.assertNotNull(key);
        System.out.println("key = [" + ByteArrayUtil.toHexString(key));
    }


    @Test
    public void testImportSessionKeyWithSM9() {
        int encType = 0;
        int keyLen = 16;
        int kekIndex = 1;
        hsmwstApiService.hsmGetPrivateKeyAccessRight(sm9UserIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_USER.getValue(), "11111111", client);
        byte[] kgcPublicKey = hsmwstApiService.hsmExportSM9EncMastPublicKey(kgcEncKeyIndex, client);
        byte[] keyCipher = hsmwstApiService.hsmGenSessionKeyWithSM9(encType, keyLen, kgcPublicKey, sm9UserId, client);
        Assert.assertNotNull(keyCipher);
        byte[] key = hsmwstApiService.hsmImportSessionKeyWithSM9(encType, kgcEncKeyIndex, sm9UserIndex, kekIndex, algId,
                kgcPublicKey, keyCipher, client);
        Assert.assertNotNull(key);
        System.out.println("key = [" + ByteArrayUtil.toHexString(key));
        hsmwstApiService.hsmReleasePrivateKeyAccessRight(sm9UserIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_USER.getValue(), client);
    }


    @Test
    public void testSM9ExportSessionKeyByHandle() {
        int encType = 0;
        byte[] keyHandle = ByteArrayUtil.BigEndian.toByteArray(1, 2);
        byte[] publicKey = hsmwstApiService.hsmExportSM9EncMastPublicKey(kgcEncKeyIndex, client);
        byte[] sessionKey = hsmwstApiService.hsmSM9ExportSessionKeyByHandle(encType, keyHandle, publicKey, userId, client);
        Assert.assertNotNull(sessionKey);
        System.out.println("sessionKey = [" + ByteArrayUtil.toHexString(sessionKey));
    }


    @Test
    public void testSM9EncDec() {
        hsmwstApiService.hsmGetPrivateKeyAccessRight(sm9UserIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_USER.getValue(), "11111111", client);
        int encType = 0;
        byte[] publicKey = hsmwstApiService.hsmExportSM9EncMastPublicKey(kgcEncKeyIndex, client);
        byte[] cipher = hsmwstApiService.hsmSM9ExternalEncrypt(encType, publicKey, sm9UserId, sm2EncData, client);
        Assert.assertNotNull(cipher);
        System.out.println("cipher = [" + ByteArrayUtil.toHexString(cipher));
        byte[] plain = hsmwstApiService.hsmSM9InternalDecrypt(null, encType, kgcEncKeyIndex, sm9UserIndex, cipher, client);
        Assert.assertNotNull(plain);
        Assert.assertArrayEquals(sm2EncData, plain);
        System.out.println("plain = [" + ByteArrayUtil.toHexString(plain));
        plain = hsmwstApiService.hsmSM9InternalDecrypt(publicKey, encType, kgcEncKeyIndex, sm9UserIndex, cipher, client);
        Assert.assertNotNull(plain);
        Assert.assertArrayEquals(sm2EncData, plain);
        System.out.println("plain = [" + ByteArrayUtil.toHexString(plain));
        hsmwstApiService.hsmReleasePrivateKeyAccessRight(sm9UserIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_USER.getValue(), client);
    }


    @Test
    public void testSM9ExternalSignVerify() {
        hsmwstApiService.hsmGetPrivateKeyAccessRight(sm9UserIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_USER.getValue(), "11111111", client);
        byte[] publicKey = hsmwstApiService.hsmExportSM9SignMastPublicKey(kgcSignKeyIndex, client);
        byte[] signature = hsmwstApiService.hsmSM9InternalSign(kgcSignKeyIndex, sm9UserIndex, sm2EncData, client);
        Assert.assertNotNull(signature);
        System.out.println("signature = [" + ByteArrayUtil.toHexString(signature));
        boolean result = hsmwstApiService.hsmSM9ExternalVerify(publicKey, sm9UserId, sm2EncData, signature, client);
        System.out.println("result = [" + result + "]");
        Assert.assertTrue(result);
        hsmwstApiService.hsmReleasePrivateKeyAccessRight(sm9UserIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_USER.getValue(), client);
    }


    @Test
    public void testSM9InternalSignVerify() {
        hsmwstApiService.hsmGetPrivateKeyAccessRight(sm9UserIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_USER.getValue(), "11111111", client);
        byte[] publicKey = hsmwstApiService.hsmExportSM9EncMastPublicKey(kgcSignKeyIndex, client);
        byte[] signature = hsmwstApiService.hsmSM9InternalSign(kgcSignKeyIndex, sm9UserIndex, sm2EncData, client);
        Assert.assertNotNull(signature);
        System.out.println("signature = [" + ByteArrayUtil.toHexString(signature));
        boolean result = hsmwstApiService.hsmSM9InternalVerify(kgcSignKeyIndex, sm9UserIndex, sm2EncData, signature, client);
        hsmwstApiService.hsmReleasePrivateKeyAccessRight(sm9UserIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_USER.getValue(), client);
        System.out.println("result = [" + result + "]");
        Assert.assertTrue(result);
    }


    @Test
    public void testGenerateAgreementDataWithSM9() {
        hsmwstApiService.hsmGetPrivateKeyAccessRight(sm9UserIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_USER.getValue(), "11111111", client);
        int initiator = 1;
        int keyBits = 128;
        byte[] userId = "abcdef1234567890".getBytes();
        byte[] agreementData = hsmwstApiService.hsmGenerateAgreementDataWithSM9(initiator,
                kgcEncKeyIndex, sm9UserIndex, keyBits, userId, client);
        Assert.assertNotNull(agreementData);
        System.out.println("agreementData = [" + ByteArrayUtil.toHexString(agreementData) + "]");
        hsmwstApiService.hsmReleasePrivateKeyAccessRight(sm9UserIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_USER.getValue(), client);
        initiator = 0;
        int responseUserIndex = 5;
        byte[] responseUserId = "1234567890abcdef".getBytes();
        hsmwstApiService.hsmGetPrivateKeyAccessRight(responseUserIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_USER.getValue(), "11111111", client);
        byte[] key = hsmwstApiService.hsmGenerateAgreementKeyWithSM9(initiator, kgcEncKeyIndex, responseUserIndex,
                keyBits, responseUserId, agreementData, client);
        hsmwstApiService.hsmReleasePrivateKeyAccessRight(responseUserIndex, KeyTypeEnum.HSM_KEY_TYPE_SM9_USER.getValue(), client);
        System.out.println("key = [" + ByteArrayUtil.toHexString(key) + "]");
        Assert.assertNotNull(key);
    }

}
