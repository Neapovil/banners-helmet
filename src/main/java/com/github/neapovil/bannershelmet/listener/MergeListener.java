package com.github.neapovil.bannershelmet.listener;

import java.util.Map;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import com.github.neapovil.bannershelmet.BannersHelmet;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

public final class MergeListener implements Listener
{
    private final BannersHelmet plugin = BannersHelmet.getInstance();

    @EventHandler
    private void prepareResult(PrepareResultEvent event)
    {
        if (!(event.getInventory() instanceof AnvilInventory anvil))
        {
            return;
        }

        final ItemStack firstitem = anvil.getFirstItem();
        final ItemStack seconditem = anvil.getSecondItem();

        if (firstitem == null)
        {
            return;
        }

        if (!firstitem.getType().getEquipmentSlot().equals(EquipmentSlot.HEAD))
        {
            return;
        }

        final NamespacedKey bannerkey = BannersHelmet.BANNER_KEY;

        if (firstitem.getItemMeta().getPersistentDataContainer().has(bannerkey))
        {
            return;
        }

        if (seconditem == null)
        {
            return;
        }

        if (seconditem.getType().isAir())
        {
            return;
        }

        if (plugin.getBanners().stream().noneMatch(i -> i.equals(seconditem.getType())))
        {
            return;
        }

        final ItemStack resultitem = firstitem.clone();

        resultitem.editMeta(meta -> {
            meta.getPersistentDataContainer().set(bannerkey, PersistentDataType.BYTE_ARRAY, seconditem.serializeAsBytes());
        });

        event.setResult(resultitem);

        plugin.getServer().getScheduler().runTask(plugin, () -> ((Player) event.getView().getPlayer()).updateInventory());
    }

    @EventHandler
    private void inventoryClick(InventoryClickEvent event)
    {
        if (!(event.getInventory() instanceof AnvilInventory anvil))
        {
            return;
        }

        if (event.getRawSlot() != 2)
        {
            return;
        }

        final ItemStack firstitem = anvil.getItem(0);
        final ItemStack seconditem = anvil.getItem(1);
        final ItemStack resultitem = anvil.getItem(2);

        if (firstitem == null)
        {
            return;
        }

        if (!firstitem.getType().getEquipmentSlot().equals(EquipmentSlot.HEAD))
        {
            return;
        }

        final NamespacedKey bannerkey = BannersHelmet.BANNER_KEY;

        if (firstitem.getItemMeta().getPersistentDataContainer().has(bannerkey))
        {
            return;
        }

        if (seconditem == null)
        {
            return;
        }

        if (plugin.getBanners().stream().noneMatch(i -> i.equals(seconditem.getType())))
        {
            return;
        }

        if (resultitem == null)
        {
            return;
        }

        if (resultitem.getType().isAir())
        {
            return;
        }

        if (!resultitem.getItemMeta().getPersistentDataContainer().has(bannerkey))
        {
            return;
        }

        final ItemStack resultitemclone = resultitem.clone();

        resultitem.subtract();
        firstitem.subtract();
        seconditem.subtract();

        final Map<Integer, ItemStack> stacks = event.getWhoClicked().getInventory().addItem(resultitemclone);

        if (!stacks.isEmpty())
        {
            event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), resultitemclone);
        }

        final Sound sound = Sound.sound(Key.key("minecraft", "block.anvil.use"), Sound.Source.BLOCK, 1f, 1f);

        event.getWhoClicked().playSound(sound);
    }
}
