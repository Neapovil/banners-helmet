package com.github.neapovil.bannershelmet.listener;

import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import com.github.neapovil.bannershelmet.BannersHelmet;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

public class UnmergeListener implements Listener
{
    private final BannersHelmet plugin = BannersHelmet.getInstance();

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

        if (!event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(plugin.getBannerKey()))
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
                meta.getPersistentDataContainer().set(plugin.getClearEnchantsKey(), PersistentDataType.INTEGER, 1);
            });
        }

        grindstone.setUpperItem(itemstackinput);

        event.getCurrentItem().setAmount(0);

        final ItemStack itemstackresult = itemstackinput.clone();

        itemstackresult.editMeta(meta -> {
            meta.getPersistentDataContainer().getKeys()
                    .stream()
                    .filter(i -> i.getNamespace().equals(plugin.getName().toLowerCase(Locale.ROOT)))
                    .filter(i -> !i.equals(plugin.getClearEnchantsKey()))
                    .forEach(i -> meta.getPersistentDataContainer().remove(i));

            if (meta.getPersistentDataContainer().has(plugin.getClearEnchantsKey()))
            {
                meta.getEnchants().keySet().forEach(i -> meta.removeEnchant(i));
            }

            meta.getPersistentDataContainer().set(plugin.getRemovalKey(), PersistentDataType.INTEGER, 1);
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

        if (!event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(plugin.getBannerKey()))
        {
            return;
        }

        event.setCancelled(true);

        grindstone.getResult().setAmount(0);

        final ItemStack itemstackclone = event.getCurrentItem().clone();

        if (itemstackclone.getItemMeta().getPersistentDataContainer().has(plugin.getClearEnchantsKey()))
        {
            itemstackclone.getEnchantments().keySet().forEach(i -> itemstackclone.removeEnchantment(i));
            itemstackclone.editMeta(meta -> {
                meta.getPersistentDataContainer().remove(plugin.getClearEnchantsKey());
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

        if (!event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(plugin.getRemovalKey()))
        {
            return;
        }

        grindstone.getUpperItem().setAmount(0);

        final ItemStack itemstackclone = event.getCurrentItem().clone();

        itemstackclone.editMeta(meta -> {
            meta.getPersistentDataContainer().getKeys()
                    .stream()
                    .filter(i -> i.getNamespace().equals(plugin.getName().toLowerCase(Locale.ROOT)))
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

        if (!grindstone.getResult().getItemMeta().getPersistentDataContainer().has(plugin.getRemovalKey()))
        {
            return;
        }

        event.setCancelled(true);
    }
}
