<template>
  <div> 

    <div>
      <b-button class="mr-2 mb-2" variant='primary' @click="navigateToCampaign"  >
        New Campaign
      </b-button>
    </div>

    <div class="mb-3 card" v-if="is_error">
      <div class="card-header-tab card-header">
        <div class="card-header-title font-size-lg text-capitalize font-weight-normal">
          <i class="header-icon lnr-charts icon-gradient bg-happy-green"> </i>
          {{ error_message }}
        </div>
      </div>
    </div>

    <div class="mb-3 card" v-else>
      <div class="card-header-tab card-header" style="padding-bottom:0;">
        <div class="card-header-title font-size-lg text-capitalize font-weight-normal">
          <i class="header-icon lnr-charts icon-gradient bg-happy-green"> </i>
          <h4 style="font-weight: bold"> Active Campaigns  </h4>
        </div>
      </div> 
      <campaign-data-table></campaign-data-table>
    </div>
  </div>

</template>

<script>
import CampaignDataTable from '../../../Pages/Elements/Tables/CampaignDataTable.vue'

export default {

  components: {
    CampaignDataTable,
  },
  data() {
    return {
      is_error: false,
      error_message: ''
    }
  },
  methods: {
    navigateToCampaign() {
      this.$router.push('/campaign');
    }
  }
}

</script>

