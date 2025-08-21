
# What are beans
https://www.reddit.com/r/SpringBoot/comments/y8xitr/what_beans_exactly_are/

# How to run application
mvn wrapper:wrapper (To generate mvnw. Not sure why tho need to read more)
./mvnw spring-boot:run

# How to run test
./mvnw test

# GRPC set up
- Need protobuf runtime, protobuf compiler, and protobuf auto code-gen plugin
- protobuf runtime: add in pom.xml
- protobuf compiler: sudo apt install protobuf-compiler (`protoc`) or download a binary online
- protobuf code-gen plugin: build plugin in pom.xml
    - tho need to configure path to `protoc`, if not available systemwide
    - need to configure protoSourceRoot (path to .proto files) 
- `mvn compile` should generate the code for you in `target` folder 
- GRPC code explained `https://www.youtube.com/watch?v=zCXN4wj0uPo`

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
