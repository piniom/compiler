package org.exeval.input

import org.exeval.input.interfaces.Input
import org.exeval.input.interfaces.Location

class Comment_cutter(private var inner: Input) : Input {

    enum class CommentType {
        SINGLE_LINE, MULTI_LINE
    }

    private var isAfterMultiLineComment = false;

    override var location: Location
        get() = inner.location
        set(value) {
            inner.location = value;
        }

    override fun nextChar(): Char? {
        val beginLocation = location;

        val nextChar: Char = inner.nextChar() ?: return null;
        if (!canMatchFirstCharacter(nextChar))
            return nextChar;

        val locationAfterFirstChar = location; // deep copy? how to do that in kotlin??

        return when (tryGetCommentType(inner.nextChar())) {
            CommentType.SINGLE_LINE -> cutUntilNewLine()
            CommentType.MULTI_LINE -> cutUntilCommentFinish(beginLocation)
            else -> setLocationAndReturn(locationAfterFirstChar, nextChar)
        };
    }

    private fun canMatchFirstCharacter(char: Char): Boolean {
        val commentStartFirstChar = '/';
        return char == commentStartFirstChar;
    }

    private fun tryGetCommentType(char: Char?): CommentType? {
        val commentSingleLineSecondChar = '/';
        val commentMultiLineSecondChar = '*';

        return when (char) {
            commentSingleLineSecondChar -> CommentType.SINGLE_LINE
            commentMultiLineSecondChar -> CommentType.MULTI_LINE
            else -> null
        };
    }

    private fun setLocationAndReturn(location: Location, char: Char): Char {
        this.location = location;
        return char;
    }

    private fun cutUntilNewLine(): Char? {
        var nextChar: Char;

        do {
            nextChar = inner.nextChar() ?: return null;
        } while (!isNewLine(nextChar))

        return nextChar;
    }

    private fun cutUntilCommentFinish(locationAtBegin: Location): Char? {
        val commentLevelsAtStart = 1;
        val commentLevelsToFinish = 0;

        val sameLineReplacement = ' ';
        val multiLineReplacement = '\n'

        var hasMultiLine = false;
        var nestedCounter = commentLevelsAtStart;
        var prevChar: Char = Char.MIN_VALUE;
        var nextChar: Char = Char.MIN_VALUE;

        while (nestedCounter > commentLevelsToFinish) {
            prevChar = nextChar;
            nextChar = inner.nextChar() ?: return null;

            if (isNewLine(nextChar))
                hasMultiLine = true;
            else
                when (prevChar to nextChar) {
                    '*' to '/' -> --nestedCounter;
                    '/' to '*' -> ++nestedCounter;
                }
        }

        isAfterMultiLineComment = true;
        location = locationAtBegin;
        return if (hasMultiLine) multiLineReplacement else sameLineReplacement;
    }

    private fun isNewLine(char: Char): Boolean {
        return char == '\n';
    }

}