## Introduction
NB. less has nothing to do with the `/usr/bin/less` command typically installed on Linux systems.  It's a preprocessor for css and run with the lessc command (after installation)
See http://www.greenwoodsoftware.com/less/ for the latest info on less. When porting top springboot 2.5, the following version was used to process the .less files

```
less --version
less 551 (GNU regular expressions)
Copyright (C) 1984-2019  Mark Nudelman
```
The original Gradle config used to build the less was
```
css.source {
	dev {
		css {
			srcDir "src/main/resources/static/less"
			include "*.less"
			outputDir = file("${sourceSets.main.output.resourcesDir}/static/css/skins")
		}
	}
}
lesscss {
	source = css.source.dev.css.asFileTree
	dest = "${sourceSets.main.output.resourcesDir}/static/css/skins"
	doLast {
	}
}
classes.dependsOn lesscss
```
using the `id "com.eriwen.gradle.js" version "2.14.1"` gradle plugin, but as this is now obsolete, the decision was taken to generate the CSS from the less manually and check it into git.

manual generation (from the GI resources/static/less/lib directory):
```
npm install -g less
lessc skin-credit.less > ../css/skins/skin-credit.css
lessc skin-credit.less > ../css/skins/skin-moov.css
```

