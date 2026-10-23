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
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Name("List and Text Tools")
@Description({
	"'distinct' removes duplicate elements (keeping the first of each), 'rotated by' moves elements around the list "
		+ "(positive = towards the front), 'occurrences of' counts how often a value is in a list, "
		+ "'left/right padded' pads a text to a length, and 'rounded to N decimal places' rounds a number."
})
@Example("set {_l::*} to distinct {_l::*}")
@Example("set {_r::*} to (1, 2, 3 and 4) rotated by 1 # 2, 3, 4, 1")
@Example("set {_n} to occurrences of \"a\" in {_l::*}")
@Example("set {_s} to \"7\" left padded to 3 with \"0\" # 007")
@Example("set {_x} to 3.14159 rounded to 2 decimal places # 3.14")
@Since("SkriptReloaded 1.0")
public class ExprListTools extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprListTools.class, Object.class, ExpressionType.COMBINED,
			"distinct %objects%",
			"%objects% rotated by %number%",
			"occurrences of %object% in %objects%",
			"%string% left padded to %number% [with %-string%]",
			"%string% right padded to %number% [with %-string%]",
			"%number% rounded to %number% decimal place[s]");
	}

	private int pattern;
	private Expression<?> a, b, c;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		pattern = matchedPattern;
		a = exprs[0];
		b = exprs.length > 1 ? exprs[1] : null;
		c = exprs.length > 2 ? exprs[2] : null;
		if (pattern <= 2) {
			a = LiteralUtils.defendExpression(a);
			if (pattern == 2)
				b = LiteralUtils.defendExpression(b);
			return LiteralUtils.canInitSafely(a) && (pattern != 2 || LiteralUtils.canInitSafely(b));
		}
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		switch (pattern) {
			case 0:
				return new LinkedHashSet<>(List.of(a.getArray(event))).toArray();
			case 1: {
				Object[] in = a.getArray(event);
				Number n = ((Expression<Number>) b).getSingle(event);
				if (in.length == 0 || n == null)
					return in;
				int shift = Math.floorMod(n.intValue(), in.length);
				Object[] out = new Object[in.length];
				for (int i = 0; i < in.length; i++)
					out[i] = in[(i + shift) % in.length];
				return out;
			}
			case 2: {
				Object needle = a.getSingle(event);
				long count = 0;
				for (Object o : b.getArray(event))
					if (Objects.equals(o, needle))
						count++;
				return new Object[]{count};
			}
			case 3:
			case 4: {
				String s = (String) a.getSingle(event);
				Number n = (Number) b.getSingle(event);
				String pad = c == null ? " " : (String) c.getSingle(event);
				if (s == null || n == null)
					return new Object[0];
				if (pad == null || pad.isEmpty())
					pad = " ";
				StringBuilder fill = new StringBuilder();
				int missing = n.intValue() - s.length();
				while (fill.length() < missing)
					fill.append(pad);
				fill.setLength(Math.max(missing, 0));
				return new Object[]{pattern == 3 ? fill + s : s + fill};
			}
			default: {
				Number x = (Number) a.getSingle(event), places = (Number) b.getSingle(event);
				if (x == null || places == null)
					return new Object[0];
				double v = x.doubleValue();
				if (Double.isNaN(v) || Double.isInfinite(v))
					return new Object[]{v};
				return new Object[]{new BigDecimal(v).setScale(Math.max(places.intValue(), 0), RoundingMode.HALF_UP).doubleValue()};
			}
		}
	}

	@Override
	public boolean isSingle() {
		return pattern >= 2;
	}

	@Override
	public Class<?> getReturnType() {
		return switch (pattern) {
			case 2 -> Long.class;
			case 3, 4 -> String.class;
			case 5 -> Double.class;
			default -> Object.class;
		};
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String x = a.toString(event, debug);
		return switch (pattern) {
			case 0 -> "distinct " + x;
			case 1 -> x + " rotated by " + b.toString(event, debug);
			case 2 -> "occurrences of " + x + " in " + b.toString(event, debug);
			case 3 -> x + " left padded to " + b.toString(event, debug) + (c != null ? " with " + c.toString(event, debug) : "");
			case 4 -> x + " right padded to " + b.toString(event, debug) + (c != null ? " with " + c.toString(event, debug) : "");
			default -> x + " rounded to " + b.toString(event, debug) + " decimal places";
		};
	}

}
