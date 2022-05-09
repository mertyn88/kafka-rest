Lific-Liam Custom
================
라이픽-검색에 관한 전용 헤더 타입을 만들고,  
스키마레지스트리를 통한 데이터 변환과 Validate를 수행하는 기능을 추가한다.  

Add Custom history (2022.05.09)
------------
<u>java/io/confluent/kafkarest/Versions.java</u>
* Content-type 공통 헤더로 변경 ( 요청 건 )
  * 기존 `application/vnd.kafka.lific.search.v2+json`에서 `application/json;charset=UTF-8`로 변경

<u>java/io/confluent/kafkarest/controllers/ProduceGenericControllerImpl.java</u>  
>예외 발생시 Response가 성공시와 같은 형태로 오지 않는다. 확인 결과, Producer의 send시 예외가 발생하였을 경우 로직이 수행되질 않는다.  
Producer에서 Send이전, `Serialize` 체크 단계에서 예외가 발생하여 CallBack까지 가질 않는듯 하다. 해서 예외처리 로직을 변경한다.  
* try - catch로 발생할 수 있는 예외 직접 Hadling (`SerializationException`, `ExecutionException`, `InterruptedException`)
* catch 단계에서 error loging 후 `CompletableFuture completeExceptionally` 처리  

```bash
# 정상 스키마 [{"value": {"id": "test","date": "test"}}]}
curl "http://localhost:18094/topics/test-hadoop-hdfs" \
  -X POST \
  -d '{"records": [{"value": {"i": "test","date": "test"}}]}' \
  -H "Content-Type: application/json;charset=UTF-8" 
```

AS-IS
```json
{
  "error_code": 40801,
  "message": "Error serializing Avro message"
}
```
TO-BE
```json
{
  "offsets": [
    {
      "partition": null,
      "offset": null,
      "error_code": 50002,
      "error": "Error serializing Avro message"
    }
  ],
  "key_schema_id": null,
  "value_schema_id": null
}
```


Add Custom history (2022.04.20)
------------
<u>java/io/confluent/kafkarest/DefaultKafkaRestContext.java</u>
* 생성자에 `ProducerGenericPool` 추가
* Override 관련 메소드 추가
  * 여기서 `Key/Value Serialize` 설정이 추가되어야 한다.

<u>java/io/confluent/kafkarest/KafkaRestContext.java</u>
* 인터페이스에 메소드 `getGenericProducer()`를 정의한다.

<u>java/io/confluent/kafkarest/ProducerGenericPool.java</u>
* **genericProducer**를 정의한다. 이때 타입은 `<String, GenericRecord>` 이다.
* 기존 **Producer**는 `<byte[], byte[]>`로 정의되어 있다.

<u>java/io/confluent/kafkarest/backends/kafka/KafkaModule.java</u>
* **ProducerGenericFactory** 팩토리를 구성한다.
* 구성된 **ProducerGenericFactory**를 `bindFactory` 한다.

<u>java/io/confluent/kafkarest/controllers/ProduceGenericController.java</u>
* 프로듀싱할 커스텀 인터페이스 클래스를 정의한다.
  * 여기서 정의된 메소드는 기존 **ProducerController**와 동일한 **produce**
  * 단, 파라미터타입은 `(String, GenericRecord)`

<u>java/io/confluent/kafkarest/controllers/ProduceGenericControllerImpl.java</u>
* 인터페이스의 메소드를 구현한다.
  * 이때 전송하는 방식은 shover의 전송방식과 동일하게 한다.
  * 해당 코드에서 Producer의 `value타입이 avro`로 되어 있고, 전송 객체가 `GenericRecord`이면, 스키마 비교가 이루어진다

<u>java/io/confluent/kafkarest/controllers/ControllersModule.java</u>
* 모듈에 인터페이스의 구현체를 bind한다.
  * 이때 **ProducerGenericController**는 **ProducerGenericControllerImpl** (코드 자체가 그런데 1:1 대응으로 보인다)

