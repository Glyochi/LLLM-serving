# Install Java
sudo apt install default-jdk
sudo apt install openjdk-21-jdk

export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$PATH:$JAVA_HOME/bin


# Install maven
wget https://dlcdn.apache.org/maven/maven-3/3.8.9/binaries/apache-maven-3.8.9-bin.tar.gz 
tar -xvf ...
sudo mv apache-maven-3.8.9/ /opt/maven

export M2_HOME=/opt/maven
export MAVEN_HOME=/opt/maven
export PATH=${M2_HOME}/bin:${PATH}


# How to run application
mvn wrapper:wrapper (To generate mvnw. Not sure why tho need to read more)
./mvnw spring-boot:run


# How to run test
./mvnw test


# What are beans
- `https://www.reddit.com/r/SpringBoot/comments/y8xitr/what_beans_exactly_are/`
- Called at startup to initialize a singleton instance, for each @Bean
- Only inject those singletons to other @Bean function or constructors of @Service, @Component, @Controller,...
- Setter/Field inejection also work but need @Autowired


# Reactor/Webflux reads
- Concurrency and event loop
    - Python `https://www.youtube.com/watch?v=MCs5OvhV9S4`
        - 1 thread runs everything per core doable. Need Redis to sync. 
        - gevent greenlets (virtual thread but in userspace, not OS)
        - Need to monkeypatch OS blocking calls, making OS blocking calls DO yield
    - Reactor `https://www.baeldung.com/spring-webflux-concurrency`
        - 1 thread per cpu core => for maximum utilization. Sync state using DB as well.
        - Using just `Reactor`, there are no `virtual thread`, the `event loop` and the `tasks` are executed on the same main thread.
        - It implies that when `Flux/Mono` are subscribed to, a callback function 
        is registered and the task gave up its time on the main thread for the `event loop` to execute again.
        - when publisher and subscriber idles (no data flows), it DOES yield
        - UNLIKE GEVENT, OS blocking calls DO NOT yield
    - [Comparisons]
        - The difference seems to be how gevent monkey patched all OS blocking functions approach vs Flux/Mono as signifier 
        - It seems that the implementation is very similar to python event loop/gevent
        => cooperative-like pretty much, just different in when it does and does not yield
        => Same issue if run compute heavy or blocking task that arent made aware to the event loop
        ==> Block the whole event loop
- `Flux/Mono` are lazy until subscribed and more things `https://spring.io/blog/2019/03/06/flight-of-the-flux-1-assembly-vs-subscription`
- `Project Loom` (Maybe in the future if needed)
    - From what I've read, it gives access to `virtual threads`, which are tasks that can be
    executed on a set of real threads managed by JVM (preemptively just like a normal OS scheduler)


# GRPC set up
- Need protobuf runtime, protobuf compiler, and protobuf auto code-gen plugin
- Addtionally, os-maven-plugin because the downloaded binary files are platform specific
- protobuf runtime: add in pom.xml
- protobuf compiler: 
    - Automatically download binary online by configuring in pom.xm, `protocArtifact`
    - Responsible for generating plain Java Classes (messages/data obj) 
- protobuf code-gen plugin: build plugin in pom.xml
    - Automatically download binary online by configuring in pom.xm, `pluginArtifact`
    - Responsible for generating service stubs (client and server classes/interfaces/functions)
- `mvn -U clean compile` should generate the code for you in `target` folder 
- GRPC code explained `https://www.youtube.com/watch?v=zCXN4wj0uPo`
- GRPC interface refer to `inference-server` README.md + `postman collection`


# Triton GRPC good to know
- More on `raw_input_contents/raw_output_contents` or `Tensor data` here `https://docs.nvidia.com/deeplearning/triton-inference-server/user-guide/docs/protocol/extension_binary_data.html?utm_source=chatgpt.com`
- `InferInputTensor` includes
    - [ALWAYS] metadata (name, shape [B, C] where `B` for batch and `C` for element per request, datatype)
    - [MAYBE, PREFERABLY NOT] data
- `InferOutputTensor` includes
    - [ALWAYS] metadata (name, shape, datatype)
    - [MAYBE, PREFERABLY NOT] data
- A more efficient way to transfer data is to just write to/read from `raw_input_contents/raw_output_contents` (Performance reason)
    - row-major
    - first 4 bytes are for unsigned int, little-endian, for the length of bytes of data following it
- `triton_final_response` in params in output isn't on by default. Triton don't waste extra message for performance reason.


# Nvim - Lsp - jdtls
- Add `target/generated-sources/protobuf/java` as one of the source directories following this `https://www.baeldung.com/maven-add-src-directories`
- The application was serving fine? I guess it was only for LSP


# Error Handling
- Goal is RFC 9457 compliant `https://www.rfc-editor.org/rfc/rfc9457.html#name-json-schema-for-http-proble`
    - TLDR: every error response body has to have a specific format, and you can return additional data fields for dev consuming the api to use for debuggings
    - Need to set up a documentation server for field `type`. Perhaps `SWAGGER`? 
- Using `jakarta` for contraint annotations + validation
- Implementations of RFC 9457 in `GlobalHandlingError.java`
    - Includes some hacky way of intercepting `jarkata` validation to return a formatted error
    - Support for `custom exceptions`


# Structure
Consumers will never access the 
*any_folder/
├── api/ (Interfaces/Contracts for consumers)
│   ├── obj_interface (What the obj do)
│   └── objRegistry_interface (What the registry do - usually supply obj given some obj's id)
├── core/ (Shared building blocks for case specific implementations)
│   ├── obj_abstract_base (Shared base implementations)
│   └── objRegistry_base (Registry implementation, usually sufficient at this point because don't have variant of objRegistry. Tho it is possible)
└── impl/ (Case specific implementations)
    └── obj_variant (variant implementation)


gly-gateway/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── gly_gateway/
│   │   │           ├── Main.java
│   │   │           ├── config/ (configurations)
│   │   │           ├── exception/
│   │   │           ├── model/ 
│   │   │           ├── repository/
│   │   │           ├── controller/    
│   │   │           │   ├── dto/ (classes/records for Controllers input typing) 
│   │   │           │   └── controllerClass.java    
│   │   │           ├── chat_template/ (LLM models chat template formatting)
│   │   │           │   ├── api/ 
│   │   │           │   └── impl/
│   │   │           └── service/ 
│   │   │               ├── domain/ (Business logic services)
│   │   │               └── triton/ (Triton specific services)
│   │   │                   ├── config/ (Model specific configs)
│   │   │                   ├── api/
│   │   │                   ├── core/
│   │   │                   └── impl/
│   │   ├── resources/
│   │   │   └── application.properties (or application.yml)
│   │   │   └── ... (other resources)
│   │   └── proto/  <- This is where triton .proto files go
│   │       └── grpc_service.proto
│   │       └── model_config.proto
│   │       └── health.proto
│   └── test/
│       └── ... (test related code and resources)
└── target/
    └── ... (build output)
