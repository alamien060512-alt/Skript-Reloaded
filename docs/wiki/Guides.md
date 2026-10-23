# Guides

## Command aliases
```
on load:
	alias command "gm" to "gamemode"
	alias command "hub" to "warp spawn"
```
Aliases are held in memory, so register them in `on load`. They apply to players and the console, keep arguments,
and do not appear in tab completion.

## Sidebar
```
every 5 seconds:
	loop all players:
		show sidebar titled "&6&lMy Server" with lines "&7Rank: &f%{rank::%loop-player%}%", "", "&eplay.example.com" to loop-player
```
Showing a sidebar replaces the player's current scoreboard. Bossbars and tab list header/footer already exist in upstream Skript.

## HTTP requests
```
command /joke:
	trigger:
		send http get request to "https://example.com/api" and store the response in {_body} and the status in {_code}
		if {_code} is 200:
			send {_body}
		else:
			send "Request failed (%{_code}%)"
```
The effect waits like `wait` but does not freeze the server. Requests time out after 15 seconds, only http/https is allowed,
and on error the status is `-1`. Headers: `with headers "Authorization: Bearer x", "Accept: application/json"`.

## PlaceholderAPI
```
send placeholders in "&7Ping: &a%player_ping%" for player to player
```
Without PlaceholderAPI installed the text is returned unchanged.
