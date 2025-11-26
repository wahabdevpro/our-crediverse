use crate::mas::masapi::{
    AccountInfo, AgentId, BuyWithMobileMoneyRequest, ChangePinRequest, FeedBackRequest,
    FeedBackResponse, GetStockBalanceRequest, GetTransactionsRequest, IsTeamLead, LoginRequest,
    LoginResponse, MobileMoneyBalance, Msisdn, NoParam, Ok, SalesSummaryRequest,
    SalesSummaryResponse, SalesSummaryValue, SellAirtimeRequest, SellAirtimeResponse, StockBalance,
    Transaction, Transactions, TransferRequest, UpdateProfileRequest, UpdateTokenResponse,
    MobileMoneyLoginRequest, MobileMoneyLoginResponse,
};

impl std::fmt::Display for Msisdn {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "Msisdn {{")?;
        writeln!(f, "  msisdn: {}", self.msisdn)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for LoginRequest {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "LoginRequest {{")?;
        writeln!(f, "  msisdn: {}", self.msisdn)?;
        writeln!(f, "  username: {}", self.username)?;
        writeln!(f, "  password: {}", self.password)?;
        writeln!(f, "  pin: {}", self.pin)?;
        writeln!(f, "  oneTimePin: {}", self.one_time_pin)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for LoginResponse {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "LoginResponse {{")?;
        writeln!(f, "  agentId: {}", self.agent_id)?;
        writeln!(f, "  agentMsisdn: {}", self.agent_msisdn)?;
        writeln!(f, "  loginToken: {}", self.login_token)?;
        writeln!(f, "  refreshToken: {}", self.refresh_token)?;
        writeln!(
            f,
            "  authenticationStatus: {:?}",
            self.authentication_status
        )?;
        writeln!(f, "  message: {}", self.message)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for GetTransactionsRequest {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "GetTransactionsRequest {{")?;
        writeln!(f, "  startPage: {}", self.start_page)?;
        writeln!(f, "  transactionsPerPage: {}", self.transactions_per_page)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for SalesSummaryRequest {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "SalesSummaryRequest {{")?;
        writeln!(f, "  startTime: {}", self.start_time)?;
        writeln!(f, "  endTime: {}", self.end_time)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for SalesSummaryResponse {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "SalesSummaryResponse {{")?;
        writeln!(f, "  responseTime: {}", self.response_time)?;
        writeln!(f, "  startTime: {}", self.start_time)?;
        writeln!(f, "  endTime: {}", self.end_time)?;
        writeln!(
            f,
            "  salesSummary: {}",
            match self.sales_summary.clone() {
                Some(sales_summary) => sales_summary.to_string(),
                None => "".to_string(),
            }
        )?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for SalesSummaryValue {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "SalesSummaryValue {{")?;
        writeln!(f, "  airtimeSalesValue: {}", self.airtime_sales_value)?;
        writeln!(f, "  airtimeSalesCount: {}", self.airtime_sales_count)?;
        writeln!(f, "  bundleSalesValue: {}", self.bundle_sales_value)?;
        writeln!(f, "  bundleSalesCount: {}", self.bundle_sales_count)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for UpdateTokenResponse {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "UpdateTokenResponse {{")?;
        writeln!(f, "  loginToken: {}", self.login_token)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for Transactions {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "Transactions {{")?;
        for (i, transaction) in self.transactions.iter().enumerate() {
            writeln!(f, "  Transaction {}:", i + 1)?;
            writeln!(f, "    transactionNo: {}", transaction.transaction_no)?;
            writeln!(f, "    amount: {}", transaction.amount)?;
            writeln!(f, "    bonus: {}", transaction.bonus)?;
            writeln!(f, "    transactionType: {:?}", transaction.transaction_type)?;
            writeln!(
                f,
                "    transactionStarted: {}",
                transaction.transaction_started
            )?;
            writeln!(f, "    transactionEnded: {}", transaction.transaction_ended)?;
            writeln!(f, "    sourceMsisdn: {}", transaction.source_msisdn)?;
            writeln!(f, "    recipientMsisdn: {}", transaction.recipient_msisdn)?;
            writeln!(f, "    balanceBefore: {}", transaction.balance_before)?;
            writeln!(
                f,
                "    bonusBalanceBefore: {}",
                transaction.bonus_balance_before
            )?;
            writeln!(
                f,
                "    onHoldBalanceBefore: {}",
                transaction.on_hold_balance_before
            )?;
            writeln!(f, "    balanceAfter: {}", transaction.balance_after)?;
            writeln!(
                f,
                "    bonusBalanceAfter: {}",
                transaction.bonus_balance_after
            )?;
            writeln!(
                f,
                "    onHoldBalanceAfter: {}",
                transaction.on_hold_balance_after
            )?;
            writeln!(f, "    status: {}", transaction.status)?;
            writeln!(
                f,
                "    followUpRequired: {}",
                transaction.follow_up_required
            )?;
            writeln!(f, "    rolledBack: {}", transaction.rolled_back)?;
            writeln!(f, "    messages: {:?}", transaction.messages)?;
            write!(
                f,
                "    item_description: {:?}\n",
                transaction.item_description
            )?;
            writeln!(
                f,
                "    grossSalesAmount: {:?}",
                transaction.gross_sales_amount
            )?;
            writeln!(
                f,
                "    commissionAmount: {:?}",
                transaction.commission_amount
            )?;
        }
        write!(f, "}}")
    }
}

