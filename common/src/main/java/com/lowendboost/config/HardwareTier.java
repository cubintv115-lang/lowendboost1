package com.lowendboost.config;

/**
 * Phân loại phần cứng của người chơi.
 * Dùng để chọn profile tối ưu phù hợp.
 */
public enum HardwareTier {
    /** Laptop cực yếu, RAM ≤ 4GB, GPU onboard, CPU 2 cores */
    LOW,
    /** Laptop tầm trung, RAM 4-8GB, GPU rời yếu hoặc onboard đời mới */
    MEDIUM,
    /** Máy tính mạnh, RAM ≥ 8GB, GPU rời */
    HIGH,
    /** Không xác định được - mặc định dùng MEDIUM */
    UNKNOWN;

    /**
     * @return tier cao hơn giữa hai tier (dùng để tổng hợp nhiều chỉ số)
     */
    public static HardwareTier max(HardwareTier a, HardwareTier b) {
        return a.ordinal() > b.ordinal() ? a : b;
    }

    /**
     * @return tier thấp hơn giữa hai tier
     */
    public static HardwareTier min(HardwareTier a, HardwareTier b) {
        return a.ordinal() < b.ordinal() ? a : b;
    }
}
