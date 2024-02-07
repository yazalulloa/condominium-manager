package kyo.yaz.condominium.manager.core.mapper;

import kyo.yaz.condominium.manager.persistence.entity.EmailConfig;
import kyo.yaz.condominium.manager.ui.views.email_config.EmailConfigViewItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EmailConfigMapper {

  EmailConfigMapper MAPPER = Mappers.getMapper(EmailConfigMapper.class);

  static EmailConfig to(EmailConfigViewItem item) {
    return MAPPER.map(item);
  }

  static EmailConfigViewItem to(EmailConfig EmailConfig) {
    return MAPPER.map(EmailConfig);
  }

  EmailConfig map(EmailConfigViewItem item);

  default EmailConfigViewItem map(EmailConfig config) {

    return new EmailConfigViewItem(config.id(), config.from(), config.config(), config.storedCredential(),
        config.active(), config.isAvailable(),
        config.error(), config.createdAt(), config.updatedAt(), config.lastCheckAt());
  }
}
