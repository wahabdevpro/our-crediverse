<template>
  <div class="mt-2">
    <div class="pb-4">
      <ul class="nav nav-tabs pb-2">
        <li class="nav-item" v-for="(tab, index) in tabs" :key="index">
          <a :class="['nav-link', { active: activeTab === index }]" @click="activeTab = index">
            {{ tab.title }}
          </a>
        </li>
      </ul>
    </div>
    <div class="p-3">
      <table v-if="activeTab == 0" class="table  table-default">
        <thead class="mdb-color darken-3">
          <tr>
            <th class="w-10">Region</th>
            <th class="w-10">Agency Area</th>
            <th class="w-10">Distrib.</th>
            <th class="right">{{ tabs[activeTab].content }} M-1</th>
            <th class="right">{{ tabs[activeTab].content }} M</th>
            <th class="right">Change in Value</th>
            <th class="right">Change in %age</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <th scope="row" rowspan="4" class="rowspan-align">ABIDJAN NORD</th>
            <td>ABOBO</td>
            <td>DGC</td>
            <td class="right last-period">9,264</td>
            <td class="right">9,190</td>
            <td class="right custom-bg-red">-74</td>
            <td class="right custom-bg-red">-0.80%</td>
          </tr>
          <tr>
            <td>YOPOUGON FICGAYO</td>
            <th class="rowspan-align" rowspan="2">ONEMART</th>
            <td class="right last-period">6,871</td>
            <td class="right">6,690</td>
            <td class="right custom-bg-red">-181</td>
            <td class="right custom-bg-red">-2.63%</td>
          </tr>
          <tr>
            <td>YOPOUGON SABLE</td>
            <td class="right last-period">5,935</td>
            <td class="right">5,800</td>
            <td class="right custom-bg-red">-135</td>
            <td class="right custom-bg-red">-2.27%</td>
          </tr>
          <tr class="custom-area-total">
            <td colspan="2">TOTAL YOPOUGON</td>
            <td class="right last-period">12,806</td>
            <td class="right">12,490</td>
            <td class="right custom-bg-red">-316</td>
            <td class="right custom-bg-red">-2.47%</td>
          </tr>
          <tr class="custom-region-total">
            <td colspan="3">Total ABIDJAN NORD</td>
            <td class="right last-period">22,070</td>
            <td class="right">21,680</td>
            <td class="right custom-bg-red">-390</td>
            <td class="right custom-bg-red">-1.77%</td>
          </tr>
        </tbody>
      </table>

      <table v-if="activeTab == 1" class="table  table-default">
        <thead class="mdb-color darken-3">
          <tr>
            <th class="w-10">Region</th>
            <th class="w-10">Agency Area</th>
            <th class="w-10">Distrib.</th>
            <th class="right">{{ tabs[activeTab].content }} M-1</th>
            <th class="right">{{ tabs[activeTab].content }} M</th>
            <th class="right">Change in Value</th>
            <th class="right">Change in %age</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <th scope="row" rowspan="4" class="rowspan-align">ABIDJAN NORD</th>
            <td>ABOBO</td>
            <td>DGC</td>
            <td class="right last-period">7,391</td>
            <td class="right">7,353</td>
            <td class="right custom-bg-red">-38</td>
            <td class="right custom-bg-red">-0.51%</td>
          </tr>
          <tr>
            <td>YOPOUGON FICGAYO</td>
            <th class="rowspan-align" rowspan="2">ONEMART</th>
            <td class="right last-period">4,859</td>
            <td class="right">4,752</td>
            <td class="right custom-bg-red">-107</td>
            <td class="right custom-bg-red">-2.20%</td>
          </tr>
          <tr>
            <td>YOPOUGON SABLE</td>
            <td class="right last-period">4,430</td>
            <td class="right">4,375</td>
            <td class="right custom-bg-red">-55</td>
            <td class="right custom-bg-red">-1.24%</td>
          </tr>
          <tr class="custom-area-total">
            <td colspan="2">TOTAL YOPOUGON</td>
            <td class="right last-period">9,289</td>
            <td class="right">9,127</td>
            <td class="right custom-bg-red">-162</td>
            <td class="right custom-bg-red">-1.74%</td>
          </tr>
          <tr class="custom-region-total">
            <td colspan="3">Total ABIDJAN NORD</td>
            <td class="right last-period">16,680</td>
            <td class="right">16,480</td>
            <td class="right custom-bg-red">-200</td>
            <td class="right custom-bg-red">-1.20%</td>
          </tr>
        </tbody>
      </table>

      <table v-if="activeTab == 2" class="table  table-default">
        <thead class="mdb-color darken-3">
          <tr>
            <th class="w-10">Region</th>
            <th class="w-10">Agency Area</th>
            <th class="w-10">Distrib.</th>
            <th class="right">{{ tabs[activeTab].content }} M-1</th>
            <th class="right">{{ tabs[activeTab].content }} M</th>
            <th class="right">Change in Value</th>
            <th class="right">Change in %age</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <th scope="row" rowspan="4" class="rowspan-align">ABIDJAN NORD</th>
            <td>ABOBO</td>
            <td>DGC</td>
            <td class="right last-period">1,909</td>
            <td class="right">1,908</td>
            <td class="right custom-bg-red">-1</td>
            <td class="right custom-bg-red">-0.05%</td>
          </tr>
          <tr>
            <td>YOPOUGON FICGAYO</td>
            <th class="rowspan-align" rowspan="2">ONEMART</th>
            <td class="right last-period">1,387</td>
            <td class="right">1,456</td>
            <td class="right custom-bg-green">69</td>
            <td class="right custom-bg-green">4.97%</td>
          </tr>
          <tr>
            <td>YOPOUGON SABLE</td>
            <td class="right last-period">1,263</td>
            <td class="right">1,304</td>
            <td class="right custom-bg-green">41</td>
            <td class="right custom-bg-green">3.25%</td>
          </tr>
          <tr class="custom-area-total">
            <td colspan="2">TOTAL YOPOUGON</td>
            <td class="right last-period">2,650</td>
            <td class="right">2,760</td>
            <td class="right custom-bg-green">110</td>
            <td class="right custom-bg-green">4.15%</td>
          </tr>
          <tr class="custom-region-total">
            <td colspan="3">Total ABIDJAN NORD</td>
            <td class="right last-period">4,559</td>
            <td class="right">4,668</td>
            <td class="right custom-bg-green">109</td>
            <td class="right custom-bg-green">2,39%</td>
          </tr>
        </tbody>
      </table>

      <table v-if="activeTab == 3" class="table  table-default">
        <thead class="mdb-color darken-3">
          <tr>
            <th class="w-10">Region</th>
            <th class="w-10">Agency Area</th>
            <th class="w-10">Distrib.</th>
            <th class="right">{{ tabs[activeTab].content }} M-1</th>
            <th class="right">{{ tabs[activeTab].content }} M</th>
            <th class="right">Change in Value</th>
            <th class="right">Change in %age</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <th scope="row" rowspan="4" class="rowspan-align">ABIDJAN NORD</th>
            <td>ABOBO</td>
            <td>DGC</td>
            <td class="right last-period">1,349</td>
            <td class="right">1,393</td>
            <td class="right custom-bg-green">44</td>
            <td class="right custom-bg-green">3.26%</td>
          </tr>
          <tr>
            <td>YOPOUGON FICGAYO</td>
            <th class="rowspan-align" rowspan="2">ONEMART</th>
            <td class="right last-period">994</td>
            <td class="right">1,044</td>
            <td class="right custom-bg-green">50</td>
            <td class="right custom-bg-green">5.03%</td>
          </tr>
          <tr>
            <td>YOPOUGON SABLE</td>
            <td class="right last-period">919</td>
            <td class="right">994</td>
            <td class="right custom-bg-green">75</td>
            <td class="right custom-bg-green">8.16%</td>
          </tr>
          <tr class="custom-area-total">
            <td colspan="2">TOTAL YOPOUGON</td>
            <td class="right last-period">1,913</td>
            <td class="right">2,038</td>
            <td class="right custom-bg-green">125</td>
            <td class="right custom-bg-green">6.53%</td>
          </tr>
          <tr class="custom-region-total">
            <td colspan="3">Total ABIDJAN NORD</td>
            <td class="right last-period">3,262</td>
            <td class="right">3,431</td>
            <td class="right custom-bg-green">169</td>
            <td class="right custom-bg-green">5.18%</td>
          </tr>
        </tbody>
      </table>

      <table v-if="activeTab == 4" class="table  table-default">
        <thead class="mdb-color darken-3">
          <tr>
            <th class="w-10">Region</th>
            <th class="w-10">Agency Area</th>
            <th class="w-10">Distrib.</th>
            <th class="right">{{ tabs[activeTab].content }} M-1</th>
            <th class="right">{{ tabs[activeTab].content }} M</th>
            <th class="right">Change in Value</th>
            <th class="right">Change in %age</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <th scope="row" rowspan="4" class="rowspan-align">ABIDJAN NORD</th>
            <td>ABOBO</td>
            <td>DGC</td>
            <td class="right last-period">11,355</td>
            <td class="right">11,278</td>
            <td class="right custom-bg-red">-77</td>
            <td class="right custom-bg-red">-0.68%</td>
          </tr>
          <tr>
            <td>YOPOUGON FICGAYO</td>
            <th class="rowspan-align" rowspan="2">ONEMART</th>
            <td class="right last-period">8,407</td>
            <td class="right">8,296</td>
            <td class="right custom-bg-red">-111</td>
            <td class="right custom-bg-red">-1.32%</td>
          </tr>
          <tr>
            <td>YOPOUGON SABLE</td>
            <td class="right last-period">7,313</td>
            <td class="right">7,245</td>
            <td class="right custom-bg-red">-68</td>
            <td class="right custom-bg-red">-0.93%</td>
          </tr>
          <tr class="custom-area-total">
            <td colspan="2">TOTAL YOPOUGON</td>
            <td class="right last-period">15,720</td>
            <td class="right">15,541</td>
            <td class="right custom-bg-red">-179</td>
            <td class="right custom-bg-red">-1.14%</td>
          </tr>
          <tr class="custom-region-total">
            <td colspan="3">Total ABIDJAN NORD</td>
            <td class="right last-period">27,075</td>
            <td class="right">26,819</td>
            <td class="right custom-bg-red">-256</td>
            <td class="right custom-bg-red">-0.95%</td>
          </tr>
        </tbody>
      </table>

      <div v-if="activeTab == 5" class="table-responsive">
        <table class="table table-sctoll table-full">
          <thead class="mdb-color darken-3">
            <tr class="darken-4">
              <th class="text-center" colspan="3">
              </th>
              <th class="text-center border-lft-right-5" colspan="4">
                {{ tabs[0].title }}
              </th>
              <th class="text-center border-lft-right-5" colspan="4">
                {{ tabs[1].title }}
              </th>

              <th class="text-center border-lft-right-5" colspan="4">
                {{ tabs[2].title }}
              </th>

              <th class="text-center border-lft-right-5" colspan="4">
                {{ tabs[3].title }}
              </th>

              <th class="text-center border-lft-right-5" colspan="4">
                {{ tabs[4].title }}
              </th>

            </tr>
            <tr class="darken-3">
              <th class="w-10">Region</th>
              <th class="w-10">Agency Area</th>
              <th class="w-10">Distrib.</th>
              <th class="right">{{ tabs[0].content }} M-1</th>
              <th class="right">{{ tabs[0].content }} M</th>
              <th class="right">Change in Value</th>
              <th class="right">Change in %age</th>

              <th class="right extra-width">{{ tabs[1].content }} M-1</th>
              <th class="right extra-width">{{ tabs[1].content }} M</th>
              <th class="right">Change in Value</th>
              <th class="right">Change in %age</th>

              <th class="right extra-width">{{ tabs[2].content }} M-1</th>
              <th class="right extra-width">{{ tabs[2].content }} M</th>
              <th class="right">Change in Value</th>
              <th class="right">Change in %age</th>

              <th class="right extra-width">{{ tabs[3].content }} M-1</th>
              <th class="right extra-width">{{ tabs[3].content }} M</th>
              <th class="right">Change in Value</th>
              <th class="right">Change in %age</th>

              <th class="right">{{ tabs[4].content }} M-1</th>
              <th class="right">{{ tabs[4].content }} M</th>
              <th class="right">Change in Value</th>
              <th class="right">Change in %age</th>
            </tr>
          </thead>
          <tbody>


            <tr>
              <th scope="row" rowspan="4" class="rowspan-align">ABIDJAN NORD</th>
              <td>ABOBO</td>
              <td>DGC</td>
              <td class="right last-period">9,264</td>
              <td class="right">9,190</td>
              <td class="right custom-bg-red">-74</td>
              <td class="right custom-bg-red">-0.80%</td>

              <td class="right last-period">7,391</td>
              <td class="right">7,353</td>
              <td class="right custom-bg-red">-38</td>
              <td class="right custom-bg-red">-0.51%</td>

              <td class="right last-period">1,909</td>
              <td class="right">1,908</td>
              <td class="right custom-bg-red">-1</td>
              <td class="right custom-bg-red">-0.05%</td>

              <td class="right last-period">1,349</td>
              <td class="right">1,393</td>
              <td class="right custom-bg-green">44</td>
              <td class="right custom-bg-green">3.26%</td>

              <td class="right last-period">11,355</td>
              <td class="right">11,278</td>
              <td class="right custom-bg-red">-77</td>
              <td class="right custom-bg-red">-0.68%</td>
            </tr>
            <tr>
              <td>YOPOUGON FICGAYO</td>
              <th class="rowspan-align" rowspan="2">ONEMART</th>
              <td class="right last-period">6,871</td>
              <td class="right">6,690</td>
              <td class="right custom-bg-red">-181</td>
              <td class="right custom-bg-red">-2.63%</td>

              <td class="right last-period">4,859</td>
              <td class="right">4,752</td>
              <td class="right custom-bg-red">-107</td>
              <td class="right custom-bg-red">-2.20%</td>

              <td class="right last-period">1,387</td>
              <td class="right">1,456</td>
              <td class="right custom-bg-green">69</td>
              <td class="right custom-bg-green">4.97%</td>

              <td class="right last-period">994</td>
              <td class="right">1,044</td>
              <td class="right custom-bg-green">50</td>
              <td class="right custom-bg-green">5.03%</td>

              <td class="right last-period">8,407</td>
              <td class="right">8,296</td>
              <td class="right custom-bg-red">-111</td>
              <td class="right custom-bg-red">-1.32%</td>
            </tr>
            <tr>
              <td>YOPOUGON SABLE</td>
              <td class="right last-period">5,935</td>
              <td class="right">5,800</td>
              <td class="right custom-bg-red">-135</td>
              <td class="right custom-bg-red">-2.27%</td>

              <td class="right last-period">4,430</td>
              <td class="right">4,375</td>
              <td class="right custom-bg-red">-55</td>
              <td class="right custom-bg-red">-1.24%</td>

              <td class="right last-period">1,263</td>
              <td class="right">1,304</td>
              <td class="right custom-bg-green">41</td>
              <td class="right custom-bg-green">3.25%</td>

              <td class="right last-period">919</td>
              <td class="right">994</td>
              <td class="right custom-bg-green">75</td>
              <td class="right custom-bg-green">8.16%</td>

              <td class="right last-period">7,313</td>
              <td class="right">7,245</td>
              <td class="right custom-bg-red">-68</td>
              <td class="right custom-bg-red">-0.93%</td>
            </tr>
            <tr class="custom-area-total">
              <td colspan="2">TOTAL YOPOUGON</td>
              <td class="right last-period">12,806</td>
              <td class="right">12,490</td>
              <td class="right custom-bg-red">-316</td>
              <td class="right custom-bg-red">-2.47%</td>

              <td class="right last-period">9,289</td>
              <td class="right">9,127</td>
              <td class="right custom-bg-red">-162</td>
              <td class="right custom-bg-red">-1.74%</td>

              <td class="right last-period">2,650</td>
              <td class="right">2,760</td>
              <td class="right custom-bg-green">110</td>
              <td class="right custom-bg-green">4.15%</td>

              <td class="right last-period">1,913</td>
              <td class="right">2,038</td>
              <td class="right custom-bg-green">125</td>
              <td class="right custom-bg-green">6.53%</td>

              <td class="right last-period">15,720</td>
              <td class="right">15,541</td>
              <td class="right custom-bg-red">-179</td>
              <td class="right custom-bg-red">-1.14%</td>
            </tr>
            <tr class="custom-region-total">
              <td colspan="3">Total ABIDJAN NORD</td>
              <td class="right last-period">22,070</td>
              <td class="right">21,680</td>
              <td class="right custom-bg-red">-390</td>
              <td class="right custom-bg-red">-1.77%</td>

              <td class="right last-period">16,680</td>
              <td class="right">16,480</td>
              <td class="right custom-bg-red">-200</td>
              <td class="right custom-bg-red">-1.20%</td>

              <td class="right last-period">4,559</td>
              <td class="right">4,668</td>
              <td class="right custom-bg-green">109</td>
              <td class="right custom-bg-green">2,39%</td>

              <td class="right last-period">3,262</td>
              <td class="right">3,431</td>
              <td class="right custom-bg-green">169</td>
              <td class="right custom-bg-green">5.18%</td>

              <td class="right last-period">27,075</td>
              <td class="right">26,819</td>
              <td class="right custom-bg-red">-256</td>
              <td class="right custom-bg-red">-0.95%</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
