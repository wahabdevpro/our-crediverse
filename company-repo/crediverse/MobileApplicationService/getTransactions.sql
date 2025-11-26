use hxc;

SELECT 
                no, 
                amount, 
                bonus, 
                type, 
                UNIX_TIMESTAMP(started), 
                UNIX_TIMESTAMP(ended), 
                a_msisdn, 
                b_msisdn, 
                CASE WHEN (a_msisdn = '111111111') THEN a_before ELSE b_before END AS 'before',
                CASE WHEN a_msisdn = '111111111' THEN a_after ELSE b_after END AS after,
                CASE WHEN a_msisdn = '111111111' THEN a_bonus_before ELSE b_bonus_before END AS bonus_before ,
                CASE WHEN a_msisdn = '111111111' THEN a_bonus_after ELSE b_bonus_after END AS bonus_after, 
                CASE WHEN a_msisdn = '111111111' THEN a_hold_before ELSE '0.0000' END AS on_hold_before ,
                CASE WHEN a_msisdn = '111111111' THEN a_hold_after ELSE '0.0000' END AS on_hold_after, 
                follow_up, 
                rolled_back, 
                ret_code, 
                additional 
                FROM ec_transact 
                WHERE 
                comp_id = 2
                AND (a_msisdn = '111111111' OR b_msisdn = '111111111' ) 
                AND (
                    type IN ( 'ST','FR','PA') 
                    OR (type = 'SL' AND a_msisdn = '111111111' ) 
                    OR (type IN ('AD','AJ') AND ret_code = 'SUCCESS') 
                    OR (type = 'RP' AND b_msisdn = '111111111'  AND ret_code = 'SUCCESS') 
                    OR (type IN ('TX','ND','RW','NR','SB') AND ((a_msisdn = '111111111') OR (b_msisdn = '111111111' AND ret_code = 'SUCCESS'))))
                ORDER BY id DESC
                LIMIT 0, 10;





/*
SELECT 
                no, 
                amount, 
                bonus, 
                type, 
                UNIX_TIMESTAMP(started), 
                UNIX_TIMESTAMP(ended), 
                a_msisdn, 
                b_msisdn, 
                CASE WHEN (a_msisdn = '111111111') THEN a_before ELSE b_before END AS 'before', 
                CASE WHEN a_msisdn = '111111111' THEN a_after ELSE b_after END AS after , 
                CASE WHEN a_msisdn = '111111111' THEN a_bonus_before ELSE b_bonus_before END AS bonus_before , 
                CASE WHEN a_msisdn = '111111111' THEN a_bonus_after ELSE b_bonus_after END AS bonus_after, 
                CASE WHEN a_msisdn = '111111111' THEN a_hold_before ELSE '0.0000' END AS on_hold_before ,
                CASE WHEN a_msisdn = '111111111' THEN a_hold_after ELSE '0.0000' END AS on_hold_after, 

                follow_up, 
                rolled_back, 
                ret_code, 
                additional 
 
FROM ec_transact 
WHERE 
-- select all transactions where requester is a or b party 
(a_msisdn = '111111111' OR b_msisdn = '111111111') 
AND (
    -- always show self topup and reversals
    type IN ( 'ST','FR','PA')
    -- show Sales when requester is a party 
    OR (type = 'SL' AND a_msisdn = '111111111')
    -- show adjudications when they are successful
    OR (type IN ('AD','AJ') AND ret_code = 'SUCCESS')
    -- show replenish when the requester is the receiver and it successful.
    OR (type = 'RP' AND b_msisdn = '111111111' AND ret_code = 'SUCCESS')
    -- show transfer, non airtime, sell bundle, and non airtime refund  when the requester is the sender or when the requester is the receiver and it successful.
    OR (type IN ('TX','ND','RW','NR','SB') AND ((a_msisdn = '111111111') OR (b_msisdn = '111111111' AND ret_code = 'SUCCESS')))
); 
*/


/*
 *         
			TRANSACTION_STATUS_ENQUIRY =     "TS";
			SALES_QUERY =                    "SQ";
			DEPOSITS_QUERY =                 "DQ";
			LAST_TRANSACTION_ENQUIRY =       "LT";
			REGISTER_PIN =                   "PR";
			BALANCE_ENQUIRY =                "BE";
			CHANGE_PIN =                     "CP";
			
            SELL =                           "SL";
		
            ADJUDICATE =                     "AD";
			ADJUST =                         "AJ";

            REPLENISH =                      "RP";

			TRANSFER =                       "TX";
			SELL_BUNDLE =                    "SB";
            SELF_TOPUP =                     "ST";
			
            PROMOTION_REWARD =               "RW";
			
            NON_AIRTIME_DEBIT =              "ND";
			NON_AIRTIME_REFUND =             "NR";



			REVERSE =                        "FR";
			REVERSE_PARTIALLY =              "PA";
	*
 * */




