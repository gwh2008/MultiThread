//package rsa;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.Map;
//
//import static org.junit.Assert.assertEquals;
//
//public class RSACoderTest {
//
//    private String publicKey;
//    private String privateKey;
//
//    @Before
//    public void setUp() throws Exception {
//        Map<String, Object> keyMap = RSACoder.initKey();
//
//        publicKey = RSACoder.getPublicKey(keyMap);
//        privateKey = RSACoder.getPrivateKey(keyMap);
//        System.err.println("公钥: \n\r" + publicKey);
//        System.err.println("私钥： \n\r" + privateKey);
//    }
//
//    @Test
//    public void test() throws Exception {
//        System.err.println("公钥加密——私钥解密");
//        String inputStr = "abc";
//        byte[] data = inputStr.getBytes();
//
//        byte[] encodedData = RSACoder.encryptByPublicKey(data, publicKey);
//
//        byte[] decodedData = RSACoder.decryptByPrivateKey(encodedData,
//                privateKey);
//
//        String outputStr = new String(decodedData);
//        System.err.println("加密前: " + inputStr + "\n\r" + "解密后: " + outputStr);
//        assertEquals(inputStr, outputStr);
//    }
//}