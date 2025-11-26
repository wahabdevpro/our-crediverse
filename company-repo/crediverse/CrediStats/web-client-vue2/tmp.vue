<template>
  <div>
    <div>
      <b-button class="mr-2 mb-2" variant='info'>
        New Campaign!!
      </b-button>
    </div>
    <div>
      <b-table :items="campaigns" :fields="fields">
        <template v-slot:cell(action)="data">
        </template>
      </b-table>
    </div>
  </div>
</template>

<script> 


  export default {
    data() {
      return { 
        fields:["id","name","satus","startDate","endDate","nTransactions","revenue","enabled"],
        headings:[
            "Campaign ID",
            "Campaign Name",
            "Status",
            "Start Date",
            "End Date",
            "Campaign Transactions",
            "Campaign Revenue",
            "Enabled",
            "Actions", 
        ],
 
        campaigns: [
        {
          id:             "1",
          name:          "Low CMS sites Q122",
          satus:         "Live",
          startDate:     "01-01-2022",
          endDate:       "31-03-2022",
          nTransactions: "$7878422",
          revenue:       "$34,878,211",
          enabled:       true,
        },
        {
          id:            "2",
          name:          "Thursday Commission Bonus",
          satus:         "Live",
          startDate:     "01-02-2022",
          endDate:       "31-12-2022",
          nTransactions: "2,123,529",
          revenue:       "$10,876,234",
          enabled:       true 
        }, 
        {
          id:            "3",
          name:          "Easter Bonanza",
          satus:         "Scheduled",
          startDate:     "15-04-2022",
          endDate:       "20-04-2022",
          nTransactions: "0",
          revenue:       "$0",
          enabled:       true
        },
        {
          id:            "4",
          name:          "Test Campaign",
          satus:         "Disabled",
          startDate:     "01-01-2022",
          endDate:       "02-02-2022",
          nTransactions: "16",
          revenue:       "$102,123",
          enabled:       false 
        },
        {
          id:            "5",
          name:          "Christmas 21",
          satus:         "Completed",
          startDate:     "20-12-2021",
          endDate:       "28-12-2021",
          nTransactions: "5,324,897",
          revenue:       "$25,786,212",
          enabled:       false 
        }, 
      ]}
    } 
  }

</script>
