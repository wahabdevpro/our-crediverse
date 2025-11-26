import Nano from 'nano';

const nano = Nano('http://admin:password@0.0.0.0:5984');

const dimensions = [       
  "Seller Location", 
  "Seller Tier", 
  "Seller Average Daily Sales",
  "Buyer Monthly Usage",
  "Buyer Last Purchase Value",
  "Transaction Amount",
  "Transaction Time of Day", 
  "Transaction Day of Deek", 
  "Transaction UCIP Service Class" ,
];

async function createLocationLists() {
  let  locationLists = [
    {
      name: "Cities",
      filenameUploaded: "cities_2022-01-01.csv",
      timeUploaded: "2022-01-01 16:07:00",
      locations: [
        "Johannesburg",
        "Pretoria",
        "Cape Town",
        "Durban",
        "Bloemfontein",
        "Gqeberha",
        "Polokwane",
      ]
    },
    {
      name: "Towns",
      filenameUploaded: "towns_2022-01-01.csv",
      timeUploaded: "2022-01-01 16:09:00",
      locations: [
        "Vereeninging",
        "Sassolburg",
        "Mokopane",
        "Villiers",
        "St Francis",
        "Paarl",
        "Cullinan",
        "Bronkhorstspruit",
        "Coligny",
        "Hoedspruit",
        "Middelburg",
      ]
    },

  ]


  let locationListsDb = nano.use('location_lists');

  for (let locationList of locationLists) {

    let insertResponse = await locationListsDb.insert(locationList);

    console.log(insertResponse);
  }


}

