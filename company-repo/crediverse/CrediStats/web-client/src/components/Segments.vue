<template>
  <v-card v-if="errorMessage" title="Error" :text="errorMessage">
  </v-card>
  <v-card v-else variant="outlined"   >
    <v-card-title>Segments</v-card-title>
    <v-table class="w-100">
      <thead>
        <tr>
          <th class="text-left">        
            Segment Name
          </th>
          <th class="text-left">        
            From Filename
          </th>
          <th class="text-left">        
            Time Stamp of Last Upload
          </th>
          <th class="text-left">        
            Segment Size
          </th>
          <th class="text-left">        
            Actions

 
          </th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="(segment, index) in segmentStore.segments"
          :key="segment._id"
        >
          <td>{{ segment.name}}</td>
          <td>{{ segment.filenameUploaded}}</td>
          <td>{{ segment.timeUploaded}}</td>
          <td>{{ segment.msisdns.length}}</td>
          <td>
          <v-btn class="text-primary" density="compact" icon="mdi-download-outline"></v-btn>
          
            <v-menu>
              <template v-slot:activator="{ props }">
                <v-btn 
                  class="text-primary" 
                  density="compact" 
                  icon="mdi-upload-outline"
                  v-bind="props"
                  id="filenameToUploadButton"
                  placeholder="Select file to upload"
                  >
                </v-btn>
              </template>

              <v-card class="pa-4 ma-4" min-width="150" min-height="100" >
                <v-file-input v-model="filenameToUpload" ></v-file-input>
                <v-btn :disabled="!filenameToUpload" class="text-primary">Upload {{filenameToUpload}}</v-btn> 

              <!-- v-list>
                <v-list-item
                  v-for="(dimension, index) in dimensions"
                  :key="index"
                  :value="dimension.label"
                  @click="onDimensionSelected(dimension.label)"
                >
                  <v-list-item-title>{{ dimension.label }}</v-list-item-title>
                </v-list-item>
              </v-list --> 

                </v-card>
            </v-menu>





          <v-btn 
              class="text-error border-danger" 
              density="compact" 
              icon="mdi-delete-outline" 
              @click="onDeleteSegmentClicked(segment)"></v-btn>

          </td>
        </tr>
      </tbody>
    </v-table>
  </v-card>
</template>
<script>
import { ref,onMounted  } from "vue";
import { useSegmentStore } from "../stores/SegmentStore";

export default {
  setup(){
    const displayName = "SegmentsComponent";


    const segmentStore =  useSegmentStore();

    let errorMessage = ref(null);
    
    let filenameToUpload = ref(null)

    onMounted (
      async () => {
        await segmentStore.getSegments();
      }
    );

    const onDeleteSegmentClicked = async (segment) => {
      await segmentStore.deleteSegment(segment._id,segment._rev);
    }

    return {
      errorMessage, 
      segmentStore,
      onDeleteSegmentClicked,
      filenameToUpload,
    }
  }, 
}

</script>
