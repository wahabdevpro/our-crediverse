<template>
  <div class="flex-container">
    <b-alert v-if="errorMessage" variant="danger" show dismissible @dismissed="errorMessage = null">
      {{ errorMessage }}
    </b-alert>
    <div class="border border-info border-primary card card-body card-shadow-info flex-column mb-2 p-2">
      <i class="header-icon lnr-charts icon-gradient bg-happy-green"> </i>
      <h4 style="font-weight: bold">Campaign Wizzard</h4>
      <div class="mb-3 card"> 
        <b-tabs pills card v-model="activeTab">
          <b-tab title="Campaign Info" >
            <div class="border border-info border-primary card card-body card-shadow-info flex-column mb-2 p-2">
              <form class="content">
                <div class="position-relative form-group">
                  <label for="campaignName"
                    class="">
                    Campaign Name 
                  </label>
                  <input name="Campaign Name"
                    v-model="campaign.campaignName"
                    id="campaignName"
                    placeholder="with a placeholder"
                    class="form-control"/>
                </div>
                <div class="position-relative form-group">
                  <label for="campaignStartDate"
                    class="">
                    Campaign Start Date
                  </label>
                  <p><date-picker v-model="campaign.startDate" valueType="format"></date-picker></p>
                </div>
                <div class="position-relative form-group">
                  <label for="campaignEndDate"
                    class="">
                    Campaign End Date
                  </label>
                  <p><date-picker v-model="campaign.endDate" valueType="format"></date-picker></p>
                </div>
                <div class="position-relative form-group">
                  <label for="enabled" class="">
                    Enabled 
                  </label>
                    <input class="form-check-input" type="checkbox" id="flexSwitchCheckDefault" v-model="campaign.enabled"> 
                    </input>
                </div>
              </form> 
            </div>
            <div class="text-right">
              <b-button class="mr-2 mb-2"  variant='primary' @click="saveCampaignInfo"  >
                <div>
                  Add Campaign Conditions 
                  <i class="pe-7s-right-arrow fa-2x"></i>
                </div>
              </b-button>
            </div>
          </b-tab>
          <b-tab title="Campaign Eligibility Conditions">
            <div class="mb-3 card"> 
              <condition @condition_changed="conditionChanged" :activeCondition = "campaign.condition">
              </condition>
            </div>
            <b-button class="mr-2 mb-2"  variant='primary' @click="saveCampaignInfo"  >
              <div>
                Add Incentives 
                <i class="pe-7s-right-arrow fa-2x"></i>
              </div>
            </b-button>
          </b-tab>
          <b-tab title="Incentives">
            <incentives :incentivesChanged = "incentivesChanged"  :activeIncentives = "{incentives: campaign.incentives}"></incentives>
          </b-tab>

          <b-tab title="Communication">
            <communications @communicationsChanged = "communicationsChanged"  :activeCommunications = "{communications: campaign.communications}"></communications >
          </b-tab>
        </b-tabs>
      </div>
    </div>
  </div>
</template>


<script>
import DatePicker from 'vue2-datepicker';
import 'vue2-datepicker/index.css';
import { addOrUpdateCampaign } from '../../../services/CampaignService';
import Condition from '../Condition/Condition';
import Incentives from '../Incentives/Incentives';
import Communications from '../Communications/Communications';

export default {
  components: { 
    DatePicker,
    Condition,
    Incentives,
    Communications,
  },
  props:  {
    activeCampaign: Object,
  },
  data() {
    return {
      campaign: this.activeCampaign,
      activeTab: 0,
      errorMessage: null,
    };
  },
  methods: {
    async conditionChanged(condition) {
      this.campaign.condition = condition;
    },
    async saveCampaignInfo() {
      try{
        console.log("Saving Campaign")
        let save_result=await addOrUpdateCampaign(this.campaign);

        this.campaign =  save_result.data;

        console.log("saved campaign: ",save_result);

        this.activeTab = 1;
        this.errorMessage=null;
        console.log("Done Saving Campaign")
      } catch (error){
        console.log("Handling Error",error)
        this.errorMessage = JSON.stringify(error) ;
        console.error(error);
      }
    },
    async incentivesChanged (incentives) {
      this.incentives = incentives;
      await this.saveCapaignInfo();
    },
    async communicationsChanged (communications) {
      this.communications= communications;
      await this.saveCapaignInfo();
    },
  },
};
</script> 

