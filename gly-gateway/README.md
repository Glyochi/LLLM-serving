
# What are beans
- https://www.reddit.com/r/SpringBoot/comments/y8xitr/what_beans_exactly_are/
- Called at startup to initialize a singleton instance, for each @Bean
- Only inject those singletons to other @Bean function or constructors of @Service, @Component, @Controller,...
- Setter/Field inejection also work but need @Autowired

# How to run application
mvn wrapper:wrapper (To generate mvnw. Not sure why tho need to read more)
./mvnw spring-boot:run

# How to run test
./mvnw test

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
