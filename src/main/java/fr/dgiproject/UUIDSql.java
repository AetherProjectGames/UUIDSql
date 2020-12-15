package fr.dgiproject;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class UUIDSql extends Plugin {

	String[] dbInformation = new String[3];
	private final PlayerListener playerListener = new PlayerListener(this,
			dbInformation);

	@Override
	public void onEnable() {

		PluginManager pm = getProxy().getPluginManager();

		File config = new File(getDataFolder() + File.separator + "config.yml");

		try {
			if(!getDataFolder().exists()) {
				getDataFolder().mkdir();
			}
			/*if(!config.exists()) {
				config.createNewFile();
			}*/

			if (!config.exists()) {
				getLogger().info("Creating configs file !");
				Configuration configuration = new Configuration();
//				Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
				configuration.set("dbName", "userUUID");
				configuration.set("username", "root");
				configuration.set("pass", "root");
				configuration.set("host", "jdbc:mysql://localhost:3306/");
				ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, config);
			} else {
				Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
				String dbURL = configuration.getString("host")
						+ configuration.getString("dbName");
				String username = configuration.getString("username");
				String password = configuration.getString("pass");

				dbInformation[0] = dbURL;
				dbInformation[1] = username;
				dbInformation[2] = password;
				Connection dbCon = null;
				try {
					dbCon = DriverManager.getConnection(dbInformation[0], dbInformation[1], dbInformation[2]);
					createTables(dbCon, this);
				} catch (SQLException e) {
					this.getLogger().severe("An error occured while connecting to the db, please change the config file." + e.getMessage());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		pm.registerListener(this, playerListener);
		getLogger().info("[UUIDSql] Loaded !");

	}

	@Override
	public void onDisable() {
		getLogger().info("[UUIDSql] Unloaded");
	}

	private static void createTables(Connection connection, Plugin plugin) {

		Statement statement;
		try {
			statement = connection.createStatement();
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS `userUUID` (" +
					"`id` int(11) NOT NULL AUTO_INCREMENT," +
					"`uuid` text NOT NULL," +
					"`username` text NOT NULL," +
					"PRIMARY KEY (`id`)" +
					");");
			plugin.getLogger().info("[UUIDSql] MySQL table has been created");
		} catch (SQLException e) {
			plugin.getLogger().severe("[UUIDSql]An error occured while creating MySQL table, please change the config file");
			plugin.getLogger().severe("Cause: " + e.getMessage());
		}

	}


}