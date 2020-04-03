package room106.calculator

class CalculatorSyntaxException(private val errorCode: Int) : Exception() {

    companion object {
        const val EMPTY_INPUT = 10001
        const val BAD_SYNTAX = 10002
        const val BAD_EXPRESSION = 10003
    }

    override fun toString(): String {
        when (errorCode) {
            EMPTY_INPUT -> return "Empty string"
            BAD_SYNTAX -> return "Bad syntax"
            BAD_EXPRESSION -> return "?"
        }
        return "Bad syntax"
    }
}