package com.glygateway;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

public class ReactiveTutorial {

  private Mono<Object> testMono() {
    return Mono.justOrEmpty(null).log();
  }

  private Flux<String> testFlux() {
    List<String> programming_languages = List.of("Flux", "Java", "Cpp", "Dart", "Rust");
    return Flux.fromIterable(programming_languages);
  }

  private Flux<String> testMap() {
    Flux<String> flux = Flux.just("Map", "Java", "Cpp", "Dart", "Rust").delayElements(Duration.ofMillis(500));
    return flux.map(s -> s.toUpperCase(Locale.ROOT));
  }

  // Flat map makes Mono<Mono<T>> to Mono<T>
  private Flux<String> testFlatMap() {
    Flux<String> flux = Flux.just("FlatMap", "Java", "Cpp", "Dart", "Rust").delayElements(Duration.ofMillis(500));
    return flux.flatMap(s -> Mono.just(s.toUpperCase(Locale.ROOT)));
  }

  private Flux<String> testBasicSkip() {
    Flux<String> flux = Flux.just("Skip", "Java", "Cpp", "Dart", "Rust").delayElements(Duration.ofSeconds(1));
    // return flux.skip(Duration.ofMillis(2010)).log();
    return flux.skipLast(2).log();
  }

  private Flux<Integer> testComplexSkip() {
    Flux<Integer> flux = Flux.range(1, 20);
    // return flux.skip(Duration.ofMillis(2010)).log();
    Boolean skip = true; 
    return flux.skipWhile(integer -> skip);
  }

  private Flux<Integer> testConcat() {
    Flux<Integer> flux1 = Flux.range(1, 20);
    Flux<Integer> flux2 = Flux.range(101, 20);
    return Flux.concat(flux1, flux2, flux1);
  }

  private Flux<Integer> testMerge() {
    Flux<Integer> flux1 = Flux.range(1, 20).delayElements(Duration.ofMillis(250));
    Flux<Integer> flux2 = Flux.range(101, 20).delayElements(Duration.ofMillis(250));
    return Flux.merge(flux1, flux2);
  }

  private Flux<Tuple3<Integer, Integer, Integer>> testBasicZip() {
    Flux<Integer> flux1 = Flux.range(1, 20).delayElements(Duration.ofMillis(250));
    Flux<Integer> flux2 = Flux.range(101, 10).delayElements(Duration.ofMillis(250));
    Flux<Integer> flux3 = Flux.range(201, 3).delayElements(Duration.ofMillis(250));
    return Flux.zip(flux1, flux2, flux3);
  }

  private Flux<Tuple2<Integer, Integer>> testComplexZip() {
    Flux<Integer> flux1 = Flux.range(1, 20).delayElements(Duration.ofMillis(250));
    Mono<Integer> mono = Mono.just(1);
    return Flux.zip(flux1, mono);
  }

  private Mono<List<Integer>> testCollect() {
    Flux<Integer> flux = Flux.range(1, 10).delayElements(Duration.ofSeconds(1));
    return flux.collectList();
  }

  private Flux<List<Integer>> testBuffer() {
    Flux<Integer> flux = Flux.range(1, 10).delayElements(Duration.ofMillis(500));
    return flux.buffer(Duration.ofMillis(1010)).log();
  }
  
  private Mono<Map<Integer, Integer>> testCollectMap() {
    Flux<Integer> flux = Flux.range(1, 10).delayElements(Duration.ofMillis(500));
    return flux.collectMap(integer -> integer, integer -> integer * integer);
  }

  private Flux<Integer> testDoFunctions() {
    Flux<Integer> flux = Flux.range(1, 10);
    return flux.doOnEach(signal -> {
      if (signal.getType() == SignalType.ON_COMPLETE) {
        System.out.println("I am done!");
      }
      else {
        System.out.println("Signal: " + signal);
      }
    });
  }

  private Flux<Integer> testDoFunctions2() {
    Flux<Integer> flux = Flux.range(1, 10);
    return flux.doOnComplete(() -> {
        System.out.println("I am done!");
    });
  }

  private Flux<Integer> testDoFunctions3() {
    Flux<Integer> flux = Flux.range(1, 10);
    return flux.doOnNext((integer) -> {
      System.out.println(integer);
    });
  }

  private Flux<Integer> testDoFunctions4() {
    Flux<Integer> flux = Flux.range(1, 10).delayElements(Duration.ofSeconds(1));
    return flux.doOnSubscribe((subscription) -> {
      System.out.println("Subscribed!!");
    }).doOnCancel(() -> {
      System.out.println("Cancelled!!");
    });
  }

  private Flux<Integer> testErrorHandling() {
    Flux<Integer> flux = Flux.range(1, 10).map(integer -> {
      if (integer == 5){
        throw new RuntimeException("Unexpected number!");
      }
      return integer;
    });
    return flux.onErrorContinue((throwable, o) -> System.out.println("Dont worry about " + o));
  }

  private Flux<Integer> testErrorHandling2() {
    Flux<Integer> flux = Flux.range(1, 10).map(integer -> {
      if (integer == 5){
        throw new RuntimeException("Unexpected number!");
      }
      return integer;
    });
    // return flux.onErrorReturn(RuntimeException.class, -1);
    
    // return flux.onErrorResume(throwable -> Flux.range(100, 5));

    return flux.onErrorMap(throwable -> new ArithmeticException(throwable.getMessage()));
  } 

  // public static void main(String[] args) {
  //   ReactiveTutorial reactiveTutorial = new ReactiveTutorial();
  //   // reactiveTutorial.testMono();
  //   // reactiveTutorial.testMono().subscribe(data -> System.out.println(data));
  //   // reactiveTutorial.testFlux().subscribe(data -> System.out.println(data));
  //   // reactiveTutorial.testMap().subscribe(data -> System.out.println(data));
  //   // reactiveTutorial.testFlatMap().subscribe(data -> System.out.println(data));
  //   // reactiveTutorial.testBasicSkip().subscribe(data -> System.out.println(data));
  //   // reactiveTutorial.testComplexSkip().subscribe(data -> System.out.println(data));
  //   // reactiveTutorial.testConcat().subscribe(data -> System.out.println(data));
  //   // reactiveTutorial.testMerge().subscribe(data -> System.out.println(data));
  //   // reactiveTutorial.testBasicZip().subscribe(data -> System.out.println(data));
  //   // reactiveTutorial.testComplexZip().subscribe(data -> System.out.println(data));
  //   //
  //   // reactiveTutorial.testCollect().subscribe(data -> System.out.println(data));
  //   // System.out.println(reactiveTutorial.testCollect().block());

  //   // reactiveTutorial.testBuffer().subscribe(data -> System.out.println(data));
  //   reactiveTutorial.testCollectMap().subscribe(data -> System.out.println(data));

  //   // reactiveTutorial.testDoFunctions().subscribe(data -> System.out.println(data));
  //   // reactiveTutorial.testDoFunctions2().subscribe(data -> System.out.println(data));
  //   // reactiveTutorial.testDoFunctions3().subscribe(data -> System.out.println(data));

  //   // Disposable disposable = reactiveTutorial.testDoFunctions4().subscribe(data -> System.out.println(data));
  //   // reactiveTutorial.testDoFunctions4().subscribe(data -> System.out.println(data));
  //   // disposable.dispose();
  //   
  //   // reactiveTutorial.testErrorHandling().subscribe(integer -> System.out.println(integer));
  //   //reactiveTutorial.testErrorHandling2().subscribe(integer -> System.out.println(integer));
  // }
}
