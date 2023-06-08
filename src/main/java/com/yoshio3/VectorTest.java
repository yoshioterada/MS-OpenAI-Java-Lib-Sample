package com.yoshio3;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.azure.core.credential.AzureKeyCredential;

public class VectorTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(VectorTest.class);

    // Azure OpenAI の API キー
    private String OPENAI_API_KEY = "";
    // Azure OpenAI のインスタンスの URL
    private String OPENAI_URL = "";

    private String POSTGRESQL_JDBC_URL = "";
    private String POSTGRESQL_USER = "";
    private String POSTGRESQL_PASSWORD = "";

    private final static List<String> INPUT_DATA = Arrays.asList(
            "Visual Studio Code for Javaは、Microsoftが提供するオープンソースのコードエディタVisual Studio Code上で、Javaプログラミング言語をサポートするための拡張機能です。Java開発者が効率的にコードを記述、ビルド、デバッグ、テスト、実行できる環境を提供します。Visual Studio Codeは多言語対応の軽量なテキストエディタであり、高い拡張性が特徴です。Java開発者にとっては、Visual Studio Code for Javaが優れた開発環境として利用できるでしょう。Visual Studio Code for Javaは、Java Development Kit (JDK)をインストールすることで使用できます。JDKはJavaプログラムをコンパイル、実行するための基本的なツールセットです。Visual Studio CodeでJavaプロジェクトを開始するには、JDKをインストールした後、Visual Studio Codeの拡張機能マーケットプレイスからJava Extension Packをインストールします。Java Extension Packには、Java言語サポート、デバッグ、テスト、プロジェクト管理などの機能が含まれています。Visual Studio Code for Javaの主な機能は以下のとおりです:1. シンタックスハイライト: Javaのコードに色付けを行い、可読性を向上させます。2. コード補完: コード入力中に可能性のあるコードを提案し、効率的にコードを記述できるようにします。3. コードナビゲーション: クラス、メソッド、変数へのジャンプや、定義の検索が容易になります。4. リファクタリング: コードの構造や名前を変更し、コードの品質を向上させる機能があります。5. デバッグ: ブレークポイントを設定し、ステップ実行や変数の監視などのデバッグ機能を利用できます。6. テスト: JUnitやTestNGなどのテストフレームワークをサポートし、テストの作成、実行、結果の表示ができます。7. プロジェクト管理: MavenやGradleなどのビルドツールをサポートし、プロジェクトの構成や依存関係の管理が行えます。8. Gitの統合: ソースコードのバージョン管理を行うGitとの統合があります。Visual Studio Code for Javaは、開発者の生産性を向上させる機能が豊富であり、Javaプロジェクトの開発に適した環境を提供します。また、Visual Studio Codeの拡張機能マーケットプレイスには、さまざまなJava関連の拡張機能がありますので、必要に応じて追加することができます。これらの拡張機能により、Java開発者はVisual Studio Codeを中心とした統合開発環境として利用できるでしょう。",
            "Azure App Service for Javaは、MicrosoftのクラウドプラットフォームAzure上でJavaアプリケーションをホスト、デプロイ、管理するためのフルマネージドプラットフォームです。Azure App Serviceは、ウェブアプリケーション、モバイルアプリ、API、およびその他のバックエンドアプリケーションの開発と実行に対応しており、Java開発者がアプリケーションを迅速にデプロイし、スケーリングすることができます。また、インフラストラクチャの管理を抽象化することで、開発者はアプリケーションのコードに集中できます。Azure App Service for Javaは、JDK（Java Development Kit）とWebサーバーを組み込んでおり、Tomcat、Jetty、JBoss EAPなどのJavaランタイムをサポートしています。さらに、Azure App Serviceは、CI/CD（継続的インテグレーション/継続的デリバリー）パイプラインの構築、カスタムドメインの設定、SSL証明書の管理、アプリケーションの監視と診断など、Javaアプリケーションのライフサイクル全体をサポートする機能を提供しています。",
            "Azure Container Appsは、MicrosoftのクラウドプラットフォームAzure上で、コンテナベースのアプリケーションをデプロイ、管理、スケーリングするためのフルマネージドサービスです。Azure Container Appsは、マイクロサービスアーキテクチャ、ウェブアプリケーション、バックエンドサービス、およびジョブを実行するアプリケーションに適しています。このサービスは、Kubernetesのようなコンテナオーケストレーションプラットフォームを抽象化し、開発者がインフラストラクチャの管理やスケーリングから解放され、アプリケーションのコードに集中できるようになります。Azure Container Appsは、Dockerコンテナイメージを使用してアプリケーションをデプロイし、自動スケーリング、ローリングアップデート、自動復元などの機能を提供します。また、Azure Container Appsは、プラットフォームに依存しないため、どのプログラミング言語やフレームワークでも対応しています。開発者は、Azureポータル、Azure CLI、またはGitHub ActionsなどのCI/CDパイプラインを使用して、アプリケーションを簡単にデプロイできます。セキュリティ面では、Azure Container Appsは、ネットワーク分離、プライベートエンドポイント、Azure Active Directory（AAD）統合などの機能を提供し、アプリケーションの安全性を保証します。また、アプリケーションの監視と診断をサポートする機能も提供されており、Azure MonitorやAzure Application Insightsといったツールを利用して、アプリケーションのパフォーマンスや問題の特定が可能です。",
            "Azure Cosmos DBは、Microsoftのグローバル分散マルチモデルデータベースサービスであり、低遅延、高可用性、および高スループットを提供します。Cosmos DBは、NoSQLデータベースであり、キー・バリュー、ドキュメント、カラムファミリー、グラフといった複数のデータモデルをサポートしています。このフルマネージドサービスは、様々なアプリケーションに対応し、ウェブ、モバイル、IoT、ゲームなどの開発に利用できます。Azure Cosmos DBの主要機能は、以下の通りです。グローバル分散: データを複数の地理的リージョンに自動的にレプリケートし、高可用性と低遅延を実現します。水平スケーリング: パーティションキーを使用して、データを複数の物理パーティションに分割し、スループットとストレージ容量を柔軟にスケーリングできます。5つの整合性モデル: グローバル分散アプリケーションの整合性要件に応じて、強い整合性からイベンチャル整合性までの5つの整合性モデルを選択できます。リアルタイムアナリティクス: Azure Synapse AnalyticsやAzure Functionsと統合し、リアルタイムのデータ処理と分析を実現します。また、Azure Cosmos DBは、複数のAPIを提供しており、SQL API、MongoDB API、Cassandra API、Gremlin API、Table APIなど、開発者が既に使い慣れたAPIを使用して、アプリケーションを構築できます。データセキュリティ面では、Cosmos DBは暗号化、ネットワーク分離、アクセス制御などの機能を提供し、データの保護を確保します。さらに、Azure MonitorやAzure Application Insightsを使用して、データベースのパフォーマンスや問題の特定を行うことができます。",
            "Azure Kubernetes Service (AKS) は、マイクロソフトが提供する Kubernetes クラスター管理サービスで、コンテナ化されたアプリケーションのデプロイ、スケーリング、運用を簡単にすることができます。AKS は、マネージド型の Kubernetes サービスであり、インフラストラクチャの管理やアップデートなどの面倒な作業を自動化することで、開発者はアプリケーション開発に集中することができます。AKS は、エンタープライズ向けのセキュリティ、監視、運用管理機能を提供し、DevOps のパイプラインとの統合も容易です。また、Azure の他のサービスとの連携も可能で、柔軟なアプリケーション開発を支援します。AKSの主な機能と利点は以下の通りです。1. クラスターのプロビジョニングとスケーリング: AKSは、クラスターのインフラストラクチャ管理を自動化し、必要に応じてノードの追加や削除ができます。これにより、リソースの適切な使用と効率的な運用が可能になります。2. セキュリティとアクセス制御: AKSは、組み込みのAzure Active Directory（AD）統合を提供し、ロールベースのアクセス制御（RBAC）を使用して、クラスターへのアクセスをセキュアに管理できます。3.CI/CDパイプラインとの統合: AKSは、Azure DevOpsやJenkinsなどのCI/CDツールと統合し、アプリケーションのビルド、テスト、デプロイを自動化できます。4. モニタリングとログ: AKSは、Azure MonitorやPrometheus、Grafanaなどの監視ツールと統合し、クラスターのパフォーマンスやリソース使用状況を監視できます。また、ログはAzure Log Analyticsを通じて一元管理できます。5. ネットワーキングとストレージ: AKSは、Azure Virtual Networks（VNet）を使用して、プライベートネットワーク内でクラスターを実行できます。また、Azure StorageやAzure Disksを使用して、永続データストレージを提供します。6. Azureの他のサービスとの連携: AKSは、Azure FunctionsやAzure Cosmos DBなどのAzureサービスと連携し、アプリケーションの機能を拡張できます。これらの機能により、AKSは開発者がアプリケーションの開発に集中し、インフラストラクチャの管理や運用の負担を軽減します。また、エンタープライズ向けのセキュリティ、監視、運用管理機能が提供されているため、安心して利用することができます。",
            "Azure Cognitive Serviceは、マイクロソフトが提供するAI機能を統合したクラウドベースのサービスで、アプリケーションやウェブサイト、ボットに簡単に人工知能機能を追加できます。ディープラーニングや機械学習の専門知識がなくても、APIを介してこれらの機能を利用できます。Azure Cognitive Serviceは、以下の5つのカテゴリに分かれています。Vision: 画像や動画を解析し、顔認識、画像認識、光学式文字認識（OCR）などの機能を提供します。コンピュータービジョン、カスタムビジョン、フェイスAPI、フォーム認識器、ビデオインデクサーなどが含まれます。Speech: 音声認識、音声合成、音声翻訳などの音声関連の機能を提供します。スピーチサービス、音声翻訳、スピーカー認識が含まれます。Language: 自然言語処理（NLP）機能を提供し、テキスト解析、機械翻訳、文書の要約、キーワード抽出などができます。テキスト分析、言語理解（LUIS）、QnAメーカー、翻訳が含まれます。Decision: 意思決定や推奨事項をサポートする機能を提供し、個々のユーザーに適したコンテンツやアクションを推奨できます。個別化、アノマリーディテクター、コンテンツモデレータが含まれます。Web Search: Bingの検索エンジンを利用した、ウェブ検索、画像検索、動画検索、ニュース検索、地図検索などの機能を提供します。Bing Web Search API、Bing Image Search API、Bing Video Search API、Bing News Search API、Bing Maps APIが含まれます。これらのAI機能を組み合わせることで、ユーザーエクスペリエンスを向上させ、アプリケーションやサービスの価値を高めることができます。また、Azure Cognitive Serviceは、プライバシーとセキュリティにも配慮されており、企業や開発者が安心して利用できるようになっています。",
            "Azure Container Instances (ACI)は、マイクロソフトが提供する、コンテナを素早く簡単にデプロイできるサービスです。仮想マシンやオーケストレーションの管理をせずに、アプリケーションのコンテナを実行できるため、開発者はインフラストラクチャの管理にかかる手間を削減できます。ACIは、秒単位の課金が可能で、リソースの使用量に応じてコストが発生します。主な特徴と利点は以下の通りです。シンプルで迅速なデプロイメント: ACIは、Dockerコンテナイメージを使用して、短時間でコンテナをデプロイできます。また、ACIで実行されるコンテナは、DockerコマンドやKubernetesクラスタと互換性があります。オペレーティングシステムの管理が不要: ACIは、ホストOSの管理やアップデートの手間を省き、開発者はアプリケーションの開発と運用に集中できます。シームレスなスケーリング: ACIは、コンテナの数を柔軟にスケーリングでき、負荷に応じてリソースを増減させることができます。セキュリティ: ACIは、Azureのセキュリティ機能を利用して、コンテナとデータの保護を実現します。また、ネットワークのアイソレーションも提供しています。用途に応じた柔軟なリソース割り当て: ACIでは、CPUとメモリを個別に割り当てることができ、アプリケーションの要件に応じてリソースを最適化できます。イベント駆動型のコンテナ実行: ACIは、Azure FunctionsやLogic Appsなどのサービスと統合して、イベント駆動型のコンテナ実行が可能です。これらの特徴により、Azure Container Instancesは、ショートタームのワークロードやバッチ処理、開発・テスト環境など、さまざまなシナリオで効果的に利用できます。また、AKSと組み合わせることで、オーケストレーション機能を活用したコンテナ管理も実現できます。",
            "Azure Data Lake Storage (ADLS)は、マイクロソフトが提供する大規模なデータレイクソリューションで、ペタバイト規模のデータを効率的に格納、処理、分析できるように設計されています。ADLSは、Azure Storageの一部であり、非構造化データや半構造化データ、構造化データを一元的に管理し、ビッグデータ分析や機械学習、リアルタイム分析などの高度な分析を実現します。ADLSの主な特徴と利点は以下の通りです。スケーラビリティ: ADLSは、ペタバイト規模のデータを格納できる高いスケーラビリティを提供し、データの増大に柔軟に対応できます。パフォーマンス: ADLSは、データの大量読み込みや書き込みに最適化されたパフォーマンスを提供し、ビッグデータ処理やリアルタイム分析に適しています。セキュリティとコンプライアンス: ADLSは、データの暗号化、アクセス制御、監査ログなどのセキュリティ機能を提供し、企業のコンプライアンス要件に対応できます。高い互換性: ADLSは、Hadoop Distributed File System (HDFS)と互換性があり、既存のHadoopエコシステムやApache Spark、Azure Databricksなどのビッグデータ分析ツールと連携できます。階層型ストレージ: ADLSは、ホット、クール、アーカイブの3つのストレージ階層を提供し、データのアクセス頻度に応じて最適なコストパフォーマンスを実現します。データ湖とオブジェクトストレージの統合: Azure Data Lake Storage Gen2は、ADLSとAzure Blob Storageを統合し、大規模なデータ湖とオブジェクトストレージの利点を兼ね備えたソリューションを提供します。Azure Data Lake Storageは、企業が大量のデータを効率的に管理し、ビッグデータ分析や機械学習などの高度なデータ処理を実現するための強力なプラットフォームです。これにより、企業はデータの価値を最大限に活用し、ビジネスインサイトの獲得や意思決定の改善を実現できます。",
            "Azure Blob Storageは、マイクロソフトが提供するオブジェクトストレージサービスで、大量の非構造化データを格納・管理できるクラウドベースのソリューションです。テキスト、バイナリデータ、画像、動画、ログファイルなど、あらゆるタイプのデータを安全かつスケーラブルに保存できます。主な特徴と利点は以下の通りです。1. スケーラビリティ: Azure Blob Storageは、ペタバイト規模のデータを格納できる高いスケーラビリティを提供し、データの増大に柔軟に対応できます。2. パフォーマンス: Azure Blob Storageは、データの読み込みや書き込みのパフォーマンスが高く、大量のデータを迅速に処理できます。3. 階層型ストレージ: Azure Blob Storageは、ホット、クール、アーカイブの3つのストレージ階層を提供し、データのアクセス頻度に応じて最適なコストパフォーマンスを実現します。4. セキュリティとコンプライアンス: Azure Blob Storageは、データの暗号化、アクセス制御、監査ログなどのセキュリティ機能を提供し、企業のコンプライアンス要件に対応できます。5. グローバルアクセス: Azure Blob Storageは、マイクロソフトのAzureデータセンターを利用し、世界中から高速かつ安全にデータにアクセスできます。6. 統合と互換性: Azure Blob Storageは、Azure Data Lake Storage Gen2や他のAzureサービス、オンプレミスシステムと連携が可能で、データの一元管理や分析を実現します。Azure Blob Storageは、ウェブアプリケーション、バックアップ、アーカイブ、ビッグデータ分析、IoTデバイスなど、さまざまなシナリオで効果的に利用できます。これにより、企業はデータの価値を最大限に活用し、ビジネスインサイトの獲得や意思決定の改善を実現できます。");

    private OpenAIClient client;

    public VectorTest() throws IOException {
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/application.properties"));
        OPENAI_API_KEY = properties.getProperty("azure.openai.api.key");
        OPENAI_URL = properties.getProperty("azure.openai.url");
        POSTGRESQL_JDBC_URL = properties.getProperty("azure.postgresql.jdbcurl");
        POSTGRESQL_USER = properties.getProperty("azure.postgresql.user");
        POSTGRESQL_PASSWORD = properties.getProperty("azure.postgresql.password");

        client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(OPENAI_API_KEY))
                .endpoint(OPENAI_URL)
                .buildClient();
    }


    public static void main(String[] args) {
        VectorTest test;
        try {
            test = new VectorTest();
            // DB へのインサートは一度だけ実行する
            // test.insertDataToPostgreSQL();

            // DB に登録されているデータに対して、ベクトル検索で類似度ドキュメントを取得する
            test.findMostSimilarString("Azure Blob について教えてください");
        } catch (IOException e) {
            LOGGER.error("Error : ", e);
        }
    }

    /**
     * テキスト・エンべディングの検証サンプル (text-embedding-ada-002)
     */
    private List<Double> invokeTextEmbedding(String originalText) {
        EmbeddingsOptions embeddingsOptions = new EmbeddingsOptions(Arrays.asList(originalText));
        var result = client.getEmbeddings("text-embedding-ada-002", embeddingsOptions);
        var embedding = result.getData().stream().findFirst().get().getEmbedding();
        return embedding;
    }

    private void insertDataToPostgreSQL() {
        try {
            var connection = DriverManager.getConnection(POSTGRESQL_JDBC_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD);
            var insertSql = "INSERT INTO TBL_VECTOR_TEST (id, embedding, origntext) VALUES (?, ?::vector, ?)";

            for (String originText : INPUT_DATA) {
                // テキスト・エンべディングを呼び出しベクター配列を取得
                List<Double> embedding = invokeTextEmbedding(originText);
                // 短時間に大量のリクエストを送るとエラーになるため、10秒間スリープ
                TimeUnit.SECONDS.sleep(10);

                PreparedStatement insertStatement = connection.prepareStatement(insertSql);
                insertStatement.setObject(1, UUID.randomUUID());
                insertStatement.setArray(2, connection.createArrayOf("double", embedding.toArray()));
                insertStatement.setString(3, originText);
                insertStatement.executeUpdate();
            }
            connection.close();
        } catch (SQLException | InterruptedException e) {
            LOGGER.error("Connection failure." + e.getMessage());
        }
    }

    public void findMostSimilarString(String data) {
        try (Connection connection = DriverManager.getConnection(POSTGRESQL_JDBC_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD)) {
            // ユーザが検索したい文字列をテキスト・エンべディングを呼び出しベクター配列を作成
            List<Double> embedding = invokeTextEmbedding(data);
            String array = embedding.toString();
            LOGGER.info("Embedding: \n" + array);

            // ベクター配列で検索 (ユーザーが入力した文字列と最も近い文字列を検索)
            String querySql = "SELECT origntext FROM TBL_VECTOR_TEST ORDER BY embedding <-> '" + array + "' LIMIT 1;";
            PreparedStatement queryStatement = connection.prepareStatement(querySql);
            ResultSet resultSet = queryStatement.executeQuery();
            while (resultSet.next()) {
                String origntext = resultSet.getString("origntext");
                LOGGER.info("Origntext: " + origntext);
            }
        } catch (SQLException e) {
            LOGGER.error("Connection failure." + e.getMessage());
        }

    }
}
