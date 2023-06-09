import io.vertx.ext.web.multipart.MultipartForm;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

public class MultipartFormTest {

  @Test
  void test() {
    final var multipartForm = MultipartForm.create();

    Consumer<MultipartForm> consumer = m -> {
      m.forEach(formDataPart -> System.out.println(formDataPart.name()));
    };

    multipartForm.attribute("test", "test");
    consumer.accept(multipartForm);

    multipartForm.attribute("test1", "test1");
    multipartForm.attribute("test2", "test2");
    multipartForm.attribute("test3", "test3");

    consumer.accept(multipartForm);

    multipartForm.attribute("test4", "test4");
    consumer.accept(multipartForm);
  }

}
