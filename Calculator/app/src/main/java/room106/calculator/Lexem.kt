package room106.calculator

class Lexem(val value: String?, val type: Type) {
    enum class Type {
        NUMBER, OPERATOR, BRACKET
    }

    constructor() : this(null, Type.NUMBER)
}