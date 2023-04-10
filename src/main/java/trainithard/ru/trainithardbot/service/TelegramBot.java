package trainithard.ru.trainithardbot.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import trainithard.ru.trainithardbot.exception.TelegramBotException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Service
public class TelegramBot extends TelegramLongPollingBot {
    private static final int QUEUE_SIZE = 500;
    private final String username;
    private final BlockingQueue<Update> incomingUpdates;
    private final BlockingQueue<BotApiMethod<?>> outgoingMessages;

    public TelegramBot(@Value("${telegram.token}") String token, @Value("${telegram.username}") String username) {
        super(token);
        this.username = username;
        this.incomingUpdates = new ArrayBlockingQueue<>(QUEUE_SIZE);
        this.outgoingMessages = new ArrayBlockingQueue<>(QUEUE_SIZE);
    }

    @PostConstruct
    void init() throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            incomingUpdates.put(update);
        } catch (InterruptedException e) {
            throw new TelegramBotException("Unexpected interruption on incoming update.", e);
        }
    }

    public boolean hasUpdates() {
        return !incomingUpdates.isEmpty();
    }

    public Update getUpdate() {
        return incomingUpdates.poll();
    }

    public void sendMessage(BotApiMethod<?> message) {
        try {
            outgoingMessages.put(message);
        } catch (InterruptedException e) {
            throw new TelegramBotException("Unexpected interruption on outgoing message.", e);
        }
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Scheduled(fixedDelay = 200)
    private void sendMessages() {
        while (!outgoingMessages.isEmpty()) {
            try {
                BotApiMethod<?> poll = outgoingMessages.poll();
                execute(poll);
            } catch (TelegramApiException e) {
                throw new TelegramBotException("Can't send the outgoing queue message.", e);
            }
        }
    }
}
