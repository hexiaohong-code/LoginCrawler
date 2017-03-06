package com.web.crawler.login.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * MD5加密算法
 *
 * @author 564137276@qq.com <br>
 * @since 1.0.0
 */
public final class MD5Utils {

    private final static String[] DIGITS_LOWER = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    private static ThreadLocal<MessageDigest> digestHolder = new ThreadLocal<MessageDigest>() {

        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException ex) {
                throw new IllegalStateException("no algorithm");
            }
        }
    };

    public static String encodeHexString(byte[] data) {
        StringBuffer c = new StringBuffer();
        for (int idx = 0; idx < data.length; idx++) {
            c.append(encodeHexString(data[idx], true));
        }
        return c.toString();
    }

    public static String encodeHexStringLittleEndian(byte[] data) {
        StringBuffer c = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            c.append(encodeHexString(data[i], false));
        }
        return c.toString();
    }

    private static String encodeHexString(byte data, boolean bigEndian) {
        int n = data;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return (bigEndian) ? (DIGITS_LOWER[d1] + DIGITS_LOWER[d2]) : (DIGITS_LOWER[d2] + DIGITS_LOWER[d1]);
    }

    public static byte[] decodeHexString(String data) {
        if (0 != data.length() % 2) {
            throw new RuntimeException("error hex data");
        }
        byte[] r = new byte[data.length() / 2];
        for (int i = 0; i < data.length(); ) {
            int pos = i / 2;
            char c = data.charAt(i++);
            char c2 = data.charAt(i++);
            r[pos] = Integer.decode("0x" + c + c2).byteValue();
        }
        return r;
    }

    public static String digest(String plain) {
        return digest(plain, null);
    }

    public static String digest(String plain, String encoding) {
        String target = null;
        try {
            target = new String(plain);
            MessageDigest md = digestHolder.get();
            if (null == encoding) {
                target = encodeHexString(md.digest(target.getBytes()));
            } else {
                target = encodeHexString(md.digest(target.getBytes(encoding)));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return target;
    }

    public static byte[] digest(byte plain[]) {
        try {
            MessageDigest messageDigest = digestHolder.get();
            return messageDigest.digest(plain);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String digestStr(byte plain[]) {
        try {
            return encodeHexString(digest(plain));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static MessageDigest getDigest() {
        return digestHolder.get();
    }

    public static Map<String, Object> calculateDigest(File file) throws IOException {
        Map<String, Object> map = new HashMap<String, Object>();
        FileInputStream is = new FileInputStream(file);
        long size = 0;
        String digest = StringUtils.EMPTY;
        try {
            MessageDigest md = MD5Utils.getDigest();
            byte[] buffer = new byte[1024];
            while (true) {
                int len = is.read(buffer);
                if (len < 0) {
                    break;
                }
                size += len;
                md.update(buffer, 0, len);
            }
            byte[] digests = md.digest();
            digest = MD5Utils.encodeHexString(digests);
            map.put("size", size);
            map.put("digest", digest);
        } finally {
            is.close();
        }
        return map;
    }
}
