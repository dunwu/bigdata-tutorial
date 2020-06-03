package io.github.dunwu.bigdata.zk.id;

import java.util.Set;
import java.util.concurrent.*;

/**
 * 并发测试生成分布式ID
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2020-06-03
 */
public class ZkDistributedIdTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        DistributedId distributedId = new ZkDistributedId("localhost:2181");

        final CountDownLatch latch = new CountDownLatch(10000);
        final ExecutorService executorService = Executors.newFixedThreadPool(20);
        long begin = System.nanoTime();

        Set<Long> set = new ConcurrentSkipListSet<>();
        for (int i = 0; i < 10000; i++) {
            Future<Long> future = executorService.submit(new MyThread(latch, distributedId));
            set.add(future.get());
        }

        try {
            latch.await();
            executorService.shutdown();

            long end = System.nanoTime();
            long time = end - begin;
            System.out.println("ID 数：" + set.size());
            System.out.println("耗时：" + TimeUnit.NANOSECONDS.toSeconds(time) + " 秒");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class MyThread implements Callable<Long> {

        private final CountDownLatch latch;
        private final DistributedId distributedId;

        MyThread(CountDownLatch latch, DistributedId distributedId) {
            this.latch = latch;
            this.distributedId = distributedId;
        }

        @Override
        public Long call() {
            Long id = distributedId.generate();
            latch.countDown();
            return id;
        }

    }

}
