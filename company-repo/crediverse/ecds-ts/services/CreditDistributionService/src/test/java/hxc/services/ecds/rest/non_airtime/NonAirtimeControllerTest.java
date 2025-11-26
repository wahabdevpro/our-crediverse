package hxc.services.ecds.rest.non_airtime;


import hxc.ecds.protocol.rest.non_airtime.DebitRequest;
import hxc.ecds.protocol.rest.non_airtime.RefundRequest;
import hxc.ecds.protocol.rest.non_airtime.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;

import static hxc.ecds.protocol.rest.ResponseHeader.RETURN_CODE_SUCCESS;
import static hxc.ecds.protocol.rest.config.TransactionsConfig.ERR_TIMED_OUT;
import static hxc.services.ecds.rest.non_airtime.CommonService.REQUEST_IS_STALE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NonAirtimeControllerTest {
    @Mock
    DebitService debitService;
    
    @Mock
    RefundService refundService;

    @Spy
    @InjectMocks
    NonAirtimeController nonAirtimeController = new NonAirtimeController();
    
    @Test
    public void debitShouldExecuteWhenRequestIsNotExpired() {
        /* Given */
        DebitRequest request = new DebitRequest();
        request.setExpiryTimeInMillisecondsSinceUnixEpoch(new Date().getTime() + 1000);
        request.setMsisdn("");
        request.setClientTransactionId("");
        Response successfulResponse = new Response(request);
        successfulResponse.setReturnCode(RETURN_CODE_SUCCESS);
        when(debitService.execute(any())).thenReturn(successfulResponse);
        doNothing().when(nonAirtimeController).checkPermission(any(), any());
        
        /* When */
        Response response = nonAirtimeController.debit("", request, null);
        
        /* Then */
        verify(debitService).execute(any());
        assertEquals(RETURN_CODE_SUCCESS, response.getReturnCode());
        assertNull(response.getResponse());
    }

    @Test
    public void debitShouldNotExecuteWhenRequestIsExpired() {
        /* Given */
        DebitRequest request = new DebitRequest();
        request.setExpiryTimeInMillisecondsSinceUnixEpoch(new Date().getTime() - 1000);
        request.setMsisdn("");
        request.setClientTransactionId("");
        doNothing().when(nonAirtimeController).checkPermission(any(), any());
        
        /* When */
        Response response = nonAirtimeController.debit("", request, null);
        
        /* Then */
        verify(debitService, never()).execute(any());
        assertEquals(ERR_TIMED_OUT, response.getReturnCode());
        assertEquals(REQUEST_IS_STALE, response.getResponse());
    }
    
    @Test
    public void refundShouldExecuteWhenRequestIsNotExpired() {
        /* Given */
        RefundRequest request = new RefundRequest();
        request.setExpiryTimeInMillisecondsSinceUnixEpoch(new Date().getTime() + 1000);
        request.setMsisdn("");
        request.setClientTransactionId("");
        Response successfulResponse = new Response(request);
        successfulResponse.setReturnCode(RETURN_CODE_SUCCESS);
        when(refundService.execute(any())).thenReturn(successfulResponse);
        doNothing().when(nonAirtimeController).checkPermission(any(), any());
        
        /* When */
        Response response = nonAirtimeController.refund("", request, null);
        
        /* Then */
        verify(refundService).execute(any());
        assertEquals(RETURN_CODE_SUCCESS, response.getReturnCode());
        assertNull(response.getResponse());
    }

    @Test
    public void refundShouldNotExecuteWhenRequestIsExpired() {
        /* Given */
        RefundRequest request = new RefundRequest();
        request.setExpiryTimeInMillisecondsSinceUnixEpoch(new Date().getTime() - 1000);
        request.setMsisdn("");
        request.setClientTransactionId("");
        doNothing().when(nonAirtimeController).checkPermission(any(), any());
        
        /* When */
        Response response = nonAirtimeController.refund("", request, null);
        
        /* Then */
        verify(refundService, never()).execute(any());
        assertEquals(ERR_TIMED_OUT, response.getReturnCode());
        assertEquals(REQUEST_IS_STALE, response.getResponse());
    }
}