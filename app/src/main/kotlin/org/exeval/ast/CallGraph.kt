package org.exeval.ast

import FunctionDeclaration

typealias CallGraph = Map<FunctionDeclaration, Set<FunctionDeclaration>>
