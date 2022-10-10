package com.github.neapovil.bannershelmet.listener;

import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.persistence.PersistentDataType;

import com.github.neapovil.bannershelmet.BannersHelmet;

public class MergeListener implements Listener
{
    private final BannersHelmet plugin = BannersHelmet.getInstance();

    @EventHandler
    private void merge(InventoryClickEvent event)
    {
        if (event.getCursor() == null)
        {
            return;
        }

        if (!plugin.getBanners().contains(event.getCursor().getType()))
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

        if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(plugin.getBannerKey()))
        {
            return;
        }

        event.setCancelled(true);

        event.getCurrentItem().editMeta(meta -> {
            meta.getPersistentDataContainer().set(plugin.getBannerKey(), PersistentDataType.INTEGER, 1);
            meta.getPersistentDataContainer().set(plugin.getBannerTypeKey(), PersistentDataType.STRING, event.getCursor().getType().toString());

            final BannerMeta bannermeta = (BannerMeta) event.getCursor().getItemMeta();

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

        event.getWhoClicked().sendMessage("banner merged");
    }
}
