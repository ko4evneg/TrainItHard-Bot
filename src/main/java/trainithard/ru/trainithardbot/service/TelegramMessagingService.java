package trainithard.ru.trainithardbot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TelegramMessagingService {
    private static final int QUEUE_SIZE = 100;
    private final AtomicInteger incomingUpdatesCount = new AtomicInteger(0);
    private final AtomicInteger outgoingMessagesCount = new AtomicInteger(0);
    private final BlockingQueue<Update> incomingUpdates = new ArrayBlockingQueue<>(QUEUE_SIZE);
    private final BlockingQueue<BotApiMethod<?>> outgoingMessages = new ArrayBlockingQueue<>(QUEUE_SIZE);

    public synchronized void addUpdate(Update update) {
        incomingUpdates.add(update);
        incomingUpdatesCount.getAndIncrement();
    }

    public synchronized void addOutMessage(BotApiMethod<?> message) {
        outgoingMessages.add(message);
        outgoingMessagesCount.getAndIncrement();
    }

    public synchronized Optional<Update> getUpdate() {
        if (incomingUpdatesCount.get() > 0) {
            incomingUpdatesCount.getAndDecrement();
            return Optional.of(incomingUpdates.remove());
        }
        return Optional.empty();
    }

    public synchronized Optional<Object> getOutMessage() {
        if (outgoingMessagesCount.get() > 0) {
            outgoingMessagesCount.getAndDecrement();
            return Optional.of(outgoingMessages.remove());
        }
        return Optional.empty();
    }
}
