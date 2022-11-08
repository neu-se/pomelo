package edu.neu.ccs.prl.pomelo.fuzz;

import edu.neu.ccs.prl.meringue.*;
import janala.instrument.SnoopInstructionTransformer;
import org.objectweb.asm.ClassVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class PomeloFuzzFramework implements FuzzFramework {
    private File corpusDir;
    private File failuresDir;
    private JvmLauncher launcher;
    private File frameworkJar;

    @Override
    public void initialize(CampaignConfiguration config, Properties frameworkArguments) throws IOException {
        File outputDir = config.getOutputDir();
        FileUtil.ensureDirectory(outputDir);
        corpusDir = new File(outputDir, "corpus");
        failuresDir = new File(outputDir, "failures");
        List<String> javaOptions = new ArrayList<>(config.getJavaOptions());
        File instrumentJar = FileUtil.getClassPathElement(SnoopInstructionTransformer.class);
        File asmJar = FileUtil.getClassPathElement(ClassVisitor.class);
        javaOptions.add(
                String.format("-Xbootclasspath/a:%s:%s", instrumentJar.getAbsolutePath(), asmJar.getAbsolutePath()));
        javaOptions.add("-javaagent:" + instrumentJar.getAbsolutePath());
        File janalaFile = new File(outputDir, "janala.conf");
        writeJanalaConfiguration(janalaFile);
        javaOptions.add("-Djanala.conf=" + janalaFile.getAbsolutePath());
        frameworkJar = new File(frameworkArguments.getProperty("frameworkJar"));
        javaOptions.add("-cp");
        javaOptions.add(
                config.getTestClassPathJar().getAbsolutePath() + File.pathSeparator + frameworkJar.getAbsolutePath());
        String[] arguments =
                new String[]{config.getTestClassName(), config.getTestMethodName(), outputDir.getAbsolutePath()};
        boolean quiet = Boolean.parseBoolean(frameworkArguments.getProperty("quiet", "false"));
        launcher = JvmLauncher.fromMain(config.getJavaExec(), FuzzForkMain.class.getName(),
                                        javaOptions.toArray(new String[0]), !quiet, arguments, config.getWorkingDir(),
                                        config.getEnvironment());

    }

    @Override
    public Process startCampaign() throws IOException {
        FileUtil.ensureEmptyDirectory(corpusDir);
        FileUtil.ensureEmptyDirectory(failuresDir);
        return launcher.launch();
    }

    @Override
    public File[] getCorpusFiles() {
        return corpusDir.listFiles();
    }

    @Override
    public File[] getFailureFiles() {
        return failuresDir.listFiles();
    }

    @Override
    public Class<? extends Replayer> getReplayerClass() {
        return PomeloReplayer.class;
    }

    @Override
    public Collection<File> getRequiredClassPathElements() {
        return Collections.singletonList(frameworkJar);
    }

    @Override
    public boolean canRestartCampaign() {
        return false;
    }

    @Override
    public Process restartCampaign() {
        throw new UnsupportedOperationException();
    }

    private static void writeJanalaConfiguration(File file) throws FileNotFoundException {
        List<String> excludes =
                Arrays.asList("java/", "com/sun/proxy/", "com/intellij/", "edu/berkeley/cs/jqf/", "org/junit/",
                              "com/pholser/junit/quickcheck/", "ru/vyarus/java/generics/resolver/",
                              "org/javaruntype/", "org/hamcrest/", "org/omg/", "org/netbeans/",
                              "edu/neu/ccs/prl/pomelo");
        List<String> includes = Arrays.asList("edu/berkeley/cs/jqf/examples", "java/text", "java/time",
                                              "com/sun/imageio",
                                              "com/pholser/junit/quickcheck/internal",
                                              "com/pholser/junit/quickcheck/generator");
        try (PrintWriter out = new PrintWriter(file)) {
            out.println("janala.excludes=" + String.join(",", excludes));
            out.println("janala.includes=" + String.join(",", includes));
        }
    }
}
