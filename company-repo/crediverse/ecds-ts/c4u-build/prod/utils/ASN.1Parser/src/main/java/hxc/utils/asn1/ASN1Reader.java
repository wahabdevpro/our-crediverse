package hxc.utils.asn1;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hxc.utils.asn1.generator.ASN1Element;
import hxc.utils.asn1.generator.ASN1Module;
import hxc.utils.asn1.generator.ASN1OptionalContraint;
import hxc.utils.asn1.generator.ASN1Specification;
import hxc.utils.asn1.generator.ASN1Type;

public class ASN1Reader
{
	private MappedByteBuffer buffer;
	private ASN1Specification specification;

	public ASN1Reader(FileInputStream stream) throws IOException
	{
		FileChannel channel = stream.getChannel();
		buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
	}

	public void analyse() throws Exception
	{
		byte data[] = new byte[buffer.capacity()];
		buffer.get(data);
		int repeat = 1;

		String line;
		Matcher matcher;
		ASN1Module currentModule = null;
		List<ASN1Module> modules = new ArrayList<>();
		List<ASN1Element> elements = new ArrayList<>();
		List<String> identifiers = new ArrayList<>();
		boolean start = false;
		specification = null;
		while (repeat > 0)
		{
			repeat = 0;
			BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data)));
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				line = line.replace('\t', ' ');
				if (line.matches(comments))
				{
					// Ignore because of comments
					continue;
				}

				// Check for the start of the specification
				if (line.matches(specificationIdentifier))
				{
					// Initialize if null
					if (specification == null)
						specification = new ASN1Specification();

					continue;
				}

				// Check if specification has started
				if (specification != null)
				{
					// Create matcher for finding modules
					matcher = modulePattern.matcher(line);

					// Check to see whether there is a module found on that line
					if (matcher.find() && currentModule == null)
					{
						// Set start to false, because we are only found the module now
						start = false;

						String moduleName = line.substring(0, matcher.start()).trim();
						// Ensure that module is unique
						for (ASN1Module m : modules)
						{
							if (m.getIdentifier().equals(moduleName))
							{
								moduleName = null;
								break;
							}
						}

						// If null, then module already exists
						if (moduleName == null)
							continue;

						// Retrieve the type of module TODO updated structure
						ASN1Type type = null;
						String asnType = line.substring(matcher.end()).trim();
						try
						{
							// Iterate through all the types
							for (ASN1Type t : ASN1Type.values())
							{
								if (asnType.contains(t.toString().replace('_', ' ')))
								{
									asnType = asnType.replace(t.toString().replace('_', ' '), "").trim();
									type = t;
									break;
								}
							}

							if (type == null)
								throw new Exception();
						}
						catch (Exception exc)
						{
							repeat++;
							continue;
						}

						// Create new module
						currentModule = new ASN1Module();
						currentModule.setIdentifier(moduleName);
						currentModule.setType(type);

						// Initialise the elements that the module is made up of.
						elements = new ArrayList<>();

						if (type == ASN1Type.SEQUENCE_OF || type == ASN1Type.SET_OF || type == ASN1Type.OBJECT_IDENTIFIER)
						{
							if (asnType.length() > 0)
							{
								asnType = asnType.replaceAll("\\(|\\)|\\{|\\}", "").trim();

								ASN1Element element = new ASN1Element();

								ASN1Module module = null;
								for (ASN1Module m : modules)
								{
									if (m == null)
										continue;

									if (m.getIdentifier().equals(asnType))
									{
										module = m;
										break;
									}
								}

								// Check to see whether the module is a type
								for (ASN1Type t : ASN1Type.values())
								{
									if (asnType.contains(t.toString().replace('_', ' ')))
									{
										module = new ASN1Module();
										module.setIdentifier(asnType);
										module.setType(t);
										break;
									}
								}

								for (String identifier : identifiers)
								{
									if (asnType.contains(identifier))
									{
										module = new ASN1Module();
										module.setIdentifier(asnType);
										module.setType(ASN1Type.UNKNOWN);
										break;
									}
								}

								// If null, then the module has not yet been defined
								if (module == null)
								{
									// Skip this module
									currentModule = null;
									start = false;

									// Ensure it will repeat
									repeat++;

									continue;
								}

								element.setName(asnType);
								element.setModule(module);
								element.setMultiple(true);

								elements.add(element);
								currentModule.setElements(elements.toArray(new ASN1Element[0]));

							}
						}

						continue;
					}

					// Check if it is the start of a module
					if (line.equals("{"))
					{
						start = true;

						continue;
					}

					// If busy with a module and has started
					if (currentModule != null && start)
					{
						// Check to see whether it matches an element structure
						if (line.matches(elementEntry))
						{
							String elementName = line.substring(0, line.indexOf(' '));
							String moduleName = line.substring(line.indexOf(' ')).trim();
							if (moduleName.lastIndexOf(',') > 0)
								moduleName = moduleName.substring(0, moduleName.lastIndexOf(',')).trim();
							String constraint = null;
							if (moduleName.indexOf(' ') > 0)
							{
								constraint = moduleName.substring(moduleName.indexOf(' '));
								moduleName = moduleName.substring(0, moduleName.indexOf(' '));
							}

							ASN1Module module = null;
							// Check to see whether the module has been defined
							for (ASN1Module m : modules)
							{
								if (m == null)
									continue;

								if (m.getIdentifier().equals(moduleName))
								{
									module = m;
									break;
								}
							}

							// Check to see whether the module is a type
							for (ASN1Type t : ASN1Type.values())
							{
								if (moduleName.contains(t.toString().replace('_', ' ')))
								{
									module = new ASN1Module();
									module.setIdentifier(moduleName);
									module.setType(t);
									break;
								}
							}

							// If null, then the module has not yet been defined
							if (module == null)
							{
								// Skip this module
								currentModule = null;
								start = false;

								// Ensure it will repeat
								repeat++;

								continue;
							}

							// Create the element
							ASN1Element element = new ASN1Element();
							element.setName(elementName);
							element.setModule(module);

							if (constraint != null)
							{
								if (constraint.contains("OPTIONAL"))
								{
									element.setConstraint(new ASN1OptionalContraint());
								}
							}

							elements.add(element);
						}

						// Check if it is the end of the module
						if (line.equals("}"))
						{
							currentModule.setElements(elements.toArray(new ASN1Element[0]));
							modules.add(currentModule);

							// Tell it that it has reached the end of the module
							start = false;
							currentModule = null;

							continue;
						}
					}

					if (currentModule != null && !start)
					{
						start = false;
						elements = null;
						modules.add(currentModule);
						currentModule = null;
					}
				}

				// Check if the end of the specification has been reached
				if (line.matches(specificationEnd))
				{
					specification = null;
					start = false;
				}

				if (line.length() > 0)
				{
					identifiers.add(line.substring(0, line.indexOf(' ') > 0 ? line.indexOf(' ') : line.length()));
				}
			}
		}
		specification = new ASN1Specification();
		specification.setModules(modules.toArray(new ASN1Module[0]));
	}

	public ASN1Specification dumpSpecification()
	{
		return specification;
	}

	private String comments = "^--.*";
	private String specificationIdentifier = "^DEFINITIONS\\sAUTOMATIC\\sTAGS\\s::=";
	private String specificationEnd = "^END";
	private Pattern modulePattern = Pattern.compile("::=");
	private String elementEntry = ".*[a-z][A-z]+\\s+.+[,]*.*";
}
