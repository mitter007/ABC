package com.atguigu.es.test.chat0929;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EsRecord {
    private String timestamp;
    private String syscode;
    private String groupcode;
    private String dimcode;
    private String dimcont;
    private Double sysSuccrate60;
    private Long tps60;
    private Dimension dimension;

    // getter/setter ...

    public static class Dimension {
        private String channelCode;
        private String proCod;
        private String transCode;

        public String getChannelCode() {
            return channelCode;
        }

        public void setChannelCode(String channelCode) {
            this.channelCode = channelCode;
        }

        public String getProCod() {
            return proCod;
        }

        public void setProCod(String proCod) {
            this.proCod = proCod;
        }

        public String getTransCode() {
            return transCode;
        }

        public void setTransCode(String transCode) {
            this.transCode = transCode;
        }
        // getter/setter ...
    }
}
