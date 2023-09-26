import kor.toxicity.questadder.util.builder.FunctionBuilder

class FunctionTest {
    fun testFunction() {
        try {
            println(FunctionBuilder.evaluate("true == false"))
        } catch (ex: Exception) {
            ex.printStackTrace()
            println("test failure!")
        }
    }
}