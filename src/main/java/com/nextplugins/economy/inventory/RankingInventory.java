package com.nextplugins.economy.inventory;

import com.google.common.collect.Lists;
import com.henryfabio.minecraft.inventoryapi.inventory.impl.paged.PagedInventory;
import com.henryfabio.minecraft.inventoryapi.item.InventoryItem;
import com.henryfabio.minecraft.inventoryapi.item.supplier.InventoryItemSupplier;
import com.henryfabio.minecraft.inventoryapi.viewer.configuration.border.Border;
import com.henryfabio.minecraft.inventoryapi.viewer.configuration.impl.ViewerConfigurationImpl;
import com.henryfabio.minecraft.inventoryapi.viewer.impl.paged.PagedViewer;
import com.nextplugins.economy.NextEconomy;
import com.nextplugins.economy.api.model.Account;
import com.nextplugins.economy.configuration.values.RankingValue;
import com.nextplugins.economy.storage.RankingStorage;
import com.nextplugins.economy.util.ItemBuilder;
import com.nextplugins.economy.util.NumberFormat;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class RankingInventory extends PagedInventory {

    private final RankingStorage rankingStorage = NextEconomy.getInstance().getRankingStorage();

    public RankingInventory() {
        super(
                "nextcash.ranking.inventory",
                RankingValue.get(RankingValue::inventoryModelTitle),
                4 * 9
        );
    }

    @Override
    protected void configureViewer(PagedViewer viewer) {
        ViewerConfigurationImpl.Paged configuration = viewer.getConfiguration();

        configuration.itemPageLimit(21);
        configuration.border(Border.of(1, 1, 2, 1));
    }

    @Override
    protected List<InventoryItemSupplier> createPageItems(PagedViewer viewer) {
        List<InventoryItemSupplier> items = Lists.newLinkedList();

        String headDisplayName = RankingValue.get(RankingValue::inventoryModelHeadDisplayName);
        List<String> headLore = RankingValue.get(RankingValue::inventoryModelHeadLore);

        AtomicInteger position = new AtomicInteger(1);

        for (Account account : rankingStorage.getRankingAccounts()) {
            String replacedDisplayName = headDisplayName.replace("$player", Bukkit.getOfflinePlayer(account.getOwner()).getName())
                    .replace("$amount", NumberFormat.format(account.getBalance()))
                    .replace("$position", String.valueOf(position.getAndIncrement()));

            List<String> replacedLore = Lists.newArrayList();

            for (String lore : headLore) {
                replacedLore.add(
                        lore.replace("$player", Bukkit.getOfflinePlayer(account.getOwner()).getName())
                                .replace("$amount", NumberFormat.format(account.getBalance()))
                                .replace("$position", String.valueOf(position.getAndIncrement()))
                );
            }

            items.add(() -> InventoryItem.of(
                    new ItemBuilder(Bukkit.getOfflinePlayer(account.getOwner()).getName())
                            .name(replacedDisplayName)
                            .setLore(replacedLore)
                            .wrap()
            ));
        }

        return items;
    }

}
