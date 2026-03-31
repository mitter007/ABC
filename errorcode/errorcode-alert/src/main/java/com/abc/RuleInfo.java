package com.abc;

/**
 * RuleInfo - minimal rule object
 */
public class RuleInfo implements java.io.Serializable {
    private Integer alertSpec; // -1 means no alert
    // can add more fields if needed

    public Integer getAlertSpec() { return alertSpec; }
    public void setAlertSpec(Integer alertSpec) { this.alertSpec = alertSpec; }
}
