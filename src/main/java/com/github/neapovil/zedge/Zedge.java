package com.github.neapovil.zedge;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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
import org.bukkit.plugin.java.JavaPlugin;

public final class Zedge extends JavaPlugin implements Listener
{
    private static Zedge instance;
    private final List<Material> materials = Arrays.asList(Material.values())
            .stream()
            .filter(i -> i.toString().toLowerCase().endsWith("banner"))
            .filter(i -> !i.toString().toLowerCase().contains("wall"))
            .toList();
    private final NamespacedKey bannerKey = new NamespacedKey(this, "hasBanner");
    private final NamespacedKey bannerType = new NamespacedKey(this, "bannerType");
    private final NamespacedKey patternsCount = new NamespacedKey(this, "patternsCount");
    private final Map<UUID, ArmorStand> entities = new HashMap<>();

    @Override
    public void onEnable()
    {
        instance = this;

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable()
    {
    }

    public static Zedge getInstance()
    {
        return instance;
    }

    @EventHandler
    private void merge(InventoryClickEvent event)
    {
        if (event.getCursor() == null)
        {
            return;
        }

        if (!this.materials.contains(event.getCursor().getType()))
        {
            return;
        }

        if (event.getCurrentItem() == null)
        {
            return;
        }

        if (!event.getCurrentItem().getType().toString().toLowerCase().endsWith("_helmet"))
        {
            return;
        }

        if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(this.bannerKey))
        {
            return;
        }

        event.setCancelled(true);

        event.getCurrentItem().editMeta(meta -> {
            meta.getPersistentDataContainer().set(this.bannerKey, PersistentDataType.INTEGER, 1);
            meta.getPersistentDataContainer().set(this.bannerType, PersistentDataType.STRING, event.getCursor().getType().toString());

            final BannerMeta bannermeta = (BannerMeta) event.getCursor().getItemMeta();

            if (!bannermeta.getPatterns().isEmpty())
            {
                meta.getPersistentDataContainer().set(this.patternsCount, PersistentDataType.INTEGER, bannermeta.numberOfPatterns());

                for (int i = 0; i < bannermeta.numberOfPatterns(); i++)
                {
                    final Pattern pattern = bannermeta.getPattern(i);

                    meta.getPersistentDataContainer().set(new NamespacedKey(this, "pattern-" + i + "-type"), PersistentDataType.STRING,
                            pattern.getPattern().getIdentifier());
                    meta.getPersistentDataContainer().set(new NamespacedKey(this, "pattern-" + i + "-color"), PersistentDataType.STRING,
                            pattern.getColor().toString());
                }
            }
        });

        event.getWhoClicked().sendMessage("banner merged");
    }

    @EventHandler
    private void despawnBanner(InventoryClickEvent event)
    {
        if (event.getWhoClicked().getInventory().getHelmet() == null)
        {
            return;
        }

        final ArmorStand entity = this.entities.remove(event.getWhoClicked().getUniqueId());

        if (entity != null)
        {
            entity.remove();
        }
    }

    // @EventHandler
    private void unmerge(InventoryClickEvent event)
    {
        if (event.getWhoClicked().getInventory().getHelmet() == null)
        {
            return;
        }

        if (event.getCurrentItem() == null)
        {
            return;
        }

        if (event.getCurrentItem().getType().equals(Material.AIR))
        {
            return;
        }

        if (!event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(this.bannerKey))
        {
            return;
        }

        event.getCurrentItem().editMeta(i -> {
            i.getPersistentDataContainer().getKeys()
                    .stream()
                    .filter(i1 -> i1.getNamespace().equals(this.getName().toLowerCase(Locale.ROOT)))
                    .forEach(i1 -> i.getPersistentDataContainer().remove(i1));
        });

        final ArmorStand entity = this.entities.remove(event.getWhoClicked().getUniqueId());

        if (entity != null)
        {
            entity.remove();
        }

        event.getWhoClicked().getPersistentDataContainer().remove(this.bannerKey);

        event.getWhoClicked().sendMessage("banner unmerged");
    }

    @EventHandler
    private void spawnBanner(InventoryClickEvent event)
    {
        this.getServer().getScheduler().runTaskLater(this, () -> this.spawnBanner((Player) event.getWhoClicked()), 1);
    }

    @EventHandler
    private void playerMove(PlayerMoveEvent event)
    {
        final ArmorStand entity = this.entities.get(event.getPlayer().getUniqueId());

        if (entity == null)
        {
            return;
        }

        entity.teleportAsync(event.getTo());
    }

    @EventHandler
    private void playerQuit(PlayerQuitEvent event)
    {
        final ArmorStand entity = this.entities.remove(event.getPlayer().getUniqueId());

        if (entity != null)
        {
            entity.remove();
        }
    }

    @EventHandler
    private void playerJoin(PlayerJoinEvent event)
    {
        this.spawnBanner(event.getPlayer());
    }

    @EventHandler
    private void playerInteract(PlayerInteractEvent event)
    {
        if (!event.getMaterial().toString().toLowerCase().endsWith("_helmet"))
        {
            return;
        }

        this.getServer().getScheduler().runTaskLater(this, () -> this.spawnBanner(event.getPlayer()), 1);
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

        if (!helmet.getItemMeta().getPersistentDataContainer().has(this.bannerKey))
        {
            return;
        }

        final String bannertype = helmet.getItemMeta().getPersistentDataContainer().get(this.bannerType, PersistentDataType.STRING);
        final ItemStack itemstack = new ItemStack(Material.getMaterial(bannertype), 1);

        if (helmet.getItemMeta().getPersistentDataContainer().has(this.patternsCount))
        {
            final int patternscount = helmet.getItemMeta().getPersistentDataContainer().get(this.patternsCount, PersistentDataType.INTEGER);

            for (int i = 0; i < patternscount; i++)
            {
                final int inneri = i;

                itemstack.editMeta(BannerMeta.class, bannermeta -> {
                    final String patternidentifier = helmet.getItemMeta().getPersistentDataContainer().get(
                            new NamespacedKey(this, "pattern-" + inneri + "-type"),
                            PersistentDataType.STRING);
                    final String color = helmet.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(this, "pattern-" + inneri + "-color"),
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

        this.entities.put(player.getUniqueId(), entity);
    }
}
