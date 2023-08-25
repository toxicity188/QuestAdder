import kor.toxicity.questadder.util.builder.FunctionBuilder
import org.junit.jupiter.api.Test

class FunctionTest {
    @Test
    fun testFunction() {
        try {
            println(Number::class.java.isAssignableFrom(FunctionBuilder.evaluate("true").getReturnType()))
        } catch (ex: Exception) {
            ex.printStackTrace()
            println("test failure!")
        }
    }
}