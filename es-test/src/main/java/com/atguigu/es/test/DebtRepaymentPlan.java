package com.atguigu.es.test;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DebtRepaymentPlan {

    static class MonthRecord {
        String month;
        double externalPayment;
        double externalRemaining;
        double mortgagePayment;
        double mortgageInterest;
        double mortgagePrincipal;
        double mortgageRemaining;

        public MonthRecord(String month, double externalPayment, double externalRemaining,
                           double mortgagePayment, double mortgageInterest, double mortgagePrincipal,
                           double mortgageRemaining) {
            this.month = month;
            this.externalPayment = externalPayment;
            this.externalRemaining = externalRemaining;
            this.mortgagePayment = mortgagePayment;
            this.mortgageInterest = mortgageInterest;
            this.mortgagePrincipal = mortgagePrincipal;
            this.mortgageRemaining = mortgageRemaining;
        }
    }

    public static void main(String[] args) throws IOException {
        // 参数
        double externalDebt = 140000; // 外债
        double monthlyExternalPayment = 15000; // 每月收入

        double mortgageBalance = 780000; //房贷总额
        double mortgageRate = 0.032; // 年利率
        double mortgageMonthlyPayment = 3665; // 原等额本息月供
        String fileName ="Debt_Repayment_Plan-15.xlsx";

        double monthlyIncome = 18000;
        double livingExpenses = 4000;
//        double availableFunds = monthlyIncome - livingExpenses; // 14000 可用还款的资金

        List<MonthRecord> records = new ArrayList<>();

        double externalRemaining = externalDebt;
        double mortgageRemaining = mortgageBalance;
        int monthCounter = 1;

        while (mortgageRemaining > 0) {
            String monthStr = "Month " + monthCounter;

            // 外债还款
            double externalPayment = externalRemaining > 0 ? Math.min(monthlyExternalPayment, 10335) : 0;


            externalRemaining -=Math.min(externalRemaining,externalPayment);

            // 房贷还款
            double mortgagePayment;
            if (externalRemaining > 0) {
                mortgagePayment = mortgageMonthlyPayment;
            } else {
                mortgagePayment = monthlyExternalPayment;
            }

            double monthlyInterest = mortgageRemaining * (mortgageRate / 12);
            double principalPayment = Math.min(mortgagePayment - monthlyInterest, mortgageRemaining);
            mortgageRemaining -= principalPayment;

            records.add(new MonthRecord(
                    monthStr,
                    externalPayment,
                    externalRemaining,
                    mortgagePayment,
                    monthlyInterest,
                    principalPayment,
                    mortgageRemaining
            ));

            monthCounter++;
        }

        // 生成 Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Debt Repayment Plan");

        // 表头
        String[] headers = {"Month", "External Debt Payment", "External Debt Remaining",
                "Mortgage Payment", "Mortgage Interest", "Mortgage Principal", "Mortgage Remaining"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 写入数据
        int rowNum = 1;
        for (MonthRecord rec : records) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rec.month);
            row.createCell(1).setCellValue(rec.externalPayment);
            row.createCell(2).setCellValue(rec.externalRemaining);
            row.createCell(3).setCellValue(rec.mortgagePayment);
            row.createCell(4).setCellValue(rec.mortgageInterest);
            row.createCell(5).setCellValue(rec.mortgagePrincipal);
            row.createCell(6).setCellValue(rec.mortgageRemaining);
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 保存文件
        try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
            workbook.write(fileOut);
        }

        workbook.close();
        System.out.println("Excel 文件生成完成："+fileName);
    }
}