package com.github.neapovil.bannershelmet.listener;

import java.util.Arrays;
import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import com.github.neapovil.bannershelmet.BannersHelmet;

public final class UnmergeListener implements Listener
{
    @EventHandler
    private void prepareResult(PrepareResultEvent event)
    {
        if (!(event.getInventory() instanceof GrindstoneInventory grindstone))
        {
            return;
        }

        final NamespacedKey bannerkey = BannersHelmet.BANNER_KEY;

        final List<ItemStack> stacks = Arrays.asList(grindstone.getContents())
                .stream()
                .filter(i -> i != null)
                .filter(i -> i.getType().getEquipmentSlot().equals(EquipmentSlot.HEAD))
                .filter(i -> i.getItemMeta().getPersistentDataContainer().has(bannerkey))
                .toList();

        if (stacks.isEmpty())
        {
            return;
        }

        final ItemStack itemhelmet = stacks.get(0).clone();

        itemhelmet.editMeta(meta -> {
            meta.getPersistentDataContainer().remove(bannerkey);
        });

        event.setResult(itemhelmet);
    }
}
