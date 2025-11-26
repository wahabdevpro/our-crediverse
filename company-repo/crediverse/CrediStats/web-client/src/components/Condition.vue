<template>
  <v-alert closable v-if="errorMessage" @click:closed="errorMessage = null">
    {{ errorMessage }}
  </v-alert>
  <div v-if="isSimpleCondition">
    <v-card v-if="!isEditing && !newCondition" class = "rounded-xl pa-2 ma-2" variant="tonal">
      <v-container class="">
        <v-row no-gutters>
          <v-col align-self="start">
            <h4> If {{ condition.dimension}} {{condition.operator}} {{condition.value}} </h4>
          </v-col>
          <v-col align-self="center">
            <v-chip v-if="parentType && (parentType==='one of' || parentType==='Campaign')" @click="onCombineWithAndClicked">
              AND ...
            </v-chip>
            <v-chip v-if="parentType && (parentType==='all of' || parentType==='Campaign')" @click="onCombineWithOrClicked">
              OR ... 
            </v-chip>
          </v-col>
          <v-col align="end">
            <v-chip @click="onEditConditionClicked"  >
              <v-icon icon="mdi-book-edit-outline" ></v-icon>
            </v-chip>
            <v-icon @click="onDeleteConditionClicked(condition.index)"  >
              <v-icon icon="mdi-delete-outline" ></v-icon>
            </v-icon>
          </v-col>
        </v-row>
      </v-container>
    </v-card>
    <!-- isEditing -->
    <v-card v-else class="ma-4 pa-4 rounded-xl" variant="tonal" > 
      <v-form v-model="isConditionBeingEditedValid" class="px-3" enabled="conditionValid" @submit="onConditionSubmited"> 
        <h3>If</h3>
        <v-container>
          <v-row>
            <v-col>
              <v-menu>
                <template v-slot:activator="{ props }">
                  <v-text-field
                    v-bind="props"
                    id="dimensionValue"
                    placeholder="Select Dimension"
                    v-model="conditionBeingEdited.dimension"
                    label="Dimension"
                    :rules="dimensionRules"
                    required
                  >
                  </v-text-field>
                </template>
                <v-list>
                  <v-list-item
                    v-for="(dimension, index) in dimensions"
                    :key="index"
                    :value="dimension.label"
                    @click="onDimensionSelected(dimension.label)"
                  >
                    <v-list-item-title>{{ dimension.label }}</v-list-item-title>
                  </v-list-item>
                </v-list> 
              </v-menu>
            </v-col>
            <v-col>
              <v-menu v-if="conditionBeingEdited.dimension && conditionBeingEdited.dimension !== 'Seller Tier'" >
                <template v-slot:activator="{ props }">
                  <v-text-field
                    v-bind="props"
                    id="operatorValue"
                    placeholder="Select Operator"
                    v-model="conditionBeingEdited.operator"
                    label="Operator"
                    :rules="operatorRules"
                    required
                  >
                  </v-text-field>
                </template>
                <v-list>
                  <v-list-item
                    v-for="(operator, index) in dimensionOperators"
                    :key="index"
                    :value="operator.label"
                    @click="onOperatorSelected(operator.label)"
                  >
                    <v-list-item-title>{{ operator.label }}</v-list-item-title>
                  </v-list-item>
                </v-list> 
              </v-menu>
            </v-col>
            <v-col>

              <!-- 
              provinceSelectionBox(single, multiple) 
              tierSelectionBox(single)
              day of week selection singe and multiple
              number range selection 
              time of day selection -->
              
              <div v-if="conditionBeingEdited.dimension === 'Seller Location'">
                <div v-if="conditionBeingEdited.operator === 'in' "> 
                  <v-menu>
                    <template v-slot:activator="{ props }">
                      <v-text-field
                        v-bind="props"
                        id="conditionOperatorOptions"
                        placeholder="Select Location"
                        v-model="conditionBeingEdited.value"
                        label="Location"
                        required
                        :rules="locationRules"               
                      >
                      </v-text-field>
                    </template>
                    <v-list>
                      <v-list-item
                        v-for="(option, index) in operatorOptions"
                        :key="index"
                        :value="option"
                        @click="onFilterSelected(option)">
                        <v-list-item-title>{{ option }}</v-list-item-title>
                      </v-list-item>
                    </v-list> 
                  </v-menu>

                </div>
                <div v-if="condition.operator === 'in one of'">
                  <v-menu>
                    <template v-slot:activator="{ props }">
                      <v-text-field
                        v-bind="props"
                        id="conditionOperatorOptions"
                        placeholder="Select Location"
                        v-model="conditionBeingEdited.value"
                        label="Location"
                        required
                        :rules="locationRules"
                      >
                      </v-text-field>
                    </template>
                    <v-list>
                      <v-list-item
                        v-for="(option, index) in operatorOptions"
                        :key="index"
                        :value="option"
                        @click="onFilterSelected(option)">
                        <v-list-item-title>{{ option }}</v-list-item-title>
                      </v-list-item>
                    </v-list> 
                  </v-menu>

                </div>
              </div>
              
              <div v-if="conditionBeingEdited.dimension === 'Seller Segment'">
                  <v-menu>
                    <template v-slot:activator="{ props }">
                      <v-text-field
                        v-bind="props"
                        id="conditionSegmentOptions"
                        placeholder="Select Segment"
                        v-model="conditionBeingEdited.value"
                        label="Segment"
                        required
                        :rules="segmentRules"
                      >
                      </v-text-field>
                    </template>
                    <v-list>
                      <v-list-item
                        v-for="(segment, index) in segmentNames"
                        :key="index"
                        :value="segment.name"
                        @click="onFilterSelected(segment.name)">
                        <v-list-item-title>{{ segment.name }}</v-list-item-title>
                      </v-list-item>
                    </v-list> 
                  </v-menu>
              </div>

              <div v-if="conditionBeingEdited.dimension === 'Seller Average Daily Sales'">

                      <v-text-field
                        v-bind="props"
                        id="from"
                        placeholder=""
                        label="From"
                      >
                      </v-text-field>
                       <v-text-field
                        v-bind="props"
                        id="from"
                        placeholder=""
                        label="To"
                      >
                      </v-text-field>
 
              </div>

              <div v-if="conditionBeingEdited.dimension === 'Buyer Monthly Usage'">
                <h4>Enter Number Range</h4>
              </div>
              <div v-if="conditionBeingEdited.dimension === 'Buyer Last Purchase Value'">
                <h4>Enter Number Range</h4>
              </div>

              <div v-if="conditionBeingEdited.dimension === 'Transaction Amount'">
                <h4>Enter Number Range</h4>
              </div>

              <div v-if="conditionBeingEdited.dimension === 'Transaction Time of Day'">
                <h4>Enter Time of day Range</h4>
              </div>

              <div v-if="conditionBeingEdited.dimension === 'Transaction Day of Week'">
                <div v-if="conditionBeingEdited.operator === 'on' "> 
                  <h4> Select one Day of the Week</h4>
                </div>
                <div v-if="conditionBeingEdited.operator === 'on one of'">
                  <h4> Select Multiple Days of the Week</h4>
                </div>
              </div>


              <!--  v-text-field
                id="conditionValue"
                placeholder="value to evaluate"
                v-model="condition.value"
                label="Condition value"
                required
                @change="onFilterSelected"
              ></v-text-field -->
            </v-col>
            <v-col>
              <v-btn   :disabled="!isConditionBeingEditedValid" rounded type="primary submit" > 
                <v-icon icon="mdi-check-outline" ></v-icon>
              </v-btn>
            </v-col>
            <v-col>
              <v-chip @click="onDeleteConditionClicked(condition,index)"  >
                <v-icon icon="mdi-delete-outline" ></v-icon>
              </v-chip>
            </v-col>
          </v-row>
        </v-container>
      </v-form>
    </v-card>
  </div>
  <div v-else >  <!-- is compoundCondition --> 
    <v-card class="pa-4 ma-4" variant="tonal">
      <h3>If</h3>
      <div v-for="(subCondition, index) in condition.subConditions" :key="index">
        <condition  
          :parentType="condition.operator"  
          @conditionChanged="onSubConditionChanged" 
          :index=index 
          :activeCondition="subCondition"
        ></condition>
        <div v-if="condition.operator == 'one of' && index  < condition.subConditions.length  - 1" >
          <h5>OR</h5>
        </div>
        <div 
          v-if="condition.operator == 'all of'&& index  < condition.subConditions.length  - 1" 
        >
          <h5>AND</h5>
        </div>
      </div>
      <div v-if="condition.operator == 'one of'"  >
        <v-chip @click="onAddSubConditionClicked">
          <h5>OR..</h5>
        </v-chip>
      </div>
      <div v-if="condition.operator == 'all of'" >
        <v-chip @click="onAddSubConditionClicked">
          <h5>AND..</h5>
        </v-chip>
      </div>
    </v-card>
  </div>
