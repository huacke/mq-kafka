package com.mq.datasources;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 增加多数据源，在此配置
 */
@AllArgsConstructor
@Getter
public enum DataSourceNames {

    MASTER("master", "主数据源"),
    SLAVE("slave", "从数据源");
    private String code;
    private String name;

    public static DataSourceNames getTypeByCode(String code) {
        DataSourceNames type = null;
        DataSourceNames[] values = DataSourceNames.values();
        Optional<DataSourceNames> optional = Stream.of(values).filter(it -> it.getCode().equals(code)).findFirst();
        if (optional.isPresent()) {
            type = optional.get();
        }
        return type;
    }
}
