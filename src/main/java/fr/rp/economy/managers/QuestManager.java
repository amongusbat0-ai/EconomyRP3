package fr.rp.economy.managers;

import fr.rp.economy.EconomyRP;
import fr.rp.economy.models.Quete;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public class QuestManager {

    private final EconomyRP plugin;
    private final Map<String, Quete> quetes = new LinkedHashMap<>();

    public QuestManager(EconomyRP plugin) {
        this.plugin = plugin;
    }

    public void charger() {
        quetes.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("quetes");
        if (section == null) return;
        for (String id : section.getKeys(false)) {
            ConfigurationSection q = section.getConfigurationSection(id);
            if (q == null) continue;
            quetes.put(id, new Quete(
                    id,
                    q.getString("nom", id),
                    q.getString("description", ""),
                    q.getString("difficulte", "facile"),
                    q.getDouble("recompense", 0),
                    q.getString("materiau", "DIRT"),
                    q.getInt("quantite", 1)
            ));
        }
    }

    public Map<String, Quete> getQuetes() {
        return quetes;
    }

    public Quete getQuete(String id) {
        return quetes.get(id);
    }

    /** Compte combien le joueur possede du materiau requis */
    public int compterMateriau(Player joueur, Material material) {
        int total = 0;
        for (ItemStack item : joueur.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                total += item.getAmount();
            }
        }
        return total;
    }

    /** Retire la quantite du materiau de l'inventaire du joueur */
    public void retirerMateriau(Player joueur, Material material, int quantite) {
        int restant = quantite;
        ItemStack[] contenu = joueur.getInventory().getContents();
        for (int i = 0; i < contenu.length && restant > 0; i++) {
            ItemStack item = contenu[i];
            if (item != null && item.getType() == material) {
                int aRetirer = Math.min(restant, item.getAmount());
                item.setAmount(item.getAmount() - aRetirer);
                restant -= aRetirer;
                if (item.getAmount() <= 0) {
                    joueur.getInventory().setItem(i, null);
                } else {
                    joueur.getInventory().setItem(i, item);
                }
            }
        }
    }

    /** Tente de valider une quete. Retourne true si le joueur avait assez d'objets et a ete recompense. */
    public boolean validerQuete(Player joueur, Quete quete) {
        Material material;
        try {
            material = Material.valueOf(quete.getMateriau());
        } catch (IllegalArgumentException e) {
            return false;
        }
        int possede = compterMateriau(joueur, material);
        if (possede < quete.getQuantite()) return false;

        retirerMateriau(joueur, material, quete.getQuantite());
        plugin.getEconomyManager().donner(joueur.getUniqueId(), quete.getRecompense());
        return true;
    }
}
