<template>
    <div class="d-flex">
        <div class="header-btn-lg pr-0">
            <div class="widget-content p-0">
                <div class="widget-content-wrapper">
                    <div class="widget-content-left">
                        <b-dropdown toggle-class="p-0 mr-2" menu-class="dropdown-menu-lg" variant="link" right>
                            <span slot="button-content">
                                <div class="icon-wrapper h-w-40 icon-wrapper-alt rounded-circle">
                                    <div class="icon-wrapper rounded-circle">
                                        <div class="icon-wrapper-bg opacity-9 bg-success"></div>
                                        <i class="pe-7s-user text-white"></i>
                                    </div>
                                </div>
                            </span>
                            <button type="button" tabindex="0" class="dropdown-item" @click="userLogout">Logout</button>
                        </b-dropdown>
                    </div>
                    <div class="widget-content-left  ml-3 header-user-info">
                        <div class="widget-heading">Web User</div>
                        <div class="widget-subheading">Guest</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { library } from '@fortawesome/fontawesome-svg-core';
import { logout } from '../../../services/Authentication';
import {
    faAngleDown,
    faCalendarAlt,
    faTrashAlt,
    faCheck,
    faFileAlt,
    faCloudDownloadAlt,
    faFileExcel,
    faFilePdf,
    faFileArchive,
    faEllipsisH,
} from '@fortawesome/free-solid-svg-icons'

library.add(
    faAngleDown,
    faCalendarAlt,
    faTrashAlt,
    faCheck,
    faFileAlt,
    faCloudDownloadAlt,
    faFileExcel,
    faFilePdf,
    faFileArchive,
    faEllipsisH,
);

export default {
    components: {
    },
    data: () => ({

    }),

    methods: {
        userLogout() {
            logout()
            this.$router.push("/login")
        }
    }
}


</script>
