package org.exeval.input

import org.exeval.input.interfaces.Input
import org.exeval.input.interfaces.Location
import org.exeval.utilities.SimpleDiagnostics
import org.exeval.utilities.interfaces.Diagnostics
import org.exeval.utilities.interfaces.OperationResult

private typealias Result = OperationResult<Char?>

class CommentCutter(private val inner: Input) : Input by inner {
    companion object {
        const val NOT_FINISHED_COMMENT_ERROR_MESSAGE = "Comment has not been finished."
    }

    override fun nextChar(): Result {
        val beginLocation = location

        val nextResult = inner.nextChar()
        val nextChar = nextResult.result ?: return nextResult
        val diagnostics = nextResult.diagnostics

        return when (tryMatchFirstCharacter(nextChar)) {
            FirstCharGuessType.MAYBE_COMMENT_START -> processPotentialCommentStart(beginLocation, nextChar, diagnostics)
            FirstCharGuessType.MAYBE_COMMENT_END -> processPotentialCommentEnd(nextChar, diagnostics)
            else -> nextResult
        }
    }

    private fun processPotentialCommentStart(
        beginLocation: Location,
        firstChar: Char,
        diagnostics: List<Diagnostics>
    ): Result {
        val locationAfterFirstChar = location
        val nextResult = nextCharWithDiagnostic(diagnostics)

        return when (tryGetCommentType(nextResult.result)) {
            CommentType.SINGLE_LINE -> cutUntilNewLine(nextResult.diagnostics)
            CommentType.MULTI_LINE -> cutUntilCommentFinish(beginLocation, nextResult)
            else -> setLocationAndReturn(locationAfterFirstChar, Result(firstChar, nextResult.diagnostics))
        }
    }

    private fun processPotentialCommentEnd(firstChar: Char, diagnostics: List<Diagnostics>): Result {
        val locationAfterFirstChar = location

        val nextResult = nextCharWithDiagnostic(diagnostics)
        val nextChar = nextResult.result

        return if (isCommentEndSecondChar(nextChar))
            return nextCharWithDiagnostic(nextResult.diagnostics)
        else setLocationAndReturn(locationAfterFirstChar, Result(firstChar, nextResult.diagnostics))
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
            return FirstCharGuessType.MAYBE_COMMENT_END
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

    private fun setLocationAndReturn(location: Location, result: Result): Result {
        this.location = location
        return result
    }

    private fun nextCharWithDiagnostic(diagnostics: List<Diagnostics>): Result {
        val result = inner.nextChar()
        return OperationResult(result.result, diagnostics + result.diagnostics)
    }

    private fun cutUntilNewLine(diagnostics: List<Diagnostics>): Result {
        var result: Result

        do {
            result = nextCharWithDiagnostic(diagnostics)
        } while (!isNewLine(result.result) && !isFinish(result.result))

        return result
    }

    private fun isFinish(result: Char?): Boolean {
        return result == null
    }

    private fun cutUntilCommentFinish(beginLocation: Location, lastResult: Result): Result {
        val commentLevelsAtStart = 1
        val commentLevelsToFinish = 0

        val sameLineReplacement = ' '
        val multiLineReplacement = '\n'

        var hasMultiLine = false
        var nestedCounter = commentLevelsAtStart

        var prevChar: Result
        var nextChar = lastResult
        var locationAtEnd = location
        var prevLocation = location
        var currLocation = location

        while (nestedCounter > commentLevelsToFinish) {
            prevChar = nextChar
            nextChar = nextCharWithDiagnostic(nextChar.diagnostics)

            if (isFinish(nextChar.result)) {
                val notFinishedDiagnostic = generateNotFinishedDiagnostic(beginLocation)
                return Result(null, nextChar.diagnostics + notFinishedDiagnostic)
            }

            locationAtEnd = prevLocation
            prevLocation = currLocation
            currLocation = location

            if (isNewLine(nextChar.result))
                hasMultiLine = true
            else
                when (prevChar.result to nextChar.result) {
                    '*' to '/' -> --nestedCounter
                    '/' to '*' -> ++nestedCounter
                }
        }

        location = locationAtEnd
        val replacement = if (hasMultiLine) multiLineReplacement else sameLineReplacement
        return Result(replacement, nextChar.diagnostics)
    }

    private fun generateNotFinishedDiagnostic(beginLocation: Location): Diagnostics {
        return SimpleDiagnostics(NOT_FINISHED_COMMENT_ERROR_MESSAGE, beginLocation, location)
    }

    private fun isNewLine(char: Char?): Boolean {
        return char == '\n'
    }

    private enum class CommentType {
        SINGLE_LINE, MULTI_LINE
    }

    private enum class FirstCharGuessType {
        MAYBE_COMMENT_START, MAYBE_COMMENT_END
    }

}