impl std::fmt::Display for Transaction {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "Transaction {{")?;
        writeln!(f, "  transactionNo: {}", self.transaction_no)?;
        writeln!(f, "  amount: {}", self.amount)?;
        writeln!(f, "  bonus: {}", self.bonus)?;
        writeln!(f, "  transactionType: {:?}", self.transaction_type)?;
        writeln!(f, "  transactionStarted: {}", self.transaction_started)?;
        writeln!(f, "  transactionEnded: {}", self.transaction_ended)?;
        writeln!(f, "  sourceMsisdn: {}", self.source_msisdn)?;
        writeln!(f, "  recipientMsisdn: {}", self.recipient_msisdn)?;
        writeln!(f, "  balanceBefore: {}", self.balance_before)?;
        writeln!(f, "  bonusBalanceBefore: {}", self.bonus_balance_before)?;
        writeln!(f, "  onHoldBalanceBefore: {}", self.on_hold_balance_before)?;
        writeln!(f, "  balanceAfter: {}", self.balance_after)?;
        writeln!(f, "  bonusBalanceAfter: {}", self.bonus_balance_after)?;
        writeln!(f, "  onHoldBalanceAfter: {}", self.on_hold_balance_after)?;
        writeln!(f, "  status: {}", self.status)?;
        writeln!(f, "  followUpRequired: {}", self.follow_up_required)?;
        writeln!(f, "  rolledBack: {}", self.rolled_back)?;
        writeln!(f, "  messages: {:?}", self.messages)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for SellAirtimeResponse {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "SellAirtimeResponse {{")?;
        writeln!(f, "  followUpRequired: {}", self.follow_up_required)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for StockBalance {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "StockBalance{{")?;
        writeln!(f, "  balance: {}", self.balance)?;
        writeln!(f, "  bonusBalance: {}", self.bonus_balance)?;
        writeln!(f, "  onHoldBalance: {}", self.on_hold_balance)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for SellAirtimeRequest {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "SellAirtimeRequest {{")?;
        writeln!(f, "  amount: {}", self.amount)?;
        writeln!(f, "  msisdn: {}", self.msisdn)?;
        writeln!(
            f,
            "  latitude: {}",
            self.latitude
                .as_ref()
                .map_or("None".to_string(), |v| v.to_string())
        )?;
        writeln!(
            f,
            "  longitude: {}",
            self.longitude
                .as_ref()
                .map_or("None".to_string(), |v| v.to_string())
        )?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for BuyWithMobileMoneyRequest {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "BuyWithMobileMoneyRequest {{")?;
        writeln!(f, "  mobileMoneyAmount: {}", self.mobile_money_amount)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for IsTeamLead {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "IsTeamLead {{")?;
        writeln!(f, "  isTeamLead : {}", self.is_team_lead)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for GetStockBalanceRequest {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "GetStockBalanceRequest {{")?;
        writeln!(
            f,
            "  msisdn : {}",
            self.msisdn.clone().unwrap_or("".to_string())
        )?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for FeedBackRequest {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "FeedBackRequest {{")?;
        writeln!(f, "  name: {}", self.name)?;
        writeln!(f, "  agentId: {}", self.agent_id)?;
        writeln!(f, "  tier: {}", self.tier)?;
        writeln!(f, "  feedBackRequestMsg: {}", self.feed_back_request_msg)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for MobileMoneyBalance {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "MobileMoneyBalance {{")?;
        writeln!(f, "  mobileMoneyBalance: {}", self.mobile_money_balance)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for FeedBackResponse {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "FeedBackResponse {{}}")
    }
}

