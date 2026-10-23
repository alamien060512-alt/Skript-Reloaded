package ch.njol.skript.reloaded.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

@Name("PlaceholderAPI")
@Description({
	"Parses PlaceholderAPI placeholders. 'placeholder' takes a single placeholder name without percent signs; "
		+ "'placeholders in' parses every %placeholder% inside a text. Without a player, only server placeholders resolve. "
		+ "If PlaceholderAPI is not installed the text is returned unchanged."
})
@Example("set {_ping} to placeholder \"player_ping\" for player")
@Example("send placeholders in \"&7Balance: &a%vault_eco_balance%\" for player to player")
@RequiredPlugins("PlaceholderAPI")
@Since("SkriptReloaded 1.0")
public class ExprPlaceholder extends SimpleExpression<String> {

	private static @Nullable Method setPlaceholders;
	private static boolean resolved;

	static {
		Skript.registerExpression(ExprPlaceholder.class, String.class, ExpressionType.COMBINED,
			"placeholder %string% [for %-offlineplayer%]",
			"placeholders in %strings% [for %-offlineplayer%]");
	}

	private boolean single;
	private Expression<String> text;
	private @Nullable Expression<OfflinePlayer> player;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		single = matchedPattern == 0;
		text = (Expression<String>) exprs[0];
		player = (Expression<OfflinePlayer>) exprs[1];
		return true;
	}

	/** Looked up reflectively so PlaceholderAPI stays an optional dependency. */
	private static synchronized @Nullable Method method() {
		if (!resolved && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			try {
				setPlaceholders = Class.forName("me.clip.placeholderapi.PlaceholderAPI")
					.getMethod("setPlaceholders", OfflinePlayer.class, String.class);
			} catch (ReflectiveOperationException e) {
				Skript.warning("Could not hook into PlaceholderAPI: " + e);
			}
			resolved = true;
		}
		return setPlaceholders;
	}

	private String parse(@Nullable OfflinePlayer p, String s) {
		Method m = method();
		if (m == null)
			return s;
		try {
			return (String) m.invoke(null, p, s);
		} catch (ReflectiveOperationException e) {
			return s;
		}
	}

	@Override
	protected String @Nullable [] get(Event event) {
		OfflinePlayer p = player != null ? player.getSingle(event) : null;
		if (single) {
			String name = text.getSingle(event);
			return name == null ? new String[0] : new String[]{parse(p, "%" + name.replace("%", "") + "%")};
		}
		return text.stream(event).map(s -> parse(p, s)).toArray(String[]::new);
	}

	@Override
	public boolean isSingle() {
		return single || text.isSingle();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (single ? "placeholder " : "placeholders in ") + text.toString(event, debug)
			+ (player != null ? " for " + player.toString(event, debug) : "");
	}

}
