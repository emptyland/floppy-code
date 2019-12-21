package io.f04.code.ringcode.constants;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Constants {

    class RingProfile {
        RingProfile(int maxBytes, int imageSize, int[] angles) {
            this.maxBytes = maxBytes;
            this.imageSize = imageSize;
            this.angles = angles;
        }
        private int maxBytes;
        private int imageSize;
        private int[] angles;

        public int getMaxBytes() {
            return maxBytes;
        }

        public int getImageSize() {
            return imageSize;
        }

        public int[] getAngles() {
            return angles;
        }
    }

    //int VERSION = 0x0001; // v0.0.1

    int RING15 = 15;

    int RING15_MAX_BITS = 666 - 18;

    int RING15_IMAGE_SIZE = 280;

    int RING15_MAX_BYTES = RING15_MAX_BITS / 8;

    int[] RING15_ANGLES = {5, 5, 5, 5, 5, 5, 10, 10, 10, 15, 15, 15, 20, 20, 20};

    int RING31 = 31;

    int RING31_MAX_BITS = 2940 - 30;

    int RING31_IMAGE_SIZE = 400;

    int RING31_MAX_BYTES = RING31_MAX_BITS / 8;

    int RING_WIDTH = 7;

    int ANCHOR_WIDTH = 4;

    int[] RING31_ANGLES = {
            3, 3, 3, 3, 3, 3, 3, 3,  3,  3,  // 10
            3, 3, 3, 3, 3, 3, 3, 3,  3,  3,  // 20
            3, 6, 6, 6, 9, 9, 9, 12, 12, 12, // 30
            12,
    };

    List<Color> RING_COLORS = new ArrayList<Color> () {
        {
            add(new Color(0x03, 0xa9, 0xf4));
            add(new Color(0x00, 0xdc, 0xb4));
            add(new Color(0x00, 0x96, 0x88));
            add(new Color(0x74, 0x3a, 0xb7));
            add(new Color(0x3f, 0x51, 0xb5));
            add(new Color(0x56, 0x77, 0xfc));
            add(new Color(0x25, 0x9b, 0x24));
            add(new Color(0x8b, 0xc3, 0x4a));
        }
    };

    Color LOGO_COLOR = new Color(57, 57, 57, 255);

    String LOGO_TEXT = "fc";

    Map<Integer, RingProfile> RINGS_PROFILE = new HashMap<Integer, RingProfile>() {
        {
            put(RING15, new RingProfile(RING15_MAX_BYTES, RING15_IMAGE_SIZE, RING15_ANGLES));
            put(RING31, new RingProfile(RING31_MAX_BYTES, RING31_IMAGE_SIZE, RING31_ANGLES));
        }
    };
}
