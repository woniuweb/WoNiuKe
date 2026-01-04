package top.naccl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.naccl.util.HashUtils;

@SpringBootTest
class BlogApiApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void test() {
	}

	@Test
	void generatePassword() {
		String password = "2000826";
		String encoded = HashUtils.getBC(password);
		System.out.println("数据库应存储的值: " + encoded);
		System.out.println("验证结果: " + HashUtils.matchBC(password, encoded));
	}

}
