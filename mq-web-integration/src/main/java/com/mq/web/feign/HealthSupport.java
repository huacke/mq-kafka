package com.mq.web.feign;

import com.mq.common.response.ResultHandleT;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

public interface HealthSupport {
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultHandleT<Void> health();
}
