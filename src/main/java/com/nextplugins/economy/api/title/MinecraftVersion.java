package com.nextplugins.economy.api.title;

import lombok.AllArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;

@AllArgsConstructor
enum MinecraftVersion {

    v1_8("1_8"),
    v1_9("1_9"),
    v1_10("1_10"),
    v1_11("1_11"),
    v1_12("1_12"),
    v1_13("1_13"),
    v1_14("1_14"),
    v1_15("1_15"),
    v1_16("1_16"),
    v1_17("1_17"),
    v1_18("1_18"),
    v1_19("1_19"),
    v1_20("1_20"),
    v1_21("1_21");

    private final String key;

    public boolean isLessThanOrEqualTo(MinecraftVersion other) {
        return ordinal() <= other.ordinal();
    }

    private static final String VERSION = getServerVersion();
    private static final MinecraftVersion MINECRAFT_VERSION = MinecraftVersion.build();

    private static String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String[] parts = packageName.split("\\.");
        if (parts.length > 3) {
            return parts[3];
        }
        return "unknown";
    }

    private static MinecraftVersion build() {
        for (val version : MinecraftVersion.values()) {
            if (VERSION.contains(version.key)) {
                return version;
            }
        }

        return v1_21;
    }

    public static MinecraftVersion get() {
        return MINECRAFT_VERSION;
    }
}