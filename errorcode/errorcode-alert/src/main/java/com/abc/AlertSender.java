package com.abc;

import java.util.Date;
import java.util.List;

public class AlertSender {

    public static class AlertPayload {
        private String syscode;
        private String groupcode;
        private String systemcase;
        private String firstDim;
        private List<String> secDims;
        private Date windowTime;

        public AlertPayload(){}
        public AlertPayload(String sys, String grp, String scase, String firstDim, List<String> secDims){
            this.syscode=sys; this.groupcode=grp; this.systemcase=scase;
            this.firstDim=firstDim; this.secDims=secDims;
        }
        public void setWindowTime(Date d){this.windowTime=d;}

        public  static void sendAlert(AlertPayload p ) {
            // 调用告警接口
            System.out.println("告警发送：" + p.syscode + " " + p.firstDim + " " + p.secDims);
        }
    }

    public static void sendAlert(AlertPayload p){
        // 调用告警接口
        System.out.println("告警发送：" + p.syscode + " " + p.firstDim + " " + p.secDims);
    }
}
