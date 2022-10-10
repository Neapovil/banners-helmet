package com.github.neapovil.bannershelmet;

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
import org.bukkit.enchantments.Enchantment;
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
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

public final class BannersHelmet extends JavaPlugin implements Listener
{
    private static BannersHelmet instance;
    private final List<Material> materials = Arrays.asList(Material.values())
            .stream()
            .filter(i -> i.toString().toLowerCase().endsWith("banner"))
            .filter(i -> !i.toString().toLowerCase().contains("wall"))
            .toList();
    private final NamespacedKey bannerKey = new NamespacedKey(this, "hasBanner");
    private final NamespacedKey bannerType = new NamespacedKey(this, "bannerType");
    private final NamespacedKey patternsCount = new NamespacedKey(this, "patternsCount");
    private final NamespacedKey clearEnchantsKey = new NamespacedKey(this, "clearEnchants");
    private final NamespacedKey removal = new NamespacedKey(this, "removal");
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

    public static BannersHelmet getInstance()
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

    @EventHandler
    private void setGrindstoneResult(InventoryClickEvent event)
    {
        if (!(event.getInventory() instanceof GrindstoneInventory))
        {
            return;
        }

        final GrindstoneInventory grindstone = (GrindstoneInventory) event.getInventory();

        if (event.getRawSlot() <= 2)
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

        event.setCancelled(true);

        if (grindstone.getUpperItem() != null || grindstone.getLowerItem() != null)
        {
            return;
        }

        final ItemStack itemstackinput = event.getCurrentItem().clone();

        if (!itemstackinput.getItemMeta().hasEnchants())
        {
            itemstackinput.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            itemstackinput.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemstackinput.editMeta(meta -> {
                meta.getPersistentDataContainer().set(this.clearEnchantsKey, PersistentDataType.INTEGER, 1);
            });
        }

        grindstone.setUpperItem(itemstackinput);

        event.getCurrentItem().setAmount(0);

        final ItemStack itemstackresult = itemstackinput.clone();

        itemstackresult.editMeta(meta -> {
            meta.getPersistentDataContainer().getKeys()
                    .stream()
                    .filter(i -> i.getNamespace().equals(this.getName().toLowerCase(Locale.ROOT)))
                    .filter(i -> !i.equals(this.clearEnchantsKey))
                    .forEach(i -> meta.getPersistentDataContainer().remove(i));

            if (meta.getPersistentDataContainer().has(this.clearEnchantsKey))
            {
                meta.getEnchants().keySet().forEach(i -> meta.removeEnchant(i));
            }

            meta.getPersistentDataContainer().set(this.removal, PersistentDataType.INTEGER, 1);
        });

        grindstone.setResult(itemstackresult);
    }

    @EventHandler
    private void grindstoneInputs(InventoryClickEvent event)
    {
        if (!(event.getInventory() instanceof GrindstoneInventory))
        {
            return;
        }

        final GrindstoneInventory grindstone = (GrindstoneInventory) event.getInventory();

        if (event.getRawSlot() >= 1)
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

        event.setCancelled(true);

        grindstone.getResult().setAmount(0);

        final ItemStack itemstackclone = event.getCurrentItem().clone();

        if (itemstackclone.getItemMeta().getPersistentDataContainer().has(this.clearEnchantsKey))
        {
            itemstackclone.getEnchantments().keySet().forEach(i -> itemstackclone.removeEnchantment(i));
            itemstackclone.editMeta(meta -> {
                meta.getPersistentDataContainer().remove(this.clearEnchantsKey);
            });
        }

        itemstackclone.removeItemFlags(ItemFlag.HIDE_ENCHANTS);

        event.getCurrentItem().setAmount(0);

        final int slotindex = event.getWhoClicked().getInventory().firstEmpty();

        if (slotindex == -1)
        {
            event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), itemstackclone);
        }
        else
        {
            event.getWhoClicked().getInventory().setItem(slotindex, itemstackclone);
        }
    }

    @EventHandler
    private void getGrindstoneResult(InventoryClickEvent event)
    {
        if (!(event.getInventory() instanceof GrindstoneInventory))
        {
            return;
        }

        final GrindstoneInventory grindstone = (GrindstoneInventory) event.getInventory();

        if (event.getRawSlot() != 2)
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

        if (!event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(this.removal))
        {
            return;
        }

        grindstone.getUpperItem().setAmount(0);

        final ItemStack itemstackclone = event.getCurrentItem().clone();

        itemstackclone.editMeta(meta -> {
            meta.getPersistentDataContainer().getKeys()
                    .stream()
                    .filter(i -> i.getNamespace().equals(this.getName().toLowerCase(Locale.ROOT)))
                    .forEach(i -> meta.getPersistentDataContainer().remove(i));
        });

        itemstackclone.removeItemFlags(ItemFlag.HIDE_ENCHANTS);

        event.getCurrentItem().setAmount(0);

        final int slotindex = event.getWhoClicked().getInventory().firstEmpty();

        if (slotindex == -1)
        {
            event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), itemstackclone);
        }
        else
        {
            event.getWhoClicked().getInventory().setItem(slotindex, itemstackclone);
        }

        event.getWhoClicked().playSound(Sound.sound(Key.key("block.grindstone.use"), Sound.Source.BLOCK, 1f, 1f));
    }

    @EventHandler
    private void lockGrindstone(InventoryClickEvent event)
    {
        if (!(event.getInventory() instanceof GrindstoneInventory))
        {
            return;
        }

        final GrindstoneInventory grindstone = (GrindstoneInventory) event.getInventory();

        if (grindstone.getResult() == null)
        {
            return;
        }

        if (grindstone.getResult().getType().equals(Material.AIR))
        {
            return;
        }

        if (!grindstone.getResult().getItemMeta().getPersistentDataContainer().has(this.removal))
        {
            return;
        }

        event.setCancelled(true);
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
