package calculator

import java.util.Scanner

class Calculator {
    fun run() {
        while (true) {
            val input = readln()

            when (input) {
                "" -> continue
                "/exit" -> break
                "/help" -> {
                    println("The program calculates the value of the expression")
                    continue
                }
                else -> println("Unknown Command")
            }

            println(evaluateExpression(input))
        }

        println("Bye!")
    }

    private fun evaluateExpression(input: String): Int {
        val scanner = Scanner(input)
        var result = 0
        var operator = "+"

        while (scanner.hasNextInt()) {
            val number = scanner.nextInt()
            result = parseOperator(operator, number, result)

            if (scanner.hasNext()) {
                operator = scanner.next()
            }
        }

        return result
    }

    private fun parseOperator(operator: String, number: Int, result: Int): Int {
        when (reduceOperator(operator)) {
            "+" -> return result + number
            "-" -> return result - number
        }

        return result
    }

    private fun reduceOperator(operator: String): String{
        if (operator.matches("""(\+)(\1)*""".toRegex())) {
            return "+"
        } else if (operator.matches("""(-)(\1)*""".toRegex())) {
            return if (operator.length % 2 == 0) "+" else "-"
        }

        return "?"
    }
}

fun main() = Calculator().run()
