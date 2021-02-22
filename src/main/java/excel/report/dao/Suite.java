package excel.report.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Suite {

    @JsonProperty("id")
    int id;
    @JsonProperty("name")
    String name;
    @JsonProperty("status")
    String status;
    @JsonProperty("path")
    String path;
    @JsonProperty("statistics")
    Statistics statistics;

    public String getName() {
        return this.name.replace("Feature: ", "");
    }

    public String getStatus() {
        // Update status
        if (this.statistics.executions.total == 0) this.status = "SKIPPED";
        else if (this.statistics.executions.failed == 0 && this.statistics.executions.passed == 0) this.status = "SKIPPED";
        else if (this.statistics.executions.failed == 0 && this.statistics.executions.passed > 0) this.status = "PASSED";

        return this.status;
    }

    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Statistics {
        @JsonProperty("executions")
        Executions executions;

        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Executions {
            @JsonProperty("total")
            int total;
            @JsonProperty("failed")
            int failed;
            @JsonProperty("passed")
            int passed;
            @JsonProperty("skipped")
            int skipped;
        }
    }
}
