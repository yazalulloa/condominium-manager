import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class VertxHttpClienttest {

    @Test
    void test(Vertx vertx, VertxTestContext testContext) {

        final var httpClientOptions = new HttpClientOptions()
                .setTrustAll(true)
                .setVerifyHost(false)
                ;

        final var client = vertx.createHttpClient(httpClientOptions);

        final var requestOptions = new RequestOptions()
                .setMethod(HttpMethod.GET)
                .setAbsoluteURI("https://www.bcv.org.ve");

        client.request(requestOptions)
                .flatMap(request -> {

                    final var connection = request.connection();
                    return request.send()
                            .onSuccess(v -> {
                                System.out.println(v.statusCode());
                                System.out.println(connection.remoteAddress().hostAddress());
                            })
                            .flatMap(HttpClientResponse::body)
                            .map(buffer -> {
                                System.out.println("SIZE " + buffer.toString().length());
                                return buffer;
                            })
                            .flatMap(b -> request.send())
                            .onSuccess(v -> {
                                System.out.println(v.statusCode());
                                System.out.println(connection.remoteAddress().hostAddress());
                            })
                            .flatMap(r -> connection.close())
                            ;
                })
                .onComplete(testContext.succeedingThenComplete());
    }
}