</template>

<script>
import { ref,onUpdated,onMounted,onBeforeUpdate,computed } from "vue";
import { useSegmentStore } from "../stores/SegmentStore.mjs";

function CompoundCondition (type,subconditions) {
  this.operator = type;
  this. subConditions =  subconditions;
}

function Condition () {
  this.operator= null;
  this.dimension= null;
  this.value= null;
};


export default {
  inheritAttrs: false,
  //emits: ["conditionChanged(newCondition,index)"],
  props: {
    activeCondition: Object,
    index: Number,
    parentType: String,
  },
  setup(props,{ emit }){

    let condition; 
    let newCondition;
    
    const segmentStore =  useSegmentStore();

    if (props.activeCondition) {
      console.log("setup => ",{props_activeCondition: props.activeCondition});

      condition = ref(props.activeCondition);

      if (condition.value.dimension) {
        console.log("setup => dimension exist ");

        newCondition= ref(false);
      } else {
        console.log("setup => no dimension ");
        newCondition= ref(true);
      }
    } else {
      console.log("setup => no activeCondition");
      condition = ref(new Condition());
      newCondition= ref(true);
    }

    let isEditing = ref (false);
    let conditionBeingEdited = ref (null);
    const simpleOperators = ["equals to", "in", "in range"];

    let isConditionBeingEditedValid = ref(false);


    const dimensions =  ref(
      [ 

        {
          label: "Seller Segment",
          operators: 
          [
            { 
              label: "in",
              values:[], 
            },
          ]
        },
        {
          label: "Seller Location",
          operators: 
          [
            {
              label: "in one of",
              values: [
                "Gauteng,Northen Cape", 
                "Western Cape", 
                "Eastern Cape", 
                "Mapumalanga", 
                "North West", 
                "Kwazulu Natal", 
                "Freestate", 
                "Limpopo"]
            },
            { 
              label: "in",
              values: [
                "Gauteng,Northen Cape", 
                "Western Cape", 
                "Eastern Cape", 
                "Mapumalanga", 
                "North West", 
                "Kwazulu Natal", 
                "Freestate", 
                "Limpopo"]
            },
          ]
        },
        {
          label: "Seller Average Daily Sales",
          operators: 
          [
            {
              label: "more than",
            },
            {
              label: "less than",
            },
            {
              label: "in range",
            },
          ],
        },

        {
          label: "Buyer Monthly Usage",
          operators: 
          [
            {
              label: "more than",
            },
            {
              label: "less than",
            },
            {
              label: "in range",
            },
          ],
        },

        {
          label: "Buyer Last Purchase Value",
          operators: 
          [
            {
              label: "more than",
            },
            {
              label: "less than",
            },
            {
              label: "in range",
            },
          ],
        },

        {
          label: "Transaction Amount",
          operators: 
          [
            {
              label: "more than",
            },
            {
              label: "less than",
            },
            {
              label: "in range",
            },
          ],
        },

        {
          label: "Transaction Time of Day",
          operators: 
          [
            {
              label: "before",
            },
            {
              label: "after",
            },
            {
              label: "between",
            },
          ],
        },


        {
          label: "Transaction Day of Week",
          operators: 
          [
            {
              label: "on",
              values: ["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"],
            } , 
            { 
              label: "on one of",
              values: ["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"],
            },
          ]
        },
      ]);

    const compoundOperators = ["one of", "all of"];


    let errorMessage = ref( null);

    const onEditConditionClicked = () => {
      isEditing.value=true;
      conditionBeingEdited.value = condition.value;
    };

    const dimensionRules = [
      dimension => (!!condition.value.dimension ) || "Please select a dimension",
    ]; 

    const operatorRules = [
      operator => (!!condition.value.operator) || "Please select an operator",
    ];

    const locationRules = [
      location => {
        console.log({condition_value: condition.value.value,} )
        return (!!condition.value.value) || "Please select a location";
      },
    ];

    const segmentRules = [
      segment => (!!condition.value.value) || "Please select a segment",
    ]; 



    const onConditionSubmited= () => {
      console.log("Condition Submitted!");
      condition.value = conditionBeingEdited.value;

      emit('conditionChanged', condition.value, props.index );
      isEditing.value=false;
    };

    const onDimensionSelected= (dimension) => {

      conditionBeingEdited.value.dimension = dimension;
      conditionBeingEdited.value.operator = null;
      conditionBeingEdited.value.value = null;
    };

    const onOperatorSelected = (operator) =>{
      conditionBeingEdited.value.operator = operator;
      conditionBeingEdited.value.value = null;
    };

    const onFilterSelected = (filter) => {
      console.log("filter changed!\n",{filter});
      conditionBeingEdited.value.value = filter;
    }

    const onDeleteConditionClicked = (index) => {
      conditionBeingEdited.value = null;
    }; 


    const onSubConditionChanged = (newCondition, index) => {
      if (newCondition) {
        condition.value.subConditions[index] = newCondition;
      } else {
        condition.value.subConditions.splice(index,1)
      }
      emit('conditionChanged',condition.value,props.index);
    };

    let isSimpleCondition = computed(()=>!condition.value.subConditions);

    let operatorOptions = computed(()=>{
      console.log({condition_dimension: condition.value.dimension});
      console.log({condition_operator: condition.value.operator});


      let dimension = dimensions.value.find((dimension)=>dimension.label === condition.value.dimension); 
      let operator = dimension.operators.find((operator)=>operator.label === condition.value.operator);

      return operator?operator.values:[]; 
    })

    let dimensionOperators = computed(() => {

      console.log({dimensions: dimensions.value});
      console.log({condition_dimension: condition.value.dimension});

      let thisDimension = dimensions.value.find((dimension)=>dimension.label===condition.value.dimension);

      return thisDimension?thisDimension.operators:[];

    });

    let segmentNames = computed(()=>{

      let segmentNames = segmentStore.getSegmentNames;

      console.log({computedSegmentNames :JSON.stringify(segmentNames) });

      return segmentNames;
    });

    const onCombineWithAndClicked = ()=>{
      let currentCondition = condition.value;
      let newSubCondition = new Condition();

      let newCondition = new CompoundCondition ( "all of", [currentCondition,newSubCondition]);
      conditionBeingEdited = newSubCondition;
      condition.value = newCondition;
    }

    const onCombineWithOrClicked = ()=>{

      let currentCondition = condition.value;
      let newCondition = new CompoundCondition ( "one of", [currentCondition,new Condition()]);
      
      condition.value = newCondition;
    }

    const onAddSubConditionClicked = () =>{
      condition.value.subConditions.push(null);
    }

    onMounted(async ()=>{
      console.log("Condition Mounted");
      console.log({activeCondition: props.activeCondition});
      await segmentStore.getSegments();

      let segmentNames = segmentStore.getSegmentNames;

      console.log({computedSegmentNamesOnMounted :JSON.stringify(segmentNames) });


      console.log("segments at start: ",JSON.stringify(segmentStore.segments));
      if(!props.activeCondition){
        isEditing.value=true;
      }
    });

    const parentType = computed(()=>props.parentType);

    return {
      compoundOperators,
      condition,
      conditionBeingEdited,
      dimensionOperators,
      dimensionRules,
      dimensions,
      errorMessage,

      isConditionBeingEditedValid,
      isEditing, 
      isSimpleCondition,

      locationRules,
      newCondition,
      
      onAddSubConditionClicked,
      onCombineWithAndClicked,
      onConditionSubmited,
      onDeleteConditionClicked,
      onDimensionSelected,
      onEditConditionClicked,
      onFilterSelected,
      onOperatorSelected,
      onCombineWithOrClicked,
      onSubConditionChanged,
      
      operatorOptions,
      operatorRules,
      parentType,
      segmentNames, 
      segmentRules,
      simpleOperators,
    };
  },
}

</script>

