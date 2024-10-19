package org.exeval.input

import org.exeval.input.interfaces.Input
import org.exeval.input.interfaces.Location

class CommentCutter(private val inner: Input) : Input by inner {
    override fun nextChar(): Char? {
        val nextChar: Char = inner.nextChar() ?: return null

        return when (tryMatchFirstCharacter(nextChar)) {
            FirstCharGuessType.MAYBE_COMMENT_START -> processPotentialCommentStart(nextChar)
            FirstCharGuessType.MAYBE_COMMENNT_END -> processPotentialCommentEnd(nextChar)
            else -> nextChar
        }
    }

    private fun processPotentialCommentStart(firstChar: Char): Char? {
        val locationAfterFirstChar = location
        return when (tryGetCommentType(inner.nextChar())) {
            CommentType.SINGLE_LINE -> cutUntilNewLine()
            CommentType.MULTI_LINE -> cutUntilCommentFinish()
            else -> setLocationAndReturn(locationAfterFirstChar, firstChar)
        }
    }

    private fun processPotentialCommentEnd(firstChar: Char): Char? {
        val locationAfterFirstChar = location
        return if (isCommentEndSecondChar(inner.nextChar()))
            return inner.nextChar()
        else setLocationAndReturn(locationAfterFirstChar, firstChar)
    }

    private fun isCommentEndSecondChar(char: Char?): Boolean {
        val commentEndSecondChar = '/'
        return char == commentEndSecondChar
    }

    private fun canBeCommentStart(char: Char): Boolean {
        val commentStartFirstChar = '/'
        return char == commentStartFirstChar
    }

    private fun canBeCommentEnd(char: Char): Boolean {
        val commentEndFirstChar = '*'
        return char == commentEndFirstChar
    }

    private fun tryMatchFirstCharacter(char: Char): FirstCharGuessType? {
        if (canBeCommentStart(char))
            return FirstCharGuessType.MAYBE_COMMENT_START
        if (canBeCommentEnd(char))
            return FirstCharGuessType.MAYBE_COMMENNT_END
        return null
    }

    private fun tryGetCommentType(char: Char?): CommentType? {
        val commentSingleLineSecondChar = '/'
        val commentMultiLineSecondChar = '*'

        return when (char) {
            commentSingleLineSecondChar -> CommentType.SINGLE_LINE
            commentMultiLineSecondChar -> CommentType.MULTI_LINE
            else -> null
        }
    }

    private fun setLocationAndReturn(location: Location, char: Char): Char {
        this.location = location
        return char
    }

    private fun cutUntilNewLine(): Char? {
        var nextChar: Char

        do {
            nextChar = inner.nextChar() ?: return null
        } while (!isNewLine(nextChar))

        return nextChar
    }

    private fun cutUntilCommentFinish(): Char? {
        val commentLevelsAtStart = 1
        val commentLevelsToFinish = 0

        val sameLineReplacement = ' '
        val multiLineReplacement = '\n'

        var hasMultiLine = false
        var nestedCounter = commentLevelsAtStart

        var prevChar: Char
        var nextChar: Char = Char.MIN_VALUE
        var locationAtEnd = location
        var prevLocation = location
        var currLocation = location

        while (nestedCounter > commentLevelsToFinish) {
            prevChar = nextChar
            nextChar = inner.nextChar() ?: return null

            locationAtEnd = prevLocation
            prevLocation = currLocation
            currLocation = location

            if (isNewLine(nextChar))
                hasMultiLine = true
            else
                when (prevChar to nextChar) {
                    '*' to '/' -> --nestedCounter
                    '/' to '*' -> ++nestedCounter
                }
        }

        location = locationAtEnd
        return if (hasMultiLine) multiLineReplacement else sameLineReplacement
    }

    private fun isNewLine(char: Char): Boolean {
        return char == '\n'
    }

    private enum class CommentType {
        SINGLE_LINE, MULTI_LINE
    }

    private enum class FirstCharGuessType {
        MAYBE_COMMENT_START, MAYBE_COMMENNT_END
    }
}
