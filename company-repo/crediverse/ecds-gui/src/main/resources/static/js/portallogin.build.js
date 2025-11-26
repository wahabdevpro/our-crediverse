({
	baseUrl: 'login',
	out: 'ptlogin-@project.version@.js',
	mainConfigFile: 'login/config/portal.js',
	name: "config/portal",
	generateSourceMaps: false,
	preserveLicenseComments: true,
	
	/** Setting to "none" as minification is performed separately */
	optimize: "none"
})
