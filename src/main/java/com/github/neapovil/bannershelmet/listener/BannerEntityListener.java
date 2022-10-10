package com.github.neapovil.bannershelmet.listener;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.persistence.PersistentDataType;

import com.github.neapovil.bannershelmet.BannersHelmet;

public class BannerEntityListener implements Listener
{
    private final BannersHelmet plugin = BannersHelmet.getInstance();

    @EventHandler
    private void despawnBanner(InventoryClickEvent event)
    {
        if (event.getWhoClicked().getInventory().getHelmet() == null)
        {
            return;
        }

        final ArmorStand entity = plugin.getEntities().remove(event.getWhoClicked().getUniqueId());

        if (entity != null)
        {
            entity.remove();
        }
    }

    @EventHandler
    private void spawnBanner(InventoryClickEvent event)
    {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> this.spawnBanner((Player) event.getWhoClicked()), 1);
    }

    @EventHandler
    private void moveBanner(PlayerMoveEvent event)
    {
        final ArmorStand entity = plugin.getEntities().get(event.getPlayer().getUniqueId());

        if (entity == null)
        {
            return;
        }

        entity.teleportAsync(event.getTo());
    }

    @EventHandler
    private void despawnBanner(PlayerQuitEvent event)
    {
        final ArmorStand entity = plugin.getEntities().remove(event.getPlayer().getUniqueId());

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
        if (!event.getMaterial().toString().toLowerCase().endsWith("_helmet"))
        {
            return;
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> this.spawnBanner(event.getPlayer()), 1);
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

        if (!helmet.getItemMeta().getPersistentDataContainer().has(plugin.getBannerKey()))
        {
            return;
        }

        final String bannertype = helmet.getItemMeta().getPersistentDataContainer().get(plugin.getBannerTypeKey(), PersistentDataType.STRING);
        final ItemStack itemstack = new ItemStack(Material.getMaterial(bannertype), 1);

        if (helmet.getItemMeta().getPersistentDataContainer().has(plugin.getPatternsCountKey()))
        {
            final int patternscount = helmet.getItemMeta().getPersistentDataContainer().get(plugin.getPatternsCountKey(), PersistentDataType.INTEGER);

            for (int i = 0; i < patternscount; i++)
            {
                final int inneri = i;

                itemstack.editMeta(BannerMeta.class, bannermeta -> {
                    final String patternidentifier = helmet.getItemMeta().getPersistentDataContainer().get(
                            new NamespacedKey(plugin, "pattern-" + inneri + "-type"),
                            PersistentDataType.STRING);
                    final String color = helmet.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "pattern-" + inneri + "-color"),
                            PersistentDataType.STRING);

                    final PatternType patterntype = PatternType.getByIdentifier(patternidentifier);
                    final DyeColor dyecolor = DyeColor.valueOf(color);

                    bannermeta.addPattern(new Pattern(dyecolor, patterntype));
                });
            }
        }

        final ArmorStand entity = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);

        entity.getEquipment().setHelmet(itemstack, true);
        entity.setInvisible(true);
        entity.setMarker(true);

        plugin.getEntities().put(player.getUniqueId(), entity);
    }
}
