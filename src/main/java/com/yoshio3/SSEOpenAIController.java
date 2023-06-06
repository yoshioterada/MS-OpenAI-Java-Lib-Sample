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
import com.azure.ai.openai.OpenAIClient;
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
    private String MODEL_NAME ;

    // Azure OpenAI API Key
    @Value("${azure.openai.api.key}")
    private String OPENAI_API_KEY;

    // A Map to hold information about connected clients
    private static Map<UUID, Sinks.Many<String>> userSinks;

    // Static Initializer
    static {
        userSinks = new ConcurrentHashMap<>();
    }

    private OpenAIClient client;

    @PostConstruct
    public void init() {
        client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(OPENAI_API_KEY))
                .endpoint(OPENAI_URL)
                .buildClient();
    }

    // When accessing the page, create a UUID for each client (for 1-on-1 sending and receiving)  
    // This part of the process is unnecessary if you want to update the same content (1-to-many) like a chat  
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

    // There is a problem with this implementation and it is not working. An issue is being registered.  
    // Now following method can't run because there is an issue.
    @PostMapping("/openai-gpt4-sse-submit")
    @ResponseBody
    public void openaiGpt4Sse(@RequestBody String inputText, @RequestParam UUID userId) {
        LOGGER.debug(inputText);
        List<ChatMessage> chatMessages = createMessages(inputText);
        client.getChatCompletionsStream("gpt-4", new ChatCompletionsOptions(chatMessages))
                .forEach(chatCompletions -> {
                    chatCompletions.getChoices().stream()
                            .map(ChatChoice::getDelta)
                            .map(ChatMessageDelta::getContent)
                            .filter(content -> content != null)
                            .forEach(content -> {   
                                System.out.println(content);
                                Sinks.Many<String> userSink = getUserSink(userId);
                                EmitResult result = userSink.tryEmitNext(content);   
                                showDetailErrorReasonForSSE(result, content, inputText);
                                try {
                                    TimeUnit.MILLISECONDS.sleep(20);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            });
                });
    }

    // The following issue is being registered for the above problem.  
    // [BUG] : The OpenAI library does not work in a WebFlux environment because it internally blocks processing  
    // https://github.com/Azure/azure-sdk-for-java/issues/35301  

    // If the following PR is merged, it will work with the implementation below.  
    // It has been confirmed to work in my local environment.  
    // https://github.com/Azure/azure-sdk-for-java/pull/35312
    // @PostMapping("/openai-gpt4-sse-submit")
    // @ResponseBody
    // public void openaiGpt4Sse(@RequestBody String inputText, @RequestParam UUID userId) {
    //     LOGGER.debug(inputText);
    //     List<ChatMessage> chatMessages = createMessages(inputText);
    //     client.getChatCompletionsStream("gpt-4", new ChatCompletionsOptions(chatMessages))
    //             .subscribe(chatCompletions -> {
    //                 chatCompletions.getChoices().stream()
    //                         .map(ChatChoice::getDelta)
    //                         .map(ChatMessageDelta::getContent)
    //                         .filter(content -> content != null)
    //                         .forEach(content -> {   
    //                             System.out.println(content);
    //                             Sinks.Many<String> userSink = getUserSink(userId);
    //                             EmitResult result = userSink.tryEmitNext(content);   
    //                             showDetailErrorReasonForSSE(result, content, inputText);
    //                             try {
    //                                 TimeUnit.MILLISECONDS.sleep(20);
    //                             } catch (InterruptedException e) {
    //                                 e.printStackTrace();
    //                             }
    //                         });
    //             });
    // }
    
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
    private List<ChatMessage> createMessages(String userInput){
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
        I am a support representative for the ACME Fitness online e-commerce website.  
        I classify the product, inquiry content, category, and sentiment analysis from the inquiry,  
        and output the recommended reply message converted to JSON format.  
        For inquiries not related to products handled online and in e-commerce, the product is NONE,  
        the inquiry content is INVALID, the category is NOT_SUPPORTED, and the returnmessage is a standard JSON format.
            """;

    private final static String USER1 = """
        I still haven't received the smartwatch I purchased here (order number: 12345). Please tell me what the situation is like now.
                        """;

    private final static String ASSISTANT1 = """
            {
            "products": "Smartwatch",  
            "messages": "I still haven't received the smartwatch I purchased here (order number: 12345). Please tell me what the situation is like now.",  
            "category": "Product Undelivered",  
            "emotional": "NEGATIVE",  
            "returnmessage": "Please wait a moment while we check the situation"
            }
                        """;

    private final static String USER2 = """
        What day is it today?  
            """;

    private final static String ASSISTANT2 = """
            {
                "products": "NONE",
                "messages": "INVALID",
                "category": "NOT_SUPPORTED",
                "emotional": "NONE",
                "returnmessage": "I apologize, but as an online support representative for ACME Fitness,  
                I cannot provide information about your inquiry.  
                I would be happy to answer any questions you have about your purchased products, services, or feedback.  
                Please feel free to contact us with any questions.  
                Thank you very much for your understanding."  
            }
            """;

}
