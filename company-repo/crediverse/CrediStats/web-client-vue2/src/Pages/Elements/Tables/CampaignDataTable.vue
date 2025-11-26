<template>

  <div>
    <div>
      <b-table :items="campaigns" :fields="fields">
        <template  v-slot:cell(enabled)="data" >
          <div class="form-check form-switch">
            <input class="form-check-input" type="checkbox" id="flexSwitchCheckDefault" v-model="data.item.enabled"  >
          </div>
        </template>
        <template v-slot:cell(status)="data">
          <div        
            :class="{
              'mb-2 mr-2 badge bg-light': data.item.status==='Disabled',
              'mb-2 mr-2 badge bg-success': data.item.status==='Live',
              'mb-2 mr-2 badge bg-dark text-white': data.item.status==='Completed',
              'mb-2 mr-2 badge bg-info': data.item.status==='Scheduled'}">
            {{data.item.status}}
          </div>
        </template>
        <template v-slot:cell(actions)="data">
         <b-button class="mr-2 mb-2" variant='info'>
            <i class="pe-7s-config" @click="editCampaign(data.item)"> </i>
          </b-button>
          <b-button class="mr-2 mb-2" variant='info' @click="deleteCampaign(data.item)" >
            <i class="pe-7s-trash"> </i>
          </b-button>
          <b-button class="mr-2 mb-2" variant='info'>
            <i class="pe-7s-graph2"> </i>
          </b-button>
        </template>
      </b-table>
    </div>
  </div>
</template>

<script> 
import { getCampaigns,deleteCampaign,addOrUpdateCampaign } from '../../../services/CampaignService.mjs' 

export default {
  created () {
    this.fetchCampaigns();
  },
  methods: {
    async fetchCampaigns() {
      try {
        let campaigns = await getCampaigns();   
        console.log("got campaigns:",campaigns);
        this.campaigns = campaigns;
      } catch (error) {
        console.log(error);
        this.campaigns = [];
      }
    }, 
    async deleteCampaign(campaign) {
      console.log("deleting campaign:", campaign)

      try{
        await deleteCampaign(campaign._id, campaign._rev);
        let index = this.campaigns.indexOf(campaign);
        this.campaigns.splice(index, 1); 
      } catch (error) {
        console.log(error);
        this.campaigns = [];
      }
    },
    editCampaign(campaign) {
      // this.$router.push('/campaign');
      this.$router.push({ name: 'campaign', params: { activeCampaign: campaign } });

    },
    async campaignChanged(campaign) {
      this.campaign = await addOrUpdateCampaign(campaign);
    },
  },
  data() {
    return { 
      fields: [
        "campaignName",
        "status",
        "startDate",
        "endDate",
        "campaignTransactions",
        "campaignRevenue",
        "enabled",
        "actions"
      ],
      campaigns: this.campaigns
    };
  },
}

</script>
