# Virtual E-commerce: ACME Fitness Web Inquiry Page

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

## 環境設定

The `src/main/resources/application.properties` file contains the following information.
Please edit the OPENAI instance name and connection key below.

```text
azure.openai.url=https://YOUR_OWN_OPENAI.openai.azure.com
azure.openai.model.path=/openai/deployments/gpt-4/chat/completions?api-version=2023-03-15-preview
azure.openai.api.key=********************

logging.group.mycustomgroup=com.yoshio3
logging.level.mycustomgroup=INFO
logging.level.root=INFO
```

## Running the Application

```bash
./mvnw spring-boot:run
```

When executed, the following message will be output.

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

Please access `http://localhost:8080`.
The following screen will be display

![OpenAI-SSE-Chat](https://live.staticflickr.com/65535/52952318155_79f600f97c_c.jpg=800x373)

Please try the following inquiries here.

```text
* The smartwatch I purchased here recently (order number: 12345) was broken. Please replace it immediately.  
* I recently bought jogging shoes for Father's Day from here, and my father said they were comfortable and easy to exercise in. Thank you very much.  
* Please tell me your recommended restaurants in Shinjuku.  
* The other day, when the product I purchased arrived at my home, it was left in front of the entrance even though I didn't have any delivery settings. It's a high-priced item and dangerous, so please don't do it again.
```

