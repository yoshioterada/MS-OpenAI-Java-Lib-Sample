package com.yoshio3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessage;
import com.azure.ai.openai.models.ChatMessageDelta;
import com.azure.ai.openai.models.ChatRole;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yoshio3.entities.contentssafety.CreateContentsSafetyMessage;
import com.yoshio3.entities.contentssafety.ResposeMessageFromContentSafety;

/*
 * Sample using Azure OpenAI API Java SDK
 * 
 * <dependency>
 * <groupId>com.azure</groupId>
 * <artifactId>azure-ai-openai</artifactId>
 * <version>1.0.0-beta.1</version>
 * </dependency>
 */

/**
 * Azure OpenAI API Java SDK を利用したサンプル
 */
@Component
public class StandAloneOpenAISampleApp {

    private final static Logger LOGGER = LoggerFactory.getLogger(StandAloneOpenAISampleApp.class);

    // Azure OpenAI の API キー
    private String OPENAI_API_KEY = "";
    // Azure OpenAI のインスタンスの URL
    private String OPENAI_URL = "";
    // Azure OpenAI のモデル名
    private String OPENAI_MODEL_NAME = "";

    // Azure Content Safety の API キー
    private String OCP_APIM_SUBSCRIPTION_KEY = "";
    private String CONTENT_SAFETY_URL = "";

    private OpenAIClient client;

