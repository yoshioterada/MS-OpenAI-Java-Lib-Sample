# Azure OpenAI Embedding モデルを利用し最も関連性の高いドキュメントを見つける方法

## 1. はじめに

私は、元々、機械学習などを今ままで触ってこなかったので、`Embedding`, `組み込み`, `埋め込み` という言葉が出てきた時に ??? となりました。このモデルをして、何ができるのか？！そこからわかっていませんでした。
そこで、実際に調べたり動かして、このモデルの有用性について理解できました。このエントリは、そうして分かった事を共有したいと多います。

## 2. 精度が高く安いモデルが登場 !

OpenAI には、Embedding のモデルとして `text-embedding-ada-002` があります。
これを利用する事で、最も関連性の高いドキュメントを、より低価格で見つける事ができます。  

同じ Embedding モデルには、`Davinci` というモデルや他にもいくつかモデルがありますが、`text-embedding-ada-002` は他のモデルに比べてほとんどの処理で精度が高く、他よりもかなり格安に使えます。そこで、類似検索のような処理に対しては、これを利用するのがとてもオススメだそうです。

[Azure OpenAI Service Pricing](https://azure.microsoft.com/en-us/pricing/details/cognitive-services/openai-service/) によると 2023/6/8 時点で下記のような価格差がありました。

|  Embedding Models  |  1,000 トークン辺りの価格 |
| ---- | ---- |
|  Ada  |  ¥0.056199  |
|  Babbage  |  ¥0.70248  |	 
|  Curie  |  ¥2.809901  |	 
|  Davinci  |  ¥28.099001  |	 

## 3. Embedding って何？

それでは、Embedding を使っていきたいのですが、そもそも Embedding とは何かを分からなければ、どう使ったら良いかも分からないので、Embedding について簡単な概念だけでも抑えておきたいと思います。
結論から言うと、高校時代に習ったベクトルの考え方を利用します。

高校時代に習ったベクトルでは、２つの矢印を比べて、同じ方向に線が向いていて、同じ長さならば、それらは同じベクトルと習いました。この方法を利用して、長さと向きができる限り近いベクトルを探し出せば、それが一番似ているということになります。下の図では青い線は全く同じベクトルですが、このベクトルに一番近いベクトル「①」を探し出せれば、それが一番近い内容になります。

![Vector Image](https://live.staticflickr.com/65535/52959955338_19b89bc1d1_b.jpg)

実際に Embedding の、`text-embedding-ada-002` モデルに対して、自然言語の文字列を渡すと、下記のように、`1536` 個の浮動小数点数の高次元配列(ベクトル) が得られます。

例：「`Azure Blob について教えてください`」という文字列の結果の抜粋

```text
[-0.013197514, -0.025243968, 0.011384923, -0.015929632, -0.006410221, 0.031038966, 
-0.016921926, -0.010776317, -0.0019300125, -0.016300088, 0.01767607, -0.0047100903,
0.009691408, -0.014183193, -0.017001309, -0.014434575, 0.01902559, 0.010961545, 
0.013561356, -0.017371766, -0.007964816, 0.0026841562, 0.0019663966, -0.0019878964, 
-0.025614424, -0.0030298054, 0.020229574, -0.01455365, 0.022703694, -0.02033542, 
0.035696134, -0.002441044, -0.008057429, 0.0061191483, 0.004263558, -0.0025518502,
0.018046526, 0.011411385, 0.0063804523, -0.0021020102, 0.027572552, -0.017967142,
0.0077663567, 0.005361697, -0.0116693815, 0.004524862, -0.043581568, -0.01028017, 

.... 中略

-0.0017265921, 0.083035186, -0.006205147, -0.008646191, 0.0070651355, -0.019052051, 
0.008374964, 0.024225213, 0.01522841, 0.019951731, -0.006516066, 0.017967142, 
0.0058082296, -0.0053253127, -0.009929558, -0.039109625, -0.031277116, -0.015863478, 
0.011040928, 0.012529369, 0.013012286, 0.022981536, -0.013706892, 0.012965979, 
0.011953839, -0.01903882, 0.015347485, 0.019052051, -0.0046538603, 0.012191989, 
-0.020983716, 0.0078722015, -0.0018605519, -0.02775778, -0.026739024, -0.010359553, 
-0.013918581, -0.011933993, 0.0066814483, 0.005196315, -0.0045744767, -2.7598185E-4, 
0.012251527, -0.018178832, -0.013276898, 0.011709073, -0.022928614, 0.002131779, 
-0.007462053, 0.0044554016]
```

ユーザが入力した文字列と、DB などに事前に保存しておいたベクトルを比較し、近いものを探すというのが、この Embedded を利用した方法になります。

そして、その一番近いものを探し出すときに使われる方法はいくつかあるのですが、最も一般的に知られている方法として、コサイン類似度を計算する方法があります。コサイン類似度は、２つのベクトルを渡して計算をすると、結果として -1 〜 1 までの値が返ってきます。これを、DB に保存されているデータと合わせて計算をしていく形になります。  
そして、最終的に 1 に一番近い結果を得た内容が一番近い内容になります。

```text
ユーザから入力された文字の例： 
AAA BBB CCC DDD EEE FFF GGG HHH III JJJ

DB に保存されている文字列とコサイン類似度の計算結果
AAA BBB CCC DDD EEE FFF GGG HHH III JJ  =       0.9942949478994986  <----最も1に近い (最も類似性が高い)
AAA BBB KKK LLL MMM NNN OOO PPP QQQ RRR =       0.930036739659776   <----次に1に近い
みなさま今日は JJUG CCC で発表しています=     		0.7775105340227892  <----最も1から離れている
```

このように、利用者から入力された問い合わせ内容の文字列をベクトル化して、されに事前に DB に保存されているベクトル（配列）を合わせて計算し、類似検索を行います。

## 4. 扱う文字列の注意点

### トークン数の上限

`text-embedding-ada-002` で扱えるトークン数（ほぼ文字数）の上限は、8192　トークンです。そこで、約 8000 文字を超えるような文章を扱う場合には、分割が必要です。

### 扱う文字列の事前整備

「[改行文字を 1 つのスペースに置き換える](https://learn.microsoft.com/ja-jp/azure/cognitive-services/openai/how-to/embeddings?tabs=console#replace-newlines-with-a-single-space)」 に記載されているのですが、 「***改行が存在すると想定どおりの結果が得られないことが確認されている***」そうです。  

そこで、`text-embedding-ada-002` にメッセージを送信する前に、対象の文字列中に含まれる`改行文字 (\n)` を空白スペースに置き換えることをお勧めします。

例えば、下記のような文章があった場合に、全ての改行文字を置き換えて1行の文字列にしてください。(下記のサンプルコードをご参照）

```text
Visual Studio Code for Javaは、Microsoftが提供するオープンソースのコードエディタ

Visual Studio Code上で、Javaプログラミング言語をサポートするための拡張機能です。

Java開発者が効率的にコードを記述、ビルド、デバッグ、テスト、実行できる環境を提供します。
Visual Studio Codeは多言語対応の軽量なテキストエディタであり、高い拡張性が特徴です。
Java開発者にとっては、Visual Studio Code for Javaが優れた開発環境として利用できるでしょう。

Visual Studio Code for Javaは、Java Development Kit (JDK)をインストールすることで使用できます。
JDKはJavaプログラムをコンパイル、実行するための基本的なツールセットです。

Visual Studio CodeでJavaプロジェクトを開始するには、JDKをインストールした後、Visual Studio Codeの
拡張機能マーケットプレイスからJava Extension Packをインストールします。

Java Extension Packには、Java言語サポート、デバッグ、テスト、プロジェクト管理などの機能が含まれています。

Visual Studio Code for Javaの主な機能は以下のとおりです:
1. シンタックスハイライト: Javaのコードに色付けを行い、可読性を向上させます。
2. コード補完: コード入力中に可能性のあるコードを提案し、効率的にコードを記述できるようにします。
3. コードナビゲーション: クラス、メソッド、変数へのジャンプや、定義の検索が容易になります。
4. リファクタリング: コードの構造や名前を変更し、コードの品質を向上させる機能があります。
5. デバッグ: ブレークポイントを設定し、ステップ実行や変数の監視などのデバッグ機能を利用できます。
6. テスト: JUnitやTestNGなどのテストフレームワークをサポートし、テストの作成、実行、結果の表示ができます。
7. プロジェクト管理: MavenやGradleなどのビルドツールをサポートし、プロジェクトの構成や依存関係の管理が行えます。
8. Gitの統合: ソースコードのバージョン管理を行うGitとの統合があります。

Visual Studio Code for Javaは、開発者の生産性を向上させる機能が豊富であり、Javaプロジェクトの開発に適した環境を提供します。また、Visual Studio Codeの拡張機能マーケットプレイス
には、さまざまなJava関連の拡張機能がありますので、必要に応じて追加することができます。これらの拡張機能
により、Java開発者はVisual Studio Codeを中心とした統合開発環境として利用できるでしょう。
```

## 4. 動作確認

### 4.1 Azure で利用可能な Vector DB

ベクトルの保存先として、Azure ではいくつかの選択肢があります。(2023/6/8時点では下記)  
ご要望に応じて適切な DB をご利用ください。

1. [Azure Database for PostgreSQL - フレキシブル サーバーで pgvector を有効にして使用する方法](https://learn.microsoft.com/ja-jp/azure/postgresql/flexible-server/how-to-use-pgvector)
2. [Azure Cosmos DB for MongoDB 仮想コアの埋め込みでのベクター検索の使用](https://learn.microsoft.com/ja-jp/azure/cosmos-db/mongodb/vcore/vector-search)
3. [Azure Cognitive Search (Private Preview)](https://aka.ms/VectorSearchSignUp)
4. [Azure Cache for Redis Enterprise](https://techcommunity.microsoft.com/t5/azure-developer-community-blog/introducing-vector-search-similarity-capabilities-in-azure-cache/ba-p/3827512)

	
### 4.2 Azure PostgreSQL Flexible Server で Vector Search

上記のように、Vector DB は選択肢が色々あるため、ご要望に応じてご選択いただきたいのですが、今回は検証のため、PostgreSQL Flexible Server を利用することにしました。Vector を扱えるようにするまでの手順を下記に記載していますので、ご興味のある方はお試しください。  
違う永続化先をご選択される場合は、この章は飛ばしてください。

#### 4.2.1 環境変数の設定

Azure 上にリソースを作成するために、以下の環境変数を適宜修正して設定してください。

```text
export RESOURCE_GROUP=PostgreSQL
export LOCATION=eastus
export POSTGRES_SERVER_NAME=yoshiopgsql3
export POSTGRES_USER_NAME=yoterada
export POSTGRES_USER_PASS='!'$(head -c 12 /dev/urandom | base64 | tr -dc '[:alpha:]'| fold -w 8 | head -n 1)$RANDOM
echo "GENERATED PASSWORD: " $POSTGRES_USER_PASS
export POSTGRES_DB_NAME=VECTOR_DB
export SUBSCRIPTION_ID=f77aafe8-6be4-4d3d-bd9c-d0c37687ef70
export PUBLIC_IP=$(curl ifconfig.io -4)
```

上記の設定例では、パスワードは自動生成し、作成したパスワードを標準出力に出力しています。
ご自身のパスワードをご入力いただくか、もしくは出力されたパスワードはメモしておいてください。

#### 4.2.2 Azure PostgreSQL Flexible Server のインストール

下記の 3 コマンドを実行してください。  
コマンドを実行すると、下記の作業を行います。

1. Azure PostgreSQL Flexible Server をインストール
2. Firewall の設定
3. 新規データベースの作成

```bash
az postgres flexible-server create --name $POSTGRES_SERVER_NAME \
    -g $RESOURCE_GROUP \
    --location $LOCATION \
    --admin-user $POSTGRES_USER_NAME \
    --admin-password $POSTGRES_USER_PASS \
    --public-access $PUBLIC_IP
    --yes
az postgres flexible-server firewall-rule create \
    -g $RESOURCE_GROUP \
    -n $POSTGRES_SERVER_NAME \
    -r AllowAllAzureIPs \
    --start-ip-address 0.0.0.0 \
    --end-ip-address 255.255.255.255
az postgres flexible-server db create \
    -g $RESOURCE_GROUP \
    -s $POSTGRES_SERVER_NAME \
    -d $POSTGRES_DB_NAME
```

#### 4.2.3 Azure PostgreSQL Flexible Server の日本語化設定

今回、永続化するデータには日本語文字列も含まれるため、DB 内で日本語 UTF-8 を扱えるように下記の設定を行ってください。

```bash
az postgres flexible-server parameter set \
    -g $RESOURCE_GROUP \
    --server-name $POSTGRES_SERVER_NAME \
    --subscription $SUBSCRIPTION_ID \
    --name lc_monetary --value "ja_JP.utf-8"
```

```bash
az postgres flexible-server parameter set \
    -g $RESOURCE_GROUP \
    --server-name $POSTGRES_SERVER_NAME \
    --subscription $SUBSCRIPTION_ID \
    --name lc_numeric --value "ja_JP.utf-8"
```

```bash
az postgres flexible-server parameter set \
    -g $RESOURCE_GROUP \
    --server-name $POSTGRES_SERVER_NAME \
    --subscription $SUBSCRIPTION_ID \
    --name timezone --value "Asia/Tokyo"
```

#### 4.2.4 Azure PostgreSQL Flexible Server に拡張機能のインストール

PostgreSQL で UUID と Vector を扱えるようにする為に拡張機能を利用できるようにします。
下記のコマンドを実行してください。

> 注意：   
> "VECTOR,UUID-OSSP" の間に 空白は開けないでください。

```bash
az postgres flexible-server parameter set \
    -g $RESOURCE_GROUP \
    --server-name $POSTGRES_SERVER_NAME \
    --subscription $SUBSCRIPTION_ID \
    --name azure.extensions --value "VECTOR,UUID-OSSP"
```

### 4.3 PostgreSQL で Vector を扱うテーブル作成

上記で、PostgreSQL の設定が完了したので、下記のコマンドを実行して接続します。

```bash
> psql -U $POSTGRES_USER_NAME -d $POSTGRES_DB_NAME \
      -h $POSTGRES_SERVER_NAME.postgres.database.azure.com 
```

接続に成功したら、先ほど追加した拡張機能を、PostgreSQL 内で利用できるようにします。  
下記のように `CREATE EXTENSION ` のコマンドをそれぞれ実行してください。

```psql
SSL connection (protocol: TLSv1.3, cipher: TLS_AES_256_GCM_SHA384, bits: 256, compression: off)
Type "help" for help.

VECTOR_DB=>
VECTOR_DB=> CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION
VECTOR_DB=> CREATE EXTENSION IF NOT EXISTS "vector";
CREATE EXTENSION
```

最後に、Vector のデータを保存するためのテーブルを作成します。   
`embedding VECTOR(1536)` の部分に Vector 情報を保存します。
今回は簡単に、origntext も一緒に保存し、一番類似性の高かった文字列のオリジナルの文字列を表示するようにしています。
実際にはリンク先 URL にしても良いかと思いますし、後から別のテーブルと結合してご利用いただいても良いかと思います。

```psql
VECTOR_DB=> CREATE TABLE TBL_VECTOR_TEST(
    id uuid,
    embedding VECTOR(1536),
    origntext varchar(8192),
    PRIMARY KEY (id)
    );
CREATE TABLE
```

### 4.4 Java アプリケーションの作成

#### 4.4.1 Maven プロジェクトに依存関係の追加

Azure OpenAI のライブラリを利用して、PostgreSQL に対して接続し、データの永続化を行う為には、最低限下記の依存関係の追加が必要です。下記を `pom.xml` に追加してください。

```xml
		<dependency>
			<groupId>com.azure</groupId>
			<artifactId>azure-ai-openai</artifactId>
			<version>1.0.0-beta.1</version>
		</dependency>
		<!-- https://mvnrepository.com/artifact/org.postgresql/postgresql -->
		<dependency>
			<groupId>org.postgresql</groupId>
			<artifactId>postgresql</artifactId>
			<version>42.6.0</version>
		</dependency>
```

#### 4.4.2 プロパティ・ファイルの作成と設定

`src/main/resources/application.properties` ファイルに作成しプロパティの設定情報を記述してください。

```text
azure.openai.url=https://YOUR_OWN_AZURE_OPENAI.openai.azure.com
azure.openai.model.name=gpt-4
azure.openai.api.key=************************************

azure.postgresql.jdbcurl=jdbc:postgresql://YOUR_POSTGRESQL.postgres.database.azure.com:5432/VECTOR_DB
azure.postgresql.user=yoterada
azure.postgresql.password=************************************

logging.group.mycustomgroup=com.yoshio3
logging.level.mycustomgroup=DEBUG
logging.level.root=INFO
```

#### 4.4.3 Java プログラムの実装

最後に、Java のコードを実装します。

サンプルコードは、下記のリンク先にあります。

[VectorTest.java](https://github.com/yoshioterada/MS-OpenAI-Java-Lib-Sample/blob/master/src/main/java/com/yoshio3/VectorTest.java)

##### 実装上のポイント 1

https://github.com/yoshioterada/MS-OpenAI-Java-Lib-Sample/blob/bd6c7c1cff34ed93d51c1ea80df7cabc1b7ba6eb/src/main/java/com/yoshio3/VectorTest.java#L34-L43

上記で `private final static List<String> INPUT_DATA` と言う文字列のリストを定義しています。
このリストの中には、下記のサービスに関する説明を、文字配列のリストとして保存しています。上記でも申し上げましたが、改行文字が含まれる場合正しい結果が得られない可能性がある為、全ての改行文字を空白文字に置き換えています。

1. Visual Studio Code
2. Azure App Service for Java
3. Azure Container Apps
4. Azure Cosmos DB
5. Azure Kubernetes Service
6. Azure Cognitive Service
7. Azure Container Instances
8. Azure Data Lake Storage 
9. Azure Blob Storage

##### 実装上のポイント 2

下記の `invokeTextEmbedding` メソッドの中から Azure OpenAI の Embedded モデルを呼び出しています。
このメソッドに文字列を与える事で、結果として浮動小数の List が返ってきます。

https://github.com/yoshioterada/MS-OpenAI-Java-Lib-Sample/blob/bd6c7c1cff34ed93d51c1ea80df7cabc1b7ba6eb/src/main/java/com/yoshio3/VectorTest.java#L80-L85

##### 実装上のポイント 3

下記のメソッドは、事前に用意した文字列の List (INPUT_DATA) から、一つずつ要素を取り出して、Azure OpenAI の Embedded を呼び出し、多次元配列（ベクター）を受け取った後、DB に対して保存しています。
この処理は、テスト用のデータ挿入なので、一度だけ実行してください。

https://github.com/yoshioterada/MS-OpenAI-Java-Lib-Sample/blob/3a5bd7fa1a13b76d353408451bd244b4c00ef243/src/main/java/com/yoshio3/VectorTest.java#L85-L104

##### 実装上のポイント 4

最後に DB に保存されている情報と、ユーザから入力された情報を比較し、最も関連性の高いドキュメントを見つけます。  

https://github.com/yoshioterada/MS-OpenAI-Java-Lib-Sample/blob/bd6c7c1cff34ed93d51c1ea80df7cabc1b7ba6eb/src/main/java/com/yoshio3/VectorTest.java#L70-L71

今回のサンプルでは、上記のように  main() メソッドの中で 「`Azure Blob について教えてください`」 と言う文字を入力しています。
そして、この文字も `invokeTextEmbedding(data);` を呼び出して多次元配列を作成している点にご注意ください。

その多次元配列を下記のクエリに渡しています。

`SELECT origntext FROM TBL_VECTOR_TEST ORDER BY embedding <-> ? LIMIT 1;"`

上記では、`LIMIT 1`と記述していますので、最も関連性の高いドキュメントだけが出力されています。複数の結果を返したい場合は、この値を変更してください。

さらに、`<->` と記載している箇所は変更が可能です。PostgreSQL の `pgvector` では、下記の３種類の演算子を指定して類似度を計算する事ができるようになっています。必要に応じて演算子を変えてください。

|  演算子  | 説明|
| ---- | ---- |
|  <->  |  ユークリッド距離 : n 次元空間内の 2 つのベクトル間の直線距離を測定 |
|  <#>  |  線型代数学における内積  |	 
|  <=>  |  コサイン類似性 : 2 つのベクトル間の角度のコサイン |	 

ユークリッド距離は n 次元空間内の 2 つのベクトル間の直線距離を測定し、余弦類似度は 2 つのベクトル間の角度の余弦を測定します。

クエリーの結果は、オリジナルのテキストを返すように実装しています。

https://github.com/yoshioterada/MS-OpenAI-Java-Lib-Sample/blob/d9a43f1e297efae93ce936f6cb5502a17db1e952/src/main/java/com/yoshio3/VectorTest.java#L106-L125

このように、多次元配列のベクトルを利用する事で、関連性の高いドキュメントを見つけ出す事ができます。  
このような、ユースケースがある場合は、どうぞお試しください。


## 参考情報

下記に参考情報を記載します。必要に応じてご覧ください。

* [Azure OpenAI を使用して埋め込みを生成する方法を学習する](https://learn.microsoft.com/ja-jp/azure/cognitive-services/openai/how-to/embeddings?tabs=console)
* [チュートリアル: Azure OpenAI Service の埋め込みとドキュメント検索を確認する](https://learn.microsoft.com/ja-jp/azure/cognitive-services/openai/tutorials/embeddings?tabs=command-line)
* [Azure Database for PostgreSQL を有効にして使用する方法 - フレキシブル サーバーpgvector](https://learn.microsoft.com/en-us/azure/postgresql/flexible-server/how-to-use-pgvector)
* [Azure Database for PostgreSQL で pgvector を使用する場合のパフォーマンスを最適化する方法 - フレキシブル サーバー](https://learn.microsoft.com/ja-jp/azure/postgresql/flexible-server/howto-optimize-performance-pgvector)
