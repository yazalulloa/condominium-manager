package kyo.yaz.condominium.manager.core.vertx.domain;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@SuperBuilder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
public class VerticleRecord {

    private final String configKey;
    private final String verticleId;
    private final String verticle;

    private final JsonObject oldConfig;

    public VerticleRecord(String configKey, String verticle) {
        this.configKey = configKey;
        this.verticle = verticle;
        this.verticleId = null;
        this.oldConfig = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final VerticleRecord that = (VerticleRecord) o;
        return Objects.equals(configKey, that.configKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configKey);
    }
}