<u>java/io/confluent/kafkarest/controllers/SchemaManager.java</u>
* 스키마레지스트리의 객체를 가지고 있는 SchemaManagerIml 클래스가, protected로 되어 있으므로 **객체 변환을 통해 다른 메소드를 사용할 수 없다. 해서 인터페이스에 메소드를 추가 정의**한다.
* 또는 SchemaManagerImpl의 `접근지시 레벨을 Public으로 변경`해도 된다. (여기서는 설계를 깨드리기 싫어서 인터페이스 메소드 추가)

<u>java/io/confluent/kafkarest/controllers/SchemaManagerImpl.java</u>
* 스키마레지스트리의 subject의 정보를 가지고 있는 Map을 선언한다. 
* 일치되는 토픽명과 조회가 가능하며 스키마레지스트리 조회시 "-value" 형태로 가져온다. (**TopicNameStrategy**)
* 초기에는 Map의 정보가 없을 때, 최초 적재 작업을 한다.
  * 이후에는 스키마 레지스트리가 변동되어도 업데이트가 이루어지지 않으므로, **리프레쉬 할 수 있는 메소드를 구현**한다.
  * 이때 메소드의 반환값은 `CompletableFuture`이다. (추후 컨트롤러의 반환값 통일)

<u>java/io/confluent/kafkarest/resources/v2/AbstractProduceAction.java</u>
* 생성자에 빈으로 등록한 **ProduceGenericController**을 추가한다.
* 실질적인 데이터 컨버팅이 되는 메소드를 작성한다. ( `produceGenericSchema` )
  * **Work 1.** 스키마레지스트리에서 가져온 subject Map에서 스키마 정보를 가져온다.
  * **Work 2.** 실제 입력된 데이터와 스키마 정보를 비교하여 `GenericRecord` 데이터를 구성한다. (이때 입력 데이터는 `List` 형태로 받으므로 반환 데이터도 `List`로 한다.)
  * **Work 3.** Producer<String, GenericRecord>으로 구성한 객체로 전송을 하며 이때 `Avro 데이터로 Serialize` 하여 전송한다.
    * __*해당 부분에서 만들어진 GenericRecord의 데이터가 스키마레지스트리에 정의된 채로 만들어 지지 않으면 예외를 일으킨다.*__

<u>java/io/confluent/kafkarest/resources/v2/ProduceToTopicAction.java</u>
* 클라이언트가 구현된 로직에 적용되도록 설정한다.
* 이때 Rest API의 content-type을 통해서 여러 조건이 가능한데 추가된 다음의 값을 설정한다.
  * **HEADERS** -> `application/vnd.kafka.lific.search.v2+json`
  * **METHOD** -> `POST`
  * **URL** -> `/topics/dev-data-benefit-coupon-consumer`
  * **DATA** -> `{ "records": [ { "value": { "[FIELD]": [VALUE] } } ] }`

<u>java/io/confluent/kafkarest/resources/v2/SchemaResource.java</u>
* 운영중인 상태에서 스키마 레지스트리가 변경될때, 해당 맵의 스키마 정보를 `refresh`할 수 있는 리소스를 추가한다.
* refresh 할때, 맵의 key값은 subject의 값인데 해당 데이터를 List형태로 적용중인 스키마 정보를 보여준다.

<u>java/io/confluent/kafkarest/resources/v2/V2ResourcesFeature.java</u>
* 클라이언트가 접속할 수 있는 URL을 등록한다. ( 해당 작업이 없으면 접근이 안됨 )

---

Kafka REST Proxy
================

The Kafka REST Proxy provides a RESTful interface to a Kafka cluster. It makes
it easy to produce and consume messages, view the state of the cluster, and
perform administrative actions without using the native Kafka protocol or
clients. Examples of use cases include reporting data to Kafka from any
frontend app built in any language, ingesting messages into a stream processing
framework that doesn't yet support Kafka, and scripting administrative actions.

Installation
------------

