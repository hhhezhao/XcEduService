package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRedis {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedis(){
        // 定义key
        String key = "user_token:66880ddd-4e61-43e2-8d35-b3a77ca8c3ec";
        // 定义value
        Map<String, String> value = new HashMap<>();
        value.put("jwt","eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6IlhjV2ViQXBwIiwic2NvcGUiOlsiYXBwIl0sIm5hbWUiOm51bGwsInV0eXBlIjpudWxsLCJpZCI6bnVsbCwiZXhwIjoxNTY0MzIxNzYwLCJqdGkiOiI2Njg4MGRkZC00ZTYxLTQzZTItOGQzNS1iM2E3N2NhOGMzZWMiLCJjbGllbnRfaWQiOiJYY1dlYkFwcCJ9.EBbQdfmbEkNgpUmzT639cGS1LcEXBe77TW4Cm1Iia2jznK-Gi1zeNP3SuA96EK91KRYSq5Bnn3TTICp9l_P0d67X9GV86ktSa5KpGrBXl2LQKYwTj-6_r-e_Qhv93MSod0OP6hyjVdBk44e99_sYFT9kQJVozDdTK-pLn52VEH9Nx2mKiEk1ufOb3AzcS9zvW_8n6Vlzv1JvUzUVxK5Pre3m35sUemBcGL-CchbS6rQxwDUQ0oiPPjkoJP5lwUDk5qZ1uddgxwvIGIXYPIQ4D3aeu1NBSVN3qbg1ykgN1601I_coNfIIfl-C76oymYrIS9BMpZClLnTR73Yhht3_tA");
        value.put("refresh_token","eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6IlhjV2ViQXBwIiwic2NvcGUiOlsiYXBwIl0sImF0aSI6IjY2ODgwZGRkLTRlNjEtNDNlMi04ZDM1LWIzYTc3Y2E4YzNlYyIsIm5hbWUiOm51bGwsInV0eXBlIjpudWxsLCJpZCI6bnVsbCwiZXhwIjoxNTY0MzIxNzYwLCJqdGkiOiJjMmNjMDFmMy0xZDUyLTRlZTMtOWNmYS1kZGVjZmQ5NWQxNzAiLCJjbGllbnRfaWQiOiJYY1dlYkFwcCJ9.VJDrwkE0aSjvFQOroJ8Jgs8gjNgBO7wsbYrpAnIoBIlhVhr8GZ-3YOvzJ7BdSQH-Grg9leEWmSrxDiuQ5LqzqjsnryefZBiKMfdagLkP9SGxdbnf8fVXURCcFEip8Yw-FD-gQpAlJisbx1AKI5QZuACZ3qIxVyQOUdCSne855VRERH92cWfo0LaJbIib5Gplg6MHac2rk_GKQWY98ZLGqaI0jj3D7RvC2CzX7SlnNDGFRsgDUmoeSUyNBQwiD-1FgW2g68vaac_Ya1bASQK3yrDKgsUVzy7eIL7kCPv0NYW_-eNnRQIjIepx6ufRJmBOiGDPOhH1Co1UaMkCm4LOow");
        String jsonString = JSON.toJSONString(value);
        // 存储数据
        stringRedisTemplate.boundValueOps(key).set(jsonString, 30, TimeUnit.SECONDS);
        // 获取数据
        String s = stringRedisTemplate.opsForValue().get(key);
        System.out.println(s);

    }
}
