<template>
    <div>
        <div class="mb-3 card" v-if="is_error">
            <div class="card-header-tab card-header">
                <div class="card-header-title font-size-lg text-capitalize font-weight-normal">
                    <i class="header-icon lnr-charts icon-gradient bg-happy-green"> </i>
                    {{ error_message }}
                </div>
            </div>
        </div>
        <div class="mb-3 card" v-else>
            <div class="card-header-tab card-header" style="padding-bottom:0;">
                <div class="card-header-title font-size-lg text-capitalize font-weight-normal">
                    <i class="header-icon lnr-charts icon-gradient bg-happy-green"> </i>
                    <h4 style="font-weight: bold">Daily Sales Stats (Fcfa)</h4>
                </div>
            </div>
            <div class="no-gutters row">
                <div class="col-sm-6 col-md-6 col-xl-6">
                    <div class="card no-shadow rm-border bg-transparent widget-chart text-left">
                        <div class="icon-wrapper rounded-circle">
                            <div class="icon-wrapper-bg opacity-10 bg-warning"></div>
                            <i class="pe-7s-phone text-white opacity-8"></i>
                        </div>
                        <div class="widget-chart-content">
                            <div class="widget-subheading">Today's Sales Amount</div>
                            <div class="widget-numbers" v-if="today_sales != 0">{{ `${today_sales}` }}</div>
                            <div class="widget-numbers text-muted-small"
                                style="font-size: 25px; color: gray; margin-top: 15px;" v-else>(no sales)</div>
                        </div>
                    </div>
                    <div class="divider m-0 d-md-none d-sm-block"></div>
                </div>
                <div class="col-sm-6 col-md-6 col-xl-6">
                    <div class="card no-shadow rm-border bg-transparent widget-chart text-left">
                        <div class="icon-wrapper rounded-circle">
                            <div class="icon-wrapper-bg opacity-9 bg-success"></div>
                            <i class="pe-7s-phone text-white"></i>
                        </div>
                        <div class="widget-chart-content">
                            <div class="widget-subheading">Yesterday's Sales Amount</div>
                            <div class="widget-numbers" v-if="yesterday_sales != 0">{{ `${yesterday_sales}` }}</div>
                            <div class="widget-numbers text-muted-small"
                                style="font-size: 25px; color: gray; margin-top: 15px;" v-else>(no sales)</div>
                        </div>
                    </div>
                    <div class="divider m-0 d-md-none d-sm-block"></div>
                </div>
            </div>
        </div>

        <div class="mb-3 card">
            <div class="card-header-tab card-header" style="margin-bottom:15px; padding-bottom:0;">
                <div class="card-header-title font-size-lg text-capitalize font-weight-normal">
                    <i class="header-icon lnr-charts icon-gradient bg-happy-green"> </i>
                    <h4 style="font-weight: bold; margin-bottom: 0px;"> Total Sales (Fcfa)</h4>
                    <p style="font-size:15px;">Gross Retail Sales for the day</p>
                </div>
            </div>
            <div style="padding:15px;">
            <bar></bar>
            </div>
        </div>

        <div class="mb-3 card">
            <div class="card-header-tab card-header">
                <div class="card-header-title font-size-lg text-capitalize font-weight-normal">
                    <i class="header-icon lnr-charts icon-gradient bg-happy-green"> </i>
                    <h4 style="font-weight: bold; margin-bottom: 0px;"> Sample Report</h4>
                    <p style="font-size:15px;">Period From 01 to 27</p>
                </div>
            </div>
            <data-table></data-table>
            <sales-report></sales-report>
        </div>
    </div>
</template>

<script>

import { getSalesAmount } from '../../services/Reports'
import { formattedCurrency } from '../../utils/CurrencyFormat'
import { Config } from '../../config/Config'
import bar from '../../utils/Charts/Chartjs/Bar'
import DataTable from '../../Pages/Elements/Tables/DataTable.vue'
import SalesReport from '../Elements/Tables/SalesReport.vue'


export default {
    components: {
        bar,
        DataTable,
        SalesReport
    },
    data() {
        return {
            today_sales: '',
            yesterday_sales: '',
            is_error: false,
            error_message: ''
        }
    },
    created() {
        this.fetchData();
        setInterval(() => {
            this.fetchData()
        }, Config.REFRESH_INTERVAL);
    },
    methods: {
        async fetchData() {
            try {
                let reports = await getSalesAmount();
                let formattedToday = reports.today_sales == 0 ? reports.today_sales : formattedCurrency(reports.today_sales);
                let formattedYesterday = reports.yesterday_sales == 0 ? reports.yesterday_sales : formattedCurrency(reports.yesterday_sales);
                this.today_sales = formattedToday;
                this.yesterday_sales = formattedYesterday;
            } catch (error) {
                this.yesterday_sales = 0
                this.today_sales = 0
                this.is_error = true
                this.error_message = error
            }
        }
    },

}

</script>
