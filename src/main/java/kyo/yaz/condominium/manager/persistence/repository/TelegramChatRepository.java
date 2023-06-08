package kyo.yaz.condominium.manager.persistence.repository;

import kyo.yaz.condominium.manager.persistence.entity.TelegramChat;
import kyo.yaz.condominium.manager.persistence.entity.TelegramChat.TelegramChatId;
import kyo.yaz.condominium.manager.persistence.repository.base.ReactiveRepository;
import kyo.yaz.condominium.manager.persistence.repository.custom.TelegramChatCustomRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface TelegramChatRepository extends ReactiveMongoRepository<TelegramChat, TelegramChatId>, ReactiveRepository<TelegramChat>
    , TelegramChatCustomRepository {

}
