<template>
  <div class="flex-container">
    <b-alert v-if="errorMessage" variant="danger" show dismissible @dismissed="errorMessage = null">
      {{ errorMessage }}
    </b-alert>
    <div class="p-2">
      <div v-if="isSimpleCondition">
        <div v-if="!editing" class="align-items-center border border-info border-primary card card-body card-shadow-info flex-row mb-2 p-2">
          <h4> If {{ condition.dimension}} {{condition.property}} {{condition.operator}} {{condition.value}} </h4>
          <b-button class="m-2"  variant='primary' @click="editCondition"  >
            <i class="pe-7s-edit"> </i>
          </b-button>

          <b-button v-if class="m-2" variant='outline-primary'>
            AND
          </b-button>

          <b-button class="m-2" variant='outline-primary'>
            OR 
          </b-button>

          <b-button class="m-2" variant='outline-danger' @click="deleteCondition(condition.index)"  >
            <i class="pe-7s-trash"> </i>
          </b-button>

        </div>
        <div v-else class="align-items-center border border-info border-primary card card-body card-shadow-info flex-row mb-2 p-2">
          <h3>If</h3>
          <b-dropdown class="m-2" variant="primary" :text="condition.dimension?condition.dimension: 'Select Dimension'" >
            <b-dropdown-item v-for="(dimension, index) in dimensions" :key="index"  @click="onDimensionChange(dimension)">
              {{ dimension }}
            </b-dropdown-item>
          </b-dropdown>

          <b-dropdown class="m-2" variant="primary" :text="condition.property?condition.property: 'Select Property'" >
            <b-dropdown-item v-for="(dimensionProperty, index) in dimensionProperties[condition.dimension]" :key="index"  @click="onDimensionPropertyChange(dimensionProperty)">
              {{ dimensionProperty }}
            </b-dropdown-item>
          </b-dropdown>

          <b-dropdown class="m-2" variant="primary" :text="condition.operator?condition.operator: 'Select evaluation operator'" >
            <b-dropdown-item v-for="(operator, index) in simpleOperators" :key="index"  @click="onOperatorChange(operator)">
              {{ operator }}
            </b-dropdown-item>
          </b-dropdown>

          <input name="Value"
            v-model="condition.value"
            id="conditionValue"
            placeholder="value to evaluate"
            class="form-control"/>

          <b-button class="m-2"  variant='primary' @click="doneEditingCondition"  >
            <i class="pe-7s-edit"> </i>
          </b-button>

          <b-button class="m-2"  variant='outline-danger' @click="deleteCondition(condition,index)"  >
            <i class="pe-7s-trash"> </i>
          </b-button>


        </div>
      </div>
      <div v-else > 
        <div class="border border-info border-secondary card card-body card-shadow-info flex-column mb-2 p-2">
          <b-dropdown class="m-2" variant="primary h4" :text="condition.operator?condition.operator: 'Select evaluation operator'" >
            <b-dropdown-item v-for="(operator, index) in compoundOperators" :key="index"  @click="onOperatorChange(operator)">
              <h4>{{ operator }}</h4>
            </b-dropdown-item>
          </b-dropdown>
          <div v-for="(subCondition, index) in condition.subConditions" :key="index">
            <condition @conditionChanged="conditionChanged" :index=index :activeCondition="subCondition"></condition>
            <div v-if="condition.operator == 'one of' && index  < condition.subConditions.length  - 1" >
              <h5>OR</h5>
            </div>
            <div v-if="condition.operator == 'all of'&& index  < condition.subConditions.length  - 1" >
              <h5>AND</h5>
            </div>
          </div>
          <div>
          <b-button class="m-2" variant="primary" >Add Sub Condition</b-button>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>


<script>

export default {
  components: {
    Condition: () => import('./Condition.vue'),
  },
  props: {
    activeCondition: Object,
    index: Number,
  },
  computed: {
    isSimpleCondition() {
      return !this.condition.subConditions;
    }
  },
  data() {
    return {
      condition: this.activeCondition, 
      editing: false,
      simpleOperators: ["is", "in", "less than", "more than", "equals" , "does not equal"],
      compoundOperators: ["one of", "all of"],
      currentDimension: "",
      dimensions: ["seller","buyer","transaction"],
      dimensionProperties: {
        "seller": ["location", "tier", "average daily sales" ],
        "buyer": ["monthly usage","last purchase value"],
        "transaction": ["amount","time of day", "day of week", "UCIP service class"],
      },
      errorMessage: null,
    };
  },
  methods: {
    getDimensions() {
      console.log(this.dimensions);
      return this.dimensions.keys();
    },
    doneEditingCondition(){
      this.editing=false;
    },
    editCondition() {
      this.editing=true;
    },
    onDimensionChange(dimension) {
      console.log("setting dimension", dimension);
      this.condition.dimension = dimension;
      this.$emit('conditionChanged', this.condition, this.index);
    },
    onDimensionPropertyChange(dimensionProperty) {
      console.log("setting dimensionProperty ", dimensionProperty);
      this.condition.property = dimensionProperty;
      this.$emit('conditionChanged', this.condition, this.index);
    },
    onOperatorChange(operator) {
      console.log("setting operator", operator);
      this.condition.operator = operator;
      this.$emit('conditionChanged', this.condition, this.index);
    },
    deleteCondition(index){
      this.condition = null;
      this.$emit('conditionChanged', null, this.index);
    },

    conditionChanged(newConditionValue,index) {
      if (newConditionValue) {
        this.condition.subConditions[index] = newConditionValue;
      } else {
        this.condition.subConditions.splice(index,1)
      }
    },


  }
}

</script>
