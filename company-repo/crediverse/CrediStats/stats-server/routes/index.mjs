import express from 'express';
import { getReport, getSalesAmount } from '../controllers/DashboardController/Dashboard.mjs';
import { getCampaigns, deleteCampaign , updateOrCreateCampaign} from '../controllers/CampaignManager/Campaigns.mjs';
import { getSegments,deleteSegment } from '../controllers/SegmentManager/Segments.mjs';
import { login } from '../controllers/LoginController/Login.mjs';

import {verifyToken} from '../verifyToken.mjs';

const router = express.Router();

router.get('/campaign_manager/campaigns', getCampaigns);
router.delete('/campaign_manager/campaigns/:id/:rev', deleteCampaign);
router.post('/campaign_manager/campaigns/',updateOrCreateCampaign);
router.get('/credistats/daily_sales', verifyToken, getSalesAmount);
router.get('/credistats/report', verifyToken, getReport);
router.post('/credistats/login', login);
router.delete('/segment_manager/segment/:id/:rev', deleteSegment);
router.get('/segment_manager/segments', getSegments);

export default router;
