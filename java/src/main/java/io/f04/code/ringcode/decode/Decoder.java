package io.f04.code.ringcode.decode;

import com.backblaze.erasure.ReedSolomon;
import io.f04.code.ringcode.enums.ErrorCorrectionLevel;
import io.f04.code.ringcode.exceptions.BadDataSizeException;
import io.f04.code.ringcode.utils.CodecUtil;

import java.nio.ByteBuffer;

public class Decoder {

    private ErrorCorrectionLevel level;

    public Decoder(ErrorCorrectionLevel level) {
        this.level = level;
    }

    public byte[] decode(byte[] input, byte[] mask, int limit) throws BadDataSizeException {
        if (input.length != mask.length) {
            throw new BadDataSizeException("input.length not equals mask.length (" + input.length + ", " + mask.length + ")");
        }
        byte[] allBytes = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            allBytes[i] = (byte)(input[i] ^ mask[i]);
        }

        final int payloadSize = level.computePayloadSize(limit);
        final int shardSize = (payloadSize + level.getDataShardCount() - 1) / level.getDataShardCount();
        final byte[][] shards = new byte [level.getTotalShardCount()][shardSize];
        final boolean[] shardPresent = new boolean [level.getTotalShardCount()];

        ByteBuffer buf = ByteBuffer.wrap(allBytes);
        for (int i = 0; i < level.getTotalShardCount(); i++) {
            short checkSum = buf.getShort();
            buf.get(shards[i]);
            shardPresent[i] = CodecUtil.crc16((short) 0, shards[i]) == checkSum;
        }

        // Use Reed-Solomon to fill in the missing shards
        ReedSolomon reedSolomon = ReedSolomon.create(level.getDataShardCount(), level.getParityShardCount());
        reedSolomon.decodeMissing(shards, shardPresent, 0, shardSize);

        final byte[] output = new byte[shardSize * level.getDataShardCount()];
        for (int i = 0; i < level.getDataShardCount(); i++) {
            System.arraycopy(shards[i], 0, output, i * shardSize, shardSize);
        }
        return output;
    }
}
