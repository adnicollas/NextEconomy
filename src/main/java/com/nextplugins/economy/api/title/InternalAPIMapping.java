package com.nextplugins.economy.api.title;

import com.nextplugins.economy.NextEconomy;
import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor
public enum InternalAPIMapping {

    REFLECTION(ReflectionTitleAPI.class, MinecraftVersion.v1_12),
    BUKKIT(BukkitTitleAPI.class, MinecraftVersion.v1_18),
    V1_19(ReflectionTitleAPI.class, MinecraftVersion.v1_19),
    V1_20(BukkitTitleAPI.class, MinecraftVersion.v1_20),
    V1_21(ReflectionTitleAPI.class, MinecraftVersion.v1_21);

    private final Class<? extends InternalTitleAPI> apiClass;
    private final MinecraftVersion maxVersion;

    public static InternalTitleAPI create() {
        val version = MinecraftVersion.get();
        for (val value : values()) {
            if (version.isLessThanOrEqualTo(value.maxVersion)) {
                try {
                    return value.apiClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    NextEconomy.getInstance().getLogger().warning("[TitleAPI] Erro ao tentar instanciar a api.");
                }
            }
        }
        return null;
    }
}