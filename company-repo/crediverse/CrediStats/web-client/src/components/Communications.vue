<template>
  <v-card v-if=errorMessage>
    {{ errorMessage }}
  </v-card>
  <v-card v-else>
    <v-table class="w-100">
      <thead>
        <tr>
          <th class="text-left">        
            Recipient
          </th>
          <th class="text-left">        
            Trigger
          </th>
          <th class="text-left">        
            Channel 
          </th>
          <th class="text-left">        
            Text 
          </th>
          <th class="text-left">        
            Actions
          </th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="(communication, communicationIndex) in communications"
          :key="communicationIndex"
        >
          <td>
            <v-menu>
              <template v-slot:activator="{ props }">
                <v-text-field
                  v-bind="props"
                  id="reciptientValue"
                  placeholder="Select Recipient"
                  v-model="communication.recipient"
                  required
                >
                </v-text-field>
              </template>
              <v-list>
                <v-list-item
                  v-for="(recipient, index) in recipients"
                  :key="index"
                  :value="recipient"
                  @click="onRecipientChange(recipient,communicationIndex)"
                >
                  <v-list-item-title>{{ recipient }}</v-list-item-title>
                </v-list-item>
              </v-list> 
            </v-menu>
          </td>
          <td>
            <v-menu>
              <template v-slot:activator="{ props }">
                <v-text-field
                  v-bind="props"
                  id="triggerValue"
                  placeholder="Select Trigger"
                  v-model="communication.trigger"
                  required
                >
                </v-text-field>
              </template>
              <v-list>
                <v-list-item
                  v-for="(trigger, index) in triggers"
                  :key="index"
                  :value="trigger"
                  @click="onTriggerChange(trigger,communicationIndex)"
                >
                  <v-list-item-title>{{ trigger }}</v-list-item-title>
                </v-list-item>
              </v-list> 
            </v-menu>
          </td>
          <td>
            <v-menu>
              <template v-slot:activator="{ props }">
                <v-text-field
                  v-bind="props"
                  id="channelValue"
                  placeholder="Select Channel"
                  v-model="communication.channel"
                  required
                >
                </v-text-field>
              </template>
              <v-list>
                <v-list-item
                  v-for="(channel, index) in channels"
                  :key="index"
                  :value="channel"
                  @click="onChannelChange(channel,communicationIndex)"
                >
                  <v-list-item-title>{{ channel }}</v-list-item-title>
                </v-list-item>
              </v-list> 
            </v-menu>

          </td>
          <td>{{communication.text}}</td>
          <td>Buttons</td>
        </tr>
      </tbody>
    </v-table>
  </v-card>
  <v-btn>Add Communication</v-btn>
</template>






<!-- b-table :items="communications" :fields="fields">
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
</b-table -->
<script>
import { ref,onUpdated,onMounted,onBeforeUpdate,computed } from "vue";

export default {
  inheritAttrs: false,
  props:  {
    activeCommunications: Object,
  },
  setup (props,{emit}) {
    let communications = ref(props.activeCommunications);
    let fields = ref( ["recipient","trigger","channel","text","actions" ]);
    let errorMessage = ref( null);
    let recipients = ref( ["seller", "buyer"]);
    let triggers = ref( ["On Transaction", "Blast"]);
    let channels = ref( ["SMS", "Email","USSD Push"]);
    /*
    const deleteCommunication = (index) => {
      communications.value = communications.value.splice(index,1);
      emit("communicationsChanged", communications.value);
    };*/

    const onRecipientChange = (recipient,communication_index) => {
      communications.value[communication_index].recipient = recipient;
      emit("communicationsChanged", communications.value);
    };
    
    const onTriggerChange = (trigger,communication_index) => {
      console.log("onTriggerChange: ", trigger,communication_index)
      console.log("onTriggerChange: ",JSON.stringify(communications.value) );
      communications.value[communication_index].trigger= trigger;
      console.log("onTriggerChange(after): ",JSON.stringify(communications.value) );
      emit("communicationsChanged", communications.value);
    };


    const onChannelChange = (channel,communication_index) => {

      communications.value[communication_index].channel = channel;

      emit("communicationsChanged", communications.value);
    };
    const onTextChange = (text,communication_index) => {
      communications.value[communication_index].text = text;
      emit("communicationsChanged", communications.value);
    };
    return {
      onTextChange,
      onChannelChange,
      onTriggerChange,
      onRecipientChange,
      communications,
      fields,
      errorMessage,
      recipients,
      triggers,
      channels,
    };
  },
}

</script>

