package io.f04.code.ringcode.codec;

import io.f04.code.ringcode.constants.Constants;
import io.f04.code.ringcode.decode.Decoder;
import io.f04.code.ringcode.encode.Encoder;
import io.f04.code.ringcode.enums.ErrorCorrectionLevel;
import io.f04.code.ringcode.utils.CodecUtil;
import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class CodecTest {

    @Test
    public void testCodec() throws Exception {
        final byte[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        final byte[] mask = CodecUtil.makeType2(Constants.RING15_MAX_BYTES);
        Encoder encoder = new Encoder(ErrorCorrectionLevel.M);
        final byte[] encoded = encoder.encode(data, mask, Constants.RING15_MAX_BYTES);

        //System.out.println(Arrays.toString(encoded));
        encoded[0] = 1;
        encoded[10] = -1;

        Decoder decoder = new Decoder(ErrorCorrectionLevel.M);
        final byte[] d = decoder.decode(encoded, mask, Constants.RING15_MAX_BYTES);

        //System.out.println(Arrays.toString(d));
        assertArrayEquals(data, Arrays.copyOf(d, data.length));
    }

    @Test
    public void testCrc16() {
        final byte[] data = {0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        short code = CodecUtil.crc16((short)0, data, 2, data.length - 2);
        ByteBuffer.wrap(data).putShort(code);

        short sum = CodecUtil.crc16((short)0, data, 2, data.length - 2);
        assertEquals(code, sum);
    }

    @Test
    public void testCrc16Check() {
        final byte[] data = {0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        short code = CodecUtil.crc16((short)0, data, 2, data.length - 2);
        ByteBuffer.wrap(data).putShort(code);

        data[2] = 1;
        short sum = CodecUtil.crc16((short)0, data, 2, data.length - 2);
        assertNotEquals(code, sum);
    }
}
