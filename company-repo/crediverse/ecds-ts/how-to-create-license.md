## Crediverse License Generation

Original place of this document was here: https://github.com/Concurrent-Systems/Concurrent-Handbook/blob/create-crediverse-license/70%20Product/73%20Crediverse/Creating%20a%20License.md

You need to be on a machine that runs docker.
You must have access to the registry.gitlab.com repo

1) Login to the gitlab repo with

	docker login -u <gitlab username> -p <gitlab password> registry.gitlab.com

NB. you can also login to the registry using an access key. which if you have one, should come with a username. Use the access key as the password in the login command.

2) Create a local license directory and ensure it is writable by the docker container:

	mkdir license
	chmod 0777 license
	
3) Use that directory in the following command:
    To generate a Java8 license (versions of Crediverse before 1.12.0) use
    
        docker run -it -v `pwd`/license:/license registry.gitlab.com/csys/products/crediverse/crediverse/ecds-ts/crediverse-licensing:java8
    
    To generate a Java11 license (versions of Crediverse from 1.12.0 inclusive) use
    
	    docker run -it -v `pwd`/license:/license registry.gitlab.com/csys/products/crediverse/crediverse/ecds-ts/crediverse-licensing:latest
	
4) You will be prompted by the software to enter a command. Type help to get a list of commands, or type **encrypt** to generate a license.
5) Follow the prompts as below:

	Location of Registration File (/license): Cryptor-> **encrypt**
	Issuer: **Concurrent Systems**
	Max TPS: **350**
	Max Peak TPS: **400**
	Max Nodes: **4**
	Supplier Password: **password**
	Add Facility (Y/N): **n**
	Are you sure about registration (Y/N): **y**
	Completed encryption.
	Cryptor-> **exit**
	
6) The finished license file will be in the license/registrsyion.lic file on your local machine.
