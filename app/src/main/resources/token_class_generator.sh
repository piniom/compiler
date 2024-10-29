#!/bin/bash

SCRIPT_DIR=$(
	cd "$(dirname "${BASH_SOURCE[0]}")"
	pwd -P
)

parse_tokens_file() {
	if [ -f "$SCRIPT_DIR/tokens.md" ]; then
		PAIRS=$(cat "$SCRIPT_DIR/tokens.md" | grep "^\S* := \S*$")

		echo "package org.exeval.utilities"
		echo -e "\nimport org.exeval.utilities.interfaces.TokenCategory"
		echo -en "\nenum class TokenCategories(val regex: String) : TokenCategory {"
		IFS=$'\n'
		SEPARATOR=''
		for PAIR in $PAIRS
		do
			NAME="`echo $PAIR | cut -d " " -f 1 | sed 's/[^_]\+/\L\u&/g;s/_//g'`"
			VALUE="`echo $PAIR | cut -d " " -f 3 | sed 's/\(\\\\\)/\1\1/g'`"

			echo "$SEPARATOR"
			echo -n "	$NAME("'"'"$VALUE"'"'")"
			SEPARATOR=","
		done
		echo -e ";\n}"
	fi
}

if [ -n "$1" ]
then
	parse_tokens_file > "$1"
else
	parse_tokens_file
fi
