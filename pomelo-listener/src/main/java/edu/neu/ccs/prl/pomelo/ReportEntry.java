package edu.neu.ccs.prl.pomelo;

import java.util.List;
import java.util.stream.Collectors;

public class ReportEntry {
    private final String projectId;
    private final String pluginName;
    private final String executionId;
    private final String testClassName;
    private final String testMethodName;
    private final String runnerClassName;
    private final boolean unambiguous;
    private final TestResult originalResult;
    private final TestResult isolatedResult;

    public ReportEntry(String projectId, String pluginName, String executionId, String testClassName,
                       String testMethodName, String runnerClassName, boolean unambiguous, TestResult originalResult,
                       TestResult isolatedResult) {
        if (projectId == null || pluginName == null || executionId == null || testClassName == null ||
                testMethodName == null || runnerClassName == null) {
            throw new NullPointerException();
        }
        this.projectId = projectId;
        this.pluginName = pluginName;
        this.executionId = executionId;
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        this.runnerClassName = runnerClassName;
        this.unambiguous = unambiguous;
        this.originalResult = originalResult;
        this.isolatedResult = isolatedResult;
    }

    public String toCsvRow() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s", projectId, pluginName, executionId, testClassName,
                             testMethodName, runnerClassName, unambiguous, originalResult, isolatedResult);
    }

    public static List<String> toCsvRows(List<ReportEntry> records) {
        return records.stream().map(ReportEntry::toCsvRow).collect(Collectors.toList());
    }


    public static String getCsvHeader() {
        return "project_id,plugin_name,execution_id,test_class_name,test_method_name,runner_class_name,unambiguous," +
                "original_result,isolated_result";
    }
}