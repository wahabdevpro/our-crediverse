// https://github.com/requirejs/r.js/blob/master/build/example.build.js

// NOTE: If this does not work the following work was done:
// 1. 'mainConfigFile' added (remove)
// 2. uncomment from "exclude: ["text"]," onwards 
// 3. init.js was split into statis and dynamic sections

// E:\ecds\ecds-gui>mvn requirejs:optimize
({
	baseUrl: 'app',
	out: 'main-@project.version@.js',
	mainConfigFile: 'app/config/init.js',
	name: "config/init",
	generateSourceMaps: false,
	preserveLicenseComments: true,

	/** Setting to "none" as minification is performed separately */
	optimize: "none",
    compile: {
        options: {
            paths: {
                'jquery':'empty:',
                'bootstrap':'empty:',
                'datatables.net':'empty:',
                'datatables':'empty:',
                'datatables.net-bs':'empty:',
                'datatables.net-responsive':'empty:'
            }
        }
    }
	
})
