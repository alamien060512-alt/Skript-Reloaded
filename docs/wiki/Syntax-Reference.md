# Syntax Reference

All of these are new compared to upstream Skript 2.17.1.

## Math
| Syntax | Result |
|---|---|
| `median of %numbers%` | middle value (average of the two middle values for even counts) |
| `standard deviation of %numbers%` | population standard deviation |
| `%number% clamped between %number% and %number%` | number limited to the range |
| `lerp from %number% to %number% at %number%` | linear interpolation, `lerp from 10 to 20 at 0.25` = 12.5 |
| `%number% rounded to %number% decimal places` | `3.14159 rounded to 2 decimal places` = 3.14 |

## Text
| Syntax | Result |
|---|---|
| `title case of %strings%` | `Hello Big World` |
| `slug of %strings%` | `Héllo, World!` becomes `hello-world` |
| `word count of %string%` | number of whitespace separated words |
| `levenshtein distance between %string% and %string%` | edit distance |
| `%string% left\|right padded to %number% [with %string%]` | `"7" left padded to 3 with "0"` = `007` |
| `%strings% is blank` / `is not blank` | condition, true for empty or whitespace-only text |
| `url encoded\|decoded %strings%` | URL component encoding |
| `base64 encoded\|decoded %strings%` | Base64 (UTF-8) |

## Lists
| Syntax | Result |
|---|---|
| `distinct %objects%` | duplicates removed, order kept |
| `%objects% rotated by %number%` | `(1, 2, 3 and 4) rotated by 1` = 2, 3, 4, 1 |
| `occurrences of %object% in %objects%` | count |

## Commands
| Syntax | Effect |
|---|---|
| `alias command "gm" to "gamemode"` | `/gm creative` runs `/gamemode creative` |
| `remove command alias "gm"` | removes one alias |
| `clear all command aliases` | removes all |

## Scoreboard
| Syntax | Effect |
|---|---|
| `show sidebar titled "T" with lines "a", "b" to %players%` | per-player sidebar, max 15 lines |
| `hide sidebar from %players%` | back to the main scoreboard |

## Networking
| Syntax | Effect |
|---|---|
| `send http get\|post\|put\|patch\|delete request to %string% [with body %string%] [with headers %strings%] and store the response in %var% [and the status in %var%]` | non-blocking HTTP(S) request |

## PlaceholderAPI (optional plugin)
| Syntax | Result |
|---|---|
| `placeholder %string% [for %offlineplayer%]` | one placeholder, written without `%` signs |
| `placeholders in %strings% [for %offlineplayer%]` | parses every `%placeholder%` in the text |
