<template>
  <div>
    <div v-if=errorMessage>
      {{ errorMessage }}
    </div>
    <div v-else>
      <div class="border card card-body card-shadow-info flex-column mb-2 p-2">

        <b-table :items="incentives" :fields="fields">
          <template v-slot:cell(recipient)="incentive">
            <b-dropdown class="m-2" variant="primary" :text="incentive.item.recipient?incentive.item.recipient: 'Select recipient'" >
              <b-dropdown-item v-for="(recipient, index) in recipients" :key="index" @click="recipientChanged(recipient,incentive.index)" >
                {{ recipient }}
              </b-dropdown-item>
            </b-dropdown>

          </template>

          <template v-slot:cell(type)="incentive">
            <b-dropdown class="m-2" variant="primary" :text="incentive.item.type?incentive.item.type: 'Select incentive type'" >
              <b-dropdown-item v-for="(type, index) in types" :key="index" @click="typeChanged(type,incentive.index)" >
                {{ type }}
              </b-dropdown-item>
            </b-dropdown>
          </template>

          <template v-slot:cell(descriptor)="incentive">
            <b-dropdown class="m-2" variant="primary" :text="incentive.item.descriptor?incentive.item.descriptor: 'Select type descriptor'" >
              <b-dropdown-item v-for="(descriptor, index) in descriptors[incentive.item.type]" :key="index" @click="descriptorChanged(descriptor,incentive.index)" >
                {{ descriptor }}
              </b-dropdown-item>
            </b-dropdown>
          </template>

          <template v-slot:cell(value)="incentive">
            <input name="Incentive Value"
              v-model="incentive.item.value" 
              id="incentiveValue"
              placeholder="with a placeholder"
              class="form-control"/>
          </template>
          <template v-slot:cell(actions)="incentive">
            <b-button class="mr-2 mb-2" variant='outline-danger' @click="deleteIncentive(incentive.index)" >
              <i class="pe-7s-trash"> </i>
            </b-button>
          </template>
        </b-table>
      <b-button class="m-2" variant="primary" >Add Incentive</b-button>
      </div>
      <b-button class="m-2" variant="primary" >Add Communications</b-button>
    </div>
  </div>
</template>

<script>

export default {
  props:  {
    activeIncentives: Object,
  },
  data() {
    return {
      incentives: this.activeIncentives.incentives,
      fields: ["recipient","type","descriptor","value","actions" ],
      errorMessage: null,
      recipients: ["seller", "buyer"],
      types: ["Free Bundle", "Airtime Credit"],
      descriptors: { 
        "Free Bundle": ["100 Free SMSs","Weekend Discounts"], 
        "Airtime Credit": ["Airtime Bonus"],
      },
    };
  },
  methods: {
    deleteIncentive(index) {
      this.incentives = this.incentives.splice(index,1);
      this.$emit('incentivesChanged', this.incentives);
    },
    recipientChanged(recipient,incentive_index) {
      this.incentives[incentive_index].recipient = recipient;
      this.$emit('incentivesChanged', this.incentives);
    },
    typeChanged(type,incentive_index) {
      this.incentives[incentive_index].type = type;
      this.$emit('incentivesChanged', this.incentives);
    },
    descriptorChanged(descriptor,incentive_index) {
      this.incentives[incentive_index].descriptor = descriptor;
      this.$emit('incentivesChanged', this.incentives);
    },

    valueChanged(value,incentive_index) {
      this.incentives[incentive_index].value = value;
      this.$emit('incentivesChanged', this.incentives);
    },
  },
}

</script>

