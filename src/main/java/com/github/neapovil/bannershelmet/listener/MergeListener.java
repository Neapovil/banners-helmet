package com.github.neapovil.bannershelmet.listener;

import java.util.Map;

import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.persistence.PersistentDataType;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import com.github.neapovil.bannershelmet.BannersHelmet;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

public class MergeListener implements Listener
{
    private final BannersHelmet plugin = BannersHelmet.getInstance();

    @EventHandler
    private void prepareResult(PrepareResultEvent event)
    {
        if (!(event.getInventory() instanceof AnvilInventory))
        {
            return;
        }

        final AnvilInventory anvil = (AnvilInventory) event.getInventory();

        final ItemStack firstitem = anvil.getFirstItem();
        final ItemStack seconditem = anvil.getSecondItem();

        if (firstitem == null)
        {
            return;
        }

        if (!firstitem.getType().toString().toLowerCase().endsWith("_helmet"))
        {
            return;
        }

        if (firstitem.getItemMeta().getPersistentDataContainer().has(plugin.getBannerKey()))
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
            meta.getPersistentDataContainer().set(plugin.getBannerKey(), PersistentDataType.INTEGER, 1);
            meta.getPersistentDataContainer().set(plugin.getBannerTypeKey(), PersistentDataType.STRING, seconditem.getType().toString());

            final BannerMeta bannermeta = (BannerMeta) seconditem.getItemMeta();

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

        event.setResult(resultitem);

        plugin.getServer().getScheduler().runTask(plugin, () -> ((Player) event.getView().getPlayer()).updateInventory());
    }

    @EventHandler
    private void inventoryClick(InventoryClickEvent event)
    {
        if (!(event.getInventory() instanceof AnvilInventory))
        {
            return;
        }

        if (event.getRawSlot() != 2)
        {
            return;
        }

        final ItemStack firstitem = event.getInventory().getItem(0);
        final ItemStack seconditem = event.getInventory().getItem(1);
        final ItemStack resultitem = event.getInventory().getItem(2);

        if (firstitem == null)
        {
            return;
        }

        if (!firstitem.getType().toString().toLowerCase().endsWith("_helmet"))
        {
            return;
        }

        if (firstitem.getItemMeta().getPersistentDataContainer().has(plugin.getBannerKey()))
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

        if (!resultitem.getItemMeta().getPersistentDataContainer().has(plugin.getBannerKey()))
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
