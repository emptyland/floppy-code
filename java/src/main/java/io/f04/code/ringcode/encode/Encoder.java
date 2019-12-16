package io.f04.code.ringcode.encode;

import com.backblaze.erasure.ReedSolomon;
import io.f04.code.ringcode.enums.ErrorCorrectionLevel;
import io.f04.code.ringcode.exceptions.BadDataSizeException;
import io.f04.code.ringcode.utils.CodecUtil;

import java.nio.ByteBuffer;
import java.util.Random;


public class Encoder {

    private ErrorCorrectionLevel level;

    private Random rand = new Random();

    public Encoder(ErrorCorrectionLevel level) {
        this.level = level;
    }

    public byte[] encode(byte[] input, byte[] mask, int limit) throws BadDataSizeException {
        int payloadSize = level.computePayloadSize(limit);
        if (input.length > payloadSize) {
            throw new BadDataSizeException();
        }

        int shardSize = (payloadSize + level.getDataShardCount() - 1) / level.getDataShardCount();
        byte[][] shards = new byte [level.getTotalShardCount()] [shardSize];

        int remaining = input.length;
        for (int i = 0; i < level.getDataShardCount(); i++) {
            int copied = Math.min(remaining, shardSize);
            if (copied > 0) {
                System.arraycopy(input, i * shardSize, shards[i], 0, copied);
            } else {
                rand.nextBytes(shards[i]);
            }
            remaining -= copied;
        }
        if (remaining > 0) {
            throw new BadDataSizeException();
        }

        // Use Reed-Solomon to calculate the parity.
        ReedSolomon reedSolomon = ReedSolomon.create(level.getDataShardCount(), level.getParityShardCount());
        reedSolomon.encodeParity(shards, 0, shardSize);

        byte[] output = new byte[limit];
        ByteBuffer outBuf = ByteBuffer.wrap(output);
        for (int i = 0; i < level.getTotalShardCount(); i++) {
            outBuf.putShort(CodecUtil.crc16((short)0, shards[i]));
            outBuf.put(shards[i]);
        }

        for (int i = 0; i < output.length; i++) {
            output[i] ^= mask[i];
        }
        return output;
    }
}
