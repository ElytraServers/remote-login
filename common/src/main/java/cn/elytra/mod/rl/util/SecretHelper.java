package cn.elytra.mod.rl.util;

import java.util.Random;

public class SecretHelper {

    private static final Random RAND = new Random();

    public static String generate() {
        String[] numbers = new String[4];
        for(int i = 0; i < 4; i++) {
            numbers[i] = String.valueOf(RAND.nextInt(10));
        }
        return String.join("-", numbers);
    }

}
