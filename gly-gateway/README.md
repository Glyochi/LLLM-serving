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
    - [Comparisions]
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
    - [ALWAYS] metadata (name, shape, datatype)
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
- RFC 9457 ??

# Structure
gly-gateway/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── gly-gateway/
│   │   │           └── Controller    
│   │   │               ├── MySpringBootAppApplication.java
│   │   │               └── ... (other Java classes)
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
