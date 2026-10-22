package ch.njol.skript.reloaded.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Clamp / Linear Interpolation")
@Description({
	"'clamped between' restricts a number to a range. 'lerp' linearly interpolates between two numbers, "
		+ "where a progress of 0 gives the start and 1 gives the end."
})
@Example("set {_hp} to {_hp} clamped between 0 and 20")
@Example("set {_x} to lerp from 10 to 20 at 0.25 # 12.5")
@Since("SkriptReloaded 1.0")
public class ExprClampLerp extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprClampLerp.class, Number.class, ExpressionType.COMBINED,
			"%number% clamped (between|within) %number% and %number%",
			"lerp from %number% to %number% at %number%");
	}

	private boolean lerp;
	private Expression<Number> a, b, c;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		lerp = matchedPattern == 1;
		a = (Expression<Number>) exprs[0];
		b = (Expression<Number>) exprs[1];
		c = (Expression<Number>) exprs[2];
		return true;
	}

	@Override
	protected Number @Nullable [] get(Event event) {
		Number x = a.getSingle(event), y = b.getSingle(event), z = c.getSingle(event);
		if (x == null || y == null || z == null)
			return new Number[0];
		if (lerp)
			return new Number[]{x.doubleValue() + (y.doubleValue() - x.doubleValue()) * z.doubleValue()};
		double min = Math.min(y.doubleValue(), z.doubleValue());
		double max = Math.max(y.doubleValue(), z.doubleValue());
		return new Number[]{Math.max(min, Math.min(max, x.doubleValue()))};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (lerp)
			return "lerp from " + a.toString(event, debug) + " to " + b.toString(event, debug) + " at " + c.toString(event, debug);
		return a.toString(event, debug) + " clamped between " + b.toString(event, debug) + " and " + c.toString(event, debug);
	}

}
