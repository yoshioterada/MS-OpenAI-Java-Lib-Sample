# Virtual E-commerce: ACME Fitness Web Inquiry Page

本アプリケーションは、[Azure OpenAI client library for Java - version 1.0.0-beta.1](https://learn.microsoft.com/ja-jp/java/api/overview/azure/ai-openai-readme) を利用して実装した、Server Sent Event を利用して、ChatGPT からの応答を Streaming 形式でブラウザに表示するサンプルです。

```java
		<dependency>
			<groupId>com.azure</groupId>
			<artifactId>azure-ai-openai</artifactId>
			<version>1.0.0-beta.2</version>
		</dependency>
```

本ライブラリを利用する以外に、[RESTful API を利用したサンプル](https://github.com/yoshioterada/Azure-OpenAI-Java-Spring-Sample-for-Chat-GPT-4) もあります。
If the following PR is merged, the code I have wrote in the comments will be working. I have confirmed that the PR fixes are working properly in my local environment.

[PR FIX: The OpenAI library does not work in a WebFlux environment becaus… #35312](https://github.com/Azure/azure-sdk-for-java/pull/35312)

https://github.com/yoshioterada/MS-OpenAI-Java-Lib-Sample/blob/24f576bca2775b919a5cfe6a9aa3c5c53a443a07/src/main/java/com/yoshio3/SSEOpenAIController.java#L110-L140

If you want to send messages in real time using Server Sent Event, please use the RESTful API implementation below until the above bug is fixed.


## Project Directory Structure

This project includes the following contents.

```text
.
├── HELP.md
├── README.md
├── image
├── mvnw
├── mvnw.cmd
├── pom.xml
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── yoshio3
    │   │           ├── AppMain.java
    │   │           ├── SSEOpenAIController.java
    │   │           ├── request
    │   │           │   ├── Message.java
    │   │           │   └── OpenAIMessages.java
    │   │           └── response
    │   │               ├── ChatCompletionChunk.java
    │   │               ├── Choice.java
    │   │               └── Delta.java
    │   └── resources
    │       ├── META-INF
    │       ├── application.properties
    │       ├── static
    │       └── templates
    │           └── index.html
    └── test
        └── java
            └── com
                └── yoshio3
```

## Required Tools

To try and run this application, the following commands and tools are required.

1. Java 17
2. Maven 3.6.3
3. Azure Account

## Preferences

このアプリケーションを動作させるためには Azure OpenAI を作成した後、インスタンス名と接続キーを設定する必要があります。
設定は `src/main/resources/application.properties` ファイルの中で行います。
下記の設定ファイルを編集し、Azure OpenAI のインスタンス名と接続キーを設定してください。

```text
azure.openai.url=https://YOUR_OWN_OPENAI.openai.azure.com
azure.openai.model.path=/openai/deployments/gpt-4/chat/completions?api-version=2023-03-15-preview
azure.openai.api.key=********************

logging.group.mycustomgroup=com.yoshio3
logging.level.mycustomgroup=INFO
logging.level.root=INFO
```

## Running the Application

設定を行ったのち、下記のコマンドを実行してください。

```bash
./mvnw spring-boot:run
```

コマンドを実行すると、下記のようなログが表示されます。

```text
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.0)

2023-06-05T16:52:27.596+09:00  INFO 4081 --- [           main] com.yoshio3.AppMain                      : Starting AppMain using Java 17.0.6 with PID 4081 (/Users/teradayoshio/JavaOnAzureDay2023/Chat-GPT-4-sample/target/classes started by teradayoshio in /Users/teradayoshio/JavaOnAzureDay2023/Chat-GPT-4-sample)
2023-06-05T16:52:27.598+09:00  INFO 4081 --- [           main] com.yoshio3.AppMain                      : No active profile set, falling back to 1 default profile: "default"
2023-06-05T16:52:28.648+09:00  INFO 4081 --- [           main] o.s.b.web.embedded.netty.NettyWebServer  : Netty started on port 8080
2023-06-05T16:52:28.655+09:00  INFO 4081 --- [           main] com.yoshio3.AppMain                      : Started AppMain in 1.287 seconds (process running for 1.53)
```

### Verify operation from browser

正常にアプリケーションが起動すると、ブラウザから `http://localhost:8080` にアクセスしてください。
すると、下記のような画面が表示されます。

![OpenAI-SSE-Chat](https://live.staticflickr.com/65535/52952318155_79f600f97c_c.jpg=800x373)

ブラウザの画面に表示されている `Submit` ボタンをクリックし、Azure OpenAI に対してリクエストを送信し結果を受信します。

例えば、下記のようなサンプルの文章を入力してください。
すると Azure OpenAI がメッセージを作成し、受信した文字を順番にブラウザ上に表示します。

```text
* "先日こちらで購入した、スマートウォッチ(注文番号 : 12345)が壊れていました。すぐに交換してください。"
* "最近、こちらで父の日にジョギングシューズを購入しましたが、父が履き心地が良く、運動しやすいと言っていました。ありがとうございます！"
* "新宿のおすすめのレストランを教えてください。"
* "先日購入した商品が自宅に届いた際、置き配設定していないのに玄関の前においていかれました。高額商品で危ないので２度とやめてください"
```

