<template>
  <v-card class="pa-4 ma-4" v-if="errorMessage" title="Error" :text="errorMessage"></v-card>
  <v-card class="pa-4 ma-4" v-else  prepend-icon="mdi-cog-clockwise" title="Campaign Configuration">
    <v-tabs bg-color="primary" v-model="activeTab">
      <v-tab value="campaignInfo">Information</v-tab>
      <v-tab value="campaignConditions">Eligibility Conditions</v-tab>
      <v-tab value="campaignIncentives">Incentives</v-tab>
      <v-tab value="campaignCommunications">Communications</v-tab>
    </v-tabs>

    <v-card-text variant="tonal">
      <v-window v-model="activeTab">
        <v-window-item value="campaignInfo">
          <v-container>
            <v-row>
              <v-col>
                <h3>Campaign Info</h3> 
              </v-col>
            </v-row>
            <v-row>
              <v-col>
                <v-card class="pa-4 ma-4" variant="outlined">
                  <v-form>
                    <v-text-field
                      v-model="campaign.campaignName"
                      :counter="10"
                      label="Campaign name"
                      required
                      hide-details

                    ></v-text-field>

                    <!-- div>
<DatePicker 
v-model="campaign.startDate" 
@update:modelValue="updateStartDate(campaign.startDate)" />
<p>Selected Date: {{ campaign.startDate }}</p>
</div -->

                    <!--v-menu :close-on-content-click="false" v-model="fromDateMenu" min-width="auto">
<template v-slot:activator="{ props }">
<v-text-field
label="Start Date"
:value="formattedDate"
v-bind="props"
prepend-inner-icon="mdi-calendar-blank-outline"
> </v-text-field>
</template>

<v-date-picker
v-model="campaign.startDate"
@input="fromDateMenu = false" 
> </v-date-picker>
</v-menu-->


                    <v-text-field
                      v-model="campaign.startDate"
                      prepend-inner-icon="mdi-calendar"
                      label="Start Date"
                      readonly
                    ></v-text-field>

                    <v-text-field
                      v-model="campaign.endDate"
                      prepend-inner-icon="mdi-calendar"
                      label="End Date"
                      readonly
                    ></v-text-field>
                      <v-checkbox label="enabled" prepend-icon="mdi-list-status"  v-model="campaign.enabled" ></v-checkbox>

                  </v-form> 
                </v-card>
              </v-col>
            </v-row>
            <v-row>
              <v-col>
                <v-btn class="bg-primary" prepend-icon="mdi-ab-testing" variant="elevated" @click="onAddConditions">
                  Add Conditions 
                </v-btn>
              </v-col>
            </v-row>
          </v-container>
        </v-window-item>
        <v-window-item value="campaignConditions">
          <v-card class="pa-4 ma-4" > 
            <condition 
              v-if="campaign.condition" 
              parentType="Campaign" 
              @conditionChanged="onCampaignConditionChanged" 
              :activeCondition="campaign.condition">
            </condition>
            <condition 
              v-else
              parentType="Campaign" 
              @conditionChanged="onCampaignConditionChanged" 
              :activeCondition = "null">
            </condition>
          </v-card>
        </v-window-item>
        <v-window-item value="campaignIncentives">
          <incentives 
            @incentivesChanged="onIncentivesChanged" 
            :activeIncentives="campaign.incentives"
          >
          </incentives>
        </v-window-item>
        <v-window-item value="campaignCommunications">
          <communications 
            @communicationsChanged="onCommunicationsChanged"
            :activeCommunications="campaign.communications"
          >
          </communications>
        </v-window-item>
      </v-window>
    </v-card-text>
    <!--

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
</b-tabs-->
  </v-card>
</template>


<script>
//import { addOrUpdateCampaign } from '../../../services/CampaignService';
//import Condition from '../Condition/Condition';
//import Incentives from '../Incentives/Incentives';
//import Communications from '../Communications/Communications';

