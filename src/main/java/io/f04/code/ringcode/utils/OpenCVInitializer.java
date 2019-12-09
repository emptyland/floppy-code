package io.f04.code.ringcode.utils;

public class OpenCVInitializer {
    private static boolean initialized = false;

    static {
        // Don't use System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        nu.pattern.OpenCV.loadShared();
        initialized = true;
    }

    public static boolean ensure() { return initialized; }
}
