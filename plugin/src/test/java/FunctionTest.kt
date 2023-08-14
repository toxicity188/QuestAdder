import kor.toxicity.questadder.util.builder.FunctionBuilder
import org.junit.jupiter.api.Test

class FunctionTest {
    @Test
    fun testFunction() {
        try {
            println(FunctionBuilder.evaluate("plus(1.0,2.0) == 3").apply(Any()))
        } catch (ex: Exception) {
            ex.printStackTrace()
            println("test failure!")
        }
    }
}