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

import java.text.Normalizer;
import java.util.Locale;

@Name("String Tools")
@Description({
	"Text helpers: 'title case' capitalizes every word, 'slug' makes a lowercase url-safe identifier, "
		+ "'word count' counts whitespace separated words, and 'levenshtein distance' is the number of single "
		+ "character edits needed to turn one text into another."
})
@Example("set {_t} to title case of \"hello big world\" # Hello Big World")
@Example("set {_s} to slug of \"Héllo, World!\" # hello-world")
@Example("set {_n} to word count of \"a b c\" # 3")
@Example("set {_d} to levenshtein distance between \"kitten\" and \"sitting\" # 3")
@Since("SkriptReloaded 1.0")
public class ExprStringTools extends SimpleExpression<Object> {

	private enum Mode { TITLE, SLUG, WORDS, LEVENSHTEIN }

	static {
		Skript.registerExpression(ExprStringTools.class, Object.class, ExpressionType.COMBINED,
			"title case of %strings%",
			"slug of %strings%",
			"word count of %string%",
			"levenshtein distance between %string% and %string%");
	}

	private Mode mode;
	private Expression<String> first, second;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		mode = Mode.values()[matchedPattern];
		first = (Expression<String>) exprs[0];
		if (mode == Mode.LEVENSHTEIN)
			second = (Expression<String>) exprs[1];
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		switch (mode) {
			case TITLE:
				return first.stream(event).map(ExprStringTools::titleCase).toArray(String[]::new);
			case SLUG:
				return first.stream(event).map(ExprStringTools::slug).toArray(String[]::new);
			case WORDS: {
				String s = first.getSingle(event);
				if (s == null)
					return new Object[0];
				String trimmed = s.trim();
				return new Object[]{trimmed.isEmpty() ? 0L : (long) trimmed.split("\\s+").length};
			}
			default: {
				String a = first.getSingle(event), b = second.getSingle(event);
				if (a == null || b == null)
					return new Object[0];
				return new Object[]{(long) levenshtein(a, b)};
			}
		}
	}

	static String titleCase(String s) {
		StringBuilder out = new StringBuilder(s.length());
		boolean start = true;
		for (char c : s.toCharArray()) {
			if (Character.isWhitespace(c)) {
				start = true;
				out.append(c);
			} else {
				out.append(start ? Character.toTitleCase(c) : Character.toLowerCase(c));
				start = false;
			}
		}
		return out.toString();
	}

	static String slug(String s) {
		String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
		return n.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
	}

	static int levenshtein(String a, String b) {
		int[] prev = new int[b.length() + 1], cur = new int[b.length() + 1];
		for (int j = 0; j <= b.length(); j++)
			prev[j] = j;
		for (int i = 1; i <= a.length(); i++) {
			cur[0] = i;
			for (int j = 1; j <= b.length(); j++) {
				int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
				cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
			}
			int[] t = prev; prev = cur; cur = t;
		}
		return prev[b.length()];
	}

	@Override
	public boolean isSingle() {
		return mode != Mode.TITLE && mode != Mode.SLUG || first.isSingle();
	}

	@Override
	public Class<?> getReturnType() {
		return mode == Mode.TITLE || mode == Mode.SLUG ? String.class : Long.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return switch (mode) {
			case TITLE -> "title case of " + first.toString(event, debug);
			case SLUG -> "slug of " + first.toString(event, debug);
			case WORDS -> "word count of " + first.toString(event, debug);
			case LEVENSHTEIN -> "levenshtein distance between " + first.toString(event, debug) + " and " + second.toString(event, debug);
		};
	}

}
