package org.exeval.cfg

interface TreeKind

object ConstantTreeKind : TreeKind

object MemoryTreeKind : TreeKind

object RegisterTreeKind : TreeKind

object AssignmentTreeKind : TreeKind

object BinaryAddTreeKind : TreeKind

object BinarySubtractTreeKind : TreeKind

object BinaryMultiplyTreeKind : TreeKind

object BinaryDivideTreeKind : TreeKind

object BinaryAndTreeKind : TreeKind

object BinaryOrTreeKind : TreeKind

object BinaryGreaterTreeKind : TreeKind

object BinaryGreaterEqualTreeKind : TreeKind

object BinaryEqualTreeKind : TreeKind

object BinaryLessTreeKind : TreeKind

object BinaryLessEqualTreeKind : TreeKind

object UnaryNotTreeKind : TreeKind

object UnaryMinusTreeKind : TreeKind

object CallTreeKind : TreeKind

object ReturnTreeKind : TreeKind

object StackPushTreeKind : TreeKind

object StackPopTreeKind : TreeKind
