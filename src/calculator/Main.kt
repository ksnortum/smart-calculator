package calculator

private const val INVALID_EXPRESSION = "Invalid expression"
private const val INVALID_IDENTIFIER = "Invalid identifier"
private const val INVALID_ASSIGNMENT = "Invalid assignment"
private const val UNKNOWN_VARIABLE = "Unknown variable"

private val intRegex = "^([+-]?\\d+)".toRegex()
private val opRegex = "^(\\++|-+|\\*|/)".toRegex()  // Tied to enum Operator
private val identRegex = "^([a-zA-Z]+)".toRegex()

private val vars = mutableMapOf<String, Int>()

class Calculator {

    // See also: checkOperator() and applyOperator()
    enum class Operator(val symbol: String) {
        PLUS("+"),
        MINUS("-"),
        MULTIPLY("*"),
        DIVIDE("/"),
        UNKNOWN("")
    }

    private val allOperators = Operator.values().joinToString("") { it.symbol }
    private var input = ""

    fun run() {
        while (true) {
            input = readln()
            if (input.isBlank()) continue

            // Do "/" commands
            if (input.startsWith('/')) {
                when (input) {
                    "/exit" -> break
                    "/help" -> println("The program calculates the value of the expression")
                    else -> println("Unknown Command")
                }
            } else {
                try {
                    // Might be an assignment
                    if (input.contains('=')) {
                        doAssignment()
                    } else {
                        // Evaluate expression
                        println(evaluateExpression())
                    }
                // All errors from doAssignment() and evaluateExpression() (and below) bubble up to here
                } catch (e: Exception) {
                    println(e.message)
                }
            }
        }

        println("Bye!")
    }

    /**
     * Assignment in the form identifier1 = (intValue | identifier2)
     */
    private fun doAssignment() {
        // Get left-hand side of assignment
        val lhs = getIdent()

        // Get assignment operator
        input = input.trimStart()
        // if (input.isEmpty()) ...?
        if (input.first() != '=') throw Exception(INVALID_IDENTIFIER)
        input = input.drop(1).trimStart()
        if (input.isEmpty()) throw Exception(INVALID_ASSIGNMENT)

        // Get right-hand side of assignment
        // TODO, rhs should really be able to handle a full expression
        val rhs: Int
        val matchResult = intRegex.find(input)

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
            input = input.replace(intRegex, "")
            rhs = matchResult.value.toInt()
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
            input.trimEnd().first() == '=' || // an equals sign
            allOperators.contains(input.trimEnd().first()) // or any valid operator
        ) {
            return matchResult.value
        }

        throw Exception(INVALID_IDENTIFIER)
    }

    /**
     * @return the integer amount for this variable, or throws error
     */
    private fun getVariable(ident: String): Int {
        return if (vars.contains(ident)) {
            vars[ident]!!
        } else {
            throw Exception(UNKNOWN_VARIABLE)
        }
    }

    /**
     * Expression is in the form (intValue | variable) [operator (intValue | variable) [...]]
     * @return the result of evaluating the expression
     */
    private fun evaluateExpression(): Int {
        // First applyOperator() will add zero, that is, do nothing
        var result = 0
        var operator = Operator.PLUS

        while (true) {
            val number = getInteger()
            result = applyOperator(operator, number, result)

            if (input.isBlank()) break

            operator = getOperator()
            if (operator == Operator.UNKNOWN) throw Exception(INVALID_EXPRESSION)
        }

        return result
    }

    /**
     * Get an integer or a variable value from the input string.
     */
    private fun getInteger(): Int {
        input = input.trimStart()
        if (input.isEmpty()) throw Exception(INVALID_EXPRESSION)
        val matchResults = intRegex.find(input)
        val result: Int

        // If you don't get an integer value from the regex, try a variable
        if (matchResults == null) {
            result = getVariable(getIdent())
            input = input.replace(identRegex, "")
        } else {
            result = matchResults.value.toInt()
            input = input.replace(intRegex, "")
        }

        return result
    }

    /**
     * Get a valid operator
     */
    private fun getOperator(): Operator {
        input = input.trimStart()
        val matchResults = opRegex.find(input) ?: return Operator.UNKNOWN
        val result = matchResults.value
        input = input.replace(opRegex, "")

        return checkOperator(result)
    }

    /**
     * Return an operator, possibly UNKNOWN.  Takes care or multiple '+' or '-'.
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
            else -> Operator.UNKNOWN
        }
    }

    /**
     * Apply this operator and number to the result
     */
    private fun applyOperator(operator: Operator, number: Int, result: Int): Int {
        return when (operator) {
            Operator.PLUS -> result + number
            Operator.MINUS -> result - number
            Operator.MULTIPLY -> result * number
            Operator.DIVIDE -> result / number
            else -> result
        }
    }
}

fun main() = Calculator().run()
