package edu.neu.ccs.prl.pomelo;

public final class TestRecord {
    private final String project;
    private final String plugin;
    private final String execution;
    private final String testClassName;
    private final String testMethodName;
    private final String runnerClassName;
    private boolean failed;
    private boolean ambiguous;

    public TestRecord(String project, String plugin, String execution, String testClassName, String testMethodName,
                      String runnerClassName, boolean failed, boolean ambiguous) {
        if (project == null || plugin == null || execution == null || testClassName == null || testMethodName == null ||
                runnerClassName == null) {
            throw new NullPointerException();
        }
        this.project = project;
        this.plugin = plugin;
        this.execution = execution;
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        this.runnerClassName = runnerClassName;
        this.failed = failed;
        this.ambiguous = ambiguous;
    }

    public String getProject() {
        return project;
    }

    public String getPlugin() {
        return plugin;
    }

    public String getExecution() {
        return execution;
    }

    public String getTestClassName() {
        return testClassName;
    }

    public String getTestMethodName() {
        return testMethodName;
    }

    public String getRunnerClassName() {
        return runnerClassName;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public boolean isAmbiguous() {
        return ambiguous;
    }

    public void setAmbiguous(boolean ambiguous) {
        this.ambiguous = ambiguous;
    }

    @Override
    public int hashCode() {
        int result = project.hashCode();
        result = 31 * result + plugin.hashCode();
        result = 31 * result + execution.hashCode();
        result = 31 * result + testClassName.hashCode();
        result = 31 * result + testMethodName.hashCode();
        result = 31 * result + runnerClassName.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof TestRecord)) {
            return false;
        }
        TestRecord that = (TestRecord) o;
        if (!project.equals(that.project)) {
            return false;
        }
        if (!plugin.equals(that.plugin)) {
            return false;
        }
        if (!execution.equals(that.execution)) {
            return false;
        }
        if (!testClassName.equals(that.testClassName)) {
            return false;
        }
        if (!testMethodName.equals(that.testMethodName)) {
            return false;
        }
        return runnerClassName.equals(that.runnerClassName);
    }

    @Override
    public String toString() {
        return "TestRecord{" +
                "project='" + project + '\'' +
                ", plugin='" + plugin + '\'' +
                ", execution='" + execution + '\'' +
                ", testClassName='" + testClassName + '\'' +
                ", testMethodName='" + testMethodName + '\'' +
                ", runnerClassName='" + runnerClassName + '\'' +
                ", failed=" + failed +
                ", ambiguous=" + ambiguous +
                '}';
    }

    public String toCsvRow() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s", project, plugin, execution, testClassName,
                             testMethodName, runnerClassName, failed, ambiguous);
    }

    public static String getCsvHeader() {
        return "project,plugin,execution,test_class,test_method,runner,failed,ambiguous";
    }
}
