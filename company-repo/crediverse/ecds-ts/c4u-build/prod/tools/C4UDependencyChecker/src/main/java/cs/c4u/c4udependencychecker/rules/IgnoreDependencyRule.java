package cs.c4u.c4udependencychecker.rules;

import cs.c4u.c4udependencychecker.DependencyChecker;

public abstract class IgnoreDependencyRule implements IRule
{
    
    public abstract boolean ignore(String dependencyName, DependencyChecker.ProjectType dependencyType);
    
}
