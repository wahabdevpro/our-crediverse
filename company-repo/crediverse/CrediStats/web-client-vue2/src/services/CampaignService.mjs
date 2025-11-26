import Api from './Api';

const getCampaigns = async () => {
  try {
    const response = await Api().get("/campaign_manager/campaigns");
    const result = response.data;
    return result;
  } catch (error) {
    throw new Error(error);
  }

} 

const addOrUpdateCampaign = async (campaign) => {
  try {
    console.log("addCampaign :",campaign);
    return Api().post("/campaign_manager/campaigns/",campaign); 
  } catch (error) {
    console.error(error);
    throw new Error(error);
  }
}

const deleteCampaign = async (id, rev) => {
  console.log("CampaignService deleting id: " ,id ,"  rev: " ,rev);
  try {
    Api().delete("/campaign_manager/campaigns/"+id+"/"+rev);
  } catch (error) {
    console.error(error);
    throw new Error(error);
  }
} 

export { getCampaigns,deleteCampaign, addOrUpdateCampaign };

