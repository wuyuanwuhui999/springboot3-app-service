package com.player.gateway.user;

import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
// 生成密钥代码（运行一次即可）
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Encoders;

import java.security.Key;

@SpringBootTest
class UserApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void test2() {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String base64Key = Encoders.BASE64.encode(key.getEncoded());
        System.out.println("安全密钥: " + base64Key);
    }

}
