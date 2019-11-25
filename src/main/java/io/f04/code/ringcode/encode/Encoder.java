package io.f04.code.ringcode.encode;

public class Encoder {

    public Encoder() {}

    public byte[] encode(byte[] input, byte[] mask, int limit) {
        byte[] output = new byte[limit];
        System.arraycopy(input, 0, output, 0, Math.min(input.length, limit));

        for (int i = 0; i < output.length; i++) {
            output[i] ^= mask[i];
        }
        return output;
    }
}
