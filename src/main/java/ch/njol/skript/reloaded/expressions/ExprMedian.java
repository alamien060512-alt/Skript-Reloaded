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

import java.util.Arrays;

@Name("Median / Standard Deviation")
@Description("The median or the (population) standard deviation of a list of numbers.")
@Example("set {_m} to median of {scores::*}")
@Example("broadcast \"spread: %standard deviation of {scores::*}%\"")
@Since("SkriptReloaded 1.0")
public class ExprMedian extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprMedian.class, Number.class, ExpressionType.COMBINED,
			"[the] median of %numbers%",
			"[the] standard deviation of %numbers%");
	}

	private Expression<Number> numbers;
	private boolean deviation;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		numbers = (Expression<Number>) exprs[0];
		deviation = matchedPattern == 1;
		return true;
	}

	@Override
	protected Number @Nullable [] get(Event event) {
		Number[] values = numbers.getArray(event);
		if (values.length == 0)
			return new Number[0];
		double[] d = Arrays.stream(values).mapToDouble(Number::doubleValue).sorted().toArray();
		if (deviation) {
			double mean = Arrays.stream(d).average().orElse(0);
			double variance = Arrays.stream(d).map(v -> (v - mean) * (v - mean)).average().orElse(0);
			return new Number[]{Math.sqrt(variance)};
		}
		int mid = d.length / 2;
		return new Number[]{d.length % 2 == 1 ? d[mid] : (d[mid - 1] + d[mid]) / 2};
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
		return (deviation ? "standard deviation" : "median") + " of " + numbers.toString(event, debug);
	}

}
