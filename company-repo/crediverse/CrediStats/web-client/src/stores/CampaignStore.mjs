

import {defineStore} from "pinia";

export const useCampaignStore = defineStore(
  "CampaignStore",
  { 
    actions: {
      async getCampaigns() {
        const campaignsResponse = await fetch(
          "http://localhost:8801/campaign_manager/campaigns", 
          {
            method: "GET",
            headers: {
              "Content-Type": "application/json",
              Accept: "application/json",
            }
          }
        );
        this.campaigns = await campaignsResponse.json();
      },

      async deleteCampaign(id,rev) {
        try {
          let deleteCampaignResponse = await fetch(
            "http://localhost:8801/campaign_manager/campaigns/"+id+"/"+rev, 
            {
              method: "DELETE",
            });

          this.campaigns= this.campaigns.filter(campaign => {
            return !(campaign._id === id && campaign._rev === rev);
          });
        } catch(deleteError) {
          console.log(deleteError);
          this.campaigns = [];
          throw deleteError;
        } 
      },
      toggleCampaign(id,rev) {
        const campaign = this.campaigns.find(campaign => campaign._id === id && campaign._rev === rev )
        campaign.enabled = !campaign.enabled
      },

      async getCampaign(id,rev){
        if (!this.campaigns) {
          await this.getCampaigns();
        }
        return this.campaigns.find(campaign => campaign._id === id && campaign._rev === rev )
      },

      async addOrUpdateCampaign(campaign) {
        if (campaign._id === "NEW") {
          return campaign;
        }
        try {
          const body = JSON.stringify(campaign);

          let response = await fetch(
            "http://localhost:8801/campaign_manager/campaigns/", 
            {
              method: "POST",
              headers: {
                "Content-Type": "application/json",
              },
              body,
            });

          let response_json = await response.json();


          await this.getCampaigns();

          let newCampaign = await this.getCampaign(response_json._id,response_json._rev);


          return newCampaign ;

        } catch (error) {
          console.error(error);
          throw new Error(error);
        }
      },
    },

    state: () => ({ 
      campaigns:[ ],
    }),
  });


