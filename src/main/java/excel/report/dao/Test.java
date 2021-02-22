package excel.report.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Test {
    @JsonProperty("issue")
    Issue issue;

    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Issue {
        @JsonProperty("comment")
        String comment;

        public String getComment() {
            return this.comment == null ? "" : this.comment;
        }
    }
}
