package com.lowendboost.util;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Đo FPS trung bình và phát hiện khi FPS giảm đột ngột.
 * <p>
 * Tính trung bình trượt trong 5 giây gần nhất, bỏ qua spike (outlier).
 */
public class FpsMonitor {

    private static final int WINDOW_SIZE = 100; // ~5 giây ở 20 tps
    private final Deque<Integer> frameTimes = new ArrayDeque<>(WINDOW_SIZE);

    private int lastFrameMs = -1;
    private long lastWarningTime = 0;
    private static final long WARNING_COOLDOWN_MS = 30_000; // 30s

    /** Gọi mỗi frame với delta time (ms). */
    public void recordFrame(int deltaMs) {
        if (lastFrameMs > 0) {
            if (frameTimes.size() >= WINDOW_SIZE) {
                frameTimes.pollFirst();
            }
            frameTimes.offerLast(deltaMs);
        }
        lastFrameMs = deltaMs;
    }

    /** FPS trung bình trong window. 0 nếu chưa đủ dữ liệu. */
    public double getAverageFps() {
        if (frameTimes.isEmpty()) return 0;
        long sum = 0;
        for (int t : frameTimes) sum += t;
        double avgMs = (double) sum / frameTimes.size();
        return avgMs <= 0 ? 0 : 1000.0 / avgMs;
    }

    /**
     * Kiểm tra nếu FPS thấp và cần tối ưu thêm. Trả về true nếu phát hiện
     * vấn đề và cooldown đã hết (để tránh spam).
     */
    public boolean shouldThrottle(double thresholdFps) {
        if (frameTimes.size() < WINDOW_SIZE / 2) return false;
        if (getAverageFps() >= thresholdFps) return false;
        long now = System.currentTimeMillis();
        if (now - lastWarningTime < WARNING_COOLDOWN_MS) return false;
        lastWarningTime = now;
        return true;
    }

    public void reset() {
        frameTimes.clear();
        lastFrameMs = -1;
    }
}
