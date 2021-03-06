package biz.paluch.spinach.commands;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.api.Job;
import biz.paluch.spinach.api.QScanArgs;
import com.lambdaworks.redis.KeyScanCursor;
import com.lambdaworks.redis.RedisException;
import org.junit.Test;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class QueueCommandTest extends AbstractCommandTest {

    @Test
    public void working() throws Exception {

        disque.addjob(queue, value, 5, TimeUnit.SECONDS);
        Job<String, String> job = disque.getjob(queue);

        long result = disque.working(job.getId());

        assertThat(result).isGreaterThan(1);
    }

    @Test(expected = RedisException.class)
    public void workingWithoutJob() throws Exception {
        disque.working("DIb043347c89a98df8dea195c47bcd715f2f78ee7705a1SQ");
    }

    @Test
    public void getJobWithoutJob() throws Exception {

        assertThat(disque.qlen(queue)).isEqualTo(0);

        Job<String, String> job = disque.getjob(1, TimeUnit.SECONDS, queue);

        assertThat(job).isNull();
    }

    @Test
    public void qpeek() throws Exception {

        disque.addjob(queue, value, 2, TimeUnit.SECONDS);
        disque.addjob(queue, value, 2, TimeUnit.SECONDS);
        disque.addjob(queue, value, 2, TimeUnit.SECONDS);
        disque.addjob(queue, value, 2, TimeUnit.SECONDS);

        List<Job<String, String>> result = disque.qpeek(queue, 2);

        assertThat(result).hasSize(2);

        Job<String, String> job1 = result.get(0);

        assertThat(job1.getBody()).isEqualTo(value);
        assertThat(job1.getQueue()).isEqualTo(queue);
        assertThat(job1.getId()).startsWith("DI");

        Job<String, String> job2 = result.get(1);

        assertThat(job2.getBody()).isEqualTo(value);
        assertThat(job2.getQueue()).isEqualTo(queue);
        assertThat(job2.getId()).startsWith("DI");
    }

    @Test
    public void qscan() throws Exception {

        addJobs(1, "q", 100, value);

        KeyScanCursor<String> result = disque.qscan();
        assertThat(result.getKeys()).hasSize(100);
    }

    @Test
    public void qscanWithArgs() throws Exception {

        addJobs(1, "q", 120, value);

        KeyScanCursor<String> result = disque.qscan(QScanArgs.builder().count(5).build());
        assertThat(result.getKeys().size()).isGreaterThan(4).isLessThan(99);
        assertThat(result.isFinished()).isFalse();

        result = disque.qscan(QScanArgs.builder().importrate(0).maxlen(1).maxlen(10).build());
        assertThat(result.getKeys().size()).isGreaterThan(99);

        assertThat(result.isFinished()).isFalse();
    }

    @Test
    public void qscanWithContinue() throws Exception {

        addJobs(1, "q", 100, value);

        QScanArgs scanArgs = QScanArgs.builder().count(5).build();
        KeyScanCursor<String> result = disque.qscan(scanArgs);
        assertThat(result.getKeys().size()).isGreaterThan(4);
        result = disque.qscan(result, scanArgs);

        assertThat(result.getKeys().size()).isGreaterThan(4);
        assertThat(result.isFinished()).isFalse();

        result = disque.qscan(scanArgs);
        result = disque.qscan(result);
        assertThat(result.getKeys().size()).isGreaterThan(80);
        assertThat(result.isFinished()).isTrue();
    }

}
