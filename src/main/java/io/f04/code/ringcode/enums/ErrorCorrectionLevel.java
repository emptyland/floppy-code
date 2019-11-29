package io.f04.code.ringcode.enums;

public enum ErrorCorrectionLevel {
    L(2, 1),
    M(2, 1),
    Q(3, 1),
    H(2, 2);

    ErrorCorrectionLevel(int dataShardCount, int parityShardCount) {
        this.dataShardCount = dataShardCount;
        this.parityShardCount = parityShardCount;
    }

    public int getDataShardCount() { return dataShardCount; }

    public int getParityShardCount() {
        return parityShardCount;
    }

    public int getTotalShardCount() { return dataShardCount + parityShardCount; }

    public int computePayloadSize(int total) {
        return total * dataShardCount / getTotalShardCount() - getTotalShardCount() * 2;
    }

    private int dataShardCount;

    private int parityShardCount;
}
