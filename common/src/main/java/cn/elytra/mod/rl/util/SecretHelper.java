package cn.elytra.mod.rl.util;

import java.util.Random;

public class SecretHelper {

    private static final Random RAND = new Random();

    public static String generate() {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < 4; i++) {
            result.append(RAND.nextInt(10));
        }
        return result.toString();
    }

}
