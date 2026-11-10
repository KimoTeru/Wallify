#!/bin/bash
# PreToolUse hook (Claude Code): file edits in the project are blocked until a doc from docs/ for the task was read
# with the Read tool in this session. The index docs/README.md is imported by CLAUDE.md and does not count. Every
# subagent has its own marker (agent_id), so it reads the docs itself. Anything unexpected lets the call through:
# the hook must never hang work.

input=$(cat)
tool=$(jq -r '.tool_name // empty' <<< "$input")
file=$(jq -r '.tool_input.file_path // .tool_input.notebook_path // empty' <<< "$input")
session=$(jq -r '(.session_id // "unknown") + "_" + (.agent_id // "main")' <<< "$input" | tr -c 'A-Za-z0-9_\n-' '_')
project="${CLAUDE_PROJECT_DIR:-$(pwd)}"

state_dir="${TMPDIR:-/tmp}/wallify-claude-docs"
marker="$state_dir/$session"

case "$tool" in
  Read)
    if [[ "$file" == "$project/docs/"*.md && "$file" != "$project/docs/README.md" ]]; then
      mkdir -p "$state_dir" && : > "$marker"
    fi
    exit 0
    ;;
  Write|Edit|MultiEdit|NotebookEdit)
    # Files outside the project (memory, scratchpad) are not project code.
    [[ "$file" != "$project/"* ]] && exit 0
    [[ -e "$marker" ]] && exit 0
    jq -n '{
      hookSpecificOutput: {
        hookEventName: "PreToolUse",
        permissionDecision: "deny",
        permissionDecisionReason: "STOP. Правка заблокирована: в этой сессии ещё не прочитан ни один док под задачу. Индекс docs/README.md уже в контексте (CLAUDE.md): найди в «Что когда читать» строки под задачу, прочитай эти доки инструментом Read, напиши строку «Доки: <файлы>» и повтори правку. Обходить хук нельзя."
      }
    }'
    exit 0
    ;;
esac
exit 0