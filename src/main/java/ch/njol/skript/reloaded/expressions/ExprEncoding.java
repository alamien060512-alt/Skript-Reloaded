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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Name("URL and Base64 Encoding")
@Description("Encodes or decodes texts as URL components or Base64. Invalid input to a decoder gives nothing.")
@Example("set {_q} to url encoded \"a b&c\" # a+b%26c")
@Example("set {_b} to base64 encoded \"hello\"")
@Since("SkriptReloaded 1.0")
public class ExprEncoding extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprEncoding.class, String.class, ExpressionType.COMBINED,
			"url encoded %strings%",
			"url decoded %strings%",
			"base64 encoded %strings%",
			"base64 decoded %strings%");
	}

	private int pattern;
	private Expression<String> input;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		pattern = matchedPattern;
		input = (Expression<String>) exprs[0];
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		return input.stream(event).map(s -> {
			try {
				return switch (pattern) {
					case 0 -> URLEncoder.encode(s, StandardCharsets.UTF_8);
					case 1 -> URLDecoder.decode(s, StandardCharsets.UTF_8);
					case 2 -> Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
					default -> new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
				};
			} catch (IllegalArgumentException e) {
				return null;
			}
		}).filter(java.util.Objects::nonNull).toArray(String[]::new);
	}

	@Override
	public boolean isSingle() {
		return input.isSingle();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String[] names = {"url encoded ", "url decoded ", "base64 encoded ", "base64 decoded "};
		return names[pattern] + input.toString(event, debug);
	}

}
