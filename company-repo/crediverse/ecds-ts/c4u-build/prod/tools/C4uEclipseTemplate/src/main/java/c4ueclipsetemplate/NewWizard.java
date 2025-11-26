package c4ueclipsetemplate;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.osgi.framework.Bundle;

public class NewWizard extends Wizard implements INewWizard, SelectionListener
{

	private NewWizard me = this;
	private WizardNewProjectCreationPage _pageOne;

	private String type;
	private boolean hasConfiguration = false;

	public NewWizard()
	{
		setWindowTitle("C4U Plugin Wizard");
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection)
	{
	}

	@SuppressWarnings("resource")
	@Override
	public boolean performFinish()
	{
		String actualType = type.substring(type.indexOf('_') + 1);
		String projectName = _pageOne.getProjectName().contains(actualType) ? _pageOne.getProjectName() : _pageOne.getProjectName() + actualType;
		String projectShortname = projectName.substring(0, projectName.indexOf(actualType));
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		try
		{
			// Create the project and open it in the IDE
			project.create(null);
			project.open(null);

			// Give it java aspects
			IProjectDescription description = project.getDescription();
			description.setNatureIds(new String[] { JavaCore.NATURE_ID });
			project.setDescription(description, null);

			// Create Java Project
			IJavaProject javaProject = JavaCore.create(project);

			// Create classes folder
			IFolder classesFolder = project.getFolder("classes");
			classesFolder.create(false, true, null);
			javaProject.setOutputLocation(classesFolder.getFullPath(), null);

			// Create the java folder
			IFolder javaFolder = project.getFolder("java");
			javaFolder.create(false, true, null);

			IFile jarProperties = project.getFile("jar.properties");
			String contents = "dest.jar=dist/" + projectName + ".jar\n" + "main.class=hxc." + actualType.toLowerCase() + "s." + projectShortname.toLowerCase() + "." + projectName + "\n"
					+ "include.libs=true";
			InputStream source = new ByteArrayInputStream(contents.getBytes());
			jarProperties.create(source, false, null);

			// Add libraries to project class path
			IPackageFragmentRoot rootPackage = javaProject.getPackageFragmentRoot(javaFolder);

			ArrayList<IClasspathEntry> classPaths = new ArrayList<>();
			classPaths.add(JavaCore.newSourceEntry(rootPackage.getPath()));
			classPaths.add(JavaRuntime.getDefaultJREContainerEntry());
			classPaths.add(JavaCore.newProjectEntry(new Path("/CoreInterfaces")));

			if (hasConfiguration)
				classPaths.add(JavaCore.newProjectEntry(new Path("/Configuration")));

			if (type.contains("Vas"))
				classPaths.add(JavaCore.newProjectEntry(new Path("/SoapProtocol")));

			javaProject.setRawClasspath(classPaths.toArray(new IClasspathEntry[0]), null);

			// Create package
			String packageName = "hxc." + actualType.toLowerCase() + "s." + projectShortname.toLowerCase();
			IPackageFragment pack = javaProject.getPackageFragmentRoot(javaFolder).createPackageFragment(packageName, false, null);

			StringBuffer buffer = new StringBuffer();
			Bundle bundle = Activator.getDefault().getBundle();
			URL fileURL = bundle.getEntry("/templates/Generic");
			String uri = FileLocator.resolve(fileURL).getFile();

			BufferedReader reader = new BufferedReader(new FileReader(new File(uri)));

			String line;
			boolean skip = false, oneSkip = false;
			boolean isVas = type.contains("Vas");
			while ((line = reader.readLine()) != null)
			{
				while (line.indexOf('$') > -1)
				{
					if (line.indexOf('^') > -1)
					{
						String key = line.substring(line.indexOf('$') + 1, line.indexOf('^'));
						String replacement = "";
						switch (key.toLowerCase())
						{
							case "package":
								replacement = packageName;
								break;

							case "name":
								replacement = projectShortname;
								break;

							case "getconfiguration":
								if (hasConfiguration)
								{
									replacement = "config";
								}
								else
								{
									replacement = "null";
								}
								break;

							case "setconfiguration":
								if (hasConfiguration)
								{
									replacement = "config = (" + projectShortname + "Configuration) config;";
								}
								break;

							case "perms":
								if (hasConfiguration)
								{
									replacement = "@Perms(perms = { @Perm(category = \"\", description = \"\", name = \"\") })";
								}
								break;

							case "path":
								if (hasConfiguration)
								{
									if (isVas)
									{
										replacement = "Vas Services";
									}
									else
									{
										replacement = "Technical Settings";
									}
								}
								break;

							case "config_start":
								if (!hasConfiguration)
								{
									skip = true;
								}
								break;

							case "config_stop":
								skip = false;
								break;

							case "extends":

								if (isVas)
								{
									replacement = "extends VasService";
								}

								break;

							case "type":

								replacement = actualType;

								break;

							case "serial":

								UUID uuid = UUID.randomUUID();
								String result = String.format("%dL", uuid.getLeastSignificantBits() ^ uuid.getMostSignificantBits());
								replacement = result;

								break;

							default:

								if (key.contains("_start"))
								{
									if (!key.equalsIgnoreCase(type + "_start") && (!key.equalsIgnoreCase(actualType + "_start")))
									{
										skip = true;
									}
								}

								if (key.contains("_stop"))
								{
									if (!key.equalsIgnoreCase(type + "_stop") && (!key.equalsIgnoreCase(actualType + "_stop")))
									{
										skip = false;
									}
								}

								break;
						}

						line = line.substring(0, line.indexOf('$')) + replacement + line.substring(line.indexOf('^') + 1);
						if (line.trim().length() == 0)
							oneSkip = true;
					}
					else
					{
						break;
					}
				}

				if (oneSkip)
				{
					oneSkip = false;
					continue;
				}

				if (skip)
				{
					continue;
				}

				buffer.append(line);
				buffer.append('\n');
			}

			pack.createCompilationUnit(projectName + ".java", buffer.toString(), false, null);
		}
		catch (CoreException e)
		{
			_pageOne.setErrorMessage(e.getMessage());
			try
			{
				project.delete(true, null);
			}
			catch (CoreException e1)
			{
			}
			return false;
		}
		catch (Exception e)
		{
			_pageOne.setErrorMessage(e.getMessage());
			try
			{
				project.delete(true, null);
			}
			catch (CoreException e1)
			{
			}
			return false;
		}

		return true;
	}

