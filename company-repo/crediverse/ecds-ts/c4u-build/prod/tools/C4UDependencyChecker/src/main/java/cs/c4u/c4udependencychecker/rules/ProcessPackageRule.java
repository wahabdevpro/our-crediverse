package cs.c4u.c4udependencychecker.rules;

import cs.c4u.c4udependencychecker.DependencyChecker;

public abstract class ProcessPackageRule implements IRule
{

    public abstract boolean process(String projectName, DependencyChecker.ProjectType projectType);
}
