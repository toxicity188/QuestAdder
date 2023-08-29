import kor.toxicity.questadder.util.builder.FunctionBuilder
import org.junit.jupiter.api.Test

class FunctionTest {
    @Test
    fun testFunction() {
        try {
            println(FunctionBuilder.evaluate("true == false"))
        } catch (ex: Exception) {
            ex.printStackTrace()
            println("test failure!")
        }
    }
}