package com.gaowh;

import cn.com.westone.common.array.ByteArrayUtil;
import com.westone.pboc.hsm.constants.ECCCurveIdEnum;
import com.westone.pboc.hsm.constants.SymAlgIDEnum;
import com.westone.pboc.mina.client.Client;
import com.westone.pboc.mina.client.ClientThreadPool;
import com.westone.pboc.service.imp.HSMWSTApiServiceImp;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class ECDHTest {
    private static HSMWSTApiServiceImp imp = null;

    private static ClientThreadPool connectPool = ClientThreadPool.getInstance();

    private static String password = "11111111";

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
    public void testECDHEncDec() {
        Client client = null;

        try {
            client = connectPool.getClient();
            int keyIndex = 1;
            SymAlgIDEnum symAlgIDEnum = SymAlgIDEnum.SGD_SM4_ECB;
            byte[] pk = exportEccPk(keyIndex, symAlgIDEnum, client);

            List<byte[]> cipher = imp.hsmAgreementECDHEnc(ECCCurveIdEnum.NID_SECP256K1.getValue(), pk, client);
            byte[] tempPk = cipher.get(0);
            byte[] key = cipher.get(1);

            System.out.println("tempPk is " + ByteArrayUtil.toHexString(tempPk));
            System.out.println("key is " + ByteArrayUtil.toHexString(key));

            Assert.assertNotNull(tempPk);
            Assert.assertNotNull(key);

            byte[] plain = imp.hsmAgreementECDHDec(keyIndex, tempPk, client);
            Assert.assertNotNull(plain);

            System.out.println("plain is " + ByteArrayUtil.toHexString(plain));

        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        } finally {
            connectPool.release(client);
        }
    }

    @Test
    public void testExportEccPk() {
        Client client = null;

        try {
            client = connectPool.getClient();

            exportEccPk(1, SymAlgIDEnum.SGD_SM4_ECB, client);

        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        } finally {
            connectPool.release(client);
        }
    }

    @Test
    public void testExportEccKeyPair() {
        Client client = null;

        try {
            client = connectPool.getClient();

            exportEccKeyPair(1, SymAlgIDEnum.SGD_SM4_ECB, client);

        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        } finally {
            connectPool.release(client);
        }
    }


    private static byte[] exportEccPk(int keyIndex, SymAlgIDEnum symAlgIDEnum, Client client) {
        byte[] pk = null;

        try {
            byte[] keyHandle = ByteArrayUtil.BigEndian.toByteArray(1, 2);

            pk = imp.hsmExportEccPublicKey(keyHandle, symAlgIDEnum.getValue(), keyIndex, password, client);

            Assert.assertNotNull(pk);

            System.out.println("pk is " + ByteArrayUtil.toHexString(pk));

        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        }

        return pk;
    }


    private static List<byte[]> exportEccKeyPair(int keyIndex, SymAlgIDEnum symAlgIDEnum, Client client) {
        List<byte[]> keyPair = null;

        try {
            byte[] keyHandle = ByteArrayUtil.BigEndian.toByteArray(1, 2);

            keyPair = imp.hsmExportEccKeyPair(keyHandle, symAlgIDEnum.getValue(), keyIndex, password, client);

            Assert.assertNotNull(keyPair);

            System.out.println("pk is " + ByteArrayUtil.toHexString(keyPair.get(0)));
            System.out.println("sk is " + ByteArrayUtil.toHexString(keyPair.get(1)));

        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        }

        return keyPair;
    }
}
