# Goal
[Not finished watching yet](https://www.youtube.com/watch?v=XpunFFS-n8I)
[Future refactor if needed](https://github.com/a-mountain/realworld-spring-webflux/tree/master)

# Install Java
```
sudo apt install default-jdk
sudo apt install openjdk-21-jdk

export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$PATH:$JAVA_HOME/bin
```


# Install maven
```
wget https://dlcdn.apache.org/maven/maven-3/3.8.9/binaries/apache-maven-3.8.9-bin.tar.gz 
tar -xvf ...
sudo mv apache-maven-3.8.9/ /opt/maven

export M2_HOME=/opt/maven
export MAVEN_HOME=/opt/maven
export PATH=${M2_HOME}/bin:${PATH}
```

# See dependencies tree
```
mvn dependency:tree -Dverbos
ls ~/.m2/repository
```


# How to run application
```
mvn wrapper:wrapper (To generate mvnw. Not sure why tho need to read more)
./mvnw spring-boot:run
```


# How to run test
```
./mvnw test
```


# What are beans
- [Beans](https://www.reddit.com/r/SpringBoot/comments/y8xitr/what_beans_exactly_are/)
- Called at startup to initialize a singleton instance, for each @Bean
- Only inject those singletons to other @Bean function or constructors of @Service, @Component, @Controller,...
- Setter and Field injection (need @Autowired) works

# Reactor/Webflux
## Basic building blocks
- Boiled down to 4 types of interface in [`reactivestreams` package](https://github.com/reactive-streams/reactive-streams-jvm/tree/master/api/src/main/java/org/reactivestreams)
    - `Publisher`
        - subscribe (takes in `Subscriber`)
    - `Subscriber`
        - onSubscribe (takes in `Subscription`)
        - onNext (Any data type)
        - onError (Throwable)
        - onComplete
    - `Subscription`
        - request (number of requests)
        - cancel
    - `Processor`
        - extends `Subscriber` and `Publisher`
- Interfaces and Classes in [`reactor-core` package](https://github.com/reactor/reactor-core/tree/2accddaef1fde04f3431b079a143da791810389c/reactor-core/src/main/java/reactor/core)
    - `CoreSubscriber`
        - extends `Subscriber`
    - `CorePublisher` 
        - extends `Publisher`
    - `InnerConsumer`
        - extends `CoreSubscriber`, which extends `Subscriber`
    - `InnerProducer`
        - extends `Supscription`
    - `InnerOperator`
        - extends InnerConsumer and InnerProducer => `Subscriber` and `Subscription`
- Java Interface
    - `Runnable`, fundamental interface for multithreading in Java. It defines a task that can be executed by a thread. Only has a `run` function
- Threading 
    - Type of threads
        - Cooperative threads (gevent/python) 
            - Thread can yield itself
            - `Scheduler` cannot force the thread to yield
        - Preemptive threads (OS level)
            - Thread can make syscall to put itself in WAITING queue => Don't take CPU time
            - `Scheduler` can force the thread to yield, back to READY queue
    - All threads here are Java threads, ultimately scheduled by the OS
    - `Netty event-loop`
        - A fixed-size (~ cpu cores) pool of threads, meant for handling socket I/O and dispatch handlers (endpoints)
        - These threads being blocked => stalled socket I/O handling for incoming requests
        - `reactor-http-nio-*`
    - `boundedElastic/elastic`: 
        - A bounded or unbounded pool of threads, meant for handling blocking tasks (disk I/O, http,...)
        - Shared queue among all workers. Tasks are put on the queue before handed off to a worker
        - Automatically
            - Spin up more thread (up to a cap) to handle more blocking calls concurrently
            - Time out and kill idling thread
        - Giving these threads compute intensive tasks will make these threads compete with event-loop threads for CPU time
        - `boundedElastic-*` or `elastic-*`
    - `parallel`
        - A fixed-size pool (~ cpu cores) of threads, meant for short-lived compute intensive task 
        - One worker <-> one thread, run start to finish
        - If it's blocking, its aight really. Just that its better to use these for compute because we could handle blocking calls much better by creating more threads and scale out
        - `parallel-*`

### Implementations of building blocks
- `Flux` and it's variants (FluxJust, FluxMap,...)
    - extends `CorePublisher` -> `Publisher`
    - when chaining operation (map, flatMap,...), returns that last Flux variant, not the root Flux
    - For source nodes (FluxJust, FluxFrom,...):
        - Extends `Flux`, which extends `CorePublisher` and then `Publisher`
        - subscribe
            - Takes in `CoreSubscriber`, which extends `Subscriber`
            - Different implementation. Instead of passing up the subscribe call, it starts the onSubscribe call downstream
    - For middle nodes (FluxMap, FluxFlatMap,...):
        - Extends `InternalFluxOperator`, which extends `FluxOperator`, which extends `Flux` -> `Publisher` still  
        - Unlike source nodes, the middle/operator nodes also have the reference of its upstream/parent (Tks to `FluxOperator`). 
        This is important for upstream traversal for later subscriber/subscription building
        - subscribe
            - Takes in private extended class of `InnerOperator` for that Flux variant (i.e. MapSubscriber for FluxMap), which means `Subscriber` and `Subscription`
            - This seems to be pretty convenient for the flow of onSubscribe and request calls later as the object can be reused
            - The implementation is in `InternalFluxOperator`, and it calls `subcribeOrReturn`
        - subscribeOrReturn
            - This is where it differs among all middle nodes implementations
            - Also where the private class extending `InnerOperator` is declared and instantiated
    - Micrometer/Observabitlity related stuffs
        - `FluxTap` in additional in propagating the calls upward and downward, it also signal the `signalListener` to do stuff
        - Otherwise similar to `middel nodes`
- [SOURCE] Codes that help getting a high level overview
    - [Flux](https://github.com/reactor/reactor-core/blob/2accddaef1fde04f3431b079a143da791810389c/reactor-core/src/main/java/reactor/core/publisher/Flux.java#L6629)
        - Looks at the subscribe function that wrap the Consumer function around LambdaSubscriber
        - Looks at subscribeWith
        - Looks at flatMap/map
    - [FluxJust](https://github.com/reactor/reactor-core/blob/main/reactor-core/src/main/java/reactor/core/publisher/FluxJust.java)
        - Look at subscribe
    - [InternalFluxOperator](https://github.com/reactor/reactor-core/blob/2accddaef1fde04f3431b079a143da791810389c/reactor-core/src/main/java/reactor/core/publisher/InternalFluxOperator.java)
        - Look at subscribe, its calling subscribeOrReturn
        - If subscribeOrReturn returns null, it will not do anything and exit
        - If subscribeOrReturn returns not null (`CoreSubscriber` object), it will make its source call subscribe on the `CoreSubscriber` (continue upstream subscribe call)
    - [FluxMap](https://github.com/reactor/reactor-core/blob/2accddaef1fde04f3431b079a143da791810389c/reactor-core/src/main/java/reactor/core/publisher/FluxMap.java)
        - Look at subscribeOrReturn, and how its return MapSubscribe
        - Look at MapSubscribe
    - [LambdaSubscriber](https://github.com/reactor/reactor-core/blob/main/reactor-core/src/main/java/reactor/core/publisher/LambdaSubscriber.java)
    - [ScalarSubscription](https://github.com/reactor/reactor-core/blob/main/reactor-core/src/main/java/reactor/core/publisher/Operators.java#L2513)
    - [FluxTap](https://github.com/reactor/reactor-core/blob/2accddaef1fde04f3431b079a143da791810389c/reactor-core/src/main/java/reactor/core/publisher/FluxTap.java#L46)
        - Look at subscribe
            - Create a `SignalListener` from `SignalListenerFactory`
            - Trigger `doFirst` call for the `SignalListener`
            - Then returns a `TapSubscriber` like normal
    - [TapSubscriber](https://github.com/reactor/reactor-core/blob/2accddaef1fde04f3431b079a143da791810389c/reactor-core/src/main/java/reactor/core/publisher/FluxTap.java#L102)
        - Just another `InnerOperator` but it also signal to the `signalListener` BEFORE passing the call to downstream/upstream
    - `SignalListener` example [MicrometerObservationListener](https://github.com/reactor/reactor-core/blob/main/reactor-core-micrometer/src/main/java/reactor/core/observability/micrometer/MicrometerObservationListener.java#L47)
        - Looks at `doFirst` and `onComplete`
        - [NOTES] Because the when `doFirst` is called, it starts the micrometer time => The last `tap` will contains all the earlier `tap`

### Implementations of building blocks, multi threaded cases
- There are two ways of doing threading 
    - `subscribeOn` call 
        - which returns `SubscribeOnSubscriber`
        - This brings both upstream and downstream to another thread 
    - `publishOn` call
        - which returns `PublishOnSubscriber`
        - This only brings downstream to another thread
- [SOURCE] Codes that help getting a high level overview
    - [Schedulers](https://github.com/reactor/reactor-core/blob/2accddaef1fde04f3431b079a143da791810389c/reactor-core/src/main/java/reactor/core/scheduler/Schedulers.java#L277)
        - Looks at boundedElastic, elastic, parallel. It caches these object => Singleton
    - [BoundedElasticScheduler](https://github.com/reactor/reactor-core/blob/2accddaef1fde04f3431b079a143da791810389c/reactor-core/src/main/java/reactor/core/scheduler/BoundedElasticScheduler.java)
        - Implements Scheduler
        - this.state is of type SchedulerState<BoundedServices>
        - Looks at createWorker and pick 
            - createWoker triggers state.currentResources.pick()
            - Or [BoundedServices.pick()](https://github.com/reactor/reactor-core/blob/2accddaef1fde04f3431b079a143da791810389c/reactor-core/src/main/java/reactor/core/scheduler/BoundedElasticScheduler.java#L613)
            => On a high level, see what thread/worker is avaible and reuse them if possible, if not make new one (depending on Scheduler implementations)
        => All we care about is that they can use `Worker` to call `Worker.schedule` on a `Runnable` aka `SubscribeOnSubscriber` or `PublishOnSubscriber`
    - [Flux](https://github.com/reactor/reactor-core/blob/2accddaef1fde04f3431b079a143da791810389c/reactor-core/src/main/java/reactor/core/publisher/Flux.java#L6629)
        - Looks at subscribeOn, which returns `FluxSubscribeOn`
        - Looks at publishOn, which returns `FluxPublishOn`
    - [FluxSubscribeOn](https://github.com/reactor/reactor-core/blob/2accddaef1fde04f3431b079a143da791810389c/reactor-core/src/main/java/reactor/core/publisher/FluxSubscribeOn.java#L53)
        - Extends `InternalFluxOperator`, which extends `FluxOperator`, which extends `Flux` -> `Publisher` still  
        - Looks at [`SubscribeOnSubscriber` class](https://github.com/reactor/reactor-core/blob/2accddaef1fde04f3431b079a143da791810389c/reactor-core/src/main/java/reactor/core/publisher/FluxSubscribeOn.java#L80)
            - which implements `InnerOperator` and `Runnable`
            - Looks at run, request, onSubscribe function
        - Looks at subscribeOrReturn 
            - it takes the scheduler, create a Worker object, then use that worker to create `SubcribeOnSubscriber`
            - it also start the onSubscribe chain call downstream, on the same initial thread
            - it also starts the subscribe chain call upstream (via `SubscribeOnSubscriber` run function), on the Worker thread
            - it returns null to disable `InternalFluxOperator` upstream subscribe call
    - [FluxPublishOn](https://github.com/reactor/reactor-core/blob/2accddaef1fde04f3431b079a143da791810389c/reactor-core/src/main/java/reactor/core/publisher/FluxPublishOn.java)
        - Extends `InternalFluxOperator`, which extends `FluxOperator`, which extends `Flux` -> `Publisher` still  
        - Looks at [PublishOnSubscriber class](https://github.com/reactor/reactor-core/blob/2accddaef1fde04f3431b079a143da791810389c/reactor-core/src/main/java/reactor/core/publisher/FluxPublishOn.java#L110)
            - which implements `InnerOperator` and `Runnable`
            - Looks at run, request, onSubscribe function
        - Looks at subscribeOrReturn
            - it takes the scheduler, create a Worker object, then use that worker to create `PublishOnSubscriber`
            - it returns `PublishOnSubscriber` for `InternalFluxOperator` to continue the upstream subscribe call


## Examples
### Simple single threaded example
- Say we have declare a reactive stream like so `Flux.just(1, 2, 3).op_1().op_2()`, and says all operations op_1 and op_2 are flatMap operation.
    - At this point, this is a `cold publisher`
    - We have a bunch of publishers linked together
    - `Flux.just()` gives us publisher FluxJust `P_0`, a source node
    - `P_0.op_1()` gives us publisher FluxFlatMap `P_1`, an operator node, and has reference to `P_0`
    - `P_1.op_2()` gives us publisher FluxFlatMap `P_2`, an operator node, and has refercne to `P_1`

- This is what it looks like [`P_0` <- `P_1` <- `P_2`]
    - Now we add a subscribe call at the end `Flux.just(1, 2, 3).op_1().op_2().subscribe(x -> sysout(x))` 
    - This subscribe function takes in a Lambda function that return void (Consumer type), it will be wrapped around by a `LambdaSubscriber`, lets call it `Sb_3`
    - `Sb_3` is only a subscriber, and it is not a publisher

- This is what it looks like now [`P_0` <- `P_1` <- `P_2`, and lingering `Sb_3`] 
    - We are still in `P_2` subscribe call. `P_2` now takes `Sb_3` and `op_2`, bundle them together in a `MapSubscriber`, lets call it `Sbp_2` and its both a `Subscriber` and `Subscription`

- This is what it looks like now [`P_0` <- `P_1` <- `P_2`, and `Sbp_2` -> `Sb_3`] 
    - `P_2` now trigger its source `P_1` to call subscribe on `Sbp_2`
    - `P_1` now takes `Sbp_2` and `op_1`, bundle them together in a `MapSubscriber`, lets call it `Sbp_1` and its both a `Subscriber` and `Subscription`

- This is what it looks like now [`P_0` <- `P_1` <- `P_2`, and `Sbp_1` -> `Sbp_2` -> `Sb_3`] 
    - `P_1` now trigger its source `P_0` to call subscribe on `Sbp_1`
    - Because `P_0` is a source node, it doesn't propagate the subscribe call upstream anymore.
    - `P_0` now takes `Sbp_1` to create a `ScalarSubscription`, which extends `InnerProducer`, which is a `Subscription`, lets call it `Sp_0`

- This is what it looks like now [`P_0` <- `P_1` <- `P_2`, and `Sp_0` -> `Sbp_1` -> `Sbp_2` -> `Sb_3`] 
    - `P_0` then trigger `Sbp_1` to call onSubscribe on `Sp_0`, which makes `Sbp_1` save the reference of `Sp_0`
    - `Sbp_1` then trigger `Sbp_2` to call onSubscribe on `Sbp_1`, which makes `Sbp_2` save the reference of `Sbp_1`
    - `Sbp_2` then trigger `Sb_3` to call onSubscribe on `Sbp_2`, which makes `Sb_3` save the reference of `Sbp_2`

- This is what it looks like now [`P_0` <- `P_1` <- `P_2`, and `Sp_0` <-> `Sbp_1` <-> `Sbp_2` <-> `Sb_3`]
    - `Sb_3`, which is a `LambdaSubscriber` has a different implementation, which makes it trigger `Sbp_2` to call request to start pulling data
    - `Sbp_2` then trigger `Sbp_1` to call request
    - `Sbp_1` then trigger `Sp_0` to call request
    - `Sp_0` is a `ScalarSubscription`, which has a different implementation. It starts feeding `Sbp_1` onNext with values ([NOTES] Still dont understand how this loop works. Need to comeback later)
    - At this point, this is a `hot publisher`
    - `Sbp_1` onNext takes the input, perform op_1, then feed it to `Sbp_2` onNext
    - `Sbp_2` onNext takes the input, perform op_2, then feed it to `Sb_3` onNext
    - `Sb_3` onNext takes the input, feed it to the consumer function it wrapped around

- And when `Sp_0` is done, it will send the onComplete signal downstream like onNext



### Multi threaded, subscribeOn 
- Say we have declare a reactive stream like so `Flux.just(1, 2, 3).op_1().subscribeOn(Schedulers.boundedElastic())`, and op_1 is a flatMap operation
    - At this point, this is a `cold publisher`
    - We have a bunch of publishers linked together
    - `Flux.just()` gives us publisher FluxJust `P_0`, a source node
    - `P_0.op_1()` gives us publisher FluxFlatMap `P_1`, an operator node, and has reference to `P_0`
    - `P_1.subscribeOn(Schedulers.boundedElastic())` gives us publisher FluxSubscribeOn `PSO_2`, an operator node, and has reference to `P_1`
    - All these happen in the thread `t-main`

- This is what it looks like [`P_0` <- `P_1` <- `PSO_2`]
    - Now we add a subscribe call at the end `Flux.just(1, 2, 3).op_1().subscribeOn(Schedulers.boundedElastic()).subsribe(lambda)` 
    - This subscribe function takes in a Lambda function that return void (Consumer type), it will be wrapped around by a `LambdaSubscriber`, lets call it `Sb_3`
    - `Sb_3` is only a subscriber, and it is not a publisher
    - Still in `t-main`

- This is what it looks like now [`P_0` <- `P_1` <- `PSO_2`, and lingering `Sb_3`] 
    - We are still in `PSO_2` subscribe call. `PSO_2` now create a new worker/thread to execute, let's call it `t-elastic-1` 
    - `PSO_2` now takes it's source `P_1`, `Sb_3`, and worker for `t-elastic-1`, bundle them together in a `SubscribeOnSubscriber`, lets call it `SOS_2` and its both a `Subscriber` and `Subscription`

- This is what it looks like now [`P_0` <- `P_1` <- `PSO_2`, and  `SOS_2` -> `Sb_3`] 
    - Now there's a thread divergent in `PSO_2` subscribe call
        - In `t-main`
            - `PSO_2` triggers a downstream onSubscribe call on `Sb_3`
            - `Sb_3` will then save the reference of `SOS_2`
            - And because `Sb_3` is a `LambdaSubscriber` leaf node, it then triggers `SOS_2` to call request upstream to start pulling data
            - `SOS_2` call request, and because its implementation is a bit different, it either
                - queue the request call and wait if `SOS_2` has not called onSubscribe yet (no reference of the upstream Supcription)
                - trigger the request call upstream in `t-elastic-1` by accessing its future `Sbp_1` (Not created yet) and trigger a request call
        - In `t-elastic-1`
            - `PSO_2` trigger `SOS_2.run()` call in `t-elastic-1` by doing `Worker.schedule(SOS_2)`
            - The `SOS_2.run()` function makes `PSO_2`'s source `P_1` call subscribe on `SOS_2` on another thread
            - This will makes all the upstream subscribe calls stay in `t-elastic-1`

- This is what it looks like now [`P_0` <- `P_1` <- `PSO_2`, and  `SOS_2` <-> `Sb_3`] 
    - In `t-elastic-1`
        - `P_1` now takes `SOS_2` and `op_1`, bundle them together in a `MapSubscriber`, lets call it `Sbp_1` and its both a `Subscriber` and `Subscription`
        - `P_1` now trigger its source `P_0` to call subscribe on `Sbp_1`
        - Because `P_0` is a source node, it doesn't propagate the subscribe call upstream anymore.
        - `P_0` now takes `Sbp_1` to create a `ScalarSubscription`, which extends `InnerProducer`, which is a `Subscription`, lets call it `Sp_0`
        - `P_0` then trigger `Sbp_1` to call onSubscribe on `Sp_0`, which makes `Sbp_1` save the reference of `Sp_0`
        - `Sbp_1` then trigger `SOS_2` to call onSubscribe on `Sbp_1`, which makes `Sbp_2` save the reference of `Sbp_1`

- This is what it looks like now [`P_0` <- `P_1` <- `PSO_2`, and `Sp_0` <-> `Sbp_1` -> `SOS_2` <-> `Sb_3`] 
    - In `t-elastic-1`
        - During `SOS_2` onSubscribe call, it will not propagate the onSubscribe downstream any more because `PSO_2` already trigger that downstream call 
        - It either
            - Don't do anything because there's no request coming from downstream yet
            - Or there's requests in queued up, so it will trigger an upstream request call (still in `t-elastic-1`)
        - The request propagate upstream until it reachs `Sp_0`, which will start the downstream onNext call (still in `t-elastic-1`)
        - These onNext calls go until it hits `Sb_3` at the end, where the final values will be consumed

=> `SubscribeOnSubscriber` is kinda like an upstream router, where it makes all the upstream calls (subscribe, request,...) happens in another thread/Worker.
As a result, the downstream calls are also on that another thread/Worker.



### Multi threaded, publishOn 
- Say we have declare a reactive stream like so `Flux.just(1, 2, 3).publishOn(Schedulers.boundedElastic()).op_2()`, and op_2 is a flatMap operation
    - At this point, this is a `cold publisher`
    - We have a bunch of publishers linked together
    - `Flux.just()` gives us publisher FluxJust `P_0`, a source node
    - `P_0.publishOn(Schedulers.boundedElastic())` gives us publisher FluxPublishOn `PPO_1`, an operator node, and has reference to `P_0`
    - `PPO_1.op_2()` gives us publisher FluxFlatMap `P_2`, an operator node, and has reference to `PPO_1`
    - All these happen in the thread `t-main`

- This is what it looks like [`P_0` <- `PPO_1` <- `P_2`]
    - Now we add a subscribe call at the end `Flux.just(1, 2, 3).publishOn(Schedulers.boundedElastic()).op_2().subscribe(lambda)` 
    - This subscribe function takes in a Lambda function that return void (Consumer type), it will be wrapped around by a `LambdaSubscriber`, lets call it `Sb_3`
    - `Sb_3` is only a subscriber, and it is not a publisher
    - `P_2` now takes `Sb_3` and `op_2`, bundle them together in a `MapSubscriber`, lets call it `Sbp_2` and its both a `Subscriber` and `Subscription`
    - Still in `t-main`

- This is what it looks like now [`P_0` <- `PPO_1` <- `P_2`, and `Sbp_2` -> `Sb_3`] 
    - `P_2` now trigger its source `PPO_1` to call subscribe on `Sbp_2`
    - `PPO_1` creates a new worker/thread to execute, let's call it `t-elastic-1` 
    - `PPO_1` now takes `Sbp_2` and the worker `t-elastic-1`,  to create a `PublishOnSubscriber`, lets call it `POS_1`, which extends `InternalFluxOperator`, which means its both a `Subscriber` and `Subscription` 

- This is what it looks like now [`P_0` <- `PPO_1` <- `P_2`, and `POS_1` -> `Sbp_2` -> `Sb_3`] 
    - `PPO_1` then trigger it's source `P_0` to call subscribe on `POS_1`
    - Because `P_0` is a source node, it doesn't propagate the subscribe call upstream anymore.
    - `P_0` now takes `POS_1` to create a `ScalarSubscription`, which extends `InnerProducer`, which is a `Subscription`, lets call it `Sp_0`

- This is what it looks like now [`P_0` <- `PPO_1` <- `P_2`, and `Sp_0` -> `POS_1` -> `Sbp_2` -> `Sb_3`] 
    - `P_0` then trigger `POS_1` to call onSubscribe on `Sp_0`
        - which makes `POS_1` save the reference of `Sp_0`
        - it also trigger `Sbp_2` to call onSubscribe on `POS_1`
            - `Sbp_2` call onSubscribe on `POS_1`, which makes `Sbp_2` save the reference of `POS_1`
            - `Sbp_2` then trigger `Sb_3` to call onSubscribe on `Sbp_2`, which makes `Sb_3` save the reference of `Sbp_2`

- This is what it looks like now [`P_0` <- `PPO_1` <- `P_2`, and `Sp_0` <-> `POS_1` <-> `Sbp_2` <-> `Sb_3`] 
    - And because `Sb_3` is a `LambdaSubscriber` leaf node, it then triggers `Sbp_2` to call request upstream to start pulling data
    - `Sbp_2` then makes `POS_1` to call request
    - However, `POS_1` stops the upstream call. Instead it calls `run` on `t-elastic-1`. If there's no element to be processed by onNext in the queue, it stops

- and it also trigger its source `Sp_0` to start the upstream request call to start pulling data
    - `Sp_0` is a `ScalarSubscription`, which has a different implementation. It starts feeding `POS_1` onNext with values 
    - `POS_1` onNext actually call `run` on `t-elastic-1`. There it finds the onNext value pasted down by `Sp_0`.
    - `POS_1` trigger `Sbp_2` to call onNext on the passed down value in `t-elastic-1`
    - `Sbp_2` trigger `Sbp_3` to consume on the passed down value in `t-elastic-1` because its the leaf node

- and when `Sp_0` is completed, it trigger `POS_1` to call onComplete, which trigger `run` on `t-elastic-1` again. This time `run` will trigger `Sbp_2` to call onComplete on `t-elastic-1`

=> `PublishOnSubscriber` is kinda like a downstream router, where it makes all the downstream calls (onNext, onComlete,...) happens in another thread/Worker.
The upstream calls are either unaffected (subscribe) or stopped from cascading (request).
    

# General learning
- Video form
    - [Vid 1](https://www.youtube.com/watch?v=hfupNIxzNP4)
        - Assembly/Subscription time 
            - Take this example `Publisher -> op_1 -> op_2 -> subscribe(lambda_1)`
            - At assembly time 
                - op_1, op_2, lambda_1 is wrapped in subscribers 
                - These subscribers subcribe to their upstream publishers, 
                and forward signals (onNext, onComplete, onSubscribe, onError) to its downstream subscribers 
                - It will result in a `Publisher -> op_1_sub -> op_2_sub -> lambda_1_sub` 
            - At runtime
                - subscribe (The bottom most subscriber) is called, which calls to 
        - Hot/Cold publisher
            - For cold publishers, nothing happens until you subscribes (Say list of https requests to execute)
            - For hot publishers, always be emitting data to their subscribers
        - Hopping Threads/Schedulers
            - Most of the time, the event-loop-1 on thread main-1, if accepted task-A, will run task-A on main-1 as well
            - However, calling
                - `subscribeOn` will affect everything upstream and downstream
                - `publishOn` will affect everything downstream

- Blog post form 
    - [blog 1](https://projectreactor.io/learn)
    - [blog 2](https://spring.io/blog/2019/03/06/flight-of-the-flux-1-assembly-vs-subscription)
    - [blog 3](https://www.baeldung.com/java-reactor-map-flatmap)

- Block vs non blocking
    - Blocking operations
        - Disk I/O
        - Traditional Http client (non default in Spring webflux)
        - acquiring locks
    - Non blocking 
        - Chaining operation on Publisher aren't blocking. Only when calling .block() they becomes blocking
        - Http request by WebClient 

- Concurrency and event loop
    - [Python](https://www.youtube.com/watch?v=MCs5OvhV9S4)
        - COOPERATIVE FAKE THREADS (on 1 real OS thread)
        - 1 thread runs everything per core doable. Need Redis to sync. 
        - gevent greenlets (virtual thread but in userspace, not OS)
        - Need to monkeypatch OS blocking calls, making OS blocking calls DO yield
    - [Reactor](https://www.baeldung.com/spring-webflux-concurrency)
        - PREMPTIVE THREADS (OS)
        - 1 thread per cpu core => for maximum utilization. Sync state using DB as well.
        - Using just `Reactor`, there are no `virtual thread`, the `event loop` and the `tasks` are executed on the same thread unless specified otherwise (subscribeOn, publishOn, time op,...).
        - It DOES NOT implies that when `Flux/Mono` are subscribed to, a callback function 
        is registered and the task gave up its time on the main thread for the `event loop` to execute again.
        => It will stall socket I/O handling => response time spikes. Need to allocate blocking calls to elastic threads
        - Like GEVENT, OS blocking calls DO yield (thread yield kernel level)
    - [Comparisons]
        - Alot more difference that i thought. Especially how optimization is handled
            - gevent: just dont do compute heavy task on same thread, dont worry about the I/O blocking calls
            - reactor: threads specialized for compute heavy, and threads specialized for blocking calls
        - Same issue if run compute heavy or blocking task that arent made aware to the event loop
        - If run blocking I/O also a problem because the whole thread yields
        ==> Block the whole event loop

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
- [GRPC code explained](https://www.youtube.com/watch?v=zCXN4wj0uPo)
- GRPC interface refer to `inference-server` README.md + `postman collection`


# Triton GRPC good to know
- More on `raw_input_contents/raw_output_contents` or `Tensor data` [here](https://docs.nvidia.com/deeplearning/triton-inference-server/user-guide/docs/protocol/extension_binary_data.html?utm_source=chatgpt.com)
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
- Add `target/generated-sources/protobuf/java` as one of the source directories following [this](https://www.baeldung.com/maven-add-src-directories)
- The application was serving fine? I guess it was only for LSP

# Application.yml
- Automatically loaded in via auto configuration of certain module. Else has to be loaded in manually (GemmaConfig)

# Model config
- Loaded from application.yml


# Error Handling
- Goal is [RFC 9457 compliant](https://www.rfc-editor.org/rfc/rfc9457.html#name-json-schema-for-http-proble)
    - TLDR: every error response body has to have a specific format, and you can return additional data fields for dev consuming the api to use for debuggings
    - Need to set up a documentation server for field `type`. Perhaps `SWAGGER`? 
- Using `jakarta` for contraint annotations + validation
- Implementations of RFC 9457 in `GlobalHandlingError.java`
    - Includes some hacky way of intercepting `jarkata` validation to return a formatted error
    - Support for `custom exceptions`

# Logging
- <springProfile> with logback-spring.xml for different formats for different env
    - automatically loaded if in resources folder
    - in prod its 1 line json for easy log scrapping
    - in dev its structured and human-readable
    
# Traces, Metrics, Logs 
- [Comprehensive overview](https://www.youtube.com/watch?v=fh3VbrPvAjg)
- Code walkthrough example 
    - [video 1](https://www.youtube.com/watch?v=Qyku6cR6ADY)
    - [video 2](https://www.youtube.com/watch?v=Ssje93u2GWM)
- [Blog post walk through](https://spring.io/blog/2024/10/28/lets-use-opentelemetry-with-spring)
    - Brave + OpenTelemetry
- Overview:
    - OpenZipkin
        - Zipkin: latency visualization tools
        - Brave: tracer library, handles life cycle of a span (Battle hardened)
    - OpenTelemetry
        - Standardize how apps are instrumented 
        - Think of interfaces to go from 
            - App -> Exporter -> Collector -> Exporter -> Data Backend 
        - Also tracer library? (Newer)
        - Push based
        - OTEL collector 
            - DOES NOT HAVE BACKEND FOR STORAGE. 
            - Purpose is to instrument with OTEL and send telemetry to supporting backend
            - Or process then send to none OTEL supporting backend
            - support prometheus by exposing and endpoint for pulling data
    - Prometheus
        - Time series database
        - Does not support OTEL, need to preprocess
        - [Pull based](https://dev.to/mikkergimenez/why-is-prometheus-pull-based-36k1#:~:text=Another%20reason%20is%20that%20a,outage%2C%20or%20has%20been%20decommissioned.)
    - Latency Visualization tools:
        - VMware Tanzu
        - OpenZipkin
        - Jaeger
        - `Grafana Tempo`
    - Metrics Visualization tools:
        - VMware Tanzu
        - `Grafana`
    - Logs
        - Elastic Logstash Kibana
        - `Grafana Loki`
    - Micrometer observation api
        - Abstraction over Traces, Metrics, and Logs module
    - Micrometer Core
        - counters, timers, gauges (metrics)
    - Micrometer Tracing
        - add traces/spans support, but no specific implementations
    - OpenTelemetry/Brave
        - Actual tracer implementation

- Sampling rules in tracing
    - Every request has 1 traceId and 1 or more spanId
    - Recording every trace is not sustainable at high trafic and might kill your backend 
    => Need to samples in production
    - `Head-based sampling`
        - Decision made as it arrives, regardless of outcomes
        - flip a coin probability types
    - `Tail-based sampling`
        - Record everything locally, wait till outcome to see if should export
        - Need OTEL Collector to process locally
    - Netflix style?
        - In prod
            - sample small percentage to cut cost
            - Alrays keep Errors/High latency request
        - In dev
            - keep all for debugging lol
    - NOTE: even though traces are not picked, the logs are still untouched, just that we dont have traceId/spanId for those

- It's a bit confusing but there are [multiple ways](https://github.com/spring-projects/spring-boot/issues/41227) of doing tracings/metrics
    - OpenTelemetry supported ways (Java Agent which inject bytes code wowow and OpenTelemetry Spring Boot Starter)
        - Java Agent way is to download the agent binary, execute it at runtime with the spring binary. That should be it
    - Spring supported way, which is micrometer with OTel tracing implementation + OTLP exporter
    - Let's just go with the harder way just to understand whats happening underthe hood => Spring supported way

# [Micrometer Observation Api](https://docs.micrometer.io/micrometer/reference/observation/introduction.html)
- Plan is to use
    - Micrometer
    - OTel tracing `micrometer-tracing-bridge-otel` + OTLP converter (`opentelemetry-exporter-otlp` for traces and `micrometer-registry-otlp` and metrics) 
    - Grafana everything
    - Send metrics to Prometheus/Grafana, traces to Tempo/Jaeger, logs to Loki.
- Setup
    - application.yml
        - logging pattern
        - management for tracing sampling (default to 0.1 or 10%) and endpoint for pushing data (otlp collector)
    - pom.xml
        - reactor-core-micrometer (reactor module for interfacing with micrometer)
        - micrometer-tracing-bridge-otel for otlp tracing implementation
        - opentelemetry-exporter-otlp for convert micrometer to otlp format
- Notes
    - For inbound and outbound requests, micrometers when integrated with OTEL automatically handles the creation and propagation of traceId and spanId 
- CURRENT PROGRESS
    - Got micrometer with Otel tracing working, and it automatically creates traces/spans for inbound/outbound requests
    - However, could not add custom spans inside those automatic traces. Ended up creating a separted traces => Bad
    - Found out that was because Reactor Core needs `reactor-core-micrometer` module as well as `reactor-core` module (why is it not imported initially, how is reactive event loop working wthelly)
    - [DOCS](https://projectreactor.io/docs/core/release/reference/metrics.html)
    - Now we can manually add metrics/tracings => GUD
- NEXT TASK
    - Figure out version for `reactor-core-micrometer` and `reactor-core`.
    - Clean up pom.xml + code
    - Start adding custom spans and tags and shessh



        

# Structure
Consumers will never access the core/impl, only the api objetcs 
```
*any_folder/
├── api/ (Interfaces/Contracts for consumers)
│   ├── obj_interface (What the obj do)
│   └── objRegistry_interface (What the registry do - usually supply obj given some obj's id)
├── core/ (Shared building blocks for case specific implementations)
│   ├── obj_abstract_base (Shared base implementations)
│   └── objRegistry_base (Registry implementation, usually sufficient at this point because don't have variant of objRegistry. Tho it is possible)
└── impl/ (Case specific implementations)
    └── obj_variant (variant implementation)
```

```
gly-gateway/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── glygateway/
│   │   │           ├── Main.java (Configured to scans configs from all over the directory - No need to create Beans for those configs)
│   │   │           ├── exception/
│   │   │           ├── model/ 
│   │   │           │   ├── domain/ (Business logic models)
│   │   │           │   └── triton/ (Triton specific models/enums)
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
│   │   │                   │   └── ModelConfig.java (record objects loaded from `application.yml`)
│   │   │                   ├── api/
│   │   │                   ├── core/
│   │   │                   └── impl/
│   │   ├── resources/
│   │   │   ├── application.properties (hosting configs)
│   │   │   ├── application.yml (model configs)
│   │   │   └── ... (other resources)
│   │   └── proto/  <- This is where triton .proto files go
│   │       ├── grpc_service.proto
│   │       ├── model_config.proto
│   │       └── health.proto
│   └── test/
│       └── ... (test related code and resources)
└── target/
    └── ... (build output)
```