You can download prebuilt versions of the Kafka REST Proxy as part of the
[Confluent Platform](http://confluent.io/downloads/). 

You can read our full [installation instructions](http://docs.confluent.io/current/installation.html#installation) and the complete  [documentation](http://docs.confluent.io/current/kafka-rest/docs/)


To install from source, follow the instructions in the Development section below.

Deployment
----------

The REST proxy includes a built-in Jetty server and can be deployed after
being configured to connect to an existing Kafka cluster.

Running ``mvn clean package`` runs all 3 of its assembly targets.
- The ``development`` target assembles all necessary dependencies in a ``kafka-rest/target``
  subfolder without packaging them in a distributable format. The wrapper scripts
  ``bin/kafka-rest-start`` and ``bin/kafka-rest-stop`` can then be used to start and stop the
  service.
- The ``package`` target is meant to be used in shared dependency environments and omits some
  dependencies expected to be provided externally. It assembles the other dependencies in a
  ``kafka-rest/target`` subfolder as well as in distributable archives. The wrapper scripts
  ``bin/kafka-rest-start`` and ``bin/kafka-rest-stop`` can then be used to start and stop the
  service.
- The ``standalone`` target packages all necessary dependencies as a distributable JAR that can
  be run as standard (``java -jar $base-dir/kafka-rest/target/kafka-rest-X.Y.Z-standalone.jar``).

Quickstart
----------

The following assumes you have Kafka  and an instance of
the REST Proxy running using the default settings and some topics already created.

```bash
    # Get a list of topics
    $ curl "http://localhost:8082/topics"
      
      ["__consumer_offsets","jsontest"]

    # Get info about one topic
    $ curl "http://localhost:8082/topics/jsontest"
    
      {"name":"jsontest","configs":{},"partitions":[{"partition":0,"leader":0,"replicas":[{"broker":0,"leader":true,"in_sync":true}]}]}

    # Produce a message with JSON data
    $ curl -X POST -H "Content-Type: application/vnd.kafka.json.v2+json" \
          --data '{"records":[{"value":{"name": "testUser"}}]}' \
          "http://localhost:8082/topics/jsontest"
          
      {"offsets":[{"partition":0,"offset":3,"error_code":null,"error":null}],"key_schema_id":null,"value_schema_id":null}

    # Create a consumer for JSON data, starting at the beginning of the topic's
    # log. The consumer group is called "my_json_consumer" and the instance is "my_consumer_instance".
    
    $ curl -X POST -H "Content-Type: application/vnd.kafka.v2+json" -H "Accept: application/vnd.kafka.v2+json" \
    --data '{"name": "my_consumer_instance", "format": "json", "auto.offset.reset": "earliest"}' \
    http://localhost:8082/consumers/my_json_consumer
          
      {"instance_id":"my_consumer_instance","base_uri":"http://localhost:8082/consumers/my_json_consumer/instances/my_consumer_instance"}
      
    # Subscribe the consumer to a topic
    
    $ curl -X POST -H "Content-Type: application/vnd.kafka.v2+json" --data '{"topics":["jsontest"]}' \
    http://localhost:8082/consumers/my_json_consumer/instances/my_consumer_instance/subscription
    # No content in response
      
    # Then consume some data from a topic using the base URL in the first response.

    $ curl -X GET -H "Accept: application/vnd.kafka.json.v2+json" \
    http://localhost:8082/consumers/my_json_consumer/instances/my_consumer_instance/records
      
      [{"key":null,"value":{"name":"testUser"},"partition":0,"offset":3,"topic":"jsontest"}]
   
    # Finally, close the consumer with a DELETE to make it leave the group and clean up
    # its resources.  
    
    $ curl -X DELETE -H "Accept: application/vnd.kafka.v2+json" \
          http://localhost:8082/consumers/my_json_consumer/instances/my_consumer_instance
      # No content in response
```

Development
-----------

To build a development version, you may need development versions of
[common](https://github.com/confluentinc/common),
[rest-utils](https://github.com/confluentinc/rest-utils), and
[schema-registry](https://github.com/confluentinc/schema-registry).  After
installing these, you can build the Kafka REST Proxy
with Maven. All the standard lifecycle phases work.

You can avoid building development versions of dependencies
by building on the latest (or earlier) release tag, or `<release>-post` branch,
which will reference dependencies available pre-built from the
[public repository](http://packages.confluent.io/maven/).  For example, branch
`6.1.1-post` can be used as a base for patches for this version.

Contribute
----------

- Source Code: https://github.com/confluentinc/kafka-rest
- Issue Tracker: https://github.com/confluentinc/kafka-rest/issues

License
-------

This project is licensed under the [Confluent Community License](LICENSE).
