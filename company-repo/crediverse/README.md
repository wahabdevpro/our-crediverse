## Background info
The structure of this project is based on the [gradle recommendations here](https://docs.gradle.org/current/userguide/structuring_software_products.html#structure_large_projects).
## Crediverse
For the moment, when building locally, Crediverse is still built as 3 separate projects.  For each of those projects, follow the instructions in the project specific README.md.

## Releasing
Assuming your release version is _1.14.0_, apply the following instructions:

* Create a branch named the same as the release version _without the minor version_ (1.14) and `push`.
* Then create a release using GitHub [Draft a new release](https://github.com/Concurrent-Systems/Crediverse/releases/new).
 * Here you choose target:1.14 (where the target name is the name of the branch you created).
 * Create a tag by choosing the _choose a tag_ dropdown. Name the tag according to the release number you want, as per our example _1.14.0_.
 * It is best to use the "generate release notes" button, and modify them to match the associated release (though at the time of writing this, stakeholders were still deciding if this was the desired course for release notes).
 * Once ready, choose the "Publish release" button.


The docker Image registry associated with releases can be found here for each project (search for the associated tag, _1.14.0_ in our example):
[ecds-ts](https://github.com/Concurrent-Systems/Crediverse/pkgs/container/crediverse%2Fecds-ts/versions?filters%5Bversion_type%5D=tagged)
[ecds-gui](https://github.com/Concurrent-Systems/Crediverse/pkgs/container/crediverse%2Fecds-gui/versions?filters%5Bversion_type%5D=tagged)
[ecds-api](https://github.com/Concurrent-Systems/Crediverse/pkgs/container/crediverse%2Fecds-api/versions?filters%5Bversion_type%5D=tagged)

NOTE: When creating new releases, you should be sure to update the [Crediverse Workflow](.github/workflows/build-crediverse.yml).
You should add the following (where _1.14_ is the BRANCH NAME associated with our release version of _1.14.0_)
```
on:
  # Triggers the workflow on push or pull request events for main or release branches
  push:
    branches:
      - main
      - 1.14
```
It's important to note, the ONLY additional line to add is `- 1.14` (with the version number of your release) with appropriate prefix spacing, the rest is just contextual information

## Running Crediverse Docker Images
In order to reproduce some problems, it may be nessasary to run a specific docker image. The [Running Credivers Under Docker](docs/RunningCrediverseUnderDocker.md) document contains details of how to setup Crediverse running under docker on your local machine.

## Additional Developer Docs
So as not to clutter up this file too much, additional developer docs are listed in the [docs index](docks/../docs/INDEX.md)

## Github Migration

Migration Date: 2022-07-25

Going forward, the Gitlab repository will be made READ ONLY. Therefore you can pull, but you cannot push or write any changes to gitlab.

In order to migrate your local repository from Gitlab to Github, run this command:  
```git remote set-url origin git@github.com:Concurrent-Systems/Crediverse.git```

In some cases, you may desire to instead re-clone, follow the default Github procedure to clone this repository  

To provide a BUILD to QA or someone desiring to build Crediverse for testing, see [https://github.com/orgs/Concurrent-Systems/packages?repo_name=Crediverse]

> **NOTE**  
> 
> For branch `pull` requests, when using git (with GitHub as the remote) on the command line, occassionally you will not receive a URL after you perform a 'push'. To create a pull request manually for your branch, visit [https://github.com/Concurrent-Systems/Crediverse/pulls] and choose **New pull request**, you can then *choose your branch* from the list.
