import com.undertone.resource.ResourceResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import ramp.lift.model.SupplyProtos;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;


@Slf4j
public class SupplyCacheTest {

  @Test
  public void supplyCacheTest() throws IOException, InterruptedException {

    ResourceResponse resourceResponse = cacheTest("/cache/supply");

    Assert.assertFalse(resourceResponse.getContent().isEmpty());

    final SupplyProtos.Supply supply = SupplyProtos.Supply.parseFrom(resourceResponse.getContent());

    Assert.assertFalse(supply.getPublishersList().isEmpty());

    ResourceResponse resourceResponse2 = cacheTest("/cache/supply/" + resourceResponse.getHash());

    Assert.assertTrue(resourceResponse2.getContent().isEmpty());
  }

  @Test
  public void commonCacheTest() throws IOException, InterruptedException {

    ResourceResponse resourceResponse = cacheTest("/cache/common");
    Assert.assertFalse(resourceResponse.getContent().isEmpty());
  }

  private ResourceResponse cacheTest(String uriExt) throws IOException, InterruptedException {

    final HttpClient client = HttpClient.newHttpClient();

    Instant start = Instant.now();

    final String serverIp = getServerIp();

    final URI uri = URI.create(serverIp + uriExt);

    log.info("uri = " + uri);

    final HttpRequest.Builder builder = HttpRequest.newBuilder()
      .uri(uri)
      .header("Content-Type", "application/x-protobuf")
      .header("Accept", "application/x-protobuf");

    Instant end = Instant.now();
    log.info("Duration of {} is {} millis", uriExt, Duration.between(start, end).toMillis());

    final HttpRequest request = builder.GET().build();

    final HttpResponse<byte[]> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

    Assert.assertEquals(HttpStatus.SC_OK, httpResponse.statusCode());

    return ResourceResponse.parseFrom(httpResponse.body());
  }

  public static String getServerIp() {
//    return "http://127.0.0.1:8080";
    return System.getenv("SERVER_URL");
  }
}
