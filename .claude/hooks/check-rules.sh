#!/bin/bash
# PostToolUse hook (Claude Code): after an edit checks the file against the rules of CLAUDE.md and docs/ that a
# script can see. Only lines that are new against HEAD are reported, so old code does not get in the way. Violations
# go back to the model (exit 2), which fixes them in the same turn. Anything unexpected passes silently.

input=$(cat)
file=$(jq -r '.tool_input.file_path // .tool_response.filePath // empty' <<< "$input")
project="${CLAUDE_PROJECT_DIR:-$(pwd)}"

[[ -z "$file" || ! -f "$file" || "$file" != "$project/"* ]] && exit 0
[[ "$file" == */build/* || "$file" == */.gradle/* || "$file" == */.git/* ]] && exit 0

relative="${file#"$project"/}"
head_file=$(mktemp)
trap 'rm -f "$head_file"' EXIT
git -C "$project" show "HEAD:$relative" > "$head_file" 2>/dev/null || : > "$head_file"

problems=()

# Reports every line matching the regex that is not in the HEAD version of the file.
check() {
  local regex="$1" message="$2"
  while IFS=: read -r number line; do
    [[ -z "$number" ]] && continue
    grep -Fxq -- "$line" "$head_file" && continue
    problems+=("$relative:$number — $message")
  done < <(grep -nE -- "$regex" "$file")
}

if [[ "$(tail -c1 "$file" | xxd -p)" == "0a" ]]; then
  problems+=("$relative — файл заканчивается переводом строки; убери его (code/code-style.md § Форматирование)")
fi

case "$relative" in
  *.kt)
    if [[ "$relative" == */commonMain/* ]]; then
      check '^import (android|java|javax)\.' 'в commonMain только мультиплатформенные API (architecture/overview.md § Платформы)'
    fi
    if [[ "$relative" != */dispatcher/AppDispatchers.kt ]]; then
      check 'Dispatchers\.(IO|Main|Default|Unconfined)' 'AppDispatchers вместо Dispatchers.* (code/code-style.md § Корутины)'
    fi
    check 'System\.currentTimeMillis' 'TimeProvider или kotlin.time.Clock (code/code-style.md § Время)'
    check '^import .*\.\*$' 'звёздочные импорты не пишем (code/code-style.md § Форматирование)'
    check 'override fun onCreate\(\) = ' 'onCreate() пишем раскрыто: coroutineScope.launch { launch { … } } (code/presentation.md, п. 8)'
    if [[ "$relative" == feature/* ]]; then
      check 'Color\(0x' 'цвета только из AppTheme (design/foundations.md)'
      check 'TextStyle\(' 'шрифты только из AppTheme.typography (design/foundations.md)'
      check '[^A-Za-z0-9_.][0-9]+(\.[0-9]+)?\.(dp|sp)\b' 'размеры только из AppTheme (design/foundations.md)'
      check 'instanceKeeper\.getOrCreate' 'логику создаёт retainedLogic { … } (code/presentation.md)'
    fi
    while read -r number; do
      line=$(sed -n "${number}p" "$file")
      grep -Fxq -- "$line" "$head_file" && continue
      problems+=("$relative:$number — строка длиннее 120 символов (code/code-style.md § Форматирование)")
    done < <(perl -CSD -ne 'chomp; print "$.\n" if length($_) > 120' "$file")
    ;;
  */build.gradle.kts)
    check 'repositories[[:space:]]*\{' 'repositories {} в модулях запрещены (setup/gradle-conventions.md)'
    ;;
esac

[[ ${#problems[@]} -eq 0 ]] && exit 0

{
  echo "Нарушены правила проекта, исправь в этом же ходе:"
  printf -- '- %s\n' "${problems[@]}"
} >&2
exit 2