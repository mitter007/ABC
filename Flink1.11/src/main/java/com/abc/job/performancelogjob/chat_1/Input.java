package com.abc.job.performancelogjob.chat_1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

class Input {
    String getKey() {
        return null;
    }

    ;

}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Output {
    Input v;
    Config d;

}

class Config {
    static Config fromJson(String v) {
        return new Config();
    }

    static Map<String, String> parseAllFromJson( String s) {
        return new HashMap<>();
    }

}