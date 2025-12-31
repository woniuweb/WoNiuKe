package top.naccl.util;

import cn.hutool.core.lang.hash.MurmurHash;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import org.springframework.util.DigestUtils;

/**
 * @Description: Hash工具类
 * @Author: Naccl
 * @Date: 2020-11-17
 */
public class HashUtils {
	/**
	 * 盐值分隔符
	 */
	private static final String SALT_SEPARATOR = "$";
	/**
	 * 盐值长度
	 */
	private static final int SALT_LENGTH = 16;

	public static String getMd5(CharSequence str) {
		return DigestUtils.md5DigestAsHex(str.toString().getBytes());
	}

	public static long getMurmurHash32(String str) {
		int i = MurmurHash.hash32(str);
		long num = i < 0 ? Integer.MAX_VALUE - (long) i : i;
		return num;
	}

	/**
	 * 使用Hutool带盐SHA256加密密码
	 *
	 * @param rawPassword 原始密码
	 * @return 加密后的密码（格式：盐$哈希值）
	 */
	public static String getBC(CharSequence rawPassword) {
		// 生成随机盐
		String salt = RandomUtil.randomString(SALT_LENGTH);
		// 创建带盐的SHA256摘要器
		Digester digester = new Digester(DigestAlgorithm.SHA256);
		digester.setSalt(salt.getBytes());
		String hash = digester.digestHex(rawPassword.toString());
		// 返回格式：盐$哈希值
		return salt + SALT_SEPARATOR + hash;
	}

	/**
	 * 验证密码是否匹配
	 *
	 * @param rawPassword     原始密码
	 * @param encodedPassword 加密后的密码（格式：盐$哈希值）
	 * @return 是否匹配
	 */
	public static boolean matchBC(CharSequence rawPassword, String encodedPassword) {
		if (rawPassword == null || encodedPassword == null) {
			return false;
		}
		// 分离盐和哈希值
		int separatorIndex = encodedPassword.indexOf(SALT_SEPARATOR);
		if (separatorIndex <= 0) {
			return false;
		}
		String salt = encodedPassword.substring(0, separatorIndex);
		String storedHash = encodedPassword.substring(separatorIndex + 1);
		// 使用相同的盐重新计算哈希
		Digester digester = new Digester(DigestAlgorithm.SHA256);
		digester.setSalt(salt.getBytes());
		String computedHash = digester.digestHex(rawPassword.toString());
		// 比较哈希值
		return storedHash.equals(computedHash);
	}

	public static void main(String[] args) {
		String encoded = getBC("2000826");
		System.out.println("加密结果: " + encoded);
		System.out.println("验证结果: " + matchBC("2000826", encoded));
	}
}
