package com.github.neapovil.bannershelmet.listener;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import com.github.neapovil.bannershelmet.BannersHelmet;

public class UnmergeListener implements Listener
{
    private final BannersHelmet plugin = BannersHelmet.getInstance();

    @EventHandler
    private void prepareResult(PrepareResultEvent event)
    {
        if (!(event.getInventory() instanceof GrindstoneInventory))
        {
            return;
        }

        final GrindstoneInventory grindstone = (GrindstoneInventory) event.getInventory();

        final List<ItemStack> stacks = Arrays.asList(grindstone.getContents())
                .stream()
                .filter(i -> i != null)
                .filter(i -> i.getType().toString().toLowerCase().endsWith("_helmet"))
                .filter(i -> i.getItemMeta().getPersistentDataContainer().has(plugin.getBannerKey()))
                .toList();

        if (stacks.isEmpty())
        {
            return;
        }

        final ItemStack itemhelmet = stacks.get(0).clone();

        itemhelmet.editMeta(meta -> {
            meta.getPersistentDataContainer().getKeys()
                    .stream()
                    .filter(i -> i.getNamespace().equals(plugin.getName().toLowerCase(Locale.ROOT)))
                    .forEach(i -> meta.getPersistentDataContainer().remove(i));
        });

        event.setResult(itemhelmet);
    }
}
