package io.f04.code.ringcode.utils;

import java.util.Arrays;

public class MaskUtil {

    public static byte[] makeType1(int limit) {
        byte[] mask = new byte[limit];
        Arrays.fill(mask, (byte)0x55);
        return mask;
    }

    public static byte[] makeType2(int limit) {
        byte[] mask = new byte[limit];
        for (int i = 0; i < mask.length; ++i) {
            if (i % 2 == 0) {
                mask[i] = (byte)0x55;
            } else {
                mask[i] = (byte)0xaa;
            }
        }
        return mask;
    }
}
