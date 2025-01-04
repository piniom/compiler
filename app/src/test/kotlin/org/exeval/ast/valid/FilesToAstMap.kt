package org.exeval.ast.valid

import org.exeval.ast.ASTNode
import org.exeval.ast.valid.blocks.BLOCKS_BLOCK_IN_BLOCK_AST
import org.exeval.ast.valid.blocks.BLOCK_MAX_AST
import org.exeval.ast.valid.comments.COMMENTS_MIXING_COMMENTS_AST
import org.exeval.ast.valid.comments.COMMENTS_MULTI_LINE_COMMENTS_AST
import org.exeval.ast.valid.comments.COMMENTS_SINGLE_LINE_COMMENT_AST
import org.exeval.ast.valid.conditionals.CONDITIONALS_CONDITIONALS_INSIDE_LOOP_AST
import org.exeval.ast.valid.conditionals.CONDITIONALS_CONDITIONAL_CALLS_FUNCTION_AST
import org.exeval.ast.valid.conditionals.CONDITIONALS_IF_WITHOUT_ELSE_AST
import org.exeval.ast.valid.conditionals.CONDITIONALS_IF_WITH_ELSE_AST
import org.exeval.ast.valid.conditionals.CONDITIONALS_NESTED_CONDITIONALS_AST
import org.exeval.ast.valid.foonctions.FOONCTIONS_CONSTANT_FUNCTION_AST
import org.exeval.ast.valid.foonctions.FOONCTIONS_FUNCTION_WITH_ARGUMENTS_AST
import org.exeval.ast.valid.foonctions.FOONCTIONS_MULTIPLE_NESTED_FUNCTIONS_AST
import org.exeval.ast.valid.foonctions.FOONCTIONS_NESTED_FUNTION_AST
import org.exeval.ast.valid.foonctions.FOONCTIONS_RECURSSIVE_FUNCTION_AST
import org.exeval.ast.valid.indentifiers.IDENTIFIERS_FUNCTIONS_AST
import org.exeval.ast.valid.indentifiers.IDENTIFIERS_VARIABLES_AST
import org.exeval.ast.valid.loops.LOOPS_BREAK_WITH_LOOP_VALUE_IN_BREAK_AST
import org.exeval.ast.valid.loops.LOOPS_NESTED_LOOPS_BREAKS_PROPERLY_WITH_LABELS_AST
import org.exeval.ast.valid.separator.SEPARATOR_DELIMITED_STATEMENTS_AST
import org.exeval.ast.valid.separator.SEPARATOR_INSTRUCTION_BLOCK_AST
import org.exeval.ast.valid.separator.SEPARATOR_VALUE_OF_FUNCTIONS_AST
import org.exeval.ast.valid.variables.VARIABLES_BASIC_OPERATIONS_OVER_VARIABLES_AST
import org.exeval.ast.valid.variables.VARIABLES_REASIGNING_VARIABLES_AST
import org.exeval.ast.valid.arrays.SIMPLE_ARRAY_DECLARATION_AST
import org.exeval.ast.valid.arrays.SIMPLE_ARRAY_DEALOCATION_AST
import org.exeval.ast.valid.arrays.PASS_ARRAY_TO_FUNCTION_AST
import org.exeval.ast.valid.arrays.ARRAY_OF_ARRAY_AST

class FilesToAst {
    companion object {
        val MAP: Map<String, ASTNode> = mapOf(
            "src/test/resources/programs/valid/blocks/blockInBlock.exe" to BLOCKS_BLOCK_IN_BLOCK_AST,
            "src/test/resources/programs/valid/blocks/max.exe" to BLOCK_MAX_AST,
            "src/test/resources/programs/valid/comments/mixingComments.exe" to COMMENTS_MIXING_COMMENTS_AST,
            "src/test/resources/programs/valid/comments/multiLineComments.exe" to COMMENTS_MULTI_LINE_COMMENTS_AST,
            "src/test/resources/programs/valid/comments/singleLineComments.exe" to COMMENTS_SINGLE_LINE_COMMENT_AST,
            "src/test/resources/programs/valid/conditionals/conditionalCallsFunction.exe" to CONDITIONALS_CONDITIONAL_CALLS_FUNCTION_AST,
            "src/test/resources/programs/valid/conditionals/conditionalsInsideLoop.exe" to CONDITIONALS_CONDITIONALS_INSIDE_LOOP_AST,
            "src/test/resources/programs/valid/conditionals/ifWithElse.exe" to CONDITIONALS_IF_WITH_ELSE_AST,
            "src/test/resources/programs/valid/conditionals/ifWithoutElse.exe" to CONDITIONALS_IF_WITHOUT_ELSE_AST,
            "src/test/resources/programs/valid/conditionals/nestedConditionals.exe" to CONDITIONALS_NESTED_CONDITIONALS_AST,
            "src/test/resources/programs/valid/foonctions/constantFunction.exe" to FOONCTIONS_CONSTANT_FUNCTION_AST,
            "src/test/resources/programs/valid/foonctions/functionWithArguments.exe" to FOONCTIONS_FUNCTION_WITH_ARGUMENTS_AST,
            "src/test/resources/programs/valid/foonctions/multipleNestedFunctions.exe" to FOONCTIONS_MULTIPLE_NESTED_FUNCTIONS_AST,
            "src/test/resources/programs/valid/foonctions/nestedFunction.exe" to FOONCTIONS_NESTED_FUNTION_AST,
            "src/test/resources/programs/valid/foonctions/recursiveFunction.exe" to FOONCTIONS_RECURSSIVE_FUNCTION_AST,
            "src/test/resources/programs/valid/identifiers/functions.exe" to IDENTIFIERS_FUNCTIONS_AST,
            "src/test/resources/programs/valid/identifiers/variables.exe" to IDENTIFIERS_VARIABLES_AST,
            "src/test/resources/programs/valid/loops/breakWithLoopValueInBreak.exe" to LOOPS_BREAK_WITH_LOOP_VALUE_IN_BREAK_AST,
            "src/test/resources/programs/valid/loops/nestedLoopsBreaksProperlyWithLabels.exe" to LOOPS_NESTED_LOOPS_BREAKS_PROPERLY_WITH_LABELS_AST,
            "src/test/resources/programs/valid/separator/delimitedStatements.exe" to SEPARATOR_DELIMITED_STATEMENTS_AST,
            "src/test/resources/programs/valid/separator/instructionBlock.exe" to SEPARATOR_INSTRUCTION_BLOCK_AST,
            "src/test/resources/programs/valid/separator/valueOfFunctions.exe" to SEPARATOR_VALUE_OF_FUNCTIONS_AST,
            "src/test/resources/programs/valid/variables/basicOperationsOverVariables.exe" to VARIABLES_BASIC_OPERATIONS_OVER_VARIABLES_AST,
            "src/test/resources/programs/valid/variables/reassigningVariables.exe" to VARIABLES_REASIGNING_VARIABLES_AST,
            //"src/test/resources/programs/valid/arrays/simpleDeclaration.exe" to SIMPLE_ARRAY_DECLARATION_AST,
            //"src/test/resources/programs/valid/arrays/simpleDealocation.exe" to SIMPLE_ARRAY_DEALOCATION_AST,
            //"src/test/resources/programs/valid/arrays/passArrToFuncSimple.exe" to PASS_ARRAY_TO_FUNCTION_AST,
            //"src/test/resources/programs/valid/arrays/simpleArrOfArr.exe" to  ARRAY_OF_ARRAY_AST
        )
    }
}