	@Override
	public void addPages()
	{
		super.addPages();

		_pageOne = new WizardNewProjectCreationPage("C4U Plugin Wizard")
		{
			@Override
			public void createControl(Composite parent)
			{
				super.createControl(parent);
				Composite composite = (Composite) getControl();

				Group group = new Group(composite, SWT.NONE);
				group.setLayout(new GridLayout());
				group.setText("C4U Type");
				group.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

				Label label = new Label(group, SWT.CENTER);
				label.setText("The type of plugin: ");

				Combo type = new Combo(group, SWT.READ_ONLY | SWT.CENTER);
				type.setItems(new String[] { "Connector", "Service", "Vas Service" });
				type.addSelectionListener(me);

				Button hasConfig = new Button(group, SWT.CHECK);
				hasConfig.setText("Has Configuration: ");
				hasConfig.addSelectionListener(me);
			}

		};

		_pageOne.setTitle("C4U Plugin Project");
		_pageOne.setDescription("Creates a C4U plugin.");

		addPage(_pageOne);
	}

	@Override
	public void widgetSelected(SelectionEvent e)
	{
		if (e.getSource() instanceof Combo)
		{
			Combo combo = (Combo) e.getSource();
			type = combo.getItem(combo.getSelectionIndex()).replace(' ', '_');
		}
		else if (e.getSource() instanceof Button)
		{
			Button button = (Button) e.getSource();
			hasConfiguration = button.getSelection();
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e)
	{
	}

}
