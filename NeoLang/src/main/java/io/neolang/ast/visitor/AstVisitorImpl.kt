package io.neolang.ast.visitor

import io.neolang.ast.base.NeoLangAst
import io.neolang.ast.node.*

/**
 * grammar: [
 * program: group (group)*
 * group: attribute (attribute)*
 * attribute: ID COLON block
 * block: STRING | NUMBER | (BRACKET_START group BRACKET_END)
 * ]
 */

/**
 * @author kiva
 */
internal object AstVisitorImpl {
    fun visitProgram(ast: NeoLangProgramNode, visitorCallback: IVisitorCallback) {
        visitorCallback.onStart()
        ast.groups.forEach { visitGroup(it, visitorCallback) }
        visitorCallback.onFinish()
    }

    fun visitGroup(ast: NeoLangGroupNode, visitorCallback: IVisitorCallback) {
        ast.attributes.forEach {
            visitAttribute(it, visitorCallback)
        }
    }

    fun visitAttribute(ast: NeoLangAttributeNode, visitorCallback: IVisitorCallback) {
        visitBlock(ast.blockNode, ast.stringNode.eval().asString(), visitorCallback)
    }

    fun visitBlock(ast: NeoLangBlockNode, blockName: String, visitorCallback: IVisitorCallback) {
        val visitingNode = ast.ast
        when (visitingNode) {
            is NeoLangGroupNode -> {
                // is a sub block, e.g.
                // block: { sub-block: {} }
                visitorCallback.onEnterContext(blockName)
                AstVisitorImpl.visitGroup(visitingNode, visitorCallback)
                visitorCallback.onExitContext()
            }
            is NeoLangStringNode -> {
                // block: { node: "hello" }
                visitorCallback.getCurrentContext().defineAttribute(blockName, visitingNode.eval())
            }
            is NeoLangNumberNode -> {
                // block: { node: 123.456 }
                visitorCallback.getCurrentContext().defineAttribute(blockName, visitingNode.eval())
            }
        }
    }

    fun visitStartAst(ast: NeoLangAst, visitorCallback: IVisitorCallback) {
        when (ast) {
            is NeoLangProgramNode -> AstVisitorImpl.visitProgram(ast, visitorCallback)
            is NeoLangGroupNode -> AstVisitorImpl.visitGroup(ast, visitorCallback)
        }
    }
}
