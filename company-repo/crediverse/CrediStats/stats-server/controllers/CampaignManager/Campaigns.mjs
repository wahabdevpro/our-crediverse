import Nano from 'nano'

const getCampaigns = async (req, res) => { 
  let campaignsDb = Nano ('http://admin:password@localhost:5984/campaigns');
  
  let response = await campaignsDb.list({include_docs: true});

  let campaigns = response.rows.map(row=>row.doc);

  return res.json(campaigns);
}

const deleteCampaign = async (req, res ) => {
  console.log("delete request params:", JSON.stringify(req.params));

  let campaignsDb = Nano ('http://admin:password@localhost:5984/campaigns');

  let response = await campaignsDb.destroy(req.params["id"],req.params["rev"]);
  console.log("delete response:", response)

  return res.status(204).end();
}

const updateOrCreateCampaign = async (req,res) => {
  let campaignsDb = Nano ('http://admin:password@localhost:5984/campaigns');

  console.log("Update Campaign request: \n",JSON.stringify(req.body)); 

  let newCampaignIdRev = await campaignsDb.insert(req.body);

  let newCampaign = await campaignsDb.get(newCampaignIdRev.id);
  console.log("newCampaign: ",newCampaign );

  return res.status(200).json(newCampaign);
}

export { getCampaigns, deleteCampaign ,updateOrCreateCampaign };
 
