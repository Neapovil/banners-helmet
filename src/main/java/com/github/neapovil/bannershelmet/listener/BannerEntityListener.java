package com.github.neapovil.bannershelmet.listener;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import com.github.neapovil.bannershelmet.BannersHelmet;

public final class BannerEntityListener implements Listener
{
    private final BannersHelmet plugin = BannersHelmet.getInstance();

    @EventHandler
    private void despawnBanner(InventoryClickEvent event)
    {
        if (event.getWhoClicked().getInventory().getHelmet() == null)
        {
            return;
        }

        final Entity entity = this.getEntity(event.getWhoClicked());

        if (entity != null)
        {
            entity.remove();

            event.getWhoClicked().getPersistentDataContainer().remove(BannersHelmet.ENTITY_KEY);
        }
    }

    @EventHandler
    private void spawnBanner(InventoryClickEvent event)
    {
        plugin.getServer().getScheduler().runTask(plugin, () -> this.spawnBanner((Player) event.getWhoClicked()));
    }

    @EventHandler
    private void moveBanner(PlayerMoveEvent event)
    {
        final Entity entity = this.getEntity(event.getPlayer());

        if (entity != null)
        {
            entity.teleportAsync(event.getTo());
        }
    }

    @EventHandler
    private void despawnBanner(PlayerQuitEvent event)
    {
        final Entity entity = this.getEntity(event.getPlayer());

        if (entity != null)
        {
            entity.remove();
        }
    }

    @EventHandler
    private void spawnBanner(PlayerJoinEvent event)
    {
        this.spawnBanner(event.getPlayer());
    }

    @EventHandler
    private void spawnBanner(PlayerInteractEvent event)
    {
        if (event.getMaterial().getEquipmentSlot().equals(EquipmentSlot.HEAD))
        {
            plugin.getServer().getScheduler().runTask(plugin, () -> this.spawnBanner(event.getPlayer()));
        }
    }

    private void spawnBanner(Player player)
    {
        final ItemStack helmet = player.getInventory().getHelmet();

        if (helmet == null)
        {
            return;
        }

        if (helmet.getType().equals(Material.AIR))
        {
            return;
        }

        final NamespacedKey bannerkey = BannersHelmet.BANNER_KEY;

        if (!helmet.getItemMeta().getPersistentDataContainer().has(bannerkey))
        {
            return;
        }

        if (this.getEntity(player) != null)
        {
            return;
        }

        final byte[] bannerbytearray = helmet.getItemMeta().getPersistentDataContainer().get(bannerkey, PersistentDataType.BYTE_ARRAY);
        final ItemStack banner = ItemStack.deserializeBytes(bannerbytearray);

        final ArmorStand entity = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);

        entity.getEquipment().setHelmet(banner, true);
        entity.setInvisible(true);
        entity.setMarker(true);
        entity.setInvulnerable(true);

        player.getPersistentDataContainer().set(BannersHelmet.ENTITY_KEY, PersistentDataType.STRING, entity.getUniqueId().toString());
    }

    @Nullable
    private Entity getEntity(Entity player)
    {
        if (!player.getPersistentDataContainer().has(BannersHelmet.ENTITY_KEY))
        {
            return null;
        }

        final String uuidstring = player.getPersistentDataContainer().get(BannersHelmet.ENTITY_KEY, PersistentDataType.STRING);
        final UUID entityid = UUID.fromString(uuidstring);

        return plugin.getServer().getEntity(entityid);
    }
}
