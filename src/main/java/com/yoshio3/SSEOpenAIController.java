package com.yoshio3;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.ChatMessageDelta;
import com.azure.ai.openai.models.ChatRole;
import com.azure.core.credential.AzureKeyCredential;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;

@Controller
@Component
public class SSEOpenAIController {

    private final Logger LOGGER = LoggerFactory.getLogger(SSEOpenAIController.class);

    // Azure OpenAI Instance URL
    @Value("${azure.openai.url}")
    private String OPENAI_URL;

    // Name of Model
    @Value("${azure.openai.model.name")
    private String MODEL_NAME;

    // Azure OpenAI API Key
    @Value("${azure.openai.api.key}")
    private String OPENAI_API_KEY;

    // A Map to hold information about connected clients
    private static Map<UUID, Sinks.Many<String>> userSinks;

    // Static Initializer
    static {
        userSinks = new ConcurrentHashMap<>();
    }

    private OpenAIAsyncClient client;

    @PostConstruct
    public void init() {
        client = new OpenAIClientBuilder()
                .endpoint(OPENAI_URL)
                .credential(new AzureKeyCredential(OPENAI_API_KEY))
                .buildAsyncClient();
    }

    // When accessing the page, create a UUID for each client (for 1-on-1 sending
    // and receiving)
    // This part of the process is unnecessary if you want to update the same
    // content (1-to-many) like a chat
    @GetMapping(path = "/openai-gpt4-sse-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<String> sseStream(@RequestParam UUID userId) {
        Sinks.Many<String> userSink = getUserSink(userId);
        if (userSink == null) {
            userSink = createUserSink(userId);
        }
        LOGGER.debug("USER ID IS ADDED: {}}", userId);
        return userSink.asFlux().delayElements(Duration.ofMillis(10));
    }

    // クライアントからメッセージを受信し、OpenAI に送信して、
    // 結果をクライアントに Server Sent Event で送信する
    @PostMapping("/openai-gpt4-sse-submit")
    @ResponseBody
    public void openaiGpt4Sse(@RequestBody String inputText, @RequestParam UUID userId) {
        LOGGER.debug(inputText);
        List<ChatMessage> chatMessages = createMessages(inputText);
        client.getChatCompletionsStream("gpt-4", new ChatCompletionsOptions(chatMessages))
                .subscribe(chatCompletions -> {
                    chatCompletions.getChoices().stream()
                            .map(ChatChoice::getDelta)
                            .map(ChatMessageDelta::getContent)
                            .filter(content -> content != null)
                            .forEach(content -> {
                                LOGGER.debug(content);
                                // 空白文字対応 (SSE で空白文字が送信されないため)
                                if (content.contains(" ")) {
                                    content = content.replace(" ", "<SPECIAL_WHITE_SPACE>");
                                }
                                // SSE でクライアントのブラウザにデータを送信
                                Sinks.Many<String> userSink = getUserSink(userId);
                                EmitResult result = userSink.tryEmitNext(content);
                                // エラーが発生した場合は、エラー内容を表示
                                showDetailErrorReasonForSSE(result, content, inputText);
                                try {
                                    TimeUnit.MILLISECONDS.sleep(20);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            });
                });
    }

    // Return index.html
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // Create Sinks for accessed User
    private Sinks.Many<String> createUserSink(UUID userId) {
        Sinks.Many<String> userSink = Sinks.many().multicast().directBestEffort();
        userSinks.put(userId, userSink);
        return userSink;
    }

    // Get User Sinks
    private Sinks.Many<String> getUserSink(UUID userId) {
        return userSinks.get(userId);
    }

    /**
     * Crete ChatMessage list
     */
    private List<ChatMessage> createMessages(String userInput) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage(ChatRole.SYSTEM)
                .setContent(SYSTEM_DEFINITION));
        chatMessages.add(new ChatMessage(ChatRole.USER).setContent(USER1));
        chatMessages.add(new ChatMessage(ChatRole.ASSISTANT).setContent(ASSISTANT1));
        chatMessages.add(new ChatMessage(ChatRole.USER).setContent(USER2));
        chatMessages.add(new ChatMessage(ChatRole.ASSISTANT).setContent(ASSISTANT2));
        chatMessages.add(new ChatMessage(ChatRole.USER).setContent(userInput));
        return chatMessages;
    }

    // Show Error Message when SSE failed to send the message
    private void showDetailErrorReasonForSSE(EmitResult result, String returnValue, String data) {
        if (result.isFailure()) {
            LOGGER.error("Failure: {}", returnValue + " " + data);
            if (result == EmitResult.FAIL_OVERFLOW) {
                LOGGER.error("Overflow: {}", returnValue + " " + data);
            } else if (result == EmitResult.FAIL_NON_SERIALIZED) {
                LOGGER.error("Non-serialized: {}", returnValue + " " + data);
            } else if (result == EmitResult.FAIL_ZERO_SUBSCRIBER) {
                LOGGER.error("Zero subscriber: {}", returnValue + " " + data);
            } else if (result == EmitResult.FAIL_TERMINATED) {
                LOGGER.error("Terminated: {}", returnValue + " " + data);
            } else if (result == EmitResult.FAIL_CANCELLED) {
                LOGGER.error("Cancelled: {}", returnValue + " " + data);
            }
        }
    }

    private final static String SYSTEM_DEFINITION = """
            私は、ACME Fitness というオンライン・電子商取 Web サイトのサポート対応担当者です。
            オンライン、電子商取引で扱う商品以外の問い合わせには、お答えできません。
            """;

    private final static String USER1 = "こちらで購入した、スマートウォッチ(注文番号 : 12345)がまだ届きません。現在どのような状況か教えてください。";

    private final static String ASSISTANT1 = "状況を確認しますので、しばらくお待ちください";

    private final static String USER2 = "今日は何日ですか？";

    private final static String ASSISTANT2 = """
                申し訳ございませんが、私は ACME Fitness のオンラインサポート担当であり、
                お問い合わせ内容に関する情報は提供できません。
                お客様の購入商品やサービス、フィードバックに関するお問い合わせには喜んでお答えいたしますので、
                どうぞお気軽にお問い合わせください。
                どうぞよろしくお願いいたします。
            """;
}
