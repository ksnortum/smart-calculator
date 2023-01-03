package calculator

import java.math.BigInteger

private const val INVALID_EXPRESSION = "Invalid expression"
private const val INVALID_IDENTIFIER = "Invalid identifier"
private const val INVALID_ASSIGNMENT = "Invalid assignment"
private const val UNKNOWN_VARIABLE = "Unknown variable"

private val numberRegex = "^([-+]?\\d+)".toRegex()
private val opRegex = "^(\\++|-+|\\*|/|\\^)".toRegex()  // Tied to enum Operator, no parens
private val identRegex = "^([a-zA-Z]+)".toRegex()

private val vars = mutableMapOf<String, BigInteger>()

private val postfix = ArrayDeque<Any>()
private val ops = ArrayDeque<Calculator.Operator>()

class Calculator {
    data class OperatorRecord(val symbol: String, val precedence: Int)

    // See also: checkOperator()
    enum class Operator(val data: OperatorRecord) {
        PLUS(OperatorRecord("+", 1)),
        MINUS(OperatorRecord("-", 1)),
        MULTIPLY(OperatorRecord("*", 2)),
        DIVIDE(OperatorRecord("/", 2)),
        EXPONENT(OperatorRecord("^", 3)),
        LEFT_PAREN(OperatorRecord("(", 5)),
        RIGHT_PAREN(OperatorRecord(")", 5)),
        UNKNOWN(OperatorRecord("", 0))
    }

    private val allOperators = Operator.values().joinToString("") {
        if (it.data.symbol !in "()") it.data.symbol else ""
    }
    private var input = ""

    fun run() {
        println("Enter your expression.  Type /help for help or /exit to quit:")

        while (true) {
            input = readln().trim()
            if (input.isEmpty()) continue

            // Do "/" commands
            if (input.startsWith('/')) {
                when (input) {
                    "/exit" -> break
                    "/help" -> printHelp()
                    else -> println("Unknown Command")
                }
            } else {
                try {
                    // Might be an assignment
                    if (input.contains('=')) {
                        processAssignment()
                    } else {
                        infixToPostfix()
                        println(evaluateExpression())
                    }
                // All errors from doAssignment() and evaluateExpression() (and below) bubble up to here
                } catch (e: Exception) {
                    println(e.message)
                }
            }
        }

        println("Exiting calculator")
    }

    /**
     * Assignment in the form identifier1 = (intValue | identifier2)
     */
    private fun processAssignment() {
        // Get left-hand side of assignment
        val lhs = getIdent()

        // Get assignment operator
        input = input.trimStart()
        // if (input.isEmpty()) not necessary because we know the input contains an =
        if (input.first() != '=') throw Exception(INVALID_IDENTIFIER)
        input = input.drop(1).trimStart()
        if (input.isEmpty()) throw Exception(INVALID_ASSIGNMENT)

        // Get right-hand side of assignment
        val rhs: BigInteger
        val matchResult = numberRegex.find(input)

        // Not an integer
        if (matchResult == null) {
            val ident: String
            // An invalid identifier here is considered an invalid assignment for some reason.
            try {
                ident = getIdent()
            } catch (e: Exception) {
                throw Exception(INVALID_ASSIGNMENT)
            }
            rhs = getVariable(ident)
        } else {
            // Is an integer
            input = input.replace(numberRegex, "")
            rhs = matchResult.value.toBigInteger()
        }

        // Nothing can come after the assignment
        if (input.isBlank()) {
            vars[lhs] = rhs
        } else {
            throw Exception(INVALID_ASSIGNMENT)
        }
    }

    /**
     * Get an identifier.  Then it looks ahead for certain conditions before returning
     */
    private fun getIdent(): String {
        val matchResult = identRegex.find(input) ?: throw Exception(INVALID_IDENTIFIER)
        input = input.replace(identRegex, "")

        // These are the only things that can follow a valid identifier
        if (input.isBlank() || // end of line
            input.first().isWhitespace() || // a whitespace character
            input.trimStart().first() == '=' || // an equals sign
            input.trimStart().first() == ')' || // a close paren
            allOperators.contains(input.trimStart().first()) // or any valid operator
        ) {
            return matchResult.value
        }

        throw Exception(INVALID_IDENTIFIER)
    }

    /**
     * @return the integer amount for this variable, or throws error
     */
    private fun getVariable(ident: String): BigInteger {
        return if (vars.contains(ident)) {
            vars[ident]!!
        } else {
            throw Exception(UNKNOWN_VARIABLE)
        }
    }

    private fun infixToPostfix() {
        postfix.clear()
        ops.clear()

        while (true) {
            processLeftParen()
            processInteger()
            if (input.isEmpty()) break
            processRightParen()
            if (input.isEmpty()) break
            processOperator()
        }

        // pop all ops
        while (ops.isNotEmpty() && ops.last() != Operator.LEFT_PAREN) {
            postfix.addLast(ops.removeLast())
        }

        if (ops.isNotEmpty()) {
            throw Exception(INVALID_EXPRESSION)
        }
    }

