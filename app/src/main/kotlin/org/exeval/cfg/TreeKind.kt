package org.exeval.cfg

// Define the TreeKind interface
interface TreeKind

// Implement TreeKind as singleton objects for each type
object ConstantKind : TreeKind
object MemoryKind : TreeKind
object RegisterKind : TreeKind
object AssignmentKind : TreeKind

// Binary operation kinds
object BinaryAddKind : TreeKind
object BinarySubtractKind : TreeKind
object BinaryMultiplyKind : TreeKind
object BinaryDivideKind : TreeKind
object BinaryAndKind : TreeKind
object BinaryOrKind : TreeKind
object BinaryGreaterKind : TreeKind
object BinaryGreaterEqualKind : TreeKind
object BinaryEqualKind : TreeKind
object BinaryLessKind : TreeKind
object BinaryLessEqualKind : TreeKind

// Unary operation kinds
object UnaryNotKind : TreeKind
object UnaryMinusKind : TreeKind

// Other kinds
object CallKind : TreeKind
object ReturnKind : TreeKind
