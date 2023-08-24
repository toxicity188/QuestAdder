import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class SpigotTest {
    @Test
    fun testSpigot() {
        val version = HttpClient.newHttpClient().send(HttpRequest.newBuilder()
            .uri(URI.create("https://api.spigotmc.org/legacy/update.php?resource=112227"))
            .GET()
            .build(),HttpResponse.BodyHandlers.ofString()).body()
        println(version)
    }
}