import { Bar } from "vue-chartjs";

export default {
    extends: Bar,
    data() {
        return {
            colors: {
                green: { color: "#56cc9d", label: "Today > Yesterday" },
                red: { color: "#ff4c4c", label: "Today < Yesterday" },
                orange: { color: "#faa918", label: "Current Hour" },
                gray: { color: "#cfcfcf", label: "Yesterday's Sales" },
            },
        };
    },
    mounted() {
        this.renderChart(
            {
                labels: this.fetchChartData().labels,
                datasets: this.fetchChartData().datasets,
            },
            {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    xAxes: [
                        {
                            ticks: {
                                beginAtZero: true,
                                stepSize: 1,
                                max: 23,
                            },
                            barPercentage: 0.7,
                            categoryPercentage: 0.5,
                            gridLines: {
                                display: false,
                            },
                        },
                    ],
                    yAxes: [
                        {
                            barPercentage: 0.5,
                            gridLines: {
                                display: false,
                            },
                        },
                    ],
                },
                legend: {
                    display: true,
                    labels: {
                        color: "rgb(255, 99, 132)",
                        generateLabels: (chart) => {
                            var data = chart.data;
                            if (data.labels.length && data.datasets.length) {
                                return Object.entries(this.colors).map(
                                    ([key, value]) => {
                                        let color = { ...value };
                                        return {
                                            text: color.label,
                                            fillStyle: color.color,
                                            lineCap: "round",
                                            lineWidth: 0,
                                        };
                                    }
                                );
                            }
                            return [];
                        },
                    },
                    position: "right",
                },
            }
        );
    },
    methods: {
        fetchChartData() {
            const now = new Date();
            const currentHour = now.getHours();

            const todayData = [
                849954, 187800, 63850, 26950, 99100, 301429, 4390649, 14145123,
                24444552, 27277284, 29755881, 21747684, 20442872, 19913596,
                18090835, 18215129, 19376840, 28387928, 32797145, 35797155,
                34797155, 24797155, 11797155, 2997155,
            ];
            const yesterdayData = [
                809954, 167800, 66850, 28950, 85100, 251429, 4090649, 15145123,
                23444552, 26277284, 25755881, 22747684, 19442872, 16913596,
                17090835, 17215129, 20376840, 26387928, 29797145, 34797155,
                32797155, 25797155, 12797155, 2497155,
            ];

            for (var h = currentHour + 1; h < 24; ++h) todayData[h] = 0;

            const labels = Array.from({ length: 24 }, (_, i) => i);

            const datasets = [
                {
                    label: "Yesterday's Record",
                    data: yesterdayData,
                    backgroundColor: [],
                    barPercentage: 0.1,
                    borderCapStyle: "round",
                },
                {
                    label: "Today's Record",
                    data: todayData,
                    backgroundColor: [],
                    barPercentage: 0.1,
                    borderCapStyle: "round",
                },
            ];

            for (let i = 0; i < 24; i++) {
                const todayRecord = todayData[i];
                datasets[0].data.push(todayRecord);

                if (i === currentHour) {
                    datasets[0].backgroundColor.push(this.colors.gray.color);
                    datasets[1].backgroundColor.push(this.colors.orange.color);
                } else if (todayRecord > yesterdayData[i]) {
                    datasets[0].backgroundColor.push(this.colors.gray.color);
                    datasets[1].backgroundColor.push(this.colors.green.color);
                } else {
                    datasets[0].backgroundColor.push(this.colors.gray.color);
                    datasets[1].backgroundColor.push(this.colors.red.color);
                }
            }

            return {
                labels: labels,
                datasets: datasets,
            };
        },
    },
};
