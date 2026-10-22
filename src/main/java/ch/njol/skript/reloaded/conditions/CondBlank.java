package ch.njol.skript.reloaded.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Is Blank")
@Description("Checks whether a text is empty or consists only of whitespace.")
@Example("if arg-1 is blank:\n\tsend \"Give me something!\"")
@Since("SkriptReloaded 1.0")
public class CondBlank extends Condition {

	static {
		Skript.registerCondition(CondBlank.class,
			"%strings% (is|are) blank",
			"%strings% (isn't|is not|aren't|are not) blank");
	}

	private Expression<String> strings;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		strings = (Expression<String>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return strings.check(event, String::isBlank, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return strings.toString(event, debug) + (isNegated() ? " is not" : " is") + " blank";
	}

}
