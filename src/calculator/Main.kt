package calculator

private const val INVALID_EXPRESSION = "Invalid expression"
private const val INVALID_IDENTIFIER = "Invalid identifier"
private const val INVALID_ASSIGNMENT = "Invalid assignment"
private const val UNKNOWN_VARIABLE = "Unknown variable"

private val intRegex = "^([+-]?\\d+)".toRegex()
private val opRegex = "^(\\++|-+|\\*|/)".toRegex()
private val identRegex = "^([a-zA-Z]+)".toRegex()

private val vars = emptyMap<String, Int>().toMutableMap()

class Calculator {
    data class IntRecord(val value: Int = 0, val isError: Boolean = false, val errorMessage: String = "")
    data class StringRecord(val value: String = "", val isError: Boolean = false, val errorMessage: String = "")

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
                // TODO, use throw exception instead of intRecord and stringRecord
                // Might be an assignment
                if (input.contains('=')) {
                    doAssignment()
                } else {
                    println(evaluateExpression())
                }
            }
        }

        println("Bye!")
    }

    private fun doAssignment() {
        // TODO, use split \\s*=\\s ?
        // Get left-hand side of assignment
        val stringRecord = getIdent()
        if (stringRecord.isError) {
            println(stringRecord.errorMessage)
            return
        }
        val lhs = stringRecord.value

        // Get assignment operator
        removeWhitespace()
        // if (input.isEmpty()) ...?
        if (input[0] != '=') {
            println(INVALID_IDENTIFIER)
            return
        }
        input = input.substring(1)

        removeWhitespace()
        if (input.isEmpty()) {
            println(INVALID_ASSIGNMENT)
            return
        }

        // Get right-hand side of assignment
        val rhs: Int
        val matchResult = intRegex.find(input)
        if (matchResult == null) {
            val strRecord = getIdent()
            if (strRecord.isError) {
                println(INVALID_ASSIGNMENT)
                return
            }
            val ident = strRecord.value

            val intRecord = getVariable(ident)
            if (intRecord.isError) {
                println(intRecord.errorMessage)
                return
            }
            rhs = intRecord.value
        } else {
            input = input.replace(intRegex, "")
            rhs = matchResult.value.toInt()
        }

        if (input.isBlank()) {
            vars[lhs] = rhs
        } else {
            println(INVALID_ASSIGNMENT)
        }
    }

    private fun getIdent(): StringRecord {
        val matchResult =
            identRegex.find(input) ?: return StringRecord(isError = true, errorMessage = INVALID_IDENTIFIER)
        input = input.replace(identRegex, "")

        if (input.isBlank() ||
            input[0].isWhitespace() ||
            input.trimEnd()[0] == '=' ||
            allOperators.contains(input.trimEnd()[0])
        ) {
            return StringRecord(matchResult.value)
        }

        return StringRecord(isError = true, errorMessage = INVALID_IDENTIFIER)
    }

    private fun getVariable(ident: String): IntRecord {
        return if (vars.contains(ident)) {
            IntRecord(vars[ident]!!)
        } else {
            IntRecord(isError = true, errorMessage = UNKNOWN_VARIABLE)
        }
    }

    private fun evaluateExpression(): String {
        var result = 0
        var operator = Operator.PLUS

        while (true) {
            val record = getInteger()
            if (record.isError) return record.errorMessage
            result = applyOperator(operator, record.value, result)

            if (input.isBlank()) break

            operator = getOperator()
            if (operator == Operator.UNKNOWN) return INVALID_EXPRESSION
        }

        return result.toString()
    }

    /**
     * Get an integer or a variable value from the input string.
     * @return IntRecord with the value set, or isError set to true with an error message
     */
    private fun getInteger(): IntRecord {
        removeWhitespace()
        if (input.isEmpty()) return IntRecord(isError = true, errorMessage = INVALID_EXPRESSION)
        val matchResults = intRegex.find(input)

        if (matchResults == null) {
            val rec = getIdent()
            if (rec.isError) return IntRecord(isError = true, errorMessage = INVALID_IDENTIFIER)
            val record = getVariable(rec.value)
            if (!record.isError) input = input.replace(identRegex, "")
            return record
        } else {
            val result = matchResults.value.toInt()
            input = input.replace(intRegex, "")
            return IntRecord(result)
        }
    }

    private fun removeWhitespace() {
        input = input.replace("^\\s*".toRegex(), "")
    }

    private fun getOperator(): Operator {
        removeWhitespace()
        val matchResults = opRegex.find(input) ?: return Operator.UNKNOWN
        val result = matchResults.value
        input = input.replace(opRegex, "")

        return checkOperator(result)
    }

    private fun checkOperator(operator: String): Operator {
        if (operator.matches("""(\+)(\1)*""".toRegex())) {
            return Operator.PLUS
        } else if (operator.matches("""(-)(\1)*""".toRegex())) {
            return if (operator.length % 2 == 0) Operator.PLUS else Operator.MINUS
        }

        if (operator.length != 1) return Operator.UNKNOWN

        return when (operator) {
            "*" -> Operator.MULTIPLY
            "/" -> Operator.DIVIDE
            else -> Operator.UNKNOWN
        }
    }

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
