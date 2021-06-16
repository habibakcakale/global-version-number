package ruby.jenkins.vnumber;

import com.google.inject.Inject;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.TaskListener;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.*;
import java.util.Scanner;
import java.util.Set;

public class GlobalVersionNumberStep extends AbstractStepImpl {

    private final String name;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public GlobalVersionNumberStep(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

//    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
//        boolean isRemote = build.getWorkspace().isRemote();
//        listener.getLogger().printf("%sb", isRemote);
//        String name = build.getProject().getName();
//
//        if (!isRemote) {
//            FilePath fp = new FilePath(build.getWorkspace(), name + ".vnumber");
//            try {
//                int newBuildNumber = 0;
//                if (fp.exists()) {
//                    String input = fp.readToString();
//                    newBuildNumber = Integer.getInteger(input) + 1;
//                }
//                fp.write(String.valueOf(newBuildNumber), "utf-8");
//                build.getBuildVariables().put("Global_Build_Number", newBuildNumber);
//            } catch (Exception e) {
//                listener.fatalError("Error while checking file", e);
//            }
//        }
//        return true;
//    }

    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(Execution.class);
        }

        @Override
        public String getFunctionName() {
            return "GlobalVersionNumber";
        }

        @Override
        public String getDisplayName() {
            return "Determine Correct Version Number Between Branches.";
        }

        @Override
        public Set<Class<?>> getProvidedContext() {
            return super.getProvidedContext();
        }
    }

    public static class Execution extends AbstractSynchronousStepExecution<String> {
        //        @StepContextParameter
//        private transient Run run;
        @StepContextParameter
        private transient EnvVars envVars;
        @Inject(optional = true)
        private transient GlobalVersionNumberStep step;
        @StepContextParameter
        TaskListener listener;
        @StepContextParameter
        FilePath workspace;
        //        @StepContextParameter
//        Launcher launcher;
        private static final long serialVersionUID = 10L;

        @Override
        protected String run() throws Exception {
            PrintStream logger = listener.getLogger();
            String path = StringUtils.stripEnd(workspace.getRemote(), workspace.getBaseName()) + "global.vnumber";
            logger.printf("%s", path);
            File fp = new File(path);
            int newBuildNumber = 0;
            if (fp.exists()) {
                newBuildNumber = readBuildNumber(fp);
            } else {
                if (!fp.createNewFile()) {
                    throw new FileNotFoundException("Coudln't create file in " + fp.getAbsolutePath());
                }
            }
            writeBuildNumber(fp, newBuildNumber);
            this.envVars.put("Global_Build_Number", String.valueOf(newBuildNumber));
            return String.valueOf(newBuildNumber);
        }

        int readBuildNumber(File fp) throws IOException {
            Scanner scanner = new Scanner(fp, "utf-8");
            int fileBuildNumber = 0;
            if (scanner.hasNextInt()) {
                fileBuildNumber = scanner.nextInt();
            }
            int newBuildNumber = fileBuildNumber + 1;
            return newBuildNumber;
        }

        void writeBuildNumber(File fp, int newBuildNumber) throws IOException {
            FileWriterWithEncoding writer = new FileWriterWithEncoding(fp, "utf-8");
            writer.write(String.valueOf(newBuildNumber));
            writer.flush();
            writer.close();
        }
    }
}

