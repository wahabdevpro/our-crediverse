({
	baseUrl: 'login',
	out: 'adlogin-@project.version@.js',
	mainConfigFile: 'login/config/admin.js',
	name: "config/admin",
	generateSourceMaps: false,
	preserveLicenseComments: true,

	/** Setting to "none" as minification is performed separately */
	optimize: "none"
})
