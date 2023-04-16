package trainithard.ru.trainithardbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class TelegramMessagingServiceTest {
    private final TelegramMessagingService messagingService = new TelegramMessagingService();
    private final AtomicInteger addedTasks = new AtomicInteger(0);
    private final AtomicInteger removedTasks = new AtomicInteger(0);

    @BeforeEach
    void beforeEach() {
        addedTasks.set(0);
        removedTasks.set(0);
    }

    @Test
    void putAndGetUpdatesSimpleLoadTest() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        Field queueSizeField = TelegramMessagingService.class.getDeclaredField("QUEUE_SIZE");
        queueSizeField.setAccessible(true);
        int queue_size = (int) queueSizeField.get(messagingService);

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(4);
        executor.scheduleAtFixedRate(this::addCountableUpdate, 10, 1, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::addCountableUpdate, 10, 1, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::getCountableUpdate, 10, 5, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::getCountableUpdate, 10, 5, TimeUnit.MILLISECONDS);
        TimeUnit.SECONDS.sleep(2);
        executor.shutdownNow();

        assertTrue(addedTasks.get() > queue_size);
        assertTrue(removedTasks.get() > queue_size);
        assertTrue(addedTasks.get() - removedTasks.get() < queue_size);
    }

    private void getCountableUpdate() {
        messagingService.getUpdate();
        removedTasks.getAndIncrement();
    }

    private void addCountableUpdate() {
        messagingService.addUpdate(new Update());
        addedTasks.getAndIncrement();
    }

    @Test
    void shouldNotGetUpdatesOfUsersSendingMessages() {
        fail();
    }

    @Test
    void shouldNotSendMessagesOfUsersProcessingUpdates() {
        fail();
    }
}
