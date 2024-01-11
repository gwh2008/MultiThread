package com.gaowh;

import com.westone.pboc.hsm.constants.SymAlgIDEnum;
import com.westone.pboc.mina.client.Client;
import com.westone.pboc.mina.client.ClientThreadPool;
import com.westone.pboc.service.HSMWSTApiService;
import com.westone.pboc.service.SerHsmApiService;
import com.westone.pboc.service.imp.HSMWSTApiServiceImp;
import com.westone.pboc.service.imp.SerHsmApiServiceImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class BaseTest {
    public static SerHsmApiService hsmApiService = new SerHsmApiServiceImpl();
    public static HSMWSTApiService hsmwstApiService = new HSMWSTApiServiceImp();
    public static ClientThreadPool connectPool = ClientThreadPool.getInstance();
    public static final int keyIndex = 1;
    public static final String password = "11111111";
    public static Client client;
    public static byte[] plainKey = "1111111111111111".getBytes();
    public static byte[] plainText = "1111111111111111".getBytes();
    public static byte[] iv_32 = "1234567812345678".getBytes();
    public static byte[] iv_16 = "12345678".getBytes();
    public static byte[] sm2EncData = "1111111111ffffffffff000000000012".getBytes();
    public static final byte[] userId = "1234567812345678".getBytes();
    public static int kgcSignKeyIndex = 1;
    public static int kgcEncKeyIndex = 1;
    public static int algId = SymAlgIDEnum.SGD_SM4_ECB.getValue();
    public static byte[] sm9UserId = "1234567890abcdef".getBytes();
    public static int sm9UserIndex = 0;

    @BeforeClass
    public static void init() {
        try {
            connectPool.initClient();
            client = connectPool.getClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void end() {
        connectPool.release(client);
    }
}
