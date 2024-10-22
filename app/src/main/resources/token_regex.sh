#!/bin/bash

SCRIPT_DIR=$(
	cd "$(dirname "${BASH_SOURCE[0]}")"
	pwd -P
)
cd "$SCRIPT_DIR"

if [ -f ./tokens.md ]; then
	CONTENT=$(cat ./tokens.md | grep "^\S* := \S*$" | cut -d" " -f3 | tr '\n' '|')

	echo "($CONTENT)"
fi
