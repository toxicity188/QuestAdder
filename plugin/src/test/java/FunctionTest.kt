import kor.toxicity.questadder.util.function.FunctionBuilder
import org.junit.jupiter.api.Test

class FunctionTest {
    @Test
    fun testFunction() {
        try {
            println(FunctionBuilder.evaluate("plus(5 + 1 + 2, minus(2,3) + mod(1,2))").apply(Any()))
        } catch (ex: Exception) {
            ex.printStackTrace()
            println("test failure!")
        }
    }
}