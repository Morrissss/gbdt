package morrissss.base.util;

import com.google.common.base.Charsets;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

public class MathUtils {

    public static double sigmoid(double val) {
        return 1.0 / (1.0 + Math.exp(-val));
    }

    public static double[] randomArray(double lowerBound, double upperBound, int length) {
        double[] result = new double[length];
        for (int i = 0; i < result.length; i++) {
            result[i] = ThreadLocalRandom.current().nextDouble(lowerBound, upperBound);
        }
        return result;
    }

    public static Hasher hasher() {
        return Hashing.murmur3_128().newHasher();
    }

    public static long longHash(String str) {
        return hasher().putString(str, Charsets.UTF_8).hash().asLong();
    }

    public static String md5(String origin) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(origin.getBytes());
            byte[] md5hash = md.digest();
            StringBuilder builder = new StringBuilder();
            for (byte b : md5hash) {
                builder.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }
}