import { ref,onMounted,onBeforeUpdate,computed } from "vue";
import { useCampaignStore } from "../stores/CampaignStore";
import { VDatePicker } from "vuetify/labs/VDatePicker";
import {storeToRefs} from "pinia";
import DatePicker from '@/components/DatePicker.vue'
import Condition from '@/components/Condition.vue'
import Incentives from '@/components/Incentives.vue'
import Communications from '@/components/Communications.vue'

export default { 
  props: {id: String,rev:String},
  components: { DatePicker, Condition, Incentives, Communications },

  setup(props) {
    const campaignStore =  useCampaignStore();

    let campaign = ref({}) ;

    let errorMessage = ref("");
    let activeTab = ref("");
    let formattedStartDate= ref("");

    let isStartDateFocused = ref(false);

    const minDate  = ref("2021-01-05");
    const maxDate = ref("2029-08-30");

    // 
    let fromDateMenu = ref(false);


    // const fromDateDisp = computed(()  => fromDateVal.value);

    const formattedDate = computed(() => {
      if(campaign.value.startDate) {


        let unixTime = Date.parse(campaign.value.startDate);

        let fullDate = new Date(unixTime);


        return fullDate.toISOString().split('T')[0];
      }
      return null;
    });

    function updateStartDate(startDate){
      campaign.value.startDate = startDate; 
    }


    onMounted(async ()=>{
      console.log("Campaign Component Mounted");

      if (props.id !== "NEW") {

        campaign.value = await campaignStore.getCampaign(props.id,props.rev);
        console.log("Editing a campaign");
      } else {
        console.log("creating a campaign");
        campaign.value = {
          campaignName:          null,
          status:                  null,
          startDate:             null,
          endDate:                null,
          campaignTransactions:  0,
          campaignRevenue:       0,
          enabled:                false,
        };
      }
    });

    const onAddConditions= () => {
      activeTab.value = 1;
    }

    const onCampaignConditionChanged = async (newCondition) => {
      console.log({handleCampaignConditionChanged: {newCondition}})
      campaign.value.condition = newCondition; 
      if (campaign.value.id != "NEW"){
        await saveCampaignInfo();
      } else {
        console.log("NOT SAVING NEW CAMPAIGN YET!");
      }
    };

    const onIncentivesChanged = async (newIncentives) => {
      console.log({handleCampaignIncentivesChanged: {newIncentives}})
      campaign.value.incentives = newIncentives;
      if (campaign.value.id != "NEW"){
        await saveCampaignInfo();
        console.log("newIncentives saved: ",newIncentives );
      } else {
        console.log("NOT SAVING NEW CAMPAIGN YET!");
      }
    };

    const onCommunicationsChanged = async (newCommunications) => {
      console.log({handleCommunicationsChanged : {newCommunications}})

      campaign.value.communications = newCommunications;
      if (campaign.value.id != "NEW"){
        await saveCampaignInfo();
        console.log("newCommunications saved: ",newCommunications);
      } else {
        console.log("NOT SAVING NEW CAMPAIGN YET!");
      }
    };

    async function saveCampaignInfo() {
      console.log("Saving Campaign");
      campaign.value = await campaignStore.addOrUpdateCampaign(campaign.value);
    };

    return {
      activeTab,
      campaign,
      errorMessage,
      onAddConditions,
      onCampaignConditionChanged,
      onCommunicationsChanged ,
      onIncentivesChanged,
      saveCampaignInfo,
    }
  }
};
</script> 

// methods: {
//   
//   async conditionChanged(condition) {
//     this.campaign.condition = condition;
//   },

//   async saveCampaignInfo() {
//     try{
//       console.log("Saving Campaign")
//       let save_result=await addOrUpdateCampaign(this.campaign);

//       this.campaign =  save_result.data;

//       console.log("saved campaign: ",save_result);

//       this.activeTab = 1;
//       this.errorMessage=null;
//       console.log("Done Saving Campaign")
//     } catch (error){
//       console.log("Handling Error",error)
//       this.errorMessage = JSON.stringify(error) ;
//       console.error(error);
//     }
//   },
//   async incentivesChanged (incentives) {
//     this.incentives = incentives;
//     await this.saveCapaignInfo();
//   },
//   async communicationsChanged (communications) {
//     this.communications= communications;
//     await this.saveCapaignInfo();
//   },
// },







