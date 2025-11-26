define(["App", "jquery", "backbone"],
    function (App, $, Backbone) {
        // Creates a new Backbone Model class object
        var GraphModel = Backbone.Model.extend({
        	defaults: {
				active: false,
				action: 'Modify',
				actionClass: "modifyAssignedRoles"
			},
        	
        	events: {
    		},
    		
            initialize:function () {
            	var self = this;
            	this.set('[\
  {\
    "name": "Top Level",\
    "parent": "null",\
    "children": [\
      {\
        "name": "Level 2: A",\
        "parent": "Top Level",\
        "children": [\
          {\
            "name": "Son of A",\
            "parent": "Level 2: A"\
          },\
          {\
            "name": "Daughter of A",\
            "parent": "Level 2: A"\
          }\
        ]\
      },\
      {\
        "name": "Level 2: B",\
        "parent": "Top Level"\
      }\
    ]\
  }\
]');
            }
        });

        return GraphModel;
    }
);