    private fun processInteger() {
        postfix.addLast(getNumber())
    }

    private fun processOperator() {
        val op = getOperator()

        if (op == Operator.UNKNOWN) {
            throw Exception(INVALID_EXPRESSION)
        }

        if (ops.isEmpty() ||
            ops.last() == Operator.LEFT_PAREN ||
            op.data.precedence > ops.last().data.precedence
        ) {
            ops.addLast(op)
        } else if (op.data.precedence <= ops.last().data.precedence) {
            while (ops.isNotEmpty() &&
                op.data.precedence <= ops.last().data.precedence &&
                ops.last() != Operator.LEFT_PAREN
            ) {
                postfix.addLast(ops.removeLast())
            }
            ops.addLast(op)
        } else {
            throw Exception("How did we get here, else in if operator, processOperator()")
        }
    }

    private fun processLeftParen() {
        input = input.trimStart()

        while (input.isNotEmpty() && input.first() == '(') {
            ops.addLast(Operator.LEFT_PAREN)
            input = input.substring(1).trimStart()
        }
    }

    private fun processRightParen() {
        input = input.trimStart()

        while (input.isNotEmpty() && input.first() == ')') {
            while (ops.isNotEmpty() && ops.last() != Operator.LEFT_PAREN) {
                postfix.addLast(ops.removeLast())
            }

            if (ops.isNotEmpty()) {
                // Remove left paren
                ops.removeLast()
            } else {
                throw Exception(INVALID_EXPRESSION)
            }

            input = input.substring(1).trimStart()
        }
    }

    private fun evaluateExpression(): BigInteger {
        val result = ArrayDeque<BigInteger>()

        while (postfix.isNotEmpty()) {
            when (val element = postfix.removeFirst()) {
                is BigInteger -> result.addLast(element)
                is Operator -> {
                    val a = result.removeLast()
                    val b = result.removeLast()
                    result.addLast(when (element.data.symbol) {
                        "+" -> b + a
                        "-" -> b - a
                        "*" -> b * a
                        "/" -> b / a
                        "^" -> b.pow(a.toInt())
                        else -> throw Exception(INVALID_EXPRESSION)
                    })
                }
            }
        }

        return result.removeLast()
    }

    /**
     * Get an integer or a variable value from the input string.
     */
    private fun getNumber(): BigInteger {
        input = input.trimStart()
        if (input.isEmpty()) throw Exception(INVALID_EXPRESSION)
        val matchResults = numberRegex.find(input)
        val result: BigInteger

        // If you don't get a number value from the regex, try a variable
        if (matchResults == null) {
            result = getVariable(getIdent())
            input = input.replace(identRegex, "").trimStart()
        } else {
            result = matchResults.value.toBigInteger()
            input = input.replace(numberRegex, "").trimStart()
        }

        return result
    }

    /**
     * Get a valid operator, (parentheses are operators)
     */
    private fun getOperator(): Operator {
        input = input.trimStart()
        val matchResults = opRegex.find(input) ?: return Operator.UNKNOWN
        val result = matchResults.value
        input = input.replace(opRegex, "").trimStart()

        return checkOperator(result)
    }

    /**
     * @return an operator, possibly UNKNOWN, based on symbol.  Takes care or multiple '+' or '-'.
     */
    private fun checkOperator(operator: String): Operator {
        if (operator.matches("\\++".toRegex())) {
            return Operator.PLUS
        } else if (operator.matches("-+".toRegex())) {
            return if (operator.length % 2 == 0) Operator.PLUS else Operator.MINUS
        }

        if (operator.length != 1) return Operator.UNKNOWN

        return when (operator) {
            "*" -> Operator.MULTIPLY
            "/" -> Operator.DIVIDE
            "^" -> Operator.EXPONENT
            else -> Operator.UNKNOWN
        }
    }

    private fun printHelp() {
        println("This is a command-line calculator that will evaluate an expression you enter.  It is\n" +
                "limited to integers, but they can be arbitrarily large.  Operators are limited to\n" +
                "addition (+), subtraction (-), multiplication (*), division(/), and exponentiation (^).\n" +
                "The normal precedence of operator is enforced and parentheses can override this.  Errors\n" +
                "are displayed for malformed expressions.  Variables can be set to hold values (but not\n" +
                "expressions).  Identifiers are made of upper and lowercase Latin character.  Using\n" +
                "variables looks like this:\n" +
                "\n" +
                "a = 12\n" +
                "b = 4\n" +
                "a + b\n" +
                "\n" +
                "Type /exit to quit the program and /help to see this message.")
    }
}

fun main() = Calculator().run()
