package com.struchev.invest.service.notification;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Service to send messages and errors in preconfigured channels
 * <p>
 * Telegram channel configuration
 * - telegram.bot.token
 * - telegram.bot.chat-id
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    @Value("${telegram.bot.token:}")
    private String telegramBotToken;
    @Value("${telegram.bot.chat-id:}")
    private String telegramBotChatId;

    private TelegramBot bot;

    public void sendMessage(String content) {
        if (bot != null && StringUtils.isNotEmpty(telegramBotChatId)) {
            var message = new SendMessage(telegramBotChatId, content);
            this.bot.execute(message);
        }
    }

    public void sendMessageAndLog(String content) {
        log.warn(content);
        sendMessage(content);
    }

    @PostConstruct
    private void init() {
        // отправляем сообщение в telegram в случае level ERROR в логах
        var loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        var appender = new UnsynchronizedAppenderBase() {
            @Override
            protected void append(Object eventObject) {
                if (eventObject instanceof LoggingEvent) {
                    var event = (LoggingEvent) eventObject;
                    if (event.getLevel() == Level.ERROR) {
                        sendMessage(event.toString());
                    }
                }
            }
        };
        loggerContext.getLoggerList().forEach(l -> l.addAppender(appender));
        appender.start();

        // по умолчанию на любое сообщение ответим в телеграм чат отправив chatId
        if (StringUtils.isNotEmpty(telegramBotToken)) {
            this.bot = new TelegramBot(telegramBotToken);
            this.bot.setUpdatesListener(updates -> {
                updates.stream().forEach(update -> {
                    var chatId = update.message().chat().id();
                    var messageIn = update.message().text();
                    if (messageIn != null) {
                        var messageOut = String.format("Received from chat id %s: %s", chatId, messageIn);
                        bot.execute(new SendMessage(update.message().chat().id(), messageOut));
                    }
                });
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            });
        }

        if (StringUtils.isEmpty(telegramBotToken) || StringUtils.isEmpty(telegramBotChatId)) {
            log.warn("Telegram properties no defined properly: telegram.bot.token: , telegram.bot.chat-id: {}",
                    telegramBotToken, telegramBotChatId);
        }
    }
}
