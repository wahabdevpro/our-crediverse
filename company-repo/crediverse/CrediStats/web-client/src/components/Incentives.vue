<template>
  <v-card v-if="errorMessage" title="Error" :text="errorMessage"></v-card>
  <v-card v-else>
      <v-table class="w-100">
        <thead>
          <tr>
            <th class="text-left">        
              Recipient
            </th>
            <th class="text-left">        
              Type
            </th>
            <th class="text-left">        
              Descriptor
            </th>
            <th class="text-left">        
              Value
            </th>
            <th class="text-left">        
              Actions
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(incentive, incentiveIndex) in incentives"
            :key="incentiveIndex"
          >
            <td>
              <v-menu>
                <template v-slot:activator="{ props }">
                  <v-text-field
                    v-bind="props"
                    id="recipientValue"
                    placeholder="Select Recipient"
                    v-model="incentive.recipient"
                    required
                  >
                  </v-text-field>
                </template>
                <v-list>
                  <v-list-item
                    v-for="(recipient, index) in recipients"
                    :key="index"
                    :value="recipient"
                    @click="onRecipientChange(recipient,incentiveIndex)"
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
                    id="typeValue"
                    placeholder="Select Type"
                    v-model="incentive.type"
                    required
                  >
                  </v-text-field>
                </template>
                <v-list>
                  <v-list-item
                    v-for="(type, index) in types"
                    :key="index"
                    :value="type"
                    @click="onTypeChange(type,incentiveIndex)"
                  >
                    <v-list-item-title>{{ type }}</v-list-item-title>
                  </v-list-item>
                </v-list> 
              </v-menu>
            </td>
            <td>
              <v-menu>
                <template v-slot:activator="{ props }">
                  <v-text-field
                    v-bind="props"
                    id="descriptorValue"
                    placeholder="Select descriptor"
                    v-model="incentive.descriptor"
                    required
                  >
                  </v-text-field>
                </template>
                <v-list>
                  <v-list-item
                    v-for="(descriptor, index) in descriptors[incentive.type]"
                    :key="index"
                    :value="descriptor"
                    @click="onDescriptorChange(descriptor,incentiveIndex)"
                  >
                    <v-list-item-title>{{ descriptor }}</v-list-item-title>
                  </v-list-item>
                </v-list> 
              </v-menu>
            </td>
            <td>{{ incentive.value }}</td>
          </tr>
        </tbody>
      </v-table> 
      <v-btn>Add Incentive</v-btn>
    </v-card>
    <v-btn>Add Communications</v-btn>
</template>


<!-- b-table :items="incentives" :fields="fields">
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
</b-table -->




<script>
import { ref,onUpdated,onMounted,onBeforeUpdate,computed } from "vue";

export default {
  inheritAttrs: false,
  props:  {
    activeIncentives: Object,
  },
  setup(props,{emit}){
    let incentives = ref(props.activeIncentives);
    let fields = ref(["recipient","type","descriptor","value","actions" ]);
    let errorMessage = ref(null);
    let recipients = ref(["seller", "buyer"]);
    let types =  ref(["Free Bundle", "Airtime Credit"]);
    let descriptors = ref ({ 
      "Free Bundle": ["100 Free SMSs","Weekend Discounts"], 
      "Airtime Credit": ["Airtime Bonus"],
    });


    function deleteIncentive(index) {
      incentives.value = incentives.value.splice(index,1);
      emit('incentivesChanged', incentives.value);
    };
    const onRecipientChange = (recipient,incentive_index) => {
      console.log("Incentives onRecipientChange: ", {recipient,incentive_index});

      incentives.value[incentive_index].recipient = recipient;

      emit('incentivesChanged', incentives.value);
    };

    const onTypeChange = (type,incentive_index) => {
      incentives.value[incentive_index].type = type;
      emit('incentivesChanged', incentives.value);
    };
    const onDescriptorChange = (descriptor,incentive_index) => {
      incentives.value[incentive_index].descriptor = descriptor;
      emit('incentivesChanged', incentives.value);
    };

    const onValueChange = (value,incentive_index) => {
      incentives.value[incentive_index].value = value;
      emit('incentivesChanged', incentives.value);
    };

    return {
      incentives ,
      deleteIncentive,
      onDescriptorChange,
      descriptors ,
      errorMessage,
      fields,
      onRecipientChange,
      recipients,
      onTypeChange,
      types,
      onValueChange,
    };
  },
}

</script>

