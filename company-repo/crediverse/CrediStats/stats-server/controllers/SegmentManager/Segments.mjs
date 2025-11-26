import Nano from 'nano'

const getSegments = async (req, res) => { 
  let segmentsDb = Nano ('http://admin:password@localhost:5984/segments');
  
  let response = await segmentsDb.list({include_docs: true});

  let segments = response.rows.map(row=>row.doc);

  return res.json(segments);
}

const deleteSegment = async (req, res ) => {
  console.log("delete segment request params:", JSON.stringify(req.params));

  let segmentsDb = Nano ('http://admin:password@localhost:5984/segments');

  let response = await segmentsDb.destroy(req.params["id"],req.params["rev"]);
  console.log("delete response:", response)

  return res.status(204).end();
}



export {getSegments, deleteSegment}


