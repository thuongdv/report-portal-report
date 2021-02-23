package excel.report;

import enums.DefinedCellStyle;
import enums.FeatureType;
import excel.report.dao.Suite;
import excel.report.dao.Test;
import helper.APIHelper;
import helper.Constants;
import helper.ExcelHelper;
import lombok.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RP2Excel {

    private static final String NON_CR_SHEET_NAME = "Non-CR";
    private static final String CR_SHEET_NAME = "CR";
    private static final int NON_CR_SHEET_POSITION = 1;
    private static final int CR_SHEET_POSITION = 2;
    private static final int HEADER_ROW_IDX = 0;
    private static final int START_ROW_IDX = 1;
    private static final int START_COL_IDX = 1;

    private ExcelHelper excelHelper;

    @SneakyThrows
    public void generate(String toFile) {
        this.excelHelper = new ExcelHelper(toFile);

        List<Suite> suites = this.getSuites();
        val nonCRSuites = suites.parallelStream().filter(e -> e.getType() == FeatureType.NON_CR).collect(Collectors.toList());
        val crSuites = suites.parallelStream().filter(e -> e.getType() == FeatureType.CR).collect(Collectors.toList());
        this.writeDataToSheet(nonCRSuites, NON_CR_SHEET_NAME, NON_CR_SHEET_POSITION);
        this.writeDataToSheet(crSuites, CR_SHEET_NAME, CR_SHEET_POSITION);
        this.finish();
    }

    private void writeDataToSheet(List<Suite> nonCRSuites, String toSheet, int position) {
        this.excelHelper.createSheet(toSheet, position);
        this.writeHeader(toSheet);
        this.writeData(toSheet, nonCRSuites);
        this.formatData(position);
    }

    private void writeHeader(String sheetName) {
        this.excelHelper.createRow(sheetName, HEADER_ROW_IDX, START_COL_IDX, RowData.Header.headers());
        this.excelHelper.formatRow(sheetName, HEADER_ROW_IDX, Arrays.asList(
            DefinedCellStyle.SET_COLOR_LIGHT_GREEN, DefinedCellStyle.ALIGN_CENTER,
            DefinedCellStyle.WRAP_TEXT, DefinedCellStyle.BORDER_ALL
        ));

        // Set column width
        this.excelHelper.setColumnWidth(sheetName, START_COL_IDX, RowData.Header.widths());
    }

    @SneakyThrows
    private void writeData(String sheetName, List<Suite> suites) {
        int currentRowIdx = START_ROW_IDX;
        for (Suite suite : suites) {
            List<Test> tests = this.getTests(suite.getPath());
            String comment = String.join("\n", this.getComments(tests));
            this.excelHelper.createRowWithFormat(
                sheetName, currentRowIdx, START_COL_IDX, new RowData(suite, comment).toList(),
                Arrays.asList(DefinedCellStyle.WRAP_TEXT, DefinedCellStyle.TOP_ALIGN, DefinedCellStyle.BORDER_ALL)
            );
            currentRowIdx++;
        }
    }

    private void formatData(int position) {
        SheetConditionalFormatting sheetCF = this.excelHelper.getSheet(position).getSheetConditionalFormatting();

        // Format FAILED data
        ConditionalFormattingRule ruleFailed = sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "\"FAILED\"");
        PatternFormatting fillFailed = ruleFailed.createPatternFormatting();
        fillFailed.setFillBackgroundColor(IndexedColors.RED.index);
        fillFailed.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        // Format PASSED data
        ConditionalFormattingRule rulePassed = sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "\"PASSED\"");
        PatternFormatting fillPassed = rulePassed.createPatternFormatting();
        fillPassed.setFillBackgroundColor(IndexedColors.GREEN.index);
        fillPassed.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        ConditionalFormattingRule[] cfRules = new ConditionalFormattingRule[]{ruleFailed, rulePassed};

        CellRangeAddress[] regions = new CellRangeAddress[]{CellRangeAddress.valueOf("C2:C120")};

        sheetCF.addConditionalFormatting(regions, cfRules);
    }

    private List<Suite> getSuites() {
        // Call Report Portal's APIs then write data to Data objects
        // filter.eq.launchId=875&filter.level.path=1&page.page=1&page.size=70&page.sort=startTime,ASC
        Map<String, String> query = new HashMap<>();
        query.put("filter.eq.launchId", Constants.LAUNCH_ID);
        query.put("filter.level.path", "1");
        query.put("page.page", "1");
        query.put("page.size", "70");
        query.put("page.sort", "startTime,ASC");
        return APIHelper.getSuites(query)
            .stream()
            .sorted((a, b) -> a.getStatus().compareToIgnoreCase(b.getStatus()))
            .collect(Collectors.toList());
    }

    private List<Test> getTests(String parentId) {
        // Call Report Portal's APIs then write data to Data objects
        // filter.eq.launchId=875&filter.eq.parentId=161810&page.page=1&page.size=50&page.sort=startTime,ASC
        Map<String, String> query = new HashMap<>();
        query.put("filter.eq.launchId", Constants.LAUNCH_ID);
        query.put("filter.eq.parentId", String.valueOf(parentId));
        query.put("filter.in.status", "FAILED,INTERRUPTED");
        query.put("page.page", "1");
        query.put("page.size", "70");
        query.put("page.sort", "startTime,ASC");
        return APIHelper.getTests(query);
    }

    private List<String> getComments(List<Test> tests) {
        return tests.stream()
            .map(e -> e.getIssue().getComment())
            .distinct()
            .collect(Collectors.toList());
    }

    @SneakyThrows
    private void finish() {
        this.excelHelper.saveAndClose();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    private static class RowData {
        @AllArgsConstructor
        enum Header {
            FEATURE("Feature", 60),
            STATUS("Status", 15),
            TOTAL("Total", 10),
            PASSED("Passed", 10),
            SKIPPED("Failed", 10),
            FAILED("Skipped", 10),
            BUG("Bug", 30);

            String title;
            Integer width;

            static List<String> headers() {
                return Arrays.stream(Header.values()).map(h -> h.title).collect(Collectors.toList());
            }

            static List<Integer> widths() {
                return Arrays.stream(Header.values()).map(h -> h.width).collect(Collectors.toList());
            }
        }

        String feature;
        String status;
        int total;
        int passed;
        int skipped;
        int failed;
        String bug;

        public RowData(Suite suite, String comment) {
            this.feature = suite.getName();
            this.status = suite.getStatus();
            this.total = suite.getStatistics().getExecutions().getTotal();
            this.passed = suite.getStatistics().getExecutions().getPassed();
            this.failed = suite.getStatistics().getExecutions().getFailed();
            this.skipped = suite.getStatistics().getExecutions().getSkipped();
            this.bug = comment;
        }

        public List<String> toList() {

            return Arrays.asList(this.feature, status,
                String.valueOf(this.total),
                String.valueOf(this.passed),
                String.valueOf(this.failed),
                String.valueOf(this.skipped),
                this.bug
            );
        }
    }
}
