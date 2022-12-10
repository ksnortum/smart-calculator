package calculator

private const val INVALID_MESSAGE = "Invalid expression"
private val intRegex = "^([+-]?\\d+)".toRegex()
private val opRegex = "^(\\++|-+|\\*|/)".toRegex()

class Calculator {
    data class Record(val value: Int, val isError: Boolean = false, val errorMessage: String = "")

    enum class Operator {
        PLUS,
        MINUS,
        MULTIPLY,
        DIVIDE,
        UNKNOWN
    }

    private var input = ""

    fun run() {
        while (true) {
            input = readln()
            if (input.isBlank()) continue

            if (input.startsWith('/')) {
                when (input) {
                    "/exit" -> break
                    "/help" -> println("The program calculates the value of the expression")
                    else -> println("Unknown Command")
                }
            } else {
                println(evaluateExpression())
            }
        }

        println("Bye!")
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
            if (operator == Operator.UNKNOWN) return INVALID_MESSAGE
        }

        return result.toString()
    }

    private fun getInteger(): Record {
        removeWhitespace()
        if (input.isBlank()) return Record(0, isError = true, errorMessage = INVALID_MESSAGE)
        val matchResults = intRegex.find(input)

        return if (matchResults == null) {
            Record(0, isError = true, errorMessage = INVALID_MESSAGE)
        } else {
            val result = matchResults.value.toInt()
            input = input.replace(intRegex, "")
            Record(result)
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
