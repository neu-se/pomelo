package edu.neu.ccs.prl.pomelo.scan;

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
    private final GeneratorsStatus generatorsStatus;

    public ReportEntry(String projectId, String pluginName, String executionId, String testClassName,
                       String testMethodName, String runnerClassName, boolean unambiguous, TestResult originalResult,
                       TestResult isolatedResult, GeneratorsStatus generatorsStatus) {
        if (projectId == null || pluginName == null || executionId == null || testClassName == null ||
                testMethodName == null || runnerClassName == null || generatorsStatus == null) {
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
        this.generatorsStatus = generatorsStatus;
    }

    public ReportEntry(String projectId, String pluginName, String executionId, TestRecord record) {
        this(projectId, pluginName, executionId,
             record.getTestClassName(), record.getTestMethodName(),
             record.getRunnerClassName(), record.isUnambiguous(),
             record.passed() ? TestResult.PASSED : TestResult.FAILED,
             TestResult.NONE, GeneratorsStatus.UNKNOWN);
    }

    public ReportEntry withIsolatedResult(TestResult isolatedResult) {
        return new ReportEntry(projectId, pluginName, executionId, testClassName, testMethodName, runnerClassName,
                               unambiguous, originalResult, isolatedResult, generatorsStatus);
    }

    public ReportEntry withGeneratorsStatus(GeneratorsStatus generatorsStatus) {
        return new ReportEntry(projectId, pluginName, executionId, testClassName, testMethodName, runnerClassName,
                               unambiguous, originalResult, isolatedResult, generatorsStatus);
    }

    public String toCsvRow() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", projectId, pluginName, executionId, testClassName,
                             testMethodName, runnerClassName, unambiguous, originalResult, isolatedResult,
                             generatorsStatus);
    }

    public static String getCsvHeader() {
        return "project_id,plugin_name,execution_id,test_class_name,test_method_name,runner_class_name,unambiguous," +
                "original_result,isolated_result,generators_status";
    }
}
