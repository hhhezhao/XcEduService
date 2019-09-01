package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
//@ContextConfiguration(value={"xc.keystore"})
public class TestJwt {

    @Test
    public void testCreateJwt(){
        // 证书文件
        String key_location = "xc.keystore";
        // 密钥库密码
        String keystore_password = "xuechengkeystore";
        // 证书访问路径
        ClassPathResource resource = new ClassPathResource(key_location);
        // 密钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, keystore_password.toCharArray());
        // 密钥的密码
        String keypassword = "xuecheng";
        // 密钥别名
        String alias = "xckey";
        // 密钥对（密钥和公钥）
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, keypassword.toCharArray());
        // 私钥
        RSAPrivateKey aPrivate = (RSAPrivateKey) keyPair.getPrivate();
        // 定义payload信息
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("name","xuecheng");
        // 生成jwt令牌
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(tokenMap), new RsaSigner(aPrivate));
        // 取出jwt令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);

    }

    @Test
    public void testVerify(){
        //jwt令牌
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOiIxIiwidXNlcnBpYyI6bnVsbCwidXNlcl9uYW1lIjoid29uaXUiLCJzY29wZSI6WyJhcHAiXSwibmFtZSI6Iuicl-eJm-WFiOeUnyIsInV0eXBlIjoiMTAxMDAyIiwiaWQiOiI0OSIsImV4cCI6MTU2NDUwMTU1NCwiYXV0aG9yaXRpZXMiOlsieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9iYXNlIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9kZWwiLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX2xpc3QiLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX3BsYW4iLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlIiwiY291cnNlX2ZpbmRfbGlzdCIsInhjX3RlYWNobWFuYWdlciIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfbWFya2V0IiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9wdWJsaXNoIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9hZGQiXSwianRpIjoiMzcyZGU1M2QtZDFmZC00ZTI0LTljZTAtZGEzMWVmZTlhYWU3IiwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ.Uq9FbzSmq2UMnmpJJk65NRUc0gaqqcY_m8XigUqfGu9_Ds6CW2TB8dlypO-exAQrPYn2ARRpgmZHcJ8ilZyrDSdzWvg7th9z98MTn2Oo-7rE-_pG2hRo8vt4e7WYGTCeC-wf_Ch9ItTGCoPnraxxqLvO1nh5q1nsqBKukK3m5FYdb0W2-oHeq-pm5sztQqrzYpPmYahgmNaAZGbNBl_taBcG1EMneBfnQqzs8COpLS2qEaoD0YBQn9Zwhi_cdrlZDjm-wJWL_Luqkn8ti2kt9lV96loBQtebpQLUYhEF96CT9fx9FeAa3Hp26O-TadPvWZ9OEEpFW1tKjjBmRqVZxw";  //公钥
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnASXh9oSvLRLxk901HANYM6KcYMzX8vFPnH/To2R+SrUVw1O9rEX6m1+rIaMzrEKPm12qPjVq3HMXDbRdUaJEXsB7NgGrAhepYAdJnYMizdltLdGsbfyjITUCOvzZ/QgM1M4INPMD+Ce859xse06jnOkCUzinZmasxrmgNV3Db1GtpyHIiGVUY0lSO1Frr9m5dpemylaT0BV3UwTQWVW9ljm6yR3dBncOdDENumT5tGbaDVyClV0FEB1XdSKd7VjiDCDbUAUbDTG1fm3K9sx7kO1uMGElbXLgMfboJ963HEJcU01km7BmFntqI5liyKheX+HBUCD4zbYNPw236U+7QIDAQAB-----END PUBLIC KEY-----";
        //校验jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));
        //获取jwt原始内容
        String claims = jwt.getClaims();
        //jwt令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }



}
