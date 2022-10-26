package com.github.neapovil.bannershelmet.listener;

import java.util.Locale;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.neapovil.bannershelmet.BannersHelmet;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

public class UnmergeListener implements Listener
{
    private final BannersHelmet plugin = BannersHelmet.getInstance();

    @EventHandler
    private void invetoryClick(InventoryClickEvent event)
    {
        if (!this.isCustomInventory(event.getInventory()))
        {
            return;
        }

        event.setCancelled(true);

        if (event.getRawSlot() > 2)
        {
            this.handleBottom(event);
        }
        else
        {
            this.handleTop(event);
        }
    }

    private void handleBottom(InventoryClickEvent event)
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

        if (!event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(plugin.getBannerKey()))
        {
            return;
        }

        final ItemStack itemhelmet = event.getCurrentItem().clone();

        event.getCurrentItem().subtract();

        event.getInventory().setItem(0, itemhelmet);

        final ItemStack itemresult = itemhelmet.clone();

        itemresult.editMeta(meta -> {
            meta.getPersistentDataContainer().getKeys()
                    .stream()
                    .filter(i -> i.getNamespace().equals(plugin.getName().toLowerCase(Locale.ROOT)))
                    .forEach(i -> meta.getPersistentDataContainer().remove(i));
        });

        event.getInventory().setItem(2, itemresult);
    }

    private void handleTop(InventoryClickEvent event)
    {
        if (event.getCurrentItem() == null)
        {
            return;
        }

        if (event.getCurrentItem().getType().equals(Material.AIR))
        {
            return;
        }

        final ItemStack itemstack = event.getCurrentItem().clone();

        event.getCurrentItem().subtract();

        if (event.getRawSlot() == 2)
        {
            event.getInventory().setItem(0, null);

            final Sound sound = Sound.sound(Key.key("minecraft", "block.grindstone.use"), Sound.Source.BLOCK, 1f, 1f);

            event.getWhoClicked().playSound(sound);
        }

        final Map<Integer, ItemStack> stacks = event.getWhoClicked().getInventory().addItem(itemstack);

        if (!stacks.isEmpty())
        {
            event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), itemstack);
        }

        if (event.getInventory().getItem(0) == null)
        {
            event.getInventory().setItem(2, null);
        }
    }

    private boolean isCustomInventory(Inventory inventory)
    {
        return inventory instanceof GrindstoneInventory &&
                inventory.getLocation() != null &&
                plugin.getBanners().stream()
                        .anyMatch(i -> i.equals(inventory.getLocation().getBlock().getRelative(BlockFace.UP).getType()));
    }
}
