package room106.calculator

class Calculator {

    private val DELIMITER = '.'
    private val MULTIPLY_CHARACTER = "\u00D7"
    private val DIVISION_CHARACTER = "\u00F7"
    private val MINUS_CHARACTER = "\u002D"
    private val PLUS_CHARACTER = "\u002B"

    // String of expression
    private var source: String? = null

    // Result of calculated part of expression
    private var result = 0.0

    // Current number
    private var num = 0.0

    // Current operator
    private var operator: Lexem? = null

    /**
     * This method use Recursive descent parser.
     *
     * @see [Wiki](https://en.wikipedia.org/wiki/Recursive_descent_parser)
     *
     * @see [Description](http://www.cs.fsu.edu/~lacher/courses/COP4020/fall14/assigns/proj2a.html)
     *
     *
     * @param source string of expression like: "2*(12/5.5)"
     * @return result of give expression
     *
     * @throws CalculatorSyntaxException exception with any kind of error like:
     * bad syntax of expression, empty given string, caught ArithmeticException etc.
     */
    @Throws(CalculatorSyntaxException::class)
    fun calculate(source: String): Double {
        if (source.isEmpty()) {
            throw CalculatorSyntaxException(CalculatorSyntaxException.EMPTY_INPUT)
        }
        reset()
        this.source = source

        // Start a descent by levels
        level2()
        return result
    }

    /**
     * The highest level of Recursive descent parser.
     * This method do calculation with lowest priority operations like: + and -.
     *
     * @throws CalculatorSyntaxException in the case of an error during calculation
     */
    @Throws(CalculatorSyntaxException::class)
    private fun level2() {

        // Go to the lower level (Finding for operation with higher priority operation)
        level3()
        var tmpResult = if (num != -1.0) num else 0.0
        var currentOperator: Lexem?

        // Do calculation with appropriate operators (+ and -)
        while (operator?.value.equals(PLUS_CHARACTER) || operator?.value.equals(MINUS_CHARACTER)) {
            currentOperator = operator
            level3()
            try {
                when (currentOperator?.value) {
                    PLUS_CHARACTER -> tmpResult += num
                    MINUS_CHARACTER -> tmpResult -= num
                    else -> tmpResult = 0.0
                }
            } catch (e: NumberFormatException) {
                throw CalculatorSyntaxException(CalculatorSyntaxException.BAD_SYNTAX)
            }
        }
        result = tmpResult
    }

    /**
     * The lower level of Recursive descent parser.<br></br>
     * This method do calculation with higher priority operations like: * and /.
     *
     * @throws CalculatorSyntaxException in the case of an error during calculation
     */
    @Throws(CalculatorSyntaxException::class)
    private fun level3() {

        // Go to the lower level (Finding for operation with higher priority operation)
        level4()
        var tmpResult: Double = if (num != -1.0) num else 0.0
        var currentOperator: Lexem?

        // Do calculation with appropriate operators (* and /)
        while (operator?.value.equals(MULTIPLY_CHARACTER) || operator?.value.equals(DIVISION_CHARACTER)) {
            currentOperator = operator
            level4()
            try {
                when (currentOperator?.value) {
                    MULTIPLY_CHARACTER -> tmpResult *= num
                    DIVISION_CHARACTER -> tmpResult /= num
                    else -> tmpResult = 0.0
                }
            } catch (e: NumberFormatException) {
                throw CalculatorSyntaxException(CalculatorSyntaxException.BAD_SYNTAX)
            }
            if (java.lang.Double.isInfinite(tmpResult) || java.lang.Double.isNaN(tmpResult)) {
                throw CalculatorSyntaxException(CalculatorSyntaxException.BAD_EXPRESSION)
            }
        }
        if (tmpResult != 0.0) {
            num = tmpResult
        }
    }

    /**
     * The lower level of Recursive descent parser.<br></br>
     * This method do calculation with higher priority operations like: %
     *
     * @throws CalculatorSyntaxException in the case of an error during calculation
     */
    @Throws(CalculatorSyntaxException::class)
    private fun level4() {

        // Go to the lowest level (To read a number or an operator)
        level5()
        var tmpResult: Double = if (num != -1.0) num else 0.0
        var currentOperator: Lexem?

        // Do calculation with appropriate operator - %
        while (operator?.value.equals("%")) {
            currentOperator = operator
            level5()
            tmpResult = try {
                when (currentOperator?.value) {
                    "%" -> tmpResult * num / 100
                    else -> 0.0
                }
            } catch (e: NumberFormatException) {
                throw CalculatorSyntaxException(CalculatorSyntaxException.BAD_SYNTAX)
            }
        }
        if (tmpResult != 0.0) {
            num = tmpResult
        }
    }

