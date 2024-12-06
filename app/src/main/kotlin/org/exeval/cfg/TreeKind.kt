package org.exeval.cfg

// Define the TreeKind interface
interface TreeKind

// Implement TreeKind as singleton objects for each type
object ConstantTreeKind : TreeKind
object MemoryTreeKind : TreeKind
object RegisterTreeKind : TreeKind
object AssignmentTreeKind : TreeKind

// Binary operation kinds
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

// Unary operation kinds
object UnaryNotTreeKind : TreeKind
object UnaryMinusTreeKind : TreeKind

// Other kinds
object CallTreeKind : TreeKind
object ReturnTreeKind : TreeKind
