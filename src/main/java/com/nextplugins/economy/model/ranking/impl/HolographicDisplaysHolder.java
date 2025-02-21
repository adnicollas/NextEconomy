package com.nextplugins.economy.model.ranking.impl;

import com.nextplugins.economy.NextEconomy;
import com.nextplugins.economy.model.ranking.IHologramHolder;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.Location;

import java.util.List;

/**
 * @author <a href="https://github.com/Yuhtin">Yuhtin</a>
 */
public class HolographicDisplaysHolder implements IHologramHolder {

    @Override
    public void destroyHolograms(List<String> holograms) {
        HolographicDisplaysAPI.get(NextEconomy.getInstance()).getHolograms().forEach(Hologram::delete);
    }

    @Override
    public String createHologram(Location location, List<String> lines) {
        Hologram hologram = HolographicDisplaysAPI.get(NextEconomy.getInstance()).createHologram(location);
        lines.forEach(line -> hologram.getLines().appendText(line));

        return "NextEconomy";
    }

}