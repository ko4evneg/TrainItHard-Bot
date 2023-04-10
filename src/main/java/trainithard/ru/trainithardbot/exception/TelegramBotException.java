package trainithard.ru.trainithardbot.exception;

public class TelegramBotException extends RuntimeException {
    public TelegramBotException(String message, Throwable cause) {
        super(message, cause);
    }
}
