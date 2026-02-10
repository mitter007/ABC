package com.abc.job;

/**
 * ClassName: RefreshConfigThread
 * Package: com.abc.job
 * Description:
 *
 * @Author JWT
 * @Create 2026/1/22 19:50
 * @Version 1.0
 */
public class RefreshConfigThread implements Runnable {
    @Override
    public void run() {
        refreshDimmapInfo();
        refreshRexInfo();
        refreshBlockFunction();
        clearCache();

        refreshStopSysInfo();
        refreshESMetricWriter();


        refreshSplitInfo();
        refreshAlertJudge();
    }

    private void refreshDimmapInfo() {
    }

    private void refreshRexInfo() {
    }

    private void refreshStopSysInfo() {
    }

    private void refreshSplitInfo() {
    }

    private void refreshESMetricWriter() {
    }

    private void refreshBlockFunction() {
    }

    private void clearCache() {
    }

    private void refreshAlertJudge() {
    }
}
