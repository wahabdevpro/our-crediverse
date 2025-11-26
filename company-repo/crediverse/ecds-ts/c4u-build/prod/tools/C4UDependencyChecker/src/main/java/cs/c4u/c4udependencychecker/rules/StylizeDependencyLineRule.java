/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.c4u.c4udependencychecker.rules;

import cs.c4u.c4udependencychecker.DependencyChecker;

public abstract class StylizeDependencyLineRule implements IRule
{
    public abstract boolean stylizeMe(String projectName, DependencyChecker.ProjectType projectType, String dependencyName, DependencyChecker.ProjectType dependencyType);
    
    public abstract String getArrowStyle();
}
