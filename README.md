# VerityPresence
Kinda like Verity but as a 1.16.5 Paper Plugin. Not tested against other Paper versions. I'd love if other people test it against other versions

> A consent-based, fictional Minecraft horror-presence plugin for **Paper 1.16.5** and **Java 11**.

VerityPresence creates private, opt-in encounters with **Verity**: an eerie fictional presence that appears in the Minecraft world as a carved pumpkin visual and can answer voluntary player messages through a Groq-backed chat-completions integration.

Verity is not a moderation tool, a real person, or a real-world surveillance system. It is fictional server lore. Players opt in with `/verity accept` and can stop participation at any time with `/verity leave`.

## Features

- Per-player opt-in and opt-out commands.
- Private Verity encounters near opted-in players.
- Vanilla carved-pumpkin visual; no resource pack is required.
- Cave ambience and red particle effects.
- Per-player encounter tracking, fear score, and short voluntary-message history.
- Private AI replies through Groq's OpenAI-compatible chat-completions API.
- Configurable persona, response length, player cooldown, global API limit, event timing, and normal-chat behavior.
- Non-blocking asynchronous HTTP requests.
- One retry for temporary HTTP/network failures.
- Safe local fallback dialogue if AI is disabled, unavailable, rate-limited, or misconfigured.
- Basic privacy keyword filtering for messages that should not be sent to the AI.
- Does not log the configured API key.

## Compatibility

| Component | Supported target |
|---|---|
| Server | Paper 1.16.5 |
| Java | Java 11 |
| Plugin API declaration | `api-version: 1.16` |
| Visual | Vanilla carved pumpkin |
| AI endpoint | Groq OpenAI-compatible Chat Completions API |

This plugin was written against Paper 1.16.5. It may or may not work on newer Paper versions; newer versions are not tested or supported by this release. Back up your server and test in a non-production world before changing Minecraft/Paper versions.

## Install

### 1. Requirements

- A running Paper 1.16.5 server.
- Java 11 for the Paper 1.16.5 server and Maven build.
- Maven, if building from source.
- A Groq account/API key only if AI replies are desired.

### 2. Build from source

```bash
cd ~/MC_SERVER/VerityPresence
mvn clean package
```

A successful build produces:

```txt
target/veritypresence-1.1.0.jar
```

### 3. Install the JAR

```bash
cp target/veritypresence-1.1.0.jar ~/MC_SERVER/server/plugins/
sudo systemctl restart paper
```

Paper creates the runtime configuration directory after it enables the plugin:

```txt
~/MC_SERVER/server/plugins/VerityPresence/
```

### 4. Configure AI safely

Stop the server before editing configuration:

```bash
sudo systemctl stop paper
nano ~/MC_SERVER/server/plugins/VerityPresence/config.yml
```

Set the `ai.api-key` value to a newly created Groq API key **on the server only**:

```yaml
ai:
  enabled: true
  api-key: "YOUR_NEW_GROQ_KEY_GOES_HERE"
```

Never commit, paste, screenshot, or upload this live runtime config. Keep the repository template value as:

```yaml
api-key: "PASTE_YOUR_GROQ_KEY_HERE"
```

Restrict local file access and start the server:

```bash
chmod 600 ~/MC_SERVER/server/plugins/VerityPresence/config.yml
sudo systemctl start paper
```

If no valid key is configured, the plugin remains usable and sends local fallback dialogue rather than making AI requests.

## Commands

| Command | Description |
|---|---|
| `/verity accept` | Opt in to Verity encounters and AI interactions. |
| `/verity leave` | Opt out, remove the current visual, and preserve existing stored progress/history locally. |
| `/verity status` | View whether your encounter is active or paused. |
| `/verity <message>` | Send a voluntary private message to Verity after opting in. |
| `/verity reload` | Reload `config.yml`; requires `verity.admin` (OP by default). |

Alias: `/v`

## Permissions

| Permission | Default | Description |
|---|---:|---|
| `verity.use` | Everyone | Use Verity player commands. |
| `verity.admin` | OP | Reload the plugin configuration. |

## Recommended first-run configuration

Start in intentional command-only mode while verifying API access, response tone, and moderation behavior:

```yaml
ai:
  cooldown-seconds: 45
  global-requests-per-minute: 6
  max-reply-characters: 220

chat:
  listen-to-normal-chat: false
  response-chance-percent: 0
```

Restart Paper after changing config:

```bash
sudo systemctl restart paper
```

Test in-game:

```txt
/verity accept
/verity status
/verity Who are you?
/verity Where are you?
/verity leave
```

After testing, ambient normal-chat responses can be enabled deliberately:

```yaml
chat:
  listen-to-normal-chat: true
  response-chance-percent: 5
  minimum-message-length: 4
```

Start with a low percentage. Normal chat is only considered for opted-in players; it is still best to tell players clearly when their voluntary in-game messages can be submitted to an external AI provider.

## Configuration overview

### `ai`

| Key | Meaning |
|---|---|
| `enabled` | Enables Groq API requests. Set `false` for local fallback-only mode. |
| `api-key` | Groq key. Put the real value only in the live server config. |
| `endpoint` | OpenAI-compatible Chat Completions endpoint. |
| `model` | Groq model name to request. Verify availability in your Groq account. |
| `timeout-seconds` | Per-request maximum wait time. |
| `cooldown-seconds` | Minimum time between AI requests from the same player. |
| `global-requests-per-minute` | Shared rolling per-minute request ceiling. |
| `max-reply-characters` | Hard character limit applied to accepted replies. |
| `max-history-messages` | Number of recent stored dialogue lines used as context. |
| `retry-on-temporary-error` | Retries once for temporary network, 429, and 5xx failures. |

### `events`

| Key | Meaning |
|---|---|
| `enabled` | Enables timed presence events. |
| `min-delay-seconds` / `max-delay-seconds` | Random interval range between encounters. |
| `only-at-night-or-underground` | Restricts events to night or below the configured terrain threshold behavior. |
| `max-active-seconds` | How long the carved-pumpkin visual remains. |

### `chat` and `privacy`

- `listen-to-normal-chat` controls whether regular chat may trigger an AI request.
- `response-chance-percent` controls the random trigger rate for normal chat that does not explicitly name Verity.
- `require-name-for-normal-chat: true` restricts normal chat triggers to messages that mention `Verity`.
- `ignored-words` is a basic keyword list for text that must not be submitted to the provider. Add terms appropriate for your server, but do not treat it as complete privacy protection.

## Privacy and data

- AI functionality sends a selected player’s voluntary Verity message, limited recent Verity conversation history, player name, world name, block coordinates, encounter count, and fear score to the configured AI provider.
- The plugin stores accepted status, encounter count, fear score, timestamps, and short history locally in `plugins/VerityPresence/players.yml`.
- This data may contain player chat excerpts. Treat `players.yml`, console logs, backups, and the runtime `config.yml` as private server data.
- Do not enable AI responses without clearly informing your community that voluntary Verity messages may be processed by the configured external provider.
- Use command-only mode if you want the clearest consent model.

## Operational safety

- Do not store secrets in Git. The committed `src/main/resources/config.yml` is a template, not a live config.
- Keep `.gitignore` for runtime folders, logs, local environment files, and server data.
- If a key is pasted into a message, committed locally, logged, or otherwise exposed, revoke it and create a replacement.
- Never use GitHub's “allow secret” bypass for a real credential.
- Back up your server before installing or updating the plugin.
- Use a test server before deploying changes to a public server.

## Troubleshooting

### The plugin does not load

- Confirm the server is Paper 1.16.5 and Java is version 11.
- Run `mvn clean package` and confirm it ends with `BUILD SUCCESS`.
- Confirm the built JAR is in `server/plugins/`.
- Read the first error in the Paper console; do not paste API keys or full runtime config in support requests.

### Verity gives fallback dialogue

This is expected when AI is disabled, no API key is configured, the key is rejected, a request times out, Groq is rate-limited, or the provider returns an unusable response. Check Paper logs for an HTTP status code without sharing your key.

### HTTP 401 or 403

The configured key is invalid, revoked, or unavailable to the requested service/model. Create a new key, update only the live server config, and restart Paper.

### HTTP 429

The provider or plugin rate limit was reached. Wait, lower player activity, or lower the plugin's configured request rate. The plugin falls back to local dialogue after retry behavior is exhausted.

### Verity does not react to normal chat

Check all of the following:

- The player has run `/verity accept`.
- `chat.listen-to-normal-chat` is `true`.
- The message meets `minimum-message-length`.
- It does not contain a configured ignored word.
- It either contains `Verity`, or passes the configured random response chance.
- The per-player cooldown and global request cap have not been reached.

## License and attribution

Choose and add a license before public distribution. Do not represent Verity as a real entity or imply that the plugin collects real-world personal information. The included carved-pumpkin visual uses vanilla Minecraft content and no custom resource pack is required.