async function createSegments() {
  let segments = [
    {
      name: "High Revenue Agents",
      filenameUploaded: "highRevenueAgents_20220101.csv",
      timeUploaded: "2022-01-01 16:07:00",
      msisdns: [
        "0820000000",
        "0820000001",
        "0820000002",
        "0820000003",
        "0820000004",
        "0820000005",
      ]
    },
    {
      name: "New Agents",
      filenameUploaded: "newAgents_20220201.csv",
      timeUploaded: "2022-02-01 13:03:00",
      msisdns: [
        "0820000006",
        "0820000007",
        "0820000008",
        "0820000009",
      ]
    },
    {
      name: "Frequent Traders",
      filenameUploaded: "newAgents_20220201.csv",
      timeUploaded: "2022-02-01 13:03:00",
      msisdns: [
        "0820000010",
        "0820000011",
        "0820000012",
        "0820000013",
        "0820000014",
        "0820000015",
        "0820000016",
        "0820000017",
      ]
    },
  ];

  let segmentsDb = nano.use('segments');

  for (let segment of segments) {

    let insertResponse = await segmentsDb.insert(segment);

    console.log(insertResponse);
  }

}

 
async function createCampaigns () {
  let campaigns = [
    {
      campaignName:          "Low CMS sites Q122",
      status:                  "Live",
      startDate:              "2022-01-01",
      endDate:                "2022-03-31",
      campaignTransactions:  "7,878,422",
      campaignRevenue:       "$34,878,211",
      enabled:                true,
      communications:[
        {
          recipient: "seller",
          trigger: "on transaction",
          channel: "USSD Push",
          text: "You have sold ${{transaction_amount}} of airtime to {{transaction_recipient}}"
        },

        {
          recipient: "buyer",
          trigger: "on transaction",
          channel: "USSD Push",
          text: "You have bought ${{transaction_amount}} of airtime}}"
        },

        {
          recipient: "buyers",
          trigger: "blast",
          channel: "SMS",
          text: "Summer bundle specials available! Visit your Crediverse Agent for more info. "
        },
      ],
      incentives: [
        {
          recipient: "seller", 
          type: "Airtime Credit", 
          descriptor: "",
          value: 10,
        },
        {
          recipient: "buyer",
          type: "Free Bundle",
          descriptor: "10 Minutes, 100 SMS",
          value: 70,
        }
      ],
      condition: {
        operator: 'one of',
        subConditions: [
          {
            operator: 'all of',
            subConditions: [
              {
                operator: 'is',
                dimension: 'Seller Location',
                value: 'Pretoria',
              }, 
              {
                operator: 'more than',
                dimension: 'Transaction Amount',
                value: '$10.00',
              }
            ]
          },
          {
            operator: 'is',
            dimension: 'Seller Location',
            value: 'Johannesburg',
          },
        ]
      },
    },
    {
      campaignName:         "Thursday Commission Bonus",
      status:                "Live",
      startDate:            "2022-02-01",
      endDate:              "2022-12-31",
      campaignTransactions: "2,123,529",
      campaignRevenue:      "$10,876,234",
      enabled:              true ,
      communications:[
        {
          recipient: "seller",
          trigger: "on transaction",
          channel: "USSD Push",
          text: "You have sold ${{transaction_amount}} of airtime to {{transaction_recipient}}"
        },

        {
          recipient: "buyer",
          trigger: "on transaction",
          channel: "USSD Push",
          text: "You have bought ${{transaction_amount}} of airtime}}"
        },

        {
          recipient: "buyers",
          trigger: "blast",
          channel: "SMS",
          text: "Summer bundle specials available! Visit your Crediverse Agent for more info. "
        },
      ],
       incentives: [
        {
          recipient: "seller", 
          type: "Airtime Credit", 
          descriptor: "",
          value: 10,
        },
        {
          recipient: "buyer",
          type: "Free Bundle",
          descriptor: "10 Minutes, 100 SMS",
          value: 70,
        }
      ],
       condition: {
        operator: 'AND',
        subConditions: [ 
          {
            operator: 'is',
            dimension: 'Seller Location',
            value: 'Pretoria',
          },
          {
            operator: 'is greater than',
            dimension: 'Transaction Amount',
            value: '$10.00',
          },
        ]
      },
    },
    {
      campaignName:         "Easter Bonanza",
      status:                "Scheduled",
      startDate:            "2022-04-15",
      endDate:              "2022-04-20",
      campaignTransactions: "0",
      campaignRevenue:      "$0",
      enabled:              true,
      communications:[
        {
          recipient: "seller",
          trigger: "on transaction",
          channel: "USSD Push",
          text: "You have sold ${{transaction_amount}} of airtime to {{transaction_recipient}}"
        },

        {
          recipient: "buyer",
          trigger: "on transaction",
          channel: "USSD Push",
          text: "You have bought ${{transaction_amount}} of airtime}}"
        },

        {
          recipient: "buyers",
          trigger: "blast",
          channel: "SMS",
          text: "Summer bundle specials available! Visit your Crediverse Agent for more info. "
        },
      ],
       incentives: [
        {
          recipient: "seller", 
          type: "Airtime Credit", 
          descriptor: "",
          value: 10,
        },
        {
          recipient: "buyer",
          type: "Free Bundle",
          descriptor: "10 Minutes, 100 SMS",
          value: 70,
        }
      ],
       condition: {
        operator: 'AND',
        subConditions: [ 
          {
            operator: 'is',
            dimension: 'Seller Location',
            value: 'Pretoria',
          },
          {
            operator: 'is greater than',
            dimension: 'Transaction Amount',
            value: '$10.00',
          },
        ]
      },  
    },  
    {
      campaignName:         "Test Campaign",
      status:                "Disabled",
      startDate:            "2022-01-01",
      endDate:              "2022-02-02",
      campaignTransactions: "16",
      campaignRevenue:      "$102,123",
      enabled:              false ,
      communications:[
        {
          recipient: "seller",
          trigger: "on transaction",
          channel: "USSD Push",
          text: "You have sold ${{transaction_amount}} of airtime to {{transaction_recipient}}"
        },

        {
          recipient: "buyer",
          trigger: "on transaction",
          channel: "USSD Push",
          text: "You have bought ${{transaction_amount}} of airtime}}"
        },

        {
          recipient: "buyers",
          trigger: "blast",
          channel: "SMS",
          text: "Summer bundle specials available! Visit your Crediverse Agent for more info. "
        },
      ],
       incentives: [
        {
          recipient: "seller", 
          type: "Airtime Credit", 
          descriptor: "",
          value: 10,
        },
        {
          recipient: "buyer",
          type: "Free Bundle",
          descriptor: "10 Minutes, 100 SMS",
          value: 70,
        }
      ],
       condition: {
        operator: 'AND',
        subConditions: [ 
          {
            operator: 'is',
            dimension: 'Seller Location',
            value: 'Pretoria',
          },
          {
            operator: 'is greater than',
            dimension: 'Transaction Amount',
            value: '$10.00',
          },
        ]
      },   
    },   
    {
      campaignName:         "Christmas 21",
      status:                "Completed",
      startDate:            "2021-12-20",
      endDate:              "2021-12-28",
      campaignTransactions: "5,324,897",
      campaignRevenue:      "$25,786,212",
      enabled:              false,
      communications:[
        {
          recipient: "seller",
          trigger: "on transaction",
          channel: "USSD Push",
          text: "You have sold ${{transaction_amount}} of airtime to {{transaction_recipient}}"
        },

        {
          recipient: "buyer",
          trigger: "on transaction",
          channel: "USSD Push",
          text: "You have bought ${{transaction_amount}} of airtime}}"
        },

        {
          recipient: "buyers",
          trigger: "blast",
          channel: "SMS",
          text: "Summer bundle specials available! Visit your Crediverse Agent for more info. "
        },
      ],
       incentives: [
        {
          recipient: "seller", 
          type: "Airtime Credit", 
          descriptor: "",
          value: 10,
        },
        {
          recipient: "buyer",
          type: "Free Bundle",
          descriptor: "10 Minutes, 100 SMS",
          value: 70,
        }
      ],
       condition: {
        operator: 'AND',
        subConditions: [ 
          {
            operator: 'is',
            dimension: 'Seller Location',
            value: 'Pretoria',
          },
          {
            operator: 'is greater than',
            dimension: 'Transaction Amount',
            value: '$10.00',
          },
        ]
      }, 
    },
  ];

  let campaignsDb = nano.use('campaigns');

  for (let campaign of campaigns) {

    let insertResponse = await campaignsDb.insert(campaign);

    console.log(insertResponse);
  }
}

try {
  await nano.db.destroy('campaigns');
} catch (error) {
  console.log(error);
}

try {
  await nano.db.destroy('segments');
} catch (error) {
  console.log(error);
}

try {
  await nano.db.destroy('location_lists');
} catch (error) {
  console.log(error);
}

await nano.db.create('campaigns');
await createCampaigns();

await nano.db.create('segments');
await createSegments();

await nano.db.create('location_lists');
await createLocationLists();



