import {defineStore} from "pinia";

export const useSegmentStore = defineStore(
  "SegmentStore",
  { 
    state: () => ({ 
      segments:[ ],
    }),
    getters: {
      getSegmentNames() {
        return this.segments.map( segment => {
          return ({
            _id: segment._id, 
            _rev: segment._rev,
            name: segment.name,
          });
        });
      },
    },

    actions: {
      async getSegments() {
        const segmentsResponse = await fetch(
          "http://localhost:8801/segment_manager/segments", 
          {
            method: "GET",
            headers: { "Content-Type": "application/json" },
            headers: {
              "Content-Type": "application/json",
              Accept: "application/json",
            }
          }
        );
        this.segments = await segmentsResponse.json();
      },
      async deleteSegment(id,rev) {
        try {
          let deleteSegmentResponse = await fetch(
            "http://localhost:8801/segment_manager/segments/"+id+"/"+rev, 
            {
              method: "DELETE",
            });

          this.segments = this.segments.filter(segment => {
            return !(segment._id === id && segment._rev === rev);
          });
        } catch(deleteError) {
          console.log(deleteError);
          this.segments = [];
          throw deleteError;
        } 
      },
    },
  }
);

