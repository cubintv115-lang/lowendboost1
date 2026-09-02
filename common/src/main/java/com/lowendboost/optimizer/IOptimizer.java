package com.lowendboost.optimizer;

import com.lowendboost.config.LowEndBoostConfig;
import com.lowendboost.hardware.HardwareInfo;
import com.lowendboost.platform.Platform;

/**
 * Một optimizer của LowEndBoost.
 * <p>
 * Mỗi optimizer chịu trách nhiệm cho một khía cạnh tối ưu:
 * render distance, particle, chunk loading, memory, animation.
 */
public interface IOptimizer {

    /** Tên ngắn (vd: "RenderDistance"). */
    String getName();

    /**
     * Áp dụng optimizer này. Gọi khi mod khởi động xong và mỗi khi config thay đổi.
     */
    void apply(Platform platform, LowEndBoostConfig config, HardwareInfo hardware);

    /**
     * Tick mỗi frame (nếu optimizer cần chạy liên tục).
     * @return true nếu cần tick tiếp, false nếu đã xong
     */
    default boolean tick(Platform platform, LowEndBoostConfig config) {
        return false;
    }

    /**
     * Hủy optimizer khi mod unload (cleanup).
     */
    default void shutdown(Platform platform) {}
}
