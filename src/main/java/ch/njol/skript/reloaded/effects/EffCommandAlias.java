package ch.njol.skript.reloaded.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.reloaded.util.CommandAliases;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Command Alias")
@Description({
	"Makes a command also usable under another name, for any command (vanilla, plugin or Skript). "
		+ "Arguments are kept: with the alias 'gm' for 'gamemode', '/gm creative' runs '/gamemode creative'. "
		+ "'clear all command aliases' removes every alias. Aliases live in memory, so register them in 'on load'."
})
@Example("""
	on load:
		alias command "gm" to "gamemode"
		alias command "spawn" to "warp spawn"
	""")
@Since("SkriptReloaded 1.0")
public class EffCommandAlias extends Effect {

	static {
		Skript.registerEffect(EffCommandAlias.class,
			"alias command %string% (to|for) %string%",
			"(remove|delete) command alias %string%",
			"clear all command aliases");
	}

	private int pattern;
	private Expression<String> alias, target;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		pattern = matchedPattern;
		if (pattern < 2)
			alias = (Expression<String>) exprs[0];
		if (pattern == 0)
			target = (Expression<String>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		switch (pattern) {
			case 0 -> {
				String a = alias.getSingle(event), t = target.getSingle(event);
				if (a != null && t != null && !a.isBlank() && !t.isBlank())
					CommandAliases.add(a, t);
			}
			case 1 -> {
				String a = alias.getSingle(event);
				if (a != null)
					CommandAliases.remove(a);
			}
			default -> CommandAliases.clear();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return switch (pattern) {
			case 0 -> "alias command " + alias.toString(event, debug) + " to " + target.toString(event, debug);
			case 1 -> "remove command alias " + alias.toString(event, debug);
			default -> "clear all command aliases";
		};
	}

}