impl std::fmt::Display for TransferRequest {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "TransferRequest {{")?;
        writeln!(f, "  amount: {}", self.amount)?;
        writeln!(f, "  msisdn: {}", self.msisdn)?;
        writeln!(
            f,
            "  latitude: {}",
            self.latitude
                .as_ref()
                .map_or("None".to_string(), |v| v.to_string())
        )?;
        writeln!(
            f,
            "  longitude: {}",
            self.longitude
                .as_ref()
                .map_or("None".to_string(), |v| v.to_string())
        )?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for AgentId {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "AgentId {{")?;
        writeln!(f, "  agentId: {}", self.agent_id)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for AccountInfo {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "AccountInfo {{")?;
        writeln!(f, "  accountNumber: {}", self.account_number)?;
        writeln!(f, "  msisdn: {}", self.msisdn)?;
        writeln!(f, "  title: {}", self.title)?;
        writeln!(f, "  firstName: {}", self.first_name)?;
        writeln!(f, "  initials: {}", self.initials)?;
        writeln!(f, "  surname: {}", self.surname)?;
        writeln!(f, "  language: {}", self.language)?;
        writeln!(f, "  altPhoneNumber: {}", self.alt_phone_number)?;
        writeln!(f, "  email: {}", self.email)?;
        writeln!(f, "  state: {}", self.state)?;
        writeln!(f, "  activationDate: {}", self.activation_date)?;
        writeln!(f, "  countryCode: {}", self.country_code)?;
        writeln!(f, "  tier: {}", self.tier)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for ChangePinRequest {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "ChangePinRequest {{")?;
        writeln!(f, "  newPin: {}", self.new_pin)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for Ok {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "Ok {{}}")
    }
}

impl std::fmt::Display for NoParam {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "NoParam {{}}")
    }
}

impl std::fmt::Display for UpdateProfileRequest {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "UpdateProfileRequest {{")?;
        writeln!(f, "  title: {}", self.title)?;
        writeln!(f, "  language: {}", self.language)?;
        writeln!(f, "  firstName: {}", self.first_name)?;
        writeln!(f, "  surname: {}", self.surname)?;
        writeln!(f, "  email: {}", self.email)?;
        writeln!(f, "  agentId: {}", self.agent_id)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for MobileMoneyLoginRequest {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "LoginRequest {{")?;
        writeln!(f, "  username: {}", self.username)?;
        writeln!(f, "  password: {}", self.password)?;
        write!(f, "}}")
    }
}

impl std::fmt::Display for MobileMoneyLoginResponse {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        writeln!(f, "LoginResponse {{")?;
        writeln!(f, "  loginToken: {}", self.login_token)?;
        write!(f, "}}")
    }
}
