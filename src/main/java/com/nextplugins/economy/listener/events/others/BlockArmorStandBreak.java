package com.nextplugins.economy.listener.events.others;

import lombok.val;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

/**
 * @author Yuhtin
 * Github: https://github.com/Yuhtin
 */
public class BlockArmorStandBreak implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.ARMOR_STAND || !event.getEntity().hasMetadata("nexteconomy")) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        val entity = event.getRightClicked();
        if (entity.getType() != EntityType.ARMOR_STAND || !entity.hasMetadata("nexteconomy")) return;
        event.setCancelled(true);
    }

}