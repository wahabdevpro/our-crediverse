
package systems.concurrent.ssim_api;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the systems.concurrent.ssim_api package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _UnsubscribeResponse_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "unsubscribeResponse");
    private final static QName _StartAutoLifeCycleEventsResponse_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "startAutoLifeCycleEventsResponse");
    private final static QName _FaultResponse_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "faultResponse");
    private final static QName _PurchaseBundleResponse_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "purchaseBundleResponse");
    private final static QName _GetServiceNamesRequest_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "getServiceNamesRequest");
    private final static QName _ActivateRequest_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "activateRequest");
    private final static QName _TopUpBenefitsResponse_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "topUpBenefitsResponse");
    private final static QName _DeactivateRequest_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "deactivateRequest");
    private final static QName _CustomiseBundleSuggestionResponse_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "customiseBundleSuggestionResponse");
    private final static QName _MigrateRequest_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "migrateRequest");
    private final static QName _CustomiseBundleSuggestionRequest_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "customiseBundleSuggestionRequest");
    private final static QName _ResponseHeader_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "responseHeader");
    private final static QName _UnsubscribeRequest_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "unsubscribeRequest");
    private final static QName _GetServiceInstancesRequest_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "getServiceInstancesRequest");
    private final static QName _PurchaseBundleRequest_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "purchaseBundleRequest");
    private final static QName _RequestHeader_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "requestHeader");
    private final static QName _GetBundleSuggestionsRequest_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "getBundleSuggestionsRequest");
    private final static QName _MigrateResponse_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "migrateResponse");
    private final static QName _ExtendValidityRequest_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "extendValidityRequest");
    private final static QName _StartAutoLifeCycleEventsRequest_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "startAutoLifeCycleEventsRequest");
    private final static QName _GetServiceInstancesResponse_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "getServiceInstancesResponse");
    private final static QName _GetBundleSuggestionsResponse_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "getBundleSuggestionsResponse");
    private final static QName _SubscribeResponse_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "subscribeResponse");
    private final static QName _SubscribeRequest_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "subscribeRequest");
    private final static QName _ActivateResponse_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "activateResponse");
    private final static QName _DeactivateResponse_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "deactivateResponse");
    private final static QName _ExtendValidityResponse_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "extendValidityResponse");
    private final static QName _StopAutoLifeCycleEventsResponse_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "stopAutoLifeCycleEventsResponse");
    private final static QName _StopAutoLifeCycleEventsRequest_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "stopAutoLifeCycleEventsRequest");
    private final static QName _GetServiceNamesResponse_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "getServiceNamesResponse");
    private final static QName _TopUpBenefitsRequest_QNAME = new QName("urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", "topUpBenefitsRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: systems.concurrent.ssim_api
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PurchaseBundleRequest }
     * 
     */
    public PurchaseBundleRequest createPurchaseBundleRequest() {
        return new PurchaseBundleRequest();
    }

    /**
     * Create an instance of {@link ResponseHeader }
     * 
     */
    public ResponseHeader createResponseHeader() {
        return new ResponseHeader();
    }

    /**
     * Create an instance of {@link UnsubscribeRequest }
     * 
     */
    public UnsubscribeRequest createUnsubscribeRequest() {
        return new UnsubscribeRequest();
    }

    /**
     * Create an instance of {@link GetServiceInstancesRequest }
     * 
     */
    public GetServiceInstancesRequest createGetServiceInstancesRequest() {
        return new GetServiceInstancesRequest();
    }

    /**
     * Create an instance of {@link TopUpBenefitsResponse }
     * 
     */
    public TopUpBenefitsResponse createTopUpBenefitsResponse() {
        return new TopUpBenefitsResponse();
    }

    /**
     * Create an instance of {@link DeactivateRequest }
     * 
     */
    public DeactivateRequest createDeactivateRequest() {
        return new DeactivateRequest();
    }

    /**
     * Create an instance of {@link CustomiseBundleSuggestionResponse }
     * 
     */
    public CustomiseBundleSuggestionResponse createCustomiseBundleSuggestionResponse() {
        return new CustomiseBundleSuggestionResponse();
    }

    /**
     * Create an instance of {@link MigrateRequest }
     * 
     */
    public MigrateRequest createMigrateRequest() {
        return new MigrateRequest();
    }

    /**
     * Create an instance of {@link CustomiseBundleSuggestionRequest }
     * 
     */
    public CustomiseBundleSuggestionRequest createCustomiseBundleSuggestionRequest() {
        return new CustomiseBundleSuggestionRequest();
    }

    /**
     * Create an instance of {@link UnsubscribeResponse }
     * 
     */
    public UnsubscribeResponse createUnsubscribeResponse() {
        return new UnsubscribeResponse();
    }

    /**
     * Create an instance of {@link StartAutoLifeCycleEventsResponse }
     * 
     */
    public StartAutoLifeCycleEventsResponse createStartAutoLifeCycleEventsResponse() {
        return new StartAutoLifeCycleEventsResponse();
    }

    /**
     * Create an instance of {@link FaultResponse }
     * 
     */
    public FaultResponse createFaultResponse() {
        return new FaultResponse();
    }

    /**
     * Create an instance of {@link PurchaseBundleResponse }
     * 
     */
    public PurchaseBundleResponse createPurchaseBundleResponse() {
        return new PurchaseBundleResponse();
    }

    /**
     * Create an instance of {@link GetServiceNamesRequest }
     * 
     */
    public GetServiceNamesRequest createGetServiceNamesRequest() {
        return new GetServiceNamesRequest();
    }

    /**
     * Create an instance of {@link ActivateRequest }
     * 
     */
    public ActivateRequest createActivateRequest() {
        return new ActivateRequest();
    }

    /**
     * Create an instance of {@link StopAutoLifeCycleEventsResponse }
     * 
     */
    public StopAutoLifeCycleEventsResponse createStopAutoLifeCycleEventsResponse() {
        return new StopAutoLifeCycleEventsResponse();
    }

    /**
     * Create an instance of {@link StopAutoLifeCycleEventsRequest }
     * 
     */
    public StopAutoLifeCycleEventsRequest createStopAutoLifeCycleEventsRequest() {
        return new StopAutoLifeCycleEventsRequest();
    }

    /**
     * Create an instance of {@link GetServiceNamesResponse }
     * 
     */
    public GetServiceNamesResponse createGetServiceNamesResponse() {
        return new GetServiceNamesResponse();
    }

    /**
     * Create an instance of {@link TopUpBenefitsRequest }
     * 
     */
    public TopUpBenefitsRequest createTopUpBenefitsRequest() {
        return new TopUpBenefitsRequest();
    }

    /**
     * Create an instance of {@link SubscribeRequest }
     * 
     */
    public SubscribeRequest createSubscribeRequest() {
        return new SubscribeRequest();
    }

    /**
     * Create an instance of {@link ActivateResponse }
     * 
     */
    public ActivateResponse createActivateResponse() {
        return new ActivateResponse();
    }

    /**
     * Create an instance of {@link DeactivateResponse }
     * 
     */
    public DeactivateResponse createDeactivateResponse() {
        return new DeactivateResponse();
    }

    /**
     * Create an instance of {@link ExtendValidityResponse }
     * 
     */
    public ExtendValidityResponse createExtendValidityResponse() {
        return new ExtendValidityResponse();
    }

    /**
     * Create an instance of {@link GetServiceInstancesResponse }
     * 
     */
    public GetServiceInstancesResponse createGetServiceInstancesResponse() {
        return new GetServiceInstancesResponse();
    }

    /**
     * Create an instance of {@link GetBundleSuggestionsResponse }
     * 
     */
    public GetBundleSuggestionsResponse createGetBundleSuggestionsResponse() {
        return new GetBundleSuggestionsResponse();
    }

    /**
     * Create an instance of {@link SubscribeResponse }
     * 
     */
    public SubscribeResponse createSubscribeResponse() {
        return new SubscribeResponse();
    }

    /**
     * Create an instance of {@link RequestHeader }
     * 
     */
    public RequestHeader createRequestHeader() {
        return new RequestHeader();
    }

    /**
     * Create an instance of {@link GetBundleSuggestionsRequest }
     * 
     */
    public GetBundleSuggestionsRequest createGetBundleSuggestionsRequest() {
        return new GetBundleSuggestionsRequest();
    }

    /**
     * Create an instance of {@link MigrateResponse }
     * 
     */
    public MigrateResponse createMigrateResponse() {
        return new MigrateResponse();
    }

    /**
     * Create an instance of {@link ExtendValidityRequest }
     * 
     */
    public ExtendValidityRequest createExtendValidityRequest() {
        return new ExtendValidityRequest();
    }

    /**
     * Create an instance of {@link StartAutoLifeCycleEventsRequest }
     * 
     */
    public StartAutoLifeCycleEventsRequest createStartAutoLifeCycleEventsRequest() {
        return new StartAutoLifeCycleEventsRequest();
    }

    /**
     * Create an instance of {@link ServiceInstance }
     * 
     */
    public ServiceInstance createServiceInstance() {
        return new ServiceInstance();
    }

    /**
     * Create an instance of {@link NillableCompoundServiceInstanceId }
     * 
     */
    public NillableCompoundServiceInstanceId createNillableCompoundServiceInstanceId() {
        return new NillableCompoundServiceInstanceId();
    }

    /**
     * Create an instance of {@link Disallowed }
     * 
     */
    public Disallowed createDisallowed() {
        return new Disallowed();
    }

    /**
     * Create an instance of {@link DisallowedReason }
     * 
     */
    public DisallowedReason createDisallowedReason() {
        return new DisallowedReason();
    }

    /**
     * Create an instance of {@link CompoundServiceDefinitionIdSequence }
     * 
     */
    public CompoundServiceDefinitionIdSequence createCompoundServiceDefinitionIdSequence() {
        return new CompoundServiceDefinitionIdSequence();
    }

    /**
     * Create an instance of {@link CompoundServiceDefinitionId }
     * 
     */
    public CompoundServiceDefinitionId createCompoundServiceDefinitionId() {
        return new CompoundServiceDefinitionId();
    }

    /**
     * Create an instance of {@link Tags }
     * 
     */
    public Tags createTags() {
        return new Tags();
    }

    /**
     * Create an instance of {@link PartialCompoundServiceInstanceId }
     * 
     */
    public PartialCompoundServiceInstanceId createPartialCompoundServiceInstanceId() {
        return new PartialCompoundServiceInstanceId();
    }

    /**
     * Create an instance of {@link CompoundServiceInstanceId }
     * 
     */
    public CompoundServiceInstanceId createCompoundServiceInstanceId() {
        return new CompoundServiceInstanceId();
    }

    /**
     * Create an instance of {@link Method }
     * 
     */
    public Method createMethod() {
        return new Method();
    }

    /**
     * Create an instance of {@link DisallowedReasons }
     * 
     */
    public DisallowedReasons createDisallowedReasons() {
        return new DisallowedReasons();
    }

    /**
     * Create an instance of {@link Tag }
     * 
     */
    public Tag createTag() {
        return new Tag();
    }

    /**
     * Create an instance of {@link Methods }
     * 
     */
    public Methods createMethods() {
        return new Methods();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnsubscribeResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "unsubscribeResponse")
    public JAXBElement<UnsubscribeResponse> createUnsubscribeResponse(UnsubscribeResponse value) {
        return new JAXBElement<UnsubscribeResponse>(_UnsubscribeResponse_QNAME, UnsubscribeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StartAutoLifeCycleEventsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "startAutoLifeCycleEventsResponse")
    public JAXBElement<StartAutoLifeCycleEventsResponse> createStartAutoLifeCycleEventsResponse(StartAutoLifeCycleEventsResponse value) {
        return new JAXBElement<StartAutoLifeCycleEventsResponse>(_StartAutoLifeCycleEventsResponse_QNAME, StartAutoLifeCycleEventsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FaultResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "faultResponse")
    public JAXBElement<FaultResponse> createFaultResponse(FaultResponse value) {
        return new JAXBElement<FaultResponse>(_FaultResponse_QNAME, FaultResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PurchaseBundleResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "purchaseBundleResponse")
    public JAXBElement<PurchaseBundleResponse> createPurchaseBundleResponse(PurchaseBundleResponse value) {
        return new JAXBElement<PurchaseBundleResponse>(_PurchaseBundleResponse_QNAME, PurchaseBundleResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetServiceNamesRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "getServiceNamesRequest")
    public JAXBElement<GetServiceNamesRequest> createGetServiceNamesRequest(GetServiceNamesRequest value) {
        return new JAXBElement<GetServiceNamesRequest>(_GetServiceNamesRequest_QNAME, GetServiceNamesRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActivateRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "activateRequest")
    public JAXBElement<ActivateRequest> createActivateRequest(ActivateRequest value) {
        return new JAXBElement<ActivateRequest>(_ActivateRequest_QNAME, ActivateRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TopUpBenefitsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "topUpBenefitsResponse")
    public JAXBElement<TopUpBenefitsResponse> createTopUpBenefitsResponse(TopUpBenefitsResponse value) {
        return new JAXBElement<TopUpBenefitsResponse>(_TopUpBenefitsResponse_QNAME, TopUpBenefitsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeactivateRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "deactivateRequest")
    public JAXBElement<DeactivateRequest> createDeactivateRequest(DeactivateRequest value) {
        return new JAXBElement<DeactivateRequest>(_DeactivateRequest_QNAME, DeactivateRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CustomiseBundleSuggestionResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "customiseBundleSuggestionResponse")
    public JAXBElement<CustomiseBundleSuggestionResponse> createCustomiseBundleSuggestionResponse(CustomiseBundleSuggestionResponse value) {
        return new JAXBElement<CustomiseBundleSuggestionResponse>(_CustomiseBundleSuggestionResponse_QNAME, CustomiseBundleSuggestionResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MigrateRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "migrateRequest")
    public JAXBElement<MigrateRequest> createMigrateRequest(MigrateRequest value) {
        return new JAXBElement<MigrateRequest>(_MigrateRequest_QNAME, MigrateRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CustomiseBundleSuggestionRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "customiseBundleSuggestionRequest")
    public JAXBElement<CustomiseBundleSuggestionRequest> createCustomiseBundleSuggestionRequest(CustomiseBundleSuggestionRequest value) {
        return new JAXBElement<CustomiseBundleSuggestionRequest>(_CustomiseBundleSuggestionRequest_QNAME, CustomiseBundleSuggestionRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResponseHeader }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "responseHeader")
    public JAXBElement<ResponseHeader> createResponseHeader(ResponseHeader value) {
        return new JAXBElement<ResponseHeader>(_ResponseHeader_QNAME, ResponseHeader.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnsubscribeRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "unsubscribeRequest")
    public JAXBElement<UnsubscribeRequest> createUnsubscribeRequest(UnsubscribeRequest value) {
        return new JAXBElement<UnsubscribeRequest>(_UnsubscribeRequest_QNAME, UnsubscribeRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetServiceInstancesRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "getServiceInstancesRequest")
    public JAXBElement<GetServiceInstancesRequest> createGetServiceInstancesRequest(GetServiceInstancesRequest value) {
        return new JAXBElement<GetServiceInstancesRequest>(_GetServiceInstancesRequest_QNAME, GetServiceInstancesRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PurchaseBundleRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "purchaseBundleRequest")
    public JAXBElement<PurchaseBundleRequest> createPurchaseBundleRequest(PurchaseBundleRequest value) {
        return new JAXBElement<PurchaseBundleRequest>(_PurchaseBundleRequest_QNAME, PurchaseBundleRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestHeader }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "requestHeader")
    public JAXBElement<RequestHeader> createRequestHeader(RequestHeader value) {
        return new JAXBElement<RequestHeader>(_RequestHeader_QNAME, RequestHeader.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetBundleSuggestionsRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "getBundleSuggestionsRequest")
    public JAXBElement<GetBundleSuggestionsRequest> createGetBundleSuggestionsRequest(GetBundleSuggestionsRequest value) {
        return new JAXBElement<GetBundleSuggestionsRequest>(_GetBundleSuggestionsRequest_QNAME, GetBundleSuggestionsRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MigrateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "migrateResponse")
    public JAXBElement<MigrateResponse> createMigrateResponse(MigrateResponse value) {
        return new JAXBElement<MigrateResponse>(_MigrateResponse_QNAME, MigrateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExtendValidityRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "extendValidityRequest")
    public JAXBElement<ExtendValidityRequest> createExtendValidityRequest(ExtendValidityRequest value) {
        return new JAXBElement<ExtendValidityRequest>(_ExtendValidityRequest_QNAME, ExtendValidityRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StartAutoLifeCycleEventsRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "startAutoLifeCycleEventsRequest")
    public JAXBElement<StartAutoLifeCycleEventsRequest> createStartAutoLifeCycleEventsRequest(StartAutoLifeCycleEventsRequest value) {
        return new JAXBElement<StartAutoLifeCycleEventsRequest>(_StartAutoLifeCycleEventsRequest_QNAME, StartAutoLifeCycleEventsRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetServiceInstancesResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "getServiceInstancesResponse")
    public JAXBElement<GetServiceInstancesResponse> createGetServiceInstancesResponse(GetServiceInstancesResponse value) {
        return new JAXBElement<GetServiceInstancesResponse>(_GetServiceInstancesResponse_QNAME, GetServiceInstancesResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetBundleSuggestionsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "getBundleSuggestionsResponse")
    public JAXBElement<GetBundleSuggestionsResponse> createGetBundleSuggestionsResponse(GetBundleSuggestionsResponse value) {
        return new JAXBElement<GetBundleSuggestionsResponse>(_GetBundleSuggestionsResponse_QNAME, GetBundleSuggestionsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubscribeResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "subscribeResponse")
    public JAXBElement<SubscribeResponse> createSubscribeResponse(SubscribeResponse value) {
        return new JAXBElement<SubscribeResponse>(_SubscribeResponse_QNAME, SubscribeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubscribeRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "subscribeRequest")
    public JAXBElement<SubscribeRequest> createSubscribeRequest(SubscribeRequest value) {
        return new JAXBElement<SubscribeRequest>(_SubscribeRequest_QNAME, SubscribeRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActivateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "activateResponse")
    public JAXBElement<ActivateResponse> createActivateResponse(ActivateResponse value) {
        return new JAXBElement<ActivateResponse>(_ActivateResponse_QNAME, ActivateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeactivateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "deactivateResponse")
    public JAXBElement<DeactivateResponse> createDeactivateResponse(DeactivateResponse value) {
        return new JAXBElement<DeactivateResponse>(_DeactivateResponse_QNAME, DeactivateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExtendValidityResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "extendValidityResponse")
    public JAXBElement<ExtendValidityResponse> createExtendValidityResponse(ExtendValidityResponse value) {
        return new JAXBElement<ExtendValidityResponse>(_ExtendValidityResponse_QNAME, ExtendValidityResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StopAutoLifeCycleEventsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "stopAutoLifeCycleEventsResponse")
    public JAXBElement<StopAutoLifeCycleEventsResponse> createStopAutoLifeCycleEventsResponse(StopAutoLifeCycleEventsResponse value) {
        return new JAXBElement<StopAutoLifeCycleEventsResponse>(_StopAutoLifeCycleEventsResponse_QNAME, StopAutoLifeCycleEventsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StopAutoLifeCycleEventsRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "stopAutoLifeCycleEventsRequest")
    public JAXBElement<StopAutoLifeCycleEventsRequest> createStopAutoLifeCycleEventsRequest(StopAutoLifeCycleEventsRequest value) {
        return new JAXBElement<StopAutoLifeCycleEventsRequest>(_StopAutoLifeCycleEventsRequest_QNAME, StopAutoLifeCycleEventsRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetServiceNamesResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "getServiceNamesResponse")
    public JAXBElement<GetServiceNamesResponse> createGetServiceNamesResponse(GetServiceNamesResponse value) {
        return new JAXBElement<GetServiceNamesResponse>(_GetServiceNamesResponse_QNAME, GetServiceNamesResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TopUpBenefitsRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:concurrent-systems:ssim-api:1.0:wsdl:1.0", name = "topUpBenefitsRequest")
    public JAXBElement<TopUpBenefitsRequest> createTopUpBenefitsRequest(TopUpBenefitsRequest value) {
        return new JAXBElement<TopUpBenefitsRequest>(_TopUpBenefitsRequest_QNAME, TopUpBenefitsRequest.class, null, value);
    }

}
