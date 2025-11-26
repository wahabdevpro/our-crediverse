<template>
  <div>
    <v-text-field
      label="Pick a date"
      :value="date"
      readonly
      v-on:click="showPicker = true"
    ></v-text-field>

    <v-date-picker
      v-model="date"
      :show="showPicker"
      v-on:close="showPicker = false"
    ></v-date-picker>
  </div>
</template>

<script>
import { ref } from 'vue'
import { VDatePicker } from "vuetify/labs/VDatePicker";

export default {
  components: {VDatePicker},
  name: 'DatePickerComponent',

  emits: ['update:modelValue'],

  props: {
    modelValue: {
      type: String,
      default: ''
    }
  },

  setup(props, { emit }) {
    let date = ref(props.modelValue )
    let showPicker = ref(false)

    const updateValue = () => {
      emit('update:modelValue', date.value)
    }

    return {
      date,
      showPicker,
      updateValue
    }
  }
}
</script>

