package hxc.utils.protocol.ucip;

import hxc.connectors.air.Air;

/**
 * AccumulatorSelection
 * 
 * The accumulatorSelection parameter is used to select which usage accumulators that will be returned. If no accumulator IDs are specified in the request all installed usage accumulators are
 * returned. The request contains first and last identities for a sequence of usage accumulators. If a single accumulator shall be returned, accumulatorIDFirst could be used alone, or the same
 * identity could be used for both accumulatorIDFirst and accumulatorIDLast. Overlapping sequences is allowed, the response will only contain one instance per accumulator. Structs are placed in an
 * <array> with maximum 255 entries. Note: Explicit requests use accumulatorIDFirst alone and if the requested accumulator does not exist response code 127 will be returned. For an implicit request
 * which uses a range of accumulators between accumulatorIDFirst and accumulatorIDLast the response code 127 will not be returned if no accumulators are found in the range (even if the same identity
 * is used for both accumulatorIDFirst and accumulatorIDLast). An explicit request overrides an implicit request. Example: 1, 2 and 3 are accumulator IDs in the example. For the three first examples
 * two structs are used in the array, the first an explicit request with accumulatorIDFirst = 1 and the second an implicit request with accumulatorIDFirst = 2 and accumulatorIDLast = 3. In the last
 * example one struct is used with an implicit request with accumulatorIDFirst = 1 and accumulatorIDLast = 3. 1,2-3: 1 does not exist -> not ok, response code 127 and no accumulators are returned
 * 1,2-3: 3 does not exist -> ok, accumulator 1 and 2 are returned 1,2-3: 2-3 do not exist -> ok, accumulator 1 is returned 1-3: 1-3 do not exist -> ok, no accumulators are returned
 */
public class AccumulatorSelection
{
	/*
	 * The accumulatorIDFirst parameter contains the first accumulator identity in a sequence of usage accumulators or the only accumulator identity if a single usage accumulator shall be obtained.
	 */
	@Air(Mandatory = true, Range = "1:2147483647")
	public int accumulatorIDFirst;

	/*
	 * The accumulatorIDLast parameter contains the last accumulator identity in a sequence of usage accumulators.
	 */
	@Air(Range = "1:2147483647")
	public Integer accumulatorIDLast;

}
