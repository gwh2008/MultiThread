package com.gaowh;

import cn.com.westone.common.array.ByteArrayUtil;
import com.westone.pboc.hsm.entity.KeyPairs;
import com.westone.pboc.hsm.entity.XYPointResEntity;
import com.westone.pboc.hsm.entity.XYZPointEntity;
import com.westone.pboc.mina.client.Client;
import com.westone.pboc.mina.client.ClientThreadPool;
//import com.westone.pboc.serHsmService.BaseTest;
import com.westone.pboc.service.imp.HSMWSTApiServiceImp;
import com.westone.pboc.util.FileUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class DrawTest extends BaseTest {
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
    public void testHsmXYChange() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        double x = 118.1111;
        double y = 37.5555;

        try {
            client = pool.getClient();

            XYPointResEntity entity = imp.hsmXYChange2(x, y, client);

            System.out.println("x is " + entity.getX());
            System.out.println("y is " + entity.getY());

        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        } finally {
            pool.release(client);
        }
    }


    @Test
    public void testHsmXYInverse() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        double x = 118.1111;
        double y = 37.5555;

        try {
            client = pool.getClient();

            XYPointResEntity entity = imp.hsmXYInverse2(x, y, client);

            System.out.println("x is " + entity.getX());
            System.out.println("y is " + entity.getY());

        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        } finally {
            pool.release(client);
        }
    }


    @Test
    public void testHsmGetLocationBoundary() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        try {
            client = pool.getClient();

            XYPointResEntity entity = imp.hsmGetLocationBoundary(client);

            System.out.println("minX is " + entity.getMinX());
            System.out.println("minY is " + entity.getMaxY());
            System.out.println("maxX is " + entity.getMaxX());
            System.out.println("maxY is " + entity.getMaxY());

        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        } finally {
            pool.release(client);
        }
    }


    @Test
    public void testHsmEncryptGeometryByWKBList() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        try {
            client = pool.getClient();
            for (int i = 0; i <= 1000; i++) {
                List<byte[]> dataList = new ArrayList<byte[]>();
                byte[] data = FileUtil.readFile("D:\\wkb2.data");
                for (int j = 0; j <= i; j++) {
                    dataList.add(data);
                }

                List<byte[]> cipher = imp.hsmEncryptGeometryByWKB(dataList, client);
                Assert.assertNotNull(cipher);
            }

            testHsmGetLocationBoundary();
            testHsmXYInverse();
            testHsmXYChange();
            testHsmSM2GenerateKeyPair();
        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        } finally {
            pool.release(client);
        }
    }

    @Test
    public void testHsmSM2GenerateKeyPair() {
        Client client = null;
        try {
            client = connectPool.getClient();
            KeyPairs keypairs = imp.hsmSM2GenerateKeyPair(256, 6, client);
            PrivateKey priKeySm2 = keypairs.getPrikey();
            PublicKey pubKeySm2 = keypairs.getPubkey();
            System.out.println("产生SM2密钥对成功,新生成私钥：" + ByteArrayUtil.toHexString(priKeySm2.getEncoded()) + ",公钥:" + ByteArrayUtil.toHexString(pubKeySm2.getEncoded()));
        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        } finally {
            connectPool.release(client);
        }
    }


    @Test
    public void testHsmEncryptGeometryByWKB() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        try {
            client = pool.getClient();

            byte[] data = FileUtil.readFile("D:\\wkb2.data");

            System.out.println("data is " + ByteArrayUtil.toHexString(data));

            byte[] cipher = imp.hsmEncryptGeometryByWKB(data, client);

            System.out.println("cipher is " + ByteArrayUtil.toHexString(cipher));

            byte[] plain = imp.hsmDecryptGeometryByWKB(cipher, client);

            System.out.println("plain is " + ByteArrayUtil.toHexString(plain));

            Assert.assertArrayEquals(data, plain);

        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        } finally {
            pool.release(client);
        }
    }

    @Test
    public void testHsmXYChange3() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        double x = 118.1111;
        double y = 37.5555;

        try {
            client = pool.getClient();

            XYPointResEntity entity = imp.hsmXYChange3(x, y, client);

            System.out.println("x is " + entity.getX());
            System.out.println("y is " + entity.getY());

        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        } finally {
            pool.release(client);
        }
    }


    @Test
    public void testHsmXYInverse3() {
        ClientThreadPool pool = ClientThreadPool.getInstance();
        Client client = null;

        double x = 118.1111;
        double y = 37.5555;

        try {
            client = pool.getClient();

            XYPointResEntity entity = imp.hsmXYInverse3(x, y, client);

            System.out.println("x is " + entity.getX());
            System.out.println("y is " + entity.getY());

        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        } finally {
            pool.release(client);
        }
    }


    @Test
    public void testHsmXYZInverseList() {
        List<XYZPointEntity> list = new ArrayList<XYZPointEntity>();
        XYZPointEntity xyzPointEntity = new XYZPointEntity();
        double x = 118.1111;
        double y = 37.5555;
        double z = 37.5555;
        xyzPointEntity.setX(x);
        xyzPointEntity.setY(y);
        xyzPointEntity.setZ(z);
        list.add(xyzPointEntity);
        List<XYZPointEntity> result = imp.hsmXYZInverseList(list, client);
        System.out.println("result is " + result.toString());
    }
}
