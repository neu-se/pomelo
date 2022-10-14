package edu.neu.ccs.prl.pomelo.scan;

public class ReportEntry {
    private final String projectId;
    private final TestPluginType plugin;
    private final String executionId;
    private final String testClassName;
    private final String testMethodName;
    private final String runnerClassName;
    private final boolean unambiguous;
    private final TestResult originalResult;
    private final TestResult isolatedResult;
    private final GeneratorsStatus generatorsStatus;

    public ReportEntry(String projectId, TestPluginType plugin, String executionId, String testClassName,
                       String testMethodName, String runnerClassName, boolean unambiguous, TestResult originalResult,
                       TestResult isolatedResult, GeneratorsStatus generatorsStatus) {
        if (projectId == null || plugin == null || executionId == null || testClassName == null ||
                testMethodName == null || runnerClassName == null || generatorsStatus == null) {
            throw new NullPointerException();
        }
        this.projectId = projectId;
        this.plugin = plugin;
        this.executionId = executionId;
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        this.runnerClassName = runnerClassName;
        this.unambiguous = unambiguous;
        this.originalResult = originalResult;
        this.isolatedResult = isolatedResult;
        this.generatorsStatus = generatorsStatus;
    }

    public ReportEntry(String projectId, TestPluginType plugin, String executionId, TestRecord record) {
        this(projectId, plugin, executionId,
             record.getTestClassName(), record.getTestMethodName(),
             record.getRunnerClassName(), record.isUnambiguous(),
             record.passed() ? TestResult.PASSED : TestResult.FAILED,
             TestResult.NONE, GeneratorsStatus.UNKNOWN);
    }

    public ReportEntry withIsolatedResult(TestResult isolatedResult) {
        return new ReportEntry(projectId, plugin, executionId, testClassName, testMethodName, runnerClassName,
                               unambiguous, originalResult, isolatedResult, generatorsStatus);
    }

    public ReportEntry withGeneratorsStatus(GeneratorsStatus generatorsStatus) {
        return new ReportEntry(projectId, plugin, executionId, testClassName, testMethodName, runnerClassName,
                               unambiguous, originalResult, isolatedResult, generatorsStatus);
    }

    public String toCsvRow() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", projectId, plugin, executionId, testClassName,
                             testMethodName, runnerClassName, unambiguous, originalResult, isolatedResult,
                             generatorsStatus);
    }

    public static String getCsvHeader() {
        return "project_id,plugin,execution_id,test_class_name,test_method_name,runner_class_name,unambiguous," +
                "original_result,isolated_result,generators_status";
    }
}
