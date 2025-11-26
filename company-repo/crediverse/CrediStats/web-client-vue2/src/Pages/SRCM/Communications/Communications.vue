<template>
  <div>
    <div v-if=errorMessage>
      {{ errorMessage }}
    </div>
    <div v-else>

      <div class="border card card-body card-shadow-info flex-column mb-2 p-2">
        <b-table :items="communications" :fields="fields">
          <template v-slot:cell(recipient)="communication">
            <b-dropdown class="m-2" variant="primary" :text="communication.item.recipient?communication.item.recipient: 'Select recipient'" >
              <b-dropdown-item v-for="(recipient, index) in recipients" :key="index" @click="recipientChanged(recipient,communication.index)" >
                {{ recipient }}
              </b-dropdown-item>
            </b-dropdown>
          </template>

          <template v-slot:cell(trigger)="communication">
            <b-dropdown class="m-2" variant="primary" :text="communication.item.trigger?communication.item.trigger: 'Select trigger'" >
              <b-dropdown-item v-for="(trigger, index) in triggers" :key="index" @click="triggerChanged(trigger,communication.index)" >
                {{ trigger }}
              </b-dropdown-item>
            </b-dropdown>
          </template>

          <template v-slot:cell(channel)="communication">
            <b-dropdown class="m-2" variant="primary" :text="communication.item.channel?communication.item.channel: 'Select channel '" >
              <b-dropdown-item v-for="(channel, index) in channels" :key="index" @click="descriptorChanged(channel,communication.index)" >
                {{ channel }}
              </b-dropdown-item>
            </b-dropdown>
          </template>

          <template v-slot:cell(text)="communication">
            <input name="communication text"
              v-model="communication.item.text" 
              id="communicationText"
              placeholder="enter message here"
              class="form-control"/>
          </template>
          <template v-slot:cell(actions)="communication">
            <b-button class="mr-2 mb-2" variant='outline-danger' @click="deleteCommunication(communication.index)" >
              <i class="pe-7s-trash"> </i>
            </b-button>
          </template>
        </b-table>
      </div>
      <b-button class="m-2" variant="primary" >Add Communication</b-button>

    </div>
  </div>
</template>

<script>

export default {
  props:  {
    activeCommunications: Object,
  },
  data() {
    return {
      communications: this.activeCommunications.communications,
      fields: ["recipient","trigger","channel","text","actions" ],
      errorMessage: null,
      recipients: ["seller", "buyer"],
      triggers: ["On Transaction", "Blast"],
      channels: ["SMS", "Email","USSD Push"],
    };
  },
  methods: {
    deleteCommunication(index) {
      this.communications = this.communications.splice(index,1);
      this.$emit('communicationsChanged', this.communications);
    },
    recipientChanged(recipient,communication_index) {
      this.communications[communication_index].recipient = recipient;
      this.$emit('communicationsChanged', this.communications);
    },
    triggerChanged(trigger,communication_index) {
      this.communications[communication_index].tigger= trigger;
      this.$emit('communicationsChanged', this.communications);
    },
    channelChanged(channel,communication_index) {
      this.communications[communication_index].chanel = channel;
      this.$emit('communicationsChanged', this.communications);
    },

    textChanged(text,communication_index) {
      this.communications[communication_index].text = text;
      this.$emit('communicationsChanged', this.communications);
    },
  },
}

</script>