    /**
     * The lowest level of Recursive descent parser. <br></br>
     * This method does not do any calculation,
     * but it's reading any kind of Lexem (numbers or operators)
     * and then assign it into appropriate variables. <br></br><br></br>
     *
     * Also, in case of '(' operator, it calls [.level_2] method
     * to do calculation in brackets.
     * @throws CalculatorSyntaxException in the case of an error during calculation
     */
    @Throws(CalculatorSyntaxException::class)
    private fun level5() {
        if (source!!.isEmpty()) {
            return
        }
        val currentLexem: Lexem = nextLexem()
        when {
            currentLexem.value.equals(MINUS_CHARACTER) -> {
                level5()
                num = -num
            }
            currentLexem.type === Lexem.Type.NUMBER -> {
                num = parse(currentLexem)
                operator = nextOperator()
            }
            currentLexem.value.equals("(") -> {
                level2()
                operator = if (operator?.value.equals(")")) {
                    nextOperator()
                } else {
                    throw CalculatorSyntaxException(CalculatorSyntaxException.BAD_SYNTAX)
                }
                num = result
                result = 0.0
            }
            else -> {
                throw CalculatorSyntaxException(CalculatorSyntaxException.BAD_SYNTAX)
            }
        }
    }

    /**
     * Convert Lexem to double
     * @param lexem required instance of Lexem class
     * @return number of double type
     */
    private fun parse(lexem: Lexem): Double {
        return if (lexem.type === Lexem.Type.NUMBER) lexem.value!!.toDouble() else 0.0
    }

    /**
     * Read the next operator
     * @return instance of Lexem class
     *
     * @throws CalculatorSyntaxException in the case of an error during calculation
     */
    @Throws(CalculatorSyntaxException::class)
    private fun nextOperator(): Lexem? {
        return if (source!!.isNotEmpty()) {
            nextLexem()
        } else Lexem()
    }

    /**
     * Read the next Lexem
     * @return instance of Lexem class
     * @throws CalculatorSyntaxException in the case of an error during calculation
     */
    @Throws(CalculatorSyntaxException::class)
    private fun nextLexem(): Lexem {

        // Read number
        if (Character.isDigit(source!![0])) {
            var foundDelimiter = false
            val num = StringBuilder()
            while (Character.isDigit(source!![0]) ||
                source!![0] == DELIMITER
            ) {
                if (source!![0] == DELIMITER) {
                    // Check if there are not two delimiters
                    foundDelimiter = if (foundDelimiter) {
                        throw CalculatorSyntaxException(CalculatorSyntaxException.BAD_SYNTAX)
                    } else {
                        true
                    }
                }
                num.append(source!![0])
                popElement()
                if (source!!.isEmpty()) {
                    break
                }
            }
            return Lexem(num.toString(), Lexem.Type.NUMBER)
        }

        // Read operators
        if (("$PLUS_CHARACTER$MINUS_CHARACTER$MULTIPLY_CHARACTER$DIVISION_CHARACTER").contains(source!![0].toString())) {
            val operator = source!![0].toString()
            popElement()

            // Check if it was a last character (Last character is an operator = Bad syntax)
            if (source!!.isEmpty()) {
                throw CalculatorSyntaxException(CalculatorSyntaxException.BAD_SYNTAX)
            }
            return Lexem(operator, Lexem.Type.OPERATOR)
        }

        // Read brackets
        if ("()".contains(source!![0].toString())) {
            val operator = source!![0].toString()
            popElement()

            // Check if it was a last character (Last character is an bracket = Bad syntax)
            if (source!!.isEmpty() && operator == "(") {
                throw CalculatorSyntaxException(CalculatorSyntaxException.BAD_SYNTAX)
            }
            return Lexem(operator, Lexem.Type.BRACKET)
        }
        return Lexem()
    }

    /**
     * Remove the last symbol from source
     */
    private fun popElement() {
        source = source!!.substring(1)
    }

    /**
     * Reset all variables
     */
    private fun reset() {
        source = ""
        result = 0.0
        num = -1.0
        operator = Lexem()
    }
}