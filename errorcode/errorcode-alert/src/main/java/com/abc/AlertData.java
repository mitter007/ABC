package com.abc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * AlertData - 聚合窗口内的数据（用于 reduceByKey）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertData implements java.io.Serializable {
        private String syscode;
        private String groupcode;
        private String systemcase;
        private String firstDim;
        private Set<String> secDims = new HashSet<String>();
        private RuleInfo ruleInfo;

        public void addSecDim(String sd) {
            if (sd == null) return;
            this.secDims.add(sd);
        }

    public AlertData(String syscode, String groupcode, String systemcase, String firstDim) {
        this.syscode = syscode;
        this.groupcode = groupcode;
        this.systemcase = systemcase;
        this.firstDim = firstDim;
    }

    public void merge(AlertData other) {
            if (other == null) return;
            if (this.syscode == null) this.syscode = other.syscode;
            if (this.groupcode == null) this.groupcode = other.groupcode;
            if (this.systemcase == null) this.systemcase = other.systemcase;
            if (this.firstDim == null) this.firstDim = other.firstDim;
            if (other.secDims != null) this.secDims.addAll(other.secDims);
            if (this.ruleInfo == null) this.ruleInfo = other.ruleInfo;
        }


}
