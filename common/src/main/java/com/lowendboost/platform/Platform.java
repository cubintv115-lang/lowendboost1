package com.lowendboost.platform;

import com.lowendboost.config.LowEndBoostConfig;
import com.lowendboost.hardware.HardwareInfo;

import java.nio.file.Path;

/**
 * Interface trừu tượng hóa các API khác nhau giữa các phiên bản Minecraft.
 * <p>
 * Mỗi version Forge có một implementation riêng ({@code ForgePlatformXYZ.java})
 * nằm trong sub-project {@code versions/X.Y.Z-forge/}, vì API Minecraft
 * thay đổi qua từng version (ví dụ: tên class, chữ ký method).
 * <p>
 * Common code chỉ gọi qua interface này, không cần biết phiên bản cụ thể.
 */
public interface Platform {

    /** Tên phiên bản Minecraft đang chạy (vd: "1.20.1"). */
    String getMinecraftVersion();

    /** Tên loader (vd: "Forge"). */
    String getLoaderName();

    /** Tên loader version (vd: "47.2.0"). */
    String getLoaderVersion();

    /** Đường dẫn tới thư mục game (nơi có mods/, config/, saves/). */
    Path getGameDirectory();

    /**
     * Trả về render distance hiện tại của client (số chunks).
     * Trên server thì trả về -1.
     */
    int getRenderDistance();

    /** Set render distance (số chunks, 2-32). */
    void setRenderDistance(int distance);

    /**
     * Set particle setting.
     * 0 = all, 1 = decreased, 2 = minimal.
     */
    void setParticleSetting(int level);

    /** Giới hạn FPS (0 = không giới hạn). */
    void setMaxFps(int fps);

    /**
     * Tắt animation cho entity ở khoảng cách xa.
     * Gọi mỗi tick để áp dụng.
     * @return số entity đã tắt animation
     */
    int disableDistantEntityAnimations(int distanceBlocks);

    /**
     * Force GC nếu hệ thống cho phép.
     * @return số byte đã giải phóng (ước lượng), -1 nếu không thực hiện
     */
    long forceGarbageCollection();

    /**
     * Đăng ký shutdown hook để ghi log khi tắt game.
     */
    void registerShutdownHook(Runnable action);

    /**
     * Gửi thông báo cho người chơi (chat).
     * Implementation tùy chọn: có thể log thay vì chat nếu chưa vào game.
     */
    void sendInfoMessage(String message);

    /**
     * Áp dụng config lên game.
     * Đây là entry point chính: optimizer sẽ gọi method này ở runtime.
     */
    void applyConfig(LowEndBoostConfig config, HardwareInfo hardware);

    /**
     * Có đang chạy trong môi trường dedicated server không?
     * Một số optimizer chỉ áp dụng cho client.
     */
    boolean isDedicatedServer();
}
