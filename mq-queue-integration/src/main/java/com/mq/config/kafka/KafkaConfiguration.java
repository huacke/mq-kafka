/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mq.config.kafka;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author huacke
 * kafka配置加载
 * @since 1.0
 */
@Configuration
@AutoConfigureAfter(KafkaAutoConfiguration.class)
public class KafkaConfiguration {
	public static  KafkaProperties kafkaProperties;
	public KafkaConfiguration(KafkaProperties kafkaProperties) {
		KafkaConfiguration.kafkaProperties = kafkaProperties;
	}

}