    public StandAloneOpenAISampleApp() throws IOException {
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/application.properties"));
        OPENAI_API_KEY = properties.getProperty("azure.openai.api.key");
        OPENAI_URL = properties.getProperty("azure.openai.url");

        OPENAI_MODEL_NAME = properties.getProperty("azure.openai.model.name");
        OCP_APIM_SUBSCRIPTION_KEY = properties.getProperty("azure.contentsafety.api.key");
        CONTENT_SAFETY_URL = properties.getProperty("azure.contentsafety.url");
        client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(OPENAI_API_KEY))
                .endpoint(OPENAI_URL)
                .buildClient();
    }

    public static void main(String[] args) {
        StandAloneOpenAISampleApp sample;
        try {
            sample = new StandAloneOpenAISampleApp();
            sample.invokeSample();
            sample.invokeStreamTest();
            sample.invokeTextEmbedding();
            sample.testContenteSafety();

        } catch (IOException e) {
            LOGGER.error("Error occurred in main method.", e);
        }
    }

    private final static String SYSTEM_DEFINITION = """
            私は、ACME Fitness というオンライン・電子商取 Web サイトのサポート対応担当者です。
            問い合わせされた内容から、製品、問い合わせ内容、カテゴリ、感情分析を分類し、
            さらに推奨する返信メッセージを JSON フォーマットに変換し出力します。
            オンライン、電子商取引で扱う商品以外の問い合わせには、製品は NONE,
            問い合わせ内容は INVALID、カテゴリは NOT_SUPPORTED、returnmessage は定型分 の JSON を返します。
            """;

    private final static String USER1 = """
            こちらで購入した、スマートウォッチ(注文番号 : 12345)がまだ届きません。現在どのような状況か教えてください。
                        """;

    private final static String ASSISTANT1 = """
            {
                "products": "スマートウォッチ",
                "messages": "スマートウォッチ(注文番号 : 12345)がまだ届きません。現在どのような状況か教えてください。",
                "category": "商品未配達",
                "emotional": "NEGATIVE",
                "returnmessage": "状況を確認しますので、しばらくお待ちください"
            }
                        """;

    private final static String USER2 = """
            今日は何日ですか？
            """;

    private final static String ASSISTANT2 = """
            {
                "products": "NONE",
                "messages": "INVALID",
                "category": "NOT_SUPPORTED",
                "emotional": "NONE",
                "returnmessage": "申し訳ございませんが、私は ACME Fitness のオンラインサポート担当であり、
                お問い合わせ内容に関する情報は提供できません。
                お客様の購入商品やサービス、フィードバックに関するお問い合わせには喜んでお答えいたしますので、
                どうぞお気軽にお問い合わせください。
                どうぞよろしくお願いいたします。"
            }
            """;

    private final static String USER_INPUT1 = "先日こちらで購入した、スマートウォッチ(注文番号 : 12345)が壊れていました。すぐに交換してください。";
    private final static String USER_INPUT2 = "最近、こちらで父の日にジョギングシューズを購入しましたが、父が履き心地が良く、運動しやすいと言っていました。ありがとうございます！";
    private final static String USER_INPUT3 = "新宿のおすすめのレストランを教えてください。";
    private final static String USER_INPUT4 = "先日購入した商品が自宅に届いた際、置き配設定していないのに玄関の前においていかれました。高額商品で危ないので２度とやめてください";

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

    /**
     * Sample to call OpenAI API
     */
    private void invokeSample() {
        // Request 1
        var chatCompletions = client.getChatCompletions(OPENAI_MODEL_NAME,
                new ChatCompletionsOptions(createMessages(USER_INPUT1)));
        chatCompletions.getChoices().forEach(choice -> {
            ChatMessage message = choice.getMessage();
            LOGGER.info("Message:");
            LOGGER.info(message.getContent());
        });

        sleep();

        // Request 2
        var chatCompletions2 = client.getChatCompletions(OPENAI_MODEL_NAME,
                new ChatCompletionsOptions(createMessages(USER_INPUT2)));
        chatCompletions2.getChoices().forEach(choice -> {
            ChatMessage message = choice.getMessage();
            LOGGER.info("Message:");
            LOGGER.info(message.getContent());
        });

        sleep();

        // Request 3
        var chatCompletions3 = client.getChatCompletions(OPENAI_MODEL_NAME,
                new ChatCompletionsOptions(createMessages(USER_INPUT3)));
        chatCompletions3.getChoices().forEach(choice -> {
            ChatMessage message = choice.getMessage();
            LOGGER.info("Message:");
            LOGGER.info(message.getContent());
        });
    }

    /**
     * Stream API を利用したサンプル
     */
    private void invokeStreamTest() {
        var chatMessages = createMessages(USER_INPUT4);
        client.getChatCompletionsStream(OPENAI_MODEL_NAME, new ChatCompletionsOptions(chatMessages))
                .forEach(chatCompletions -> {
                    chatCompletions.getChoices().stream()
                            .map(ChatChoice::getDelta)
                            .map(ChatMessageDelta::getContent)
                            .filter(content -> content != null)
                            .forEach(content -> {
                                LOGGER.info(content);
                                try {
                                    TimeUnit.MILLISECONDS.sleep(20);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            });
                });
    }

    /**
     * テキスト・エンベディングの比較用サンプル文字列
     */
    private final static String REQUEST_STRING1 = "AAA BBB CCC DDD EEE FFF GGG HHH III JJJ";
    private final static String DBSTORE_STRING2 = "AAA BBB CCC DDD EEE FFF GGG HHH III JJ";
    private final static String DBSTORE_STRING3 = "AAA BBB KKK LLL MMM NNN OOO PPP QQQ RRR";
    private final static String DBSTORE_STRING4 = "みなさま今日は JJUG CCC で発表しています";

    /**
     * テキスト・エンべディングの検証サンプル (text-embedding-ada-002)
     */
    private void invokeTextEmbedding() {
        // 現時点では 1回のリクエストに１つの配列のみをサポートされているようです
        // 最初のがユーザーから入力された文字列を想定
        EmbeddingsOptions embeddingsOptions = new EmbeddingsOptions(Arrays.asList(REQUEST_STRING1));

        var result = client.getEmbeddings("text-embedding-ada-002", embeddingsOptions);
        var embedding = result.getData().stream().findFirst().get().getEmbedding();
        // ここで出力されるのは多次元配列だけ。
        LOGGER.info(embedding.toString());
        LOGGER.info("------- 上記が Embedded を使用した際の OpenAI からの返り値です。 -------");
        LOGGER.info("------- Vector 配列を返し、これをコサイン類似性で類似度を取得します -------");

        sleep();

        /////////////////// DB に入っているデーターと想定 ///////////////////////
        // ２つ目の文字列作成
        var embeddingsOptions2 = new EmbeddingsOptions(Arrays.asList(DBSTORE_STRING2));
        var embeddings2 = client.getEmbeddings("text-embedding-ada-002", embeddingsOptions2);
        var embedding2 = embeddings2.getData().stream().findFirst().get().getEmbedding();
        sleep();

        // ３つ目の文字列作成
        var embeddingsOptions3 = new EmbeddingsOptions(Arrays.asList(DBSTORE_STRING3));
        var embeddings3 = client.getEmbeddings("text-embedding-ada-002", embeddingsOptions3);
        var embedding3 = embeddings3.getData().stream().findFirst().get().getEmbedding();
        sleep();

        // 4つ目の文字列作成
        var embeddingsOptions4 = new EmbeddingsOptions(Arrays.asList(DBSTORE_STRING4));
        var embeddings4 = client.getEmbeddings("text-embedding-ada-002", embeddingsOptions4);
        var embedding4 = embeddings4.getData().stream().findFirst().get().getEmbedding();
        /////////////////// DB に入っているデーターと想定 ///////////////////////

        // DB に検索をかけているのと同じ
        double cosineSimilarity1 = cosineSimilarity(embedding, embedding2);
        double cosineSimilarity2 = cosineSimilarity(embedding, embedding3);
        double cosineSimilarity3 = cosineSimilarity(embedding, embedding4);

        // DB はこれらの内 1 に一番近い値を返すという事？
        LOGGER.info("ユーザから入力された文字 : \n" + REQUEST_STRING1);
        LOGGER.info("\nDB に保存されている文字列＆ベクトル:");
        LOGGER.info(DBSTORE_STRING2 + "  = \t" + cosineSimilarity1 + "\t<----最も1に近い (最も類似性が高い)");
        LOGGER.info(DBSTORE_STRING3 + " = \t" + cosineSimilarity2 + "\t<----次に1に近い");
        LOGGER.info(DBSTORE_STRING4 + "= \t" + cosineSimilarity3 + "\t<----最も1から離れている");
    }

    // コサイン類似性を計算する (簡易版）
    public double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // リクエストの呼び出し制限にかからないように
    private void sleep() {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Content Safety 検証用サンプル文字列
    private final static String SAFETY_TEST1 = "赤ちゃんがおっぱいを飲みたいと言っています";
    private final static String SAFETY_TEST2 = "赤ちゃんがおっぱいをみたいと言っています";
    private final static String SAFETY_TEST3 = "おっぱいをみたいと言っています";

    private void testContenteSafety() {
        var category = List.of("Hate", "Sexual", "SelfHarm", "Violence");
        var blockLists = List.of("");

        // テスト１
        var createMessage1 = new CreateContentsSafetyMessage(SAFETY_TEST1, category, blockLists, false);
        invokeContentsSafety(createMessage1);

        // テスト2
        var createMessage2 = new CreateContentsSafetyMessage(SAFETY_TEST2, category, blockLists, false);
        invokeContentsSafety(createMessage2);

        // テスト3
        var createMessage3 = new CreateContentsSafetyMessage(SAFETY_TEST3, category, blockLists, false);
        invokeContentsSafety(createMessage3);

        // テスト4
        var createMessage4 = new CreateContentsSafetyMessage(SAFETY_TEST1, category, List.of("test"), true);
        invokeContentsSafety(createMessage4);
    }

    // Content Safety の呼び出し
    private void invokeContentsSafety(CreateContentsSafetyMessage createMessage) {
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(createMessage);

        var restTemplate = new RestTemplate();
        var httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.set("Ocp-Apim-Subscription-Key", OCP_APIM_SUBSCRIPTION_KEY);
        var entity = new HttpEntity<>(json, httpHeaders);

        ResposeMessageFromContentSafety result = null;
        try {
            var exchange = restTemplate.exchange(CONTENT_SAFETY_URL, HttpMethod.POST, entity,
                    String.class);

            String body = exchange.getBody();
            result = gson.fromJson(body, ResposeMessageFromContentSafety.class);
            LOGGER.info("----- Sefety Check Reulst for " + createMessage.text());
            printShowResult(result);
            LOGGER.info("--------------------");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void printShowResult(ResposeMessageFromContentSafety result) {
        if (result == null) {
            LOGGER.info("result is null");
            return;
        }
        var blocklistsMatchResults = result.blocklistsMatchResults();
        var hateResult = result.hateResult();
        var selfHarmResult = result.selfHarmResult();
        var sexualResult = result.sexualResult();
        var violenceResult = result.violenceResult();

        if (blocklistsMatchResults != null && blocklistsMatchResults.size() > 0) {
            LOGGER.info("BlockListsにマッチしました : ");
            blocklistsMatchResults.forEach(blocklistsMatchResult -> {
                LOGGER.info("blocklistsMatchResult : " + blocklistsMatchResult);
            });
        }

        if (hateResult != null && hateResult.severity() > 0) {
            LOGGER.info("Hateにマッチしました : ");
            LOGGER.info("hateResult : " + hateResult);
        }

        if (sexualResult != null && sexualResult.severity() > 0) {
            LOGGER.info("Sexualにマッチしました : ");
            LOGGER.info("sexualResult : " + sexualResult);
        }

        if (selfHarmResult != null && selfHarmResult.severity() > 0) {
            LOGGER.info("SelfHarmにマッチしました : ");
            LOGGER.info("selfHarmResult : " + selfHarmResult);
        }

        if (violenceResult != null && violenceResult.severity() > 0) {
            LOGGER.info("Violenceにマッチしました : ");
            LOGGER.info("violenceResult : " + violenceResult);
        }
    }
}
