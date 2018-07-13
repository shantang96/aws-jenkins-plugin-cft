package io.jenkins.plugins.sample;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import jenkins.tasks.SimpleBuildStep;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

import java.lang.*;

public class HelloWorldBuilder extends Builder implements SimpleBuildStep {

    private final String name;
    public final String cloudformationtemplate;
    public final String stackname;
    public final String parameters;
    public final String keyvaluepairs;
    private boolean useFrench;

    public void newFunction()
    {
    	
    }
    
    @DataBoundConstructor
    public HelloWorldBuilder(String name, String cloudformationtemplate, String stackname, String parameters, String keyvaluepairs) {
//     	System.out.println("GOT HERE - dataBoundConstructor() :" + cloudformationtemplate);
        this.name = name;
        this.cloudformationtemplate = cloudformationtemplate;
        this.stackname = stackname;
        this.parameters = parameters;
        this.keyvaluepairs = keyvaluepairs;
    }

    public String getName() {
//     	System.out.println("GOT HERE - getName()");
        return name;
    }
    
    // public void setCloudFormationTemplate(String cloudFormationTemplate)
//     {
//     	System.out.println("GOT HERE - setCloudFormationTemplate(): " + cloudFormationTemplate);
//     	this.cloudformationtemplate = cloudFormationTemplate;
//     }
    
    public String getCloudFormationTemplate() {
//         System.out.println("GOT HERE - getCloudFormationTemplate()");
        return cloudformationtemplate;
    }


    public boolean isUseFrench() {
        return useFrench;
    }

    @DataBoundSetter
    public void setUseFrench(boolean useFrench) {
        this.useFrench = useFrench;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        if (useFrench) {
            listener.getLogger().println("Bonjour, " + name + "!");
        } else {
            listener.getLogger().println("Deploying CFT Template for user: " + name);
            listener.getLogger().println("");

            listener.getLogger().println("WARNING: Remember to destroy any AWS resources generated by this plugin by logging in to the AWS Console.");
			    		
    		String executeStr = "pwd"; //temporarily.
         	String CFTFilePath = cloudformationtemplate;
   			String stackName = stackname;
   			String parametersPairs = parameters;
   			String keyValuePairs= keyvaluepairs; //Should be in the form k1=v1 k2=v2 k3=v3 (separated by spaces)

            listener.getLogger().println("Cloud Formation Template File Path = " + CFTFilePath);
            listener.getLogger().println("Stack Name = " + stackName);
            listener.getLogger().println("Parameters = " + parametersPairs);
            listener.getLogger().println("Key Value Pairs = " + keyValuePairs);
            String executeStrX = "";
            if(!parametersPairs.isEmpty() && !keyValuePairs.isEmpty())
            {
			 	executeStrX = "aws cloudformation deploy --template-file " + CFTFilePath +  " --stack-name " + stackName + " --parameter-overrides " + parametersPairs + " --tags " + keyValuePairs + " --no-execute-changeset";
			}
			
			if(parametersPairs.isEmpty() && keyValuePairs.isEmpty())
			{
				executeStrX = "aws cloudformation deploy --template-file " + CFTFilePath +  " --stack-name " + stackName + " --no-execute-changeset";
			}
			
			if(parametersPairs.isEmpty() && !keyValuePairs.isEmpty())
			{
				executeStrX = "aws cloudformation deploy --template-file " + CFTFilePath +  " --stack-name " + stackName + " --tags " + keyValuePairs + " --no-execute-changeset";
			}
			if(!parametersPairs.isEmpty() && keyValuePairs.isEmpty())
            {
			 	executeStrX = "aws cloudformation deploy --template-file " + CFTFilePath +  " --stack-name " + stackName + " --parameter-overrides " + parametersPairs + " --no-execute-changeset";
			}
			
			
			listener.getLogger().println("");
			listener.getLogger().println("Executing Command  = " + executeStrX);

    		try {
//     			ProcessBuilder pb = new ProcessBuilder("aws", "cloudformation", "deploy", "--template-file", CFTFilePath, "--stack-name", stackName, "--parameters-overrides", parametersPairs, "--tags", keyValuePairs, "--no-execute-changeset");
//     			Process p = pb.start();
				Process p = Runtime.getRuntime().exec(executeStrX);
    			listener.getLogger().println("\nExecuting.............");
    			//Thread.sleep(10000);
    			p.waitFor();
    			String output = IOUtils.toString(p.getInputStream());
    			String errorStream = IOUtils.toString(p.getErrorStream());
    			listener.getLogger().println("Output:\n" + output);
    			listener.getLogger().println("Errors:\n" + errorStream);

    		} catch (IOException e) {
    			e.printStackTrace();
				listener.getLogger().println("");
    			listener.getLogger().println("FAILED TO DEPLOY CLOUD FORMATION TEMPLATE. Check command line output for info.");
    			listener.getLogger().println("To debug run the following command on your command line.");
    			listener.getLogger().println(executeStrX);
    		}
        }
    }

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckName(@QueryParameter String value, @QueryParameter boolean useFrench)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error(Messages.HelloWorldBuilder_DescriptorImpl_errors_missingName());
            if (value.length() < 4)
                return FormValidation.warning(Messages.HelloWorldBuilder_DescriptorImpl_warnings_tooShort());
            if (!useFrench && value.matches(".*[éáàç].*")) {
                return FormValidation.warning(Messages.HelloWorldBuilder_DescriptorImpl_warnings_reallyFrench());
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.HelloWorldBuilder_DescriptorImpl_DisplayName();
        }

    }

}
