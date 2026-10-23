package ch.njol.skript.reloaded.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Name("HTTP Request")
@Description({
	"Sends an HTTP(S) request without freezing the server. The rest of the trigger continues once the response "
		+ "arrived, like a 'wait'. The body is stored in the first variable and the status code in the optional second one. "
		+ "On a network error the body is empty and the status is -1. Headers are written as \"Name: value\". "
		+ "Requests time out after 15 seconds. Local variables survive the request, but loop values do not."
})
@Example("""
	send http get request to "https://api.example.com/data" and store the response in {_body} and the status in {_code}
	""")
@Example("""
	send http post request to {webhook} with body "{""content"":""hi""}" with headers "Content-Type: application/json" and store the response in {_r}
	""")
@Since("SkriptReloaded 1.0")
public class EffHttpRequest extends AsyncEffect {

	private static final HttpClient CLIENT = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(10))
		.followRedirects(HttpClient.Redirect.NORMAL)
		.build();

	static {
		Skript.registerEffect(EffHttpRequest.class,
			"(send|make) [an] http (:get|:post|:put|:patch|:delete) request to %string% [with body %-string%] "
				+ "[with headers %-strings%] and (store|save) [the] (response|body) in %~object% [and [the] status [code] in %-~object%]");
	}

	private String method;
	private Expression<String> url;
	private @Nullable Expression<String> body, headers;
	private Expression<?> responseVar;
	private @Nullable Expression<?> statusVar;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		getParser().setHasDelayBefore(Kleenean.TRUE);
		method = parseResult.tags.get(0).toUpperCase();
		url = (Expression<String>) exprs[0];
		body = (Expression<String>) exprs[1];
		headers = (Expression<String>) exprs[2];
		responseVar = exprs[3];
		statusVar = exprs[4];
		if (responseVar.acceptChange(ChangeMode.SET) == null || (statusVar != null && statusVar.acceptChange(ChangeMode.SET) == null)) {
			Skript.error("The response and status must be stored in variables");
			return false;
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		String target = url.getSingle(event);
		String payload = body != null ? body.getSingle(event) : null;
		String[] headerLines = headers != null ? headers.getArray(event) : new String[0];
		String result = "";
		long status = -1;
		try {
			if (target == null)
				throw new IllegalArgumentException("no url");
			URI uri = URI.create(target);
			if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
				throw new IllegalArgumentException("only http and https urls are supported");
			HttpRequest.Builder builder = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(15));
			for (String line : headerLines) {
				int colon = line.indexOf(':');
				if (colon > 0)
					builder.header(line.substring(0, colon).trim(), line.substring(colon + 1).trim());
			}
			HttpRequest.BodyPublisher publisher = payload == null
				? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(payload);
			HttpResponse<String> response = CLIENT.send(builder.method(method, publisher).build(), HttpResponse.BodyHandlers.ofString());
			result = response.body();
			status = response.statusCode();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			Skript.warning("HTTP request failed: " + e.getMessage());
		}
		responseVar.change(event, new Object[]{result}, ChangeMode.SET);
		if (statusVar != null)
			statusVar.change(event, new Object[]{status}, ChangeMode.SET);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "send http " + method.toLowerCase() + " request to " + url.toString(event, debug)
			+ " and store the response in " + responseVar.toString(event, debug);
	}

}
