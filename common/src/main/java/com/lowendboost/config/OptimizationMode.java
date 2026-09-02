package com.lowendboost.config;

/**
 * Chế độ hoạt động của mod:
 * - AUTO: tự động phát hiện phần cứng và áp dụng profile phù hợp.
 * - CUSTOM: người dùng tự cấu hình qua config file.
 * - OFF: tắt hoàn toàn, không tối ưu gì cả.
 */
public enum OptimizationMode {
    AUTO,
    CUSTOM,
    OFF
}
