package ch.njol.skript.reloaded.util;

import ch.njol.skript.Skript;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rewrites aliased commands (e.g. /gm -> /gamemode) before they are dispatched.
 * Aliases are kept in memory only, so scripts should (re)register them on load.
 */
public final class CommandAliases implements Listener {

	private static final Map<String, String> ALIASES = new ConcurrentHashMap<>();
	private static CommandAliases instance;

	private CommandAliases() {}

	public static synchronized void add(String alias, String target) {
		if (instance == null) {
			instance = new CommandAliases();
			Bukkit.getPluginManager().registerEvents(instance, Skript.getInstance());
		}
		ALIASES.put(normalize(alias), target.startsWith("/") ? target.substring(1) : target);
	}

	public static void remove(String alias) {
		ALIASES.remove(normalize(alias));
	}

	public static synchronized void clear() {
		ALIASES.clear();
		if (instance != null) {
			HandlerList.unregisterAll(instance);
			instance = null;
		}
	}

	private static String normalize(String s) {
		return (s.startsWith("/") ? s.substring(1) : s).toLowerCase(Locale.ENGLISH).trim();
	}

	/** @param line a command line without the leading slash */
	private static String rewrite(String line) {
		int space = line.indexOf(' ');
		String label = (space == -1 ? line : line.substring(0, space)).toLowerCase(Locale.ENGLISH);
		String target = ALIASES.get(label);
		if (target == null)
			return null;
		return space == -1 ? target : target + line.substring(space);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayer(PlayerCommandPreprocessEvent event) {
		String rewritten = rewrite(event.getMessage().substring(1));
		if (rewritten != null)
			event.setMessage("/" + rewritten);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onConsole(ServerCommandEvent event) {
		String rewritten = rewrite(event.getCommand());
		if (rewritten != null)
			event.setCommand(rewritten);
	}

}
