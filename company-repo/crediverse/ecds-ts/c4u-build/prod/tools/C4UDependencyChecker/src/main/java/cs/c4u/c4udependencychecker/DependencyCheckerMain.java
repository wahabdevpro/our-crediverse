/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.c4u.c4udependencychecker;

import cs.c4u.c4udependencychecker.rules.StylizeDependencyLineRule;
import cs.c4u.c4udependencychecker.rules.IRule;
import cs.c4u.c4udependencychecker.rules.IgnoreDependencyRule;
import cs.c4u.c4udependencychecker.rules.ProcessPackageRule;
import cs.c4u.utils.FileUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author blueDaemon <blouduiwel@gmail.com>
 */
public class DependencyCheckerMain 
{
    DependencyChecker dependencyChecker = null;
    
    private static final List<String> ecdsConnectorsList = Arrays.asList(new String [] {
        "AirConnector", 
        "ArchivingConnector", 
        "C4USuTConnector", 
        "DataWarehouseConnector", 
        "DiagnosticConnector", 
        "FileConnector",
        "HmxConnector",
        "HuxConnector",
        "KerberosConnector",
        "LifecycleConnector",
        "MySqlConnector",
        "SmppConnector",
        "SnmpConnector",
        "SoapConnector",
        "UIConnector"
    });
    
    private static final List<String> ecdsServicesList = Arrays.asList(new String [] {
        "AirSimService",
        "NumberPlanService",
        "ReportingService",
        "SecurityService",
        "CreditDistributionService"
    });
    
    // ------ Common Rules -----
    /**
     * Do not draw nodes that are not of type service or connector
     */
    IRule ignoreNonServicesAndConnectors = new IgnoreDependencyRule() 
    {
        @Override
        public boolean ignore(String dependencyName, DependencyChecker.ProjectType type) 
        {
            boolean accept = ((type == DependencyChecker.ProjectType.service) 
                || (type == DependencyChecker.ProjectType.connector));

            return (! accept);
        }
    };

    /**
     * Colourize connections between either (connector and another connector) or (service and another service)
     */
    IRule colourConnectedServicesAndConnectors = new StylizeDependencyLineRule() {
        @Override
        public boolean stylizeMe(String projectName, DependencyChecker.ProjectType projectType, String dependencyName, DependencyChecker.ProjectType dependencyType) {
            return
                (projectType == DependencyChecker.ProjectType.service && dependencyType == DependencyChecker.ProjectType.service)
                || (projectType == DependencyChecker.ProjectType.connector && dependencyType == DependencyChecker.ProjectType.connector);
        }

        @Override
        public String getArrowStyle() {
            return "-[#blue,bold]->";
        }

    };
    
    /**
     * Colourize connections between connectors and services
     */
    IRule colourInterConnectedServicesAndConnectors = new StylizeDependencyLineRule() {
        @Override
        public boolean stylizeMe(String projectName, DependencyChecker.ProjectType projectType, String dependencyName, DependencyChecker.ProjectType dependencyType) {
            return
                (projectType == DependencyChecker.ProjectType.service && dependencyType == DependencyChecker.ProjectType.connector)
                || (projectType == DependencyChecker.ProjectType.connector && dependencyType == DependencyChecker.ProjectType.service);
        }

        @Override
        public String getArrowStyle() {
            return "-[#Red,bold]->";
        }
    };
    
    /**
     * Rule for processing all ECDS required plugins and their dependencies
     */
    IRule processOnlyCreditDistributionService = new ProcessPackageRule() 
    {

        @Override
        public boolean process(String projectName, DependencyChecker.ProjectType projectType) 
        {
            return (DependencyCheckerMain.ecdsConnectorsList.contains(projectName) 
                || DependencyCheckerMain.ecdsServicesList.contains(projectName));
        }
    };
    
    DependencyChecker.ProjectType[] alwaysIncludeServicesAndConnectorsPackage = new DependencyChecker.ProjectType[] {DependencyChecker.ProjectType.service, DependencyChecker.ProjectType.connector};

    public DependencyCheckerMain(DependencyChecker dependencyChecker)
    {
        this.dependencyChecker = dependencyChecker;
    }
    
    public void checkEcdsServiceAndConnectorDependencies() throws IOException 
    {
        IRule [] rules = new IRule[] {processOnlyCreditDistributionService, ignoreNonServicesAndConnectors, colourConnectedServicesAndConnectors, colourInterConnectedServicesAndConnectors};
        dependencyChecker.createDependencyDiagram("EcdsOnlyServiceAndConnectors", rules, alwaysIncludeServicesAndConnectorsPackage);        
    }
    
    public void checkAllConnectorAndServiceDependencies() throws IOException
    {
        IRule processOnlyServicesAndConnectors = new ProcessPackageRule() 
        {
        
            @Override
            public boolean process(String projectName, DependencyChecker.ProjectType projectType) 
            {
                return (projectType == DependencyChecker.ProjectType.service || projectType == DependencyChecker.ProjectType.connector);
            }
            
        };
        
        IRule [] rules = new IRule[] {processOnlyServicesAndConnectors, ignoreNonServicesAndConnectors, colourConnectedServicesAndConnectors, colourInterConnectedServicesAndConnectors};
        dependencyChecker.createDependencyDiagram("C4UServicesAndConectors", rules, alwaysIncludeServicesAndConnectorsPackage);
    }
    
    public void createEcdsDependencyDiagram() throws IOException
    {
        IRule [] rules = new IRule[] {processOnlyCreditDistributionService, colourConnectedServicesAndConnectors, colourInterConnectedServicesAndConnectors};
        dependencyChecker.createDependencyDiagram("EcdsDependencyDiagram", rules, alwaysIncludeServicesAndConnectorsPackage);        
    }
    
    public void createFullTree() throws IOException
    {
        dependencyChecker.createDependencyDiagram("FullC4uDependencyGraph", new IRule[] {colourConnectedServicesAndConnectors, colourInterConnectedServicesAndConnectors}, null);
    }
    
    public void testCoverage() throws IOException
    {
        IRule processTestsAndDependencies = new ProcessPackageRule() 
        {
        
            @Override
            public boolean process(String projectName, DependencyChecker.ProjectType projectType) 
            {
                return (projectType == DependencyChecker.ProjectType.test);
            }
            
        };
        
        IRule colourTestDependencies = new StylizeDependencyLineRule() {
            @Override
            public boolean stylizeMe(String projectName, DependencyChecker.ProjectType projectType, String dependencyName, DependencyChecker.ProjectType dependencyType) {
                return
                    (projectType == DependencyChecker.ProjectType.test);
            }

            @Override
            public String getArrowStyle() {
                return "-[#Blue,bold]->";
            }
        };
        dependencyChecker.createDependencyDiagram("TestCoverage", new IRule[] {processTestsAndDependencies, colourTestDependencies}, null);        
    }
    
    public static void main(String[] args) throws IOException, Exception
    {
        String rootPath = FileUtils.findRoot();
        DependencyChecker dc = new DependencyChecker(rootPath);
        
        DependencyCheckerMain main = new DependencyCheckerMain(dc);
        main.checkEcdsServiceAndConnectorDependencies();
        main.checkAllConnectorAndServiceDependencies();
        main.createFullTree();
        main.createEcdsDependencyDiagram();
        main.testCoverage();
        
    }
    
}
