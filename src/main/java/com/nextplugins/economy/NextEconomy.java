package com.nextplugins.economy;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Holograms.CMIHologram;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.base.Stopwatch;
import com.henryfabio.minecraft.inventoryapi.manager.InventoryManager;
import com.henryfabio.sqlprovider.connector.SQLConnector;
import com.henryfabio.sqlprovider.executor.SQLExecutor;
import com.nextplugins.economy.api.PurseAPI;
import com.nextplugins.economy.api.backup.BackupManager;
import com.nextplugins.economy.api.conversor.ConversorManager;
import com.nextplugins.economy.api.group.GroupWrapperManager;
import com.nextplugins.economy.api.metric.MetricProvider;
import com.nextplugins.economy.api.model.account.storage.AccountStorage;
import com.nextplugins.economy.api.model.discord.manager.PayActionDiscordManager;
import com.nextplugins.economy.api.model.interactions.registry.InteractionRegistry;
import com.nextplugins.economy.api.skins.SkinsRestorerManager;
import com.nextplugins.economy.command.bukkit.registry.CommandRegistry;
import com.nextplugins.economy.command.discord.registry.DiscordCommandRegistry;
import com.nextplugins.economy.configuration.DiscordValue;
import com.nextplugins.economy.configuration.FeatureValue;
import com.nextplugins.economy.configuration.RankingValue;
import com.nextplugins.economy.configuration.registry.ConfigurationRegistry;
import com.nextplugins.economy.dao.SQLProvider;
import com.nextplugins.economy.dao.repository.AccountRepository;
import com.nextplugins.economy.listener.ListenerRegistry;
import com.nextplugins.economy.placeholder.registry.PlaceholderRegistry;
import com.nextplugins.economy.ranking.CustomRankingRegistry;
import com.nextplugins.economy.ranking.manager.LocationManager;
import com.nextplugins.economy.ranking.storage.RankingStorage;
import com.nextplugins.economy.ranking.types.ArmorStandRunnable;
import com.nextplugins.economy.ranking.types.NPCRunnable;
import com.nextplugins.economy.ranking.util.RankingChatBody;
import com.nextplugins.economy.vault.registry.VaultHookRegistry;
import com.nextplugins.economy.views.registry.InventoryRegistry;
import lombok.Getter;
import lombok.val;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@Getter
public final class NextEconomy extends JavaPlugin {

    private SQLConnector sqlConnector;
    private SQLExecutor sqlExecutor;

    private AccountRepository accountRepository;

    private AccountStorage accountStorage;
    private RankingStorage rankingStorage;

    private BackupManager backupManager;
    private LocationManager locationManager;
    private ConversorManager conversorManager;
    private GroupWrapperManager groupWrapperManager;
    private SkinsRestorerManager skinsRestorerManager;
    private PayActionDiscordManager payActionDiscordManager;

    private InteractionRegistry interactionRegistry;
    private DiscordCommandRegistry discordCommandRegistry;

    private RankingChatBody rankingChatBody;

    private File npcFile;
    private FileConfiguration npcConfig;

    @Override
    public void onLoad() {

        saveDefaultConfig();

        npcFile = new File(getDataFolder(), "npcs.yml");
        if (!npcFile.exists()) saveResource("npcs.yml", false);

        val discordSrv = new File(getDataFolder(), "libs/DiscordSRV.rar");
        if (!discordSrv.exists()) saveResource("DiscordSRV.rar", false);

        npcConfig = YamlConfiguration.loadConfiguration(npcFile);

    }

    @Override
    public void onEnable() {
        getLogger().info("Iniciando carregamento do plugin.");

        val loadTime = Stopwatch.createStarted();

        sqlConnector = SQLProvider.of(this).setup(null);
        sqlExecutor = new SQLExecutor(sqlConnector);

        accountRepository = new AccountRepository(sqlExecutor);
        accountStorage = new AccountStorage(accountRepository);

        conversorManager = new ConversorManager(accountRepository);
        rankingStorage = new RankingStorage();
        backupManager = new BackupManager();
        locationManager = new LocationManager();
        groupWrapperManager = new GroupWrapperManager();
        skinsRestorerManager = new SkinsRestorerManager();
        interactionRegistry = new InteractionRegistry();
        discordCommandRegistry = new DiscordCommandRegistry();

        rankingChatBody = new RankingChatBody();

        accountStorage.init();
        interactionRegistry.init();

        InventoryManager.enable(this);

        ConfigurationRegistry.of(this).register();
        CommandRegistry.of(this).register();
        VaultHookRegistry.of(this).register();
        MetricProvider.of(this).register();
        InventoryRegistry.of(this).register();

        Bukkit.getScheduler().runTaskLater(this, () -> {

            PlaceholderRegistry.of(this).register();
            CustomRankingRegistry.of(this).register();
            ListenerRegistry.of(this).register();

            if (!PurseAPI.init()) getLogger().info("Sistema de bolsa de valores desativado.");
            else PurseAPI.getInstance(); // force purse update

            groupWrapperManager.init();

            // bump money top one time and add, if enabled, stands/npcs
            rankingStorage.updateRanking(true);

            registerPayDiscordManager();
            discordCommandRegistry.init();

            skinsRestorerManager.init();

        }, 150L);

        loadTime.stop();
        getLogger().log(Level.INFO, "Plugin inicializado com sucesso. ({0})", loadTime);

    }

    @Override
    public void onDisable() {

        accountStorage.getCache().synchronous().invalidateAll();
        getLogger().info("Jogadores salvos com sucesso");

        unloadRanking();

        if (FeatureValue.get(FeatureValue::autoBackup)) {

            CompletableFuture.completedFuture(
                    backupManager.createBackup(null, null, accountRepository, false, false)
            ).join(); // freeze thread

        }

    }

    private void unloadRanking() {
        if (CustomRankingRegistry.getInstance().isEnabled()) {

            if (CustomRankingRegistry.getInstance().isHolographicDisplays()) {
                HologramsAPI.getHolograms(this).forEach(Hologram::delete);
            } else {
                // jump concurrentmodificationexception
                val holograms = new ArrayList<CMIHologram>();
                val hologramManager = CMI.getInstance().getHologramManager();
                for (val entry : hologramManager.getHolograms().entrySet()) {
                    if (entry.getKey().startsWith("NextEconomy")) holograms.add(entry.getValue());
                }

                holograms.forEach(hologramManager::removeHolo);
            }

            String type = RankingValue.get(RankingValue::npcType);
            if (type.equalsIgnoreCase("npc")) {

                for (val id : NPCRunnable.NPCS) {
                    val npc = CitizensAPI.getNPCRegistry().getById(id);
                    if (npc == null) continue;

                    npc.despawn();
                    npc.destroy();
                }

            }

            if (type.equalsIgnoreCase("armorstand")) {

                for (val stand : ArmorStandRunnable.STANDS) {
                    stand.remove();
                }

            }

            getLogger().info("Sistema de ranking visual descarregado com sucesso");
        }
    }

    private void registerPayDiscordManager() {
        if (!DiscordValue.get(DiscordValue::enable) || !Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) return;
        payActionDiscordManager = new PayActionDiscordManager(accountStorage);
    }

    public static NextEconomy getInstance() {
        return getPlugin(NextEconomy.class);
    }

}
