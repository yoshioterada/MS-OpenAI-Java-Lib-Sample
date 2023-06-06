# Virtual E-commerce: ACME Fitness Web Inquiry Page

Currently, this program does not work correctly.
I'm creating the following bug registration and pull request.

The following issue is being registered for the above problem.  
[BUG : The OpenAI library does not work in a WebFlux environment because it internally blocks processing](https://github.com/Azure/azure-sdk-for-java/issues/35301)

If the following PR is merged, the code I have wrote in the comments will be working. I have confirmed that the PR fixes are working properly in my local environment.

[PR FIX: The OpenAI library does not work in a WebFlux environment becaus… #35312](https://github.com/Azure/azure-sdk-for-java/pull/35312)

https://github.com/yoshioterada/MS-OpenAI-Java-Lib-Sample/blob/24f576bca2775b919a5cfe6a9aa3c5c53a443a07/src/main/java/com/yoshio3/SSEOpenAIController.java#L110-L140

If you want to send messages in real time using Server Sent Event, please use the RESTful API implementation below until the above bug is fixed.

[REST Version : Azure-OpenAI-Java-Spring-Sample-for-Chat-GPT-4
Public](https://github.com/yoshioterada/Azure-OpenAI-Java-Spring-Sample-for-Chat-GPT-4)

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

