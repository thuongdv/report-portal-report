package excel.report.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Test {
    @JsonProperty("status")
    String status;

    @JsonProperty("issue")
    Issue issue;

    public String getComment() {
        val comment = this.getIssue().getComment();
        return comment == null ? "" : this.getStatus() + ": " + comment;
    }

    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Issue {
        @JsonProperty("comment")
        String comment;
    }
}
