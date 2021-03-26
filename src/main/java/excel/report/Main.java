package excel.report;

import helper.Constants;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;

import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        val rp2Excel = new RP2Excel();
        rp2Excel.generate(getOutputFile());
    }

    @SneakyThrows
    private static String getOutputFile() {
        val pwd = System.getProperty("user.dir");
        val templateFile = Paths.get(pwd, Constants.REPORT_TEMPLATE_FILE);
        val outputFile = Paths.get(pwd, Constants.REPORT_FILE);
        FileUtils.copyFile(templateFile.toFile(), outputFile.toFile());

        return outputFile.toString();
    }
}
