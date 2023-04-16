package trainithard.ru.trainithardbot.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Service
public class TelegramApiService extends TelegramLongPollingBot {
    private final TelegramMessagingService telegramMessagingService;
    private final String username;

    public TelegramApiService(@Value("${telegram.token}") String token, @Value("${telegram.username}") String username,
                              TelegramMessagingService telegramMessagingService) {
        super(token);
        this.username = username;
        this.telegramMessagingService = telegramMessagingService;
    }

    @PostConstruct
    void init() throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        //TODO 17.04.2023: master validation (file size)
        telegramMessagingService.addUpdate(update);
    }

    @Override
    public String getBotUsername() {
        return username;
    }
}
