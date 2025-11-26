<template>
    <div id="app">
        <div class="container mt-4">
            <div class="row">
                <div class="col-md-12">
                    <div class="card">
                        <div class="card-header">
                            Aggregations
                        </div>
                        <div class="card-body">
                            <div class="row">
                                <div class="col-md-6">
                                    <div id="groupings">
                                        <label for="added-groupings">Aggregate rows by</label>
                                        <div id="added-groupings" style="margin-bottom:15px;">
                                            <small v-if="groupings.length === 0">(no aggregations added, use the form below
                                                to add)</small>
                                            <div class="grouping-block" v-for="(grouping, index) in groupings" :key="index"
                                                style="margin-bottom:5px; background-color:#e0e0e0; padding:8px;">
                                                <span class="grouping-label">{{ grouping.label }}</span>
                                                <span class="badge badge-pill badge-primary ml-2"
                                                    v-if="grouping.showSubTotals">sub-totals</span>
                                                <a href="javascript:void(0)" class="ml-2 remove-grouping" style="color:red;"
                                                    @click="removeGrouping(index)">&times;</a>
                                            </div>
                                        </div>
                                        <hr />
                                        <div class="form-group">
                                            <select class="form-control" v-model="selectedRowGrouping">
                                                <option v-for="(option, index) in rowGroupingOptions" :value="option"
                                                    :key="index + 1">{{ option }}
                                                </option>
                                            </select>
                                        </div>
                                        <div style="padding-left:15px;">
                                            <input id="show-row-sub-totals" v-model="showRowSubTotals" type="checkbox" />
                                            <label for="show-row-sub-totals">&nbsp;Show sub-totals</label>
                                        </div>
                                    </div>
                                    <button class="btn btn-primary btn-sm mt-3" @click="addRowGrouping">Add Row
                                        Grouping</button>
                                </div>

                                <div class="col-md-6">
                                    <div id="col-groupings">
                                        <label for="added-groupings">Aggregate columns by</label>
                                        <div id="added-col-groupings" style="margin-bottom:15px;">
                                            <small v-if="colGroupings.length === 0">(no aggregations added, use the form
                                                below to add)</small>
                                            <div class="grouping-block" v-for="(grouping, index) in colGroupings"
                                                :key="index"
                                                style="margin-bottom:5px; background-color:#e0e0e0; padding:8px;">
                                                <span class="grouping-label">{{ grouping.label }}</span>
                                                <span class="badge badge-pill badge-primary ml-2"
                                                    v-if="grouping.showSubTotals">sub-totals</span>
                                                <a href="javascript:void(0)" class="ml-2 remove-grouping" style="color:red;"
                                                    @click="removeColGrouping(index)">&times;</a>
                                            </div>
                                        </div>
                                        <hr />
                                        <div class="form-group">
                                            <select class="form-control" v-model="selectedColGrouping">
                                                <option v-for="(option, index) in colGroupingOptions" :value="option"
                                                    :key="index + 1">{{ option }}
                                                </option>
                                            </select>
                                        </div>
                                        <div>
                                            <input id="show-col-sub-totals" v-model="showColSubTotals" type="checkbox" />
                                            <label for="show-col-sub-totals">&nbsp;Show sub-totals</label>
                                        </div>
                                    </div>
                                    <button class="btn btn-primary btn-sm mt-3" @click="addColGrouping">Add Column
                                        Grouping</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="p-3">
            <div class="w-full">
                <div>
                    <label class="typo__label">Select Months for Comparison</label>
                    <multiselect v-model="selectedMonths" :options="months" :multiple="true" :close-on-select="false"
                        :clear-on-select="false" :preserve-search="true" placeholder="Select Months"
                        :preselect-first="true">
                        <template slot="selection" slot-scope="{ values, isOpen }"><span class="multiselect__single"
                                v-if="values.length" v-show="!isOpen">{{ values.length }} options selected</span></template>
                    </multiselect>
                </div>
            </div>
        </div>
        <div class="container mt-1">
            <div class="row">
                <div class="col-md-8">
                </div>
                <div class="col-md-4 text-right mb-2">
                    <button class="btn btn-primary btn-sm mt-3" @click="generateReport">Generate Report</button>
                </div>
            </div>
        </div>
        <div class="container mt-2" v-if="showTable">

            <div class="table-responsive">
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
                            <th class="w-10" v-for="(grouping, index) in colGroupings"
                                :colspan="grouping.label + '_colspan' || 1" :key="index">
                                {{ grouping.label }}
                            </th>

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
                        <!-- <tr v-for="(rowGroup, mainIndex) in rowSpanData" :key="mainIndex">
                            <td v-for="(grouping, index) in groupings" :rowspan="rowGroup[grouping.label + '_rowspan'] || 1"
                                :key="index">
                                {{ rowGroup[grouping.label] }}
                            </td>
                            <td>{{ rowGroup.sales }}</td>
                        </tr> -->
                        <tr v-for="(rowGroup, mainIndex) in rowSpanData_dummy" :class="rowGroup.class_ || ''"
                            :key="mainIndex">
                            <template v-for="(item, index) in rowGroup.childs">
                                <td v-if="item.nodeName == 'td'" :rowspan="item.rowspan || 1" :colspan="item.colspan || 1"
                                    :scope="item.scope || ''" :class="item.class_ || ''" :key="index">
                                    {{ item.value }}
                                </td>
                                <th v-if="item.nodeName == 'th'" :rowspan="item.rowspan || 1" :colspan="item.colspan || 1"
                                    :scope="item.scope || ''" :class="item.class_ || ''" :key="index">
                                    {{ item.value }}
                                </th>
                            </template>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</template>