<script>

export default {
  data() {
    return {
      activeTab: 0,
      tabs: [
        { title: 'AIRTIME CABIN', content: 'Transfer Cabin' },
        { title: 'CABIN BUNDLES SALE', content: 'Cabin Bundle Sales' },
        { title: 'AIRTIME RETAILER', content: 'Retailer Transfer' },
        { title: 'BUNDLES RETAILER', content: 'Sales Bundles Retailer' },
        { title: 'AIRTIME + BUNDLES', content: 'Performance' },
        { title: 'Full Table', content: '' },
      ],
      tableData: [
        {
          level1Column1: 'Data 1',
          level1Column2: 'Data 2',
          level2Column1Row1: 'Data 3',
          level2Column2Row1: 'Data 4',
          level2Column1Row2: 'Data 5',
          level2Column2Row2: 'Data 6',
          level2Column1Row3: 'Data 7',
          level2Column2Row3: 'Data 8',
        },
      ],
    };
  },

  mounted() {
    this.capitalizeText();
  },

  methods: {

    capitalizeText() {
      const tds = document.querySelectorAll('td');
      tds.forEach(td => {
        td.textContent = this.capitalizeInitials(td.textContent);
      });
    },

    capitalizeInitials(text) {
      return text.replace(/\b\w/g, char => char.toUpperCase());
    },

    generateRandom(option) {
      /*
      'n' => Number between 0 and 5000
      'p' => percentage between -50 to 100
      'v' => small values from -50 to 400
      */
      if (option === "n") {
        return Math.floor(Math.random() * 5000) + 1;
      } else if (option === "p") {
        let randomNumber = Math.floor(Math.random() * 99);
        let isNegative = Math.random() < 0.5;
        if (isNegative) {
          randomNumber *= -1;
        }
        return randomNumber - 50;
      } else if (option === "v") {
        let randomNumber = Math.floor(Math.random() * 400);
        let isNegative = Math.random() < 0.5;
        if (isNegative) {
          randomNumber *= -1;
        }
        return randomNumber - 50;
      } else {
        return Math.floor(Math.random() * 5000) + 1;
      }

    },

    calculateSum(columnIndex, rowIndex) {
      let sum = 0;
      try {
        const rows = document.querySelectorAll('table tr');
        for (let i = 1; i < rowIndex; i++) {
          const cells = rows[i].querySelectorAll('td');
          const cellValue = parseInt(cells[columnIndex].textContent);
          if (!isNaN(cellValue)) {
            sum += cellValue;
          }
        }
      } catch (ex) {
        return 0;
      }
      return sum;
    }
  }
};
</script>
  
