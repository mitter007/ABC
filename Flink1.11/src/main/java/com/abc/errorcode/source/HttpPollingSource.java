package com.abc.errorcode.source;

import com.abc.errorcode.AlarmRule;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.util.Collections;
import java.util.List;

public class HttpPollingSource extends RichSourceFunction<AlarmRule> {

    private volatile boolean running = true;


    private final long intervalMs;



    public HttpPollingSource( long intervalMs) {
        this.intervalMs = intervalMs;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        // 初始化 HTTP client（可以用 Apache HttpClient / OkHttp）
    }

    @Override
    public void run(SourceContext<AlarmRule> ctx) throws Exception {
        while (running) {

            // 1. 发送 HTTP 请求
            String response = doHttpRequest("");

            // 2. 解析数据（假设返回 JSON 数组）
            List<String> records = parse(response);

            // 3. 发射数据
            synchronized (ctx.getCheckpointLock()) {
                for (String record : records) {
                    AlarmRule alarmRule = new AlarmRule();
                    ctx.collect(alarmRule);
                }
            }

            Thread.sleep(intervalMs);
        }
    }

    @Override
    public void cancel() {
        running = false;
    }

    private String doHttpRequest(String url) throws Exception {
        // 伪代码
        return HttpUtils.get(url);
    }

    private List<String> parse(String response) {
        // JSON 解析
        return Collections.singletonList(response);
    }
}
