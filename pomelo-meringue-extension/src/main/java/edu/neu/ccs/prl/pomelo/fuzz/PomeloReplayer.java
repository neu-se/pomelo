package edu.neu.ccs.prl.pomelo.fuzz;

import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;
import edu.neu.ccs.prl.meringue.Replayer;
import edu.neu.ccs.prl.meringue.ReplayerManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.function.Consumer;

public final class PomeloReplayer implements Replayer {
    private String testMethodName;
    private String testClassName;
    private ClassLoader testClassLoader;

    @Override
    public void configure(String testClassName, String testMethodName, ClassLoader classLoader) {
        if (testClassName == null || testMethodName == null || classLoader == null) {
            throw new NullPointerException();
        }
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        this.testClassLoader = classLoader;
    }

    @Override
    public void accept(ReplayerManager manager) throws Throwable {
        try {
            FuzzForkMain.run(testClassName, testMethodName, testClassLoader, new ReplayGuidance(manager));
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    private static final class ReplayGuidance implements Guidance {
        private final ReplayerManager manager;

        private ReplayGuidance(ReplayerManager manager) {
            this.manager = manager;
        }

        @Override
        public InputStream getInput() throws GuidanceException {
            try {
                return new ByteArrayInputStream(Files.readAllBytes(manager.nextInput().toPath()));
            } catch (IOException e) {
                throw new GuidanceException(e);
            }
        }

        @Override
        public boolean hasInput() {
            return manager.hasNextInput();
        }

        @Override
        public void handleResult(Result result, Throwable error) throws GuidanceException {
            try {
                manager.handleResult(result == Result.FAILURE ? error : null);
            } catch (IOException e) {
                throw new GuidanceException(e);
            }
        }

        @Override
        public Consumer<TraceEvent> generateCallBack(Thread thread) {
            return (t) -> {
            };
        }
    }
}