<style scoped>
.darken-3 {
  background-color: rgba(0, 0, 0, .075);
}

.darken-4 {
  background-color: rgba(0, 0, 0, .15);
}

.table thead th:not(:nth-child(-n+3)) {
  /*text-align: left;*/
  line-height: 3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.table thead th:nth-child(-n+3) {
  min-width: 160px;
  /*text-align: left;*/
  line-height: 3;
}

.border-lft-right-5 {
  border-left: 5px solid white;
}

.table td {
  /* text-align: left;*/
  line-height: 3;
}

.w-10 {
  width: 160px !important;
}

.custom-bg-green {
  background-color: #55d91112;
}

.custom-bg-red {
  background-color: #FFCCCB;
  color: red;
}

.nav-tabs .nav-link {
  border: none;
  cursor: pointer;
}

.nav-tabs .nav-link.active {
  position: relative;
  font-weight: bolder;
}

.nav-tabs .nav-link.active:before {
  content: '';
  position: absolute;
  bottom: -10px;
  width: 75%;
  height: 4px;
  background-color: #56CC9D;
  left: 50%;
  transform: translateX(-50%);
}

.vertical-align {
  vertical-align: middle;
}

.table-responsive {
  width: 100%;
  overflow-x: auto;
}

.custom-region-total {
  font-weight: bold;
  background-color: #e0e0e0;
}

.custom-area-total {
  font-weight: bold;
  background-color: #f0f0f0;
}

.right {
  text-align: right;
}

.last-period {
  color: #999999;
}

.rowspan-align {
  vertical-align: top;
  line-height: 3;
}
</style>
