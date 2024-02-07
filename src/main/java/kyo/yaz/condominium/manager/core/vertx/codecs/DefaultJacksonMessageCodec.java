package kyo.yaz.condominium.manager.core.vertx.codecs;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultJacksonMessageCodec implements MessageCodec<Object, Object> {

  @Override
  public void encodeToWire(Buffer buffer, Object o) {
    try {
      log.info("encodeToWire");
      final var json = DatabindCodec.mapper().writeValueAsBytes(o);
      buffer.appendBytes(json);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Object decodeFromWire(int pos, Buffer buffer) {
    try {
      log.info("decodeFromWire");
      int _pos = pos;

      // Length of JSON
      int length = buffer.getInt(_pos);

      // Get JSON string by it`s length
      // Jump 4 because getInt() == 4 bytes
      final var json = buffer.getBytes(_pos += 4, _pos + length);
      //final var json = buffer.getString(_pos += 4, _pos + length);

      return DatabindCodec.mapper().reader().readValue(json);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String name() {
    return getClass().getCanonicalName();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }

  @Override
  public Object transform(Object o) {
    //log.info("transform");
    return o;
  }
}

