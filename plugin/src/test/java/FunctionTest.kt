import kor.toxicity.questadder.util.builder.FunctionBuilder
import org.junit.jupiter.api.Test

class FunctionTest {
    @Test
    fun testFunction() {
        try {
            println(FunctionBuilder.evaluate("and(plus(1,2) <= 3, 2 < 5)").apply(Any()))
        } catch (ex: Exception) {
            ex.printStackTrace()
            println("test failure!")
        }
    }
}