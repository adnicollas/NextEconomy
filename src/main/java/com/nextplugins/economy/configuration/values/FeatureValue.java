package com.nextplugins.economy.configuration.values;

import com.henryfabio.minecraft.configinjector.common.annotations.ConfigField;
import com.henryfabio.minecraft.configinjector.common.annotations.ConfigFile;
import com.henryfabio.minecraft.configinjector.common.annotations.ConfigSection;
import com.henryfabio.minecraft.configinjector.common.annotations.TranslateColors;
import com.henryfabio.minecraft.configinjector.common.injector.ConfigurationInjectable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.function.Function;

@Getter
@TranslateColors
@Accessors(fluent = true)
@ConfigSection("plugin.configuration")
@ConfigFile("config.yml")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FeatureValue implements ConfigurationInjectable {

    @Getter private static final FeatureValue instance = new FeatureValue();

    // configuration

    @ConfigField("initial-balance") private double initialBalance;
    @ConfigField("format-type") private String formatType;
    @ConfigField("save-delay") private int saveDelay;

    public static <T> T get(Function<FeatureValue, T> function) {
        return function.apply(instance);
    }

}
