import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;

public class VertxTest {

  @Test
  void test() {
    final var string = Vertx.vertx().fileSystem().readFileBlocking(".gitignore").toString();
    System.out.println(string);
  }

}
