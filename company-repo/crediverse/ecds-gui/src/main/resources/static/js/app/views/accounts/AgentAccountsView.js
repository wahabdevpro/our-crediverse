define(['jquery', 'App', 'backbone', 'marionette',
  "views/accounts/AgentAccountsDialogView", "handlebars",
  'views/accounts/AgentAccountsTableView',
  'collections/TierCollection',
  'utils/CommonUtils', 'toastr'],
  function ($, App, BackBone, Marionette, AgentAccountsDialogView, Handlebars, AgentAccountsTableView,
    TierCollection, CommonUtils, Toastr) {

    var i18ntxt = App.i18ntxt.agentAccounts;
    var AgentAccountsView = AgentAccountsTableView.extend({
      template: "AgentAccounts#accountmaster",
      url: 'api/agents',
      error: null,
      tierList: null,
      criteria: {},
      tagName: 'div',
      attributes: {
        id: "accountmaster"
      },
      i18ntxt: null,
      ui: {
        accountSearch: '.accountSearchButton',
        accountSearchExpand: '.accountSearchExpandButton',
        accountSearchCancel: '.accountSearchCancelButton',
        accountSearchReset: '.accountSearchResetButton',
        searchInput: '.advancedSearchInput .searchInput',
        role: '',
        createAgent: '.createAgentButton',
        editAgent: '.editAgentButton',
        suspendAgent: '.suspendAgentButton',
        unsuspendAgent: '.unsuspendAgentButton',
        activateAgent: '.reactivateAgentButton',
        deactivateAgent: '.deactivateAgentButton',
        performTransfer: '.performTransferButton',
        performAdjustment: '.performAdjustmentButton',
        performReplenish: '.performReplenishButton',
        exportAgent: '.exportAgentButton',
        pinReset: '.pinResetButton',
        quickDeactivateAgent: '.quickDeactivateAgent',
      },

      events: {
        "click @ui.accountSearch": 'accountSearch',
        "click @ui.accountSearchExpand": 'displayAdvancedSearch',
        "click @ui.accountSearchCancel": 'accountSearchCancel',
        "click @ui.accountSearchReset": 'accountSearchReset',
        "focus @ui.searchInput": 'displayAdvancedSearch',
        "click @ui.role": 'viewRole',
        "click @ui.createAgent": 'createAgent',
        "click @ui.editAgent": 'editAgent',
        "click @ui.suspendAgent": 'suspendAgent',
        "click @ui.unsuspendAgent": 'activateAgent',
        "click @ui.activateAgent": 'activateAgent',
        "click @ui.deactivateAgent": 'deactivateAgent',
        "click @ui.performTransfer": 'performTransfer',
        "click @ui.performAdjustment": 'performAdjustment',
        "click @ui.performReplenish": 'performReplenish',
        "click @ui.exportAgent": 'exportAgent',
        "click @ui.pinReset": 'agentPinReset',
        "click @ui.quickDeactivateAgent": 'quickDeactivateAgentDialog'
      },

      quickDeactivateAgentDialog: function (ev) {
        var self = this;
        var result = false;
        App.vent.trigger('application:dialog', {
          name: "quickDeactivateDialog",
          title: "Deactivate Agent",
          init: function () {
            $(".failQuickDeactivation").css("display", "none");
          },
          hide: function () {
            console.log('Called Hide');
            self.dataTable.ajax.reload().draw();
          },
          events: {
            "click .quickDeactivateAgentBtn": function (event) {
              var msisdn = $("#quickDeactivateMsisdnTxt").val();
              console.log('deactivateAgentByMsisdn ' + msisdn);
              var dialog = $(".failQuickDeactivation");
              $.ajax({
                url: 'api/agents/deactivate/by-msisdn/' + msisdn,
                type: 'PUT',
                success: function (response) {
                  var successMessage = ""
                  dialog.css("display", "none");
                  $('.modal').delay(100).fadeOut(450);
                  setTimeout(function () { $('.modal').modal('hide') }, 500);
                  console.log("response " + response);
                  Toastr.success("Successfully Deactivated the Agent with MSISDN "+msisdn);
                },
                error: function (error) {
                  console.log('Error status ' + error["status"]);
                  console.log('Error statusText ' + error["statusText"]);
                  console.log('Error status ' + error["responseJSON"]["status"]);
                  console.log('Error responseJSON ' + error["responseJSON"]["message"]);
                  if (!msisdn) {
                    $(".failQuickDeactivation .error-code").text('Please provide an MSISDN');
                  } else if (error.status == 404) {
                    $(".failQuickDeactivation .error-code").text('There is no agent with MSISDN ' + msisdn);
                  }
                  else if (error.status == 412) {
                    $(".failQuickDeactivation .error-code").text('MSISDN ' + msisdn + ' has already been deactivated');
                  }
                  else {
                    $(".failQuickDeactivation .error-code").text('An internal server error occured, the error code was ' + error.status);
                  }

                  dialog.css("display", "block");
                }
              });
            }
          }
        });

        return false;
      },

      displayAdvancedSearch: function (ev) {
        $('.advancedSearchInput').hide();
        $('.advancedSearchForm').slideDown({
          duration: 'slow',
          easing: 'linear',
          start: function () {
            $('.advancedSearchForm #firstName').focus();
          },
          complete: function () {

          }
        });
        App.appRouter.navigate(window.location.hash.replace('/asf!off', '/asf!on'), { trigger: false, replace: true });
      },

      hideAdvancedSearch: function (ev) {
        $('.advancedSearchInput').show().focus();
        $('.advancedSearchForm').slideUp({
          duration: 'slow',
          easing: 'linear',
          start: function () {
          },
          complete: function () {
          }
        });
        App.appRouter.navigate(window.location.hash.replace('/asf!on', '/asf!off'), { trigger: false, replace: true });
      },

      breadcrumb: function () {
        var txt = App.i18ntxt.agentAccounts;
        return {
          heading: txt.agentAccounts,
          defaultHome: false,
          breadcrumb: [{
            text: txt.agentAccounts,
            href: "#accountList",
            iclass: "fa fa-users"
          }]
        }
      },

      initialize: function (options) {
        var self = this;
        if (!_.isUndefined(options)) this.i18ntxt = options;

        this.url = this.url + "?state=" + encodeURIComponent('~D');

        self.model = new Backbone.Model();
        var tiers = new TierCollection();
        tiers.fetch({
          success: function (ev) {
            function filterTiers(val) {
              return (val.type != "S");
            }
            var tierData = tiers.toJSON();
            self.model.set('tierList', tierData.filter(filterTiers));
            //self.render();	// this causes double-loading
          }
        });

      },

      getAjaxConfig: function (type) {
        var config = {
          type: "GET",
          url: "api/" + type + "/dropdown",
          dataType: 'json',
          //contentType: "application/json",
          delay: 250,
          //data: selectedItem,
          processResults: function (data) {
            return {
              results: $.map(data, function (item, i) {
                return {
                  text: item,
                  id: i
                }
              })
            };
          }
        };
        return config;
      },

      configureSearchForm: function () {
        var that = this;
        var groupID = that.$('#groupID');
        var tierID = that.$('#tierID');
        var baseUrl = '#accountList';

        $('input[data-toggle="tooltip"]').tooltip({
          placement: "top",
          trigger: "manual",
          container: '.advancedSearchForm',
          template: '<div class="tooltip" role="tooltip"><div class="tooltip-inner"></div></div>',
        });
        $('input[data-toggle="tooltip"]').on('hover', function () {
          $(that).tooltip('hide')
        });
        $('input[data-toggle="tooltip"]').on('focus', function () {
          $(that).tooltip('show')
        });
        $('input[data-toggle="tooltip"]').on('blur', function () {
          $(that).tooltip('hide')
        });

        var ajaxConfig = that.getAjaxConfig('groups');
        groupID.select2({
          ajax: ajaxConfig,
          minimumInputLength: 0,
          allowClear: true,
          placeholder: i18ntxt.selectGroupHint,
        });
        groupID.data('config', ajaxConfig);

        var ajaxConfig = that.getAjaxConfig('tiers');
        tierID.select2({
          ajax: ajaxConfig,
          minimumInputLength: 0,
          allowClear: true,
          placeholder: i18ntxt.selectGroupHint,
        });
        tierID.data('config', ajaxConfig);

        var serviceClassID = that.$('#serviceClassID');

        ajaxConfig = that.getAjaxConfig('serviceclass');

        serviceClassID.select2({
          ajax: ajaxConfig,
          minimumInputLength: 0,
          allowClear: true,
          placeholder: i18ntxt.selectServiceClassHint,
        });
        serviceClassID.data('config', ajaxConfig);

        var pcs = window.location.hash.indexOf(baseUrl) == 0 ? window.location.hash.substr(baseUrl.length).split('/') : [];
        var form = $('form');

        setTimeout(function () {
          if (CommonUtils.urlDecodeForm(pcs, form)) {
            that.displayAdvancedSearch();
            that.accountSearch({ encode: false });
          }
        }, 100);


        that.getSelect2Data("ownerAgentID", "agents", "api/agents/dropdown", 2, i18ntxt.searchOwnerAgentHint);
        //Disabled and hidden to keep search form evenly balanced.
        //that.getSelect2Data("supplierAgentID", "api/agents/dropdown", 2, i18ntxt.searchSupplierAgentHint);
        // that.getSelect2Data("areaID", "areas", "api/areas/dropdown", 0, i18ntxt.searchAreaHint);

        var areaElement = this.$('#areaID');
  		  		CommonUtils.configureSelect2Control({
  		  			jqElement: areaElement,
  		  			url: "api/areas/dropdown",
  		  			placeholderText: i18ntxt.searchAreaHint,
  		  			minLength: 0,
					    isHtml : true
  		  		});
      },

      getSelect2Data: function (elementID, entityType, ajaxurl, minLength, placeholderText) {
        var that = this;
        var jqElement = that.$('#' + elementID);
        var ajaxConfig = that.getAjaxConfig(entityType);
        jqElement.select2({
          ajax: ajaxConfig,
          minimumInputLength: minLength,
          allowClear: true,
          placeholder: placeholderText,
        });
        jqElement.data('config', ajaxConfig);
        return jqElement;
      },

      onRender: function () {
        var self = this;
        this.configureSearchForm();
        try {
          self.renderTable({
            searchBox: false
          });
        } catch (err) {
          if (console) console.error(err);
        }
      },

      getFormData: function () {
        var self = this;
        var criteria = Backbone.Syphon.serialize($('form'));
        var args = "";
        for (var key in criteria) {
          if (criteria[key] != "") {
            if (args != "") args += "&";
            args += key + "=" + encodeURIComponent(criteria[key]);
          }
        }
        self.criteria = criteria;
        return args;
      },

      exportAgent: function (ev) {
        var self = this;
        var table = this.$('.accountstable');
        var pos = self.url.indexOf('?')
        var baseUrl = (pos >= 0) ? self.url.substr(0, pos) : self.url;
        CommonUtils.exportAsCsv(ev, baseUrl + '/search', self.currentFilter.data, self.criteria);
      },

      accountSearchCancel: function (ev) {
        var self = this;
        this.hideAdvancedSearch();
        return false;
      },

      accountSearchReset: function (ev) {
        var self = this;
        self.enableResetButton(false);
        var form = self.$('form')[0];
        form.reset();
        //Uncomment and replace to add supplierAgentID back:
        //self.$("#groupID,#serviceClassID,#supplierAgentID,#ownerAgentID,#areaID").val(null).trigger("change");
        self.$("#groupID,#serviceClassID,#ownerAgentID,#areaID,#tierID").val(null).trigger("change");
        var ajax = self.dataTable.ajax;
        var url = 'api/agents';
        ajax.url(url).load(function () { self.enableResetButton(true) }, true);
      },

      accountSearch: function (ev) {
        var self = this;
        //if (_.isUndefined(ev.encode)) App.appRouter.navigate('#accountSearch' + CommonUtils.urlEncodeForm($('form')), {trigger: false, replace: true});
        self.enableSearchButton(false);
        var ajax = this.dataTable.ajax;
        var url = 'api/agents?' + self.getFormData();
        ajax.url(url).load(function () {
          self.enableSearchButton(true);
        }, true);
        //$('.advancedSearchResults .dataTables_filter label').show();
        //self.renderTable(self.getFormData());
        return false;
      },

      enableSearchButton: function (isEnabled) {
        if (isEnabled)
          self.$('.accountSearchButton').prop('disabled', !isEnabled).find('i').removeClass('fa-spinner fa-spin').addClass('fa-search');
        else
          self.$('.accountSearchButton').prop('disabled', !isEnabled).find('i').removeClass('fa-search').addClass('fa-spinner fa-spin');
      },

      enableResetButton: function (isEnabled) {
        self.$('.accountSearchResetButton').prop('disabled', !isEnabled);
      },
    });
    return AgentAccountsView;
  });
