package com.gaowh;

import cn.com.westone.common.array.ByteArrayUtil;
import com.westone.pboc.hsm.HSMConstant;
import com.westone.pboc.hsm.entity.Configuration;
import com.westone.pboc.hsm.entity.KeyPairs;
import com.westone.pboc.mina.client.Client;
import com.westone.pboc.mina.client.ClientThreadPool;
import com.westone.pboc.service.imp.HSMWSTApiServiceImp;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;


/**
 * 测试配置项（用户设置配置项）
 */
public class TestInitConfig {

    private static ClientThreadPool connectPool = ClientThreadPool.getInstance();

    public static void main(String[] args) {
        Client client;
        try {
            HSMWSTApiServiceImp imp = new HSMWSTApiServiceImp();
            Configuration configuration = Configuration.getInstance();
            List<String> ipList = new ArrayList<String>();
            ipList.add("192.168.6.211");
            configuration.initConfig(ipList, 6667, 30, 30, 30, "1");
            connectPool.initClient();
            client = imp.hsmOpenSession();
            try {
                byte[] random = imp.hsmGenerateRandom(128, client);

                System.out.println("random is " + ByteArrayUtil.toHexString(random));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connectPool.release(client);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
