#!/bin/bash
# preToolUse hook: file edits are blocked until docs/README.md was read with the Read tool in this chat.
# Reading it (Read, or cat in the shell) leaves a marker named after the chat; subagents have their own chat id, so
# they have to read the docs themselves. Anything unexpected lets the call through: the hook must never hang work.

input=$(cat)
tool=$(echo "$input" | jq -r '.tool_name // empty')
conversation=$(echo "$input" | jq -r '.conversation_id // "unknown"' | tr -c 'A-Za-z0-9_\n-' '_')
payload=$(echo "$input" | jq -c '.tool_input // {}')

state_dir="${TMPDIR:-/tmp}/wallify-docs-read"
marker="$state_dir/$conversation"

allow() {
  echo '{ "permission": "allow" }'
  exit 0
}

case "$tool" in
  Read)
    if [[ "$payload" == *"docs/README.md"* ]]; then
      mkdir -p "$state_dir" && : > "$marker"
    fi
    allow
    ;;
  Shell)
    if [[ "$payload" =~ (cat|head|less|bat)[^|\;\&]*docs/README\.md ]]; then
      mkdir -p "$state_dir" && : > "$marker"
    fi
    allow
    ;;
  Write|StrReplace|Delete|EditNotebook|ApplyPatch|MultiEdit|Edit)
    if [[ -e "$marker" ]]; then
      allow
    fi
    jq -n \
      --arg user "Правка заблокирована: агент ещё не прочитал docs/README.md в этом чате." \
      --arg agent "STOP. Before any file edit read the project docs: call the Read tool on docs/README.md, find the rows for your task in «Что когда читать», Read those docs, write one line «Доки: <files>», then repeat this edit. Do not try to bypass this hook." \
      '{ permission: "deny", user_message: $user, agent_message: $agent }'
    exit 0
    ;;
  *)
    allow
    ;;
esac