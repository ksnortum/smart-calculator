package calculator

private const val INVALID_MESSAGE = "Invalid expression"

class Calculator {
    data class Record(val value: Int, val isError: Boolean = false, val errorMessage: String = "")

    data class Expression(private val string: String) {
        var atEnd = false
        private var index = 0

        fun thisChar() = string[index]

        fun nextChar(): Boolean {
            return if (index < string.length - 1) {
                index++
                true
            } else {
                atEnd = true
                false
            }
        }
    }

    enum class Operator(val symbol: String) {
        PLUS("+"),
        MINUS("-"),
        MULTIPLY("*"),
        DIVIDE("/"),
        UNKNOWN("")
    }

    private val allOperators = Operator.values().joinToString("") { it.symbol }

    fun run() {
        while (true) {
            val input = readln()
            if (input.isBlank()) continue

            if (input.startsWith('/')) {
                when (input) {
                    "/exit" -> break
                    "/help" -> println("The program calculates the value of the expression")
                    else -> println("Unknown Command")
                }
            } else {
                println(evaluateExpression(Expression(input)))
            }
        }

        println("Bye!")
    }

    private fun evaluateExpression(expression: Expression): String {
        var result = 0
        var operator = Operator.PLUS

        while (true) {
            val record = getInteger(expression)
            if (record.isError) return record.errorMessage
            result = applyOperator(operator, record.value, result)

            if (expression.atEnd) break

            operator = getOperator(expression)
            if (operator == Operator.UNKNOWN) return INVALID_MESSAGE
        }

        return result.toString()
    }

    private fun getInteger(expression: Expression): Record {
        var number = 0
        var foundNumber = false

        removeWhitespace(expression)
        if (expression.atEnd) return Record(0, isError = true, errorMessage = INVALID_MESSAGE)
        val sign = getSign(expression)
        if (expression.atEnd) return Record(0, isError = true, errorMessage = INVALID_MESSAGE)

        while (!expression.atEnd && expression.thisChar().isDigit()) {
            number = number * 10 + expression.thisChar().toString().toInt() * sign
            expression.nextChar()
            foundNumber = true
        }

        return if (foundNumber) {
            Record(number)
        } else {
            Record(0, isError = true, errorMessage = INVALID_MESSAGE)
        }
    }

    private fun getSign(expression: Expression): Int {
        return when (expression.thisChar()) {
            '-' -> {
                expression.nextChar()
                -1
            }
            '+' -> {
                expression.nextChar()
                1
            }
            else -> 1
        }
    }

    private fun removeWhitespace(expression: Expression) {
        while (!expression.atEnd && expression.thisChar().isWhitespace()) {
            expression.nextChar()
        }
    }

    private fun getOperator(expression: Expression): Operator {
        var result = ""
        removeWhitespace(expression)

        while (!expression.atEnd && expression.thisChar() in allOperators) {
            result += expression.thisChar()
            expression.nextChar()
        }

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
