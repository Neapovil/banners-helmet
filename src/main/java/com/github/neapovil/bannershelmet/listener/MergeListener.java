package com.github.neapovil.bannershelmet.listener;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.persistence.PersistentDataType;

import com.github.neapovil.bannershelmet.BannersHelmet;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

public class MergeListener implements Listener
{
    private final BannersHelmet plugin = BannersHelmet.getInstance();

    @EventHandler
    private void inventoryClick(InventoryClickEvent event)
    {
        if (!this.isCustomInventory(event.getInventory()))
        {
            return;
        }

        event.setCancelled(true);

        if (event.getRawSlot() <= 2)
        {
            this.handleTopFirstSecondItem(event);
            this.handleTopResultItem(event);
        }
        else
        {
            this.handleBottomFirstItem(event);
            this.handleBottomSecondItem(event);
        }

        this.handleResultItem(event);
    }

    private void handleBottomFirstItem(InventoryClickEvent event)
    {
        if (event.getCurrentItem() == null)
        {
            return;
        }

        if (event.getCurrentItem().getType().equals(Material.AIR))
        {
            return;
        }

        if (event.getInventory().getItem(0) != null)
        {
            return;
        }

        if (!event.getCurrentItem().getType().toString().toLowerCase().endsWith("_helmet"))
        {
            return;
        }

        if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(plugin.getBannerKey()))
        {
            return;
        }

        final ItemStack itemhelmet = event.getCurrentItem().clone();

        event.getCurrentItem().setAmount(0);

        event.getInventory().setItem(0, itemhelmet);
    }

    private void handleBottomSecondItem(InventoryClickEvent event)
    {
        if (event.getCurrentItem() == null)
        {
            return;
        }

        if (event.getCurrentItem().getType().equals(Material.AIR))
        {
            return;
        }

        if (event.getInventory().getItem(1) != null)
        {
            return;
        }

        if (plugin.getBanners().stream().noneMatch(i -> i.equals(event.getCurrentItem().getType())))
        {
            return;
        }

        final ItemStack itembanner = event.getCurrentItem().asOne();

        event.getCurrentItem().subtract();

        event.getInventory().setItem(1, itembanner);
    }

    private void handleResultItem(InventoryClickEvent event)
    {
        if (event.getInventory().getItem(0) == null)
        {
            return;
        }

        if (event.getInventory().getItem(1) == null)
        {
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            final ItemStack itembanner = event.getInventory().getItem(1);
            final ItemStack resultitem = event.getInventory().getItem(0).clone();

            resultitem.editMeta(meta -> {
                meta.getPersistentDataContainer().set(plugin.getBannerKey(), PersistentDataType.INTEGER, 1);
                meta.getPersistentDataContainer().set(plugin.getBannerTypeKey(), PersistentDataType.STRING, itembanner.getType().toString());

                final BannerMeta bannermeta = (BannerMeta) itembanner.getItemMeta();

                if (!bannermeta.getPatterns().isEmpty())
                {
                    meta.getPersistentDataContainer().set(plugin.getPatternsCountKey(), PersistentDataType.INTEGER, bannermeta.numberOfPatterns());

                    for (int i = 0; i < bannermeta.numberOfPatterns(); i++)
                    {
                        final Pattern pattern = bannermeta.getPattern(i);

                        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "pattern-" + i + "-type"), PersistentDataType.STRING,
                                pattern.getPattern().getIdentifier());
                        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "pattern-" + i + "-color"), PersistentDataType.STRING,
                                pattern.getColor().toString());
                    }
                }
            });

            event.getInventory().setItem(2, resultitem);
        });
    }

    private void handleTopFirstSecondItem(InventoryClickEvent event)
    {
        if (event.getCurrentItem() == null)
        {
            return;
        }

        if (event.getCurrentItem().getType().equals(Material.AIR))
        {
            return;
        }

        if (event.getRawSlot() > 1)
        {
            return;
        }

        event.getInventory().setItem(2, null);

        final ItemStack itemstack = event.getCurrentItem().clone();

        event.getCurrentItem().setAmount(0);

        this.addItem((Player) event.getWhoClicked(), itemstack);
    }

    private void handleTopResultItem(InventoryClickEvent event)
    {
        if (event.getCurrentItem() == null)
        {
            return;
        }

        if (event.getCurrentItem().getType().equals(Material.AIR))
        {
            return;
        }

        if (event.getRawSlot() != 2)
        {
            return;
        }

        final ItemStack resultitem = event.getCurrentItem().clone();

        event.getCurrentItem().setAmount(0);

        event.getInventory().setItem(0, null);
        event.getInventory().setItem(1, null);

        this.addItem((Player) event.getWhoClicked(), resultitem);

        final Sound sound = Sound.sound(Key.key("minecraft", "block.anvil.use"), Sound.Source.BLOCK, 1f, 1f);

        event.getWhoClicked().playSound(sound);
    }

    private boolean isCustomInventory(Inventory inventory)
    {
        return inventory instanceof AnvilInventory &&
                inventory.getLocation() != null &&
                plugin.getBanners().stream().anyMatch(i -> i.equals(inventory.getLocation().getBlock().getRelative(BlockFace.UP).getType()));
    }

    private void addItem(Player player, ItemStack... itemStacks)
    {
        final Map<Integer, ItemStack> stacks = player.getInventory().addItem(itemStacks);

        for (ItemStack i : stacks.values())
        {
            player.getWorld().dropItem(player.getLocation(), i);
        }
    }
}
