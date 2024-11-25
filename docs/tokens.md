# Tokens

The list of tokens available in our language is in [`app/src/main/kotlin/org/exeval/utilities/TokenCategories.kt`](../app/src/main/kotlin/org/exeval/utilities/TokenCategories.kt) file.


## Regex syntax


Those are supported regex operators, listed by precedence:

| Symbol | Meaning                                               |
|--------|-------------------------------------------------------|
| ()     | parentheses                                           |
| *      | Kleene star                                           |
|        | concatenation                                         |
| \|     | or                                                    |

And those are available character groups:

| Symbol | Character group                                       |
|--------|-------------------------------------------------------|
| \a     | all letters (A-Za-z)                                  |
| \l     | all lowercase letters (a-z)                           |
| \u     | all upercase letters (A-Z)                            |
| \d     | all digits                                            |
| \s     | all whitespaces                                       |
| \i     | all symbols allowed in identifiers (A-Z, a-z, 0-9, _) |
