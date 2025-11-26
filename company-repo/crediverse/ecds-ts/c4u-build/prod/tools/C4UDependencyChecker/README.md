## What does this project do

The goal of this small project is to find dependencies between artifacts, especially connector and service dependencies,
as these highlight some architectural rearrangements which might need to be made.

## To run application from jar

mvn clean install && cd target
java -DPLANTUML_LIMIT_SIZE=81920 -Xmx1024m -jar C4UDependencyChecker-1.0-SNAPSHOT.jar

OR (just using Maven)

mvn clean install exec:java

Note: Images and uml files will be created in diagrams folder

## To build Plant UML from command line

### To Install PlantUML

sudo apt-get install graphviz
sudo apt install plantuml

### To create diagram (from uml file)

plantuml -tpng fileName.uml
