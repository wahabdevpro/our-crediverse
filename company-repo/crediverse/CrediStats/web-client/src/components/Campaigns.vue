<template>
    <v-card v-if="errorMessage" title="Error" :text="errorMessage">
    </v-card>
    <v-card v-else    >
      <v-card-title>Campaigns</v-card-title>
      <v-table>
        <thead>
          <tr>
            <th class="text-left">        
              Name
            </th>
            <th class="text-left">        
              Status
            </th>
            <th class="text-left">        
              Start Date
            </th>
            <th class="text-left">        
              End Date
            </th>
            <th class="text-left">        
              Number of Transactions
            </th>
            <th class="text-left">        
              Revenue
            </th>
            <th class="text-left">        
              Enable/Disable
            </th>
            <th class="text-left">        
              Actions 
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(campaign, index) in campaignStore.campaigns"
            :key="campaign._id"
          >
            <td>{{ campaign.campaignName }}</td>
            <td>
              <div :class="{
                'bg-green': campaign.status==='Live', 
                'bg-orange': campaign.status==='Completed', 
                'bg-black text-red': campaign.status==='Disabled', 
                'bg-blue': campaign.status==='Scheduled'}"
              >
                {{campaign.status}}
              </div>
            </td>
            <td> {{ campaign.startDate }}</td>
            <td> {{ campaign.endDate }}</td>
            <td> {{ campaign.campaignTransactions }}</td>
            <td> {{ campaign.campaignRevenue }}</td>
            <td>
              <v-btn 
                v-if="campaign.enabled" 
                @click="toggleCampaign(campaign)" 
                class="text-red" 
                density="compact" 
                icon="mdi-toggle-switch-off-outline"></v-btn>
              <v-btn 
                v-else 
                @click="toggleCampaign(campaign)" 
                class="text-green" 
                density="compact" 
                icon="mdi-toggle-switch-outline"></v-btn>
            </td>
            <td> 
              <v-btn class="text-primary" density="compact" icon="mdi-book-edit-outline" @click="editCampaign(campaign)"></v-btn>
              <v-btn class="text-error " density="compact"  icon="mdi-delete-outline" @click="deleteCampaign(campaign)"></v-btn>
            </td>
          </tr>
        </tbody>
      </v-table>
    </v-card>
    <v-btn class="bg-primary" prepend-icon="mdi-pencil-ruler" variant="elevated" @click="editCampaign(null)">
        New Campaign
    </v-btn>
</template>

<script>

import { ref,onMounted,} from "vue";
import { useRouter } from "vue-router"
import { useCampaignStore } from "../stores/CampaignStore";

export default {
  setup () {
    const displayName = "CampaignsComponent";

    let errorMessage = ref('');
    const router = useRouter();

    const campaignStore =  useCampaignStore();

    function deleteCampaign(campaign) {
      campaignStore.deleteCampaign(campaign._id,campaign._rev);
    };

    function toggleCampaign(campaign) {
      campaignStore.toggleCampaign(campaign._id,campaign._rev);
    };
    function editCampaign(campaign) {
      if (campaign ){
        router.push({ name: "Campaign", params: { "id": campaign._id,"rev": campaign._rev } });
      } else  {
        router.push({ name: "Campaign", params: { "id": "NEW","rev": "NEW"}});
      }
    }

    onMounted(
      async () => {
        await campaignStore.getCampaigns();
      }
    );

    return {
      errorMessage, 
      campaignStore,
      deleteCampaign,
      toggleCampaign,
      editCampaign,
    };

  }
  // data() {
  //   return {
  //     error_message: '',
  //     campaigns: [],
  //   }
  // },
  // async mounted() {
  //   console.log("Mounted");
  //   try {
  //     const campaignsResponse = await fetch(
  //       "http://localhost:8801/campaign_manager/campaigns", 
  //       {
  //         method: "GET",
  //         headers: { "Content-Type": "application/json" },
  //         headers: {
  //           "Content-Type": "application/json",
  //           Accept: "application/json",
  //         }
  //       }
  //     );
  //     this.campaigns = await campaignsResponse.json();

  //   } catch (error) {
  //     this.error_message = JSON.stringify(error);
  //     throw new Error(error);
  //   }
  // },
  // methods: {
  //   async deleteCampaign(index) {
  //     console.log("deleting campaign:", this.campaigns[index],"at index", index);
  //     try {
  //       let deleteCampaignResponse = await fetch(
  //         "http://localhost:8801/campaign_manager/campaigns/"+this.campaigns[index]._id+"/"+ this.campaigns[index]._rev, 
  //         {
  //           method: "DELETE",
  //         });
  //       console.log("deleteCampaignResponse.ok" ,deleteCampaignResponse.ok);
  //       console.log("deleteCampaignResponse" ,deleteCampaignResponse);

  //       this.campaigns.splice(index, 1); 

  //     } catch(deleteError) {
  //       console.log(deleteError);
  //       this.error_message = JSON.stringify(deleteError);
  //       this.campaigns = [];
  //     } 
  //   },
  //   editCampaign(index) {
  //     
  //     console.log("Trying to edit: ", JSON.stringify(this.campaigns[index]));

  //     // this.$router.push('/campaign');
  //     //this.$router.push({ name: 'Campaign', params: { "activeCampaign": this.campaigns[index] } });
  //     this.$router.push({ name: 'Campaign', params: { "id": this.campaigns[index]._id,"rev": this.campaigns[index]._rev } });
  //   },

  //   newCampaign() {
  //     try{
  //     let campaign = {
  //       campaignName: '',
  //       startDate: '',
  //       endDate: '',
  //       enabled: false,
  //     };
  //       this.campaigns.push(campaign);
  //     
  //     //this.$router.push({ name: 'campaign', params: { activeCampaign: this.campaigns[this.campaigns.len-1]} });
  //       this.$router.push({ name: '/campaign', params: { activeCampaign: campaign } });
  //     }catch(error){
  //       console.log(error.message);
  //       this.error_message = error.message;
  //     }
  //   },
  //   toggleCampaign (index) {
  //     this.campaigns[index].enabled = !this.campaigns[index].enabled;
  //   },
  // }
}



</script>

<style scoped>
.cs-primary-button {
background-color: #0088FF;
color: #ffffff; /* This is for white text. Adjust if needed */
border: none;
border-radius: 5px;
padding: 10px 15px;
cursor: pointer;
transition: background-color 0.3s ease;
/* Add any other desired styles like font-size, margins, etc. */
}

.cs-primary-button:hover {
background-color: #0066cc; /* A slightly darker shade for hover effect */
}

.cs-primary-button:active {
background-color: #004999; /* An even darker shade for active (pressed) effect */
}

.cs-primary-button:focus {
outline: none;
box-shadow: 0 0 0 3px rgba(0, 136, 255, 0.5); /* This is for focus outline, adjust if needed */
}

.cs-danger-button {
background-color: #0088FF;
color: #ffffff; /* This is for white text. Adjust if needed */
border: none;
border-radius: 5px;
padding: 10px 15px;
cursor: pointer;
transition: background-color 0.3s ease;

/* Add any other desired styles like font-size, margins, etc. */
}

.cs-danger-button:hover {
background-color: #0066cc; /* A slightly darker shade for hover effect */
}

.cs-danger-button:active {
background-color: #004999; /* An even darker shade for active (pressed) effect */
}

.cs-danger-button:focus {
outline: none;
box-shadow: 0 0 0 3px rgba(0, 136, 255, 0.5); /* This is for focus outline, adjust if needed */
}
</style>
