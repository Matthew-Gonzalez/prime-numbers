package cl.ucn.disc.hpc.primos;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Main Class.
 *
 * @author Matthew Gonzalez-Mansilla.
 */
@Slf4j
public class Main {

    private static final AtomicInteger counter = new AtomicInteger(0);

    /**
     * The main.
     * @param args optional args.
     * @throws InterruptedException error.
     */
    public static void main(String[] args) throws InterruptedException {

        // Finding the numbers of cores
        final int maxCores = Runtime.getRuntime().availableProcessors();
        log.debug("The max cores:{}" + '\n', maxCores);

        final long start = 2;
        final long end = 5000000;
        final int attempts = 20;
        final int maxTimeMinutes = 5;

        for (int i = 1; i <= maxCores; i++){
            findPrimes(start, end, i, attempts, maxTimeMinutes);
        }
    }

    /**
     * Find the primes with n cores.
     *
     * @param start start number.
     * @param end end number.
     * @param cores number of cores.
     * @param attempts number of attempts to average.
     * @param maxTimeMinutes the maximum time a thread will take to finish.
     */
    private static void findPrimes(long start, long end, int cores, int attempts, int maxTimeMinutes) throws InterruptedException {

        final List<Long> times = new ArrayList<>();

        log.info("Find primes between {} and {} with {} cores and {} attempts", start, end, cores, attempts);

        for(int i = 0; i < attempts; i++){
            final ExecutorService executor = Executors.newFixedThreadPool(cores);
            counter.set(0);
            StopWatch sw = StopWatch.createStarted();
            for(long j = start; j <= end; j++){
                long toTest = j;
                executor.submit(()->{
                    if  (isPrime(toTest)){
                        counter.incrementAndGet();
                    }
                });
            }
            executor.shutdown();
            if (executor.awaitTermination(maxTimeMinutes, TimeUnit.MINUTES)){
                long timeToCompleted = sw.getTime(TimeUnit.MILLISECONDS);
                times.add(timeToCompleted);
                //log.info("Executor OK | The N of primes is {} | Time to completed: {} ms", counter.get(), timeToCompleted);
            }else{
                log.warn("Executor did not finish in {} minutes", maxTimeMinutes);
            }
        }

        long min = Collections.min(times);
        log.debug("Min time:{} ms", min);
        times.remove(min);

        long max = Collections.max(times);
        log.debug("Max time:{} ms", max);
        times.remove(max);

        double average = times.stream().mapToLong(n -> n).average().getAsDouble();

        log.debug("Average time:{} ms" + '\n', average);
    }

    /**
     * Check if a number is prime.
     * @param n the number.
     * @return true if the number is prime.
     */
    private static boolean isPrime(final long n){

        // No prime
        if (n <= 0) {
            throw new IllegalArgumentException("Error in n: can't process negative numbers..");
        }

        // One isn't prime
        if (n == 1){
            return false;
        }

        if (n == 2){
            return false;
        }

        if (n % 2 == 0){
            return false;
        }

        // Testing the primality
        for (long i = 3; (i*i) < n; i += 2){

            // n is divisible by i
            if (n % i == 0){
                return false;
            }
        }

        return true;
    }
}