<script>
import Multiselect from 'vue-multiselect'
import { getSalesReport } from '../../../services/Reports';

export default {
    components: { Multiselect },
    data() {
        return {
            rowGroupingOptions: [
                "(select row aggregation field to add)",
                "By product (airtime, data bundles)",
                "By Location | Region",
                "By Location | Area",
                "By Location | City",
                "By Location | Suburb",
                "By Channel",
                "By Group",
                "By Retailer Tier",
                "By Owner (Distributor)"
            ],
            colGroupingOptions: [
                "(select column aggregation field to add)",
                "By product (airtime, data bundles)",
                "By Location | Region",
                "By Location | Area",
                "By Location | City",
                "By Location | Suburb",
                "By Channel",
                "By Group",
                "By Retailer Tier",
                "By Owner (Distributor)"
            ],
            selectedRowGrouping: null,
            showRowSubTotals: false,
            selectedColGrouping: null,
            showColSubTotals: false,
            groupings: [],
            colGroupings: [],
            tableData: [
            ],
            showTable: false,
            selectedMonths: [],
            months: [],
            tabs: [
                { title: 'AIRTIME CABIN', content: 'Transfer Cabin' },
                { title: 'CABIN BUNDLES SALE', content: 'Cabin Bundle Sales' },
                { title: 'AIRTIME RETAILER', content: 'Retailer Transfer' },
                { title: 'BUNDLES RETAILER', content: 'Sales Bundles Retailer' },
                { title: 'AIRTIME + BUNDLES', content: 'Performance' },
                { title: 'Full Table', content: '' },
            ],
            rowSpanData_dummy: [
                {
                    "class_": null,
                    "childs": [
                        {
                            "nodeName": "th",
                            "label": "By Location | Region",
                            "value": "ABIDJAN NORD",
                            "rowspan": "4",
                            "colspan": null,
                            "scope": "row",
                            "class_": "rowspan-align"
                        },
                        {
                            "nodeName": "td",
                            "value": "ABOBO",
                            "label": "By Location | Area",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": null
                        },
                        {
                            "nodeName": "td",
                            "value": "DGC",
                            "label": "By Owner (Distributor)",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": null
                        },
                        {
                            "nodeName": "td",
                            "value": "9,264",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "9,190",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "-74",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "-0.80%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "7,391",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "7,353",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "-38",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "-0.51%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "1,909",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "1,908",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "-1",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "-0.05%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "1,349",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "1,393",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "44",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-green"
                        },
                        {
                            "nodeName": "td",
                            "value": "3.26%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-green"
                        },
                        {
                            "nodeName": "td",
                            "value": "11,355",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "11,278",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "-77",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "-0.68%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        }
                    ]
                },
                {
                    "class_": null,
                    "childs": [
                        {
                            "nodeName": "td",
                            "value": "YOPOUGON FICGAYO",
                            "label": "By Location | Area",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": null
                        },
                        {
                            "nodeName": "th",
                            "value": "ONEMART",
                            "label": "By Owner (Distributor)",
                            "rowspan": "2",
                            "colspan": null,
                            "scope": null,
                            "class_": "rowspan-align"
                        },
                        {
                            "nodeName": "td",
                            "value": "6,871",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "6,690",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "-181",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "-2.63%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "4,859",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "4,752",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "-107",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "-2.20%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "1,387",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "1,456",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "69",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-green"
                        },
                        {
                            "nodeName": "td",
                            "value": "4.97%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-green"
                        },
                        {
                            "nodeName": "td",
                            "value": "994",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "1,044",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "50",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-green"
                        },
                        {
                            "nodeName": "td",
                            "value": "5.03%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-green"
                        },
                        {
                            "nodeName": "td",
                            "value": "8,407",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "8,296",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "-111",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "-1.32%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        }
                    ]
                },
                {
                    "class_": null,
                    "childs": [
                        {
                            "nodeName": "td",
                            "value": "YOPOUGON SABLE",
                            "label": "By Location | Area",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": null
                        },
                        {
                            "nodeName": "td",
                            "value": "5,935",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "5,800",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "-135",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "-2.27%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "4,430",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "4,375",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "-55",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "-1.24%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "1,263",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "1,304",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "41",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-green"
                        },
                        {
                            "nodeName": "td",
                            "value": "3.25%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-green"
                        },
                        {
                            "nodeName": "td",
                            "value": "919",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "994",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "75",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-green"
                        },
                        {
                            "nodeName": "td",
                            "value": "8.16%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-green"
                        },
                        {
                            "nodeName": "td",
                            "value": "7,313",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "7,245",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "-68",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "-0.93%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        }
                    ]
                },
                {
                    "class_": "custom-area-total",
                    "childs": [
                        {
                            "nodeName": "td",
                            "value": "TOTAL YOPOUGON",
                            "rowspan": null,
                            "colspan": "2",
                            "scope": null,
                            "class_": null
                        },
                        {
                            "nodeName": "td",
                            "value": "12,806",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "12,490",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "-316",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "-2.47%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "9,289",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "9,127",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "-162",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "-1.74%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "2,650",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "2,760",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "110",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-green"
                        },
                        {
                            "nodeName": "td",
                            "value": "4.15%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-green"
                        },
                        {
                            "nodeName": "td",
                            "value": "1,913",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "2,038",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "125",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-green"
                        },
                        {
                            "nodeName": "td",
                            "value": "6.53%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-green"
                        },
                        {
                            "nodeName": "td",
                            "value": "15,720",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "15,541",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "-179",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "-1.14%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        }
                    ]
                },
                {
                    "class_": "custom-region-total",
                    "childs": [
                        {
                            "nodeName": "td",
                            "value": "Total ABIDJAN NORD",
                            "rowspan": null,
                            "colspan": "3",
                            "scope": null,
                            "class_": null
                        },
                        {
                            "nodeName": "td",
                            "value": "22,070",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "21,680",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "-390",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "-1.77%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "16,680",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "16,480",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "-200",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "-1.20%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "4,559",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "4,668",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "109",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-green"
                        },
                        {
                            "nodeName": "td",
                            "value": "2,39%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-green"
                        },
                        {
                            "nodeName": "td",
                            "value": "3,262",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "3,431",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "169",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-green"
                        },
                        {
                            "nodeName": "td",
                            "value": "5.18%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-green"
                        },
                        {
                            "nodeName": "td",
                            "value": "27,075",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right last-period"
                        },
                        {
                            "nodeName": "td",
                            "value": "26,819",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right"
                        },
                        {
                            "nodeName": "td",
                            "value": "-256",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        },
                        {
                            "nodeName": "td",
                            "value": "-0.95%",
                            "rowspan": null,
                            "colspan": null,
                            "scope": null,
                            "class_": "right custom-bg-red"
                        }
                    ]
                }
            ]
        }
    },

    methods: {
        addRowGrouping() {
            if (this.selectedRowGrouping && this.selectedRowGrouping !== this.rowGroupingOptions[0]) {
                const exists = this.groupings.some(
                    (grouping) =>
                        grouping.label === this.selectedRowGrouping &&
                        grouping.showSubTotals === this.showRowSubTotals
                );

                if (!exists) {
                    this.groupings.push({
                        label: this.selectedRowGrouping,
                        showSubTotals: this.showRowSubTotals
                    });
                }
            }
        },
        addColGrouping() {
            if (this.selectedColGrouping && this.selectedColGrouping !== this.colGroupingOptions[0]) {
                const exists = this.colGroupings.some(
                    (grouping) =>
                        grouping.label === this.selectedColGrouping &&
                        grouping.showSubTotals === this.showColSubTotals
                );

                if (!exists) {
                    this.colGroupings.push({
                        label: this.selectedColGrouping,
                        showSubTotals: this.showColSubTotals
                    });
                }
            }
        },
        removeGrouping(index) {
            this.groupings.splice(index, 1);
        },
        removeColGrouping(index) {
            this.colGroupings.splice(index, 1);
        },
        generateReport() {
            this.showTable = true;
            this.fetchData();

        },
        getMonthName(monthIndex) {
            const monthNames = [
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            ];
            return monthNames[monthIndex];
        },
        async fetchData() {
            try {
            let date = this.selectedMonths;
            let aggregations = this.groupings;
            let result = await getSalesReport(date, aggregations);

            } catch (err) {
                console.log(err);
            }
        }
    },

    computed: {
        rowSpanData() {
            if (this.groupings.length === 0) return [];

            const subTotalLabel = "Sub-Total";

            const rowSpanData = [];
            const groupings = this.groupings.slice();
            const firstGrouping = groupings.shift();

            let subTotalData = {};
            let subTotalCount = 0;

            this.tableData.forEach((rowData) => {
                const newRowData = {};
                newRowData[firstGrouping.label] = rowData[firstGrouping.label];
                newRowData[firstGrouping.label + "_rowspan"] = 1;

                const matchingRow = rowSpanData.find(
                    (row) => row[firstGrouping.label] === rowData[firstGrouping.label]
                );

                if (!matchingRow) {
                    rowSpanData.push(newRowData);
                } else {
                    matchingRow[firstGrouping.label + "_rowspan"]++;
                }

                if (this.groupings.some(grouping => grouping.showSubTotals)) {
                    groupings.forEach(grouping => {
                        if (grouping.showSubTotals) {
                            subTotalData[grouping.label] = (subTotalData[grouping.label] || 0) + rowData.sales;
                        }
                    });
                    subTotalCount++;

                    if (subTotalCount === 2) {
                        const subTotalRow = {};
                        subTotalRow[firstGrouping.label] = subTotalLabel;

                        groupings.forEach(grouping => {
                            if (grouping.showSubTotals) {
                                subTotalRow[grouping.label] = subTotalData[grouping.label];
                            }
                        });

                        rowSpanData.push(subTotalRow);
                        subTotalData = {};
                        subTotalCount = 0;
                    }
                }
            });
            return rowSpanData;
        },
        colSpanData() {
            if (this.colGroupings.length === 0) return [];

            const subTotalLabel = "Sub-Total";

            const colSpanData = [];
            const colGroupings = this.colGroupings.slice();
            const firstColGrouping = colGroupings.shift();

            let subTotalData = {};
            let subTotalCount = 0;

            this.tableData.forEach((rowData) => {
                const newColData = {};
                newColData[firstColGrouping.label] = rowData[firstColGrouping.label];
                newColData[firstColGrouping.label + "_colspan"] = 1;

                const matchingCol = colSpanData.find(
                    (col) => col[firstColGrouping.label] === rowData[firstColGrouping.label]
                );

                if (!matchingCol) {
                    colSpanData.push(newColData);
                } else {
                    matchingCol[firstColGrouping.label + "_colspan"]++;
                }

                if (this.colGroupings.some(grouping => grouping.showSubTotals)) {
                    colGroupings.forEach(grouping => {
                        if (grouping.showSubTotals) {
                            subTotalData[grouping.label] = (subTotalData[grouping.label] || 0) + rowData.sales;
                        }
                    });
                    subTotalCount++;
                    if (subTotalCount === 2) {
                        const subTotalCol = {};
                        subTotalCol[firstColGrouping.label] = subTotalLabel;

                        colGroupings.forEach(grouping => {
                            if (grouping.showSubTotals) {
                                subTotalCol[grouping.label] = subTotalData[grouping.label];
                            }
                        });

                        colSpanData.push(subTotalCol);
                        subTotalData = {};
                        subTotalCount = 0;
                    }
                }
            });

            return colSpanData;
        },
    },

    mounted() {
        this.selectedRowGrouping = this.rowGroupingOptions[0];
        this.selectedColGrouping = this.colGroupingOptions[0];
        const currentMonth = new Date().getMonth();
        for (let i = 0; i <= currentMonth; i++) {
            this.months.push(this.getMonthName(i));
        }
        this.selectedMonths = [this.getMonthName(currentMonth), this.getMonthName(currentMonth - 1)];
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

<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>